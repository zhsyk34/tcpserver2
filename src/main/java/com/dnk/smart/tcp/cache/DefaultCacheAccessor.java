package com.dnk.smart.tcp.cache;

import com.alibaba.fastjson.JSON;
import com.dnk.smart.dict.Config;
import com.dnk.smart.dict.redis.cache.Command;
import com.dnk.smart.dict.redis.cache.TcpSession;
import com.dnk.smart.dict.redis.cache.UdpSession;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dnk.smart.dict.redis.RedisKey.*;

@Service
public final class DefaultCacheAccessor extends SimpleChannelCacheAccessor implements CacheAccessor {

    @Resource
    private RedisAccessor redisAccessor;

    @Override
    public void registerTcpSession(@NonNull TcpSession tcpSession) {
        redisAccessor.put(TCP_SESSION, tcpSession.getSn(), JSON.toJSONString(tcpSession));
    }

    @Override
    public TcpSession getTcpSession(@NonNull String sn) {
        return JSON.parseObject(redisAccessor.get(TCP_SESSION, sn), TcpSession.class);
    }

    @Override
    public void unregisterTcpSession(@NonNull String sn) {
        redisAccessor.remove(TCP_SESSION, sn);
    }

    @Override
    public void reportUdpSession(@NonNull UdpSession udpSession) {
        redisAccessor.put(TCP_UDP_SESSION, udpSession.getSn(), JSON.toJSONString(udpSession));
    }

    @Override
    public UdpSession getUdpSession(@NonNull String sn) {
        return JSON.parseObject(redisAccessor.get(TCP_UDP_SESSION, sn), UdpSession.class);
    }

    @Override
    public void reportServerStatus(@NonNull String serverId) {
        redisAccessor.put(TCP_SERVER, serverId, Long.toString(System.currentTimeMillis()));
    }

    @Override
    public void reportServerStatus() {
        this.reportServerStatus(Config.TCP_SERVER_ID);
    }

    @Override
    public void shareAppCommand(@NonNull String appId, @NonNull Command command) {
        redisAccessor.enqueue(TCP_COMMAND, JSON.toJSONString(command));
    }

    @Override
    public Command getFirstCommand(@NonNull String sn) {
        return JSON.parseObject(redisAccessor.dequeue(TCP_COMMAND), Command.class);
    }

    @Override
    public List<Command> getAllCommand(@NonNull String sn) {
        return Optional.ofNullable(redisAccessor.dequeueAll(TCP_COMMAND)).map(Collection::stream).orElse(Stream.empty()).map(s -> JSON.parseObject(s, Command.class)).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
