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

package com.zfoo.net.dispatcher.manager;

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayCheck;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayConfirm;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayEvent;
import com.zfoo.net.dispatcher.model.answer.AsyncAnswer;
import com.zfoo.net.dispatcher.model.answer.SyncAnswer;
import com.zfoo.net.dispatcher.model.exception.ErrorResponseException;
import com.zfoo.net.dispatcher.model.exception.NetTimeOutException;
import com.zfoo.net.dispatcher.model.exception.UnexpectedProtocolException;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.packet.common.Heartbeat;
import com.zfoo.net.packet.model.EncodedPacketInfo;
import com.zfoo.net.packet.model.GatewayPacketAttachment;
import com.zfoo.net.packet.model.IPacketAttachment;
import com.zfoo.net.packet.model.SignalPacketAttachment;
import com.zfoo.net.session.model.AttributeType;
import com.zfoo.net.session.model.Session;
import com.zfoo.net.task.TaskManager;
import com.zfoo.net.task.model.ReceiveTask;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.util.math.HashUtils;
import com.zfoo.util.math.RandomUtils;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 消息派发
 *
 * @author jaysunxiao
 * @version 3.0
 */
public class PacketDispatcher implements IPacketDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(PacketDispatcher.class);

    public static final long DEFAULT_TIMEOUT = 3000;

    /**
     * 会把receive收到的attachment存储在这个地方，只针对task线程。
     * doWithReceivePacket会设置receivePacketAttachment，但是在方法调用完成会取消，不需要过多关注。
     * asyncRequest会再次设置receivePacketAttachment，需要重点关注。
     */
    private final FastThreadLocal<SignalPacketAttachment> serverReceiveSignalPacketAttachment = new FastThreadLocal<>();


    @Override
    public void receive(Session session, IPacket packet, @Nullable IPacketAttachment packetAttachment) {
        if (packet.protocolId() == Heartbeat.heartbeatProtocolId()) {
            logger.info("heartbeat");
            return;
        }

        // 发送者（客户端）同步和异步消息的接收，发送者通过packetId判断重复
        if (packetAttachment != null) {
            switch (packetAttachment.packetType()) {
                case SIGNAL_PACKET:
                    var signalPacketAttachment = (SignalPacketAttachment) packetAttachment;

                    if (signalPacketAttachment.isClient()) {
                        // 服务器收到signalPacketAttachment，不做任何处理
                        signalPacketAttachment.setClient(false);

                    } else {
                        // 客户端收到服务器应答，客户端发送的时候isClient为true，服务器收到的时候将其设置为false
                        var attachment = (SignalPacketAttachment) PacketSignal.removeSignalAttachment(signalPacketAttachment);
                        if (attachment != null) {
                            attachment.getResponseFuture().complete(packet);
                        } else {
                            logger.error("client receives packet:[{}] and packetAttachment:[{}] from server, but clientPacketAttachmentMap has no attachment, perhaps timeout exception."
                                    , JsonUtils.object2String(packet), JsonUtils.object2String(packetAttachment));
                        }
                        return;
                    }

                    break;
                case GATEWAY_PACKET:
                    var gatewayPacketAttachment = (GatewayPacketAttachment) packetAttachment;
                    if (gatewayPacketAttachment.isClient()) {
                        gatewayPacketAttachment.setClient(false);
                    } else {
                        var gatewaySession = NetContext.getSessionManager().getServerSession(gatewayPacketAttachment.getSid());
                        if (gatewaySession != null) {
                            var signalAttachment = gatewayPacketAttachment.getSignalPacketAttachment();
                            if (signalAttachment != null) {
                                signalAttachment.setClient(false);
                            }
                            // 网关授权，授权完成直接返回
                            if (AuthUidToGatewayCheck.getAuthProtocolId() == packet.protocolId()) {
                                var uid = ((AuthUidToGatewayCheck) packet).getUid();
                                if (uid <= 0) {
                                    logger.error("错误的网关授权信息，uid必须大于0");
                                    return;
                                }
                                gatewaySession.putAttribute(AttributeType.UID, uid);
                                EventBus.asyncSubmit(AuthUidToGatewayEvent.valueOf(gatewaySession.getSid(), uid));

                                NetContext.getDispatcher().send(session, AuthUidToGatewayConfirm.valueOf(uid), new GatewayPacketAttachment(gatewaySession, null));
                                return;
                            }
                            send(gatewaySession, packet, signalAttachment);
                        } else {
                            logger.error("gateway receives packet:[{}] and packetAttachment:[{}] from server" +
                                            ", but serverSessionMap has no session[id:{}], perhaps client disconnected from gateway."
                                    , JsonUtils.object2String(packet), JsonUtils.object2String(packetAttachment), gatewayPacketAttachment.getSid());
                        }
                        return;
                    }
                    break;
                default:
                    break;
            }
        }

        // 正常发送消息的接收
        TaskManager.getInstance().addTask(new ReceiveTask(session, packet, packetAttachment));
    }

    @Override
    public void send(Session session, IPacket packet, IPacketAttachment packetAttachment) {
        if (session == null) {
            logger.error("session is null and can not be sent.");
            return;
        }
        if (packet == null) {
            logger.error("packet is null and can not be sent.");
            return;
        }

        var packetInfo = EncodedPacketInfo.valueOf(packet, packetAttachment);

        var channel = session.getChannel();
        channel.writeAndFlush(packetInfo);
    }

    @Override
    public void send(Session session, IPacket packet) {
        // 服务器异步返回的消息的发送会有signalPacketAttachment，验证返回的消息是否满足
        var serverSignalPacketAttachment = serverReceiveSignalPacketAttachment.get();

        if (serverSignalPacketAttachment != null) {
            if (serverSignalPacketAttachment.isClient()) {
                // 客户端发送的时候不应该有serverSignalPacketAttachment
                logger.error("client can not have serverSignalPacketAttachment:[{}] and packet:[{}]", serverSignalPacketAttachment, packet);
            } else if (Error.errorProtocolId() == packet.protocolId()) {
                // 错误信息直接返回
            }
        }


        send(session, packet, serverSignalPacketAttachment);
    }


    @Override
    public <T extends IPacket> SyncAnswer<T> syncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception {
        var clientAttachment = new SignalPacketAttachment();
        var executorConsistentHash = (argument == null) ? RandomUtils.randomInt() : HashUtils.fnvHash(argument);
        clientAttachment.setExecutorConsistentHash(executorConsistentHash);

        try {
            PacketSignal.addSignalAttachment(clientAttachment);
            send(session, packet, clientAttachment);

            IPacket responsePacket = clientAttachment.getResponseFuture().get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responsePacket.protocolId() == Error.errorProtocolId()) {
                throw new ErrorResponseException((Error) responsePacket);
            }
            if (answerClass != null && answerClass != responsePacket.getClass()) {
                throw new UnexpectedProtocolException(StringUtils.format("client expect protocol:[{}], but found protocol:[{}]"
                        , answerClass, responsePacket.getClass().getName()));
            }

            return new SyncAnswer<>((T) responsePacket, clientAttachment);
        } catch (TimeoutException e) {
            throw new NetTimeOutException(StringUtils.format("syncRequest timeout exception, ask:[{}], attachment:[{}]"
                    , JsonUtils.object2String(packet), JsonUtils.object2String(clientAttachment)));
        } finally {
            PacketSignal.removeSignalAttachment(clientAttachment);
        }

    }

    @Override
    public <T extends IPacket> AsyncAnswer<T> asyncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument) {
        var clientAttachment = new SignalPacketAttachment();
        var executorConsistentHash = (argument == null) ? RandomUtils.randomInt() : HashUtils.fnvHash(argument);
        clientAttachment.setExecutorConsistentHash(executorConsistentHash);

        // 服务器在同步或异步的消息处理中，又调用了同步或异步的方法，这时候threadReceiverAttachment不为空
        var serverSignalPacketAttachment = serverReceiveSignalPacketAttachment.get();

        try {
            var asyncAnswer = new AsyncAnswer<T>();
            asyncAnswer.setFutureAttachment(clientAttachment);

            clientAttachment.getResponseFuture()
                    .completeOnTimeout(null, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                    .thenApply(answer -> {
                        if (answer == null) {
                            throw new NetTimeOutException(StringUtils.format("async ask [{}] timeout exception", packet.getClass().getSimpleName()));
                        }

                        if (answer.protocolId() == Error.errorProtocolId()) {
                            throw new ErrorResponseException((Error) answer);
                        }

                        if (answerClass != null && answerClass != answer.getClass()) {
                            throw new UnexpectedProtocolException("client expect protocol:[{}], but found protocol:[{}]", answerClass, answer.getClass().getName());
                        }
                        return answer;
                    })
                    .whenCompleteAsync((answer, throwable) -> {
                        try {
                            PacketSignal.removeSignalAttachment(clientAttachment);

                            // 接收者在同步或异步的消息处理中，又调用了异步的方法，这时候threadServerAttachment不为空
                            if (serverSignalPacketAttachment != null) {
                                serverReceiveSignalPacketAttachment.set(serverSignalPacketAttachment);
                            }

                            // 如果有异常的话，whenCompleteAsync的下一个thenAccept不会执行
                            if (throwable != null) {
                                var notCompleteCallback = asyncAnswer.getNotCompleteCallback();
                                if (notCompleteCallback != null) {
                                    notCompleteCallback.run();
                                }
                                return;
                            }

                            // 异步返回，回调业务逻辑
                            asyncAnswer.setFuturePacket((T) answer);
                            asyncAnswer.consume();
                        } catch (Throwable throwable1) {
                            logger.error("异步回调方法[ask:{}][answer:{}]错误", packet.getClass().getSimpleName(), answer.getClass().getSimpleName(), throwable1);
                        } finally {
                            if (serverSignalPacketAttachment != null) {
                                serverReceiveSignalPacketAttachment.set(null);
                            }
                        }

                    }, TaskManager.getInstance().getExecutorByConsistentHash(executorConsistentHash));


            PacketSignal.addSignalAttachment(clientAttachment);

            // 等到上层调用whenComplete才会发送消息
            asyncAnswer.setAskCallback(() -> send(session, packet, clientAttachment));
            return asyncAnswer;
        } catch (Exception e) {
            PacketSignal.removeSignalAttachment(clientAttachment);
            throw e;
        }
    }


    /**
     * 正常消息的接收
     * <p>
     * 发送者同时能发送多个包
     * 接收者同时只能处理一个session的一个包，同一个发送者发送过来的包排队处理
     */
    @Override
    public void atReceiver(Session session, IPacket packet, IPacketAttachment packetAttachment) {
        try {
            // 接收者（服务器）同步和异步消息的接收
            if (packetAttachment != null) {
                switch (packetAttachment.packetType()) {
                    case SIGNAL_PACKET:
                        serverReceiveSignalPacketAttachment.set((SignalPacketAttachment) packetAttachment);
                        break;
                    default:
                        break;
                }
            }

            // 调用PacketReceiver
            PacketBus.submit(session, packet, packetAttachment);
        } catch (Exception e) {
            logger.error(StringUtils.format("e[uid:{}][sid:{}]未知exception异常", session.getAttribute(AttributeType.UID), session.getSid(), e.getMessage()), e);
        } catch (Throwable t) {
            logger.error(StringUtils.format("e[uid:{}][sid:{}]未知error错误", session.getAttribute(AttributeType.UID), session.getSid(), t.getMessage()), t);
        } finally {
            // 如果有服务器在处理同步或者异步消息的时候由于错误没有返回给客户端消息，则可能会残留serverAttachment，所以先移除
            if (packetAttachment != null) {
                switch (packetAttachment.packetType()) {
                    case SIGNAL_PACKET:
                        serverReceiveSignalPacketAttachment.set(null);
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
