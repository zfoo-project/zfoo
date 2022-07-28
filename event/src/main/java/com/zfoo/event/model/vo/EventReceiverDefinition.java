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

package com.zfoo.event.model.vo;

import com.zfoo.event.model.event.IEvent;
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 动态代理被EventReceiver注解标注的方法，为了避免反射最终会用javassist字节码增强的方法去代理EventReceiverDefinition
 *
 * @author godotg
 * @version 3.0
 */
public class EventReceiverDefinition implements IEventReceiver {


    private Object bean;

    // 被EventReceiver注解标注的方法
    private Method method;

    // 接收的参数Class
    private Class<? extends IEvent> eventClazz;

    public EventReceiverDefinition(Object bean, Method method, Class<? extends IEvent> eventClazz) {
        this.bean = bean;
        this.method = method;
        this.eventClazz = eventClazz;
        ReflectionUtils.makeAccessible(this.method);
    }

    @Override
    public void invoke(IEvent event) {
        ReflectionUtils.invokeMethod(bean, method, event);
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

    public Class<? extends IEvent> getEventClazz() {
        return eventClazz;
    }

    public void setEventClazz(Class<? extends IEvent> eventClazz) {
        this.eventClazz = eventClazz;
    }
}
