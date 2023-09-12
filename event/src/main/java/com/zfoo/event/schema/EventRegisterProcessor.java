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

package com.zfoo.event.schema;

import com.zfoo.event.anno.EventReceiver;
import com.zfoo.event.enhance.EnhanceUtils;
import com.zfoo.event.enhance.EventReceiverDefinition;
import com.zfoo.event.manager.EventBus;
import com.zfoo.event.model.IEvent;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.GraalVmUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Modifier;

/**
 * 这是一个后置处理器，在boot项目中注册EventContext时，会import导入EventRegisterProcessor这个组件，这是一个后置处理器，
 * 断点发现 在AbstractAutowireCapableBeanFactory或调用getBeanPostProcessors，这样子每一个Bean创建后都会走postProcessAfterInitialization这个方法
 *
 * @author godotg
 */
public class EventRegisterProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EventRegisterProcessor.class);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        var clazz = bean.getClass();
        var methods = ReflectionUtils.getMethodsByAnnoInPOJOClass(clazz, EventReceiver.class);
        if (ArrayUtils.isEmpty(methods)) {
            return bean;
        }

        if (!ReflectionUtils.isPojoClass(clazz)) {
            logger.warn("The message registration class [{}] is not a POJO class, and the parent class will not be scanned", clazz);
        }

        try {
            for (var method : methods) {
                var paramClazzs = method.getParameterTypes();
                if (paramClazzs.length != 1) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must have one parameter!", bean.getClass().getName(), method.getName()));
                }
                if (!IEvent.class.isAssignableFrom(paramClazzs[0])) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] must have one [IEvent] type parameter!", bean.getClass().getName(), method.getName()));
                }
                @SuppressWarnings("unchecked")
                var eventClazz = (Class<? extends IEvent>) paramClazzs[0];
                var eventName = eventClazz.getCanonicalName();
                var methodName = method.getName();

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] must use 'public' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] can not use 'static' as modifier!", bean.getClass().getName(), methodName, eventName));
                }

                var expectedMethodName = StringUtils.format("on{}", eventClazz.getSimpleName());
                if (!methodName.equals(expectedMethodName)) {
                    throw new IllegalArgumentException(StringUtils.format("[class:{}] [method:{}] [event:{}] expects '{}' as method name!"
                            , bean.getClass().getName(), methodName, eventName, expectedMethodName));
                }

                var bus = method.getDeclaredAnnotation(EventReceiver.class).value();
                var receiverDefinition = new EventReceiverDefinition(bean, method, bus, eventClazz);
                if (GraalVmUtils.isGraalVM()) {
                    EventBus.registerEventReceiver(eventClazz, receiverDefinition);
                } else {
                    // key:class类型 value:观察者 注册Event的receiverMap中
                    EventBus.registerEventReceiver(eventClazz, EnhanceUtils.createEventReceiver(receiverDefinition));
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return bean;
    }

}
