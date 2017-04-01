package com.dnk.smart.tcp.cache;

import com.dnk.smart.dict.redis.RedisKey;

import java.util.List;

interface RedisAccessor {

    void enqueue(String key, String value);

    void enqueue(RedisKey redisKey, String value);

    String dequeue(String key);

    String dequeue(RedisKey redisKey);

    /**
     * unsafe in thread
     */
    List<String> dequeueAll(String key);

    List<String> dequeueAll(RedisKey redisKey);

    void put(String key, String hashKey, String value);

    void put(RedisKey redisKey, String hashKey, String value);

    String get(String key, String hashKey);

    String get(RedisKey redisKey, String hashKey);

    long remove(String key, String hashKey);

    long remove(RedisKey redisKey, String hashKey);
}
