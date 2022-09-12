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

package com.zfoo.net.handler.codec.json;

import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.buffer.ByteBufUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

/**
 * @author godotg
 * @version 3.0
 */
public class JsonWebSocketCodecHandler extends MessageToMessageCodec<WebSocketFrame, EncodedPacketInfo> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame, List<Object> list) {
        var byteBuf = webSocketFrame.content();
        var bytes = ByteBufUtils.readAllBytes(byteBuf);
        var jsonStr = StringUtils.bytesToString(bytes);
        var jsonMap = JsonUtils.getJsonMap(jsonStr);
        var protocolId = Short.parseShort(jsonMap.get("protocolId"));
        var packetStr = jsonMap.get("packet");
        var protocolClass = ProtocolManager.getProtocol(protocolId).protocolConstructor().getDeclaringClass();
        var packet = JsonUtils.string2Object(packetStr, protocolClass);
        list.add(DecodedPacketInfo.valueOf((IPacket) packet, null));
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, EncodedPacketInfo out, List<Object> list) {
        var byteBuf = channelHandlerContext.alloc().ioBuffer();

        var packet = out.getPacket();
        var jsonPacket = JsonPacket.valueOf(packet.protocolId(), packet);
        var bytes = StringUtils.bytes(JsonUtils.object2String(jsonPacket));
        byteBuf.writeBytes(bytes);

        list.add(new BinaryWebSocketFrame(byteBuf));
    }

}
