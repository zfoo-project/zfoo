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

package com.zfoo.net.handler.codec.json;

/**
 * @author godotg
 */
public class JsonPacket {

    private short protocolId;

    private short attachmentId;

    private Object packet;
    private Object attachment;


    public static JsonPacket valueOf(short protocolId, Object packet, short attachmentId, Object attachment) {
        var jsonPacket = new JsonPacket();
        jsonPacket.protocolId = protocolId;
        jsonPacket.attachmentId = attachmentId;
        jsonPacket.packet = packet;
        jsonPacket.attachment = attachment;
        return jsonPacket;
    }

    public short getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(short protocolId) {
        this.protocolId = protocolId;
    }

    public short getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(short attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Object getPacket() {
        return packet;
    }

    public void setPacket(Object packet) {
        this.packet = packet;
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }
}
