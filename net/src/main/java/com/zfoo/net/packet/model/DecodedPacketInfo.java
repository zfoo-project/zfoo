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

import com.zfoo.protocol.IPacket;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class DecodedPacketInfo {

    /**
     * 解码后的包
     */
    private IPacket packet;

    /**
     * 解码后的包的附加包
     */
    private IPacketAttachment packetAttachment;


    public static DecodedPacketInfo valueOf(IPacket packet, IPacketAttachment packetAttachment) {
        DecodedPacketInfo packetInfo = new DecodedPacketInfo();
        packetInfo.packet = packet;
        packetInfo.packetAttachment = packetAttachment;
        return packetInfo;
    }


    public IPacket getPacket() {
        return packet;
    }

    public void setPacket(IPacket packet) {
        this.packet = packet;
    }

    public IPacketAttachment getPacketAttachment() {
        return packetAttachment;
    }

    public void setPacketAttachment(IPacketAttachment packetAttachment) {
        this.packetAttachment = packetAttachment;
    }

}
