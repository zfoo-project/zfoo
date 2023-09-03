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
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class EnhanceUtils {

    static {
        var classArray = new Class<?>[]{
                IPacket.class,
                IAttachment.class,
                IPacketReceiver.class,
                Session.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getCanonicalName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }

    public static IPacketReceiver createPacketReceiver(PacketReceiverDefinition definition) throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var classPool = ClassPool.getDefault();
        var bean = definition.getBean();
        var method = definition.getMethod();
        var packetClazz = definition.getPacketClazz();
        var attachmentClazz = definition.getAttachmentClazz();

        var enhanceClazz = classPool.makeClass(EnhanceUtils.class.getCanonicalName() + "Dispatcher" + UuidUtils.getLocalIntId());
        enhanceClazz.addInterface(classPool.get(IPacketReceiver.class.getCanonicalName()));

        var field = new CtField(classPool.get(bean.getClass().getCanonicalName()), "bean", enhanceClazz);
        field.setModifiers(Modifier.PRIVATE);
        enhanceClazz.addField(field);

        var constructor = new CtConstructor(classPool.get(new String[]{bean.getClass().getCanonicalName()}), enhanceClazz);
        constructor.setBody("{this.bean=$1;}");
        constructor.setModifiers(Modifier.PUBLIC);
        enhanceClazz.addConstructor(constructor);

        var invokeMethod = new CtMethod(classPool.get(void.class.getCanonicalName()), "invoke", classPool.get(new String[]{Session.class.getCanonicalName(), IPacket.class.getCanonicalName(), IAttachment.class.getCanonicalName()}), enhanceClazz);
        invokeMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        if (attachmentClazz == null) {
            // Cast type(强制类型转换)
            String invokeMethodBody = StringUtils.format("{this.bean.{}($1, ({})$2);}", method.getName(), packetClazz.getCanonicalName());
            invokeMethod.setBody(invokeMethodBody);
        } else {
            String invokeMethodBody = StringUtils.format("{this.bean.{}($1, ({})$2, ({})$3);}", method.getName(), packetClazz.getCanonicalName(), attachmentClazz.getCanonicalName());
            invokeMethod.setBody(invokeMethodBody);
        }
        enhanceClazz.addMethod(invokeMethod);

        enhanceClazz.detach();

        var resultClazz = enhanceClazz.toClass(IPacketReceiver.class);
        var resultConstructor = resultClazz.getConstructor(bean.getClass());
        var receiver = (IPacketReceiver) resultConstructor.newInstance(bean);
        return receiver;
    }
}
