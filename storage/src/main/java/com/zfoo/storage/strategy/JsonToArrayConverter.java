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
import com.zfoo.storage.util.ConvertUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import java.lang.reflect.Array;
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
        if (content.startsWith("[") || content.endsWith("]")) {
            return JsonUtils.string2Object(content, targetType.getType());
        }
        // 用普通的逗号分隔符解析
        var splits = content.split(StringUtils.COMMA_REGEX);
        var length = splits.length;
        Object array = Array.newInstance(componentType, length);
        for (var i = 0; i < length; i++) {
            Object value = ConvertUtils.convert(splits[i], componentType);
            Array.set(array, i, value);
        }
        return array;
    }
}
