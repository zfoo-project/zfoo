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

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.PacketService;
import com.zfoo.protocol.buffer.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;


/**
 * @author jaysunxiao
 */
public class TunnelProtocolServer2Client {

    private long sid;

    private long uid;

    private ByteBuf retainedByteBuf;

    public static TunnelProtocolServer2Client valueOf(long sid, long uid, ByteBuf retainedByteBuf) {
        var tunnelProtocol = new TunnelProtocolServer2Client();
        tunnelProtocol.sid = sid;
        tunnelProtocol.uid = uid;
        tunnelProtocol.retainedByteBuf = retainedByteBuf;
        return tunnelProtocol;
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static class TunnelPacketInfo {

        public long sid;

        public long uid;

        public Object packet;

        public Object attachment;

        public TunnelPacketInfo(long sid, long uid, Object packet, Object attachment) {
            this.sid = sid;
            this.uid = uid;
            this.packet = packet;
            this.attachment = attachment;
        }
    }

    public static TunnelPacketInfo read(ByteBuf in) {
        var sid = ByteBufUtils.readLong(in);
        var uid = ByteBufUtils.readLong(in);
        var packetInfo = NetContext.getPacketService().read(in);
        return new TunnelPacketInfo(sid, uid, packetInfo.getPacket(), packetInfo.getAttachment());
    }
    // -----------------------------------------------------------------------------------------------------------------


    public void write(ByteBuf out) {
        try {
            out.ensureWritable(22);
            out.writerIndex(PacketService.PACKET_HEAD_LENGTH);
            ByteBufUtils.writeLong(out, sid);
            ByteBufUtils.writeLong(out, uid);
            out.writeBytes(retainedByteBuf);
            NetContext.getPacketService().writeHeaderBefore(out);
        } finally {
            ReferenceCountUtil.release(retainedByteBuf);
        }
    }
    // -----------------------------------------------------------------------------------------------------------------


    public long getSid() {
        return sid;
    }

    public long getUid() {
        return uid;
    }

    public ByteBuf getRetainedByteBuf() {
        return retainedByteBuf;
    }
}
