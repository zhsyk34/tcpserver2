package com.dnk.smart.tcp.message.direct;

import com.alibaba.fastjson.JSONObject;
import com.dnk.smart.dict.*;
import com.dnk.smart.tcp.cache.CacheAccessor;
import io.netty.channel.Channel;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public final class SimpleClientMessageProcessor implements ClientMessageProcessor {

    @Resource
    private CacheAccessor cacheAccessor;

    @Override
    public void refuseForLogin(@NonNull Channel channel) {
        JSONObject json = new JSONObject();
        json.put(Key.RESULT.getName(), Result.NO.getName());
        json.put(Key.ERROR_NO.getName(), ErrorCode.PARAMETER.getCode());//pdf文档为:101
        channel.writeAndFlush(json);
    }

    @Override
    public void sendVerificationQuestion(@NonNull Channel channel, @NonNull String question) {
        JSONObject json = new JSONObject();
        json.put(Key.ACTION.getName(), Action.LOGIN_VERIFY.getName());
        json.put(Key.KEY.getName(), question);
        channel.writeAndFlush(json);
    }

    @Override
    public void refuseForVerificationAnswer(@NonNull Channel channel) {
        JSONObject json = new JSONObject();
        json.put(Key.RESULT.getName(), Result.NO.getName());
        json.put(Key.ERROR_NO.getName(), ErrorCode.PARAMETER.getCode()); //pdf文档为:101
        channel.writeAndFlush(json);
    }

    @Override
    public void responseAfterLogin(@NonNull Channel channel) {
        JSONObject json = new JSONObject();
        json.put(Key.RESULT.getName(), Result.OK.getName());

        int allocated = cacheAccessor.info(channel).getAllocated();
        if (allocated >= Config.TCP_ALLOT_MIN_UDP_PORT) {
            json.put(Key.APPLY.getName(), allocated);
        }

        channel.writeAndFlush(json);
    }

    @Override
    public void responseHeartbeat(@NonNull Channel channel) {
        JSONObject json = new JSONObject();
        json.put(Key.RESULT.getName(), Result.OK.getName());
        channel.writeAndFlush(json);
    }

    @Override
    public void responseVersionRequest(@NonNull Channel channel, @NonNull String result) {
        channel.writeAndFlush(result);
    }

    @Override
    public void responseAppCommandResult(@NonNull Channel channel, @NonNull String result) {
        channel.writeAndFlush(result);
    }

}
