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

package com.zfoo.net.task;

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.IPacket;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.session.Session;

/**
 * @author godotg
 * @version 3.0
 */
public final class PacketReceiverTask implements Runnable {

    private Session session;
    private IPacket packet;
    private IAttachment attachment;

    public PacketReceiverTask(Session session, IPacket packet, IAttachment attachment) {
        this.session = session;
        this.packet = packet;
        this.attachment = attachment;
    }

    @Override
    public void run() {
        NetContext.getRouter().atReceiver(session, packet, attachment);
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

    public IAttachment getAttachment() {
        return attachment;
    }

    public void setAttachment(IAttachment attachment) {
        this.attachment = attachment;
    }
}
