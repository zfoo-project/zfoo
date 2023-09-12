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

package com.zfoo.storage.strategy;

import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.util.Collections;
import java.util.Set;

/**
 * @author godotg
 */
public class JsonToArrayConverter implements ConditionalGenericConverter {


    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.getType() == String.class && targetType.getType().isArray();
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Object[].class));
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        // String content = (String) source;
        // return targetType.isPrimitive() ? JsonUtil.string2Object(content, targetType.getObjectType())
        //         : JsonUtil.string2Array(content, targetType.getType());
        Class<?> clazz = null;

        String content = (String) source;

        String targetClazzName = targetType.getObjectType().getName();
        if (targetClazzName.contains(StringUtils.LEFT_SQUARE_BRACKET) || targetClazzName.contains(StringUtils.SEMICOLON)) {
            String clazzPath = targetClazzName.substring(2, targetClazzName.length() - 1);
            try {
                clazz = Class.forName(clazzPath);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            clazz = targetType.getObjectType();
        }

        return JsonUtils.string2Array(content, clazz);
    }
}
