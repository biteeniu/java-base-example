package io.biteeniu.redis.delay.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Iterator;
import java.util.Set;

/**
 * 消息消费者
 * @author luzhanghong
 * @date 2018-07-19 15:32
 */
public class Consumer implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Consumer.class);
    private final Jedis jedis;

    public Consumer(JedisPool jedisPool) {
        this.jedis = jedisPool.getResource();
    }

    @Override
    public void run() {
        synchronized (this) {
            while (true) {
                long timestamp = System.currentTimeMillis();
                Set<String> jobs = jedis.zrangeByScore("JOB_BUCKET", 0, timestamp, 0, 5);
                if (jobs.isEmpty()) {
                    // 没有任务的时候休眠100ms，避免
                    try {
                        this.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                Iterator<String> iterator = jobs.iterator();
                while (iterator.hasNext()) {
                    // 在这里处理任务
                    String id = iterator.next();
                    LOGGER.info("Consumer received job: {}  -- delete ...", id);
                    jedis.zrem("JOB_BUCKET", id);
                }
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
