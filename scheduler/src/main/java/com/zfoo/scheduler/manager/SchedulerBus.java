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
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.SchedulerContext;
import com.zfoo.scheduler.enhance.SchedulerDefinition;
import com.zfoo.scheduler.util.TimeUtils;
import io.netty.util.concurrent.FastThreadLocalThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
public abstract class SchedulerBus {


    private static final Logger logger = LoggerFactory.getLogger(SchedulerBus.class);

    private static final List<SchedulerDefinition> schedulerDefList = new CopyOnWriteArrayList<>();
    /**
     * scheduler默认只有一个单线程的线程池
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory(1));

    /**
     * executor创建的线程id号
     */
    private static long threadId = 0;

    /**
     * 上一次trigger触发时间
     */
    private static long lastTriggerTimestamp = 0L;


    /**
     * 在scheduler中，最小的triggerTimestamp
     */
    private static long minTriggerTimestamp = 0L;

    static {
        executor.scheduleAtFixedRate(() -> {
            try {
                triggerPerSecond();
            } catch (Exception e) {
                logger.error("scheduler triggers an error.", e);
            }
        }, 0, TimeUtils.MILLIS_PER_SECOND, TimeUnit.MILLISECONDS);
    }

    public static class SchedulerThreadFactory implements ThreadFactory {

        private final int poolNumber;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;

        public SchedulerThreadFactory(int poolNumber) {
            this.group = Thread.currentThread().getThreadGroup();
            this.poolNumber = poolNumber;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            var threadName = StringUtils.format("scheduler-p{}-t{}", poolNumber, threadNumber.getAndIncrement());
            var thread = new FastThreadLocalThread(group, runnable, threadName);
            thread.setDaemon(false);
            thread.setPriority(Thread.NORM_PRIORITY);
            thread.setUncaughtExceptionHandler((t, e) -> logger.error(t.toString(), e));
            threadId = thread.getId();
            return thread;
        }

    }


    public static void refreshMinTriggerTimestamp() {
        var minTimestamp = Long.MAX_VALUE;
        for (var scheduler : schedulerDefList) {
            if (scheduler.getTriggerTimestamp() < minTimestamp) {
                minTimestamp = scheduler.getTriggerTimestamp();
            }
        }
        minTriggerTimestamp = minTimestamp;
    }

    /**
     * 每一秒执行一次，如果这个任务执行时间过长超过，比如10秒，执行完成后，不会再执行10次
     */
    private static void triggerPerSecond() {
        var currentTimeMillis = TimeUtils.currentTimeMillis();

        if (CollectionUtils.isEmpty(schedulerDefList)) {
            return;
        }


        // 有人向前调整过机器时间，重新计算scheduler里的triggerTimestamp
        // var diff = timestamp - lastTriggerTimestamp;
        if (currentTimeMillis < lastTriggerTimestamp) {
            for (SchedulerDefinition schedulerDef : schedulerDefList) {
                var nextTriggerTimestamp = TimeUtils.nextTimestampByCronExpression(schedulerDef.getCronExpression(), currentTimeMillis);
                schedulerDef.setTriggerTimestamp(nextTriggerTimestamp);
            }
            refreshMinTriggerTimestamp();
        }

        // diff > 0, 没有人调整时间或者有人向后调整过机器时间，可以忽略，因为向后调整时间时间戳一定会大于triggerTimestamp，所以一定会触发
        lastTriggerTimestamp = currentTimeMillis;

        // 如果minSchedulerTriggerTimestamp大于timestamp，说明没有可执行的scheduler
        if (currentTimeMillis < minTriggerTimestamp) {
            return;
        }

        var minTimestamp = Long.MAX_VALUE;
        var timestampZonedDataTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), TimeUtils.DEFAULT_ZONE_ID);
        for (var scheduler : schedulerDefList) {
            var triggerTimestamp = scheduler.getTriggerTimestamp();
            if (triggerTimestamp <= currentTimeMillis) {
                // 到达触发时间，则执行runnable方法
                try {
                    scheduler.getScheduler().invoke();
                } catch (Throwable t) {
                    logger.error("scheduler任务调度未知异常", t);
                }
                // 重新设置下一次的触发时间戳
                triggerTimestamp = TimeUtils.nextTimestampByCronExpression(scheduler.getCronExpression(), timestampZonedDataTime);
                scheduler.setTriggerTimestamp(triggerTimestamp);
            }
            if (triggerTimestamp < minTimestamp) {
                minTimestamp = scheduler.getTriggerTimestamp();
            }
        }
        minTriggerTimestamp = minTimestamp;
    }

    public static void registerScheduler(SchedulerDefinition scheduler) {
        schedulerDefList.add(scheduler);
        refreshMinTriggerTimestamp();
    }


    /**
     * 不断执行的周期循环任务
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit unit) {

        return executor.scheduleAtFixedRate(ThreadUtils.safeRunnable(runnable), 0, period, unit);
    }


    /**
     * 固定延迟执行的任务
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {

       return executor.schedule(ThreadUtils.safeRunnable(runnable), delay, unit);
    }

    /**
     * cron表达式执行的任务
     */
    public static void scheduleCron(Runnable runnable, String cron) {
        if (SchedulerContext.isStop()) {
            return;
        }

        registerScheduler(SchedulerDefinition.valueOf(cron, runnable));
    }

    public static Executor threadExecutor(long currentThreadId) {
        return threadId == currentThreadId ? executor : null;
    }
}
