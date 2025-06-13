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
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.buffer.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;


/**
 * protocol schema:
 * <p>
 * 4byte header length
 * 1byte flag, 0 is packet, 10 is register client, 30 is register uid
 * body
 *
 * @author jaysunxiao
 */
public class TunnelProtocolClient2Server {

    public static final byte FLAG_PACKET = 0;
    public static final byte FLAG_REGISTER = 10;
    public static final byte FLAG_HEARTBEAT = 20;

    private long sid;

    private long uid;

    private ByteBuf byteBuf;

    public static TunnelProtocolClient2Server valueOf(long sid, long uid, ByteBuf byteBuf) {
        var tunnelProtocol = new TunnelProtocolClient2Server();
        tunnelProtocol.sid = sid;
        tunnelProtocol.uid = uid;
        tunnelProtocol.byteBuf = byteBuf;
        return tunnelProtocol;
    }

    public long getSid() {
        return sid;
    }

    public long getUid() {
        return uid;
    }

    public ByteBuf getByteBuf() {
        return byteBuf;
    }


    // -----------------------------------------------------------------------------------------------------------------
    public static void writePacket(ByteBuf out, EncodedPacketInfo encodedPacketInfo) {
        out.ensureWritable(4);
        out.writerIndex(PacketService.PACKET_HEAD_LENGTH);
        out.writeByte(FLAG_PACKET);
        ByteBufUtils.writeLong(out, encodedPacketInfo.getSid());
        ByteBufUtils.writeLong(out, encodedPacketInfo.getUid());

        var packet = encodedPacketInfo.getPacket();
        var attachment = encodedPacketInfo.getAttachment();
        NetContext.getPacketService().write(out, packet, attachment);
        NetContext.getPacketService().writeHeaderBefore(out);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static class TunnelRegister {
        public long sid;

        public TunnelRegister(long sid) {
            this.sid = sid;
        }
    }

    public static void writeRegister(ByteBuf out, TunnelRegister register) {
        out.ensureWritable(4);
        out.writerIndex(PacketService.PACKET_HEAD_LENGTH);
        out.writeByte(FLAG_REGISTER);
        ByteBufUtils.writeLong(out, register.sid);
        NetContext.getPacketService().writeHeaderBefore(out);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static class TunnelHeartbeat {
    }

    public static void writeHeartbeat(ByteBuf out, TunnelHeartbeat heartbeat) {
        out.ensureWritable(4);
        out.writerIndex(PacketService.PACKET_HEAD_LENGTH);
        out.writeByte(FLAG_HEARTBEAT);
        NetContext.getPacketService().writeHeaderBefore(out);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static void read(Channel channel, ByteBuf in) {
        var flag = in.readByte();
        if (flag == FLAG_PACKET) {
            var sid = ByteBufUtils.readLong(in);
            var uid = ByteBufUtils.readLong(in);
            var session = NetContext.getSessionManager().getServerSession(sid);
            if (SessionUtils.isActive(session)) {
                session.getChannel().writeAndFlush(TunnelProtocolServer2Client.valueOf(sid, uid, in));
            } else {
                ReferenceCountUtil.release(in);
            }
        } else if (flag == FLAG_REGISTER) {
            TunnelServer.tunnels.add(channel);
            ReferenceCountUtil.release(in);
        } else if (flag == FLAG_HEARTBEAT) {
            ReferenceCountUtil.release(in);
        }
    }

}
