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
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Dynamic proxy for methods annotated with @EventReceiver. To avoid reflection overhead,
 * Javassist bytecode enhancement is used to proxy EventReceiverDefinition.
 *
 * @author godotg
 */
public class EventReceiverDefinition implements IEventReceiver {

    // The observer's bean instance
    private Object bean;

    // The method annotated with @EventReceiver
    private Method method;

    // The event dispatching strategy
    private Bus bus;

    // The event parameter class type
    private Class<? extends IEvent> eventClazz;

    public EventReceiverDefinition(Object bean, Method method, Bus bus, Class<? extends IEvent> eventClazz) {
        this.bean = bean;
        this.method = method;
        this.bus = bus;
        this.eventClazz = eventClazz;
        ReflectionUtils.makeAccessible(this.method);
    }

    @Override
    public Bus bus() {
        return bus;
    }

    @Override
    public void invoke(IEvent event) {
        ReflectionUtils.invokeMethod(bean, method, event);
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public Bus getBus() {
        return bus;
    }

    public Class<? extends IEvent> getEventClazz() {
        return eventClazz;
    }


}
