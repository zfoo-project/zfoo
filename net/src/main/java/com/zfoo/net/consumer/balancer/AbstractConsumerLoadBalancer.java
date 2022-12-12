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

import com.zfoo.net.NetContext;
import com.zfoo.net.consumer.registry.RegisterVO;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.protocol.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class AbstractConsumerLoadBalancer implements IConsumerLoadBalancer {

    public static AbstractConsumerLoadBalancer valueOf(String loadBalancer) {
        AbstractConsumerLoadBalancer balancer;
        switch (loadBalancer) {
            case "random":
                balancer = RandomConsumerLoadBalancer.getInstance();
                break;
            case "consistent-hash":
                balancer = ConsistentHashConsumerLoadBalancer.getInstance();
                break;
            default:
                throw new RuntimeException(StringUtils.format("Load balancer is not recognized[{}]", loadBalancer));
        }
        return balancer;
    }

    public List<Session> getSessionsByPacket(IPacket packet) {
        return getSessionsByModule(ProtocolManager.moduleByProtocolId(packet.protocolId()));
    }

    public List<Session> getSessionsByModule(ProtocolModule module) {
        return NetContext.getSessionManager().getClientSessionMap()
                .values()
                .stream()
                .filter(it -> it.getConsumerAttribute() != null && it.getConsumerAttribute().getProviderConfig() != null)
                .filter(it -> it.getConsumerAttribute().getProviderConfig().getProviders().stream().anyMatch(provider -> provider.getProtocolModule().equals(module)))
                .collect(Collectors.toList());
    }

    public List<Session> sessionsByModule(ProtocolModule module) {
        var clientSessionMap = NetContext.getSessionManager().getClientSessionMap();
        var sessions = new ArrayList<Session>();
        for (var clientSession : clientSessionMap.values()) {
            var consumerAttribute = clientSession.getConsumerAttribute();
            if (consumerAttribute == null) {
                continue;
            }

            var registerVO = (RegisterVO) consumerAttribute;
            var providerConfig = registerVO.getProviderConfig();
            if (providerConfig == null) {
                continue;
            }

            if (providerConfig.getProviders().stream().anyMatch(it -> it.getProtocolModule().getId() == module.getId())) {
                sessions.add(clientSession);
            }
        }
        return sessions;
    }


    public boolean sessionHasModule(Session session, IPacket packet) {
        var consumerAttribute = session.getConsumerAttribute();
        if (Objects.isNull(consumerAttribute)) {
            return false;
        }

        var registerVO = (RegisterVO) consumerAttribute;
        if (Objects.isNull(registerVO.getProviderConfig())) {
            return false;
        }

        var module = ProtocolManager.moduleByProtocolId(packet.protocolId());
        return registerVO.getProviderConfig().getProviders().contains(module);
    }
}
