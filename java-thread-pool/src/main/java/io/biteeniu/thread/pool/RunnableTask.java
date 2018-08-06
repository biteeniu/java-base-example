package io.biteeniu.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程池任务定义类
 * @author luzhanghong
 * @date 2018-08-06 14:34
 */
public class RunnableTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnableTask.class);
    private String taskName;
    private long sleepTime;

    /**
     * 构造函数
     * @param taskName 线程任务的名称
     * @param sleepTime 模拟任务执行耗时时间，单位毫秒
     */
    public RunnableTask(String taskName, long sleepTime) {
        this.taskName = taskName;
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        //LOGGER.info("task[{}] is running ...", taskName);
        sleep(sleepTime);
        //LOGGER.info("task[{}] execute complete.", taskName);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
