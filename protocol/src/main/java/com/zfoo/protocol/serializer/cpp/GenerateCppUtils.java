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

package com.zfoo.protocol.serializer.cpp;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateCppUtils {

    private static String protocolOutputRootPath = "cppProtocol";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, ICppSerializer> cppSerializerMap;

    public static ICppSerializer cppSerializer(ISerializer serializer) {
        return cppSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputPath));
        FileUtils.createDirectory(protocolOutputPath);

        cppSerializerMap = new HashMap<>();
        cppSerializerMap.put(BooleanSerializer.INSTANCE, new CppBooleanSerializer());
        cppSerializerMap.put(ByteSerializer.INSTANCE, new CppByteSerializer());
        cppSerializerMap.put(ShortSerializer.INSTANCE, new CppShortSerializer());
        cppSerializerMap.put(IntSerializer.INSTANCE, new CppIntSerializer());
        cppSerializerMap.put(LongSerializer.INSTANCE, new CppLongSerializer());
        cppSerializerMap.put(FloatSerializer.INSTANCE, new CppFloatSerializer());
        cppSerializerMap.put(DoubleSerializer.INSTANCE, new CppDoubleSerializer());
        cppSerializerMap.put(StringSerializer.INSTANCE, new CppStringSerializer());
        cppSerializerMap.put(ArraySerializer.INSTANCE, new CppArraySerializer());
        cppSerializerMap.put(ListSerializer.INSTANCE, new CppListSerializer());
        cppSerializerMap.put(SetSerializer.INSTANCE, new CppSetSerializer());
        cppSerializerMap.put(MapSerializer.INSTANCE, new CppMapSerializer());
        cppSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new CppObjectProtocolSerializer());
    }

    public static void clear() {
        cppSerializerMap = null;
        protocolOutputRootPath = null;
        protocolOutputPath = null;
    }

    /**
     * 生成协议依赖的工具类
     */
    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("cpp/ByteBuffer.h");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "cpp/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolManagerTemplate.h");

        var headerBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            headerBuilder.append(StringUtils.format("#include \"{}/{}.h\"", protocolOutputRootPath, GenerateProtocolPath.protocolAbsolutePath(protocolId, CodeLanguage.Cpp))).append(LS);
            initProtocolBuilder.append(TAB).append(TAB).append(StringUtils.format("protocols[{}] = new {}Registration();", protocolId, protocol.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS);
        }
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, headerBuilder.toString(), initProtocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.h")), protocolManagerTemplate, true);
    }

    /**
     * 生成协议类
     */
    public static void createCppProtocolFile(ProtocolRegistration registration) throws IOException {
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();

        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolTemplate.h");

        // protocol object
        var defineProtocolName = protocolClazzName.toUpperCase();
        var includeHeaders = includeSubProtocol(registration);
        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Cpp);
        var fieldDefinition = fieldDefinition(registration);
        var valueOfMethod = valueOfMethod(registration);
        var operator = operator(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, defineProtocolName, defineProtocolName, protocolOutputRootPath, includeHeaders, classNote
                , protocolClazzName, fieldDefinition, protocolClazzName, protocolClazzName, valueOfMethod.getKey(), protocolClazzName
                , valueOfMethod.getValue().trim(), protocolId, protocolClazzName, operator.trim(),
                protocolClazzName, protocolId, protocolClazzName, writeObject.trim(), protocolClazzName, readObject.trim());

        var protocolOutputPath = StringUtils.format("{}/{}/{}.h"
                , GenerateCppUtils.protocolOutputPath
                , GenerateProtocolPath.getCapitalizeProtocolPath(protocolId)
                , protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), protocolTemplate, true);
    }


    private static String includeSubProtocol(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);

        if (CollectionUtils.isEmpty(subProtocols)) {
            return StringUtils.EMPTY;
        }
        var cppBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("#include \"{}/{}/{}.h\""
                    , protocolOutputRootPath
                    , GenerateProtocolPath.getCapitalizeProtocolPath(protocolId)
                    , protocolClassName);
            subProtocolPath = subProtocolPath.replaceAll("//", StringUtils.SLASH);
            cppBuilder.append(subProtocolPath).append(LS);
        }

        return cppBuilder.toString();
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var cppBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var propertyTypeAndName = cppSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            var propertyType = propertyTypeAndName.getKey();
            var propertyName = propertyTypeAndName.getValue();

            var propertyFullName = StringUtils.format("{} {};", propertyType, propertyName);
            // 生成注释
            var filedNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Cpp);
            if (StringUtils.isNotBlank(filedNote)) {
                cppBuilder.append(TAB + TAB).append(filedNote).append(LS);
            }
            cppBuilder.append(TAB + TAB).append(propertyFullName).append(LS);
        }

        return cppBuilder.toString().trim();
    }

    private static Pair<String, String> valueOfMethod(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var filedList = new ArrayList<Pair<String, String>>();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var propertyTypeAndName = cppSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            filedList.add(propertyTypeAndName);
        }

        // ValueOf()方法
        var valueOfParams = filedList.stream()
                .map(it -> StringUtils.format("{} {}", it.getKey(), it.getValue()))
                .toList();
        var valueOfParamsStr = StringUtils.joinWith(StringUtils.COMMA + " ", valueOfParams.toArray());

        var cppBuilder = new StringBuilder();
        filedList.forEach(it -> cppBuilder.append(TAB + TAB + TAB).append(StringUtils.format("packet.{} = {};", it.getValue(), it.getValue())).append(LS));
        return new Pair<>(valueOfParamsStr, cppBuilder.toString());
    }

    private static String operator(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var filedList = new ArrayList<Pair<String, String>>();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var propertyTypeAndName = cppSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            filedList.add(propertyTypeAndName);
        }

        // bool operator<
        var cppBuilder = new StringBuilder();
        for (var fieldPair : filedList) {
            cppBuilder.append(TAB + TAB + TAB).append(StringUtils.format("if ({} < _.{}) { return true; }", fieldPair.getValue(), fieldPair.getValue())).append(LS);
            cppBuilder.append(TAB + TAB + TAB).append(StringUtils.format("if (_.{} < {}) { return false; }", fieldPair.getValue(), fieldPair.getValue())).append(LS);
        }
        return cppBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var cppBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var serializer = cppSerializer(fieldRegistration.serializer());
            if (ProtocolManager.isProtocolClass(field.getType())) {
                serializer.writeObject(cppBuilder, "&message->" + field.getName(), 3, field, fieldRegistration);
            } else {
                serializer.writeObject(cppBuilder, "message->" + field.getName(), 3, field, fieldRegistration);
            }
        }
        return cppBuilder.toString();
    }


    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var cppBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                cppBuilder.append(TAB + TAB + TAB).append(StringUtils.format("if (!buffer.isReadable()) { return packet; }")).append(LS);
            }

            var readObject = cppSerializer(fieldRegistration.serializer()).readObject(cppBuilder, 3, field, fieldRegistration);
            cppBuilder.append(TAB + TAB + TAB);
            if (ProtocolManager.isProtocolClass(field.getType())) {
                cppBuilder.append(StringUtils.format("packet->{} = *{};", field.getName(), readObject));
            } else {
                cppBuilder.append(StringUtils.format("packet->{} = {};", field.getName(), readObject));
            }

            cppBuilder.append(LS);
        }
        return cppBuilder.toString();
    }

    public static String toCppClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        // CSharp不适用基础类型的泛型，会影响性能
        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "bool";
                return typeName;
            case "byte":
            case "Byte":
                typeName = "int8_t";
                return typeName;
            case "short":
            case "Short":
                typeName = "int16_t";
                return typeName;
            case "int":
            case "Integer":
                typeName = "int32_t";
                return typeName;
            case "long":
            case "Long":
                typeName = "int64_t";
                return typeName;
            case "Float":
                typeName = "float";
                return typeName;
            case "Double":
                typeName = "double";
                return typeName;
            case "Character":
                typeName = "char";
                return typeName;
            case "String":
                typeName = "string";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "bool");
        typeName = typeName.replace("<Boolean", "<bool");
        typeName = typeName.replace("Boolean>", "bool>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "int8_t");
        typeName = typeName.replace("Byte>", "int8_t>");
        typeName = typeName.replace("<Byte", "<int8_t");

        // 将Short转为short
        typeName = typeName.replace("Short[", "int16_t");
        typeName = typeName.replace("Short>", "int16_t>");
        typeName = typeName.replace("<Short", "<int16_t");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "int32_t");
        typeName = typeName.replace("Integer>", "int32_t>");
        typeName = typeName.replace("<Integer", "<int32_t");


        // 将Long转为long
        typeName = typeName.replace("Long[", "int64_t");
        typeName = typeName.replace("Long>", "int64_t>");
        typeName = typeName.replace("<Long", "<int64_t");

        // 将Float转为float
        typeName = typeName.replace("Float[", "float");
        typeName = typeName.replace("Float>", "float>");
        typeName = typeName.replace("<Float", "<float");

        // 将Double转为double
        typeName = typeName.replace("Double[", "double");
        typeName = typeName.replace("Double>", "double>");
        typeName = typeName.replace("<Double", "<double");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "char");
        typeName = typeName.replace("Character>", "char>");
        typeName = typeName.replace("<Character", "<char");

        // 将String转为string
        typeName = typeName.replace("String[", "string");
        typeName = typeName.replace("String>", "string>");
        typeName = typeName.replace("<String", "<string");

        // 将Map转为map
        typeName = typeName.replace("Map<", "map<");

        // 将Set转为set
        typeName = typeName.replace("Set<", "set<");

        // 将List转为vector
        typeName = typeName.replace("List<", "list<");

        return typeName;
    }
}
