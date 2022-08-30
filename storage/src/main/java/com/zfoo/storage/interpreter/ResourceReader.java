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
package com.zfoo.storage.interpreter;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.anno.Id;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceEnum;
import com.zfoo.storage.strategy.*;
import com.zfoo.storage.util.CsvReaderUtils;
import com.zfoo.storage.util.ExcelReaderUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.TypeDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 4.0
 */
public class ResourceReader implements IResourceReader {

    private static final TypeDescriptor TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    private static final ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();

    static {
        var converters = new HashSet<>();
        converters.add(new JsonToArrayConverter());
        converters.add(new JsonToMapConverter());
        converters.add(new JsonToObjectConverter());
        converters.add(new StringToClassConverter());
        converters.add(new StringToDateConverter());
        converters.add(new StringToMapConverter());
        conversionServiceFactoryBean.setConverters(converters);
        conversionServiceFactoryBean.afterPropertiesSet();
    }

    @Override
    public <T> List<T> read(InputStream inputStream, Class<T> clazz, String suffix) throws IOException {
        ResourceData resource = null;
        var resourceEnum = ResourceEnum.getResourceEnumByType(suffix);
        if (resourceEnum == ResourceEnum.JSON) {
            resource = JsonUtils.string2Object(StringUtils.bytesToString(IOUtils.toByteArray(inputStream)), ResourceData.class);
        } else if (resourceEnum == ResourceEnum.EXCEL_XLS || resourceEnum == ResourceEnum.EXCEL_XLSX) {
            resource = ExcelReaderUtils.readResourceDataFromExcel(inputStream, clazz.getSimpleName());
        } else if (resourceEnum == ResourceEnum.CSV) {
            resource = CsvReaderUtils.readResourceDataFromCSV(inputStream, clazz.getSimpleName());
        } else {
            throw new RunException("不支持文件[{}]的配置类型[{}]", clazz.getSimpleName(), suffix);
        }

        var result = new ArrayList<T>();
        //获取所有字段
        var cellFieldMap = getFieldMap(resource, clazz);
        var fieldInfos = getFieldInfos(cellFieldMap, clazz);

        var iterator = resource.getRows().iterator();
        // 从ROW_SERVER这行开始读取数据
        while (iterator.hasNext()) {
            var columns = iterator.next();
            var instance = ReflectionUtils.newInstance(clazz);

            for (var fieldInfo : fieldInfos) {
                var content = columns.get(fieldInfo.index);
                if (StringUtils.isNotEmpty(content) || fieldInfo.field.getType() == String.class) {
                    inject(instance, fieldInfo.field, content);
                }
            }
            result.add(instance);
        }
        return result;
    }

    private void inject(Object instance, Field field, String content) {
        try {
            var targetType = new TypeDescriptor(field);
            var value = conversionServiceFactoryBean.getObject().convert(content, TYPE_DESCRIPTOR, targetType);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, instance, value);
        } catch (Exception e) {
            throw new RunException(e, "无法将Excel资源[class:{}]中的[content:{}]转换为属性[field:{}]", instance.getClass().getSimpleName(), content, field.getName());
        }
    }

    // 只读取代码里写的字段
    private Collection<FieldInfo> getFieldInfos(Map<String, Integer> fieldMap, Class<?> clazz) {
        var fieldList = Arrays.stream(clazz.getDeclaredFields())
                .filter(it -> !Modifier.isTransient(it.getModifiers()))
                .filter(it -> !Modifier.isStatic(it.getModifiers()))
                .collect(Collectors.toList());

        for (var field : fieldList) {
            if (!fieldMap.containsKey(field.getName())) {
                throw new RunException("资源类[class:{}]的声明属性[filed:{}]无法获取，请检查配置表的格式", clazz, field.getName());
            }

            if (field.isAnnotationPresent(Id.class)) {
                var cellIndex = fieldMap.get(field.getName());
                if (cellIndex != 0) {
                    throw new RunException("资源类[class:{}]的主键[Id:{}]必须放在Excel配置表的第一列，请检查配置表的格式", clazz, field.getName());
                }
            }

            if (Modifier.isPublic(field.getModifiers())) {
                throw new RunException("因为静态资源类是不能被修改的，所以资源类[class:{}]的属性[filed:{}]不能被public修饰，请改为private修饰", clazz, field.getName());
            }

            var setMethodName = StringUtils.EMPTY;
            try {
                setMethodName = ReflectionUtils.fieldToSetMethod(clazz, field);
            } catch (Exception e) {
                // 没有setMethod是正确的
            }
            if (StringUtils.isNotBlank(setMethodName)) {
                throw new RunException("因为静态资源类是不能被修改的，所以资源类[class:{}]的属性[filed:{}]不能含有set方法[{}]", clazz, field.getName(), setMethodName);
            }
        }

        return fieldList.stream().map(it -> new FieldInfo(fieldMap.get(it.getName()), it)).collect(Collectors.toList());
    }

    private Workbook createWorkbook(InputStream input, String name) {
        try {
            return WorkbookFactory.create(input);
        } catch (IOException e) {
            throw new RunException("静态资源[{}]异常，无法读取文件", name);
        }
    }

    private static class FieldInfo {
        public final int index;
        public final Field field;

        public FieldInfo(int index, Field field) {
            this.index = index;
            this.field = field;
        }
    }

    public Map<String, Integer> getFieldMap(ResourceData resource, Class<?> clazz) {
        var header = resource.getHeaders();
        if (header == null) {
            throw new RunException("无法获取资源[class:{}]的Excel文件的属性控制列", clazz.getSimpleName());
        }

        var cellFieldMap = new HashMap<String, Integer>();
        for (var i = 0; i < header.size(); i++) {
            var cell = header.get(i);
            if (Objects.isNull(cell)) {
                continue;
            }

            var name = cell.getName();
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            var previousValue = cellFieldMap.put(name, i);
            if (Objects.nonNull(previousValue)) {
                throw new RunException("资源[class:{}]的Excel文件出现重复的属性控制列[field:{}]", clazz.getSimpleName(), name);
            }
        }
        return cellFieldMap;
    }

}
