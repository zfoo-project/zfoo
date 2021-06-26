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

package com.zfoo.scheduler;

import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.scheduler.schema.SchedulerRegisterProcessor;
import com.zfoo.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class SchedulerContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerContext.class);

    private static SchedulerContext instance;

    private static boolean stop = false;

    private ApplicationContext applicationContext;


    public static SchedulerContext getSchedulerContext() {
        return instance;
    }

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    public static boolean isStop() {
        return stop;
    }


    public synchronized static void shutdown() {
        if (stop) {
            return;
        }

        stop = true;

        try {
            Field field = SchedulerBus.class.getDeclaredField("executor");
            ReflectionUtils.makeAccessible(field);
            var executor = (ScheduledExecutorService) ReflectionUtils.getField(field, null);
            ThreadUtils.shutdown(executor);
        } catch (Throwable e) {
            logger.error("Scheduler thread pool failed shutdown.", e);
            return;
        }

        logger.info("Scheduler shutdown gracefully.");
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (instance != null) {
                return;
            }
            // 初始化上下文
            SchedulerContext.instance = this;
            instance.applicationContext = event.getApplicationContext();

            var beanNames = applicationContext.getBeanDefinitionNames();
            var processor = applicationContext.getBean(SchedulerRegisterProcessor.class);

            for (var beanName : beanNames) {
                processor.postProcessAfterInitialization(applicationContext.getBean(beanName), beanName);
            }

        } else if (event instanceof ContextClosedEvent) {
            shutdown();
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
