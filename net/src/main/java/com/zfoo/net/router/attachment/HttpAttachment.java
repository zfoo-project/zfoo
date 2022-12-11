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

import com.zfoo.util.math.HashUtils;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author godotg
 * @version 3.0
 */
public class HttpAttachment implements IAttachment {

    public static final transient short PROTOCOL_ID = 3;

    private long uid;

    private boolean useExecutorConsistentHash;

    private int executorConsistentHash;

    private transient FullHttpRequest fullHttpRequest;

    private transient HttpResponseStatus httpResponseStatus;

    public static HttpAttachment valueOf(FullHttpRequest fullHttpRequest, HttpResponseStatus httpResponseStatus) {
        var attachment = new HttpAttachment();
        attachment.fullHttpRequest = fullHttpRequest;
        attachment.httpResponseStatus = httpResponseStatus;
        return attachment;
    }

    @Override
    public AttachmentType packetType() {
        return AttachmentType.HTTP_PACKET;
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
