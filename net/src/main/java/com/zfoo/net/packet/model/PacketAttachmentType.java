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

package com.zfoo.net.packet.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public enum PacketAttachmentType {

    /**
     * 正常的附加包
     */
    NORMAL_PACKET((byte) 0, null),

    /**
     * 带有同步或者异步信息的附加包
     */
    SIGNAL_PACKET((byte) 1, SignalPacketAttachment.class),

    /**
     * 带有网关信息的附加包
     */
    GATEWAY_PACKET((byte) 2, GatewayPacketAttachment.class),

    /**
     * udp消息的附加包
     */
    UDP_PACKET((byte) 3, UdpPacketAttachment.class),

    /**
     * 无返回消息的附加包
     */
    NO_ANSWER_PACKET((byte) 4, NoAnswerAttachment.class),

    
    ;


    public static final Map<Byte, PacketAttachmentType> map = new HashMap<>(values().length);

    static {
        for (var packetType : PacketAttachmentType.values()) {
            map.put(packetType.packetType, packetType);
        }
    }

    public static PacketAttachmentType getPacketType(byte packetType) {
        return map.getOrDefault(packetType, PacketAttachmentType.NORMAL_PACKET);
    }

    public byte getPacketType() {
        return packetType;
    }

    private byte packetType;
    private Class<? extends IPacketAttachment> clazz;

    PacketAttachmentType(byte packetType, Class<? extends IPacketAttachment> clazz) {
        this.packetType = packetType;
        this.clazz = clazz;
    }

}
