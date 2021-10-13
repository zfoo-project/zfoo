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

package com.zfoo.net;

import com.zfoo.net.config.manager.IConfigManager;
import com.zfoo.net.consumer.IConsumer;
import com.zfoo.net.core.AbstractClient;
import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.packet.service.IPacketService;
import com.zfoo.net.router.IRouter;
import com.zfoo.net.session.manager.ISessionManager;
import com.zfoo.net.session.model.Session;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.scheduler.SchedulerContext;
import com.zfoo.scheduler.model.StopWatch;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class NetContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(NetContext.class);

    private static NetContext instance;

    private ApplicationContext applicationContext;

    private IRouter router;

    private IConsumer consumer;

    private IConfigManager configManager;

    private IPacketService packetService;

    private ISessionManager sessionManager;


    public static NetContext getNetContext() {
        return instance;
    }

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    public static IRouter getRouter() {
        return instance.router;
    }

    public static IConsumer getConsumer() {
        return instance.consumer;
    }

    public static IConfigManager getConfigManager() {
        return instance.configManager;
    }

    public static IPacketService getPacketService() {
        return instance.packetService;
    }

    public static ISessionManager getSessionManager() {
        return instance.sessionManager;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            var stopWatch = new StopWatch();
            NetContext.instance = this;
            instance.applicationContext = event.getApplicationContext();
            instance.configManager = applicationContext.getBean(IConfigManager.class);
            instance.packetService = applicationContext.getBean(IPacketService.class);
            instance.router = applicationContext.getBean(IRouter.class);
            instance.consumer = applicationContext.getBean(IConsumer.class);
            instance.sessionManager = applicationContext.getBean(ISessionManager.class);

            instance.packetService.init();
            instance.configManager.initRegistry();

            logger.info("Net started successfully and cost [{}] seconds", stopWatch.costSeconds());
        } else if (event instanceof ContextClosedEvent) {
            shutdownBefore();
            shutdownAfter();
        }
    }


    public synchronized void shutdownBefore() {
        SchedulerContext.shutdown();
    }

    public synchronized void shutdownAfter() {
        // 关闭zookeeper的客户端
        configManager.getRegistry().shutdown();

        // 先关闭所有session
        IOUtils.closeIO(ArrayUtils.listToArray(new ArrayList<>(sessionManager.getClientSessionMap().values()), Session.class));
        IOUtils.closeIO(ArrayUtils.listToArray(new ArrayList<>(sessionManager.getServerSessionMap().values()), Session.class));

        // 关闭客户端和服务器
        AbstractClient.shutdown();
        AbstractServer.shutdownAllServers();

        // 关闭TaskManager
        try {
            Field field = TaskBus.class.getDeclaredField("executors");
            ReflectionUtils.makeAccessible(field);

            var executors = (ExecutorService[]) ReflectionUtils.getField(field, null);
            for (ExecutorService executor : executors) {
                ThreadUtils.shutdown(executor);
            }
        } catch (Throwable e) {
            logger.error("Net thread pool failed shutdown: " + ExceptionUtils.getMessage(e));
            return;
        }

        logger.info("Net shutdown gracefully.");
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
