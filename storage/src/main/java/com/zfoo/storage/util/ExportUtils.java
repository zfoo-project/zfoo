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
import com.zfoo.storage.interpreter.data.StorageEnum;
import com.zfoo.storage.model.IStorage;

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

    public static void excel2json(String inputDir, String outputDir) throws IOException {
        var excelFiles = scanExcelFiles(inputDir);
        for (var excel : excelFiles) {
            var excelSimpleName = FileUtils.fileSimpleName(excel.getName());
            var resourceData = ExcelReader.readResourceDataFromExcel(FileUtils.openInputStream(excel), excel.getName());

            var outputFilePath = FileUtils.joinPath(outputDir, StringUtils.format("{}.json", excelSimpleName));
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), JsonUtils.object2String(resourceData), true);
        }
    }

    public static void excel2csv(String inputDir, String outputDir) throws IOException {
        var excelFiles = scanExcelFiles(inputDir);
        for (var excel : excelFiles) {
            var excelSimpleName = FileUtils.fileSimpleName(excel.getName());
            var resourceData = ExcelReader.readResourceDataFromExcel(FileUtils.openInputStream(excel), excel.getName());
            var headers = resourceData.getHeaders();
            var rows = resourceData.getRows();
            var builder = new StringBuilder();
            builder.append(headers.stream().map(it -> wrapCsvData(it.getName())).collect(Collectors.joining(StringUtils.COMMA))).append(FileUtils.LS);
            builder.append(headers.stream().map(it -> wrapCsvData(it.getType())).collect(Collectors.joining(StringUtils.COMMA))).append(FileUtils.LS);
            for (var row : rows) {
                builder.append(row.stream().map(it -> wrapCsvData(it)).collect(Collectors.joining(StringUtils.COMMA))).append(FileUtils.LS);
            }
            var outputFilePath = FileUtils.joinPath(outputDir, StringUtils.format("{}.csv", excelSimpleName));
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), builder.toString(), true);
        }
    }

    public static void csv2json(String inputDir, String outputDir) throws IOException {
        var csvFiles = scanCsvFiles(inputDir);
        for (var csv : csvFiles) {
            var excelSimpleName = FileUtils.fileSimpleName(csv.getName());
            var resourceData = CsvReader.readResourceDataFromCSV(FileUtils.openInputStream(csv), csv.getName());

            var outputFilePath = FileUtils.joinPath(outputDir, StringUtils.format("{}.json", excelSimpleName));
            FileUtils.deleteFile(new File(outputFilePath));
            FileUtils.writeStringToFile(new File(outputFilePath), JsonUtils.object2String(resourceData), true);
        }
    }

    public static List<File> scanExcelFiles(String inputDir) {
        return FileUtils.getAllReadableFiles(new File(inputDir))
                .stream()
                .filter(it -> StorageEnum.isExcel(FileUtils.fileExtName(it.getName())))
                .collect(Collectors.toList());
    }

    public static List<File> scanCsvFiles(String inputDir) {
        return FileUtils.getAllReadableFiles(new File(inputDir))
                .stream()
                .filter(it -> StorageEnum.getResourceEnumByType(FileUtils.fileExtName(it.getName())) == StorageEnum.CSV)
                .collect(Collectors.toList());
    }

    // 将class里的map自动赋值storage
    public static <T> T autoWrapData(Class<T> clazz, Map<Class<?>, IStorage<?, ?>> storageMap) {
        var instance = ReflectionUtils.newInstance(clazz);

        var filedList = ReflectionUtils.notStaticAndTransientFields(clazz);

        var wrapClass = new HashSet<Class<?>>();

        for (var field : filedList) {
            var fieldType = field.getType();
            if (!fieldType.equals(Map.class)) {
                throw new RunException("[class:{}] type declaration is incorrect, the exported storage must be a Map interface type", clazz.getCanonicalName());
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
                    throw new RunException("The [field:{}] type declaration in [class:{}] is incorrect, the type needs to be changed to [Map<{}, {}>]", field.getName(), clazz.getSimpleName(), idFieldType, valueClass.getSimpleName());
                }
            } else if (!keyType.equals(idFieldType)) {
                throw new RunException("The [field:{}] type declaration in [class:{}] is incorrect, the type needs to be changed to [Map<{}, {}>]", field.getName(), clazz.getSimpleName(), idFieldType, valueClass.getSimpleName());
            }
            if (!wrapClass.add(valueClass)) {
                throw new RunException("[class:{}] contains duplicate type [{}]", clazz.getCanonicalName(), valueType);
            }
            ReflectionUtils.setField(field, instance, storage.getData());
        }
        return instance;
    }

    private static String wrapCsvData(String value) {
        char textDelimiter = '"';
        char fieldSeparator = ',';

        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }


        char[] valueChars = value.toCharArray();
        boolean needsTextDelimiter = false;
        boolean containsTextDelimiter = false;

        for (char c : valueChars) {
            if (c == textDelimiter) {
                // 字段值中存在包装符
                containsTextDelimiter = needsTextDelimiter = true;
                break;
            } else if (c == fieldSeparator || c == '\n' || c == '\r') {
                // 包含分隔符或换行符需要包装符包装
                needsTextDelimiter = true;
            }
        }

        // 包装符开始
        var builder = new StringBuilder();
        if (needsTextDelimiter) {
            builder.append(textDelimiter);
        }

        // 正文
        if (containsTextDelimiter) {
            for (char c : valueChars) {
                // 转义文本包装符
                if (c == textDelimiter) {
                    builder.append(textDelimiter);
                }
                builder.append(c);
            }
        } else {
            builder.append(valueChars);
        }

        // 包装符结尾
        if (needsTextDelimiter) {
            builder.append(textDelimiter);
        }
        return builder.toString();
    }
}
