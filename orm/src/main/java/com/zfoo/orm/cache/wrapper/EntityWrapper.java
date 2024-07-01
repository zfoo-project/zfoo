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

package com.zfoo.orm.cache.wrapper;

import com.zfoo.orm.anno.Id;
import com.zfoo.orm.anno.Version;
import com.zfoo.orm.model.IEntity;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.util.FieldUtils;
import com.zfoo.protocol.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author godotg
 */
public class EntityWrapper implements IEntityWrapper {

    private Class<? extends IEntity<?>> entityClass;
    private Field idField;
    private Field versionField;
    private Method setIdMethod;
    private Method getVersionMethod;
    private Method setVersionMethod;

    public EntityWrapper(Class<? extends IEntity<?>> entityClass) {
        this.entityClass = entityClass;

        var idFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(entityClass, Id.class);
        ReflectionUtils.makeAccessible(idFields[0]);
        this.idField = idFields[0];
        this.setIdMethod = ReflectionUtils.getMethodByNameInPOJOClass(entityClass, FieldUtils.fieldToSetMethod(entityClass, idField), idField.getType());

        var versionFields = ReflectionUtils.getFieldsByAnnoInPOJOClass(entityClass, Version.class);
        if (ArrayUtils.isNotEmpty(versionFields)) {
            this.versionField = versionFields[0];
            var getMethod = ReflectionUtils.getMethodByNameInPOJOClass(entityClass, FieldUtils.fieldToGetMethod(entityClass, versionField));
            var setMethod = ReflectionUtils.getMethodByNameInPOJOClass(entityClass, FieldUtils.fieldToSetMethod(entityClass, versionField), versionField.getType());
            this.getVersionMethod = getMethod;
            this.setVersionMethod = setMethod;
        }
    }

    @Override
    public IEntity<?> newEntity(Object id) {
        var entity = ReflectionUtils.newInstance(entityClass);
        ReflectionUtils.invokeMethod(entity, setIdMethod, id);
        return entity;
    }

    @Override
    public String versionFieldName() {
        return idField.getName();
    }

    @Override
    public long gvs(IEntity<?> entity) {
        if (getVersionMethod == null) {
            return 0;
        }
        return (long) ReflectionUtils.invokeMethod(entity, getVersionMethod);
    }

    @Override
    public void svs(IEntity<?> entity, long vs) {
        if (setVersionMethod == null) {
            return;
        }
        ReflectionUtils.invokeMethod(entity, setVersionMethod, vs);
    }

    public Class<? extends IEntity<?>> getEntityClass() {
        return entityClass;
    }

    public Field getIdField() {
        return idField;
    }

    public Field getVersionField() {
        return versionField;
    }

    public Method getSetIdMethod() {
        return setIdMethod;
    }

    public Method getGetVersionMethod() {
        return getVersionMethod;
    }

    public Method getSetVersionMethod() {
        return setVersionMethod;
    }
}
