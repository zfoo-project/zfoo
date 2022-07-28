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

import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.task.TaskBus;
import com.zfoo.net.task.model.PacketReceiverTask;
import com.zfoo.util.math.HashUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author godotg
 * @version 3.0
 */
public class ConsistentHashTaskDispatch extends AbstractTaskDispatch {

    private static final ConsistentHashTaskDispatch INSTANCE = new ConsistentHashTaskDispatch();

    public static ConsistentHashTaskDispatch getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public Executor getExecutor(ExecutorService[] executors, PacketReceiverTask packetReceiverTask) {
        var attachment = packetReceiverTask.getAttachment();

        if (attachment == null) {
            var session = packetReceiverTask.getSession();
            var uid = session.getAttribute(AttributeType.UID);

            if (uid == null) {
                return SessionIdTaskDispatch.getInstance().getExecutor(executors, packetReceiverTask);
            } else {
                return executors[TaskBus.executorIndex(HashUtils.fnvHash(uid))];
            }
        }

        // 可见最终是根据附加包的信息选择服务端由哪个线程执行这个业务
        return executors[TaskBus.executorIndex(attachment.executorConsistentHash())];
    }

}
