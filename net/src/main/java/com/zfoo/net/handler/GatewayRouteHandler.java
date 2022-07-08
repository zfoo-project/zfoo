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

package com.zfoo.net.handler;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.consumer.balancer.ConsistentHashConsumerLoadBalancer;
import com.zfoo.net.core.gateway.IGatewayLoadBalancer;
import com.zfoo.net.core.gateway.model.GatewaySessionInactiveEvent;
import com.zfoo.net.packet.common.Heartbeat;
import com.zfoo.net.packet.common.Ping;
import com.zfoo.net.packet.common.Pong;
import com.zfoo.net.packet.model.DecodedPacketInfo;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;

/**
 * @author jaysunxiao
 * @version 3.0
 */
@ChannelHandler.Sharable
public class GatewayRouteHandler extends ServerRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRouteHandler.class);

    private final BiFunction<Session, IPacket, Boolean> packetFilter;

    public GatewayRouteHandler(BiFunction<Session, IPacket, Boolean> packetFilter) {
        this.packetFilter = packetFilter;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 请求者的session，一般是serverSession
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }

        var decodedPacketInfo = (DecodedPacketInfo) msg;
        var packet = decodedPacketInfo.getPacket();
        if (packet.protocolId() == Heartbeat.PROTOCOL_ID) {
            return;
        }
        if (packet.protocolId() == Ping.PROTOCOL_ID) {
            NetContext.getRouter().send(session, Pong.valueOf(TimeUtils.now()), null);
            return;
        }

        // 过滤非法包
        if (packetFilter != null && packetFilter.apply(session, packet)) {
            throw new IllegalArgumentException(StringUtils.format("[session:{}]发送了一个非法包[{}]"
                    , SessionUtils.sessionInfo(ctx), JsonUtils.object2String(packet)));
        }

        var signalAttachment = (SignalAttachment) decodedPacketInfo.getAttachment();

        // 把客户端信息包装为一个GatewayAttachment,因此通过这个网关附加包可以得到玩家的uid、sid之类的信息
        var gatewayAttachment = new GatewayAttachment(session, signalAttachment);

        // 网关优先使用IGatewayLoadBalancer作为一致性hash的计算参数，然后才会使用客户端的session做参数
        // 例子：以聊天服务来说，玩家知道自己在哪个群组groupId中，那往这个群发送消息时，会在Packet中带上这个groupId做为一致性hash就可以了。
        if (packet instanceof IGatewayLoadBalancer) {
            var loadBalancerConsistentHashObject = ((IGatewayLoadBalancer) packet).loadBalancerConsistentHashObject();
            gatewayAttachment.useExecutorConsistentHash(loadBalancerConsistentHashObject);
            forwardingPacket(packet, gatewayAttachment, loadBalancerConsistentHashObject);
            return;
        } else {
            // 使用用户的uid做一致性hash
            var uid = (Long) session.getAttribute(AttributeType.UID);
            if (uid != null) {
                forwardingPacket(packet, gatewayAttachment, uid);
                return;
            }
        }
        // 再使用session的sid做一致性hash，因为每次客户端连接过来sid都会改变，所以客户端重新建立连接的话可能会被路由到其它的服务器
        // 如果有特殊需求的话，可以考虑去重写网关的转发策略
        // 拿着玩家的sid做一致性hash，那肯定是：一旦重连sid就会一直变化。所以：一般情况下除非自己创建TcpClient，否则逻辑不应该走到这里。 而是走上面的通过UID做一致性hash
        var sid = session.getSid();
        forwardingPacket(packet, gatewayAttachment, sid);
    }

    /**
     * 转发网关收到的包到Provider
     */
    private void forwardingPacket(IPacket packet, IAttachment attachment, Object argument) {
        try {
            var consumerSession = ConsistentHashConsumerLoadBalancer.getInstance().loadBalancer(packet, argument);
            NetContext.getRouter().send(consumerSession, packet, attachment);
        } catch (Exception e) {
            logger.error("网关发生异常", e);
        } catch (Throwable t) {
            logger.error("网关发生错误", t);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }

        var sid = session.getSid();
        var uid = (Long) session.getAttribute(AttributeType.UID);

        // 连接到网关的客户端断开了连接
        EventBus.asyncSubmit(GatewaySessionInactiveEvent.valueOf(sid, uid == null ? 0 : uid.longValue()));

        super.channelInactive(ctx);
    }

}
