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

package com.zfoo.orm.cache.version;

import com.zfoo.orm.model.IEntity;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.UuidUtils;
import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 */
public abstract class EnhanceUtils {

    static {
        // 适配Tomcat，因为Tomcat不是用的默认的类加载器，而Javassist用的是默认的加载器
        var classArray = new Class<?>[]{
                IVersion.class
        };

        var classPool = ClassPool.getDefault();

        for (var clazz : classArray) {
            if (classPool.find(clazz.getName()) == null) {
                ClassClassPath classPath = new ClassClassPath(clazz);
                classPool.insertClassPath(classPath);
            }
        }
    }

    public static IVersion createVersion(VersionReflect cacheVersion) throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        var classPool = ClassPool.getDefault();

        Class<?> clazz = cacheVersion.getEntityClass();
        Method getVersionMethod = cacheVersion.getGetVersionMethod();
        Method setVersionMethod = cacheVersion.getSetVersionMethod();

        // 定义类名称
        CtClass enhanceClazz = classPool.makeClass(EnhanceUtils.class.getName() + UuidUtils.getLocalIntId());
        enhanceClazz.addInterface(classPool.get(IVersion.class.getName()));

        // 定义类实现的接口方法name
        CtMethod nameMethod = new CtMethod(classPool.get(String.class.getName()), "name", null, enhanceClazz);
        nameMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String nameMethodBody = StringUtils.format("{ return \"{}\"; }", cacheVersion.name());
        nameMethod.setBody(nameMethodBody);
        enhanceClazz.addMethod(nameMethod);

        // 定义类实现的接口方法gvs
        CtMethod gvsMethod = new CtMethod(classPool.get(long.class.getName()), "gvs", classPool.get(new String[]{IEntity.class.getName()}), enhanceClazz);
        gvsMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String gvsMethodBody = StringUtils.format("{ return (({})$1).{}(); }", clazz.getName(), getVersionMethod.getName());
        gvsMethod.setBody(gvsMethodBody);
        enhanceClazz.addMethod(gvsMethod);

        // 定义类实现的接口方法svs
        CtMethod svsMethod = new CtMethod(classPool.get(void.class.getName()), "svs", classPool.get(new String[]{IEntity.class.getName(), long.class.getName()}), enhanceClazz);
        svsMethod.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
        String svsMethodBody = StringUtils.format("{ (({})$1).{}($2); }", clazz.getName(), setVersionMethod.getName());
        svsMethod.setBody(svsMethodBody);
        enhanceClazz.addMethod(svsMethod);

        // 释放缓存
        enhanceClazz.detach();

        Class<?> resultClazz = enhanceClazz.toClass(IVersion.class);
        return (IVersion) resultClazz.newInstance();
    }
}
