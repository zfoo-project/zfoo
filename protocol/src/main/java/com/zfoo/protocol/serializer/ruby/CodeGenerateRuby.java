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

package com.zfoo.protocol.serializer.ruby;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.CodeTemplatePlaceholder;
import com.zfoo.protocol.serializer.ICodeGenerate;
import com.zfoo.protocol.serializer.csharp.CodeGenerateCsharp;
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

/**
 * @author godotg
 */
public class CodeGenerateRuby implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateRuby.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfooruby";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IRubySerializer> rbSerializerMap = new HashMap<>();

    public static IRubySerializer rbSerializer(ISerializer serializer) {
        return rbSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        rbSerializerMap.put(BoolSerializer.INSTANCE, new RubyBoolSerializer());
        rbSerializerMap.put(ByteSerializer.INSTANCE, new RubyByteSerializer());
        rbSerializerMap.put(ShortSerializer.INSTANCE, new RubyShortSerializer());
        rbSerializerMap.put(IntSerializer.INSTANCE, new RubyIntSerializer());
        rbSerializerMap.put(LongSerializer.INSTANCE, new RubyLongSerializer());
        rbSerializerMap.put(FloatSerializer.INSTANCE, new RubyFloatSerializer());
        rbSerializerMap.put(DoubleSerializer.INSTANCE, new RubyDoubleSerializer());
        rbSerializerMap.put(StringSerializer.INSTANCE, new RubyStringSerializer());
        rbSerializerMap.put(ArraySerializer.INSTANCE, new RubyArraySerializer());
        rbSerializerMap.put(ListSerializer.INSTANCE, new RubyListSerializer());
        rbSerializerMap.put(SetSerializer.INSTANCE, new RubySetSerializer());
        rbSerializerMap.put(MapSerializer.INSTANCE, new RubyMapSerializer());
        rbSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new RubyObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolManagerTemplate.rb");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("require_relative 'Protocols.rb'").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("@@protocols[{}] = {}Registration.new()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("@@protocolIdMap[{}] = {}", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.rb"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Ruby protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_class.append(protocol_class(registration)).append(LS);
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolsTemplate.rb");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.rb", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Ruby protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());

    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolManagerTemplate.rb");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("require_relative '{}/{}.rb'", GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("@@protocols[{}] = {}Registration.new()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("@@protocolIdMap[{}] = {}", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.rb"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Ruby protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolTemplate.rb");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.rb", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Ruby protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolManagerTemplate.rb");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("require_relative '{}.rb'", protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("@@protocols[{}] = {}Registration.new()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("@@protocolIdMap[{}] = {}", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.rb"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Ruby protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolTemplate.rb");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.rb", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Ruby protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("ruby/ByteBuffer.rb");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "ruby/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    public String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolClassTemplate.rb");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Ruby)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_accessor, protocol_field_accessor(registration)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, placeholderMap);
    }

    public String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ruby/ProtocolRegistrationTemplate.rb");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Ruby)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, placeholderMap);
    }

    private String protocol_field_accessor(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var rbBuilder = new StringBuilder();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (var field : sequencedFields) {
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Ruby);
            for (var fieldNote : fieldNotes) {
                rbBuilder.append(fieldNote).append(LS);
            }
            // 生成类型的注释
            rbBuilder.append(StringUtils.format("attr_accessor :{}", fieldName));
            rbBuilder.append(StringUtils.format("  # {}", CodeGenerateCsharp.toCsClassName(field.getGenericType().getTypeName())));
            rbBuilder.append(LS);
        }
        return rbBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var rbBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var fieldDefaultValue = rbSerializer(fieldRegistration.serializer()).fieldDefaultValue(field, fieldRegistration);
            rbBuilder.append(StringUtils.format("@{} = {}", fieldName, fieldDefaultValue));
            rbBuilder.append(LS);
        }
        return rbBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var rbBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            rbBuilder.append("beforeWriteIndex = buffer.getWriteOffset()").append(LS);
            rbBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            rbBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            rbSerializer(fieldRegistration.serializer()).writeObject(rbBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            rbBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return rbBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var rbBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                rbBuilder.append("if buffer.compatibleRead(beforeReadIndex, length)").append(LS);
                var compatibleReadObject = rbSerializer(fieldRegistration.serializer()).readObject(rbBuilder, 1, field, fieldRegistration);
                GenerateProtocolFile.addTabWith2Space(rbBuilder, 1);
                rbBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                rbBuilder.append("end").append(LS);
                continue;
            }
            var readObject = rbSerializer(fieldRegistration.serializer()).readObject(rbBuilder, 0, field, fieldRegistration);
            rbBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return rbBuilder.toString();
    }

}
