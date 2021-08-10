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

import com.zfoo.net.packet.common.Message;
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.net.packet.model.HttpPacketAttachment;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class HttpCodecHandler extends MessageToMessageCodec<FullHttpRequest, EncodedPacketInfo> {

    private static final Logger logger = LoggerFactory.getLogger(HttpCodecHandler.class);

    private Function<FullHttpRequest, IPacket> uriResolver;

    public HttpCodecHandler(Function<FullHttpRequest, IPacket> uriResolver) {
        super();
        this.uriResolver = uriResolver;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, List<Object> list) {
        try {
            var packet = uriResolver.apply(fullHttpRequest);
            var attachment = HttpPacketAttachment.valueOf(fullHttpRequest, HttpResponseStatus.OK);
            var decodedPacketInfo = DecodedPacketInfo.valueOf(packet, attachment);
            list.add(decodedPacketInfo);
        } catch (Exception e) {
            logger.error("exception异常", e);
            throw e;
        } catch (Throwable t) {
            logger.error("throwable错误", t);
            throw t;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, EncodedPacketInfo out, List<Object> list) {

        try {
            var packet = (IPacket) out.getPacket();
            var attachment = (HttpPacketAttachment) out.getPacketAttachment();

            var protocolVersion = attachment.getFullHttpRequest().protocolVersion();
            var httpResponseStatus = attachment.getHttpResponseStatus();
            if (packet.protocolId() == Message.PROTOCOL_ID) {
                var fullHttpResponse = new DefaultFullHttpResponse(protocolVersion, httpResponseStatus);
                list.add(fullHttpResponse);
            } else {
                var byteBuf = channelHandlerContext.alloc().ioBuffer();
                var jsonStr = JsonUtils.object2StringTurbo(packet);
                byteBuf.writeBytes(StringUtils.bytes(jsonStr));
                var fullHttpResponse = new DefaultFullHttpResponse(protocolVersion, httpResponseStatus, byteBuf);
                list.add(fullHttpResponse);
            }
        } catch (Exception e) {
            logger.error("[{}]编码exception异常", JsonUtils.object2String(out), e);
            throw e;
        } catch (Throwable t) {
            logger.error("[{}]编码throwable错误", JsonUtils.object2String(out), t);
            throw t;
        }
    }
}
