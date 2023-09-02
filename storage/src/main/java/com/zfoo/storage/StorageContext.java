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

package com.zfoo.storage;

import com.zfoo.scheduler.util.StopWatch;
import com.zfoo.storage.manager.IStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

/**
 * @author godotg
 * @version 3.0
 */
public class StorageContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(StorageContext.class);

    private static StorageContext instance;

    private ApplicationContext applicationContext;

    private IStorageManager storageManager;

    public static StorageContext getStorageContext() {
        return instance;
    }

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    public static IStorageManager getStorageManager() {
        return instance.storageManager;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {

        if (event instanceof ContextRefreshedEvent) {
            var stopWatch = new StopWatch();
            // 初始化上下文
            StorageContext.instance = this;
            instance.applicationContext = event.getApplicationContext();
            instance.storageManager = applicationContext.getBean(IStorageManager.class);

            // 初始化，并读取配置表
            instance.storageManager.initBefore();

            // 注入配置表资源
            instance.storageManager.inject();

            // 移除没有被引用的不必要资源，为了节省服务器内存
            instance.storageManager.initAfter();
            logger.info("Storage started successfully and cost [{}] seconds", stopWatch.costSeconds());
        } else if (event instanceof ContextClosedEvent) {

        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
