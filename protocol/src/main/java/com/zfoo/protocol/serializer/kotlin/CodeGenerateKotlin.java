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

package com.zfoo.protocol.serializer.kotlin;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
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
public class CodeGenerateKotlin implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateKotlin.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfookt";
    private static String protocolOutputPath = StringUtils.EMPTY;
    public static String protocolPackage = "com.zfoo.kotlin";

    private static final Map<ISerializer, IKtSerializer> ktSerializerMap = new HashMap<>();

    public static IKtSerializer ktSerializer(ISerializer serializer) {
        return ktSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        ktSerializerMap.put(BooleanSerializer.INSTANCE, new KtBooleanSerializer());
        ktSerializerMap.put(ByteSerializer.INSTANCE, new KtByteSerializer());
        ktSerializerMap.put(ShortSerializer.INSTANCE, new KtShortSerializer());
        ktSerializerMap.put(IntSerializer.INSTANCE, new KtIntSerializer());
        ktSerializerMap.put(LongSerializer.INSTANCE, new KtLongSerializer());
        ktSerializerMap.put(FloatSerializer.INSTANCE, new KtFloatSerializer());
        ktSerializerMap.put(DoubleSerializer.INSTANCE, new KtDoubleSerializer());
        ktSerializerMap.put(StringSerializer.INSTANCE, new KtStringSerializer());
        ktSerializerMap.put(ArraySerializer.INSTANCE, new KtArraySerializer());
        ktSerializerMap.put(ListSerializer.INSTANCE, new KtListSerializer());
        ktSerializerMap.put(SetSerializer.INSTANCE, new KtSetSerializer());
        ktSerializerMap.put(MapSerializer.INSTANCE, new KtMapSerializer());
        ktSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new KtObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {}", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolManagerTemplate.kt");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        protocol_imports.append(StringUtils.format("import {}.*", protocolPackage)).append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put({}::class.java, {})", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.kt"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Kotlin protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : GenerateProtocolFile.subProtocolFirst(registrations)) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolsTemplate.kt");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.kt", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Kotlin protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolManagerTemplate.kt");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {}.{}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_imports.append(StringUtils.format("import {}.{}.{}Registration", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap[{}::class.java] = {}.toShort()", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, StringUtils.format("package {}", protocolPackage)
                , CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.kt"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Kotlin protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolTemplate.kt");
            var protocol_root_path = StringUtils.format("package {}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id));
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.kt", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Kotlin protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {}", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolManagerTemplate.kt");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap[{}::class.java] = {}.toShort()", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.kt"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Kotlin protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolTemplate.kt");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.kt", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Kotlin protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() {
        var rootPackage = StringUtils.format("package {}", protocolPackage);
        var list = List.of("kotlin/IProtocolRegistration.kt"
                , "kotlin/ByteBuffer.kt");
        for (var fileName : list) {
            // IProtocolRegistration
            var template = ClassUtils.getFileFromClassPathToString(fileName);
            var formatTemplate = CodeTemplatePlaceholder.formatTemplate(template, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, rootPackage
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
            ));
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "kotlin/")));
            FileUtils.writeStringToFile(createFile, formatTemplate, false);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolClassTemplate.kt");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Kotlin)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("kotlin/ProtocolRegistrationTemplate.kt");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_imports_fold(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var ktBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("import {}.{}.{}", protocolPackage, GenerateProtocolPath.protocolPathPeriod(subProtocolId), protocolName);
            ktBuilder.append(subProtocolPath).append(LS);
        }
        ktBuilder.append(StringUtils.format("import {}.IProtocolRegistration", protocolPackage)).append(LS);
        ktBuilder.append(StringUtils.format("import {}.ByteBuffer", protocolPackage)).append(LS);
        return ktBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var ktBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Kotlin);
            for (var fieldNote : fieldNotes) {
                ktBuilder.append(fieldNote).append(LS);
            }
            var pair = ktSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            ktBuilder.append(StringUtils.format("var {}: {} = {}", fieldName, pair.getKey(), pair.getValue())).append(LS);
        }
        return ktBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var ktBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            ktBuilder.append("val beforeWriteIndex = buffer.writeOffset()").append(LS);
            ktBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            ktBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            ktSerializer(fieldRegistration.serializer()).writeObject(ktBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            ktBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return ktBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var ktBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                ktBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = ktSerializer(fieldRegistration.serializer()).readObject(ktBuilder, 1, field, fieldRegistration);
                ktBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                ktBuilder.append("}").append(LS);
                continue;
            }
            var readObject = ktSerializer(fieldRegistration.serializer()).readObject(ktBuilder, 0, field, fieldRegistration);
            ktBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return ktBuilder.toString();
    }

    public static String toKotlinClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "Boolean";
                return typeName;
            case "byte":
            case "Byte":
                typeName = "Byte";
                return typeName;
            case "short":
            case "Short":
                typeName = "Short";
                return typeName;
            case "int":
            case "Integer":
                typeName = "Int";
                return typeName;
            case "long":
            case "Long":
                typeName = "Long";
                return typeName;
            case "float":
            case "Float":
                typeName = "Float";
                return typeName;
            case "double":
            case "Double":
                typeName = "Double";
                return typeName;
            case "char":
            case "Character":
            case "String":
                typeName = "String";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "Boolean");
        typeName = typeName.replace("<Boolean", "<Boolean");
        typeName = typeName.replace("Boolean>", "Boolean>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "Byte");
        typeName = typeName.replace("Byte>", "Byte>");
        typeName = typeName.replace("<Byte", "<Byte");

        // 将Short转为short
        typeName = typeName.replace("Short[", "Short");
        typeName = typeName.replace("Short>", "Short>");
        typeName = typeName.replace("<Short", "<Short");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "Int");
        typeName = typeName.replace("Integer>", "Int>");
        typeName = typeName.replace("<Integer", "<Int");

        // 将Long转为long
        typeName = typeName.replace("Long[", "Long");
        typeName = typeName.replace("Long>", "Long>");
        typeName = typeName.replace("<Long", "<Long");

        // 将Float转为float
        typeName = typeName.replace("Float[", "Float");
        typeName = typeName.replace("Float>", "Float>");
        typeName = typeName.replace("<Float", "<Float");

        // 将Double转为double
        typeName = typeName.replace("Double[", "Double");
        typeName = typeName.replace("Double>", "Double>");
        typeName = typeName.replace("<Double", "<Double");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "String");
        typeName = typeName.replace("Character>", "String>");
        typeName = typeName.replace("<Character", "<String");

        // 将String转为string
        typeName = typeName.replace("String[", "String");
        typeName = typeName.replace("String>", "String>");
        typeName = typeName.replace("<String", "<String");

        typeName = typeName.replace("Map<", "Map<");
        typeName = typeName.replace("Set<", "Set<");
        typeName = typeName.replace("List<", "List<");

        return typeName;
    }

}
