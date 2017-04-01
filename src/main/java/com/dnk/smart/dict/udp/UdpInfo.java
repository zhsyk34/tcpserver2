package com.dnk.smart.dict.udp;

import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.cache.UdpSession;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.net.InetSocketAddress;

@RequiredArgsConstructor(staticName = "of")
@Getter
public final class UdpInfo {
    @NonNull
    private final String sn;
    @NonNull
    private final String ip;
    private final int port;
    @NonNull
    private final String version;
    private final long happen;

    public static UdpInfo from(@NonNull String sn, @NonNull InetSocketAddress address, @NonNull String version) {
        String ip = address.getAddress().getHostAddress();
        int port = address.getPort();
        return UdpInfo.of(sn, ip, port, version, System.currentTimeMillis());
    }

    public static UdpInfo from(@NonNull UdpSession udpSession) {
        return UdpInfo.of(udpSession.getSn(), udpSession.getIp(), udpSession.getPort(), udpSession.getVersion(), udpSession.getHappen());
    }

    public UdpSession toUdpSession() {
        return UdpSession.of(Config.TCP_SERVER_ID, sn, ip, port, version, happen);
    }
}
