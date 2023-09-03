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

package com.zfoo.net.router.receiver;

import com.zfoo.net.packet.IPacket;
import com.zfoo.net.router.attachment.IAttachment;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * EN:Dynamic proxy methods annotated by PacketReceiver annotations, to avoid reflection, will eventually use javassist bytecode enhanced methods to proxy this class
 * CN:动态代理被PacketReceiver注解标注的方法，为了避免反射最终会用javassist字节码增强的方法去代理PacketReceiverDefinition
 *
 * @author godotg
 * @version 3.0
 */
public class PacketReceiverDefinition implements IPacketReceiver {

    /**
     * A controller bean
     */
    private Object bean;

    /**
     * Methods annotated by PacketReceiver annotations, eg: public void atTcpHelloRequest(Session session, TcpHelloRequest request)
     */
    private Method method;

    /**
     * The protocol class that receives the package, eg: TcpHelloRequest
     */
    private Class<?> packetClazz;

    /**
     * attachment class, eg: GatewayAttachment
     */
    private Class<?> attachmentClazz;

    public PacketReceiverDefinition(Object bean, Method method, Class<?> packetClazz, Class<?> attachmentClazz) {
        this.bean = bean;
        this.method = method;
        this.packetClazz = packetClazz;
        this.attachmentClazz = attachmentClazz;
        ReflectionUtils.makeAccessible(method);
    }

    @Override
    public void invoke(Session session, IPacket packet, IAttachment attachment) {
        if (attachmentClazz == null) {
            ReflectionUtils.invokeMethod(bean, method, session, packet);
        } else {
            ReflectionUtils.invokeMethod(bean, method, session, packet, attachment);
        }
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getPacketClazz() {
        return packetClazz;
    }

    public void setPacketClazz(Class<?> packetClazz) {
        this.packetClazz = packetClazz;
    }

    public Class<?> getAttachmentClazz() {
        return attachmentClazz;
    }

    public void setAttachmentClazz(Class<?> attachmentClazz) {
        this.attachmentClazz = attachmentClazz;
    }

}
