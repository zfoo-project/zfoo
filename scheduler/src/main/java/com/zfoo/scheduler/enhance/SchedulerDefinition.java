/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.scheduler.enhance;

import com.zfoo.protocol.util.GraalVmUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.scheduler.util.TimeUtils;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.springframework.scheduling.support.CronExpression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Trigger timestamp: the scheduler fires whenever the current timestamp exceeds this value.
 *
 * @author godotg
 */
public class SchedulerDefinition {

    private CronExpression cronExpression;

    private IScheduler scheduler;

    private long triggerTimestamp;

    public static SchedulerDefinition valueOf(String cron, Object bean, Method method) throws NoSuchMethodException, IllegalAccessException, InstantiationException, CannotCompileException, NotFoundException, InvocationTargetException {
        var schedulerDef = new SchedulerDefinition();
        var cronExpression = CronExpression.parse(cron);
        schedulerDef.cronExpression = cronExpression;
        if (GraalVmUtils.isGraalVM()) {
            schedulerDef.scheduler = ReflectScheduler.valueOf(bean, method);
        } else {
            // bytecode enhancements to avoid reflection
            schedulerDef.scheduler = EnhanceUtils.createScheduler(ReflectScheduler.valueOf(bean, method));
        }
        schedulerDef.triggerTimestamp = TimeUtils.nextTimestampByCronExpression(cronExpression, TimeUtils.currentTimeMillis());
        ReflectionUtils.makeAccessible(method);
        return schedulerDef;
    }

    public static SchedulerDefinition valueOf(String cron, Runnable runnable) {
        var schedulerDef = new SchedulerDefinition();
        var cronExpression = CronExpression.parse(cron);
        schedulerDef.cronExpression = cronExpression;
        schedulerDef.scheduler = RunnableScheduler.valueOf(runnable);
        schedulerDef.triggerTimestamp = TimeUtils.nextTimestampByCronExpression(cronExpression, TimeUtils.currentTimeMillis());
        return schedulerDef;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    public IScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(IScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public long getTriggerTimestamp() {
        return triggerTimestamp;
    }

    public void setTriggerTimestamp(long triggerTimestamp) {
        this.triggerTimestamp = triggerTimestamp;
    }
}
