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
import com.zfoo.net.enhance.EnhanceUtils;
import com.zfoo.net.enhance.IPacketReceiver;
import com.zfoo.net.enhance.PacketReceiverDefinition;
import com.zfoo.net.packet.EncodedPacketInfo;
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
import com.zfoo.net.session.Session;
import com.zfoo.net.task.PacketReceiverTask;
import com.zfoo.net.task.TaskBus;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.ExceptionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.util.*;
import io.netty.util.collection.ShortObjectHashMap;
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

    protected final ShortObjectHashMap<IPacketReceiver> receiverMap = new ShortObjectHashMap<>();

    /**
     * As the server receiver, the attachment received in receive() is stored here, applicable only to task threads.
     * atReceiver sets the attachment and clears it after the method returns, no special attention needed.
     * asyncAsk sets the attachment again and requires careful attention.
     */
    protected final FastThreadLocalAdapter<Object> serverReceiverAttachmentThreadLocal = new FastThreadLocalAdapter<>();

    /**
     * Called after the server receives data. This method is invoked from channelRead in BaseRouteHandler.java.
     */
    @Override
    public void receive(Session session, Object packet, @Nullable Object attachment) {
        if (packet.getClass() == Heartbeat.class) {
            logger.info("server receive heartbeat from [sid:{}]", session.getSid());
            return;
        }

        var task = new PacketReceiverTask(session, packet, attachment);
        if (attachment == null) {
            // Normal message reception: wrap the client's request and dispatch it to the thread specified by the routing strategy
            // When the client sends a request via asyncAsk, the attachment is non-null and the server also enters this receive method without returning early
            dispatchBySession(task);
            return;
        }

        // Reception of sync/async responses for the sender (client), deduplicated by signalId
        if (attachment.getClass() == SignalAttachment.class) {
            var signalAttachment = (SignalAttachment) attachment;

            if (signalAttachment.getClient() == SignalAttachment.SIGNAL_OUTSIDE_CLIENT) {
                // Server received SIGNAL_OUTSIDE_CLIENT from an external client, no special handling needed
                dispatchBySession(task);
            } else if (signalAttachment.getClient() == SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT) {
                signalAttachment.setClient(SignalAttachment.SIGNAL_SERVER);
                dispatchByTaskExecutorHash(signalAttachment.getTaskExecutorHash(), task);
            } else if (signalAttachment.getClient() == SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT) {
                signalAttachment.setClient(SignalAttachment.SIGNAL_SERVER);
                dispatchBySession(task);
            } else {
                // Client received a response from the server; client sets it to SIGNAL_NATIVE_CLIENT when sending, server changes it to SIGNAL_SERVER
                var removedAttachment = (SignalAttachment) SignalBridge.removeSignalAttachment(signalAttachment);
                if (removedAttachment == null) {
                    logger.error("client receives packet:[{}] [{}] and attachment:[{}] [{}] from server, but clientAttachmentMap has no attachment, perhaps timeout exception."
                            , packet.getClass().getSimpleName(), JsonUtils.object2String(packet), attachment.getClass(), JsonUtils.object2String(attachment));
                    return;
                }
                // Completes the CompletableFuture so that asyncAsk callbacks can receive the result
                removedAttachment.getResponseFuture().complete(packet);
            }
            return;
        }

        if (attachment.getClass() == GatewayAttachment.class) {
            var gatewayAttachment = (GatewayAttachment) attachment;

            // For example: after the gateway detects GatewaySessionInactiveEvent and notifies the home server,
            // client is set to true. Note: no return here so the message can be forwarded from gateway to home for LogoutRequest handling.
            if (gatewayAttachment.isClient()) {
                gatewayAttachment.setClient(false);
                dispatchByTaskExecutorHash(gatewayAttachment.taskExecutorHash(), task);
            } else {
                // Another service provider sends authorization to the gateway, e.g., after login the home server looks up the player's uid and sends it to the gateway
                var gatewaySession = NetContext.getSessionManager().getServerSession(gatewayAttachment.getSid());
                if (gatewaySession == null) {
                    logger.error("gateway receives packet:[{}] and attachment:[{}] from server" + ", but serverSessionMap has no session[id:{}], perhaps client disconnected from gateway.", JsonUtils.object2String(packet), JsonUtils.object2String(attachment), gatewayAttachment.getSid());
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
     * The most important aspect of the Actor model is the threading model. The Actor model guarantees that
     * tasks represented by an Actor are never processed on two threads simultaneously, thereby avoiding concurrency issues.
     * Java, Kotlin, and Scala do not have true coroutines, so the Actor model can only be achieved through fine-grained thread control.
     * <p>
     * zfoo implements the Actor model through fine-grained thread pool control.
     * An Actor can be understood simply as a user or a player.
     * Because the uid of the same user/player is fixed, computing the consistent hash (taskExecutorHash) from uid always yields
     * the same result (sid is used if uid is unavailable), ensuring that all requests from the same user/player are always
     * routed to the same thread on the same server via taskExecutorHash, thus eliminating concurrency and achieving lock-free design.
     * <p>
     * The Actor model in zfoo is a streamlined version that is transparent to the caller. In zfoo, actor = taskExecutorHash.
     * <p>
     * In this threading model, packets received by the server (wrapped as PacketReceiverTask) are always processed
     * on the same thread. TaskBus dispatches PacketReceiverTask via AbstractTaskDispatch, and the target thread is
     * determined by the taskExecutorHash from IAttachment.
     * <p>
     * This pipeline approach is very CPU-cache-friendly: Java threads mostly run on the same CPU core,
     * and user logic maps one-to-one to threads, maximizing CPU cache hit rate.
     * The larger the CPU cache, the higher the hit rate, and the more significant the performance improvement.
     * <p>
     * Regarding single-thread hotspot issues: under sufficient load (e.g., 5000 concurrent users on an 8-core server),
     * the sample size is large enough that the number of users per core is fairly balanced.
     * Probability theory tells us that large samples yield uniform distributions, so single-thread hotspot issues
     * are rare and can be ignored; otherwise just add more threads.
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
            case EventBus -> EventBus.asyncExecute(taskExecutorHash, packetReceiverTask);
            case NettyIO -> atReceiver(packetReceiverTask);
//            case VirtualThread -> Thread.ofVirtual().name("virtual-at" + clazz.getSimpleName()).start(() -> atReceiver(packetReceiverTask));
        }
    }

    @Override
    public void send(Session session, Object packet, Object attachment) {
        if (session == null || packet == null) {
            return;
        }
        var channel = session.getChannel();
        if (!channel.isActive()) {
            return;
        }
        var packetInfo = EncodedPacketInfo.valueOf(session.getSid(), session.getUid(), packet, attachment);
        if (!channel.isWritable()) {
            logger.warn("send msg error, protocol [{}] sid=[{}] uid=[{}] isActive=[{}] isWritable=[{}]"
                    , packet.getClass().getSimpleName(), session.getSid(), session.getUid(), channel.isActive(), channel.isWritable());
        }
        channel.writeAndFlush(packetInfo);
    }

    @Override
    public void send(Session session, Object packet) {
        // When the server sends an async response, there may be a signalAttachment to verify the response
        var serverSignalAttachment = serverReceiverAttachmentThreadLocal.get();
        send(session, packet, serverSignalAttachment);
    }


    @Override
    public <T> SyncAnswer<T> syncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) throws Exception {
        return syncAsk(session, packet, answerClass, argument, DEFAULT_TIMEOUT);
    }

    @Override
    public <T> SyncAnswer<T> syncAsk(Session session, Object packet, Class<T> answerClass, Object argument, long timeoutMillis) throws Exception {
        var clientSignalAttachment = new SignalAttachment();
        if (argument == null) {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT);
        } else {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT);
            clientSignalAttachment.setTaskExecutorHash(TaskBus.calTaskExecutorHash(argument));
        }

        try {
            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // Internally still calls the send method to send the message
            send(session, packet, clientSignalAttachment);

            Object responsePacket = clientSignalAttachment.getResponseFuture().get(timeoutMillis, TimeUnit.MILLISECONDS);

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
     * Note:
     * 1. Internally this still calls send() to deliver the message.
     * 2. The argument parameter only determines which thread executes on the provider side (it is essentially a hashId).
     *    For example, pass guildId for guild logic. After the callback, execution is guaranteed to return to the caller's thread.
     */
    @Override
    public <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, @Nullable Class<T> answerClass, @Nullable Object argument) {
        return asyncAsk(session, packet, answerClass, argument, DEFAULT_TIMEOUT);
    }

    @Override
    public <T> AsyncAnswer<T> asyncAsk(Session session, Object packet, Class<T> answerClass, Object argument, long timeoutMillis) {
        var clientSignalAttachment = new SignalAttachment();

        if (argument == null) {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_NO_ARGUMENT_CLIENT);
        } else {
            clientSignalAttachment.setClient(SignalAttachment.SIGNAL_NATIVE_ARGUMENT_CLIENT);
            clientSignalAttachment.setTaskExecutorHash(TaskBus.calTaskExecutorHash(argument));
        }

        // If the server calls a sync/async method within another sync/async handler, serverSignalAttachment will be non-null
        var serverSignalAttachment = serverReceiverAttachmentThreadLocal.get();

        try {
            var asyncAnswer = new AsyncAnswer<T>();
            asyncAnswer.setSignalAttachment(clientSignalAttachment);

            clientSignalAttachment.getResponseFuture()
                    .completeOnTimeout(null, timeoutMillis, TimeUnit.MILLISECONDS) // On timeout, the result is null
                    .thenApply(answer -> {
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
                    })
                    .whenCompleteAsync((answer, throwable) -> {
                        // Note: this callback is triggered when receive() completes the CompletableFuture
                        // because the asyncAsk message carries a non-null attachment
                        try {
                            SignalBridge.removeSignalAttachment(clientSignalAttachment);

                            // If the receiver calls another async method within a sync/async handler, serverSignalAttachment will be non-null
                            if (serverSignalAttachment != null) {
                                serverReceiverAttachmentThreadLocal.set(serverSignalAttachment);
                            }

                            // If there is an exception, the subsequent thenAccept will not be executed
                            if (throwable != null) {
                                var notCompleteCallback = asyncAnswer.getNotCompleteCallback();
                                if (notCompleteCallback != null) {
                                    notCompleteCallback.run();
                                } else {
                                    logger.error(ExceptionUtils.getMessage(throwable));
                                }
                                return;
                            }

                            // Async response received, invoke the business callback
                            @SuppressWarnings("unchecked")
                            var answerPacket = (T) answer;
                            asyncAnswer.setFuturePacket(answerPacket);
                            asyncAnswer.consume();
                        } catch (Throwable throwable1) {
                            logger.error("Async callback method [ask:{}][answer:{}] error", packet.getClass().getSimpleName(), answer.getClass().getSimpleName(), throwable1);
                        } finally {
                            if (serverSignalAttachment != null) {
                                serverReceiverAttachmentThreadLocal.set(null);
                            }
                        }

                    }, TaskBus.currentThreadExecutor());


            SignalBridge.addSignalAttachment(clientSignalAttachment);

            // The message is sent only after the caller invokes whenComplete
            asyncAnswer.setAskCallback(() -> send(session, packet, clientSignalAttachment));
            return asyncAnswer;
        } catch (Exception e) {
            SignalBridge.removeSignalAttachment(clientSignalAttachment);
            throw e;
        }
    }


    /**
     * Normal message reception.
     * <p>
     * A sender can send multiple packets simultaneously.
     * A receiver processes only one packet from a session at a time; packets from the same sender are queued.
     */
    @Override
    public void atReceiver(PacketReceiverTask packetReceiverTask) {
        var session = packetReceiverTask.getSession();
        var packet = packetReceiverTask.getPacket();
        var attachment = packetReceiverTask.getAttachment();

        // The routing of the message
        var receiver = receiverMap.get(ProtocolManager.protocolId(packet.getClass()));
        try {
            // The receiver (server) sets up the attachment for sync/async message handling
            serverReceiverAttachmentThreadLocal.set(attachment);

            // Assigning ThreadLocal inside a virtual thread may cause errors; provide a warning to users
            if (receiver.task() == Task.VirtualThread && receiver.attachment() == null) {
                logger.warn("virtual thread task can not set Attachment to ThreadLocal, may cause some sync and async timeout exception, please use attachment in receiver method signature and use attachment in sync and async request");
            }

            receiver.invoke(session, packet, attachment);
        } catch (Exception e) {
            exceptionHandler(e, packetReceiverTask);
        } catch (Throwable t) {
            throwableHandler(t, packetReceiverTask);
        } finally {
            // If the server fails to respond during sync/async processing, a stale serverAttachment may remain; clear it here
            serverReceiverAttachmentThreadLocal.set(null);
        }
    }

    protected void exceptionHandler(Exception e, PacketReceiverTask packetReceiverTask) {
        var session = packetReceiverTask.getSession();
        var packet = packetReceiverTask.getPacket();
        var attachment = packetReceiverTask.getAttachment();
        EventBus.post(ServerExceptionEvent.valueOf(session, packet, attachment, e));
        logger.error("at{} e[uid:{}][sid:{}] invoke exception", StringUtils.capitalize(packet.getClass().getSimpleName()), session.getUid(), session.getSid(), e);
    }

    protected void throwableHandler(Throwable t, PacketReceiverTask packetReceiverTask) {
        var session = packetReceiverTask.getSession();
        var packet = packetReceiverTask.getPacket();
        logger.error("at{} e[uid:{}][sid:{}] invoke error", StringUtils.capitalize(packet.getClass().getSimpleName()), session.getUid(), session.getSid(), t);
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

            AssertionUtils.isTrue(paramClazzs.length == 2 || paramClazzs.length == 3, "[class:{}] [method:{}] must have two or three parameter! such as atMethod(session, packet) or atMethod(session, packet, attachment)", bean.getClass().getName(), method.getName());

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
            if (attachmentClazz != null && packetName.endsWith(GenerateProtocolFile.NET_ASK_SUFFIX)) {
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
