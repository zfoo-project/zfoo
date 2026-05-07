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

package com.zfoo.net.router;

import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.session.Session;
import com.zfoo.net.task.PacketReceiverTask;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 */
public interface IRouter {

    /**
     * Entry point for sending messages.
     */
    void send(Session session, Object packet);

    void send(Session session, Object packet, @Nullable Object attachment);

    /**
     * Entry point for receiving messages.
     */
    void receive(Session session, Object packet, @Nullable Object attachment);

    void atReceiver(PacketReceiverTask packetReceiverTask);

    void registerPacketReceiverDefinition(Object bean);

    /**
     * The server can only reply with one message per syncAsk/asyncAsk request.
     * Multiple replies to a single async message are not allowed.
     *
     * @param session     the network session
     * @param packet      the request packet to send
     * @param answerClass the expected response class.
     *                    If null, the protocol ID of the response is not verified;
     *                    if not null, the response protocol ID will be checked.
     *                    Null is mainly used in gateway scenarios.
     * @param argument    parameter for computing the consistent-hash ID; the server uses this to determine
     *                    which thread the task runs on after receiving the request.
     * @return the server's response message
     * @throws Exception if the request times out or another exception occurs
     */
    <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception;

    <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument, long timeoutMillis) throws Exception;

    <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument);

    <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument, long timeoutMillis);

}
