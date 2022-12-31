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
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.model.anno.ExcelFieldName;
import com.zfoo.storage.model.anno.Id;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceEnum;
import com.zfoo.storage.strategy.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.TypeDescriptor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 4.0
 */
public class ResourceInterpreter {

    private static final TypeDescriptor TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);

    private static final ConversionServiceFactoryBean conversionServiceFactoryBean = new ConversionServiceFactoryBean();

    static {
        var converters = new HashSet<>();
        converters.add(new JsonToArrayConverter());
        converters.add(new JsonToListConverter());
        converters.add(new JsonToMapConverter());
        converters.add(new JsonToObjectConverter());
        converters.add(new StringToClassConverter());
        converters.add(new StringToDateConverter());
        converters.add(new StringToMapConverter());
        conversionServiceFactoryBean.setConverters(converters);
        conversionServiceFactoryBean.afterPropertiesSet();
    }

    public static <T> List<T> read(InputStream inputStream, Class<T> clazz, String suffix) throws IOException {
        ResourceData resource = null;
        var resourceEnum = ResourceEnum.getResourceEnumByType(suffix);
        if (resourceEnum == ResourceEnum.JSON) {
            resource = JsonReader.readResourceDataFromCSV(inputStream);
        } else if (resourceEnum == ResourceEnum.EXCEL_XLS || resourceEnum == ResourceEnum.EXCEL_XLSX) {
            resource = ExcelReader.readResourceDataFromExcel(inputStream, clazz.getSimpleName());
        } else if (resourceEnum == ResourceEnum.CSV) {
            resource = CsvReader.readResourceDataFromCSV(inputStream, clazz.getSimpleName());
        } else {
            throw new RunException("Configuration type [{}] of file [{}] is not supported", suffix, clazz.getSimpleName());
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

    private static void inject(Object instance, Field field, String content) {
        try {
            var targetType = new TypeDescriptor(field);
            var value = conversionServiceFactoryBean.getObject().convert(content, TYPE_DESCRIPTOR, targetType);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, instance, value);
        } catch (Exception e) {
            throw new RunException(e, "Unable to convert [content:{}] to property [field:{}] in Excel resource [class:{}]", content, field.getName(), instance.getClass().getSimpleName());
        }
    }

    // 只读取代码里写的字段
    private static Collection<FieldInfo> getFieldInfos(Map<String, Integer> fieldMap, Class<?> clazz) {
        var fieldList = ReflectionUtils.notStaticAndTransientFields(clazz);
        for (var field : fieldList) {
            if (field.isAnnotationPresent(Id.class)) {
                var cellIndex = fieldMap.get(field.getName());
                if (cellIndex != 0) {
                    throw new RunException("The primary key [Id:{}] of the resource class [class:{}] must be placed in the first column of the Excel configuration table, please check the format of the configuration table", field.getName(), clazz);
                }
            }
        }
        return fieldList.stream().filter(it1->
            fieldMap.keySet().stream().anyMatch(it->{
                var ans=false;
                if(it1.isAnnotationPresent(ExcelFieldName.class)){
                    ans|=it.equals(it1.getAnnotation(ExcelFieldName.class).value());
                }
                ans|=it.equals(it1.getName());
                return ans;
            })).map(it1->{
                String excelFieldName;
                List<String> list=null;
            if(it1.isAnnotationPresent(ExcelFieldName.class)){
                list=fieldMap.keySet().stream().filter(it->it.equals(it1.getAnnotation(ExcelFieldName.class).value())).collect(Collectors.toList());
            }
            if(list==null||list.size()==0){
                list=fieldMap.keySet().stream().filter(it->it.equals(it1.getName())).collect(Collectors.toList());
            }
            excelFieldName=list.get(0);
            return new FieldInfo(fieldMap.get(excelFieldName), it1);
        }).collect(Collectors.toList());
    }

    private static class FieldInfo {
        public final int index;
        public final Field field;

        public FieldInfo(int index, Field field) {
            this.index = index;
            this.field = field;
        }
    }

    public static Map<String, Integer> getFieldMap(ResourceData resource, Class<?> clazz) {
        var header = resource.getHeaders();
        if (header == null) {
            throw new RunException("Failed to get attribute control column from excel file of resource [class:{}]", clazz.getSimpleName());
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
            var previousValue = cellFieldMap.put(name, cell.getIndex());
            if (Objects.nonNull(previousValue)) {
                throw new RunException("There are duplicate attribute control columns [field:{}] in the Excel file of the resource [class:{}]", name, clazz.getSimpleName());
            }
        }
        return cellFieldMap;
    }

}
