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

package com.zfoo.orm.cache.persister;

import com.zfoo.event.manager.EventBus;
import com.zfoo.orm.OrmContext;
import com.zfoo.orm.cache.EntityCache;
import com.zfoo.orm.model.EntityDef;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.scheduler.manager.SchedulerBus;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.CronExpression;

import java.util.concurrent.TimeUnit;

/**
 * @author godotg
 * @version 3.0
 */
public class CronOrmPersister extends AbstractOrmPersister {

    private static final Logger logger = LoggerFactory.getLogger(CronOrmPersister.class);

    /**
     * 持久化默认的延迟时间
     */
    private static final long DEFAULT_DELAY = 30 * TimeUtils.MILLIS_PER_SECOND;

    /**
     * cron表达式
     */
    private final CronExpression cronExpression;


    public CronOrmPersister(EntityDef entityDef, EntityCache<?, ?> entityCaches) {
        super(entityDef, entityCaches);
        this.cronExpression = CronExpression.parse(entityDef.getPersisterStrategy().getConfig());
    }


    @Override
    public void start() {
        schedulePersist();
    }

    private void schedulePersist() {
        var delay = 0L;
        try {
            var now = TimeUtils.now();
            var nextTimestamp = TimeUtils.nextTimestampByCronExpression(cronExpression, now);
            delay = nextTimestamp - now;

            if (delay < 0) {
                delay = DEFAULT_DELAY;
                logger.error("计算[cron:{}]表达式发生错误，当前时间[now:{}]，计算出来的时间[nextTimestamp:{}]，两者之差小于0"
                        , cronExpression.toString(), now, nextTimestamp);
            }
        } catch (Exception e) {
            delay = DEFAULT_DELAY;
            logger.error(ExceptionUtils.getMessage(e));
        }

        if (!OrmContext.isStop()) {
            SchedulerBus.schedule(() -> {
                if (!OrmContext.isStop()) {
                    EventBus.execute(entityDef.getClazz().hashCode(), () -> {
                        entityCaches.persistAll();
                        schedulePersist();
                    });
                }
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

}
