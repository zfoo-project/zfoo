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
import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.exception.RunException;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateProtobufUtils {

    private static String protocolOutputRootPath = "protos/";
    private static String protocolManagerName = "protocols";

    private static XmlProtobuf xmlProtobuf = null;

    public static String syntax() {
        return StringUtils.format("syntax = {}{}{};", StringUtils.QUOTATION_MARK, xmlProtobuf.getSyntax(), StringUtils.QUOTATION_MARK);
    }

    public static String option(String optionKey, String optionValue) {
        return StringUtils.format("option {} = {}{}{};", optionKey, StringUtils.QUOTATION_MARK, optionValue, StringUtils.QUOTATION_MARK);
    }

    public static String importProto(String importValue) {
        return StringUtils.format("import {}{}.proto{};", StringUtils.QUOTATION_MARK, importValue, StringUtils.QUOTATION_MARK);
    }

    public static Map<String, String> parseParam(String param) {
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
        if (StringUtils.isEmpty(protocolParam)) {
            throw new RunException("生成protobuf协议的protocolParam参数不能为空");
        }

        var map = parseParam(protocolParam);

        var protobufXmlPath = map.get("protobuf");

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

        var inputStream = ClassUtils.getFileFromClassPath(protobufXmlPath);
        var xmlProtobufObj = DomUtils.inputStream2Object(inputStream, XmlProtobuf.class);

        if (!xmlProtobufObj.getSyntax().equals("proto3")) {
            throw new RunException("生成protobuf协议只支持proto3");
        }

        var protoSet = new HashSet<String>();

        for (var protos : xmlProtobufObj.getProtos()) {
            if (protos.getName().equals(protocolManagerName)) {
                throw new RunException("protobuf的协议文件名称不能用保留名称[{}]", protocolManagerName);
            }

            if (protoSet.contains(protos.getName())) {
                throw new RunException("protobuf的协议文件名称重复定义[{}]", protos.getName());
            }

            protoSet.add(protos.getName());
        }

        xmlProtobuf = xmlProtobufObj;
    }

    public static void createProtocolManager() throws ClassNotFoundException {
        var allGenerateProtocols = new HashSet<IProtocolRegistration>();
        for (var protos : xmlProtobuf.getProtos()) {
            for (var protocol : protos.getProtocols()) {
                var protocolClass = Class.forName(protocol.getLocation());
                var protocolId = ProtocolAnalysis.getProtocolIdByClass(protocolClass);
                var protocolRegistration = ProtocolManager.getProtocol(protocolId);

                if (allGenerateProtocols.contains(protocolRegistration)) {
                    throw new RunException("protobuf的xml协议文件中重复定义了协议[{}]", protocolClass.getSimpleName());
                }

                allGenerateProtocols.add(protocolRegistration);
            }
        }

        var builder = new StringBuilder();
        builder.append(syntax());
        builder.append(LS).append(LS);

        if (StringUtils.isNotEmpty(xmlProtobuf.getOption())) {
            var optionMap = parseParam(xmlProtobuf.getOption());
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

    public static void createProtocols() throws ClassNotFoundException {
        for (var protos : xmlProtobuf.getProtos()) {

            var builder = new StringBuilder();
            builder.append(syntax());
            builder.append(LS).append(LS);

            if (StringUtils.isNotEmpty(protos.getImportProto())) {
                var params = protos.getImportProto().trim().split(StringUtils.SEMICOLON_REGEX);
                for (var importProto : params) {
                    if (StringUtils.isBlank(importProto)) {
                        continue;
                    }
                    builder.append(importProto(importProto.trim())).append(LS);
                }
                builder.append(LS);
            }

            if (StringUtils.isNotEmpty(protos.getOption())) {
                var optionMap = parseParam(protos.getOption());
                for (var option : optionMap.entrySet()) {
                    builder.append(option(option.getKey(), option.getValue())).append(LS);
                }
                builder.append(LS);
            }

            for (var protocol : protos.getProtocols()) {
                var protocolClass = Class.forName(protocol.getLocation());
                var protocolId = ProtocolAnalysis.getProtocolIdByClass(protocolClass);
                var protocolRegistration = ProtocolManager.getProtocol(protocolId);

                builder.append("// id = ").append(protocolId).append(LS);

                var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Protobuf);
                if (StringUtils.isNotBlank(classNote)) {
                    builder.append(classNote).append(LS);
                }

                builder.append(StringUtils.format("message {} {", protocolClass.getSimpleName())).append(LS);
                builder.append(protobufFiled((ProtocolRegistration) protocolRegistration));
                builder.append("}").append(LS).append(LS);
            }

            var protocolOutputPath = StringUtils.format("{}/{}.proto", protocolOutputRootPath, protos.getName());
            FileUtils.writeStringToFile(new File(protocolOutputPath), builder.toString(), true);
        }
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
            } else if (serializer instanceof CharSerializer) {
                return "char";
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
        xmlProtobuf = null;
    }

}
