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

import java.lang.reflect.Array;
import java.util.Set;

/**
 * @author godotg
 */
public class ArrayConverter implements ConditionalGenericConverter {


    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return sourceType.getType() == String.class && targetType.isArray();
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return null;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        var content = StringUtils.trim((String) source);
        var componentType = targetType.getType().getComponentType();
        // null safe，content为空则返回长度为0的数组
        if (StringUtils.isEmpty(content)) {
            return Array.newInstance(componentType, 0);
        }
        // 如果为json格式，则以json格式解析
        if (content.startsWith("[") && content.endsWith("]")) {
            try {
                return componentType.isPrimitive()
                        ? JsonUtils.string2Object(content, targetType.getObjectType())
                        : JsonUtils.string2Array(content, componentType);

            } catch (Exception e) {
            }
        }
        return ConvertUtils.convertToArray(content, componentType);
    }
}
