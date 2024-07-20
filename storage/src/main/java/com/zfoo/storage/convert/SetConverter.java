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

package com.zfoo.storage.convert;

import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

/**
 * @author liqi
 */
public class SetConverter implements ConditionalGenericConverter {

    public static final SetConverter INSTANCE = new SetConverter();

    private SetConverter() {
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.getType() == String.class && Set.class.isAssignableFrom(targetType.getType());
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Set.class));
    }

    @Override
    public Object convert(Object source, @NonNull TypeDescriptor sourceType, TypeDescriptor targetType) {
        var content = StringUtils.trim((String) source);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptySet();
        }
        Class<?> clazz = null;
        Type type = targetType.getResolvableType().getGeneric(0).getType();
        if (type instanceof ParameterizedType parameterizedType) {
            clazz = (Class<?>) parameterizedType.getRawType();
        } else {
            clazz = (Class<?>) type;
        }
        if (content.startsWith("[") && content.endsWith("]")) {
            try {
                return Collections.unmodifiableSet(JsonUtils.string2Set(content, clazz));
            } catch (Exception e) {
            }
        }
        return ConvertUtils.convertToSet(content, clazz);
    }
}
