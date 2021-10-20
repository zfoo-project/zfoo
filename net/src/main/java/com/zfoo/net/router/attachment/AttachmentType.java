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
 * @author jaysunxiao
 * @version 3.0
 */
public enum AttachmentType {

    /**
     * 带有同步或者异步信息的附加包
     */
    SIGNAL_PACKET((byte) 0, SignalAttachment.class),

    /**
     * 带有网关信息的附加包
     */
    GATEWAY_PACKET((byte) 1, GatewayAttachment.class),

    /**
     * udp消息的附加包
     */
    UDP_PACKET((byte) 2, UdpAttachment.class),


    /**
     * http消息的附加包
     */
    HTTP_PACKET((byte) 3, HttpAttachment.class),

    /**
     * 无返回消息的附加包
     */
    NO_ANSWER_PACKET((byte) 4, NoAnswerAttachment.class),


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
