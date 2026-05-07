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

import com.zfoo.net.config.IConfigManager;
import com.zfoo.net.consumer.IConsumer;
import com.zfoo.net.core.AbstractClient;
import com.zfoo.net.core.AbstractServer;
import com.zfoo.net.packet.IPacketService;
import com.zfoo.net.router.IRouter;
import com.zfoo.net.session.ISessionManager;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.SchedulerContext;
import com.zfoo.scheduler.util.StopWatch;
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
 * @author godotg
 */
public class NetContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(NetContext.class);

    private static NetContext instance;

    private ApplicationContext applicationContext;

    /**
     * When using the Router, the first parameter of the send() method specifies the session for sending messages.
     * For example: after creating a TcpClient and obtaining its session, messages can be sent through that session.
     */
    private IRouter router;

    /**
     * When using the Consumer, the provided interface has no Session parameter.
     * Sessions are obtained via the load-balancing strategy (ConsumerSession) and messages are sent through it.
     * For example: when a web client sends a request to a game server, the web side holds no session,
     * so it must use Consumer to make the request.
     */
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
            instance.consumer.init();

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
        // Close the ZooKeeper client
        configManager.getRegistry().shutdown();

        // Close all sessions first
        sessionManager.forEachClientSession(it -> IOUtils.closeIO(it));
        sessionManager.forEachServerSession(it -> IOUtils.closeIO(it));

        // Shut down clients and servers
        AbstractClient.shutdown();
        AbstractServer.shutdownAllServers();

        // Shut down TaskBus
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
