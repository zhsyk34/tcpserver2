package com.dnk.smart.tcp.task;

import com.dnk.smart.dict.Config;
import com.dnk.smart.tcp.awake.AwakeService;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.tcp.session.SessionRegistry;
import com.dnk.smart.util.ThreadUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public final class TaskServer {
    @Resource
    private SessionRegistry sessionRegistry;
    @Resource
    private AwakeService awakeService;
    @Resource
    private CacheAccessor accessor;

    @SuppressWarnings("InfiniteLoopStatement")
    public void startup() {
        List<LoopTask> loopTasks = loopTasks();
        ExecutorService service = Executors.newFixedThreadPool(loopTasks.size());
        loopTasks.forEach(task -> service.submit(() -> {
            while (true) {
                task.run();
                ThreadUtils.await(1000 * 1);//TODO:TEST
            }
        }));
        service.shutdown();

    }

    private List<LoopTask> loopTasks() {
        List<LoopTask> list = new ArrayList<>();

        //session timeout and command timeout
        List<LoopTask> sessionTasks = sessionRegistry.monitor();

        //awake gateway
        LoopTask awakeTask = awakeService.monitor();

        list.addAll(sessionTasks);
        list.add(awakeTask);

        return list;
    }

    public void report() {
        accessor.reportServerStatus(Config.TCP_SERVER_ID);
    }

}
