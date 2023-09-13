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

import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.session.Session;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 */
public interface IConsumerLoadBalancer {

    /**
     * 只有一致性hash会使用这个argument参数，如果在一致性hash没有传入argument默认使用随机负载均衡
     *
     * @param packet   请求包
     * @param argument 计算参数
     * @return 一个服务提供者的session
     */
    Session loadBalancer(Object packet, @Nullable Object argument);

    default void beforeLoadBalancer(Session session, Object packet, SignalAttachment attachment) {
    }

    default void afterLoadBalancer(Session session, Object packet, SignalAttachment attachment) {
    }

}
