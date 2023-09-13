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

package com.zfoo.net.handler.codec.http;

import com.zfoo.net.packet.DecodedPacketInfo;
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.common.Message;
import com.zfoo.net.router.attachment.HttpAttachment;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.*;

import java.util.List;
import java.util.function.Function;


/**
 * @author godotg
 */
public class HttpCodecHandler extends MessageToMessageCodec<FullHttpRequest, EncodedPacketInfo> {

    private final Function<FullHttpRequest, DecodedPacketInfo> uriResolver;

    public HttpCodecHandler(Function<FullHttpRequest, DecodedPacketInfo> uriResolver) {
        super();
        this.uriResolver = uriResolver;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, List<Object> list) {
        var decodedPacketInfo = uriResolver.apply(fullHttpRequest);
        list.add(decodedPacketInfo);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, EncodedPacketInfo out, List<Object> list) {
        var packet = out.getPacket();
        var attachment = (HttpAttachment) out.getAttachment();

        var protocolVersion = attachment.getFullHttpRequest().protocolVersion();
        var httpResponseStatus = attachment.getHttpResponseStatus();
        if (packet.getClass() == Message.class) {
            var message = (Message) packet;
            if (message.fail()) {
                httpResponseStatus = HttpResponseStatus.BAD_REQUEST;
            }

            if (StringUtils.isEmpty(message.getMessage())) {
                var fullHttpResponse = new DefaultFullHttpResponse(protocolVersion, httpResponseStatus);
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                list.add(fullHttpResponse);
            } else {
                var byteBuf = channelHandlerContext.alloc().ioBuffer();
                byteBuf.writeCharSequence(message.getMessage(), StringUtils.DEFAULT_CHARSET);
                var fullHttpResponse = new DefaultFullHttpResponse(protocolVersion, httpResponseStatus, byteBuf);

                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
                fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                list.add(fullHttpResponse);
            }
        } else {
            var byteBuf = channelHandlerContext.alloc().ioBuffer();
            var jsonStr = JsonUtils.object2String(packet);
            byteBuf.writeBytes(StringUtils.bytes(jsonStr));
            var fullHttpResponse = new DefaultFullHttpResponse(protocolVersion, httpResponseStatus, byteBuf);

            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
            fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, byteBuf.readableBytes());
            fullHttpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//                fullHttpResponse.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

            list.add(fullHttpResponse);
        }
    }
}
