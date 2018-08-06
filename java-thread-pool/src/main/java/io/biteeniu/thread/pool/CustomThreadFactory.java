package io.biteeniu.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义的线程工厂
 * @author luzhanghong
 * @date 2018-08-06 14:02
 */
public class CustomThreadFactory implements ThreadFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomThreadFactory.class);
    /**
     * THREAD_POOL_NUMBER：线程池的序号
     */
    private static final AtomicInteger THREAD_POOL_NUMBER = new AtomicInteger(1);
    /**
     * threadNumber：在该线程池中，每创建一个线程，自动生成一个线程序号
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup threadGroup;
    private final String prefix;

    public CustomThreadFactory() {
        SecurityManager securityManager = System.getSecurityManager();
        threadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        // 自定义创建新线程的名称，异常排查的时候方便定位问题
        prefix = "custom-pool-" + THREAD_POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        /**
         * 在这里可以对新创建的线程进行定制化设置
         */
        // 创建新线程的时候，设置新线程的名称
        String newThreadName = prefix + threadNumber.getAndIncrement();
        Thread thread = new Thread(threadGroup, runnable, newThreadName);
        LOGGER.info("created new thread -> {}", newThreadName);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        return thread;
    }

}
