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

package com.zfoo.net.router.route;

import com.zfoo.event.model.event.IEvent;
import com.zfoo.net.packet.service.PacketService;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.HttpAttachment;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.attachment.UdpAttachment;
import com.zfoo.net.router.receiver.EnhanceUtils;
import com.zfoo.net.router.receiver.IPacketReceiver;
import com.zfoo.net.router.receiver.PacketReceiver;
import com.zfoo.net.router.receiver.PacketReceiverDefinition;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

/**
 * 包的接收路线，服务器收到packet调用对应的Receiver
 *
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class PacketBus {

    private static final Logger logger = LoggerFactory.getLogger(PacketBus.class);

    /**
     * 正常消息的接收
     * <p>
     * 发送者同时能发送多个包
     * 接收者同时只能处理一个session的一个包，同一个发送者发送过来的包排队处理
     */
    public static void submit(Session session, IPacket packet, IAttachment attachment) {
        // 客户端和服务端都有接受packet的方法，packetReceiverList对应的就是包的接收方法，将receiver注册到IProtocolRegistration
        var packetReceiver = (IPacketReceiver) ProtocolManager.getProtocol(packet.protocolId()).receiver();
        if (packetReceiver == null) {
            throw new RuntimeException(StringUtils.format("no any packetReceiverDefinition found for this [packet:{}]", packet.getClass().getName()));
        }

        // 调用PacketReceiver
        packetReceiver.invoke(session, packet, attachment);
    }


    public static void registerPacketReceiverDefinition(Object bean) {
        var clazz = bean.getClass();

        var methods = ReflectionUtils.getMethodsByAnnoInPOJOClass(clazz, PacketReceiver.class);
        if (ArrayUtils.isEmpty(methods)) {
            return;
        }

        if (!ReflectionUtils.isPojoClass(clazz)) {
            logger.warn("消息注册类[{}]不是POJO类，父类的消息接收不会被扫描到", clazz);
        }

        for (var method : methods) {
            var paramClazzs = method.getParameterTypes();

            AssertionUtils.isTrue(paramClazzs.length == 2 || paramClazzs.length == 3
                    , "[class:{}] [method:{}] must have two or three parameter!", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(Session.class.isAssignableFrom(paramClazzs[0])
                    , "[class:{}] [method:{}],the first parameter must be Session type parameter Exception.", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(IPacket.class.isAssignableFrom(paramClazzs[1])
                    , "[class:{}] [method:{}],the second parameter must be IPacket type parameter Exception.", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(paramClazzs.length != 3 || IAttachment.class.isAssignableFrom(paramClazzs[2])
                    , "[class:{}] [method:{}],the third parameter must be IAttachment type parameter Exception.", bean.getClass().getName(), method.getName());

            var packetClazz = (Class<? extends IEvent>) paramClazzs[1];
            var attachmentClazz = paramClazzs.length == 3 ? paramClazzs[2] : null;
            var packetName = packetClazz.getCanonicalName();
            var methodName = method.getName();

            AssertionUtils.isTrue(Modifier.isPublic(method.getModifiers())
                    , "[class:{}] [method:{}] [packet:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName, packetName);

            AssertionUtils.isTrue(!Modifier.isStatic(method.getModifiers())
                    , "[class:{}] [method:{}] [packet:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName, packetName);

            var expectedMethodName = StringUtils.format("at{}", packetClazz.getSimpleName());
            AssertionUtils.isTrue(methodName.equals(expectedMethodName)
                    , "[class:{}] [method:{}] [packet:{}] expects '{}' as method name!", bean.getClass().getName(), methodName, packetName, expectedMethodName);

            // 如果以Request结尾的请求，那么attachment应该为GatewayAttachment
            // 如果以Ask结尾的请求，那么attachment不能为GatewayAttachment
            if (attachmentClazz != null) {
                if (packetName.endsWith(PacketService.NET_REQUEST_SUFFIX)) {
                    AssertionUtils.isTrue(attachmentClazz.equals(GatewayAttachment.class) || attachmentClazz.equals(UdpAttachment.class) || attachmentClazz.equals(HttpAttachment.class)
                            , "[class:{}] [method:{}] [packet:{}] must use [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayAttachment.class.getCanonicalName());
                } else if (packetName.endsWith(PacketService.NET_ASK_SUFFIX)) {
                    AssertionUtils.isTrue(!attachmentClazz.equals(GatewayAttachment.class)
                            , "[class:{}] [method:{}] [packet:{}] can not match with [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayAttachment.class.getCanonicalName());
                }
            }

            try {
                var protocolId = ProtocolAnalysis.getProtocolIdByClass(packetClazz);

                // 将receiver注册到IProtocolRegistration
                var protocolRegistration = ProtocolManager.getProtocol(protocolId);
                AssertionUtils.notNull(protocolRegistration, "协议类[class:{}][protocolId:{}]没有注册", packetClazz.getSimpleName(), protocolId);
                AssertionUtils.isNull(protocolRegistration.receiver(), "协议类[class:{}]被重复接收[at{}]，一个协议只能对应一个被@PacketReceiver标注的方法"
                        , packetClazz.getSimpleName(), packetClazz.getSimpleName());

                var receiverDefinition = new PacketReceiverDefinition(bean, method, packetClazz, attachmentClazz);
                var enhanceReceiverDefinition = EnhanceUtils.createPacketReceiver(receiverDefinition);

                var receiverField = ReflectionUtils.getFieldByNameInPOJOClass(protocolRegistration.getClass(), "receiver");
                ReflectionUtils.makeAccessible(receiverField);
                ReflectionUtils.setField(receiverField, protocolRegistration, enhanceReceiverDefinition);
            } catch (Throwable t) {
                throw new RunException(t, "解析协议类[class:{}]未知异常", packetClazz.getSimpleName());
            }
        }
    }

}
