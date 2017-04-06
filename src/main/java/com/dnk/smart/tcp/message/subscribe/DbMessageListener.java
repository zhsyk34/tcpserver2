package com.dnk.smart.tcp.message.subscribe;

import com.alibaba.fastjson.JSON;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.RedisChannel;
import com.dnk.smart.dict.redis.channel.GatewayUdpPortAllocateData;
import com.dnk.smart.dict.redis.channel.GatewayVersionResponseData;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.message.direct.ClientMessageProcessor;
import com.dnk.smart.tcp.session.SessionRegistry;
import com.dnk.smart.tcp.state.StateController;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

import static com.dnk.smart.dict.redis.RedisChannel.GATEWAY_UDP_PORT_ALLOCATE;
import static com.dnk.smart.dict.redis.RedisChannel.GATEWAY_VERSION_RESPONSE;

@Service
public final class DbMessageListener extends AbstractRedisListener {

    @Resource
    private SessionRegistry sessionRegistry;
    @Resource
    private ClientMessageProcessor clientMessageProcessor;
    @Resource
    private StateController stateController;

    DbMessageListener() {
        super(GATEWAY_VERSION_RESPONSE, GATEWAY_UDP_PORT_ALLOCATE);
    }

    @Override
    void handleMessage(RedisChannel redisChannel, byte[] content) {
        switch (redisChannel) {
            case GATEWAY_VERSION_RESPONSE:
                GatewayVersionResponseData versionData = JSON.parseObject(content, GATEWAY_VERSION_RESPONSE.getClazz());

                Optional.ofNullable(sessionRegistry.getGatewayChannel(versionData.getSn())).ifPresent(channel -> clientMessageProcessor.responseVersionRequest(channel, versionData.getResult()));
                LoggerFactory.REDIS_RECEIVE.logger("接收到[{}]版本请求回复:{}", versionData.getSn(), versionData.getResult());
                break;
            case GATEWAY_UDP_PORT_ALLOCATE:
                GatewayUdpPortAllocateData portData = JSON.parseObject(content, GATEWAY_UDP_PORT_ALLOCATE.getClazz());

                LoggerFactory.REDIS_RECEIVE.logger("接收到[{}]端口请求回复:{}", portData.getSn(), portData.getAllocated());

                Optional.ofNullable(sessionRegistry.getAcceptChannel(portData.getSn())).ifPresent(channel -> {
                    int allocated = portData.getAllocated();
                    if (allocated < Config.TCP_ALLOT_MIN_UDP_PORT) {
                        stateController.close(channel);
                    } else {
                        stateController.success(channel, allocated);
                    }
                });
                break;
            default:
                break;
        }
    }
}
