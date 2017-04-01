package com.dnk.smart.logging;

/**
 * 日志分类
 */
enum Category {
    /**
     * 会话事件(如连接创建,登录验证等的状态变化)
     */
    EVENT,

    /**
     * 收信息
     */
    RECEIVE,

    /**
     * 发信息
     */
    SEND,

    /**
     * 执行指令或扫描任务等
     */
    EXECUTE,

    /**
     * 其它
     */
    OTHER
}
