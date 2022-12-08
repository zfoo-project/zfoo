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
import com.zfoo.storage.model.anno.ExcelColumn;
import com.zfoo.storage.model.anno.Id;
import com.zfoo.storage.model.resource.ResourceData;
import com.zfoo.storage.model.resource.ResourceEnum;
import com.zfoo.storage.model.resource.ResourceHeader;
import com.zfoo.storage.model.vo.ResourceDef;
import com.zfoo.storage.strategy.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.io.Resource;

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

    public static <T> List<T> read(InputStream inputStream, ResourceDef resourceDef, String suffix) throws IOException {
        ResourceData resourceData = null;
        var resource=resourceDef.getResource();
        var fileName=resource.getFilename();
        var clazz=(Class<T>)resourceDef.getClazz();
        var resourceEnum = ResourceEnum.getResourceEnumByType(suffix);
        if (resourceEnum == ResourceEnum.JSON) {
            resourceData = JsonReader.readResourceDataFromCSV(inputStream);
        } else if (resourceEnum == ResourceEnum.EXCEL_XLS || resourceEnum == ResourceEnum.EXCEL_XLSX) {
            resourceData = ExcelReader.readResourceDataFromExcel(inputStream, fileName);
        } else if (resourceEnum == ResourceEnum.CSV) {
            resourceData = CsvReader.readResourceDataFromCSV(inputStream, fileName);
        } else {
            throw new RunException("不支持文件[{}]的配置类型[{}]", fileName, suffix);
        }

        var result = new ArrayList<T>();
        //获取所有字段
        var fieldInfos = getFieldInfos(resourceData, clazz,fileName);

        var iterator = resourceData.getRows().iterator();
        // 从ROW_SERVER这行开始读取数据
        while (iterator.hasNext()) {
            var columns = iterator.next();
            var instance = ReflectionUtils.newInstance(clazz);

            for (var fieldInfo : fieldInfos) {
                var content = columns.get(fieldInfo.index);
                if (StringUtils.isNotEmpty(content) || fieldInfo.field.getType() == String.class) {
                    inject(instance, fieldInfo.field, content,fileName);
                }
            }
            result.add(instance);
        }
        return result;
    }

    private static void inject(Object instance, Field field, String content,String fileName) {
        try {
            var targetType = new TypeDescriptor(field);
            var value = conversionServiceFactoryBean.getObject().convert(content, TYPE_DESCRIPTOR, targetType);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, instance, value);
        } catch (Exception e) {
            throw new RunException(e, "无法将文件[{}]中的[content:{}]转换为属性[field:{}]", fileName, content, field.getName());
        }
    }
    private static List<FieldInfo> getFieldInfos(ResourceData resourceData, Class<?> clazz, String fileName){
        var fieldList = ReflectionUtils.notStaticAndTransientFields(clazz);
        var resourceHeaders=resourceData.getHeaders();
        if (resourceHeaders == null) {
            throw new RunException("无法获取[{}]文件的属性控制列", fileName);
        }
        List<FieldInfo> fieldInfos=new ArrayList<>();
        for(var field :fieldList) {
            if(field.isAnnotationPresent(ExcelColumn.class)) {
                var excelColumnAnnotation=field.getAnnotation(ExcelColumn.class);
                var value=excelColumnAnnotation.value();
                var index=excelColumnAnnotation.index();
                if(index!=-1){
                    if(index<0||index>=resourceHeaders.size()){
                        throw new RunException("不存在第[{}]列",index);
                    }
                    fieldInfos.add(new FieldInfo(index,field));
                }
                if("".equals(value)==false){
                    boolean findFlag=false;
                    for(ResourceHeader resourceHeader:resourceHeaders){
                        if(resourceHeader.getName().equals(value)==true){
                            fieldInfos.add(new FieldInfo(resourceHeader.getIndex(),field));
                            findFlag=true;
                            break;
                        }
                    }
                    if(findFlag==false){
                        throw new RunException("[{}]文件中不存在[{}]字段",fileName,value);
                    }
                }
            }
            else{
                for(ResourceHeader resourceHeader:resourceHeaders){
                    if(resourceHeader.getName().equals(field.getName())){
                        fieldInfos.add(new FieldInfo(resourceHeader.getIndex(),field));
                    }
                }
            }
        }
        return fieldInfos;
    }
    private static class FieldInfo {
        public final int index;
        public final Field field;

        public FieldInfo(int index, Field field) {
            this.index = index;
            this.field = field;
        }
    }
}
