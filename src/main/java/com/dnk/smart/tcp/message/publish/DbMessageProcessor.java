package com.dnk.smart.tcp.message.publish;

import lombok.NonNull;

/**
 * 与dbServer通讯
 */
interface DbMessageProcessor {

    /**
     * 网关登录成功后请求端口分配
     *
     * @param ip    登录ip
     * @param sn    网关序列号
     * @param apply 网关预请求端口(上次分配的端口,首次使用默认端口50000)
     */
    void publishForAllocateUdpPort(@NonNull String ip, @NonNull String sn, int apply);

    /**
     * 网关请求最新版本信息
     *
     * @param sn 网关序列号
     */
    void publishForGatewayVersion(@NonNull String sn);

    /**
     * 网关主动上报的报警等推送信息
     *
     * @param sn      网关序列号
     * @param message 上报的信息
     */
    void publishPushMessage(@NonNull String sn, @NonNull String message);
}
