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
import com.zfoo.net.task.dispatcher.AbstractTaskDispatch;
import com.zfoo.net.task.dispatcher.ITaskDispatch;
import com.zfoo.net.task.model.PacketReceiverTask;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public final class TaskBus {

    private static final Logger logger = LoggerFactory.getLogger(TaskBus.class);

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

        taskDispatch = AbstractTaskDispatch.valueOf(providerConfig == null ? "consistent-hash" : providerConfig.getTaskDispatch());

        EXECUTOR_SIZE = (providerConfig == null || StringUtils.isBlank(providerConfig.getThread()))
                ? (Runtime.getRuntime().availableProcessors() + 1)
                : Integer.parseInt(providerConfig.getThread());

        executors = new ExecutorService[EXECUTOR_SIZE];
        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new TaskThreadFactory();
            executors[i] = Executors.newSingleThreadExecutor(namedThreadFactory);
        }
    }


    public static void submit(PacketReceiverTask task) {
        taskDispatch.getExecutor(task).execute(task);
    }

    public static ExecutorService executor(int executorConsistentHash) {
        return executors[Math.abs(executorConsistentHash % EXECUTOR_SIZE)];
    }
}
