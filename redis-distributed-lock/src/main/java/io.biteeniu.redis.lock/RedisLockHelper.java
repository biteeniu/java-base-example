package io.biteeniu.redis.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author luzhanghong
 * @date 2018-07-25 17:04
 */
public final class RedisLockHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisLockHelper.class);

    /**
     * 获取分布式锁——错误的示例1
     * @param jedis Jedis实例
     * @param key 用key来当锁，因为key是唯一的
     * @param clientId 执行加锁操作的客户端（或线程）的ID，必须保证唯一性
     * @param expireTime 锁的超时时间，单位秒——超过此时间未解锁则Redis会删除锁
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
     */
    public static boolean lockWithWrongWay2(Jedis jedis, String key, int expireTime) {
        long expires = System.currentTimeMillis() + expireTime;
        Long result = jedis.setnx(key, String.valueOf(expires));
        if (result == 1) {
            LOGGER.info("Client[{}] execute SETNX ok.");
            // 在执行EXPIRE指令之前，延时10秒钟，方便我们手动杀掉加锁的客户端进程（模拟客户端崩溃情况）
            sleep(10000);
            // 若在执行EXPIRE指令之前，该客户端突然崩溃，则无法设置过期时间，将发生死锁情况
            return jedis.expire(key, expireTime) == 1;
        }
        return false;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
