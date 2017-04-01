package com.dnk.smart.udp.session;

import com.dnk.smart.dict.udp.UdpInfo;
import io.netty.channel.Channel;
import lombok.NonNull;

import java.net.InetSocketAddress;

/**
 * 记录udp心跳信息并保存到redisServer上
 * 发送信息唤醒网关
 */
public interface UdpSessionController {

    Channel channel();

    void receive(@NonNull UdpInfo info);

    void response(@NonNull InetSocketAddress target);

    UdpInfo info(@NonNull String sn);

    void awake(@NonNull InetSocketAddress target);

    void awake(@NonNull String ip, int port);

    void awake(@NonNull String sn);

}
