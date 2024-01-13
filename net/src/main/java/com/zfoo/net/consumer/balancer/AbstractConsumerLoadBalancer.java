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

import com.zfoo.protocol.util.StringUtils;

/**
 * @author godotg
 */
public abstract class AbstractConsumerLoadBalancer implements IConsumerLoadBalancer {

    public static AbstractConsumerLoadBalancer valueOf(String loadBalancer) {
        AbstractConsumerLoadBalancer balancer;
        switch (loadBalancer) {
            case "random":
                balancer = RandomLoadBalancer.getInstance();
                break;
            case "consistent-hash":
                balancer = ConsistentHashLoadBalancer.getInstance();
                break;
            case "cached-consistent-hash":
                balancer = CachedConsistentHashLoadBalancer.getInstance();
                break;
            default:
                throw new RuntimeException(StringUtils.format("Load balancer is not recognized[{}]", loadBalancer));
        }
        return balancer;
    }

}
