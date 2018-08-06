package io.biteeniu.redis.delay.queue.utils;

import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;

/**
 * Redis工具类
 * @author luzhanghong
 * @date 2018-07-30 11:34
 */
public final class RedisHelper {

    private RedisHelper() {}

    /**
     * JOB_POOL: 存放延迟任务的元信息
     */
    public final static String JOB_POOL = "job-pool";
    /**
     * JOB_DELAY_BUCKET: 存放延迟任务的ID：Redis SortSet结构，用于对任务进行延时
     */
    public final static String JOB_DELAY_BUCKET = "job-delay-bucket";
    private static final String EX = "EX";  // 设置键的过期时间为second秒。SET key value EX second效果等同于SETEX key second value。
    private static final String PX = "PX";  // 设置键的过期时间为millisecond毫秒。SET key value PX millisecond效果等同于PSETEX key millisecond value。
    private static final String NX = "NX";  // 只在键不存在时，才对键进行设置操作。SET key value NX 效果等同于SETNX key value。
    private static final String XX = "XX";  // 只在键已经存在时，才对键进行设置操作。
    private static final String LOCK_SUCCESS = "OK";  // OK表示加锁成功
    private static final Long UNLOCK_SUCCESS = 1L;    // 1表示解锁成功
    private static final ThreadLocal<String> uuid = new ThreadLocal<>();  // 将客户端的ID（ClientId）设置成Thread-Local变量，保证每个线程都有自己独享的唯一ClientId
    // 解锁的Lua脚本代码
    private static final String UNLOCK_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    // UNLOCK_SCRIPT脚本代码的SHA1值，使用EVALSHA命令来传脚本的SHA1值，避免每次都传输整个脚本，浪费带宽
    private static final String UNLOCK_SCRIPT_SHA1 = "e9f69f2beb755be68b5e456ee2ce9aadfbc4ebf4";


    /**
     * 对某个Key加锁（Redis分布式锁）
     * @param jedis Jedis
     * @param key Key
     * @return true-加锁成功；false-加锁失败
     */
    public static boolean lock(Jedis jedis, String key) {
        return LOCK_SUCCESS.equals(jedis.set(key, getThreadLocalClientId(), NX, EX, 30L));
    }

    /**
     * 对某个Key解锁（Redis分布式锁）
     * @param jedis Jedis
     * @param key Key
     * @return true-解锁成功；false-解锁失败
     */
    public static boolean unlock(Jedis jedis, String key) {
        return UNLOCK_SUCCESS.equals(jedis.evalsha(UNLOCK_SCRIPT_SHA1, Collections.singletonList(key), Collections.singletonList(getThreadLocalClientId())));
    }

    /**
     * 为每个线程设置一个Thread-Local的clientId（Redis分布式锁）
     * @return clientId
     */
    private static String getThreadLocalClientId() {
        String clientId = uuid.get();
        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            uuid.set(clientId);
        }
        return clientId;
    }

}
