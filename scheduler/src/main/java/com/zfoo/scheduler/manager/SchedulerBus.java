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
     * Scheduler uses a single-threaded executor by default
     */
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory(1));


    /**
     * Timestamp of the last trigger invocation
     */
    private static long lastTriggerTimestamp = 0L;


    /**
     * The minimum triggerTimestamp among all registered schedulers
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
            ThreadUtils.registerSingleThreadExecutor(thread, executor);
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
     * Executed once per second. If this task takes longer than expected (e.g. 10 seconds),
     * it will NOT catch up by running 10 times after completion.
     */
    private static void triggerPerSecond() {
        var currentTimeMillis = TimeUtils.currentTimeMillis();

        if (CollectionUtils.isEmpty(schedulerDefList)) {
            return;
        }


        // Someone moved the system clock backward; recalculate triggerTimestamp for all schedulers
        // var diff = timestamp - lastTriggerTimestamp;
        if (currentTimeMillis < lastTriggerTimestamp) {
            for (SchedulerDefinition schedulerDef : schedulerDefList) {
                var nextTriggerTimestamp = TimeUtils.nextTimestampByCronExpression(schedulerDef.getCronExpression(), currentTimeMillis);
                schedulerDef.setTriggerTimestamp(nextTriggerTimestamp);
            }
            refreshMinTriggerTimestamp();
        }

        // diff > 0: no clock adjustment, or clock was moved forward; can be ignored since the timestamp will exceed triggerTimestamp and will always trigger
        lastTriggerTimestamp = currentTimeMillis;

        // If minTriggerTimestamp is greater than currentTimeMillis, no scheduler is ready to execute
        if (currentTimeMillis < minTriggerTimestamp) {
            return;
        }

        var minTimestamp = Long.MAX_VALUE;
        var timestampZonedDataTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(currentTimeMillis), TimeUtils.DEFAULT_ZONE_ID);
        for (var scheduler : schedulerDefList) {
            var triggerTimestamp = scheduler.getTriggerTimestamp();
            if (triggerTimestamp <= currentTimeMillis) {
                // Trigger time reached, invoke the scheduler
                try {
                    scheduler.getScheduler().invoke();
                } catch (Exception e) {
                    logger.error("scheduler invoke exception", e);
                } catch (Throwable t) {
                    logger.error("scheduler invoke error", t);
                }
                // Reset the next trigger timestamp
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
     * Periodically recurring task that runs continuously
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long period, TimeUnit unit) {

        return executor.scheduleAtFixedRate(ThreadUtils.safeRunnable(runnable), 0, period, unit);
    }


    /**
     * Task executed with a fixed delay
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {

        return executor.schedule(ThreadUtils.safeRunnable(runnable), delay, unit);
    }

    /**
     * Task triggered by a cron expression
     */
    public static void scheduleCron(Runnable runnable, String cron) {
        if (SchedulerContext.isStop()) {
            return;
        }

        registerScheduler(SchedulerDefinition.valueOf(cron, runnable));
    }

    /**
     * Task executed immediately
     */
    public static void execute(Runnable runnable) {
        executor.execute(ThreadUtils.safeRunnable(runnable));
    }

}
