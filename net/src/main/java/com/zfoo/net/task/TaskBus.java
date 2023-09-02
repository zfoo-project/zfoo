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
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.HttpAttachment;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.protocol.util.RandomUtils;
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
 * EN: The Task thread pool is generally used to process customer requests, do some CPU-intensive tasks, and try to avoid some blocking operations;
 * IO-intensive tasks can be executed in the Event thread pool
 * CN: Task线程池一般是用来处理客户的请求，做一些cpu密集型任务，尽量避免做一些阻塞操作；IO密集型任务可以放在Event线程池去做
 *
 * @author godotg
 * @version 3.0
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

    /**
     * Actor模型，最主要的就是线程模型，Actor模型保证了某个Actor所代表的任务永远不会同时在两条线程同时处理任务，这就避免了并发。
     * 无论是Java，Kotlin，Scala都没有真正的协程，所以最终做到Actor模型的只能是细致的控制线程。
     * <p>
     * zfoo中通过对线程池的细粒度控制，从而实现了Actor模型。
     * 为了简单，可以把Actor可以理解为一个用户或者一个玩家。
     * 因为同一个用户或者玩家的uid是固定的，通过uid去计算一致性hash（taskExecutorHash）永远会得到一致的结果，
     * 从而保证同一个用户或者玩家的请求总能通过taskExecutorHash被路由到同一台服务器的同一个线程去执行，从而避免了并发，实现了无锁化。
     * <p>
     * zfoo所代表的Actor模型，是更加精简的Actor模型，让上层调用无感知，在zfoo中可以简单的理解 actor = taskExecutorHash。
     * <p>
     * 在zfoo这套线程模型中，保证了服务器所接收到的Packet（最终被包装成PacketReceiverTask任务），永远只会在同一条线程处理，
     * TaskBus通过AbstractTaskDispatch去派发PacketReceiverTask任务，具体在哪个线程处理通过IAttachment的taskExecutorHash计算。
     * <p>
     * IAttachment的不同，taskExecutorHash也不同：
     * GatewayAttachment：默认是taskExecutorHash等于用户活玩家的uid，也可以通过IGatewayLoadBalancer接口指定
     * SignalAttachment：taskExecutorHash通过IRouter和IConsumer的argument参数指定
     */
    public static void dispatch(PacketReceiverTask task) {
        var attachment = task.getAttachment();
        if (attachment == null) {
            dispatchBySession(task.getSession(), task);
        } else {
            dispatchByAttachment(attachment, task);
        }
    }

    private static void dispatchBySession(Session session, PacketReceiverTask task) {
        var uid = session.getUid();
        if (uid > 0) {
            execute((int) uid, task);
        } else {
            execute((int) session.getSid(), task);
        }
    }

    private static void dispatchByAttachment(IAttachment attachment, PacketReceiverTask task) {
        switch (attachment.packetType()) {
            case SIGNAL_PACKET:
                execute(((SignalAttachment) attachment).taskExecutorHash(), task);
                break;
            case GATEWAY_PACKET:
                execute(((GatewayAttachment) attachment).taskExecutorHash(), task);
                break;
            case HTTP_PACKET:
                execute(((HttpAttachment) attachment).taskExecutorHash(), task);
                break;
            case SIGNAL_ONLY_PACKET:
            case NO_ANSWER_PACKET:
            case UDP_PACKET:
                dispatchBySession(task.getSession(), task);
                break;
            default:
        }
    }

    public static int calTaskExecutorHash(int taskExecutorHash) {
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
        return calTaskExecutorHash(hash);
    }

    public static void execute(int taskExecutorHash, Runnable runnable) {
        executors[calTaskExecutorHash(taskExecutorHash)].execute(ThreadUtils.safeRunnable(runnable));
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

        return executors[calTaskExecutorHash(RandomUtils.randomInt())];
    }

}
