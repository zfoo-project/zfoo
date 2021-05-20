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

package com.zfoo.net.task.model;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.model.IPacketAttachment;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.IPacket;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public final class ReceiveTask implements Runnable {

    private Session session;
    private IPacket packet;
    private IPacketAttachment packetAttachment;

    public ReceiveTask(Session session, IPacket packet, IPacketAttachment packetAttachment) {
        this.session = session;
        this.packet = packet;
        this.packetAttachment = packetAttachment;
    }

    @Override
    public void run() {
        NetContext.getDispatcher().doReceive(session, packet, packetAttachment);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
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
