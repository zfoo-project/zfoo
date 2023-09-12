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
import com.zfoo.protocol.registration.ProtocolModule;
import org.springframework.lang.Nullable;

/**
 * @author godotg
 */
public interface IConsumer {

    void init();

    IConsumerLoadBalancer loadBalancer(ProtocolModule protocolModule);

    /**
     * 直接发送，不需要任何返回值
     * <p>
     * 例子：参考 com.zfoo.app.zapp.chat.controller。FrinedController 的 atApplyFriendRequest方法，客户端发起申请请求，chat服务处理后，再把消息直接发给网关
     *
     * @param packet   需要发送的包
     * @param argument 计算负载均衡的参数，比如用户的id
     */
    void send(Object packet, @Nullable Object argument);

    <T> SyncAnswer<T> syncAsk(Object packet, Class<T> answerClass, @Nullable Object argument) throws Exception;

    <T> AsyncAnswer<T> asyncAsk(Object packet, Class<T> answerClass, @Nullable Object argument);

}
