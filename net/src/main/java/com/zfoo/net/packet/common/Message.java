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

import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;

/**
 * 通用的返回，既可以用在远程调用，又可以嵌套在其它协议里
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class Message implements IPacket {

    public static final transient short PROTOCOL_ID = 100;

    public static final Message DEFAULT = new Message();

    private byte module;

    /**
     * 1是成功，其它的均视为失败的请求
     */
    private int code;

    private String message;

    public static Message valueOf(IPacket packet, int code, String message) {
        var mess = new Message();
        mess.module = ProtocolManager.moduleByProtocolId(packet.protocolId()).getId();
        mess.code = code;
        mess.message = message;
        return mess;
    }

    public static Message valueOf(IPacket packet, int code) {
        return Message.valueOf(packet, code, null);
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public boolean success() {
        return code == 1;
    }

    public byte getModule() {
        return module;
    }

    public void setModule(byte module) {
        this.module = module;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
