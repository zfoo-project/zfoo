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

import com.zfoo.event.manager.EventBus;
import com.zfoo.net.NetContext;
import com.zfoo.net.core.event.ServerExceptionEvent;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayCheck;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayConfirm;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayEvent;
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.IPacket;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.packet.common.Heartbeat;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.router.exception.ErrorResponseException;
import com.zfoo.net.router.exception.NetTimeOutException;
import com.zfoo.net.router.exception.UnexpectedProtocolException;
import com.zfoo.net.router.route.PacketBus;
import com.zfoo.net.router.route.SignalBridge;
import com.zfoo.net.session.Session;
import com.zfoo.net.task.PacketReceiverTask;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Message distribution
 *
 * @author godotg
 * @version 3.0
 */
public class Router implements IRouter {

    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static final long DEFAULT_TIMEOUT = 3000;

    /**
     * 作为服务器接收方，会把receive收到的attachment存储在这个地方，只针对task线程。
     * atReceiver会设置attachment，但是在方法调用完成会取消，不需要过多关注。
     * asyncAsk会再次设置attachment，需要重点关注。
     */
    private final FastThreadLocal<IAttachment> serverReceiverAttachmentThreadLocal = new FastThreadLocal<>();

    /**
     * 在服务端收到数据后，会调用这个方法. 这个方法在BaseRouteHandler.java的channelRead中被调用
     */
    @Override
    public void receive(Session session, IPacket packet, @Nullable IAttachment attachment) {
        if (packet.getClass() == Heartbeat.class) {
            logger.info("heartbeat");
            return;
        }

        // 发送者（客户端）同步和异步消息的接收，发送者通过signalId判断重复
        if (attachment != null) {
            switch (attachment.packetType()) {
                case SIGNAL_PACKET:
                    var signalAttachment = (SignalAttachment) attachment;

                    if (signalAttachment.isClient()) {
                        // 服务器收到signalAttachment，不做任何处理
                        signalAttachment.setClient(false);
                    } else {
                        // 客户端收到服务器应答，客户端发送的时候isClient为true，服务器收到的时候将其设置为false
                        var removedAttachment = (SignalAttachment) SignalBridge.removeSignalAttachment(signalAttachment);
                        if (removedAttachment != null) {
                            // 这里会让之前的CompletableFuture得到结果，从而像asyncAsk之类的回调到结果
                            removedAttachment.getResponseFuture().complete(packet);
                        } else {
                            logger.error("client receives packet:[{}] and attachment:[{}] from server, but clientAttachmentMap has no attachment, perhaps timeout exception.", JsonUtils.object2String(packet), JsonUtils.object2String(attachment));
                        }
                        // 注意：这个return，这样子，asyncAsk的结果就返回了。
                        return;
                    }
                    break;
                case GATEWAY_PACKET:
                    var gatewayAttachment = (GatewayAttachment) attachment;

                    // 如：在网关监听到GatewaySessionInactiveEvent后，这时告诉home时，这个client参数设置的true
                    // 注意：此时并没有return，这样子网关的消息才能发给home，在home进行处理LogoutRequest消息的处理
                    if (gatewayAttachment.isClient()) {
                        gatewayAttachment.setClient(false);
                    } else {
                        // 这里是：别的服务提供者提供授权给网关，比如：在玩家登录后，home服查到了玩家uid，然后发给Gateway服
                        var gatewaySession = NetContext.getSessionManager().getServerSession(gatewayAttachment.getSid());
                        if (gatewaySession != null) {
                            var signalAttachmentInGatewayAttachment = gatewayAttachment.getSignalAttachment();
                            if (signalAttachmentInGatewayAttachment != null) {
                                signalAttachmentInGatewayAttachment.setClient(false);
                            }

                            // 网关授权，授权完成直接返回
                            // 注意：这个 AuthUidToGatewayCheck 是在home的LoginController中处理完登录后，把消息发给网关进行授权
                            if (AuthUidToGatewayCheck.class == packet.getClass()) {
                                var uid = ((AuthUidToGatewayCheck) packet).getUid();
                                if (uid <= 0) {
                                    logger.error("错误的网关授权信息，uid必须大于0");
                                    return;
                                }
                                gatewaySession.setUid(uid);
                                EventBus.post(AuthUidToGatewayEvent.valueOf(gatewaySession.getSid(), uid));

                                NetContext.getRouter().send(session, AuthUidToGatewayConfirm.valueOf(uid), new GatewayAttachment(gatewaySession));
                                return;
                            }
                            send(gatewaySession, packet, gatewayAttachment.attachment());
                        } else {
                            logger.error("gateway receives packet:[{}] and attachment:[{}] from server" + ", but serverSessionMap has no session[id:{}], perhaps client disconnected from gateway.", JsonUtils.object2String(packet), JsonUtils.object2String(attachment), gatewayAttachment.getSid());
                        }
                        return;
                    }
                    break;
                default:
                    break;
            }
        }

        // 正常发送消息的接收,把客户端的业务请求包装下到路由策略指定的线程进行业务处理
        // 注意：像客户端以asyncAsk发送请求，在服务器处理完后返回结果，在请求方也是进入这个receive方法，但是attachment不为空，会提前return掉不会走到这
        TaskBus.dispatch(new PacketReceiverTask(session, packet, attachment));
    }

    @Override
    public void send(Session session, IPacket packet, IAttachment attachment) {
        if (session == null) {
            logger.error("session is null and can not be sent.");
            return;
        }
        if (packet == null) {
            logger.error("packet is null and can not be sent.");
            return;
        }

        var packetInfo = EncodedPacketInfo.valueOf(packet, attachment);

        var channel = session.getChannel();
        if (!channel.isActive() || !channel.isWritable()) {
            logger.warn("send msg error, protocol [{}] isActive=[{}] isWritable=[{}]"
                    , packet.getClass().getSimpleName(), channel.isActive(), channel.isWritable());
        }
        channel.writeAndFlush(packetInfo);
    }

    @Override
    public void send(Session session, IPacket packet) {
        // 服务器异步返回的消息的发送会有signalAttachment，验证返回的消息是否满足
        var serverSignalAttachment = serverReceiverAttachmentThreadLocal.get();
        send(session, packet, serverSignalAttachment);
    }


    @Override
    public <T extends IPacket> SyncAnswer<T> syncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception {
        var clientSignalAttachment = new SignalAttachment();
        var taskExecutorHash = TaskBus.calTaskExecutorHash(argument);
        clientSignalAttachment.setTaskExecutorHash(taskExecutorHash);

        try {
            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // 里面调用的依然是：send方法发送消息
            send(session, packet, clientSignalAttachment);

            IPacket responsePacket = clientSignalAttachment.getResponseFuture().get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responsePacket.getClass() == Error.class) {
                throw new ErrorResponseException((Error) responsePacket);
            }
            if (answerClass != null && answerClass != responsePacket.getClass()) {
                throw new UnexpectedProtocolException("client expect protocol:[{}], but found protocol:[{}]", answerClass, responsePacket.getClass().getName());
            }

            return new SyncAnswer<>((T) responsePacket, clientSignalAttachment);
        } catch (TimeoutException e) {
            throw new NetTimeOutException("syncAsk timeout exception, ask:[{}], attachment:[{}]", JsonUtils.object2String(packet), JsonUtils.object2String(clientSignalAttachment));
        } finally {
            SignalBridge.removeSignalAttachment(clientSignalAttachment);
        }

    }

    /**
     * 注意：
     * 1.这个里面其实还是调用send发送的消息
     * 2.这个argument的参数，只用于provider处哪个线程执行，其实就是hashId，如：工会业务，则传入guildId，回调回来后，一定会在发起者线程。
     */
    @Override
    public <T extends IPacket> AsyncAnswer<T> asyncAsk(Session session, IPacket packet, @Nullable Class<T> answerClass, @Nullable Object argument) {
        var clientSignalAttachment = new SignalAttachment();
        var taskExecutorHash = TaskBus.calTaskExecutorHash(argument);

        clientSignalAttachment.setTaskExecutorHash(taskExecutorHash);

        // 服务器在同步或异步的消息处理中，又调用了同步或异步的方法，这时候threadReceiverAttachment不为空
        var serverSignalAttachment = serverReceiverAttachmentThreadLocal.get();

        try {
            var asyncAnswer = new AsyncAnswer<T>();
            asyncAnswer.setSignalAttachment(clientSignalAttachment);

            clientSignalAttachment.getResponseFuture()
                    // 因此超时的情况，返回的是null
                    .completeOnTimeout(null, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS).thenApply(answer -> {
                        if (answer == null) {
                            throw new NetTimeOutException("async ask [{}] timeout exception", packet.getClass().getSimpleName());
                        }

                        if (answer.getClass() == Error.class) {
                            throw new ErrorResponseException((Error) answer);
                        }

                        if (answerClass != null && answerClass != answer.getClass()) {
                            throw new UnexpectedProtocolException("client expect protocol:[{}], but found protocol:[{}]", answerClass, answer.getClass().getName());
                        }
                        return answer;
                    }).whenCompleteAsync((answer, throwable) -> {
                        // 注意：进入这个方法的时机是：在上面的receive方法中，由于是asyncAsk的消息，attachment不为空，会调用CompletableFuture的complete方法
                        try {
                            SignalBridge.removeSignalAttachment(clientSignalAttachment);

                            // 接收者在同步或异步的消息处理中，又调用了异步的方法，这时候threadServerAttachment不为空
                            if (serverSignalAttachment != null) {
                                serverReceiverAttachmentThreadLocal.set(serverSignalAttachment);
                            }

                            // 如果有异常的话，whenCompleteAsync的下一个thenAccept不会执行
                            if (throwable != null) {
                                var notCompleteCallback = asyncAnswer.getNotCompleteCallback();
                                if (notCompleteCallback != null) {
                                    notCompleteCallback.run();
                                } else {
                                    logger.error(ExceptionUtils.getMessage(throwable));
                                }
                                return;
                            }

                            // 异步返回，回调业务逻辑
                            asyncAnswer.setFuturePacket((T) answer);
                            asyncAnswer.consume();
                        } catch (Throwable throwable1) {
                            logger.error("Asynchronous callback method [ask:{}][answer:{}] error", packet.getClass().getSimpleName(), answer.getClass().getSimpleName(), throwable1);
                        } finally {
                            if (serverSignalAttachment != null) {
                                serverReceiverAttachmentThreadLocal.set(null);
                            }
                        }

                    }, TaskBus.currentThreadExecutor());


            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // 等到上层调用whenComplete才会发送消息
            asyncAnswer.setAskCallback(() -> send(session, packet, clientSignalAttachment));
            return asyncAnswer;
        } catch (Exception e) {
            SignalBridge.removeSignalAttachment(clientSignalAttachment);
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
    public void atReceiver(Session session, IPacket packet, IAttachment attachment) {
        try {
            // 接收者（服务器）同步和异步消息的接收
            if (attachment != null) {
                serverReceiverAttachmentThreadLocal.set(attachment);
            }

            // 调用PacketReceiver,进行真正的业务处理,这个submit只是根据packet找到protocolId，然后调用对应的消息处理方法
            // 这个在哪个线程处理取决于：这个上层的PacketReceiverTask被丢到了哪个线程中
            PacketBus.route(session, packet, attachment);
        } catch (Exception e) {
            EventBus.post(ServerExceptionEvent.valueOf(session, packet, attachment, e));
            logger.error(StringUtils.format("e[uid:{}][sid:{}] unknown exception", session.getUid(), session.getSid(), e.getMessage()), e);
        } catch (Throwable t) {
            logger.error(StringUtils.format("e[uid:{}][sid:{}] unknown error", session.getUid(), session.getSid(), t.getMessage()), t);
        } finally {
            // 如果有服务器在处理同步或者异步消息的时候由于错误没有返回给客户端消息，则可能会残留serverAttachment，所以先移除
            if (attachment != null) {
                serverReceiverAttachmentThreadLocal.set(null);
            }
        }
    }

}
