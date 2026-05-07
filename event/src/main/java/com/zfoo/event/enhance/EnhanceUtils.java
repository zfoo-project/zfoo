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
 */

package com.zfoo.event.enhance;

import com.zfoo.event.anno.Bus;
import com.zfoo.event.model.IEvent;
import com.zfoo.event.schema.NamespaceHandler;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 */
public abstract class EnhanceUtils {

    static {
        // Adapt to Tomcat: Tomcat uses a custom class loader, while Javassist uses the default class loader
        var classArray = new Class<?>[]{
                IEventReceiver.class,
                IEvent.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }

    public static IEventReceiver createEventReceiver(EventReceiverDefinition definition) throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var classPool = ClassPool.getDefault();

        Object bean = definition.getBean();
        Method method = definition.getMethod();
        Class<?> clazz = definition.getEventClazz();

        // Define the class name
        CtClass enhanceClazz = classPool.makeClass(EnhanceUtils.class.getName() + StringUtils.capitalize(NamespaceHandler.EVENT) + UuidUtils.getLocalIntId());
        enhanceClazz.addInterface(classPool.get(IEventReceiver.class.getName()));

        // Define a member field 'bean' in the class
        CtClass beanClass = classPool.get(bean.getClass().getName());
        CtField field = new CtField(beanClass, "bean", enhanceClazz);
        field.setModifiers(Modifier.PRIVATE + Modifier.FINAL);
        enhanceClazz.addField(field);

        // Define the class constructor
        // Create constructor parameter array
        CtClass[] parameterTypes = {beanClass};
        CtConstructor constructor = new CtConstructor(parameterTypes, enhanceClazz);
        constructor.setBody("{this.bean=$1;}");
        constructor.setModifiers(Modifier.PUBLIC);
        enhanceClazz.addConstructor(constructor);

        // Define the 'invoke' method from the interface
        CtMethod invokeMethod = new CtMethod(classPool.get(void.class.getName()), "invoke", classPool.get(new String[]{IEvent.class.getName()}), enhanceClazz);
        invokeMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String invokeMethodBody = StringUtils.format("{ this.bean.{}(({})$1); }", method.getName(), clazz.getName()); // Cast to the specific Event type
        invokeMethod.setBody(invokeMethodBody);
        enhanceClazz.addMethod(invokeMethod);

        // Define the 'bus' method from the interface
        CtMethod busMethod = new CtMethod(classPool.get(Bus.class.getName()), "bus", null, enhanceClazz);
        busMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String busMethodBody = StringUtils.format("{ return {}.{}; }", Bus.class.getName(), definition.getBus());
        busMethod.setBody(busMethodBody);
        enhanceClazz.addMethod(busMethod);

        // Define the 'getBean' method from the interface
        CtMethod beanMethod = new CtMethod(classPool.get(Object.class.getName()), "getBean", null, enhanceClazz);
        beanMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String beanMethodBody = "{ return this.bean; }";
        beanMethod.setBody(beanMethodBody);
        enhanceClazz.addMethod(beanMethod);

        // Detach from pool to release cached class definition
        enhanceClazz.detach();

        Class<?> resultClazz = enhanceClazz.toClass(IEventReceiver.class);
        Constructor<?> resultConstructor = resultClazz.getConstructor(bean.getClass());
        return (IEventReceiver) resultConstructor.newInstance(bean);
    }
}
