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
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.anno.Task;
import com.zfoo.net.core.event.ServerExceptionEvent;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayCheck;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayConfirm;
import com.zfoo.net.core.gateway.model.AuthUidToGatewayEvent;
import com.zfoo.net.packet.EncodedPacketInfo;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.packet.common.Error;
import com.zfoo.net.packet.common.Heartbeat;
import com.zfoo.net.router.answer.AsyncAnswer;
import com.zfoo.net.router.answer.SyncAnswer;
import com.zfoo.net.router.attachment.AttachmentType;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.HttpAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.router.exception.ErrorResponseException;
import com.zfoo.net.router.exception.NetTimeOutException;
import com.zfoo.net.router.exception.UnexpectedProtocolException;
import com.zfoo.net.enhance.EnhanceUtils;
import com.zfoo.net.enhance.IPacketReceiver;
import com.zfoo.net.enhance.PacketReceiverDefinition;
import com.zfoo.net.session.Session;
import com.zfoo.net.task.PacketReceiverTask;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.*;
import io.netty.util.collection.ShortObjectHashMap;
import io.netty.util.concurrent.FastThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.reflect.Modifier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Message distribution
 *
 * @author godotg
 */
public class Router implements IRouter {

    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static final long DEFAULT_TIMEOUT = 3000;

    private final ShortObjectHashMap<IPacketReceiver> receiverMap = new ShortObjectHashMap<>();

    /**
     * 作为服务器接收方，会把receive收到的attachment存储在这个地方，只针对task线程。
     * atReceiver会设置attachment，但是在方法调用完成会取消，不需要过多关注。
     * asyncAsk会再次设置attachment，需要重点关注。
     */
    private final FastThreadLocal<Object> serverReceiverAttachmentThreadLocal = new FastThreadLocal<>();

    /**
     * 在服务端收到数据后，会调用这个方法. 这个方法在BaseRouteHandler.java的channelRead中被调用
     */
    @Override
    public void receive(Session session, Object packet, @Nullable Object attachment) {
        if (packet.getClass() == Heartbeat.class) {
            logger.info("heartbeat");
            return;
        }

        var task = new PacketReceiverTask(session, packet, attachment);
        if (attachment == null) {
            // 正常发送消息的接收,把客户端的业务请求包装下到路由策略指定的线程进行业务处理
            // 注意：像客户端以asyncAsk发送请求，在服务器处理完后返回结果，在请求方也是进入这个receive方法，但是attachment不为空，会提前return掉不会走到这
            dispatchBySession(task);
            return;
        }

        // 发送者（客户端）同步和异步消息的接收，发送者通过signalId判断重复
        if (attachment.getClass() == SignalAttachment.class) {
            var signalAttachment = (SignalAttachment) attachment;

            if (signalAttachment.getClient() == SignalAttachment.SIGNAL_OUTSIDE_CLIENT) {
                // 服务器收到外部客户端的SIGNAL_OUTSIDE_CLIENT，不做任何处理
                dispatchBySession(task);
            } else if (signalAttachment.getClient() == SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT) {
                signalAttachment.setClient(SignalAttachment.SIGNAL_SERVER);
                dispatchByTaskExecutorHash(signalAttachment.getTaskExecutorHash(), task);
            } else if (signalAttachment.getClient() == SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT) {
                signalAttachment.setClient(SignalAttachment.SIGNAL_SERVER);
                dispatchBySession(task);
            } else {
                // 客户端收到服务器应答，客户端发送的时候client为SIGNAL_NATIVE_CLIENT，服务器收到的时候将其设置为SIGNAL_SERVER
                var removedAttachment = (SignalAttachment) SignalBridge.removeSignalAttachment(signalAttachment);
                if (removedAttachment == null) {
                    logger.error("client receives packet:[{}] [{}] and attachment:[{}] [{}] from server, but clientAttachmentMap has no attachment, perhaps timeout exception."
                            , packet.getClass().getSimpleName(), JsonUtils.object2String(packet), attachment.getClass(), JsonUtils.object2String(attachment));
                    return;
                }
                // 这里会让之前的CompletableFuture得到结果，从而像asyncAsk之类的回调到结果
                removedAttachment.getResponseFuture().complete(packet);
            }
            return;
        }

        if (attachment.getClass() == GatewayAttachment.class) {
            var gatewayAttachment = (GatewayAttachment) attachment;

            // 如：在网关监听到GatewaySessionInactiveEvent后，这时告诉home时，这个client参数设置的true
            // 注意：此时并没有return，这样子网关的消息才能发给home，在home进行处理LogoutRequest消息的处理
            if (gatewayAttachment.isClient()) {
                gatewayAttachment.setClient(false);
                dispatchByTaskExecutorHash(gatewayAttachment.taskExecutorHash(), task);
            } else {
                // 这里是：别的服务提供者提供授权给网关，比如：在玩家登录后，home服查到了玩家uid，然后发给Gateway服
                var gatewaySession = NetContext.getSessionManager().getServerSession(gatewayAttachment.getSid());
                if (gatewaySession == null) {
                    logger.warn("gateway receives packet:[{}] and attachment:[{}] from server" + ", but serverSessionMap has no session[id:{}], perhaps client disconnected from gateway.", JsonUtils.object2String(packet), JsonUtils.object2String(attachment), gatewayAttachment.getSid());
                    return;
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
                send(gatewaySession, packet, gatewayAttachment.getSignalAttachment());
            }
            return;
        }

        if (attachment.getClass() == HttpAttachment.class) {
            var httpAttachment = (HttpAttachment) attachment;
            dispatchByTaskExecutorHash(httpAttachment.getTaskExecutorHash(), task);
            return;
        }

        dispatchBySession(task);
    }

    /**
     * Actor模型，最主要的就是线程模型，Actor模型保证了某个Actor所代表的任务永远不会同时在两条线程同时处理任务，这就避免了并发。
     * 无论是Java，Kotlin，Scala都没有真正的协程，所以最终做到Actor模型的只能是细致的控制线程。
     * <p>
     * zfoo中通过对线程池的细粒度控制，从而实现了Actor模型。
     * 为了简单，可以把Actor可以理解为一个用户或者一个玩家。
     * 因为同一个用户或者玩家的uid是固定的，通过uid去计算一致性hash（taskExecutorHash）永远会得到一致的结果，
     * 从而保证同一个用户或者玩家的请求总能通过taskExecutorHash被路由到同一台服务器的同一个线程去执行，从而避免了并发，实现了无锁化。
     * <p>
     * zfoo所代表的Actor模型，是更加精简的Actor模型，让上层调用无感知，在zfoo中可以简单的理解 actor = taskExecutorHash。
     * <p>
     * 在zfoo这套线程模型中，保证了服务器所接收到的Packet（最终被包装成PacketReceiverTask任务），永远只会在同一条线程处理，
     * TaskBus通过AbstractTaskDispatch去派发PacketReceiverTask任务，具体在哪个线程处理通过IAttachment的taskExecutorHash计算。
     * <p>
     * IAttachment的不同，taskExecutorHash也不同：
     * GatewayAttachment：默认是taskExecutorHash等于用户活玩家的uid，也可以通过IGatewayLoadBalancer接口指定
     * SignalAttachment：taskExecutorHash通过IRouter和IConsumer的argument参数指定
     */
    public void dispatchBySession(PacketReceiverTask task) {
        var session = task.getSession();
        var uid = session.getUid();
        if (uid > 0) {
            dispatchByTaskExecutorHash((int) uid, task);
        } else {
            dispatchByTaskExecutorHash((int) session.getSid(), task);
        }
    }

    public void dispatchByTaskExecutorHash(int taskExecutorHash, PacketReceiverTask packetReceiverTask) {
        var packet = packetReceiverTask.getPacket();
        var clazz = packet.getClass();
        var receiver = receiverMap.get(ProtocolManager.protocolId(clazz));
        if (receiver == null) {
            var name = packet.getClass().getSimpleName();
            throw new RuntimeException(StringUtils.format("no any packetReceiver:[at{}] found for this packet:[{}] or no GatewayAttachment sent back if this server is gateway", name, name));
        }

        switch (receiver.task()) {
            case TaskBus -> TaskBus.execute(taskExecutorHash, packetReceiverTask);
            case NettyIO -> atReceiver(packetReceiverTask);
//            case VirtualThread -> Thread.ofVirtual().name("virtual-at" + clazz.getSimpleName()).start(() -> atReceiver(packetReceiverTask));
        }
    }

    @Override
    public void send(Session session, Object packet, Object attachment) {
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
    public void send(Session session, Object packet) {
        // 服务器异步返回的消息的发送会有signalAttachment，验证返回的消息是否满足
        var serverSignalAttachment = serverReceiverAttachmentThreadLocal.get();
        send(session, packet, serverSignalAttachment);
    }


    @Override
    public <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception {
        var clientSignalAttachment = new SignalAttachment();
        if (argument == null) {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT);
        } else {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT);
            clientSignalAttachment.setTaskExecutorHash(TaskBus.calTaskExecutorHash(argument));
        }

        try {
            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // 里面调用的依然是：send方法发送消息
            send(session, packet, clientSignalAttachment);

            Object responsePacket = clientSignalAttachment.getResponseFuture().get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

            if (responsePacket.getClass() == Error.class) {
                throw new ErrorResponseException((Error) responsePacket);
            }
            if (answerClass != null && answerClass != responsePacket.getClass()) {
                throw new UnexpectedProtocolException("client expect protocol:[{}], but found protocol:[{}]", answerClass, responsePacket.getClass().getName());
            }

            @SuppressWarnings("unchecked")
            var syncAnswer = new SyncAnswer<>((T) responsePacket, clientSignalAttachment);
            return syncAnswer;
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
    public <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) {
        var clientSignalAttachment = new SignalAttachment();

        if (argument == null) {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT);
        } else {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT);
            clientSignalAttachment.setTaskExecutorHash(TaskBus.calTaskExecutorHash(argument));
        }

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
                            @SuppressWarnings("unchecked")
                            var answerPacket = (T) answer;
                            asyncAnswer.setFuturePacket(answerPacket);
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
    public void atReceiver(PacketReceiverTask packetReceiverTask) {
        var session = packetReceiverTask.getSession();
        var packet = packetReceiverTask.getPacket();
        var attachment = packetReceiverTask.getAttachment();

        // The routing of the message
        var receiver = receiverMap.get(ProtocolManager.protocolId(packet.getClass()));
        var threadLocalAttachment = attachment != null && receiver.task() != Task.VirtualThread;
        try {

            // 接收者（服务器）同步和异步消息的接收
            if (threadLocalAttachment) {
                serverReceiverAttachmentThreadLocal.set(attachment);
            }

            if (receiver.task() == Task.VirtualThread && receiver.attachment() == null) {
                logger.warn("virtual thread task can not set Attachment, may cause some sync and async timeout exception, please use attachment in receiver method signature and use attachment in sync and async request");
            }

            receiver.invoke(session, packet, attachment);
        } catch (Exception e) {
            EventBus.post(ServerExceptionEvent.valueOf(session, packet, attachment, e));
            logger.error(StringUtils.format("e[uid:{}][sid:{}] unknown exception", session.getUid(), session.getSid(), e.getMessage()), e);
        } catch (Throwable t) {
            logger.error(StringUtils.format("e[uid:{}][sid:{}] unknown error", session.getUid(), session.getSid(), t.getMessage()), t);
        } finally {
            // 如果有服务器在处理同步或者异步消息的时候由于错误没有返回给客户端消息，则可能会残留serverAttachment，所以先移除
            if (threadLocalAttachment) {
                serverReceiverAttachmentThreadLocal.set(null);
            }
        }
    }


    @Override
    public void registerPacketReceiverDefinition(Object bean) {
        var clazz = bean.getClass();

        var methods = ReflectionUtils.getMethodsByAnnoInPOJOClass(clazz, PacketReceiver.class);
        if (ArrayUtils.isEmpty(methods)) {
            return;
        }

        if (!ReflectionUtils.isPojoClass(clazz)) {
            logger.warn("The message registration class [{}] is not a POJO class, and the parent class will not be scanned", clazz);
        }

        for (var method : methods) {
            var paramClazzs = method.getParameterTypes();

            AssertionUtils.isTrue(paramClazzs.length == 2 || paramClazzs.length == 3, "[class:{}] [method:{}] must have two or three parameter!", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(Session.class.isAssignableFrom(paramClazzs[0]), "[class:{}] [method:{}],the first parameter must be Session type parameter Exception.", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(paramClazzs.length != 3 || AttachmentType.isAttachmentClass(paramClazzs[2]), "[class:{}] [method:{}],the third parameter must be Attachment type parameter Exception.", bean.getClass().getName(), method.getName());

            var packetClazz = paramClazzs[1];
            var attachmentClazz = paramClazzs.length == 3 ? paramClazzs[2] : null;
            var packetName = packetClazz.getCanonicalName();
            var methodName = method.getName();

            AssertionUtils.isTrue(Modifier.isPublic(method.getModifiers()), "[class:{}] [method:{}] [packet:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName, packetName);

            AssertionUtils.isTrue(!Modifier.isStatic(method.getModifiers()), "[class:{}] [method:{}] [packet:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName, packetName);

            var expectedMethodName = StringUtils.format("at{}", packetClazz.getSimpleName());
            AssertionUtils.isTrue(methodName.equals(expectedMethodName), "[class:{}] [method:{}] [packet:{}] expects '{}' as method name!", bean.getClass().getName(), methodName, packetName, expectedMethodName);

            // These rules are not necessary, but can reduce us from making low-level mistakes
            // If the request class name ends with Request which is for outer net client, then the attachment can not be a SignalAttachment
            // If the request class name ends with Ask which is for intranet client, then attachment can not be a GatewayAttachment
            if (attachmentClazz != null && packetName.endsWith(PacketService.NET_ASK_SUFFIX)) {
                AssertionUtils.isTrue(!attachmentClazz.equals(GatewayAttachment.class), "[class:{}] [method:{}] [packet:{}] can not match with [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayAttachment.class.getCanonicalName());
            }

            var protocolId = Short.MIN_VALUE;
            try {
                protocolId = ProtocolManager.protocolId(packetClazz);
            } catch (Exception e) {
                throw new RunException("[class:{}][protocolId:{}] has no registration, please register for this protocol", packetClazz.getSimpleName(), protocolId);
            }

            try {
                AssertionUtils.isNull(receiverMap.get(protocolId), "duplicate protocol registration, @PacketReceiver [class:{}] is repeatedly received [at{}]", packetClazz.getSimpleName(), packetClazz.getSimpleName());
                var task = method.getDeclaredAnnotation(PacketReceiver.class).value();
                var receiverDefinition = new PacketReceiverDefinition(bean, method, task, packetClazz, attachmentClazz);
                if (GraalVmUtils.isGraalVM()) {
                    receiverMap.put(protocolId, receiverDefinition);
                } else {
                    var enhanceReceiverDefinition = EnhanceUtils.createPacketReceiver(receiverDefinition);
                    receiverMap.put(protocolId, enhanceReceiverDefinition);
                }
            } catch (Throwable t) {
                throw new RunException("Registration protocol [class:{}] unknown exception", packetClazz.getSimpleName(), t);
            }
        }
    }
}
