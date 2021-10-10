package com.zfoo.scheduler.timeWheelUtils;

import java.util.concurrent.DelayQueue;

/**
 * 时间轮
 */
public class TimeWheel {

    /**
     * 一个时间槽的范围
     */
    private long tickMs;

    /**
     * 时间轮大小
     */
    private int wheelSize;

    /**
     * 时间轮的范围
     */
    private long interval;

    /**
     * 时间槽
     */
    private Bucket[] buckets;

    /**
     * 当前时间
     */
    private long currentTime;

    /**
     * 上层时间轮
     */
    private volatile TimeWheel overflowWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private DelayQueue<Bucket> delayQueue;

    public TimeWheel(long tickMs, int wheelSize, long currentTime, DelayQueue<Bucket> delayQueue) {
        this.currentTime = currentTime;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.buckets = new Bucket[wheelSize];
        //currentTime为tickMs的整数倍 这里做取整操作
        this.currentTime = currentTime - (currentTime % tickMs);
        this.delayQueue = delayQueue;
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new Bucket();
        }
    }

    /**
     * 创建或者获取上层时间轮
     */
    private TimeWheel getOverflowWheel() {
        if (overflowWheel == null) {
            synchronized (this) {
                if (overflowWheel == null) {
                    overflowWheel = new TimeWheel(interval, wheelSize, currentTime, delayQueue);
                }
            }
        }
        return overflowWheel;
    }

    /**
     * 添加任务到时间轮
     */
    public boolean addTask(TimerTask timerTask) {
        long expiration = timerTask.getDelayMs();
        //过期任务直接执行
        if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            //当前时间轮可以容纳该任务 加入时间槽
            Long bound = expiration / tickMs;
            int bucketIndex = (int) (bound % wheelSize);
            //System.out.println("tickMs:" + tickMs + "------bucketIndex:" + bucketIndex + "------expiration:" + expiration);
            Bucket bucket = buckets[bucketIndex];
            bucket.addTask(timerTask);
            if (bucket.setExpiration(bound * tickMs)) {
                //添加到delayQueue中
                delayQueue.offer(bucket);
            }
        } else {
            //放到上一层的时间轮
            TimeWheel timeWheel = getOverflowWheel();
            timeWheel.addTask(timerTask);
        }
        return true;
    }

    /**
     * 推进时间
     */
    public void advanceClock(long timestamp) {
        if (timestamp >= currentTime + tickMs) {
            currentTime = timestamp - (timestamp % tickMs);
            if (overflowWheel != null) {
                //推进上层时间轮时间
                this.getOverflowWheel().advanceClock(timestamp);
            }
        }
    }
}
