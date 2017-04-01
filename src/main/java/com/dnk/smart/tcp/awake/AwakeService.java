package com.dnk.smart.tcp.awake;

import com.dnk.smart.tcp.task.LoopTask;
import lombok.NonNull;

public interface AwakeService {

    /**
     * 新增唤醒任务
     *
     * @param sn 网关序列号
     */
    void append(@NonNull String sn);

    /**
     * 取消唤醒任务
     *
     * @param sn 网关序列号
     */
    void cancel(@NonNull String sn);

    /**
     * 执行任务
     */
    LoopTask monitor();
}
