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
import com.zfoo.net.packet.IPacket;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.registration.ProtocolModule;
import com.zfoo.protocol.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

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
        return getSessionsByModule(ProtocolManager.moduleByProtocol(packet.getClass()));
    }

    public List<Session> getSessionsByModule(ProtocolModule module) {
        var list = new ArrayList<Session>();
        NetContext.getSessionManager().forEachClientSession(new Consumer<Session>() {
            @Override
            public void accept(Session session) {
                if (session.getConsumerAttribute() == null || session.getConsumerAttribute().getProviderConfig() == null) {
                    return;
                }
                var providerConfig = session.getConsumerAttribute().getProviderConfig();
                if (providerConfig.getProviders().stream().anyMatch(it -> it.getProtocolModule().equals(module))) {
                    list.add(session);
                }
            }
        });
        return list;
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

        var module = ProtocolManager.moduleByProtocol(packet.getClass());
        return registerVO.getProviderConfig().getProviders().contains(module);
    }
}
