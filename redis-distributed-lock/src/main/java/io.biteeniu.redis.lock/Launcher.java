package io.biteeniu.redis.lock;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 程序启动类
 * @author luzhanghong
 * @date 2018-07-25 17:02
 */
public class Launcher {

    private final static Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    /**
     * 程序启动入口方法
     * @param args args
     */
    public static void main(String[] args) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, "10.200.0.206");

        // 测试获取分布式锁——错误的示例1
        // testLockWithWrongWay1(jedisPool.getResource());
        // 测试获取分布式锁——正确的姿势——锁的可重入性测试
        // testReentrantLockWithCorrectWay(jedisPool.getResource());
        // 测试正确姿势下的加锁解锁操作
        testLockAndUnlockWithCorrectWay(jedisPool);
    }

    /**
     * 获取分布式锁——错误的示例1的测试方法
     * @param jedis Jedis
     */
    private static void testLockWithWrongWay1(Jedis jedis) {
        String key = "distributed-lock";  // 加锁的key
        String clientId = RedisLockHelper.getThreadLocalClientId();  // 客户端ID
        int expireTime = 20;  // 锁的超时时间设置为20秒
        LOGGER.info("Client[{}] now try to get lock.", clientId);
        while (true) {
            boolean lockResult = RedisLockHelper.lockWithWrongWay1(jedis, key, expireTime);
            if (lockResult) {
                LOGGER.info("Client[{}] get lock success.", clientId);
            } else {
                LOGGER.warn("Client[{}] get lock failed.", clientId);
            }
            sleep(1000);  // 客户端每隔1秒尝试获取一次锁
        }
    }

    /**
     * 获取分布式锁——正确的姿势——锁的可重入性测试
     * @param jedis Jedis
     */
    private static void testReentrantLockWithCorrectWay(Jedis jedis) {
        String key = "distributed-lock";  // 加锁的key
        String clientId = RedisLockHelper.getThreadLocalClientId();  // 客户端ID
        int expireTime = 5;  // 锁的超时时间设置为5秒
        LOGGER.info("Client[{}] now try to get lock.", clientId);
        while (true) {
            boolean lockResult = RedisLockHelper.lock(jedis, key, expireTime);
            if (lockResult) {
                LOGGER.info("Client[{}] get lock success.", clientId);
            } else {
                LOGGER.warn("Client[{}] get lock failed.", clientId);
            }
            sleep(2000);  // 同一个客户端每隔2秒尝试获取一次锁
        }
    }

    /**
     * 测试正确姿势下的加锁解锁操作
     * @param jedisPool JedisPool
     */
    private static void testLockAndUnlockWithCorrectWay(JedisPool jedisPool) {
        // 开启两个线程来处理业务逻辑，两个线程都需要现获取到分布式锁才能处理业务
        Jedis jedis1 = jedisPool.getResource();
        new Thread(() -> {
            while (true) {
                handleBusinessLogic(jedis1);
            }
        }).start();
        Jedis jedis2 = jedisPool.getResource();
        new Thread(() -> {
            while (true) {
                handleBusinessLogic(jedis2);
            }
        }).start();
    }

    /**
     * 模拟处理业务逻辑测试：加锁-处理业务-解锁
     * @param jedis Jedis
     */
    private static void handleBusinessLogic(Jedis jedis) {
        String key = "distributed-lock";  // 加锁的key
        String clientId = RedisLockHelper.getThreadLocalClientId();  // 客户端ID
        int expireTime = 5;  // 锁的超时时间设置为5秒
        if (RedisLockHelper.lock(jedis, key, expireTime)) {
            LOGGER.info("Client[{}] get lock ok.", clientId);
            // 加锁成功后，执行业务逻辑：这里假设业务逻辑处理需要2秒的时间
            sleep(2000L);
            // 业务逻辑处理完毕，执行解锁操作
            RedisLockHelper.unlock(jedis, key);
            LOGGER.info("Client[{}] handle business ok and release lock ok.", clientId);
            // 业务处理完毕：休眠600ms
            sleep(600L);
        } else {
            LOGGER.warn("Client[{}] get lock failed.", clientId);
            // 获取锁失败后：休眠500ms再尝试获取锁
            sleep(500L);
        }
    }

    /**
     * 线程休眠
     * @param millis 休眠时间，单位毫秒
     */
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
