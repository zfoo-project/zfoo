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
import com.zfoo.net.task.model.AbstractTaskDispatch;
import com.zfoo.net.task.model.ITaskDispatch;
import com.zfoo.net.task.model.ReceiveTask;
import com.zfoo.protocol.util.StringUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public final class TaskManager implements ITaskManager {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private static final TaskManager INSTANCE = new TaskManager();

    // 线程池的大小
    public static final int EXECUTOR_SIZE;

    private static final ITaskDispatch taskDispatch;


    /**
     * 使用不同的线程池，让线程池之间实现隔离，互不影响
     */
    private static final ExecutorService[] executors;

    static {
        var localConfig = NetContext.getConfigManager().getLocalConfig();
        var providerConfig = localConfig.getProvider();

        var dispatch = providerConfig == null ? "consistent-hash" : providerConfig.getDispatch();
        var dispatchThread = (providerConfig == null || StringUtils.isBlank(providerConfig.getDispatchThread()))
                ? "default" : providerConfig.getDispatchThread();

        EXECUTOR_SIZE = "default".equals(dispatchThread) ? (Runtime.getRuntime().availableProcessors() + 1) : Integer.parseInt(dispatchThread);
        taskDispatch = AbstractTaskDispatch.valueOf(dispatch);

        executors = new ExecutorService[EXECUTOR_SIZE];
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new TaskThreadFactory();
            executors[i] = Executors.newSingleThreadExecutor(namedThreadFactory);
        }
    }

    private static class TaskThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        TaskThreadFactory() {
            var s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "task-p" + poolNumber.getAndIncrement() + "-t";
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

    private TaskManager() {
    }

    public static TaskManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void addTask(ReceiveTask task) {
        taskDispatch.getExecutor(task).execute(task);
    }

    @Override
    public ExecutorService getExecutorByConsistentHash(int executorConsistentHash) {
        return executors[Math.abs(executorConsistentHash % EXECUTOR_SIZE)];
    }
}
