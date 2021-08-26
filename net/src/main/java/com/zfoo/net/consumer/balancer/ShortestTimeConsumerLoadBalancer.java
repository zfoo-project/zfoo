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

package com.zfoo.net.consumer.balancer;

import com.zfoo.net.packet.model.SignalPacketAttachment;
import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.scheduler.util.TimeUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 最少时间调用负载均衡器，优先选择调用时间最短的session
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class ShortestTimeConsumerLoadBalancer extends AbstractConsumerLoadBalancer {

    private static final ShortestTimeConsumerLoadBalancer INSTANCE = new ShortestTimeConsumerLoadBalancer();

    private ShortestTimeConsumerLoadBalancer() {
    }

    public static ShortestTimeConsumerLoadBalancer getInstance() {
        return INSTANCE;
    }

    @Override
    public Session loadBalancer(IPacket packet, Object argument) {
        var module = ProtocolManager.moduleByProtocolId(packet.protocolId());
        var sessions = getSessionsByModule(module);

        if (sessions.isEmpty()) {
            throw new RunException("一致性hash负载均衡[protocolId:{}]参数[argument:{}],没有服务提供者提供服务[module:{}]", packet.protocolId(), argument, module);
        }

        var sortedSessions = sessions.stream()
                .sorted((a, b) -> {
                    var aMap = (Map<Short, Long>) a.getAttribute(AttributeType.RESPONSE_TIME);
                    var bMap = (Map<Short, Long>) b.getAttribute(AttributeType.RESPONSE_TIME);
                    if (aMap == null) {
                        return -1;
                    } else if (bMap == null) {
                        return 1;
                    } else {
                        var aTime = aMap.get(packet.protocolId());
                        var bTime = bMap.get(packet.protocolId());
                        if (aTime == null) {
                            return -1;
                        } else if (bTime == null) {
                            return 1;
                        } else {
                            return (aTime > bTime) ? 1 : -1;
                        }
                    }
                }).findFirst();
        return sortedSessions.get();
    }

    @Override
    public void beforeLoadBalancer(Session session, IPacket packet, SignalPacketAttachment attachment) {
        // 因为要通过最短响应时间来路由分发消息，这里使用更精确的时间
        attachment.setTimestamp(TimeUtils.currentTimeMillis());
    }

    @Override
    public void afterLoadBalancer(Session session, IPacket packet, SignalPacketAttachment attachment) {
        var map = (Map<Short, Long>) session.getAttribute(AttributeType.RESPONSE_TIME);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            session.putAttribute(AttributeType.RESPONSE_TIME, map);
        }
        map.put(packet.protocolId(), TimeUtils.currentTimeMillis() - attachment.getTimestamp());
    }

}
