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

import com.zfoo.net.NetContext;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.packet.model.NoAnswerAttachment;
import com.zfoo.net.packet.model.SignalPacketAttachment;
import com.zfoo.net.router.Router;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.router.exception.ErrorResponseException;
import com.zfoo.net.router.exception.NetTimeOutException;
import com.zfoo.net.router.exception.UnexpectedProtocolException;
import com.zfoo.net.router.route.PacketSignal;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.math.HashUtils;
import com.zfoo.util.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 服务调度和负载均衡，两个关键点：摘除故障节点，负载均衡
 * <p>
 * 在clientSession中选择一个可用的session，最终还是调用的IRouter中的方法
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class Consumer implements IConsumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);


    @Override
    public void send(IPacket packet, Object argument) {
        try {
            var loadBalancer = NetContext.getConfigManager().consumerLoadBalancer();
            var session = loadBalancer.loadBalancer(packet, argument);
            var executorConsistentHash = (argument == null) ? RandomUtils.randomInt() : HashUtils.fnvHash(argument);
            NetContext.getRouter().send(session, packet, NoAnswerAttachment.valueOf(executorConsistentHash));
        } catch (Throwable t) {
            logger.error("consumer发送未知异常", t);
        }
    }

    @Override
    public <T extends IPacket> SyncAnswer<T> syncAsk(IPacket packet, Class<T> answerClass, Object argument) throws Exception {
        var loadBalancer = NetContext.getConfigManager().consumerLoadBalancer();
        var session = loadBalancer.loadBalancer(packet, argument);


        // 下面的代码逻辑同Router的syncAsk，如果修改的话，记得一起修改
        var clientAttachment = new SignalPacketAttachment();
        var executorConsistentHash = (argument == null) ? RandomUtils.randomInt() : HashUtils.fnvHash(argument);
        clientAttachment.setExecutorConsistentHash(executorConsistentHash);

        try {
            PacketSignal.addSignalAttachment(clientAttachment);

            // load balancer之前调用
            loadBalancer.beforeLoadBalancer(session, packet, clientAttachment);

            NetContext.getRouter().send(session, packet, clientAttachment);

            IPacket responsePacket = clientAttachment.getResponseFuture().get(Router.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responsePacket.protocolId() == Error.errorProtocolId()) {
                throw new ErrorResponseException((Error) responsePacket);
            }
            if (answerClass != null && answerClass != responsePacket.getClass()) {
                throw new UnexpectedProtocolException(StringUtils.format("client expect protocol:[{}], but found protocol:[{}]"
                        , answerClass, responsePacket.getClass().getName()));
            }
            var syncAnswer = new SyncAnswer<>((T) responsePacket, clientAttachment);

            // load balancer之后调用
            loadBalancer.afterLoadBalancer(session, packet, clientAttachment);
            return syncAnswer;
        } catch (TimeoutException e) {
            throw new NetTimeOutException(StringUtils.format("syncRequest timeout exception, ask:[{}], attachment:[{}]"
                    , JsonUtils.object2String(packet), JsonUtils.object2String(clientAttachment)));
        } finally {
            PacketSignal.removeSignalAttachment(clientAttachment);
        }
    }

    @Override
    public <T extends IPacket> AsyncAnswer<T> asyncAsk(IPacket packet, Class<T> answerClass, Object argument) {
        var loadBalancer = NetContext.getConfigManager().consumerLoadBalancer();
        var session = loadBalancer.loadBalancer(packet, argument);
        var asyncAnswer = NetContext.getRouter().asyncAsk(session, packet, answerClass, argument);

        // load balancer之前调用
        loadBalancer.beforeLoadBalancer(session, packet, asyncAnswer.getFutureAttachment());

        // load balancer之后调用
        asyncAnswer.thenAccept(responsePacket -> loadBalancer.afterLoadBalancer(session, packet, asyncAnswer.getFutureAttachment()));
        return asyncAnswer;
    }

}
