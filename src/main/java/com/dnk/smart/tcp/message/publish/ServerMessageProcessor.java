package com.dnk.smart.tcp.message.publish;

import com.dnk.smart.dict.redis.cache.Command;
import lombok.NonNull;

/**
 * 与其它tcpServer通讯
 */
interface ServerMessageProcessor {

    /**
     * 网关登录成功后发布通告:
     * 1.以释放其它服务器可能存在着过期的连接或重复连接(恶意攻击?)
     * 2.停止唤醒任务
     *
     * @param sn       网关序列号
     * @param serverId 当前网关登录的服务器编号
     */
    void publishGatewayLogin(@NonNull String sn, @NonNull String serverId);

    /**
     * 网关接收到app请求时发布通知
     * app连接的服务器与网关登录服务器可能不同
     * 请求结果做同样处理
     *
     * @param sn 网关序列号
     * @see #publishAppCommandResult(String, String)
     */
    void publishAppCommandRequest(@NonNull String sn);

    /**
     * app请求指令处理结果发布
     *
     * @param appId  客户端标识即连接的channelId
     * @param result 响应结果
     * @see #publishAppCommandRequest(String)
     */
    void publishAppCommandResult(@NonNull String appId, @NonNull String result);

    /**
     * 统一回复响应失败的请求指令
     *
     * @param command 当前处理的请求指令
     * @see #publishAppCommandResult(String, String)
     * @see WebMessageProcessor#publishWebCommandResult(String, String, boolean)
     */
    void publishCommandAborted(@NonNull Command command);

}
