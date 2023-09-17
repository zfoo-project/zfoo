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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zfoo.protocol.anno.Note;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author godotg
 */
@Protocol(id = 0)
@JsonIgnoreProperties("responseFuture")
public class SignalAttachment {

    /**
     * EN:Negative signalId are allowed
     * CN:允许负数的signalId
     */
    public static final AtomicInteger ATOMIC_ID = new AtomicInteger(0);
    /**
     * 0 for the server, 1 or 2 for the sync or async native client, 12 for the outside client such as browser, mobile
     */
    public static final byte SIGNAL_SERVER = 0;
    public static final byte SIGNAL_NATIVE_ARGUMENT_CLIENT = 1;
    public static final byte SIGNAL_NATIVE_NO_ARGUMENT_CLIENT = 2;
    public static final byte SIGNAL_OUTSIDE_CLIENT = 12;

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

    @Note("0 for the server, 1 or 2 for the sync or async native client, 12 for the outside client such as browser, mobile")
    private byte client = SIGNAL_NATIVE_ARGUMENT_CLIENT;

    /**
     * The timestamp the client sent it
     */
    private long timestamp = TimeUtils.now();

    /**
     * EN:The method of callback when the client receives a reply from the server
     * CN:客户端收到服务器回复的时候回调的方法
     */
    @JsonIgnore
    private transient CompletableFuture<Object> responseFuture = new CompletableFuture<>();

    public SignalAttachment() {
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

    public byte getClient() {
        return client;
    }

    public void setClient(byte client) {
        this.client = client;
    }

    public CompletableFuture<Object> getResponseFuture() {
        return responseFuture;
    }

    public void setResponseFuture(CompletableFuture<Object> responseFuture) {
        this.responseFuture = responseFuture;
    }
}
