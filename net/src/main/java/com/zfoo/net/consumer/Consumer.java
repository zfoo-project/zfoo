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
import com.zfoo.net.consumer.balancer.AbstractConsumerLoadBalancer;
import com.zfoo.net.consumer.balancer.IConsumerLoadBalancer;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.router.Router;
import com.zfoo.net.router.SignalBridge;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.router.attachment.NoAnswerAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.router.exception.ErrorResponseException;
import com.zfoo.net.router.exception.NetTimeOutException;
import com.zfoo.net.router.exception.UnexpectedProtocolException;
import com.zfoo.net.session.Session;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 服务调度和负载均衡，两个关键点：摘除故障节点，负载均衡
 * <p>
 * 在clientSession中选择一个可用的session，最终还是调用的IRouter中的方法
 *
 * @author godotg
 */
public class Consumer implements IConsumer {

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    // consumer|provider -> LoadBalancer
    private final Map<String, IConsumerLoadBalancer> consumerLoadBalancerMap = new HashMap<>();

    @Override
    public void init() {
        var consumerConfig = NetContext.getConfigManager().getLocalConfig().getConsumer();
        if (consumerConfig == null || CollectionUtils.isEmpty(consumerConfig.getConsumers())) {
            return;
        }
        var consumers = consumerConfig.getConsumers();
        for (var consumer : consumers) {
            var loadBalancer = AbstractConsumerLoadBalancer.valueOf(consumer.getLoadBalancer());
            consumerLoadBalancerMap.put(consumer.getConsumer(), loadBalancer);
        }
    }


    // find all session that can process interface/packet of protocolModule
    @Override
    public List<Session> findProviders(Object packet) {
        var protocolModule = ProtocolManager.moduleByProtocol(packet.getClass());
        var list = new ArrayList<Session>();
        NetContext.getSessionManager().forEachClientSession(session -> {
            var consumerAttribute = session.getConsumerRegister();
            if (consumerAttribute == null) {
                return;
            }
            var providerConfig = consumerAttribute.getProviderConfig();
            if (providerConfig == null) {
                return;
            }
            var providers = providerConfig.getProviders();
            if (providers == null) {
                return;
            }
            if (providers.stream().noneMatch(it -> it.getProtocolModule().equals(protocolModule.getName()))) {
                return;
            }
            list.add(session);
        });
        if (CollectionUtils.isEmpty(list)) {
            throw new RunException("[protocol:{}] has no service that provides the [module:{}]", packet.getClass().getSimpleName(), protocolModule);
        }
        return list;
    }

    // Select a consumer loadBalancer
    @Override
    public IConsumerLoadBalancer selectLoadBalancer(List<Session> providers, Object packet) {
        // select first consumer loadBalancer
        // 不同的服务提供者可能会提供同一个接口，消费者可能同时消费了这些提供了同一个接口的服务提供者，取第一个消费者的loadBalancer
        IConsumerLoadBalancer loadBalancer = null;
        for (var providerSession : providers) {
            for (var provider : providerSession.getConsumerRegister().getProviderConfig().getProviders()) {
                if (consumerLoadBalancerMap.containsKey(provider.getProvider())) {
                    loadBalancer = consumerLoadBalancerMap.get(provider.getProvider());
                    break;
                }
            }
            if (loadBalancer != null) {
                break;
            }
        }
        if (loadBalancer == null) {
            var protocolModule = ProtocolManager.moduleByProtocol(packet.getClass());
            throw new RunException("[protocol:{}] can not find any loadBalancer for the [module:{}]", packet.getClass().getSimpleName(), protocolModule);
        }
        return loadBalancer;
    }


    @Override
    public void send(Object packet, Object argument) {
        var providers = findProviders(packet);
        var loadBalancer = selectLoadBalancer(providers, packet);
        var session = loadBalancer.selectProvider(providers, packet, argument);
        var taskExecutorHash = TaskBus.calTaskExecutorHash(argument);
        NetContext.getRouter().send(session, packet, NoAnswerAttachment.valueOf(taskExecutorHash));
    }

    @Override
    public <T> SyncAnswer<T> syncAsk(Object packet, Class<T> answerClass, Object argument) throws Exception {
        var providers = findProviders(packet);
        var loadBalancer = selectLoadBalancer(providers, packet);
        var session = loadBalancer.selectProvider(providers, packet, argument);

        // 下面的代码逻辑同Router的syncAsk，如果修改的话，记得一起修改
        var clientSignalAttachment = new SignalAttachment();
        var taskExecutorHash = TaskBus.calTaskExecutorHash(argument);
        clientSignalAttachment.setTaskExecutorHash(taskExecutorHash);

        try {
            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // load balancer之前调用
            loadBalancer.beforeLoadBalancer(session, packet, clientSignalAttachment);

            NetContext.getRouter().send(session, packet, clientSignalAttachment);

            Object responsePacket = clientSignalAttachment.getResponseFuture().get(Router.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responsePacket.getClass() == Error.class) {
                throw new ErrorResponseException((Error) responsePacket);
            }
            if (answerClass != null && answerClass != responsePacket.getClass()) {
                throw new UnexpectedProtocolException("client expect protocol:[{}], but found protocol:[{}]", answerClass, responsePacket.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            var syncAnswer = new SyncAnswer<>((T) responsePacket, clientSignalAttachment);

            // load balancer之后调用
            loadBalancer.afterLoadBalancer(session, packet, clientSignalAttachment);
            return syncAnswer;
        } catch (TimeoutException e) {
            throw new NetTimeOutException("syncAsk timeout exception, ask:[{}], attachment:[{}]", JsonUtils.object2String(packet), JsonUtils.object2String(clientSignalAttachment));
        } finally {
            SignalBridge.removeSignalAttachment(clientSignalAttachment);
        }
    }

    @Override
    public <T> AsyncAnswer<T> asyncAsk(Object packet, Class<T> answerClass, Object argument) {
        var providers = findProviders(packet);
        var loadBalancer = selectLoadBalancer(providers, packet);
        var session = loadBalancer.selectProvider(providers, packet, argument);

        var asyncAnswer = NetContext.getRouter().asyncAsk(session, packet, answerClass, argument);

        // load balancer之前调用
        loadBalancer.beforeLoadBalancer(session, packet, asyncAnswer.getSignalAttachment());

        // load balancer之后调用
        asyncAnswer.thenAccept(responsePacket -> loadBalancer.afterLoadBalancer(session, packet, asyncAnswer.getSignalAttachment()));
        return asyncAnswer;
    }

}
