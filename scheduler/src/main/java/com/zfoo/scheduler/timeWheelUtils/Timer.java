package com.zfoo.scheduler.timeWheelUtils;

import com.zfoo.scheduler.manager.SchedulerThreadFactory;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.concurrent.*;

import static com.zfoo.scheduler.manager.SchedulerBus.executor;

/**
 * 定时器
 */
public class Timer {

    /**
     * 底层时间轮
     */
    private TimeWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private DelayQueue<Bucket> delayQueue = new DelayQueue<>();


    /**
     * 构造函数
     */
    public Timer() {
        timeWheel = new TimeWheel(1000, 20, TimeUtils.currentTimeMillis(), delayQueue);
    }

    /**
     * 添加任务
     */
    public void addTask(TimerTask timerTask) {
        //添加失败任务直接执行
        if (!timeWheel.addTask(timerTask)) {
            executor.submit(timerTask.getTask());
        }
    }

    /**
     * 获取过期任务
     */
    public void advanceClock(long timestamp) {
        try {
            //阻塞获取队头元素
            Bucket bucket = delayQueue.poll(timestamp, TimeUnit.MILLISECONDS);

            if (bucket != null) {
                //推进时间
                timeWheel.advanceClock(bucket.getExpiration());
                //执行过期任务（包含降级操作）
                bucket.flush(this::addTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
