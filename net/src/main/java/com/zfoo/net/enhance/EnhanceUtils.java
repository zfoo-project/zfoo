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

package com.zfoo.net.enhance;

import com.zfoo.net.anno.Task;
import com.zfoo.net.session.Session;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 */
public abstract class EnhanceUtils {

    static {
        var classArray = new Class<?>[]{
                IPacketReceiver.class,
                Session.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
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

        var enhanceClazz = classPool.makeClass(EnhanceUtils.class.getName() + "Route" + UuidUtils.getLocalIntId());
        enhanceClazz.addInterface(classPool.get(IPacketReceiver.class.getName()));

        var field = new CtField(classPool.get(bean.getClass().getName()), "bean", enhanceClazz);
        field.setModifiers(Modifier.PRIVATE);
        enhanceClazz.addField(field);

        var constructor = new CtConstructor(classPool.get(new String[]{bean.getClass().getName()}), enhanceClazz);
        constructor.setBody("{this.bean=$1;}");
        constructor.setModifiers(Modifier.PUBLIC);
        enhanceClazz.addConstructor(constructor);

        var invokeMethod = new CtMethod(classPool.get(void.class.getName()), "invoke", classPool.get(new String[]{Session.class.getName(), Object.class.getName(), Object.class.getName()}), enhanceClazz);
        invokeMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        if (attachmentClazz == null) {
            // Cast type(强制类型转换)
            String invokeMethodBody = StringUtils.format("{this.bean.{}($1, ({})$2);}", method.getName(), packetClazz.getName());
            invokeMethod.setBody(invokeMethodBody);
        } else {
            String invokeMethodBody = StringUtils.format("{this.bean.{}($1, ({})$2, ({})$3);}", method.getName(), packetClazz.getName(), attachmentClazz.getName());
            invokeMethod.setBody(invokeMethodBody);
        }
        enhanceClazz.addMethod(invokeMethod);

        // 定义类实现的接口方法task
        CtMethod taskMethod = new CtMethod(classPool.get(Task.class.getName()), "task", null, enhanceClazz);
        taskMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String taskMethodBody = StringUtils.format("{ return {}.{}; }", Task.class.getName(), definition.getTask());
        taskMethod.setBody(taskMethodBody);
        enhanceClazz.addMethod(taskMethod);

        // 定义类实现的接口方法attachment
        CtMethod attachmentMethod = new CtMethod(classPool.get(Class.class.getName()), "attachment", null, enhanceClazz);
        attachmentMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        if (attachmentClazz == null) {
            String attachmentMethodBody = "{ return null; }";
            attachmentMethod.setBody(attachmentMethodBody);
        } else {
            String attachmentMethodBody = StringUtils.format("{ return {}.class; }", attachmentClazz.getName());
            attachmentMethod.setBody(attachmentMethodBody);
        }
        enhanceClazz.addMethod(attachmentMethod);

        enhanceClazz.detach();

        var resultClazz = enhanceClazz.toClass(IPacketReceiver.class);
        var resultConstructor = resultClazz.getConstructor(bean.getClass());
        var receiver = (IPacketReceiver) resultConstructor.newInstance(bean);
        return receiver;
    }
}
