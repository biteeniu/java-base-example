package io.biteeniu.redis.lock;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

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
        // 测试获取分布式锁——正确的姿势——测试所的可重入性
        testReentrantLockWithCorrectWay(jedisPool.getResource());
    }

    /**
     * 获取分布式锁——错误的示例1的测试方法
     * @param jedis Jedis
     */
    private static void testLockWithWrongWay1(Jedis jedis) {
        String key = "distributed-lock-key";  // 加锁的key
        String clientId = UUID.randomUUID().toString();  // 客户端ID
        int expireTime = 20;  // 锁的超时时间设置为20秒
        LOGGER.info("Client[{}] now try to get lock.", clientId);
        while (true) {
            boolean lockResult = RedisLockHelper.lockWithWrongWay1(jedis, key, clientId, expireTime);
            if (lockResult) {
                LOGGER.info("Client[{}] get lock success.", clientId);
            } else {
                LOGGER.warn("Client[{}] get lock failed.", clientId);
            }
            sleep(1000);  // 客户端每隔1秒尝试获取一次锁
        }
    }

    /**
     * 获取分布式锁——正确的姿势——测试所的可重入性
     * @param jedis Jedis
     */
    private static void testReentrantLockWithCorrectWay(Jedis jedis) {
        String key = "distributed-lock-key";  // 加锁的key
        String clientId = UUID.randomUUID().toString();  // 客户端ID
        int expireTime = 5;  // 锁的超时时间设置为5秒
        LOGGER.info("Client[{}] now try to get lock.", clientId);
        while (true) {
            boolean lockResult = RedisLockHelper.lockWithCorrectWay(jedis, key, clientId, expireTime);
            if (lockResult) {
                LOGGER.info("Client[{}] get lock success.", clientId);
            } else {
                LOGGER.warn("Client[{}] get lock failed.", clientId);
            }
            sleep(2000);  // 同一个客户端每隔2秒尝试获取一次锁
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
