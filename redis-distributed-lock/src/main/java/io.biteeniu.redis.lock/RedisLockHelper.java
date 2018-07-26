package io.biteeniu.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Collections;

/**
 * @author luzhanghong
 * @date 2018-07-25 17:04
 */
public final class RedisLockHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisLockHelper.class);
    private static final String LOCK_SUCCESS = "OK";  // OK表示加锁成功
    private static final Long UNLOCK_SUCCESS = 1L;    // 1表示解锁成功
    private static final String EX = "EX";  // 设置键的过期时间为second秒。SET key value EX second效果等同于SETEX key second value。
    private static final String PX = "PX";  // 设置键的过期时间为millisecond毫秒。SET key value PX millisecond效果等同于PSETEX key millisecond value。
    private static final String NX = "NX";  // 只在键不存在时，才对键进行设置操作。SET key value NX 效果等同于SETNX key value。
    private static final String XX = "XX";  // 只在键已经存在时，才对键进行设置操作。

    private RedisLockHelper() {}

    /**
     * 获取分布式锁——错误的示例1
     * @param jedis Jedis实例
     * @param key 用key来当锁，因为key是唯一的
     * @param clientId 执行加锁操作的客户端（或线程）的ID，必须保证唯一性
     * @param expireTime 锁的超时时间，单位秒——超过此时间未解锁则Redis会删除锁
     * @return true-加锁成功；false-加锁失败
     */
    public static boolean lockWithWrongWay1(Jedis jedis, String key, String clientId, int expireTime) {
        Long result = jedis.setnx(key, clientId);
        if (result == 1) {
            LOGGER.info("Client[{}] execute SETNX ok.", clientId);
            // 在执行EXPIRE指令之前，延时10秒钟，方便我们手动杀掉加锁的客户端进程（模拟客户端崩溃情况）
            sleep(10000);
            // 若在执行EXPIRE指令之前，该客户端突然崩溃，则无法设置过期时间，将发生死锁情况
            return jedis.expire(key, expireTime) == 1;
        }
        return false;
    }

    /**
     * 获取分布式锁——错误的示例2
     * @param jedis Jedis实例
     * @param key 用key来当锁，因为key是唯一的
     * @param expireTime 锁的超时时间，单位秒——超过此时间未解锁则Redis会删除锁
     * @return true-加锁成功；false-加锁失败
     */
    public static boolean lockWithWrongWay2(Jedis jedis, String key, int expireTime) {
        long expires = System.currentTimeMillis() + (expireTime * 1000L);
        // 若当前锁不存在，则SETNX可以设置成功（成功加锁，将过期时间作为锁的value值），返回1
        if (1 == jedis.setnx(key, String.valueOf(expires))) {
            return true;
        }
        // 若当前锁存在，则获取锁的过期时间
        String value = jedis.get(key);
        if (value != null && Long.parseLong(value) < System.currentTimeMillis()) {
            // 锁已经过期，获取上一个锁的过期时间，并设置当前锁的过期时间
            String oldValue = jedis.getSet(key, String.valueOf(expires));
            if (oldValue != null && oldValue.equals(value)) {
                // 考虑多线程并发的情况，只有一个线程的设置值与当前值相同，它才有加锁的权利
                return true;
            }
        }
        return false;
    }

    /**
     * 获取分布式锁——正确的姿势
     * @param jedis Jedis实例
     * @param key 用key来当锁，因为key是唯一的
     * @param clientId 执行加锁操作的客户端（或线程）的ID，必须保证唯一性
     * @param expireTime 锁的超时时间，单位秒——超过此时间未解锁则Redis会删除锁
     * @return true-加锁成功；false-加锁失败
     */
    public static boolean lockWithCorrectWay(Jedis jedis, String key, String clientId, int expireTime) {
        return LOCK_SUCCESS.equals(jedis.set(key, clientId, NX, PX, expireTime*1000L));
    }

    /**
     * 解锁——错误的示例1
     * @param jedis Jedis
     * @param key key
     */
    public static void unlockWithWrongWay1(Jedis jedis, String key) {
        jedis.del(key);
    }

    /**
     * 解锁——错误的示例2
     * @param jedis Jedis
     * @param key key
     * @param clientId 客户端ID，具备唯一性
     */
    public static void unlockWithWrongWay2(Jedis jedis, String key, String clientId) {
        if (clientId.equals(jedis.get(key))) {
            jedis.del(key);
        }
    }

    /**
     * 解锁——正确的姿势
     * @param jedis Jedis
     * @param key key
     * @param clientId 客户端ID，具备唯一性
     */
    public static boolean unlockWithCorrectWay(Jedis jedis, String key, String clientId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(clientId));
        return UNLOCK_SUCCESS.equals(result);
    }

    /**
     * 线程休眠
     * @param millis millis
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
