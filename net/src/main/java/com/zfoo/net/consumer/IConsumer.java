/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.zfoo.net.consumer;

import com.zfoo.net.consumer.balancer.IConsumerLoadBalancer;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.session.Session;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author godotg
 */
public interface IConsumer {

    void init();

    List<Session> findProviders(Object packet);

    IConsumerLoadBalancer selectLoadBalancer(List<Session> providers, Object packet);

    /**
     * Send a packet without waiting for a response.
     * <p>
     * Example: see FriendController#atApplyFriendRequest in com.zfoo.app.zapp.chat.controller.
     * The client sends a friend-request, the chat service processes it, then fires the message directly to the gateway.
     *
     * @param packet   the packet to send
     * @param argument the parameter used for load-balancing calculation (e.g., user ID),
     *                 which determines which service provider to route to
     */
    void send(Object packet, @Nullable Object argument);

    <T> SyncAnswer<T> syncAsk(Object packet, Class<T> answerClass, @Nullable Object argument) throws Exception;

    <T> AsyncAnswer<T> asyncAsk(Object packet, Class<T> answerClass, @Nullable Object argument);

}
