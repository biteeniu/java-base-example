package io.biteeniu.redis.delay.queue;

import com.alibaba.fastjson.JSON;

import java.util.UUID;

/**
 * 任务元信息定义类
 * @author luzhanghong
 * @date 2018-07-20 09:31
 */
public class Job {

    private String topic;  // 任务的类型
    private String id;     // 任务的ID：使用UUID，确保每个任务都有一个唯一的ID
    private Integer delay; // 任务延迟的时间，单位秒
    private Integer ttr;   // Time-to-run：Job执行超时时间，单位秒
    private Object body;   // 任务的内容：json格式

    /**
     * 构造函数
     * @param topic 任务的类型
     * @param delay 任务延迟的时间，单位秒
     * @param ttr 任务执行超时时间，单位秒
     * @param body 任务的内容
     */
    public Job(String topic, Integer delay, Integer ttr, Object body) {
        this.topic = topic;
        this.id = UUID.randomUUID().toString();
        this.delay = delay;
        this.ttr = ttr;
        this.body = body;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getTtr() {
        return ttr;
    }

    public void setTtr(Integer ttr) {
        this.ttr = ttr;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

}
