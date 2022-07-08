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

package com.zfoo.storage.model.vo;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.storage.model.anno.Id;

import java.lang.reflect.Field;

/**
 * @author godotg
 * @version 4.0
 */
public class IdDef {

    private Field field;

    public static IdDef valueOf(Class<?> clazz) {
        var fields = ReflectionUtils.getFieldsByAnnoInPOJOClass(clazz, Id.class);
        if (fields.length <= 0) {
            throw new RunException("class[{}]没有被Id注解标识的主键（如果确实已经被Id注解标注，注意不要使用ORM的Id注解）", clazz.getName());
        }
        if (fields.length > 1) {
            throw new RunException("类[{}]的主键Id注解重复", clazz.getName());
        }
        if (fields[0] == null) {
            throw new RunException("不合法的Id资源映射对象：" + clazz.getName());
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
