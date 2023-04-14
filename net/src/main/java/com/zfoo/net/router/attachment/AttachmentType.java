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

import java.util.HashMap;
import java.util.Map;

/**
 * @author godotg
 * @version 3.0
 */
public enum AttachmentType {

    /**
     * synchronous or asynchronous attachment
     */
    SIGNAL_PACKET((byte) 0, SignalAttachment.class),

    SIGNAL_ONLY_PACKET((byte) 1, SignalOnlyAttachment.class),

    /**
     * gateway attachment
     */
    GATEWAY_PACKET((byte) 2, GatewayAttachment.class),

    /**
     * udp attachment
     */
    UDP_PACKET((byte) 3, UdpAttachment.class),


    /**
     * http attachment
     */
    HTTP_PACKET((byte) 4, HttpAttachment.class),

    /**
     * not used attachment
     */
    NO_ANSWER_PACKET((byte) 5, NoAnswerAttachment.class),


    ;


    public static final Map<Byte, AttachmentType> map = new HashMap<>(values().length);

    static {
        for (var packetType : AttachmentType.values()) {
            map.put(packetType.packetType, packetType);
        }
    }

    private final byte packetType;

    public byte getPacketType() {
        return packetType;
    }

    private final Class<? extends IAttachment> clazz;

    AttachmentType(byte packetType, Class<? extends IAttachment> clazz) {
        this.packetType = packetType;
        this.clazz = clazz;
    }

    public static AttachmentType getPacketType(byte packetType) {
        return map.getOrDefault(packetType, AttachmentType.NO_ANSWER_PACKET);
    }

}
