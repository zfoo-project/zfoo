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

import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.util.math.HashUtils;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 * @version 3.0
 */
public class GatewayAttachment implements IAttachment {

    public static final transient short PROTOCOL_ID = 1;

    /**
     * session id
     */
    private long sid;

    /**
     * EN:User ID, the message forwarded from the gateway to the back must be accompanied by the user's ID information,
     * otherwise it is impossible to know which user sent it, 0 means no user ID
     * <p>
     * CN:用戶Id，从网关转发到后面的消息必须要附带用户的Id信息，要不然无法知道是哪个用户发过来的，0代表没有用户id
     */
    private long uid;

    /**
     * EN:Whether to use a consistent hash ID as a consistent hash ID
     * CN:是否使用consistentHashId作为一致性hashId
     */
    private boolean useExecutorConsistentHash;
    private int executorConsistentHash;

    /**
     * true for the client, false for the server
     */
    private boolean client;


    /**
     * EN:The client may send an packet with synchronous or asynchronous to the gateway, and the gateway needs to bring this attachment when forwarding
     * CN:客户端发到网关的可能是一个带有同步或者异步的附加包，网关转发的时候需要把这个附加包给带上
     */
    private SignalAttachment signalAttachment;


    public GatewayAttachment() {
    }

    public GatewayAttachment(Session session, @Nullable SignalAttachment signalAttachment) {
        this.client = true;
        this.sid = session.getSid();
        var uid = session.getAttribute(AttributeType.UID);
        this.uid = uid == null ? 0 : (long) uid;
        this.signalAttachment = signalAttachment;
    }

    public GatewayAttachment(long sid, long uid) {
        this.sid = sid;
        this.uid = uid;
    }


    @Override
    public AttachmentType packetType() {
        return AttachmentType.GATEWAY_PACKET;
    }

    @Override
    public int executorConsistentHash() {
        if (useExecutorConsistentHash) {
            return executorConsistentHash;
        } else {
            return HashUtils.fnvHash(uid);
        }
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public void useExecutorConsistentHash(Object argument) {
        this.useExecutorConsistentHash = true;
        this.executorConsistentHash = HashUtils.fnvHash(argument);
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

    public boolean isUseExecutorConsistentHash() {
        return useExecutorConsistentHash;
    }

    public void setUseExecutorConsistentHash(boolean useExecutorConsistentHash) {
        this.useExecutorConsistentHash = useExecutorConsistentHash;
    }

    public int getExecutorConsistentHash() {
        return executorConsistentHash;
    }

    public void setExecutorConsistentHash(int executorConsistentHash) {
        this.executorConsistentHash = executorConsistentHash;
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
