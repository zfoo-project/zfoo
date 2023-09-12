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

/**
 * @author godotg
 */
public class RunnableScheduler implements IScheduler {

    private Runnable runnable;

    public static RunnableScheduler valueOf(Runnable runnable) {
        var scheduler = new RunnableScheduler();
        scheduler.runnable = runnable;
        return scheduler;
    }

    @Override
    public void invoke() {
        runnable.run();
    }
}
