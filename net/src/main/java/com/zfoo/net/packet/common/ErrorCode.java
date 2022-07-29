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

package com.zfoo.net.packet.common;

import com.baidu.bjf.remoting.protobuf.annotation.Ignore;
import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.zfoo.protocol.IPacket;

/**
 * @author meiw
 * @version 3.0
 */
@ProtobufClass
public class ErrorCode implements IPacket {

    @Ignore
    public static final transient short PROTOCOL_ID = 105;

    @Protobuf(order = 1)
    private int protocolId;
    @Protobuf(order = 2)
    private int errorCode;
    @Protobuf(order = 3)
    private String errorMessage;

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public static ErrorCode valueOf(int protocolId, int errorCode, String errorMessage) {
        ErrorCode response = new ErrorCode();
        response.protocolId = protocolId;
        response.errorCode = errorCode;
        response.errorMessage = errorMessage;
        return response;
    }

    public static ErrorCode valueOf(IPacket packet, int errorCode, String errorMessage) {
        ErrorCode response = new ErrorCode();
        response.protocolId = packet.protocolId();
        response.errorCode = errorCode;
        response.errorMessage = errorMessage;
        return response;
    }

    public static ErrorCode valueOf(IPacket packet, int errorCode) {
        return valueOf(packet, errorCode, "");
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
