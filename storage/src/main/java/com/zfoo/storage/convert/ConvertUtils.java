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

import com.zfoo.protocol.util.StringUtils;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author godotg
 */
public abstract class ConvertUtils {

    private static final TypeDescriptor TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    private static ConversionService conversionService = customConvert(Collections.emptySet());


    public static ConversionService customConvert(Set<?> customConverts) {
        var converters = new HashSet<>();
        // default convert
        converters.add(ArrayConverter.INSTANCE);
        converters.add(ListConverter.INSTANCE);
        converters.add(SetConverter.INSTANCE);
        converters.add(JsonToMapConverter.INSTANCE);
        converters.add(JsonToObjectConverter.INSTANCE);
        converters.add(StringToClassConverter.INSTANCE);
        converters.add(StringToDateConverter.INSTANCE);
        // custom convert
        converters.addAll(customConverts);

        ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();
        conversionServiceFactoryBean.setConverters(converters);
        conversionServiceFactoryBean.afterPropertiesSet();
        return conversionServiceFactoryBean.getObject();
    }


    public static <T> T convert(String content, Class<T> targetType) {
        return conversionService.convert(content, targetType);
    }

    public static Object convertField(String content, Field field) {
        var targetType = new TypeDescriptor(field);
        return conversionService.convert(content, TYPE_DESCRIPTOR, targetType);
    }

    public static Object convertToArray(String content, Class<?> componentType) {
        content = StringUtils.trim(content);
        // null safe，content为空则返回长度为0的数组
        if (StringUtils.isEmpty(content)) {
            return Array.newInstance(componentType, 0);
        }
        // 用普通的逗号分隔符解析
        var list = convertToList(content, componentType);
        Object array = Array.newInstance(componentType, list.size());
        for (var i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return array;
    }

    public static <T> List<T> convertToList(String content, Class<T> genericType) {
        content = StringUtils.trim(content);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptyList();
        }
        var splits = content.split(StringUtils.COMMA_REGEX);
        var list = new ArrayList<T>();
        for (var split : splits) {
            var value = convert(StringUtils.trim(split), genericType);
            list.add(value);
        }
        return Collections.unmodifiableList(list);
    }

    public static <T> Set<T> convertToSet(String content, Class<T> genericType) {
        content = StringUtils.trim(content);
        if (StringUtils.isEmpty(content)) {
            return Collections.emptySet();
        }
        return Set.copyOf(convertToList(content, genericType));
    }
}
