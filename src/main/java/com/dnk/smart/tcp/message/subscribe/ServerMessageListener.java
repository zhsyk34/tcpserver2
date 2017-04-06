package com.dnk.smart.tcp.message.subscribe;

import com.alibaba.fastjson.JSON;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.RedisChannel;
import com.dnk.smart.dict.redis.channel.AppCommandRequestData;
import com.dnk.smart.dict.redis.channel.AppCommandResponseData;
import com.dnk.smart.dict.redis.channel.GatewayLoginData;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.awake.AwakeService;
import com.dnk.smart.tcp.command.CommandProcessor;
import com.dnk.smart.tcp.message.direct.ClientMessageProcessor;
import com.dnk.smart.tcp.session.SessionRegistry;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

import static com.dnk.smart.dict.redis.RedisChannel.*;

@Service
public final class ServerMessageListener extends AbstractRedisListener {

    @Resource
    private SessionRegistry sessionRegistry;
    @Resource
    private AwakeService awakeService;
    @Resource
    private ClientMessageProcessor clientMessageProcessor;
    @Resource
    private CommandProcessor commandProcessor;

    ServerMessageListener() {
        super(GATEWAY_LOGIN, APP_COMMAND_REQUEST, APP_COMMAND_RESPONSE);
    }

    @Override
    void handleMessage(RedisChannel redisChannel, byte[] content) {
        switch (redisChannel) {
            case GATEWAY_LOGIN:
                GatewayLoginData loginData = JSON.parseObject(content, GATEWAY_LOGIN.getClazz());
                String sn = loginData.getSn();

                awakeService.cancel(sn);//取消所有的唤醒任务

                //发布tcpServer已在sessionRegistry中重新注册并执行任务
                if (!Config.TCP_SERVER_ID.equals(loginData.getServerId())) {
                    sessionRegistry.closeChannelQuietly(sn);//移除过时/恶意连接
                }

                LoggerFactory.REDIS_RECEIVE.logger("网关[{}]在{}登录", sn, loginData.getServerId());
                break;
            case APP_COMMAND_REQUEST:
                AppCommandRequestData requestData = JSON.parseObject(content, APP_COMMAND_REQUEST.getClazz());

                LoggerFactory.REDIS_RECEIVE.logger("接收到app关于网关[{}]的请求指令", requestData.getSn());

                //唤醒任务已由发布tcpServer执行
                Optional.ofNullable(sessionRegistry.getGatewayChannel(requestData.getSn())).ifPresent(channel -> commandProcessor.startup(channel));
                break;
            case APP_COMMAND_RESPONSE:
                AppCommandResponseData responseData = JSON.parseObject(content, APP_COMMAND_RESPONSE.getClazz());

                LoggerFactory.REDIS_RECEIVE.logger("接收到app的请求响应结果", responseData.getResult());

                Optional.ofNullable(sessionRegistry.getAppChannel(responseData.getAppId())).ifPresent(channel -> clientMessageProcessor.responseAppCommandResult(channel, responseData.getResult()));
                break;
            default:
                break;
        }
    }
}
