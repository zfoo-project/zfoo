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
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CodeTemplatePlaceholder;
import com.zfoo.protocol.serializer.ICodeGenerate;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CodeGenerateTypeScript implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateTypeScript.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoots";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, ITsSerializer> tsSerializerMap = new HashMap<>();

    public static ITsSerializer tsSerializer(ISerializer serializer) {
        return tsSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

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

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");
        var protocol_imports_manager = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports_manager.append("import Protocols from './Protocols';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, Protocols.{});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports_manager.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated TypeScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_imports_protocols = new StringBuilder();
        protocol_imports_protocols.append("import IByteBuffer from './IByteBuffer';").append(LS);
        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolClassTemplate.ts");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.TypeScript)
                    , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                    , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                    , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
            ));
            protocol_class.append(formatProtocolTemplate).append(LS);
            protocol_registration.append(StringUtils.format("static {} = {}", protocol_name, protocol_name)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolsTemplate.ts");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports_protocols.toString()
                ,CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                ,     CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.ts", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated TypeScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}/{}';", protocol_name, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated TypeScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolTemplate.ts");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.TypeScript)
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                    , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                    , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.ts", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated TypeScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}';", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated TypeScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolTemplate.ts");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.TypeScript)
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_default(registration)
                    , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                    , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                    , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.ts", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated TypeScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("typescript/IByteBuffer.ts", "typescript/buffer/ByteBuffer.ts", "typescript/buffer/Long.ts", "typescript/buffer/Longbits.ts");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "typescript/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private String protocol_imports_fold(ProtocolRegistration registration) {
        // import IByteBuffer first
        var protocolId = registration.getId();
        var importBuilder = new StringBuilder();
        var protocolPath = GenerateProtocolPath.protocolPathPeriod(protocolId);
        var splits = protocolPath.split(StringUtils.PERIOD_REGEX);
        importBuilder.append(StringUtils.format("import IByteBuffer from '{}IByteBuffer';", "../".repeat(splits.length))).append(LS);

        var subProtocols = ProtocolAnalysis.getFirstSubProtocolIds(protocolId);
        // import other sub protocols
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.getRelativePath(protocolId, subProtocolId);
            importBuilder.append(StringUtils.format("import {} from '{}/{}';", protocolClassName, path, protocolClassName)).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_imports_default(ProtocolRegistration registration) {
        // import IByteBuffer first
        var protocolId = registration.getId();
        var importBuilder = new StringBuilder();
        importBuilder.append(StringUtils.format("import IByteBuffer from './IByteBuffer';")).append(LS);

        // import other sub protocols
        var subProtocols = ProtocolAnalysis.getFirstSubProtocolIds(protocolId);
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            importBuilder.append(StringUtils.format("import {} from './{}';", protocolClassName, protocolClassName)).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var fieldDefinitionBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.TypeScript);
            for (var fieldNote : fieldNotes) {
                fieldDefinitionBuilder.append(fieldNote).append(LS);
            }
            var triple = tsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(StringUtils.format("{}{} = {};", triple.getMiddle(), triple.getLeft(), triple.getRight())).append(LS);
        }
        return fieldDefinitionBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var tsBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            tsBuilder.append("const beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            tsBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            tsBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            tsSerializer(fieldRegistration.serializer()).writeObject(tsBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            tsBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return tsBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var tsBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                tsBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = tsSerializer(fieldRegistration.serializer()).readObject(tsBuilder, 1, field, fieldRegistration);
                tsBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                tsBuilder.append("}").append(LS);
                continue;
            }
            var readObject = tsSerializer(fieldRegistration.serializer()).readObject(tsBuilder, 0, field, fieldRegistration);
            tsBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
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
