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
import com.zfoo.protocol.util.RandomUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author godotg
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    /**
     * event mapping
     */
    private static final Map<Class<? extends IEvent>, List<IEventReceiver>> receiverMap = new HashMap<>();

    /**
     * custom thread event receiver
     */
    public static BiConsumer<IEventReceiver, IEvent> manualThreadHandler = EventBus::doReceiver;

    /**
     * event exception handler
     */
    public static BiConsumer<IEventReceiver, IEvent> exceptionHandler = null;
    public static Consumer<IEvent> noEventReceiverHandler = null;


    /**
     * Publish the event
     *
     * @param event Event object
     */
    public static void post(IEvent event) {
        if (event == null) {
            return;
        }
        var clazz = event.getClass();
        var receivers = receiverMap.get(clazz);
        if (CollectionUtils.isEmpty(receivers)) {
            if (noEventReceiverHandler != null) {
                noEventReceiverHandler.accept(event);
            }
            return;
        }
        for (var receiver : receivers) {
            switch (receiver.bus()) {
                case CurrentThread -> doReceiver(receiver, event);
                case AsyncThread -> asyncExecute(event.executorHash(), () -> doReceiver(receiver, event));
//                case VirtualThread -> Thread.ofVirtual().name("virtual-on" + clazz.getSimpleName()).start(() -> doReceiver(receiver, event));
                case ManualThread -> manualThreadHandler.accept(receiver, event);
            }
        }
    }

    private static void doReceiver(IEventReceiver receiver, IEvent event) {
        try {
            receiver.invoke(event);
        } catch (Throwable t) {
            logger.error("eventBus {} [{}] unknown error", receiver.bus(), event.getClass().getSimpleName(), t);
            if (exceptionHandler != null) {
                exceptionHandler.accept(receiver, event);
            }
        }
    }

    public static void asyncExecute(Runnable runnable) {
        asyncExecute(RandomUtils.randomInt(), runnable);
    }

    /**
     * Use the event thread specified by the hashcode to execute the task
     */
    public static void asyncExecute(int executorHash, Runnable runnable) {
        EventExecutors.execute(executorHash, ThreadUtils.safeRunnable(runnable));
    }

    /**
     * Register the event and its counterpart observer
     */
    public static void registerEventReceiver(Class<? extends IEvent> eventType, IEventReceiver receiver) {
        receiverMap.computeIfAbsent(eventType, it -> new ArrayList<>(1)).add(receiver);
    }


    // ------------------------------------------------------------------------------------------------------------------
    static final CopyOnWriteHashMapLongObject<ExecutorService> threadMap = new CopyOnWriteHashMapLongObject<>();
    public static Executor threadExecutor(long currentThreadId) {
        return threadMap.getPrimitive(currentThreadId);
    }
}


