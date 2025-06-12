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

package com.zfoo.net.packet;

import org.springframework.lang.Nullable;

/**
 * 被解码后的Packet的信息
 *
 * @author godotg
 */
public class EncodedPacketInfo {

    private long sid;
    private long uid;

    /**
     * 解码后的包
     */
    private Object packet;

    /**
     * 解码后的包的附加包
     */
    private Object attachment;


    public static EncodedPacketInfo valueOf(long sid, long uid, Object packet, @Nullable Object attachment) {
        EncodedPacketInfo packetInfo = new EncodedPacketInfo();
        packetInfo.sid = sid;
        packetInfo.uid = uid;
        packetInfo.packet = packet;
        packetInfo.attachment = attachment;
        return packetInfo;
    }

    public long getSid() {
        return sid;
    }

    public long getUid() {
        return uid;
    }

    public Object getPacket() {
        return packet;
    }

    public Object getAttachment() {
        return attachment;
    }
}
