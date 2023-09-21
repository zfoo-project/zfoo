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
package com.zfoo.event.manager;

import com.zfoo.event.enhance.IEventReceiver;
import com.zfoo.event.model.IEvent;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.collection.concurrent.CopyOnWriteHashMapLongObject;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    /**
     * EN: The size of the thread pool. Event's thread pool is often used to do time-consuming operations, so set it a little bigger
     * CN: 线程池的大小. event的线程池经常用来做一些耗时的操作，所以要设置大一点
     */
    public static final int EXECUTORS_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService[] executors = new ExecutorService[EXECUTORS_SIZE];

    private static final CopyOnWriteHashMapLongObject<ExecutorService> threadMap = new CopyOnWriteHashMapLongObject<>(EXECUTORS_SIZE);
    /**
     * event mapping
     */
    private static final Map<Class<? extends IEvent>, List<IEventReceiver>> receiverMap = new HashMap<>();

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
     * Publish the event
     */
    public static void post(IEvent event) {
        if (event == null) {
            return;
        }
        var clazz = event.getClass();
        var receivers = receiverMap.get(clazz);
        if (CollectionUtils.isEmpty(receivers)) {
            return;
        }
        for (var receiver : receivers) {
            switch (receiver.bus()) {
                case CurrentThread -> doReceiver(receiver, event);
                case AsyncThread -> execute(event.executorHash(), () -> doReceiver(receiver, event));
//                case VirtualThread -> Thread.ofVirtual().name("virtual-on" + clazz.getSimpleName()).start(() -> doReceiver(receiver, event));
            }
        }
    }

    private static void doReceiver(IEventReceiver receiver, IEvent event) {
        try {
            receiver.invoke(event);
        } catch (Exception e) {
            logger.error("eventBus {} [{}] unknown exception", receiver.bus(), event.getClass().getSimpleName(), e);
        } catch (Throwable t) {
            logger.error("eventBus {} [{}] unknown error", receiver.bus(), event.getClass().getSimpleName(), t);
        }
    }


    public static void asyncExecute(Runnable runnable) {
        execute(RandomUtils.randomInt(), runnable);
    }

    /**
     * Use the event thread specified by the hashcode to execute the task
     */
    public static void execute(int executorHash, Runnable runnable) {
        executors[Math.abs(executorHash % EXECUTORS_SIZE)].execute(ThreadUtils.safeRunnable(runnable));
    }

    /**
     * Register the event and its counterpart observer
     */
    public static void registerEventReceiver(Class<? extends IEvent> eventType, IEventReceiver receiver) {
        receiverMap.computeIfAbsent(eventType, it -> new ArrayList<>(1)).add(receiver);
    }

    public static Executor threadExecutor(long currentThreadId) {
        return threadMap.getPrimitive(currentThreadId);
    }
}


