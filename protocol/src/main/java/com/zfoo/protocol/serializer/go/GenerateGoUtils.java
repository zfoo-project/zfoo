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

package com.zfoo.protocol.serializer.go;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateGoUtils {

    private static String protocolOutputRootPath = "goProtocol/";

    private static Map<ISerializer, IGoSerializer> goSerializerMap;

    public static IGoSerializer goSerializer(ISerializer serializer) {
        return goSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

        goSerializerMap = new HashMap<>();
        goSerializerMap.put(BooleanSerializer.INSTANCE, new GoBooleanSerializer());
        goSerializerMap.put(ByteSerializer.INSTANCE, new GoByteSerializer());
        goSerializerMap.put(ShortSerializer.INSTANCE, new GoShortSerializer());
        goSerializerMap.put(IntSerializer.INSTANCE, new GoIntSerializer());
        goSerializerMap.put(LongSerializer.INSTANCE, new GoLongSerializer());
        goSerializerMap.put(FloatSerializer.INSTANCE, new GoFloatSerializer());
        goSerializerMap.put(DoubleSerializer.INSTANCE, new GoDoubleSerializer());
        goSerializerMap.put(CharSerializer.INSTANCE, new GoCharSerializer());
        goSerializerMap.put(StringSerializer.INSTANCE, new GoStringSerializer());
        goSerializerMap.put(ArraySerializer.INSTANCE, new GoArraySerializer());
        goSerializerMap.put(ListSerializer.INSTANCE, new GoListSerializer());
        goSerializerMap.put(SetSerializer.INSTANCE, new GoSetSerializer());
        goSerializerMap.put(MapSerializer.INSTANCE, new GoMapSerializer());
        goSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new GoObjectProtocolSerializer());
    }

    public static void clear() {
        goSerializerMap = null;
        protocolOutputRootPath = null;
    }

    /**
     * 生成协议依赖的工具类
     */
    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("go/ByteBuffer.go");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, StringUtils.substringAfterFirst(fileName, "go/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var initProtocolBuilder = new StringBuilder();
        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> initProtocolBuilder.append(TAB).append(StringUtils.format("Protocols[{}] = new({})", it.protocolId(), it.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS));

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("go/ProtocolManagerTemplate.go");
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, initProtocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.go")), protocolManagerTemplate, true);
    }

    public static void createGoProtocolFile(ProtocolRegistration registration) throws IOException {
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ArrayUtils.isEmpty(registration.getFields())
                ? ClassUtils.getFileFromClassPathToString("go/ProtocolTemplateEmpty.go")
                : ClassUtils.getFileFromClassPathToString("go/ProtocolTemplate.go");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Go);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);
        if (ArrayUtils.isEmpty(registration.getFields())) {
            protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, fieldDefinition.trim()
                    , protocolClazzName, protocolId, protocolClazzName, protocolClazzName, protocolClazzName);
        } else {
            protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, fieldDefinition.trim()
                    , protocolClazzName, protocolId, protocolClazzName, protocolClazzName
                    , writeObject.trim(), protocolClazzName, protocolClazzName, readObject.trim());
        }

        var protocolPath = GenerateProtocolPath.getProtocolPath(protocolId);
        if (StringUtils.isEmpty(protocolPath)) {
            protocolPath = protocolClazzName;
        } else if (protocolPath.contains(StringUtils.SLASH)) {
            protocolPath = StringUtils.substringAfterLast(protocolPath, StringUtils.SLASH);
        }

        var protocolOutputPath = StringUtils.format("{}/{}.go"
                , protocolOutputRootPath
                , protocolPath);
        var protocolFile = new File(protocolOutputPath);
        if (protocolFile.exists()) {
            FileUtils.writeStringToFile(protocolFile, StringUtils.substringAfterFirst(protocolTemplate, "package protocol"), true);
        } else {
            FileUtils.writeStringToFile(protocolFile, protocolTemplate, true);
        }
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = StringUtils.capitalize(field.getName());
            var fieldType = goSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);

            var propertyFullName = StringUtils.format("{} {}", fieldName, fieldType);
            // 生成注释
            var filedNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Go);
            if (StringUtils.isNotBlank(filedNote)) {
                goBuilder.append(TAB).append(filedNote).append(LS);
            }
            goBuilder.append(TAB).append(propertyFullName).append(LS);
        }
        return goBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            goSerializer(fieldRegistration.serializer()).writeObject(goBuilder, "message." + StringUtils.capitalize(field.getName()), 1, field, fieldRegistration);
        }
        return goBuilder.toString();
    }


    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                goBuilder.append(TAB).append("if !buffer.IsReadable()").append(LS);
                goBuilder.append(TAB).append("{").append(LS);
                goBuilder.append(TAB + TAB).append("return packet").append(LS);
                goBuilder.append(TAB).append("}").append(LS);
            }
            var readObject = goSerializer(fieldRegistration.serializer()).readObject(goBuilder, 1, field, fieldRegistration);
            goBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", StringUtils.capitalize(field.getName()), readObject)).append(LS);
        }
        return goBuilder.toString();
    }

    public static String toGoClassName(String typeName) {
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
                typeName = "int8";
                return typeName;
            case "short":
            case "Short":
                typeName = "int16";
                return typeName;
            case "int":
            case "Integer":
                typeName = "int";
                return typeName;
            case "long":
            case "Long":
                typeName = "int64";
                return typeName;
            case "Float":
                typeName = "float32";
                return typeName;
            case "Double":
                typeName = "float64";
                return typeName;
            case "Character":
                typeName = "string";
                return typeName;
            default:
        }

        return typeName;
    }
}
