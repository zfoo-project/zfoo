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

package com.zfoo.protocol.serializer.python;

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
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public class CodeGeneratePython implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratePython.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoopy";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IPySerializer> pySerializerMap = new HashMap<>();

    public static IPySerializer pySerializer(ISerializer serializer) {
        return pySerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        pySerializerMap.put(BooleanSerializer.INSTANCE, new PyBooleanSerializer());
        pySerializerMap.put(ByteSerializer.INSTANCE, new PyByteSerializer());
        pySerializerMap.put(ShortSerializer.INSTANCE, new PyShortSerializer());
        pySerializerMap.put(IntSerializer.INSTANCE, new PyIntSerializer());
        pySerializerMap.put(LongSerializer.INSTANCE, new PyLongSerializer());
        pySerializerMap.put(FloatSerializer.INSTANCE, new PyFloatSerializer());
        pySerializerMap.put(DoubleSerializer.INSTANCE, new PyDoubleSerializer());
        pySerializerMap.put(StringSerializer.INSTANCE, new PyStringSerializer());
        pySerializerMap.put(ArraySerializer.INSTANCE, new PyArraySerializer());
        pySerializerMap.put(ListSerializer.INSTANCE, new PyListSerializer());
        pySerializerMap.put(SetSerializer.INSTANCE, new PySetSerializer());
        pySerializerMap.put(MapSerializer.INSTANCE, new PyMapSerializer());
        pySerializerMap.put(ObjectProtocolSerializer.INSTANCE, new PyObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolManagerTemplate.py");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("from . import Protocols").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = Protocols.{}", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.py"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Python protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_class.append(formatProtocolTemplate(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolsTemplate.py");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.py", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Python protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());

    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolManagerTemplate.py");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("from .{} import {}", GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}.{}", protocol_id, protocol_name, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.py"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Python protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}/{}.py", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Python protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolManagerTemplate.py");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("from . import {}", protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}.{}", protocol_id, protocol_name, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.py"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Python protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}.py", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Python protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("python/ByteBuffer.py");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "python/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    public String formatProtocolTemplate(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolTemplate.py");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Python)
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
        var pyBuilder = new StringBuilder();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Python);
            for (var fieldNote : fieldNotes) {
                pyBuilder.append(fieldNote).append(LS);
            }
            var fieldDefaultValue = pySerializer(fieldRegistration.serializer()).fieldDefaultValue(field, fieldRegistration);
            // 生成类型的注释
            pyBuilder.append(StringUtils.format("{} = {}", fieldName, fieldDefaultValue));
            pyBuilder.append(StringUtils.format("  # {}", CodeGenerateCsharp.toCsClassName(field.getGenericType().getTypeName())));
            pyBuilder.append(LS);
        }
        return pyBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var pyBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            pyBuilder.append("beforeWriteIndex = buffer.getWriteOffset()").append(LS);
            pyBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            pyBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            pySerializer(fieldRegistration.serializer()).writeObject(pyBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            pyBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return pyBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var pyBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                pyBuilder.append("if buffer.compatibleRead(beforeReadIndex, length):").append(LS);
                var compatibleReadObject = pySerializer(fieldRegistration.serializer()).readObject(pyBuilder, 1, field, fieldRegistration);
                pyBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                continue;
            }
            var readObject = pySerializer(fieldRegistration.serializer()).readObject(pyBuilder, 0, field, fieldRegistration);
            pyBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return pyBuilder.toString();
    }

}
