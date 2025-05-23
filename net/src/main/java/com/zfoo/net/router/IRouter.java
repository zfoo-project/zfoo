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
     * EN: send messages entry
     * CN: 发送消息的入口
     */
    void send(Session session, Object packet);

    void send(Session session, Object packet, @Nullable Object attachment);

    /**
     * EN: receive messages entry
     * CN: 接收消息的入口
     */
    void receive(Session session, Object packet, @Nullable Object attachment);

    void atReceiver(PacketReceiverTask packetReceiverTask);

    void registerPacketReceiverDefinition(Object bean);

    /**
     * 服务器对每个syncAsk和asyncAsk请求消息也只能回复一条消息，不能在处理一条不同或者异步消息的时候回复多条消息。
     *
     * @param session     一个网络通信的会话
     * @param packet      一个网络通信包，需要发送的消息体
     * @param answerClass 等待返回包的class类。
     *                    如果为null，则不会检查这个class类的协议号是否和返回消息体的协议号相等；
     *                    如果不为null，会检查返回包的协议号。为null的情况主要用在网关。
     * @param argument    参数，主要用来计算一致性hashId，服务器收到请求过后会使用这个参数来计算再哪个线程执行任务；
     * @return 服务器返回的消息Response
     * @throws Exception 如果超时或者其它异常
     */
    <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception;

    <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument, long timeoutMillis) throws Exception;

    <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument);

    <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument, long timeoutMillis);

}
