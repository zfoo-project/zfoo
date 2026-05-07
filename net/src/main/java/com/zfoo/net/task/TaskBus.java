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

import com.zfoo.net.NetContext;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Task thread pool is used to handle client requests and CPU-intensive tasks.
 * Avoid performing blocking operations here; IO-intensive tasks should be delegated to the Event thread pool.
 *
 * @author godotg
 */
public final class TaskBus {

    private static final Logger logger = LoggerFactory.getLogger(TaskBus.class);

    // The thread pool size can also be configured via the provider's thread setting.
    // EXECUTOR_MASK is used with a bitwise AND to efficiently locate the target Executor.
    public static final int EXECUTOR_SIZE;
    private static final int EXECUTOR_MASK;

    /**
     * Multiple independent thread pools to ensure isolation; failures in one pool do not affect others.
     */
    private static final ExecutorService[] executors;

    static {
        var providerConfig = NetContext.getConfigManager().getLocalConfig().getProvider();
        var expectThreads = (providerConfig == null || StringUtils.isBlank(providerConfig.getThread()))
                ? Runtime.getRuntime().availableProcessors()
                : Integer.parseInt(providerConfig.getThread());
        EXECUTOR_SIZE = MathUtil.safeFindNextPositivePowerOfTwo(expectThreads);
        // if EXECUTOR_SIZE = 8 then EXECUTOR_MASK = 7 =  0b0111
        EXECUTOR_MASK = EXECUTOR_SIZE - 1;
        executors = new ExecutorService[EXECUTOR_SIZE];
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new TaskThreadFactory(i);
            var executor = Executors.newSingleThreadExecutor(namedThreadFactory);
            executors[i] = executor;
        }
    }

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
            ThreadUtils.registerSingleThreadExecutor(thread, executor);
            return thread;
        }
    }


    /**
     * Bind tasks to the same thread using uid, sid, or hashCode to improve CPU cache hit ratio.
     * <a href="https://zhuanlan.zhihu.com/p/439381000">System performance tuning - binding CPU</a>
     */
    private static int calTaskExecutorIndex(int taskExecutorHash) {
        return taskExecutorHash & EXECUTOR_MASK;
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
        executorOf(taskExecutorHash).execute(ThreadUtils.safeRunnable(runnable));
    }

    public static void execute(long taskExecutorHash, Runnable runnable) {
        execute((int) taskExecutorHash, runnable);
    }

    public static void execute(Object argument, Runnable runnable) {
        execute(calTaskExecutorHash(argument), runnable);
    }

    public static ExecutorService executorOf(int hash) {
        return executors[calTaskExecutorIndex(hash)];
    }

    // Async requests executed in task/event/scheduler threads will continue to run their callbacks on the same thread
    public static Executor currentThreadExecutor() {
        var currentThreadId = Thread.currentThread().getId();
        var executor = ThreadUtils.executorByThreadId(currentThreadId);
        return executor == null ? executorOf(RandomUtils.randomInt()) : executor;
    }

}
