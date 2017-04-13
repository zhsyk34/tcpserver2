package com.dnk.smart.dict.redis;

import com.dnk.smart.dict.redis.cache.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * redis缓存的key值定义及数据内容
 */
@RequiredArgsConstructor
@Getter
public enum RedisKey {
    /*--------------------------以下在tcpServer中处理--------------------------*/
    TCP_SERVER(TcpServer.class),
    TCP_SESSION(TcpSession.class),
    TCP_UDP_SESSION(UdpSession.class),
//    TCP_COMMAND(Command.class),

    /*--------------------------以下在webServer/udpServer中处理--------------------------*/
    UDP_COMMAND(Command.class),
    WEB_SERVER(WebServer.class),
    UDP_SESSION(UdpSession.class);

    @NonNull
    private final Class<?> clazz;

    public final String key() {
        return this.name().replace("_", ":").toLowerCase();
    }
}
