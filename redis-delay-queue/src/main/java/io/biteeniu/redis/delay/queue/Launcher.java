package io.biteeniu.redis.delay.queue;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 程序启动类
 * @author luzhanghong
 * @date 2018-07-17 10:02
 */
public class Launcher {

    private final static Logger LOGGER = LoggerFactory.getLogger(Launcher.class);
    private final static String JOB_POOL = "JOB_POOL";
    private final static String JOB_BUCKET = "JOB_BUCKET";
    private final static ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    private static JedisPool jedisPool;
    private static Jedis jedis;

    /**
     * 程序启动入口方法
     * @param args args
     */
    public static void main(String[] args) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(1);
        jedisPool = new JedisPool(poolConfig, "10.200.0.206");
        Thread consumerThread = new Thread(new Consumer(jedisPool));
        consumerThread.setName("consumer-thread");
        consumerThread.start();

        Map<String, String> jobPool = new HashMap<>();
        Map<String, Double> jobBuckets = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            int delay = 10;// + RANDOM.nextInt(21);
            Long expiredAt = System.currentTimeMillis() + (delay * 1000L);
            Job job = new Job("send-email" + i, delay, "biteeniu@gmail.com");
            jobPool.put(job.getId(), job.toJsonString());
            jobBuckets.put(job.getId(), Double.valueOf(expiredAt));
            LOGGER.info("Producer publish job: {}, delay: {} seconds.", job.getId(), job.getDelay());
        }

        Jedis jedis = jedisPool.getResource();
        jedis.getSet("", "");
        jedis.eval("", Collections.singletonList("hello"), Collections.singletonList("hello"));
        jedis.set("", "", "", "", 1);
        jedis.hmset(JOB_POOL, jobPool);
        Transaction transaction = jedis.multi();
        transaction.hmset(JOB_POOL, jobPool);
        transaction.zadd(JOB_BUCKET, jobBuckets);
        transaction.exec();
        jedis.close();

        CacheLoader<String, String> loader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(new Listener())
                .build(loader);
//        for (int i = 1; i <= 10; i++) {
//            cache.put(String.valueOf(i), String.valueOf(i));
//        }
        cache.put("test", "haha");
        cache.refresh("test");
        cache.asMap();
        sleep(20000);
        System.out.println(cache.getUnchecked("test"));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
