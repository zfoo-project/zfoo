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

import com.zfoo.event.model.anno.EventReceiver;
import com.zfoo.event.model.event.IEvent;
import com.zfoo.event.model.vo.EnhanceUtils;
import com.zfoo.event.model.vo.EventReceiverDefinition;
import com.zfoo.event.model.vo.IEventReceiver;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.math.RandomUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class EventBus {

    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);

    // 线程池的大小
    private static final int EXECUTORS_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService[] executors;

    private static final Map<Class<? extends IEvent>, List<IEventReceiver>> receiverMap;


    static {
        receiverMap = new HashMap<>();
        executors = new ExecutorService[EXECUTORS_SIZE];

        for (int i = 0; i < executors.length; i++) {
            var namedThreadFactory = new EventThreadFactory();
            executors[i] = Executors.newSingleThreadExecutor(namedThreadFactory);
        }
    }

    private static class EventThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        EventThreadFactory() {
            var s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "event-p" + poolNumber.getAndIncrement() + "-t";
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

    /**
     * 同步抛出一个事件，会在当前线程中运行
     *
     * @param event 需要抛出的事件
     */
    public static void syncSubmit(IEvent event) {
        var list = receiverMap.get(event.getClass());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        doSubmit(event, list);
    }


    /**
     * 异步抛出一个事件，事件不在同一个线程中处理
     *
     * @param event 需要抛出的事件
     */
    public static void asyncSubmit(IEvent event) {
        var list = receiverMap.get(event.getClass());
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        executors[Math.abs(event.threadId() % EXECUTORS_SIZE)].execute(new Runnable() {
            @Override
            public void run() {
                doSubmit(event, list);
            }
        });
    }

    /**
     * 随机获取一个线程池
     */
    public static Executor asyncExecute() {
        return executors[RandomUtils.randomInt(EXECUTORS_SIZE)];
    }

    private static void doSubmit(IEvent event, List<IEventReceiver> listReceiver) {
        for (var receiver : listReceiver) {
            try {
                receiver.invoke(event);
            } catch (Exception e) {
                logger.error("eventBus未知exception异常", e);
            } catch (Throwable t) {
                logger.error("eventBus未知error异常", t);
            }
        }
    }

    public static void registerEventReceiver(Object bean) {
        try {
            var clazz = bean.getClass();
            var methods = ReflectionUtils.getMethodsByAnnoInPOJOClass(clazz, EventReceiver.class);
            for (var method : methods) {
                var paramClazzs = method.getParameterTypes();
                if (paramClazzs.length != 1) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must have one parameter!", bean.getClass().getName(), method.getName()));
                }
                if (!IEvent.class.isAssignableFrom(paramClazzs[0])) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must have one [IEvent] type parameter!", bean.getClass().getName(), method.getName()));
                }

                var eventClazz = (Class<? extends IEvent>) paramClazzs[0];
                var eventName = eventClazz.getCanonicalName();
                var methodName = method.getName();

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                var expectedMethodName = StringUtils.format("on{}", eventClazz.getSimpleName());
                if (!methodName.equals(expectedMethodName)) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] expects '{}' as method name!"
                            , bean.getClass().getName(), methodName, eventName, expectedMethodName));
                }

                var receiverDefinition = new EventReceiverDefinition(bean, method, eventClazz);
                if (!receiverMap.containsKey(eventClazz)) {
                    receiverMap.put(eventClazz, new LinkedList<>());
                }

                var enhanceReceiverDefinition = EnhanceUtils.createEventReceiver(receiverDefinition);
                receiverMap.get(eventClazz).add(enhanceReceiverDefinition);
            }
        } catch (NotFoundException | CannotCompileException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}


