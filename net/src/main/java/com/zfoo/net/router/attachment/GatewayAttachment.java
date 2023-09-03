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
 * @version 3.0
 */
@Protocol(id = 2)
public class GatewayAttachment implements IAttachment {

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

    private boolean useTaskExecutorHashParam;
    private int taskExecutorHashParam;

    /**
     * true for the client, false for the server
     */
    private boolean client;


    /**
     * EN:The client may send an packet with synchronous or asynchronous to the gateway, and the gateway needs to bring this attachment when forwarding
     * CN:客户端发到网关的可能是一个带有同步或者异步的附加包，网关转发的时候需要把这个附加包给带上
     */
    private SignalAttachment signalAttachment;
    private SignalOnlyAttachment signalOnlyAttachment;


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


    @Override
    public AttachmentType packetType() {
        return AttachmentType.GATEWAY_PACKET;
    }

    /**
     * EN:Used to determine which thread the message is processed on
     * CN:用来确定这条消息在哪一个线程处理
     */
    public int taskExecutorHash() {
        return useTaskExecutorHashParam ? taskExecutorHashParam : (int) uid;
    }

    public void wrapTaskExecutorHash(Object argument) {
        this.useTaskExecutorHashParam = true;
        this.taskExecutorHashParam = argument.hashCode();
    }

    public void wrapAttachment(IAttachment attachment) {
        if (attachment == null) {
            return;
        }
        switch (attachment.packetType()) {
            case SIGNAL_ONLY_PACKET:
                signalOnlyAttachment = (SignalOnlyAttachment) attachment;
                break;
            case SIGNAL_PACKET:
                signalAttachment = (SignalAttachment) attachment;
                break;
            default:
        }
    }

    public IAttachment attachment() {
        if (signalAttachment != null) {
            return signalAttachment;
        }
        if (signalOnlyAttachment != null) {
            return signalOnlyAttachment;
        }
        return null;
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

    public boolean isUseTaskExecutorHashParam() {
        return useTaskExecutorHashParam;
    }

    public void setUseTaskExecutorHashParam(boolean useTaskExecutorHashParam) {
        this.useTaskExecutorHashParam = useTaskExecutorHashParam;
    }

    public int getTaskExecutorHashParam() {
        return taskExecutorHashParam;
    }

    public void setTaskExecutorHashParam(int taskExecutorHashParam) {
        this.taskExecutorHashParam = taskExecutorHashParam;
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

    public SignalOnlyAttachment getSignalOnlyAttachment() {
        return signalOnlyAttachment;
    }

    public void setSignalOnlyAttachment(SignalOnlyAttachment signalOnlyAttachment) {
        this.signalOnlyAttachment = signalOnlyAttachment;
    }
}
