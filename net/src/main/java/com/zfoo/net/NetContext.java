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

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.config.manager.IConfigManager;
import com.zfoo.net.consumer.service.IConsumer;
import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.core.tcp.TcpClient;
import com.zfoo.net.dispatcher.manager.IPacketDispatcher;
import com.zfoo.net.packet.service.IPacketService;
import com.zfoo.net.session.manager.ISessionManager;
import com.zfoo.net.task.TaskManager;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.scheduler.SchedulerContext;
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
import java.util.concurrent.ExecutorService;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class NetContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(NetContext.class);

    private static NetContext instance;

    private ApplicationContext applicationContext;

    private IConfigManager configManager;

    private IPacketService packetService;

    private IPacketDispatcher packetDispatcher;

    private ISessionManager sessionManager;

    private IConsumer consumer;

    public static NetContext getNetContext() {
        return instance;
    }

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
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

    public static IPacketDispatcher getDispatcher() {
        return instance.packetDispatcher;
    }

    public static IConsumer getConsumer() {
        return instance.consumer;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (instance != null) {
                return;
            }

            NetContext.instance = this;
            instance.applicationContext = event.getApplicationContext();
            instance.configManager = applicationContext.getBean(IConfigManager.class);
            instance.packetService = applicationContext.getBean(IPacketService.class);
            instance.packetDispatcher = applicationContext.getBean(IPacketDispatcher.class);
            instance.consumer = applicationContext.getBean(IConsumer.class);
            instance.sessionManager = applicationContext.getBean(ISessionManager.class);

            instance.packetService.init();
            instance.configManager.initRegistry();

        } else if (event instanceof ContextClosedEvent) {
            shutdownBefore();
            shutdownAfter();
        }
    }


    public synchronized static void shutdownBefore() {
        SchedulerContext.shutdown();
    }

    public static synchronized void shutdownAfter() {
        // 关闭zookeeper的客户端
        NetContext.getConfigManager().getRegistry().shutdown();


        // 先关闭所有session
        NetContext.getSessionManager().shutdown();

        // 关闭客户端和服务器
        TcpClient.shutdown();
        AbstractServer.shutdownAllServers();

        // 关闭TaskManager
        try {
            Field field = EventBus.class.getDeclaredField("executors");
            ReflectionUtils.makeAccessible(field);

            var executors = (ExecutorService[]) ReflectionUtils.getField(field, TaskManager.getInstance());
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
