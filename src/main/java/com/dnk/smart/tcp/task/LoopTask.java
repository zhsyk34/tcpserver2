package com.dnk.smart.tcp.task;

import com.dnk.smart.util.ThreadUtils;

@FunctionalInterface
public interface LoopTask {

    void run();

    default Runnable executor() {
        while (true) {
            run();
            ThreadUtils.await(2000);
        }
    }
}
