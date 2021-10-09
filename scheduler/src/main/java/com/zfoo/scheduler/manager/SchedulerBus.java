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

package com.zfoo.scheduler.manager;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.scheduler.SchedulerContext;
import com.zfoo.scheduler.model.vo.SchedulerDefinition;
import com.zfoo.scheduler.timeWheelUtils.Timer;
import com.zfoo.scheduler.timeWheelUtils.TimerTask;
import com.zfoo.scheduler.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class SchedulerBus {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerBus.class);

    private static Timer timer = new Timer();

    /**
     * scheduler默认只有一个单线程的线程池
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory(1));


    public static void registerScheduler(SchedulerDefinition scheduler) {
        var timerTask = new TimerTask(scheduler.getTriggerTimestamp(), () -> {
            scheduler.getScheduler().invoke();
            refreshTask(scheduler);
        });
        timer.addTask(timerTask);
    }


    /**
     * 不断执行的周期循环任务
     */
    public static void scheduleAtFixedRate(Runnable runnable, long period, TimeUnit unit) {
        if (SchedulerContext.isStop()) {
            return;
        }

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("scheduleAtFixedRate未知exception异常", e);
                } catch (Throwable t) {
                    logger.error("scheduleAtFixedRate未知error异常", t);
                }
            }
        }, 0, period, unit);
    }


    /**
     * 固定延迟执行的任务
     */
    public static void schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (SchedulerContext.isStop()) {
            return;
        }

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } catch (Exception e) {
                    logger.error("schedule未知exception异常", e);
                } catch (Throwable t) {
                    logger.error("schedule未知error异常", t);
                }
            }
        }, delay, unit);
    }

    /**
     * cron表达式执行的任务
     */
    public static void scheduleCron(Runnable runnable, String cron) {
        if (SchedulerContext.isStop()) {
            return;
        }

        SchedulerDefinition scheduler = SchedulerDefinition.valueOf(cron, runnable);
        var timerTask = new TimerTask(scheduler.getTriggerTimestamp(), () -> {
            scheduler.getScheduler().invoke();
            refreshTask(scheduler);
        });
        timer.addTask(timerTask);
    }

    public static void refreshTask(SchedulerDefinition schedulerDefinition) {
        var timestamp = TimeUtils.currentTimeMillis();
        var nextTriggerTimestamp = TimeUtils.getNextTimestampByCronExpression(schedulerDefinition.getCronExpression(), timestamp);
        schedulerDefinition.setTriggerTimestamp(nextTriggerTimestamp);
        var timerTask = new TimerTask(schedulerDefinition.getTriggerTimestamp(), () -> {
            schedulerDefinition.getScheduler().invoke();
            refreshTask(schedulerDefinition);
        });
        timer.addTask(timerTask);
    }
}
