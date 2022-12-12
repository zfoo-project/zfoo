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
 * @author godotg
 * @version 3.0
 */
@ProtobufClass
public class Pong implements IPacket {

    @Ignore
    public static final short PROTOCOL_ID = 104;

    /**
     * 服务器当前的时间戳
     */
    @Protobuf(order = 1)
    private long time;

    public static Pong valueOf(long time) {
        var pong = new Pong();
        pong.time = time;
        return pong;
    }

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
