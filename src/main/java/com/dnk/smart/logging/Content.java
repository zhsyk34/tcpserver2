package com.dnk.smart.logging;

import lombok.*;
import lombok.experimental.Accessors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Accessors(chain = true)
final class Content {
    private String msg;
    private String format;
    private Object arg;
    private Object arg1;
    private Object arg2;
    private Object[] arguments;
    private Throwable throwable;

    static Content from(@NonNull String msg) {
        return new Content().setMsg(msg);
    }

    static Content from(@NonNull String format, Object arg) {
        return new Content().setFormat(format).setArg(arg);
    }

    static Content from(String format, Object arg1, Object arg2) {
        return new Content().setFormat(format).setArg1(arg1).setArg2(arg2);
    }

    static Content from(String format, Object... arguments) {
        return new Content().setFormat(format).setArguments(arguments);
    }

    static Content from(String msg, Throwable t) {
        return new Content().setMsg(msg).setThrowable(t);
    }
}