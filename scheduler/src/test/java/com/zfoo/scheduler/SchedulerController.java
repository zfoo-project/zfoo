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

package com.zfoo.scheduler;

import com.zfoo.scheduler.anno.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author godotg
 */
@Component
public class SchedulerController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);

    @Scheduler(cron = "0/5 * * * * ?")
    public void cronScheduler1() {
        logger.info("scheduler1 每5秒时间调度任务");
    }

    @Scheduler(cron = "0,10,20,40 * * * * ?")
    public void cronScheduler2() {
        logger.info("scheduler2 每分钟的10秒，20秒，40秒调度任务");
    }

}
