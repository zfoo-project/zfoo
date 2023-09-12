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

package com.zfoo.scheduler.enhance;

import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 动态代理被Scheduler注解标注的方法，为了避免反射最终会用javassist字节码增强的方法去代理ReflectScheduler
 *
 * @author godotg
 */
public class ReflectScheduler implements IScheduler {

    private Object bean;

    private Method method;

    public static ReflectScheduler valueOf(Object bean, Method method) {
        var scheduler = new ReflectScheduler();
        scheduler.bean = bean;
        scheduler.method = method;
        return scheduler;
    }

    @Override
    public void invoke() {
        ReflectionUtils.invokeMethod(bean, method);
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }
}
