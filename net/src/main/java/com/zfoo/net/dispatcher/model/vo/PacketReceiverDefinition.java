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

package com.zfoo.net.dispatcher.model.vo;

import com.zfoo.net.packet.model.IPacketAttachment;
import com.zfoo.net.session.model.Session;
import com.zfoo.protocol.IPacket;
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public class PacketReceiverDefinition implements IPacketReceiver {

    /**
     * 一个facade的bean，这个bean里有void methodName(Session session,CM_Int cm)接受的方法
     */
    private Object bean;

    /**
     * 接受的方法void methodName(Session session,CM_Int cm)
     */
    private Method method;

    /**
     * 接收的包的Class类，如CM_Int
     */
    private Class<?> packetClazz;

    /**
     * 接收的包的附加包的Class类，如GatewayPacketAttachment
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
    public void invoke(Session session, IPacket packet, IPacketAttachment attachment) {
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
