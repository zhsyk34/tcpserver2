package com.dnk.smart.tcp.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dnk.smart.dict.Action;
import com.dnk.smart.dict.Key;
import com.dnk.smart.dict.Result;
import com.dnk.smart.dict.redis.cache.Command;
import com.dnk.smart.dict.tcp.LoginInfo;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.awake.AwakeService;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.tcp.command.CommandProcessor;
import com.dnk.smart.tcp.message.direct.ClientMessageProcessor;
import com.dnk.smart.tcp.message.publish.ChannelMessageProcessor;
import com.dnk.smart.tcp.state.StateController;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 处理除登录以外的请求:登录验证已在此前的 {@link TcpLoginHandler} 中处理
 * <p>
 * 1.app 指令请求保存至redisServer同时广播通知其它tcpServer
 * 2.gateway
 * 2-1.心跳:直接回复
 * 2-2.推送信息:发布至dBServer
 * 2-3.版本请求:发布至dBServer并订阅回复
 * 2-4.指令响应:根据Command类型广播 ==> 并尝试继续执行任务
 */
@Component
@ChannelHandler.Sharable
final class TcpMessageHandler extends ChannelInboundHandlerAdapter {
    @Resource
    private CacheAccessor dataAccessor;
    @Resource
    private StateController stateController;
    @Resource
    private ClientMessageProcessor clientMessageProcessor;
    @Resource
    private ChannelMessageProcessor channelMessageProcessor;
    @Resource
    private CommandProcessor commandProcessor;
    @Resource
    private AwakeService awakeService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof String)) {
            return;
        }
        Channel channel = ctx.channel();
        String command = (String) msg;

        LoggerFactory.TCP_RECEIVE.logger("接收到登录后的终端请求:{}", command);

        JSONObject json = JSON.parseObject(command);
        String actionValue = json.getString(Key.ACTION.getName());
        Action action = Action.from(actionValue);
        Result result = Result.from(json.getString(Key.RESULT.getName()));

        LoginInfo info = dataAccessor.info(channel);
        String sn = info.getSn();

        switch (info.getDevice()) {
            case APP:
                if (StringUtils.hasText(actionValue)) {
                    dataAccessor.submitCommand(sn, Command.of(null, dataAccessor.id(channel), command, 0));

                    if (dataAccessor.getTcpSession(sn) == null) {
                        awakeService.append(sn);//直接唤醒
                    }

                    //总是广播
                    channelMessageProcessor.publishAppCommandRequest(sn);
                }
                break;
            case GATEWAY:
                //1.心跳
                if (action == Action.HEART_BEAT) {
                    LoggerFactory.TCP_RECEIVE.logger("网关[{}] 发送心跳", sn);
                    clientMessageProcessor.responseHeartbeat(channel);
                    return;
                }

                //2.推送
                if (action != null && action.getType() == 3) {
                    LoggerFactory.TCP_RECEIVE.logger("网关[{}] 推送数据:{}", sn, command);
                    channelMessageProcessor.publishPushMessage(sn, command);
                    return;
                }

                //3.版本
                if (action == Action.GET_VERSION) {
                    LoggerFactory.TCP_RECEIVE.logger("网关[{}]请求版本信息", sn);
                    channelMessageProcessor.publishForGatewayVersion(sn);
                    return;
                }

                //4.响应请求
                if (result != null) {
                    LoggerFactory.TCP_RECEIVE.logger("接收到网关[{}]的指令处理结果", sn);

                    Command current = dataAccessor.command(channel);
                    if (current == null) {
                        //normally it won't happen
                        stateController.close(channel);
                        return;
                    }
                    if (StringUtils.hasText(current.getId())) {
                        channelMessageProcessor.publishWebCommandResult(current.getTerminalId(), current.getId(), result == Result.OK);
                    } else {
                        channelMessageProcessor.publishAppCommandResult(current.getTerminalId(), command);
                    }

                    commandProcessor.restart(channel);
                }
                break;
            default:
                break;
        }
    }
}
