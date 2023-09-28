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

package com.zfoo.protocol.serializer.typescript;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateTsUtils {

    // custom configuration
    public static String protocolOutputRootPath = "zfoots";
    public static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, ITsSerializer> tsSerializerMap;

    public static ITsSerializer tsSerializer(ISerializer serializer) {
        return tsSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        // if not specify output path, then use current default path
        if (StringUtils.isEmpty(generateOperation.getProtocolPath())) {
            protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        } else {
            protocolOutputPath = generateOperation.getProtocolPath();
        }
        FileUtils.deleteFile(new File(protocolOutputPath));

        tsSerializerMap = new HashMap<>();
        tsSerializerMap.put(BooleanSerializer.INSTANCE, new TsBooleanSerializer());
        tsSerializerMap.put(ByteSerializer.INSTANCE, new TsByteSerializer());
        tsSerializerMap.put(ShortSerializer.INSTANCE, new TsShortSerializer());
        tsSerializerMap.put(IntSerializer.INSTANCE, new TsIntSerializer());
        tsSerializerMap.put(LongSerializer.INSTANCE, new TsLongSerializer());
        tsSerializerMap.put(FloatSerializer.INSTANCE, new TsFloatSerializer());
        tsSerializerMap.put(DoubleSerializer.INSTANCE, new TsDoubleSerializer());
        tsSerializerMap.put(StringSerializer.INSTANCE, new TsStringSerializer());
        tsSerializerMap.put(ArraySerializer.INSTANCE, new TsArraySerializer());
        tsSerializerMap.put(ListSerializer.INSTANCE, new TsListSerializer());
        tsSerializerMap.put(SetSerializer.INSTANCE, new TsSetSerializer());
        tsSerializerMap.put(MapSerializer.INSTANCE, new TsMapSerializer());
        tsSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new TsObjectProtocolSerializer());
    }

    public static void clear() {
        protocolOutputRootPath = null;
        protocolOutputPath = null;
        tsSerializerMap = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("typescript/buffer/ByteBuffer.ts", "typescript/buffer/Long.ts", "typescript/buffer/Longbits.ts");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "typescript/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");

        var importBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocolId, CodeLanguage.TypeScript);
            importBuilder.append(StringUtils.format("import {} from './{}';", name, path)).append(LS);
            initProtocolBuilder.append(StringUtils.format("protocols.set({}, {});", protocolId, name)).append(LS);

        }

        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), initProtocolBuilder.toString().trim());
        var outputPath = StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts");
        FileUtils.writeStringToFile(new File(outputPath), protocolManagerTemplate, true);
    }

    public static void createTsProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolTemplate.ts");

        var importSubProtocol = importSubProtocol(registration);
        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.TypeScript);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, importSubProtocol, classNote, protocolClazzName, fieldDefinition.trim()
                , protocolId, protocolClazzName, protocolClazzName, writeObject.trim(), protocolClazzName, protocolClazzName, readObject.trim(), protocolClazzName);
        var outputPath = StringUtils.format("{}/{}/{}.ts", protocolOutputPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(outputPath), protocolTemplate, true);
    }

    private static String importSubProtocol(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getFirstSubProtocolIds(protocolId);

        if (CollectionUtils.isEmpty(subProtocols)) {
            return StringUtils.EMPTY;
        }
        var importBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.getRelativePath(protocolId, subProtocolId);
            if (StringUtils.isBlank(path)) {
                importBuilder.append(StringUtils.format("import {} from './{}';", protocolClassName, protocolClassName)).append(LS);
            } else {
                importBuilder.append(StringUtils.format("import {} from '{}/{}';", protocolClassName, path, protocolClassName)).append(LS);
            }
        }
        return importBuilder.toString();
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var fieldDefinitionBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            IFieldRegistration fieldRegistration = fieldRegistrations[i];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.TypeScript);
            if (StringUtils.isNotBlank(fieldNote)) {
                fieldDefinitionBuilder.append(TAB).append(fieldNote).append(LS);
            }
            var triple = tsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(TAB).append(StringUtils.format("{}{} = {};", triple.getMiddle(), triple.getLeft(), triple.getRight())).append(LS);
        }
        return fieldDefinitionBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var tsBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            tsBuilder.append("const beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            tsBuilder.append(TAB + TAB).append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            tsBuilder.append(TAB + TAB).append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            tsSerializer(fieldRegistration.serializer()).writeObject(tsBuilder, "packet." + field.getName(), 2, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            tsBuilder.append(TAB + TAB).append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return tsBuilder.toString();
    }

    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var tsBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {

                tsBuilder.append(TAB + TAB).append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = tsSerializer(fieldRegistration.serializer()).readObject(tsBuilder, 3, field, fieldRegistration);
                tsBuilder.append(TAB + TAB+ TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                tsBuilder.append(TAB + TAB).append("}").append(LS);
                continue;
            }
            var readObject = tsSerializer(fieldRegistration.serializer()).readObject(tsBuilder, 2, field, fieldRegistration);
            tsBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return tsBuilder.toString();
    }

    public static String toTsClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "boolean";
                return typeName;
            case "byte":
            case "Byte":
            case "short":
            case "Short":
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "float":
            case "Float":
            case "double":
            case "Double":
                typeName = "number";
                return typeName;
            case "char":
            case "Character":
            case "String":
                typeName = "string";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "boolean");
        typeName = typeName.replace("<Boolean", "<boolean");
        typeName = typeName.replace("Boolean>", "boolean>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "number");
        typeName = typeName.replace("Byte>", "number>");
        typeName = typeName.replace("<Byte", "<number");

        // 将Short转为short
        typeName = typeName.replace("Short[", "number");
        typeName = typeName.replace("Short>", "number>");
        typeName = typeName.replace("<Short", "<number");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "number");
        typeName = typeName.replace("Integer>", "number>");
        typeName = typeName.replace("<Integer", "<number");


        // 将Long转为long
        typeName = typeName.replace("Long[", "number");
        typeName = typeName.replace("Long>", "number>");
        typeName = typeName.replace("<Long", "<number");

        // 将Float转为float
        typeName = typeName.replace("Float[", "number");
        typeName = typeName.replace("Float>", "number>");
        typeName = typeName.replace("<Float", "<number");

        // 将Double转为double
        typeName = typeName.replace("Double[", "number");
        typeName = typeName.replace("Double>", "number>");
        typeName = typeName.replace("<Double", "<number");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "string");
        typeName = typeName.replace("Character>", "string>");
        typeName = typeName.replace("<Character", "<string");

        // 将String转为string
        typeName = typeName.replace("String[", "string");
        typeName = typeName.replace("String>", "string>");
        typeName = typeName.replace("<String", "<string");

        typeName = typeName.replace("Map<", "Map<");
        typeName = typeName.replace("Set<", "Set<");
        typeName = typeName.replace("List<", "Array<");

        return typeName;
    }
}
