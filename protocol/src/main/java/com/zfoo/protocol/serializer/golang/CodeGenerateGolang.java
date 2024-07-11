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

package com.zfoo.protocol.serializer.golang;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CodeTemplatePlaceholder;
import com.zfoo.protocol.serializer.ICodeGenerate;
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
public class CodeGenerateGolang implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateGolang.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfoogo";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IGoSerializer> goSerializerMap = new HashMap<>();

    public static IGoSerializer goSerializer(ISerializer serializer) {
        return goSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        goSerializerMap.put(BoolSerializer.INSTANCE, new GoBoolSerializer());
        goSerializerMap.put(ByteSerializer.INSTANCE, new GoByteSerializer());
        goSerializerMap.put(ShortSerializer.INSTANCE, new GoShortSerializer());
        goSerializerMap.put(IntSerializer.INSTANCE, new GoIntSerializer());
        goSerializerMap.put(LongSerializer.INSTANCE, new GoLongSerializer());
        goSerializerMap.put(FloatSerializer.INSTANCE, new GoFloatSerializer());
        goSerializerMap.put(DoubleSerializer.INSTANCE, new GoDoubleSerializer());
        goSerializerMap.put(StringSerializer.INSTANCE, new GoStringSerializer());
        goSerializerMap.put(ArraySerializer.INSTANCE, new GoArraySerializer());
        goSerializerMap.put(ListSerializer.INSTANCE, new GoListSerializer());
        goSerializerMap.put(SetSerializer.INSTANCE, new GoSetSerializer());
        goSerializerMap.put(MapSerializer.INSTANCE, new GoMapSerializer());
        goSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new GoObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        createProtocolManagerFile(registrations);


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("golang/ProtocolsTemplate.go");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.go", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Golang protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        defaultProtocol(registrations);
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();
        createProtocolManagerFile(registrations);


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("golang/ProtocolTemplate.go");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));

            var outputPath = StringUtils.format("{}/{}.go", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Golang protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("golang/ByteBuffer.go");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "golang/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private void createProtocolManagerFile(List<ProtocolRegistration> registrations) throws IOException {
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("golang/ProtocolManagerTemplate.go");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("Protocols[{}] = new({})", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.go"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Golang protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolClassTemplate = ClassUtils.getFileFromClassPathToString("golang/ProtocolClassTemplate.go");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Golang)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolClassTemplate, placeholderMap);
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolRegistrationTemplate = ArrayUtils.isEmpty(registration.getFields())
                ? ClassUtils.getFileFromClassPathToString("golang/ProtocolRegistrationTemplateEmpty.go")
                : ClassUtils.getFileFromClassPathToString("golang/ProtocolRegistrationTemplate.go");

        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Golang)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolRegistrationTemplate, placeholderMap);
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
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
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, field.getName(), CodeLanguage.Golang);
            for (var fieldNote : fieldNotes) {
                goBuilder.append(fieldNote).append(LS);
            }
            goBuilder.append(propertyFullName).append(LS);
        }
        return goBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            goBuilder.append("var beforeWriteIndex = buffer.WriteOffset()").append(LS);
            goBuilder.append(StringUtils.format("buffer.WriteInt({})", registration.getPredictionLength())).append(LS);
        } else {
            goBuilder.append("buffer.WriteInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            goSerializer(fieldRegistration.serializer()).writeObject(goBuilder, "message." + StringUtils.capitalize(field.getName()), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            goBuilder.append(StringUtils.format("buffer.AdjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return goBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                goBuilder.append("if buffer.CompatibleRead(beforeReadIndex, length) {").append(LS);
                var compatibleReadObject = goSerializer(fieldRegistration.serializer()).readObject(goBuilder, 1, field, fieldRegistration);
                goBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", StringUtils.capitalize(field.getName()), compatibleReadObject)).append(LS);
                goBuilder.append("}").append(LS);
                continue;
            }
            var readObject = goSerializer(fieldRegistration.serializer()).readObject(goBuilder, 0, field, fieldRegistration);
            goBuilder.append(StringUtils.format("packet.{} = {}", StringUtils.capitalize(field.getName()), readObject)).append(LS);
        }
        return goBuilder.toString();
    }

}
