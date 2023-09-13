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

import com.zfoo.protocol.anno.Protocol;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author godotg
 */
@Protocol(id = 4)
public class HttpAttachment {

    private long uid;

    private int taskExecutorHash;

    private transient FullHttpRequest fullHttpRequest;

    private transient HttpResponseStatus httpResponseStatus;

    public static HttpAttachment valueOf(FullHttpRequest fullHttpRequest, HttpResponseStatus httpResponseStatus) {
        var attachment = new HttpAttachment();
        attachment.fullHttpRequest = fullHttpRequest;
        attachment.httpResponseStatus = httpResponseStatus;
        return attachment;
    }


    /**
     * EN:Used to determine which thread the message is processed on
     * CN:用来确定这条消息在哪一个线程处理
     */
    public int taskExecutorHash() {
        return taskExecutorHash == 0 ? (int) uid : taskExecutorHash;
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

    public FullHttpRequest getFullHttpRequest() {
        return fullHttpRequest;
    }

    public void setFullHttpRequest(FullHttpRequest fullHttpRequest) {
        this.fullHttpRequest = fullHttpRequest;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return httpResponseStatus;
    }

    public void setHttpResponseStatus(HttpResponseStatus httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
    }
}
