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
 *
 */

package com.zfoo.net.task.dispatcher;

import com.zfoo.net.task.TaskBus;
import com.zfoo.net.task.model.PacketReceiverTask;
import com.zfoo.util.math.RandomUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author godotg
 * @version 3.0
 */
public class RandomTaskDispatch extends AbstractTaskDispatch {

    private static final RandomTaskDispatch INSTANCE = new RandomTaskDispatch();

    public static RandomTaskDispatch getInstance() {
        return INSTANCE;
    }

    @Override
    public Executor getExecutor(ExecutorService[] executors, PacketReceiverTask packetReceiverTask) {
        return executors[TaskBus.executorIndex(RandomUtils.randomInt())];
    }

}
