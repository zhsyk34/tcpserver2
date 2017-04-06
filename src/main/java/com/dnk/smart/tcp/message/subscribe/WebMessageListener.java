package com.dnk.smart.tcp.message.subscribe;

import com.alibaba.fastjson.JSON;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.RedisChannel;
import com.dnk.smart.dict.redis.channel.WebCommandRequestData;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.awake.AwakeService;
import com.dnk.smart.tcp.command.CommandProcessor;
import com.dnk.smart.tcp.session.SessionRegistry;
import io.netty.channel.Channel;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.dnk.smart.dict.redis.RedisChannel.WEB_COMMAND_REQUEST;

@Service
public final class WebMessageListener extends AbstractRedisListener {

    @Resource
    private SessionRegistry sessionRegistry;
    @Resource
    private CommandProcessor commandProcessor;
    @Resource
    private AwakeService awakeService;

    WebMessageListener() {
        super(WEB_COMMAND_REQUEST);
    }

    @Override
    void handleMessage(RedisChannel redisChannel, byte[] content) {
        switch (redisChannel) {
            case WEB_COMMAND_REQUEST:
                WebCommandRequestData data = JSON.parseObject(content, WEB_COMMAND_REQUEST.getClazz());
                //webServer会优先指定网关登录的服务器,否则将随机挑选存活的服务器负责唤醒
                String serverId = data.getServerId();

                LoggerFactory.REDIS_RECEIVE.logger("接收到web关于网关[{}]指令请求", data.getSn());

                if (Config.TCP_SERVER_ID.equals(serverId)) {
                    String sn = data.getSn();

                    //如网关此时已重新登录到其它tcpServer则将做失败(简化)处理
                    //实际上网关登录后会立即尝试执行任务
                    Channel channel = sessionRegistry.getGatewayChannel(sn);
                    if (channel != null) {
                        LoggerFactory.TCP_EXECUTE.logger("网关[{}]开始执行任务", sn);
                        commandProcessor.startup(channel);
                    } else {
                        awakeService.append(sn);
                    }
                }
                break;
            default:
                break;
        }
    }
}
