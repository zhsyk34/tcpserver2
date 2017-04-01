package com.dnk.smart.logging;

import com.dnk.smart.dict.Config;
import io.netty.handler.logging.LogLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

final class LoggerExecutor {

    private static final String RECEIVE_prefix = "";
    private static final String SEND_prefix = "";
    private static final String message_prefix = "";
    /**
     * TODO
     * 待写入日志队列
     */
    private static final LinkedBlockingQueue<Record> RECORDS = new LinkedBlockingQueue<>(Config.LOGGER_CAPACITY);

    static void append(@NonNull Logger logger, @NonNull LogLevel level, @NonNull Content content) {
        Record record = Record.of(logger, level, content);
//        RECORDS.offer(record);
//        if (RECORDS.size() == Config.LOGGER_CAPACITY) {
//            //done!
//        }
        try {
            invoke(record);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * not in any particular order!!!
     */
    private static final Field[] CONTENT_FIELDS = Content.class.getDeclaredFields();

    private static void invoke(@NonNull Record record) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Class<?>> types = new ArrayList<>();
        List<Object> params = new ArrayList<>();

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
    }

    private static void prepare(@NonNull Content content) {

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
