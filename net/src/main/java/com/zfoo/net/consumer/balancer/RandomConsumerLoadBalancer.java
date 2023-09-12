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

import com.zfoo.net.session.Session;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.RandomUtils;

/**
 * 随机负载均衡器，任选服务提供者的其中之一
 *
 * @author godotg
 */
public class RandomConsumerLoadBalancer extends AbstractConsumerLoadBalancer {

    private static final RandomConsumerLoadBalancer INSTANCE = new RandomConsumerLoadBalancer();

    private RandomConsumerLoadBalancer() {
    }

    public static RandomConsumerLoadBalancer getInstance() {
        return INSTANCE;
    }

    @Override
    public Session loadBalancer(Object packet, Object argument) {
        var module = ProtocolManager.moduleByProtocol(packet.getClass());
        var sessions = getSessionsByModule(module);

        if (sessions.isEmpty()) {
            throw new RunException("RandomConsumerLoadBalancer [protocol:{}][argument:{}], no service provides the [module:{}]", packet.getClass(), argument, module);
        }

        return RandomUtils.randomEle(sessions);
    }

}
