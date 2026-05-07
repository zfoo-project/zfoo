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

package com.zfoo.event.model;

import com.zfoo.protocol.util.RandomUtils;

/**
 * @author godotg
 */
public interface IEvent {

    /**
     * Returns a parameter used to determine which thread pool in EventBus the event should execute on. Only applies to async events.
     * <p>
     * For example, if the CPU has 4 cores, EventBus creates 8 executor thread pools. Modulo determines the target pool.
     * If the value is 0: 0 % 8 = 0, the async event executes in executors[0].
     * If the value is 1: 1 % 8 = 1, the async event executes in executors[1].
     * If the value is 8: 8 % 8 = 0, the async event executes in executors[0].
     * If the value is 9: 9 % 8 = 1, the async event executes in executors[1].
     * By choosing the return value, you can control which thread pool handles the async event.
     * Since each pool is single-threaded, routing related events to the same pool avoids locking and improves throughput.
     * By default returns a random value, so async events may execute on any thread pool if this method is not overridden.
     * @return the parameter used to select the target thread pool
     */
    default int executorHash() {
        return RandomUtils.randomInt();
    }

}
