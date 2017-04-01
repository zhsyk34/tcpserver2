package com.dnk.smart.tcp.message.subscribe;

import com.dnk.smart.dict.redis.RedisChannel;
import lombok.NonNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.Topic;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractRedisListener implements RedisListener, MessageListener {

    @NonNull
    private final Collection<String> names;

    private AbstractRedisListener(@NonNull Collection<String> names) {
        this.names = names;
    }

    AbstractRedisListener(@NonNull RedisChannel... redisChannels) {
        this(Arrays.stream(redisChannels).map(RedisChannel::channel).collect(Collectors.toList()));
    }

    @Override
    public Collection<Topic> topics() {
        return names.stream().map(ChannelTopic::new).collect(Collectors.toList());
    }

    @Override
    public MessageListener listener() {
        return this;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
//        logger.info("receive message: {}, on getGatewayChannel: {}", message, channel);

        RedisChannel redisChannel = RedisChannel.from(channel);
        if (redisChannel != null) {
            handleMessage(redisChannel, message.getBody());
        }
    }

    abstract void handleMessage(RedisChannel redisChannel, byte[] content);

}
