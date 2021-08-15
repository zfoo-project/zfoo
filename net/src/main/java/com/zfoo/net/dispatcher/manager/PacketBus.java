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

import com.zfoo.event.model.event.IEvent;
import com.zfoo.net.dispatcher.model.anno.PacketReceiver;
import com.zfoo.net.dispatcher.model.vo.EnhanceUtils;
import com.zfoo.net.dispatcher.model.vo.IPacketReceiver;
import com.zfoo.net.dispatcher.model.vo.PacketReceiverDefinition;
import com.zfoo.net.packet.model.GatewayPacketAttachment;
import com.zfoo.net.packet.model.HttpPacketAttachment;
import com.zfoo.net.packet.model.IPacketAttachment;
import com.zfoo.net.packet.model.UdpPacketAttachment;
import com.zfoo.net.packet.service.PacketService;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class PacketBus {

    private static final Logger logger = LoggerFactory.getLogger(PacketBus.class);

    /**
     * 客户端和服务端都有接受packet的方法，packetReceiverList对应的就是包的接收方法，将receiver注册到IProtocolRegistration
     */
    public static final IProtocolRegistration[] packetReceiverList = ProtocolManager.protocols;


    /**
     * 正常消息的接收
     * <p>
     * 发送者同时能发送多个包
     * 接收者同时只能处理一个session的一个包，同一个发送者发送过来的包排队处理
     */
    public static void submit(Session session, IPacket packet, IPacketAttachment packetAttachment) {
        var packetReceiver = (IPacketReceiver) packetReceiverList[packet.protocolId()].receiver();
        if (packetReceiver == null) {
            throw new RuntimeException(StringUtils.format("no any packetReceiverDefinition found for this [packet:{}]", packet.getClass().getName()));
        }

        // 调用PacketReceiver
        packetReceiver.invoke(session, packet, packetAttachment);
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

            AssertionUtils.isTrue(paramClazzs.length != 3 || IPacketAttachment.class.isAssignableFrom(paramClazzs[2])
                    , "[class:{}] [method:{}],the third parameter must be IPacketAttachment type parameter Exception.", bean.getClass().getName(), method.getName());

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
                    AssertionUtils.isTrue(attachmentClazz.equals(GatewayPacketAttachment.class) || attachmentClazz.equals(UdpPacketAttachment.class) || attachmentClazz.equals(HttpPacketAttachment.class)
                            , "[class:{}] [method:{}] [packet:{}] must use [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayPacketAttachment.class.getCanonicalName());
                } else if (packetName.endsWith(PacketService.NET_ASK_SUFFIX)) {
                    AssertionUtils.isTrue(!attachmentClazz.equals(GatewayPacketAttachment.class)
                            , "[class:{}] [method:{}] [packet:{}] can not match with [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayPacketAttachment.class.getCanonicalName());
                }
            }

            try {
                var protocolIdField = packetClazz.getDeclaredField(ProtocolManager.PROTOCOL_ID);
                ReflectionUtils.makeAccessible(protocolIdField);
                var protocolId = (short) protocolIdField.get(null);
                var receiverDefinition = new PacketReceiverDefinition(bean, method, packetClazz, attachmentClazz);
                var enhanceReceiverDefinition = EnhanceUtils.createPacketReceiver(receiverDefinition);

                // 将receiver注册到IProtocolRegistration
                var protocolRegistration = packetReceiverList[protocolId];
                AssertionUtils.notNull(protocolRegistration, "协议类[class:{}][protocolId:{}]没有注册", packetClazz.getSimpleName(), protocolId);

                var receiverField = ReflectionUtils.getFieldByNameInPOJOClass(protocolRegistration.getClass(), "receiver");
                ReflectionUtils.makeAccessible(receiverField);
                ReflectionUtils.setField(receiverField, protocolRegistration, enhanceReceiverDefinition);
            } catch (Throwable t) {
                throw new RunException(t, "解析协议类[class:{}]未知异常", packetClazz.getSimpleName());
            }
        }
    }

}
