package com.dnk.smart.tcp.task;

import com.dnk.smart.dict.Config;
import com.dnk.smart.tcp.awake.AwakeService;
import com.dnk.smart.tcp.cache.CacheAccessor;
import com.dnk.smart.tcp.session.SessionRegistry;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public final class TaskServer {
    @Resource
    private SessionRegistry sessionRegistry;
    @Resource
    private AwakeService awakeService;
    @Resource
    private CacheAccessor accessor;

    @Resource
    private TaskExecutor taskExecutor;

    /**
     * session timeout and command timeout
     * awake gateway
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @PostConstruct
    public void startup() {
        List<LoopTask> tasks = new ArrayList<>();
        //session && command timeout
        tasks.addAll(sessionRegistry.monitor());
        //awake gateway
        tasks.add(awakeService.monitor());

        tasks.forEach(task -> taskExecutor.execute(task::executor));
    }

    /**
     * 定时上报tcpServer状态
     */
    public void report() {
        accessor.reportServerStatus(Config.TCP_SERVER_ID);
    }

}
