package com.dnk.smart.tcp.awake;

import com.dnk.smart.dict.Config;
import com.dnk.smart.logging.LoggerFactory;
import com.dnk.smart.tcp.command.CommandProcessor;
import com.dnk.smart.tcp.task.LoopTask;
import com.dnk.smart.udp.session.UdpSessionController;
import com.dnk.smart.util.TimeUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class SimpleAwakeService implements AwakeService {

    private static final Map<String, Record> TASKS = new ConcurrentHashMap<>();

    @Resource
    private UdpSessionController udpSessionController;
    @Resource
    private CommandProcessor commandProcessor;

    @Override
    public void append(@NonNull String sn) {
        LoggerFactory.REDIS_RECEIVE.logger("尝试唤醒网关[{}]", sn);
        TASKS.put(sn, Record.instance());
    }

    @Override
    public void cancel(@NonNull String sn) {
        LoggerFactory.REDIS_RECEIVE.logger("取消唤醒网关[{}]", sn);
        TASKS.remove(sn);
    }

    @Override
    public LoopTask monitor() {
        return () -> {
            for (Map.Entry<String, Record> entry : TASKS.entrySet()) {
                String sn = entry.getKey();
                Record record = entry.getValue();

                if (record.count > Config.GATEWAY_AWAKE_TIME) {
                    TASKS.remove(sn);
                    //this means the gateway can't awake this time,then clean all command request
                    LoggerFactory.UDP_EVENT.logger("唤醒网关[{}]失败,清空消息队列", sn);
                    commandProcessor.clean(sn);
                    continue;
                }

                if (TimeUtils.timeout(record.last, Config.GATEWAY_AWAKE_STEP)) {
                    record.count++;
                    record.last = System.currentTimeMillis();
                    udpSessionController.awake(sn);
                }
            }
        };
    }

    @NoArgsConstructor(staticName = "instance")
    @Getter
    @Setter
    private static class Record {
        private int count;//尝试次数
        private long last;//上次执行时间
    }
}
