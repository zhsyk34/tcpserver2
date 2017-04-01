package com.dnk.smart.logging;

import io.netty.handler.logging.LogLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.util.EnumMap;

import static com.dnk.smart.logging.Category.*;
import static com.dnk.smart.logging.Module.*;
import static io.netty.handler.logging.LogLevel.*;

@RequiredArgsConstructor
@Getter
public enum LoggerFactory {
    TCP_EVENT(TCP, EVENT, ERROR),
    TCP_RECEIVE(TCP, RECEIVE, DEBUG),
    TCP_SEND(TCP, SEND, DEBUG),
    TCP_EXECUTE(TCP, EXECUTE, WARN),
    TCP_OTHER(TCP, OTHER, DEBUG),

    REDIS_RECEIVE(REDIS, RECEIVE, INFO),
    REDIS_SEND(REDIS, SEND, INFO),

    UDP_EVENT(UDP, EVENT, ERROR),
    UDP_RECEIVE(UDP, RECEIVE, DEBUG),
    UDP_SEND(UDP, SEND, DEBUG);

    private static final EnumMap<LoggerFactory, Logger> LOGGERS = new EnumMap<>(LoggerFactory.class);

    static {
        for (LoggerFactory factory : values()) {
            String name = (factory.getModule() + "." + factory.getCategory()).replace("_", ".").toLowerCase();
            LOGGERS.put(factory, org.slf4j.LoggerFactory.getLogger(name));
        }
    }

    @NonNull
    private final Module module;
    @NonNull
    private final Category category;
    @NonNull
    private final LogLevel level;

    public void logger(@NonNull String msg) {
        LoggerExecutor.append(LOGGERS.get(this), level, Content.from(msg));
    }

    public void logger(@NonNull String format, Object arg) {
        LoggerExecutor.append(LOGGERS.get(this), level, Content.from(format, arg));
    }

    public void logger(@NonNull String format, Object arg1, Object arg2) {
        LoggerExecutor.append(LOGGERS.get(this), level, Content.from(format, arg1, arg2));
    }

    public void logger(@NonNull String format, Object... arguments) {
        LoggerExecutor.append(LOGGERS.get(this), level, Content.from(format, arguments));
    }

    public void logger(@NonNull String msg, Throwable t) {
        LoggerExecutor.append(LOGGERS.get(this), level, Content.from(msg, t));
    }

}
