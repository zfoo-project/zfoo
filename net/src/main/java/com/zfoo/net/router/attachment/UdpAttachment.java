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

import com.zfoo.util.math.RandomUtils;

/**
 * @author godotg
 * @version 3.0
 */
public class UdpAttachment implements IAttachment {

    public static final short PROTOCOL_ID = 3;

    private String host;
    private int port;

    public static UdpAttachment valueOf(String host, int port) {
        var attachment = new UdpAttachment();
        attachment.host = host;
        attachment.port = port;
        return attachment;
    }

    @Override
    public AttachmentType packetType() {
        return AttachmentType.UDP_PACKET;
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
