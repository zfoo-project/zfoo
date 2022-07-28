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

import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.scheduler.model.anno.Scheduler;
import com.zfoo.scheduler.model.vo.SchedulerDefinition;
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
import java.lang.reflect.Modifier;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author godotg
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
            // 初始化上下文
            SchedulerContext.instance = this;
            instance.applicationContext = event.getApplicationContext();
            inject();
            // 启动位于SchedulerBus的static静态代码块中
        } else if (event instanceof ContextClosedEvent) {
            // 反射获取executor,关闭掉
            shutdown();
        }
    }

    public void inject() {
        var beanNames = applicationContext.getBeanDefinitionNames();
        for (var beanName : beanNames) {
            var bean = applicationContext.getBean(beanName);
            var clazz = bean.getClass();

            var methods = ReflectionUtils.getMethodsByAnnoInPOJOClass(bean.getClass(), Scheduler.class);

            if (ArrayUtils.isEmpty(methods)) {
                continue;
            }

            if (!ReflectionUtils.isPojoClass(clazz)) {
                logger.warn("调度注册类[{}]不是POJO类，父类的调度不会被扫描到", clazz);
            }

            try {
                for (var method : methods) {
                    var schedulerMethod = method.getAnnotation(Scheduler.class);

                    var paramClazzs = method.getParameterTypes();
                    if (paramClazzs.length >= 1) {
                        throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] can not have any parameters", bean.getClass(), method.getName()));
                    }

                    var methodName = method.getName();

                    if (!Modifier.isPublic(method.getModifiers())) {
                        throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName));
                    }

                    if (Modifier.isStatic(method.getModifiers())) {
                        throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName));
                    }

                    if (!methodName.startsWith("cron")) {
                        throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must start with 'cron' as method name!"
                                , bean.getClass().getName(), methodName));
                    }

                    var scheduler = SchedulerDefinition.valueOf(schedulerMethod.cron(), bean, method);
                    SchedulerBus.registerScheduler(scheduler);
                }
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
