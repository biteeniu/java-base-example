package io.biteeniu.redis.delay.queue;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * @author luzhanghong
 * @date 2018-07-24 14:40
 */
public class Listener implements RemovalListener<String, String> {

    @Override
    public void onRemoval(RemovalNotification notification) {
        System.out.println(notification.getKey() + " has been removed.");
    }

}
