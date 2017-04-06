package com.dnk.smart.udp.session;

import com.alibaba.fastjson.JSONObject;
import com.dnk.smart.dict.Action;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.Key;
import com.dnk.smart.dict.Result;
import com.dnk.smart.dict.udp.UdpInfo;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.udp.UdpServer;
import com.dnk.smart.util.TimeUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.NonNull;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public final class DefaultUdpSessionController implements UdpSessionController {

    private static final Map<String, UdpInfo> UDP_SESSIONS = new ConcurrentHashMap<>();

    @Resource
    private CacheAccessor cacheAccessor;

    @Override
    public Channel channel() {
        return UdpServer.getChannel();
    }

    @Override
    public void receive(@NonNull UdpInfo info) {
        UDP_SESSIONS.put(info.getSn(), info);
        cacheAccessor.reportUdpSession(info.toUdpSession());
    }

    @Override
    public void response(@NonNull InetSocketAddress target) {
        JSONObject json = new JSONObject();
        json.put(Key.RESULT.getName(), Result.OK.getName());
        send(target, json);
    }

    @Override
    public UdpInfo info(@NonNull String sn) {
        UdpInfo udpInfo = Optional.ofNullable(UDP_SESSIONS.get(sn)).orElse(Optional.ofNullable(cacheAccessor.getUdpSession(sn)).map(UdpInfo::from).orElse(null));
        return Optional.ofNullable(udpInfo).filter(info -> !TimeUtils.timeout(info.getHappen(), Config.UDP_INFO_EXPIRE)).orElse(null);
    }

    @Override
    public void awake(@NonNull InetSocketAddress target) {
        JSONObject json = new JSONObject();
        json.put(Key.ACTION.getName(), Action.LOGIN_INFORM.getName());
        send(target, json);
    }

    @Override
    public void awake(@NonNull String ip, int port) {
        this.awake(new InetSocketAddress(ip, port));
    }

    @Override
    public void awake(@NonNull String sn) {
        Optional.ofNullable(this.info(sn)).ifPresent(info -> this.awake(info.getIp(), info.getPort()));
    }

    private void send(InetSocketAddress target, JSONObject json) {
        Optional.of(this.channel()).ifPresent(channel -> channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(json.toString().getBytes(CharsetUtil.UTF_8)), target)));
    }
}
