package com.zfoo.scheduler.timeWheelUtils;

/**
 * 任务
 */
public class TimerTask {

    /**
     * 延迟时间
     */
    private long delayMs;

    /**
     * 任务
     */
    private Runnable task;

    /**
     * 时间槽
     */
    protected Bucket bucket;

    /**
     * 下一个节点
     */
    protected TimerTask next;

    /**
     * 上一个节点
     */
    protected TimerTask pre;


    public TimerTask(long delayMs, Runnable task) {
        this.delayMs = delayMs;
        this.task = task;
        this.bucket = null;
        this.next = null;
        this.pre = null;
    }

    public Runnable getTask() {
        return task;
    }

    public long getDelayMs() {
        return delayMs;
    }


}
