package io.biteeniu.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池饱和拒绝策略执行处理器
 * @author luzhanghong
 * @date 2018-08-06 14:25
 */
public class CustomRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRejectedExecutionHandler.class);

    @Override
    public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
        /**
         * 在这里编写自定义的处理逻辑，对被线程池拒绝的任务进行处理
         */
        LOGGER.warn("new runnable task has been rejected.");
    }

}
