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

package com.zfoo.protocol.serializer.java;

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
public class CodeGenerateJava implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateJava.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoojava";
    private static String protocolOutputPath = StringUtils.EMPTY;
    public static String protocolPackage = "com.zfoo.java";

    private static final Map<ISerializer, IJavaSerializer> javaSerializerMap = new HashMap<>();

    public static IJavaSerializer javaSerializer(ISerializer serializer) {
        return javaSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        javaSerializerMap.put(BoolSerializer.INSTANCE, new JavaBoolSerializer());
        javaSerializerMap.put(ByteSerializer.INSTANCE, new JavaByteSerializer());
        javaSerializerMap.put(ShortSerializer.INSTANCE, new JavaShortSerializer());
        javaSerializerMap.put(IntSerializer.INSTANCE, new JavaIntSerializer());
        javaSerializerMap.put(LongSerializer.INSTANCE, new JavaLongSerializer());
        javaSerializerMap.put(FloatSerializer.INSTANCE, new JavaFloatSerializer());
        javaSerializerMap.put(DoubleSerializer.INSTANCE, new JavaDoubleSerializer());
        javaSerializerMap.put(StringSerializer.INSTANCE, new JavaStringSerializer());
        javaSerializerMap.put(ArraySerializer.INSTANCE, new JavaArraySerializer());
        javaSerializerMap.put(ListSerializer.INSTANCE, new JavaListSerializer());
        javaSerializerMap.put(SetSerializer.INSTANCE, new JavaSetSerializer());
        javaSerializerMap.put(MapSerializer.INSTANCE, new JavaMapSerializer());
        javaSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new JavaObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {};", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolManagerTemplate.java");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = new Protocols.{}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put(Protocols.{}.class, (short){});", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.java"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Java protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : GenerateProtocolFile.subProtocolFirst(registrations)) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append("public static " + protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolsTemplate.java");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString().replace("public class", "public static class")
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.java", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Java protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolManagerTemplate.java");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {}.{}.{};", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}.{}Registration;", protocol_id, protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put({}.class, (short){});", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, StringUtils.format("package {};", protocolPackage)
                , CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.java"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Java protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolTemplate.java");
            var protocol_root_path = StringUtils.format("package {}.{};", protocolPackage, GenerateProtocolPath.protocolPathPeriod(protocol_id));
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class_fold(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.java", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Java protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        var protocol_root_path = StringUtils.format("package {};", protocolPackage);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolManagerTemplate.java");
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.put({}.class, (short){});", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.java"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Java protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolTemplate.java");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocol_root_path
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.java", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Java protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() {
        var rootPackage = StringUtils.format("package {};", protocolPackage);
        var list = List.of("java/IProtocolRegistration.java"
                , "java/ByteBuffer.java");
        for (var fileName : list) {
            // IProtocolRegistration
            var template = ClassUtils.getFileFromClassPathToString(fileName);
            var formatTemplate = CodeTemplatePlaceholder.formatTemplate(template, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, rootPackage
                    , CodeTemplatePlaceholder.protocol_imports, StringUtils.EMPTY
            ));
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "java/")));
            FileUtils.writeStringToFile(createFile, formatTemplate, false);
        }
    }

    private String protocol_class_fold(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolClassFoldTemplate.java");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Java)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolClassTemplate.java");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Java)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("java/ProtocolRegistrationTemplate.java");
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
        var javaBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("import {}.{}.{};", protocolPackage, GenerateProtocolPath.protocolPathPeriod(subProtocolId), protocolName);
            javaBuilder.append(subProtocolPath).append(LS);
        }
        javaBuilder.append(StringUtils.format("import {}.IProtocolRegistration;", protocolPackage)).append(LS);
        javaBuilder.append(StringUtils.format("import {}.ByteBuffer;", protocolPackage)).append(LS);
        return javaBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var javaBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var propertyType = toJavaClassName(field.getGenericType().getTypeName());
            var propertyFullName = StringUtils.format("public {} {};", propertyType, fieldName);
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Java);
            for (var fieldNote : fieldNotes) {
                javaBuilder.append(fieldNote).append(LS);
            }
            javaBuilder.append(propertyFullName).append(LS);
        }
        return javaBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var javaBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            javaBuilder.append("int beforeWriteIndex = buffer.writeOffset();").append(LS);
            javaBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            javaBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            javaSerializer(fieldRegistration.serializer()).writeObject(javaBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            javaBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return javaBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var javaBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                javaBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = javaSerializer(fieldRegistration.serializer()).readObject(javaBuilder, 1, field, fieldRegistration);
                javaBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                javaBuilder.append("}").append(LS);
                continue;
            }
            var readObject = javaSerializer(fieldRegistration.serializer()).readObject(javaBuilder, 0, field, fieldRegistration);
            javaBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return javaBuilder.toString();
    }

    public static String toJavaClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);
        return typeName;
    }

}
