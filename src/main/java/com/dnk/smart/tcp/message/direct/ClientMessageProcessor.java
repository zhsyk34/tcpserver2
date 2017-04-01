package com.dnk.smart.tcp.message.direct;

import io.netty.channel.Channel;
import lombok.NonNull;

/**
 * 与终端 app/gateway 直接通讯
 */
public interface ClientMessageProcessor {

    /*--------------------登录环节的回复--------------------*/

    /**
     * 拒绝错误的登录请求
     */
    void refuseForLogin(@NonNull Channel channel);

    /**
     * 发送登录验证码
     */
    void sendVerificationQuestion(@NonNull Channel channel, @NonNull String question);

    /**
     * 反馈错误的验证码
     */
    void refuseForVerificationAnswer(@NonNull Channel channel);

    /**
     * 登录成功后回复
     */
    void responseAfterLogin(@NonNull Channel channel);


    /*--------------------登录后处理请求的回复--------------------*/

    /**
     * 回复网关(登录后)心跳请求
     */
    void responseHeartbeat(@NonNull Channel channel);

    /**
     * 回复网关(登录后)的版本信息请求
     */
    void responseVersionRequest(@NonNull Channel channel, @NonNull String result);

    /**
     * 回复app请求指令处理结果
     */
    void responseAppCommandResult(@NonNull Channel channel, @NonNull String result);

}
