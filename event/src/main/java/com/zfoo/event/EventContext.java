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

package com.zfoo.event;

import com.zfoo.event.manager.EventBus;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import java.util.concurrent.ExecutorService;

/**
 * @author godotg
 * @version 3.0
 */
public class EventContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(EventContext.class);

    private static EventContext instance;

    private ApplicationContext applicationContext;

    public static EventContext getEventContext() {
        return instance;
    }

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    private synchronized void shutdown() {
        try {
            var field = EventBus.class.getDeclaredField("executors");
            ReflectionUtils.makeAccessible(field);

            var executors = (ExecutorService[]) ReflectionUtils.getField(field, null);
            for (var executor : executors) {
                ThreadUtils.shutdown(executor);
            }
        } catch (Throwable e) {
            logger.error("Event thread pool failed shutdown: " + ExceptionUtils.getMessage(e));
            return;
        }

        logger.info("Event shutdown gracefully.");
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            // 初始化上下文
            EventContext.instance = this;
            instance.applicationContext = event.getApplicationContext();
        } else if (event instanceof ContextClosedEvent) {
            shutdown();
            ThreadUtils.shutdownForkJoinPool();
        }
    }

    @Override
    public int getOrder() {
        return 30;
    }

}
