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

package com.zfoo.storage.util;

import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.storage.interpreter.CsvReader;
import com.zfoo.storage.interpreter.ExcelReader;
import com.zfoo.storage.model.resource.ResourceEnum;
import com.zfoo.storage.model.vo.Storage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class ExportUtils {

    public static void excel2Json(String inputDir, String outputDir) throws IOException {
        var excelFiles = scanExcelFiles(inputDir);
        for (var excel : excelFiles) {
            var excelSimpleName = FileUtils.fileSimpleName(excel.getName());
            var jsonFileName = StringUtils.format("{}.json", excelSimpleName);
            var resourceData = ExcelReader.readResourceDataFromExcel(FileUtils.openInputStream(excel), excel.getName());

            var outputFilePath = FileUtils.joinPath(outputDir, jsonFileName);
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), JsonUtils.object2String(resourceData), true);
        }
    }

    public static void excel2Csv(String inputDir, String outputDir) throws IOException {
        var excelFiles = scanExcelFiles(inputDir);
        for (var excel : excelFiles) {
            var excelSimpleName = FileUtils.fileSimpleName(excel.getName());
            var csvFileName = StringUtils.format("{}.csv", excelSimpleName);
            var resourceData = CsvReader.readResourceDataFromCSV(FileUtils.openInputStream(excel), excel.getName());

            var outputFilePath = FileUtils.joinPath(outputDir, csvFileName);
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), JsonUtils.object2String(resourceData), true);
        }
    }

    private static List<File> scanExcelFiles(String inputDir) {
        return FileUtils.getAllReadableFiles(new File(inputDir))
                .stream()
                .filter(it -> ResourceEnum.isExcel(FileUtils.fileExtName(it.getName())))
                .collect(Collectors.toList());
    }

    // 将class里的map自动赋值storage
    public static <T> T autoWrapData(Class<T> clazz, Map<Class<?>, Storage<?, ?>> storageMap) {
        var instance = ReflectionUtils.newInstance(clazz);

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        var wrapClass = new HashSet<Class<?>>();

        for (var field : filedList) {
            var fieldType = field.getType();
            if (!fieldType.equals(Map.class)) {
                throw new RunException("[class:{}]类型声明不正确，导出的storage必须为Map接口类型", clazz.getCanonicalName());
            }

            var type = field.getGenericType();
            var types = ((ParameterizedType) type).getActualTypeArguments();

            var keyType = types[0];
            var valueType = types[1];
            var valueClass = (Class<?>) valueType;
            var storage = storageMap.get(valueClass);
            var idFieldType = storage.getIdDef().getField().getType();
            if (idFieldType.isPrimitive()) {
                if (idFieldType.equals(long.class) && keyType.equals(Long.class)) {
                } else if (idFieldType.equals(int.class) && keyType.equals(Integer.class)) {
                } else if (idFieldType.equals(short.class) && keyType.equals(Short.class)) {
                } else if (idFieldType.equals(byte.class) && keyType.equals(Byte.class)) {
                } else {
                    throw new RunException("[class:{}]中的[field:{}]类型声明不正确，类型需要改为[Map<{}, {}>]", clazz.getSimpleName(), field.getName(), idFieldType, valueClass.getSimpleName());
                }
            } else if (!keyType.equals(idFieldType)) {
                throw new RunException("[class:{}]中的[field:{}]类型声明不正确，类型需要改为[Map<{}, {}>]", clazz.getSimpleName(), field.getName(), idFieldType, valueClass.getSimpleName());
            }
            if (!wrapClass.add(valueClass)) {
                throw new RunException("[class:{}]中含有重复的类型[{}]", clazz.getCanonicalName(), valueType);
            }
            ReflectionUtils.setField(field, instance, storage.getData());
        }
        return instance;
    }

}
