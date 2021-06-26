package com.zfoo.event.manager;

import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class EventThreadFactory implements ThreadFactory {

    private static final Logger logger = LoggerFactory.getLogger(EventThreadFactory.class);

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public EventThreadFactory(int poolNumber) {
        var s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = "event-p" + poolNumber + "-t";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        var t = new FastThreadLocalThread(group, runnable, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        t.setUncaughtExceptionHandler((thread, e) -> logger.error(thread.toString(), e));
        return t;
    }

}
