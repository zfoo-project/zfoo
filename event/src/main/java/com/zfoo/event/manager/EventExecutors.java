package com.zfoo.event.manager;

import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
public abstract class EventExecutors {

    private static final Logger logger = LoggerFactory.getLogger(EventExecutors.class);

    /**
     * EN: The size of the thread pool. Event's thread pool is often used to do time-consuming operations, so set it a little bigger
     * CN: 线程池的大小. event的线程池经常用来做一些耗时的操作，所以要设置大一点
     */
    private static final int EXECUTORS_SIZE = Math.max(Runtime.getRuntime().availableProcessors(), 4) * 2 + 1;

    private static final ExecutorService[] executors = new ExecutorService[EXECUTORS_SIZE];

    private static final CopyOnWriteHashMapLongObject<ExecutorService> threadMap = new CopyOnWriteHashMapLongObject<>(EXECUTORS_SIZE);


    static {
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new EventThreadFactory(i);
            var executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executors[i] = executor;
        }
    }

    public static class EventThreadFactory implements ThreadFactory {
        private final int poolNumber;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public EventThreadFactory(int poolNumber) {
            this.group = Thread.currentThread().getThreadGroup();
            this.poolNumber = poolNumber;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var threadName = StringUtils.format("event-p{}-t{}", poolNumber + 1, threadNumber.getAndIncrement());
            var thread = new FastThreadLocalThread(group, runnable, threadName);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> logger.error(t.toString(), e));
            var executor = executors[poolNumber];
            AssertionUtils.notNull(executor);
            threadMap.put(thread.getId(), executor);
            return thread;
        }
    }

    /**
     * Use the event thread specified by the hashcode to execute the task
     */
    public static void execute(int executorHash, Runnable runnable) {
        executors[Math.abs(executorHash % EXECUTORS_SIZE)].execute(ThreadUtils.safeRunnable(runnable));
    }

    public static Executor threadExecutor(long currentThreadId) {
        return threadMap.getPrimitive(currentThreadId);
    }
}
