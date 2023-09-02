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

package com.zfoo.orm.model;

import com.zfoo.orm.anno.IndexText;
import com.zfoo.protocol.util.AssertionUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author godotg
 * @version 3.0
 */
public class IndexTextDef {

    private Field field;

    public IndexTextDef(Field field, IndexText indexText) {
        AssertionUtils.notNull(field);

        // 是否被private修饰
        if (!Modifier.isPrivate(field.getModifiers())) {
            throw new IllegalArgumentException(StringUtils.format("[{}]没有被private修饰", field.getName()));
        }

        // 唯一索引不能有set方法，为了避免客户端改变javabean中的属性
        Class<?> clazz = field.getDeclaringClass();
        AssertionUtils.notNull(clazz);
        this.field = field;
        ReflectionUtils.makeAccessible(field);
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

}
