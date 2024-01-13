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
import com.zfoo.protocol.util.RandomUtils;

import java.util.List;

/**
 * 随机负载均衡器，任选服务提供者的其中之一
 *
 * @author godotg
 */
public class RandomLoadBalancer extends AbstractConsumerLoadBalancer {

    private static final RandomLoadBalancer INSTANCE = new RandomLoadBalancer();

    private RandomLoadBalancer() {
    }

    public static RandomLoadBalancer getInstance() {
        return INSTANCE;
    }

    @Override
    public Session selectProvider(List<Session> providers, Object packet, Object argument) {
        return RandomUtils.randomEle(providers);
    }

}
