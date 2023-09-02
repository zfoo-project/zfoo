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

package com.zfoo.storage.model;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.storage.anno.Id;

import java.lang.reflect.Field;

/**
 * @author godotg
 * @version 3.0
 */
public class IdDef {

    private Field field;

    public static IdDef valueOf(Class<?> clazz) {
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        if (fields.length <= 0) {
            throw new RunException("There is no a primary key identified by the Id annotation in class[{}](if it has indeed been annotated by the Id annotation, be careful not to use the ORM Id annotation)", clazz.getName());
        }
        if (fields.length > 1) {
            throw new RunException("The primary key Id annotation of class [{}] is duplicated", clazz.getName());
        }
        if (fields[0] == null) {
            throw new RunException("Illegal Id resource mapping object:" + clazz.getName());
        }
        var idField = fields[0];
        ReflectionUtils.makeAccessible(idField);
        var idDef = new IdDef();
        idDef.setField(idField);
        return idDef;
    }


    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
}
