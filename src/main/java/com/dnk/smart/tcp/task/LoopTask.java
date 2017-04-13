package com.dnk.smart.tcp.task;

import com.dnk.smart.util.ThreadUtils;

@FunctionalInterface
public interface LoopTask {

    void run();

    @SuppressWarnings("InfiniteLoopStatement")
    default Runnable executor() {
        while (true) {
            run();
            ThreadUtils.await(2 * 1000);
        }
    }
}
