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

package com.zfoo.net.router.attachment;

import com.zfoo.protocol.anno.Protocol;

/**
 * not used attachment
 *
 * @author godotg
 */
@Protocol(id = 5)
public class NoAnswerAttachment {
    private int taskExecutorHash;

    public static NoAnswerAttachment valueOf(int taskExecutorHash) {
        var attachment = new NoAnswerAttachment();
        attachment.taskExecutorHash = taskExecutorHash;
        return attachment;
    }

    public int getTaskExecutorHash() {
        return taskExecutorHash;
    }

    public void setTaskExecutorHash(int taskExecutorHash) {
        this.taskExecutorHash = taskExecutorHash;
    }
}
