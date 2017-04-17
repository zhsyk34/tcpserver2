package com.dnk.smart.tcp.cache;

import com.dnk.smart.dict.redis.cache.Command;
import com.dnk.smart.dict.redis.cache.TcpSession;
import com.dnk.smart.dict.redis.cache.UdpSession;
import lombok.NonNull;

import java.util.List;

/**
 * redis-Server数据读写
 */
public interface RedisCacheAccessor {
    /**
     * 网关登录后登记
     *
     * @param tcpSession 网关tcp连接信息
     */
    void registerTcpSession(@NonNull TcpSession tcpSession);

    /**
     * @param sn 网关序列号
     * @return 获取网关tcp连接信息:判断是否在线
     */
    TcpSession getTcpSession(@NonNull String sn);

    /**
     * 网关下线后注销登记
     *
     * @param sn 网关序列号
     */
    void unregisterTcpSession(@NonNull String sn);

    /**
     * (定时)上报更新网关心跳相关信息
     *
     * @param udpSession 网关udp心跳信息
     */
    void reportUdpSession(@NonNull UdpSession udpSession);

    /**
     * @param sn 网关序列号
     * @return 网关udp心跳信息
     */
    UdpSession getUdpSession(@NonNull String sn);

    /**
     * (定时)上报服务器状态
     *
     * @param serverId 服务器编号
     */
    void reportServerStatus(@NonNull String serverId);

    /**
     * 上报本服务器的状态
     *
     * @see #reportServerStatus(String)
     */
    void reportServerStatus();

    /**
     * 提交请求指令
     *
     * @param sn      网关序列号
     * @param command 请求指令
     */
    void submitCommand(@NonNull String sn, @NonNull Command command);

    /**
     * 获取并移除待处理的第一条请求指令
     *
     * @param sn 网关序列号
     */
    Command getFirstCommand(@NonNull String sn);

    /**
     * 获取并移除所有的待处理请求指令
     *
     * @param sn 网关序列号
     */
    List<Command> getAllCommand(@NonNull String sn);

}
