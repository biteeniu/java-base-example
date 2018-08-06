package io.biteeniu.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 程序启动类
 * @author luzhanghong
 * @date 2018-08-06 14:01
 */
public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    /**
     * 程序启动入口
     * @param args args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10L;
        // 创建一个有界的工作队列，容量为5
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(5);
        // 创建一个自定义的线程工厂
        ThreadFactory threadFactory = new CustomThreadFactory();
        // 创建一个自定义的线程池饱和拒绝策略执行处理器
        RejectedExecutionHandler rejectedExecutionHandler = new CustomRejectedExecutionHandler();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                rejectedExecutionHandler);

        LOGGER.info("thread pool init: {}", threadPoolExecutor.toString());

        for (int i = 1; i <= 20; i++) {
            RunnableTask runnableTask = new RunnableTask(String.valueOf(i), 3600000L);
            LOGGER.info("Submit new task[{}] ... {}", String.valueOf(i), threadPoolExecutor.toString());
            threadPoolExecutor.execute(runnableTask);
            sleep(1000L);
        }
    }

    private static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

}
