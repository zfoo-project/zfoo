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
 * 主要用来支持godot，unity，ts这种异步语法的使用，做为async/await语法的支撑
 *
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 1)
public class SignalOnlyAttachment implements IAttachment {

    private int signalId;

    private long timestamp;

    @Override
    public AttachmentType packetType() {
        return AttachmentType.SIGNAL_ONLY_PACKET;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignalOnlyAttachment that = (SignalOnlyAttachment) o;
        return signalId == that.signalId;
    }

    @Override
    public int hashCode() {
        return signalId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getSignalId() {
        return signalId;
    }

    public void setSignalId(int signalId) {
        this.signalId = signalId;
    }

}
