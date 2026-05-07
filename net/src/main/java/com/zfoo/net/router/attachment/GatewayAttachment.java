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

import com.zfoo.net.session.Session;
import com.zfoo.protocol.anno.Protocol;

/**
 * @author godotg
 */
@Protocol(id = 2)
public class GatewayAttachment {

    /**
     * session id
     */
    private long sid;

    /**
     * User ID. Messages forwarded from the gateway must carry the user's ID so that downstream services
     * know which user sent the message. 0 means no user ID.
     */
    private long uid;

    /**
     * Determines which thread this message is processed on.
     */
    private int taskExecutorHash;

    /**
     * true for the client, false for the server
     */
    private boolean client;


    /**
     * A client packet sent to the gateway may carry a synchronous or asynchronous attachment.
     * The gateway must forward this attachment along with the packet.
     */
    private SignalAttachment signalAttachment;


    public GatewayAttachment() {
    }

    public GatewayAttachment(Session session) {
        this.client = true;
        this.sid = session.getSid();
        this.uid = session.getUid();
    }

    public GatewayAttachment(long sid, long uid) {
        this.sid = sid;
        this.uid = uid;
    }


    /**
     * Determines which thread this message is processed on.
     * Falls back to uid when taskExecutorHash is 0.
     */
    public int taskExecutorHash() {
        return taskExecutorHash == 0 ? (int) uid : taskExecutorHash;
    }


    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getTaskExecutorHash() {
        return taskExecutorHash;
    }

    public void setTaskExecutorHash(int taskExecutorHash) {
        this.taskExecutorHash = taskExecutorHash;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(boolean client) {
        this.client = client;
    }


    public SignalAttachment getSignalAttachment() {
        return signalAttachment;
    }

    public void setSignalAttachment(SignalAttachment signalAttachment) {
        this.signalAttachment = signalAttachment;
    }

}
