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
import com.zfoo.net.core.gateway.IGatewayLoadBalancer;
import com.zfoo.net.core.gateway.model.GatewaySessionInactiveEvent;
import com.zfoo.net.packet.DecodedPacketInfo;
import com.zfoo.net.packet.common.Heartbeat;
import com.zfoo.net.packet.common.Ping;
import com.zfoo.net.packet.common.Pong;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.session.Session;
import com.zfoo.net.util.SessionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.scheduler.util.TimeUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author godotg
 */
@ChannelHandler.Sharable
public class GatewayRouteHandler extends ServerRouteHandler {

    private static final Logger logger = LoggerFactory.getLogger(GatewayRouteHandler.class);

    public static final BiFunction<Session, Object, Boolean> DEFAULT_PACKER_FILTER = (session, packet) -> Boolean.FALSE;

    private final BiFunction<Session, Object, Boolean> packetFilter;

    public GatewayRouteHandler(@Nullable BiFunction<Session, Object, Boolean> packetFilter) {
        this.packetFilter = Objects.requireNonNullElse(packetFilter, DEFAULT_PACKER_FILTER);
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // Requester's session, typically a serverSession
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }

        var decodedPacketInfo = (DecodedPacketInfo) msg;
        var packet = decodedPacketInfo.getPacket();
        if (packet.getClass() == Heartbeat.class) {
            return;
        }
        if (packet.getClass() == Ping.class) {
            NetContext.getRouter().send(session, Pong.valueOf(TimeUtils.now()), null);
            return;
        }

        // Filter illegal packets
        if (packetFilter.apply(session, packet)) {
            throw new IllegalArgumentException(StringUtils.format(" session:{} sent an illegal packet [{}]", SessionUtils.sessionSimpleInfo(ctx), JsonUtils.object2String(packet)));
        }

        // Wrap the client information into a GatewayAttachment,
        // so that uid/sid and other client info can be retrieved from the attachment downstream.
        var gatewayAttachment = new GatewayAttachment(session);
        var signalAttachment = (SignalAttachment) decodedPacketInfo.getAttachment();
        if (signalAttachment != null) {
            signalAttachment.setClient(SignalAttachment.SIGNAL_SERVER);
            gatewayAttachment.setSignalAttachment(signalAttachment);
        }

        // The gateway prefers IGatewayLoadBalancer as the consistent-hash parameter;
        // only falls back to the client session when not implemented.
        // Example: for a chat service, the client knows its groupId, so it includes groupId in the Packet as the hash key.
        if (packet instanceof IGatewayLoadBalancer) {
            var loadBalancerConsistentHashObject = ((IGatewayLoadBalancer) packet).loadBalancerConsistentHashObject();
            gatewayAttachment.setTaskExecutorHash(loadBalancerConsistentHashObject.hashCode());
            forwardingPacket(packet, gatewayAttachment, loadBalancerConsistentHashObject);
            return;
        } else {
            // Use the user's uid as the consistent-hash key
            var uid = session.getUid();
            if (uid > 0) {
                forwardingPacket(packet, gatewayAttachment, uid);
                return;
            }
        }
        // Fall back to the session's sid as the consistent-hash key.
        // Note: since sid changes on every reconnect, the client may be routed to a different server after reconnecting.
        // Override the gateway's forwarding strategy if custom behavior is needed.
        // In most cases, logic should reach the uid-based routing above, not here.
        var sid = session.getSid();
        forwardingPacket(packet, gatewayAttachment, sid);
    }

    /**
     * Forward a packet received by the gateway to the Provider.
     */
    private void forwardingPacket(Object packet, Object attachment, Object argument) {
        try {
            // The gateway uses moduleId + uid to locate the session
            var providers = NetContext.getConsumer().findProviders(packet);
            var loadBalancer = NetContext.getConsumer().selectLoadBalancer(providers, packet);
            var providerSession = loadBalancer.selectProvider(providers, packet, argument);
            NetContext.getRouter().send(providerSession, packet, attachment);
        } catch (Exception e) {
            logger.error("An exception occurred at the gateway", e);
        } catch (Throwable t) {
            logger.error("An error occurred at the gateway", t);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        var session = SessionUtils.getSession(ctx);
        if (session == null) {
            return;
        }

        var sid = session.getSid();
        var uid = session.getUid();

        // A client connected to the gateway has disconnected
        EventBus.post(GatewaySessionInactiveEvent.valueOf(sid, uid));

        super.channelInactive(ctx);
    }

}
