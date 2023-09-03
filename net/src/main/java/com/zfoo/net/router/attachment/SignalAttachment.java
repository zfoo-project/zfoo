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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zfoo.net.packet.IPacket;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 0)
public class SignalAttachment implements IAttachment {

    /**
     * EN:Negative signalId are allowed
     * CN:允许负数的signalId
     */
    public static final AtomicInteger ATOMIC_ID = new AtomicInteger(0);

    /**
     * EN:Unique identification of a packet, unique representation of an attachment, hashcode() and equals() equals signalId value
     * CN:唯一标识一个packet， 唯一表示一个Attachment，hashcode() and equals() 等于signalId
     */
    private int signalId = ATOMIC_ID.incrementAndGet();

    /**
     * EN:The parameter used to calculate the hash in TaskBus to determine which thread the task is executed on
     * CN:用来在TaskBus中计算hash的参数，用来决定任务在哪一条线程执行
     */
    private int taskExecutorHash = -1;

    /**
     * true for the client, false for the server
     */
    private boolean client = true;

    /**
     * The time the client sent it
     */
    private long timestamp = TimeUtils.now();

    /**
     * EN:The method of callback when the client receives a reply from the server
     * CN:客户端收到服务器回复的时候回调的方法
     */
    @JsonIgnore
    private transient CompletableFuture<IPacket> responseFuture = new CompletableFuture<>();

    public SignalAttachment() {
    }


    @Override
    public AttachmentType packetType() {
        return AttachmentType.SIGNAL_PACKET;
    }

    /**
     * EN:Used to determine which thread the message is processed on
     * CN:用来确定这条消息在哪一个线程处理
     */
    public int taskExecutorHash() {
        return taskExecutorHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SignalAttachment that = (SignalAttachment) o;
        return signalId == that.signalId;
    }

    @Override
    public int hashCode() {
        return signalId;
    }

    public int getSignalId() {
        return signalId;
    }

    public void setSignalId(int signalId) {
        this.signalId = signalId;
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


    public CompletableFuture<IPacket> getResponseFuture() {
        return responseFuture;
    }

    public void setResponseFuture(CompletableFuture<IPacket> responseFuture) {
        this.responseFuture = responseFuture;
    }
}
