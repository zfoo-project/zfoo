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

import com.zfoo.net.packet.IPacket;
import com.zfoo.net.packet.PacketService;
import com.zfoo.net.router.attachment.GatewayAttachment;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.router.attachment.SignalAttachment;
import com.zfoo.net.router.receiver.EnhanceUtils;
import com.zfoo.net.router.receiver.IPacketReceiver;
import com.zfoo.net.anno.PacketReceiver;
import com.zfoo.net.router.receiver.PacketReceiverDefinition;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.GraalVmUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import io.netty.util.collection.ShortObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;

/**
 * EN:The receiving route of the packet, the server/client receives the packet and then call corresponding to the Receiver
 * CN:包的接收路线，服务器收到packet然后调用对应的Receiver
 *
 * @author godotg
 * @version 3.0
 */
public abstract class PacketBus {

    private static final Logger logger = LoggerFactory.getLogger(PacketBus.class);

    private static final ShortObjectHashMap<IPacketReceiver> receiverMap = new ShortObjectHashMap<>();

    /**
     * The routing of the message
     */
    public static void route(Session session, IPacket packet, IAttachment attachment) {
        var receiver = receiverMap.get(ProtocolManager.protocolId(packet.getClass()));
        if (receiver == null) {
            var name = packet.getClass().getSimpleName();
            throw new RuntimeException(StringUtils.format("no any packetReceiver:[at{}] found for this packet:[{}] or no GatewayAttachment sent back if this server is gateway", name, name));
        }
        receiver.invoke(session, packet, attachment);
    }


    public static void registerPacketReceiverDefinition(Object bean) {
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

            AssertionUtils.isTrue(IPacket.class.isAssignableFrom(paramClazzs[1]), "[class:{}] [method:{}],the second parameter must be IPacket type parameter Exception.", bean.getClass().getName(), method.getName());

            AssertionUtils.isTrue(paramClazzs.length != 3 || IAttachment.class.isAssignableFrom(paramClazzs[2]), "[class:{}] [method:{}],the third parameter must be IAttachment type parameter Exception.", bean.getClass().getName(), method.getName());

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
            if (attachmentClazz != null) {
                if (packetName.endsWith(PacketService.NET_REQUEST_SUFFIX)) {
                    AssertionUtils.isTrue(!attachmentClazz.equals(SignalAttachment.class), "[class:{}] [method:{}] [packet:{}] must use [attachment:{}]!"
                            , bean.getClass().getName(), methodName, packetName, GatewayAttachment.class.getCanonicalName());
                } else if (packetName.endsWith(PacketService.NET_ASK_SUFFIX)) {
                    AssertionUtils.isTrue(!attachmentClazz.equals(GatewayAttachment.class), "[class:{}] [method:{}] [packet:{}] can not match with [attachment:{}]!", bean.getClass().getName(), methodName, packetName, GatewayAttachment.class.getCanonicalName());
                }
            }

            var protocolId = Short.MIN_VALUE;
            try {
                protocolId = ProtocolManager.protocolId(packetClazz);
            } catch (Exception e) {
                throw new RunException("[class:{}][protocolId:{}] has no registration, please register for this protocol", packetClazz.getSimpleName(), protocolId);
            }

            try {
                AssertionUtils.isNull(receiverMap.get(protocolId), "duplicate protocol registration, @PacketReceiver [class:{}] is repeatedly received [at{}]", packetClazz.getSimpleName(), packetClazz.getSimpleName());

                var receiverDefinition = new PacketReceiverDefinition(bean, method, packetClazz, attachmentClazz);
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
