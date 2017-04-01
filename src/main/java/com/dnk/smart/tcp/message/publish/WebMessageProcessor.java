package com.dnk.smart.tcp.message.publish;

import lombok.NonNull;

/**
 * 与webServer通讯
 */
interface WebMessageProcessor {

    /**
     * web请求指令处理结果响应
     *
     * @param webServerId webServer的编号
     * @param messageId   指令在webServer中保存的序列号
     * @param result      处理结果
     */
    void publishWebCommandResult(@NonNull String webServerId, @NonNull String messageId, boolean result);
}
