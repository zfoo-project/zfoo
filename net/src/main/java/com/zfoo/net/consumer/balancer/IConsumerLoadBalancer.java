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

import java.util.List;

/**
 * @author godotg
 */
public interface IConsumerLoadBalancer {

    /**
     * Select a service provider that can handle the given packet.
     * Only the consistent-hash balancer uses the {@code argument} parameter;
     * if no argument is passed, it falls back to random load balancing.
     *
     * @param packet   the request packet
     * @param argument the parameter used to compute the consistent hash
     * @return a session of the selected service provider
     */
    Session selectProvider(List<Session> providers, Object packet, @Nullable Object argument);

    default void beforeLoadBalancer(Session session, Object packet, SignalAttachment attachment) {
    }

    default void afterLoadBalancer(Session session, Object packet, SignalAttachment attachment) {
    }

}
