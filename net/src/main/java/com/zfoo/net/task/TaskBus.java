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
     * 使用不同的线程池，让线程池之间实现隔离，互不影响（每个线程池只有一个线程）
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


    /**
     * Actor模型，最主要的就是线程模型， Actor模型保证了某个Actor所代表的任务永远不会同时在两条线程同时处理任务，这就就避免了并发。
     * 无论是Java，Kotlin，Scala都没有真正的协程？，所以最终做到Actor模型的只能是细致的控制线程。
     * <p>
     * zfoo中通过对线程池的细粒度控制，从而实现了Actor模型。
     *
     * 为了简单，可以把Actor可以理解为一个用户或者一个玩家。
     * 因为同一个用户或者玩家的uid是固定的，通过uid去计算一致性hash（executorConsistentHash）永远会得到一致的结果，
     * 从而保证同一个用户或者玩家的请求总能通过executorConsistentHash被路由到同一台服务器的同一个线程去执行，从而避免了并发，实现了无锁化。
     * <p>
     * zfoo所代表的Actor模型，是更加精简的Actor模型，让上层调用无感知，在zfoo中可以简单的理解 actor = executorConsistentHash。
     * <p>
     * 在zfoo这套线程模型中，保证了服务器所接收到的Packet（最终被包装成PacketReceiverTask任务），【永远只会在同一条线程处理，】
     * TaskBus通过AbstractTaskDispatch去派发PacketReceiverTask任务，具体在哪个线程处理通过IAttachment的executorConsistentHash计算。
     * <p>
     * IAttachment的不同，executorConsistentHash也不同：
     * GatewayAttachment：默认是executorConsistentHash等于用户活玩家的uid，也可以通过IGatewayLoadBalancer接口指定
     * SignalAttachment：executorConsistentHash通过IRouter和IConsumer的argument参数指定
     */
    public static void submit(PacketReceiverTask task) {
        taskDispatch.getExecutor(task).execute(task);
    }

    public static ExecutorService executor(int executorConsistentHash) {
        return executors[Math.abs(executorConsistentHash % EXECUTOR_SIZE)];//一致性哈希值 取模
    }
}
