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

package com.zfoo.net.packet.jprotobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.zfoo.net.packet.IPacket;
import com.zfoo.protocol.anno.Protocol;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 1500)
public class JProtobufHelloRequest implements IPacket {

    @Protobuf(order = 1)
    private String message;

    public static JProtobufHelloRequest valueOf(String message) {
        var request = new JProtobufHelloRequest();
        request.message = message;
        return request;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
