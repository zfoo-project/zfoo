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

package com.zfoo.net.core.proxy;

import io.netty.buffer.ByteBuf;


/**
 * @author jaysunxiao
 */
public class TunnelProtocolServer2Client {

    private long sid;

    private ByteBuf byteBuf;

    public static TunnelProtocolServer2Client valueOf(long sid, ByteBuf byteBuf) {
        var tunnelProtocol = new TunnelProtocolServer2Client();
        tunnelProtocol.sid = sid;
        tunnelProtocol.byteBuf = byteBuf;
        return tunnelProtocol;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    public void setByteBuf(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }
}
