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

package com.zfoo.protocol.serializer.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.*;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.DomUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateProtobufUtils {

    private static String protocolOutputRootPath = "protos/";
    private static String protocolManagerName = "protocols";
    public static final String NET_COMMON_MODULE = "common";

    private static String protocolParam = null;

    public static String syntax() {
        return StringUtils.format("syntax = {}{}{};", StringUtils.QUOTATION_MARK, "proto3", StringUtils.QUOTATION_MARK);
    }

    public static String option(String optionKey, String optionValue) {
        return StringUtils.format("option {} = {}{}{};", optionKey, StringUtils.QUOTATION_MARK, optionValue, StringUtils.QUOTATION_MARK);
    }

    public static String importProto(String importValue) {
        return StringUtils.format("import {}{}.proto{};", StringUtils.QUOTATION_MARK, importValue, StringUtils.QUOTATION_MARK);
    }

    public static Map<String, String> parseParam(String param) {
        if (StringUtils.isEmpty(param)) {
            return Map.of();
        }
        var params = param.trim().split(StringUtils.SEMICOLON_REGEX);

        if (ArrayUtils.isEmpty(params)) {
            throw new RunException("参数格式错误，格式protobuf=xxx;java=xxx");
        }

        var map = Arrays.stream(params)
                .map(it -> it.trim())
                .map(it -> new Pair<>(StringUtils.substringBeforeFirst(it, StringUtils.EQUAL), StringUtils.substringAfterFirst(it, StringUtils.EQUAL)))
                .collect(Collectors.toMap(key -> key.getKey(), value -> value.getValue()));

        return map;
    }

    public static void init(GenerateOperation generateOperation) throws IOException {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        var protocolParam = generateOperation.getProtocolParam();

        GenerateProtobufUtils.protocolParam = protocolParam;

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

    }

    public static void createProtocolManager(List<IProtocolRegistration> allGenerateProtocols) {
        var builder = new StringBuilder();
        builder.append(syntax());
        builder.append(LS).append(LS);

        var protocolParam = GenerateProtobufUtils.protocolParam;
        var optionMap = parseParam(protocolParam);
        if (CollectionUtils.isNotEmpty(optionMap)) {
            for (var option : optionMap.entrySet()) {
                builder.append(option(option.getKey(), option.getValue())).append(LS);
            }
        }
        builder.append(LS);

        builder.append("enum ProtocolManager {").append(LS);

        if (allGenerateProtocols.stream().noneMatch(it -> it.protocolId() == 0)) {
            builder.append(TAB).append(StringUtils.format("{} = 0;", "ZFOO_AUTO_GENERATE_ZERO_ENUM_VALUE")).append(LS).append(LS);
        }

        allGenerateProtocols.stream()
                .sorted((a, b) -> a.protocolId() - b.protocolId())
                .forEach(it -> builder.append(TAB).append(StringUtils.format("{} = {};", it.protocolConstructor().getDeclaringClass().getSimpleName(), it.protocolId())).append(LS));

        builder.append("}").append(LS);

        var protocolOutputPath = StringUtils.format("{}/{}.proto", protocolOutputRootPath, protocolManagerName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), builder.toString(), true);
    }

    public static void createProtocols(List<IProtocolRegistration> allGenerateProtocols) {
        var packageMap = new HashMap<String, List<IProtocolRegistration>>();
        for (var protocolRegistration : allGenerateProtocols) {
            var clazz = protocolRegistration.protocolConstructor().getDeclaringClass();
            String packageName = clazz.getPackageName();
            var packagList = packageMap.computeIfAbsent(packageName, k -> new ArrayList<>());
            packagList.add(protocolRegistration);
        }
        for (var protos : packageMap.entrySet()) {
            var builder = new StringBuilder();
            builder.append(syntax());
            builder.append(LS).append(LS);

            var protocolParam = GenerateProtobufUtils.protocolParam;
            var optionMap = parseParam(protocolParam);
            if (CollectionUtils.isNotEmpty(optionMap)) {
                for (var option : optionMap.entrySet()) {
                    builder.append(option(option.getKey(), option.getValue())).append(LS);
                }
                builder.append(LS);
            }
            builder.append(importProto(NET_COMMON_MODULE)).append(LS);
            builder.append(LS);

            for (var protocolRegistration : protos.getValue()) {
                var protocolClass = protocolRegistration.protocolConstructor().getDeclaringClass();
                var protocolId = protocolRegistration.protocolId();

                if (!isProtobufProtocol((ProtocolRegistration) protocolRegistration)) {
                    continue;
                }

                builder.append("// id = ").append(protocolId).append(LS);

                var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Protobuf);
                if (StringUtils.isNotBlank(classNote)) {
                    builder.append(classNote).append(LS);
                }

                builder.append(StringUtils.format("message {} {", protocolClass.getSimpleName())).append(LS);
                builder.append(protobufFiled((ProtocolRegistration) protocolRegistration));
                builder.append("}").append(LS).append(LS);
            }

            String protoName = StringUtils.substringAfterLast(protos.getKey(), StringUtils.PERIOD);
            var protocolOutputPath = StringUtils.format("{}/{}.proto", protocolOutputRootPath, protoName);
            FileUtils.writeStringToFile(new File(protocolOutputPath), builder.toString(), true);
        }
    }

    /**
     * 判断是否是protobuf协议
     * @param registration
     * @return
     */
    private static boolean isProtobufProtocol(ProtocolRegistration registration) {
        var protocolClass = registration.protocolConstructor().getDeclaringClass();
        if (protocolClass.isAnnotationPresent(ProtobufClass.class)) {
            return true;
        }
        var fields = registration.getFields();
        for (var field : fields) {
            if (!field.isAnnotationPresent(Protobuf.class)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 优化一下生成协议顺序
     *
     * @param registration
     * @return
     */
    private static String protobufFiled(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        // 先检查注解id顺序是否有重复
        var orderMap = new TreeMap<Integer, String>();
        for (int i = 0, length = fields.length; i < length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            var protobuf = field.getDeclaredAnnotation(Protobuf.class);
            if (protobuf == null) {
                throw new RunException("protobuf协议类必须加上注解[{}]，并且标识order的顺序", Protobuf.class.getSimpleName());
            }
            var order = protobuf.order();
            if (orderMap.containsKey(order)) {
                throw new RunException("protobuf协议类注解[{}]，order的顺序重复[{}]", Protobuf.class.getSimpleName(), order);
            }
            var builder = new StringBuilder();
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Protobuf);
            if (StringUtils.isNotBlank(fieldNote)) {
                builder.append(TAB).append(fieldNote).append(LS);
            }

            var singleFieldStr = toFieldTypeName(fieldRegistration);
            if (StringUtils.isNotBlank(singleFieldStr)) {
                builder.append(TAB).append(StringUtils.format("{} {} = {};", singleFieldStr, fieldName, order)).append(LS);
                orderMap.put(order, builder.toString());
                continue;
            }

            if (fieldRegistration instanceof ArrayField) {
                var arrayField = (ArrayField) fieldRegistration;
                var arrayFieldStr = toFieldTypeName(arrayField.getArrayElementRegistration());
                builder.append(TAB).append(StringUtils.format("repeated {} {} = {};", arrayFieldStr, fieldName, order)).append(LS);
            } else if (fieldRegistration instanceof ListField) {
                var listField = (ListField) fieldRegistration;
                var listFieldStr = toFieldTypeName(listField.getListElementRegistration());
                builder.append(TAB).append(StringUtils.format("repeated {} {} = {};", listFieldStr, fieldName, order)).append(LS);
            } else if (fieldRegistration instanceof SetField) {
                var setField = (SetField) fieldRegistration;
                var setFieldStr = toFieldTypeName(setField.getSetElementRegistration());
                builder.append(TAB).append(StringUtils.format("repeated {} {} = {};", setFieldStr, fieldName, order)).append(LS);
            } else if (fieldRegistration instanceof MapField) {
                var mapField = (MapField) fieldRegistration;
                var keyFieldStr = toFieldTypeName(mapField.getMapKeyRegistration());
                var valueFieldStr = toFieldTypeName(mapField.getMapValueRegistration());
                builder.append(TAB).append(StringUtils.format("map<{}, {}> {} = {};", keyFieldStr, valueFieldStr, fieldName, order)).append(LS);
            } else {
                throw new RunException("无法识别的protobuf类型[{}]", field.getName());
            }
            orderMap.put(order, builder.toString());
        }
        
        var builder = new StringBuilder();
        for (var entry : orderMap.entrySet()) {
            builder.append(entry.getValue());
        }
        return builder.toString();
    }


    /**
     * 注意protobuf不支持byte，short，char
     */
    private static String toFieldTypeName(IFieldRegistration fieldRegistration) {
        if (fieldRegistration instanceof BaseField) {
            var serializer = fieldRegistration.serializer();
            if (serializer instanceof BooleanSerializer) {
                return "bool";
            } else if (serializer instanceof ByteSerializer) {
                return "byte";
            } else if (serializer instanceof ShortSerializer) {
                return "int16";
            } else if (serializer instanceof IntSerializer) {
                return "int32";
            } else if (serializer instanceof LongSerializer) {
                return "int64";
            } else if (serializer instanceof FloatSerializer) {
                return "float";
            } else if (serializer instanceof DoubleSerializer) {
                return "double";
            } else if (serializer instanceof StringSerializer) {
                return "string";
            }
        } else if (fieldRegistration instanceof ObjectProtocolField) {
            var objectProtocolField = (ObjectProtocolField) fieldRegistration;
            return ProtocolManager.getProtocol(objectProtocolField.getProtocolId()).protocolConstructor().getDeclaringClass().getSimpleName();
        }

        return null;
    }

    public static void clear() {
        protocolOutputRootPath = null;
        protocolManagerName = null;
        protocolParam = null;
    }

}
