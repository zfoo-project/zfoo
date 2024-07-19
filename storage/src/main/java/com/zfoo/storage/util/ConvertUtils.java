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
package com.zfoo.storage.util;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.strategy.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author godotg
 */
public abstract class ConvertUtils {

    private static final TypeDescriptor TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    private static final ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();

    static {
        var converters = new HashSet<>();
        converters.add(new ArrayConverter());
        converters.add(new ListConverter());
        converters.add(new JsonToMapConverter());
        converters.add(new JsonToObjectConverter());
        converters.add(new StringToClassConverter());
        converters.add(new StringToDateConverter());
        converters.add(new StringToMapConverter());
        conversionServiceFactoryBean.setConverters(converters);
        conversionServiceFactoryBean.afterPropertiesSet();
    }


    public static <T> T convert(String content, Class<T> targetType) {
        return conversionServiceFactoryBean.getObject().convert(content, targetType);
    }

    public static Object convertField(String content, Field field) {
        var targetType = new TypeDescriptor(field);
        return conversionServiceFactoryBean.getObject().convert(content, TYPE_DESCRIPTOR, targetType);
    }

    public static Object convertToArray(String content, Class<?> componentType) {
        content = StringUtils.trim(content);
        // null safe，content为空则返回长度为0的数组
        if (StringUtils.isEmpty(content)) {
            return Array.newInstance(componentType, 0);
        }
        // 用普通的逗号分隔符解析
        var splits = content.split(StringUtils.COMMA_REGEX);
        var length = splits.length;
        Object array = Array.newInstance(componentType, length);
        for (var i = 0; i < length; i++) {
            Object value = ConvertUtils.convert(StringUtils.trim(splits[i]), componentType);
            Array.set(array, i, value);
        }
        return array;
    }

    public static <T> List<T> convertToList(String content, Class<T> type) {
        content = StringUtils.trim(content);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        var splits = content.split(StringUtils.COMMA_REGEX);
        var length = splits.length;
        var list = new ArrayList<T>();
        for (var i = 0; i < length; i++) {
            var value = ConvertUtils.convert(StringUtils.trim(splits[i]), type);
            list.add(value);
        }
        return Collections.unmodifiableList(list);
    }

    public static <T> Set<T> convertToSet(String content, Class<T> type) {
        content = StringUtils.trim(content);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptySet();
        }
        var splits = content.split(StringUtils.COMMA_REGEX);
        var length = splits.length;
        var set = new HashSet<T>();
        for (var i = 0; i < length; i++) {
            var value = ConvertUtils.convert(StringUtils.trim(splits[i]), type);
            set.add(value);
        }
        return Collections.unmodifiableSet(set);
    }
}
