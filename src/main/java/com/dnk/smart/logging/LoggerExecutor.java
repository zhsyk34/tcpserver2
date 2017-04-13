package com.dnk.smart.logging;

import com.dnk.smart.dict.Config;
import io.netty.handler.logging.LogLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

@Service
final class LoggerExecutor {

    private static final AtomicReference<Queue<Record>> RECORDS = new AtomicReference<>(new LinkedBlockingQueue<>());
    /**
     * TODO
     * not in any particular order!!!
     */
    private static final Field[] CONTENT_FIELDS = Content.class.getDeclaredFields();

    static void append(@NonNull Logger logger, @NonNull LogLevel level, @NonNull Content content) {
        Queue<Record> records = LoggerExecutor.RECORDS.get();
        records.add(Record.of(logger, level, content));
        if (records.size() >= Config.LOGGER_CAPACITY) {
            monitor();
        }

        //invoke(Record.of(logger, level, content));//TODO
    }

    public static void monitor() {
        System.err.println("logging task begin...");
        RECORDS.getAndSet(new LinkedBlockingQueue<>()).forEach(LoggerExecutor::invoke);
    }

    private static void invoke(@NonNull Record record) {
        List<Class<?>> types = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        try {
            for (Field field : CONTENT_FIELDS) {
                field.setAccessible(true);
                Optional.ofNullable(field.get(record.content)).ifPresent(value -> {
                    types.add(field.getType());
                    params.add(value);
                });
            }

            Logger logger = record.logger;
            Method method = logger.getClass().getMethod(record.level.name().toLowerCase(), types.toArray(new Class<?>[types.size()]));
            method.invoke(logger, params.toArray());
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class Record {
        @NonNull
        private final Logger logger;
        @NonNull
        private final LogLevel level;
        @NonNull
        private final Content content;
    }

}
