package com.dnk.smart.tcp.cache;

import com.dnk.smart.dict.redis.cache.Command;
import com.dnk.smart.dict.tcp.LoginInfo;
import com.dnk.smart.dict.tcp.State;
import com.dnk.smart.dict.tcp.Verifier;
import io.netty.channel.Channel;
import lombok.NonNull;

public interface ChannelCacheAccessor {

    String id(@NonNull Channel channel);

    String ip(@NonNull Channel channel);

    int port(@NonNull Channel channel);

    LoginInfo info(@NonNull Channel channel);

    void info(@NonNull Channel channel, @NonNull LoginInfo loginInfo);

    State state(@NonNull Channel channel);

    void state(@NonNull Channel channel, @NonNull State state);

    Verifier verifier(@NonNull Channel channel);

    /**
     * @param channel can be null for clean
     */
    void verifier(@NonNull Channel channel, Verifier verifier);

    Command command(@NonNull Channel channel);

    /**
     * @param channel can be null when request finished or timeout
     */
    void command(@NonNull Channel channel, Command command);

}
