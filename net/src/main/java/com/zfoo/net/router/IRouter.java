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

import com.zfoo.net.packet.IPacket;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.session.Session;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 * @version 3.0
 */
public interface IRouter {

    /**
     * EN:send() and receive() are the entry points for sending and receiving messages, which can be called directly
     * CN:send()和receive()是消息的发送和接收的入口，可以直接调用
     */
    void send(Session session, IPacket packet);

    void send(Session session, IPacket packet, @Nullable IAttachment attachment);

    void receive(Session session, IPacket packet, @Nullable IAttachment attachment);

    void atReceiver(Session session, IPacket packet, @Nullable IAttachment attachment);

    /**
     * attention：syncAsk和asyncAsk只能客户端调用
     * 同一个客户端可以同时发送多条同步或者异步消息。
     * 服务器对每个请求消息也只能回复一条消息，不能在处理一条不同或者异步消息的时候回复多条消息。
     *
     * @param session     一个网络通信的会话
     * @param packet      一个网络通信包，消息体
     * @param answerClass 等待返回包的class类。
     *                    如果为null，则不会检查这个class类的协议号是否和返回消息体的协议号相等；
     *                    如果不为null，会检查返回包的协议号。为null的情况主要用在网关。
     * @param <T>         请求消息需要服务器返回的类型
     * @param argument    参数，主要用来计算一致性hashId。
     *                    1.IConsumer会使用这个参数计算负载到哪个服务提供者；
     *                    2.服务提供者收到请求过后会使用这个参数来计算再哪个线程执行任务；
     *                    综上所述，这个参数会在上面两种情况使用。
     * @return 服务器返回的消息Response
     * @throws Exception 如果超时或者其它异常
     */
    <T extends IPacket> SyncAnswer<T> syncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception;

    <T extends IPacket> AsyncAnswer<T> asyncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument);

}
