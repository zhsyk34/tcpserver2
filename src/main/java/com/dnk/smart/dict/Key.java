package com.dnk.smart.dict;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 与终端(app/gateway)通讯时可能存在的key值
 * 只枚举tcpServer需要处理的情况
 */
@RequiredArgsConstructor
@Getter
public enum Key {
    ACTION("action", "指令"),
    RESULT("result", "结果"),
    TYPE("clientType", "设备类型"),
    SN("devSN", "网关SN码"),
    VERSION("appVersionNo", "网关版本"),
    APPLY("UDPPort", "网关请求的udp端口"),
    ERROR_NO("errno", "错误码"),
    KEY("key", "密钥信息"),
    KEYCODE("keyCode", "密钥值");

    @NonNull
    private final String name;
    @NonNull
    private final String description;
}
