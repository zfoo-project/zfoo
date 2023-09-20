/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.net.task;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * EN: The Task thread pool is generally used to process customer requests, do some CPU-intensive tasks, and try to avoid some blocking operations;
 * IO-intensive tasks can be executed in the Event thread pool
 * CN: Task线程池一般是用来处理客户的请求，做一些cpu密集型任务，尽量避免做一些阻塞操作；IO密集型任务可以放在Event线程池去做
 *
 * @author godotg
 */
public final class TaskBus {

    private static final Logger logger = LoggerFactory.getLogger(TaskBus.class);

    // EN: The size of the thread pool can also be specified through the provider thread configuration
    // CN: 线程池的大小，也可以通过provider thread配置指定
    public static final int EXECUTOR_SIZE;

    /**
     * EN: Use different thread pools to achieve isolation between thread pools without affecting each other
     * CN: 使用不同的线程池，让线程池之间实现隔离，互不影响
     */
    private static final ExecutorService[] executors;

    static {
        var localConfig = NetContext.getConfigManager().getLocalConfig();
        var providerConfig = localConfig.getProvider();

        EXECUTOR_SIZE = (providerConfig == null || StringUtils.isBlank(providerConfig.getThread())) ? (Runtime.getRuntime().availableProcessors() + 1) : Integer.parseInt(providerConfig.getThread());

        executors = new ExecutorService[EXECUTOR_SIZE];
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new TaskThreadFactory(i);
            var executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executors[i] = executor;
        }
    }

    private static final CopyOnWriteHashMapLongObject<ExecutorService> threadMap = new CopyOnWriteHashMapLongObject<>(EXECUTOR_SIZE);

    public static class TaskThreadFactory implements ThreadFactory {
        private final int poolNumber;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public TaskThreadFactory(int poolNumber) {
            this.group = Thread.currentThread().getThreadGroup();
            this.poolNumber = poolNumber;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var threadName = StringUtils.format("task-p{}-t{}", poolNumber + 1, threadNumber.getAndIncrement());
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


    private static int calTaskExecutorIndex(int taskExecutorHash) {
        // Other hash algorithms can be customized to make the distribution more uniform
        return Math.abs(taskExecutorHash) % EXECUTOR_SIZE;
    }

    public static int calTaskExecutorHash(Object argument) {
        var hash = 0;
        if (argument == null) {
            hash = RandomUtils.randomInt();
        } else if (argument instanceof Number) {
            hash = ((Number) argument).intValue();
        } else {
            hash = argument.hashCode();
        }
        return hash;
    }

    public static void execute(int taskExecutorHash, Runnable runnable) {
        executors[calTaskExecutorIndex(taskExecutorHash)].execute(ThreadUtils.safeRunnable(runnable));
    }

    public static void execute(Object argument, Runnable runnable) {
        execute(calTaskExecutorHash(argument), runnable);
    }

    // 在task，event，scheduler线程执行的异步请求，请求成功过后依然在相同的线程执行回调任务
    public static Executor currentThreadExecutor() {
        var threadId = Thread.currentThread().getId();
        var taskExecutor = threadMap.getPrimitive(threadId);
        if (taskExecutor != null) {
            return taskExecutor;
        }

        var eventExecutor = EventBus.threadExecutor(threadId);
        if (eventExecutor != null) {
            return eventExecutor;
        }

        var schedulerExecutor = SchedulerBus.threadExecutor(threadId);
        if (schedulerExecutor != null) {
            return schedulerExecutor;
        }

        return executors[calTaskExecutorIndex(RandomUtils.randomInt())];
    }

}
