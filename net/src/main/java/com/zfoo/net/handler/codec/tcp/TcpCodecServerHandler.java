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

package com.zfoo.net.handler.codec.tcp;

import com.zfoo.net.packet.EncodedPacketInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * header(4byte) + protocolId(2byte) + packet
 * header = body(bytes.length) + protocolId.length(2byte)
 *
 * @author godotg
 */
@ChannelHandler.Sharable
public class TcpCodecServerHandler extends TcpCodecHandler {

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        super.decode(ctx, in, out);
    }

    @Override
    public void encode(ChannelHandlerContext ctx, EncodedPacketInfo packetInfo, ByteBuf out) {
        super.encode(ctx, packetInfo, out);
    }

}
