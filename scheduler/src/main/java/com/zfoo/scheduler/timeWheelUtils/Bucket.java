package com.zfoo.scheduler.timeWheelUtils;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 时间槽
 */
public class Bucket implements Delayed {

    /**
     * 过期时间
     */
    private AtomicLong expiration = new AtomicLong(-1L);

    /**
     * 傀儡节点
     */
    private TimerTask root = new TimerTask(-1L, null);

    {
        root.pre = root;
        root.next = root;
    }

    /**
     * 设置过期时间
     */
    public boolean setExpiration(long expire) {
        return expiration.getAndSet(expire) != expire;
    }

    /**
     * 获取过期时间
     */
    public long getExpiration() {
        return expiration.get();
    }

    /**
     * 新增任务，加入链表尾部
     */
    public void addTask(TimerTask timerTask) {
        synchronized (this) {
            if (timerTask.bucket == null) {
                timerTask.bucket = this;
                TimerTask tail = root.pre;
                timerTask.next = root;
                timerTask.pre = tail;
                tail.next = timerTask;
                root.pre = timerTask;
            }
        }
    }

    /**
     * 移除任务
     */
    public void removeTask(TimerTask timerTask) {
        synchronized (this) {
            if (timerTask.bucket.equals(this)) {
                timerTask.next.pre = timerTask.pre;
                timerTask.pre.next = timerTask.next;
                timerTask.bucket = null;
                timerTask.next = null;
                timerTask.pre = null;
            }
        }
    }

    /**
     * 重新分配
     */
    public void flush(Consumer<TimerTask> flush) {
        TimerTask timerTask = root.next;
        while (!timerTask.equals(root)) {
            this.removeTask(timerTask);
            flush.accept(timerTask);
            timerTask = root.next;
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof Bucket) {
            return Long.compare(expiration.get(), ((Bucket) o).expiration.get());
        }
        return 0;
    }
}
