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

package com.zfoo.protocol.serializer.ecmascript;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolRegistration;
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
public class CodeGenerateEcmaScript implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateEcmaScript.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfooes";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IEsSerializer> esSerializerMap = new HashMap<>();


    public static IEsSerializer esSerializer(ISerializer serializer) {
        return esSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        esSerializerMap.put(BooleanSerializer.INSTANCE, new EsBooleanSerializer());
        esSerializerMap.put(ByteSerializer.INSTANCE, new EsByteSerializer());
        esSerializerMap.put(ShortSerializer.INSTANCE, new EsShortSerializer());
        esSerializerMap.put(IntSerializer.INSTANCE, new EsIntSerializer());
        esSerializerMap.put(LongSerializer.INSTANCE, new EsLongSerializer());
        esSerializerMap.put(FloatSerializer.INSTANCE, new EsFloatSerializer());
        esSerializerMap.put(DoubleSerializer.INSTANCE, new EsDoubleSerializer());
        esSerializerMap.put(StringSerializer.INSTANCE, new EsStringSerializer());
        esSerializerMap.put(ArraySerializer.INSTANCE, new EsArraySerializer());
        esSerializerMap.put(ListSerializer.INSTANCE, new EsListSerializer());
        esSerializerMap.put(SetSerializer.INSTANCE, new EsSetSerializer());
        esSerializerMap.put(MapSerializer.INSTANCE, new EsMapSerializer());
        esSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new EsObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolManagerTemplate.mjs");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("import Protocols from './Protocols.mjs';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, Protocols.{});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.mjs"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated ES protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            // protocol
            protocol_class.append(formatProtocolClassTemplate(registration)).append(LS);
            protocol_registration.append(StringUtils.format("Protocols.{} = {}", protocol_name, protocol_name)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolsTemplate.mjs");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.mjs", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated ES protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.mjs文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolManagerTemplate.mjs");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var protocol : registrations) {
            var protocol_id = protocol.protocolId();
            var protocol_name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}/{}.mjs';", protocol_name, GenerateProtocolPath.protocolPathSlash(protocol.protocolId()), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.mjs"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated ES protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}/{}.mjs", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated ES protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.mjs文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolManagerTemplate.mjs");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var protocol : registrations) {
            var protocol_id = protocol.protocolId();
            var protocol_name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}.mjs';", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.mjs"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated ES protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}.mjs", protocolOutputPath, protocol_name, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated ES protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("ecmascript/buffer/ByteBuffer.mjs", "ecmascript/buffer/long.mjs", "ecmascript/buffer/longbits.mjs");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "ecmascript/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    public String formatProtocolTemplate(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolTemplate.mjs");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.EcmaScript)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, placeholderMap);
    }

    public String formatProtocolClassTemplate(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("ecmascript/ProtocolClassTemplate.mjs");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.EcmaScript)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, placeholderMap);
    }


    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var fieldDefinitionBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.EcmaScript);
            for (var fieldNote : fieldNotes) {
                fieldDefinitionBuilder.append(fieldNote).append(LS);
            }
            var triple = esSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(StringUtils.format("{} = {}; // {}", fieldName, triple.getRight(), triple.getLeft()))
                    .append(LS);

        }
        return fieldDefinitionBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var jsBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            jsBuilder.append("const beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            jsBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            jsBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            esSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            jsBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return jsBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var jsBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                jsBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = esSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
                jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                jsBuilder.append("}").append(LS);
                continue;
            }
            var readObject = esSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 0, field, fieldRegistration);
            jsBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return jsBuilder.toString();
    }
}
