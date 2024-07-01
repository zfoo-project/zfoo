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
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * @author godotg
 */
public class VersionReflect implements IVersion {

    private Class<? extends IEntity<?>> entityClass;
    private Method getVersionMethod;
    private Method setVersionMethod;
    private String versionFiled;

    public VersionReflect(Class<? extends IEntity<?>> entityClass, Method getVersionMethod, Method setVersionMethod, String versionFiled) {
        this.entityClass = entityClass;
        this.getVersionMethod = getVersionMethod;
        this.setVersionMethod = setVersionMethod;
        this.versionFiled = versionFiled;
    }

    @Override
    public String name() {
        return versionFiled;
    }

    @Override
    public long gvs(IEntity<?> entity) {
        return (long) ReflectionUtils.invokeMethod(entity, getVersionMethod);
    }

    @Override
    public void svs(IEntity<?> entity, long vs) {
        ReflectionUtils.invokeMethod(entity, setVersionMethod, vs);
    }


    public Class<? extends IEntity<?>> getEntityClass() {
        return entityClass;
    }

    public Method getGetVersionMethod() {
        return getVersionMethod;
    }

    public Method getSetVersionMethod() {
        return setVersionMethod;
    }

    public String getVersionFiled() {
        return versionFiled;
    }
}
