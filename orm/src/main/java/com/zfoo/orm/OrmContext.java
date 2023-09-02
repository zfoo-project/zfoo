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

package com.zfoo.orm;

import com.mongodb.client.MongoClient;
import com.zfoo.orm.accessor.IAccessor;
import com.zfoo.orm.manager.IOrmManager;
import com.zfoo.orm.manager.OrmManager;
import com.zfoo.orm.model.IEntity;
import com.zfoo.orm.query.IQuery;
import com.zfoo.orm.query.IQueryBuilder;
import com.zfoo.protocol.util.ReflectionUtils;
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

/**
 * @author godotg
 * @version 3.0
 */
public class OrmContext implements ApplicationListener<ApplicationContextEvent>, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(OrmContext.class);

    private static OrmContext instance;

    private ApplicationContext applicationContext;

    private IAccessor accessor;

    private IQuery query;

    private IOrmManager ormManager;

    private boolean stop = false;

    public static ApplicationContext getApplicationContext() {
        return instance.applicationContext;
    }

    public static OrmContext getOrmContext() {
        return instance;
    }

    public static IAccessor getAccessor() {
        return instance.accessor;
    }

    public static <E extends IEntity<?>> IQueryBuilder<E> getQuery(Class<E> entityClazz) {
        return instance.query.builder(entityClazz);
    }

    public static IOrmManager getOrmManager() {
        return instance.ormManager;
    }

    public static boolean isStop() {
        return instance.stop;
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            var stopWatch = new StopWatch();
            OrmContext.instance = this;
            instance.applicationContext = event.getApplicationContext();

            instance.accessor = applicationContext.getBean(IAccessor.class);
            instance.query = applicationContext.getBean(IQuery.class);
            instance.ormManager = applicationContext.getBean(IOrmManager.class);

            instance.ormManager.initBefore();
            instance.ormManager.inject();
            instance.ormManager.initAfter();

            logger.info("Orm started successfully and cost [{}] seconds", stopWatch.costSeconds());
        } else if (event instanceof ContextClosedEvent) {
            shutdownBefore();
            shutdownBetween();
            shutdownAfter();
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }

    public static synchronized void shutdownBefore() {
        SchedulerContext.shutdown();
    }

    public static synchronized void shutdownBetween() {
        instance.stop = true;
        try {
            instance.ormManager
                    .getAllEntityCaches()
                    .forEach(it -> it.persistAll());
        } catch (Exception e) {
            logger.error("关闭服务器时，持久化缓存数据异常", e);
        } finally {
            instance.stop = true;
        }
    }

    public static synchronized void shutdownAfter() {
        try {
            var field = OrmManager.class.getDeclaredField("mongoClient");
            ReflectionUtils.makeAccessible(field);
            var mongoClient = (MongoClient) ReflectionUtils.getField(field, instance.ormManager);
            mongoClient.close();
        } catch (Exception e) {
            logger.error("关闭MongoClient数据库连接失败", e);
            return;
        }

        logger.info("Orm shutdown gracefully.");
    }

}
