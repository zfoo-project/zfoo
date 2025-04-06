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

package com.zfoo.protocol.serializer.gdscript;

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
import com.zfoo.protocol.serializer.typescript.CodeGenerateTypeScript;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB_ASCII;

/**
 * @author godotg
 */
public class CodeGenerateGdScript implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateGdScript.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoogd";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IGdSerializer> gdSerializerMap = new HashMap<>();

    public static IGdSerializer gdSerializer(ISerializer serializer) {
        return gdSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        gdSerializerMap.put(BoolSerializer.INSTANCE, new GdBoolSerializer());
        gdSerializerMap.put(ByteSerializer.INSTANCE, new GdByteSerializer());
        gdSerializerMap.put(ShortSerializer.INSTANCE, new GdShortSerializer());
        gdSerializerMap.put(IntSerializer.INSTANCE, new GdIntSerializer());
        gdSerializerMap.put(LongSerializer.INSTANCE, new GdLongSerializer());
        gdSerializerMap.put(FloatSerializer.INSTANCE, new GdFloatSerializer());
        gdSerializerMap.put(DoubleSerializer.INSTANCE, new GdDoubleSerializer());
        gdSerializerMap.put(StringSerializer.INSTANCE, new GdStringSerializer());
        gdSerializerMap.put(ArraySerializer.INSTANCE, new GdArraySerializer());
        gdSerializerMap.put(ListSerializer.INSTANCE, new GdListSerializer());
        gdSerializerMap.put(SetSerializer.INSTANCE, new GdSetSerializer());
        gdSerializerMap.put(MapSerializer.INSTANCE, new GdMapSerializer());
        gdSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new GdObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.gd文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolManagerTemplate.gd");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("const Protocols = preload(\"./Protocols.gd\")").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = Protocols.{}Registration.new()", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolClassMap[{}] = Protocols.{}", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, StringUtils.substringBeforeLast(protocol_manager_registrations.toString(), StringUtils.COMMA));
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.gd"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated GdScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        protocol_imports = new StringBuilder();
        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        protocol_imports.append("const ByteBuffer = preload(\"./ByteBuffer.gd\")").append(LS);
        for (var registration : registrations) {
            protocol_class.append(protocol_class(registration)).append(LS).append(LS);
            protocol_registration.append(protocol_registration(registration)).append(LS).append(LS).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolsTemplate.gd");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.gd", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated GdScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.gd文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolManagerTemplate.gd");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("const {} = preload(\"./{}/{}.gd\")", protocol_name, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}.{}Registration.new()", protocol_id, protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolClassMap[{}] = {}", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, StringUtils.substringBeforeLast(protocol_manager_registrations.toString(), StringUtils.COMMA));
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.gd"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated GdScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolTemplate.gd");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class_default(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.gd", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated GdScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.gd文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolManagerTemplate.gd");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("const ByteBuffer = preload(\"./ByteBuffer.gd\")").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("const {} = preload(\"./{}.gd\")", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}.{}Registration.new()", protocol_id, protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolClassMap[{}] = {}", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, StringUtils.substringBeforeLast(protocol_manager_registrations.toString(), StringUtils.COMMA));
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.gd"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated GdScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolTemplate.gd");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_default(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class_default(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.gd", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated GdScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ByteBuffer.gd"));
        var fileInputStream = ClassUtils.getFileFromClassPath("gdscript/ByteBuffer.gd");
        FileUtils.writeInputStreamToFile(createFile, fileInputStream);
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolClassTemplate.gd");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.GdScript)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_json, protocol_json(registration)
                , CodeTemplatePlaceholder.protocol_to_string, protocol_to_string(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_class_default(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolClassDefaultTemplate.gd");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.GdScript)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_json, protocol_json(registration)
                , CodeTemplatePlaceholder.protocol_to_string, protocol_to_string(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolRegistrationTemplate.gd");
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
        var importBuilder = new StringBuilder();
        var protocolPath = GenerateProtocolPath.protocolPathPeriod(protocolId);
        var splits = protocolPath.split(StringUtils.PERIOD_REGEX);
        importBuilder.append(StringUtils.format("const ByteBuffer = preload(\"{}ByteBuffer.gd\")", "../".repeat(splits.length))).append(LS);
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.relativePath(protocolId, subProtocolId);
            importBuilder.append(StringUtils.format("const {} = preload(\"{}/{}.gd\")", protocolName, path, protocolName)).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_imports_default(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var importBuilder = new StringBuilder();
        importBuilder.append("const ByteBuffer = preload(\"./ByteBuffer.gd\")").append(LS);
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            importBuilder.append(StringUtils.format("const {} = preload(\"./{}.gd\")", protocolName, protocolName)).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_json(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        gdBuilder.append("{");
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var params = new ArrayList<String>();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var fieldType = gdSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);
            if (fieldType.equals("String")) {
                params.add(StringUtils.format("{}:'{}'", fieldName));
            } else {
                params.add(StringUtils.format("{}:{}", fieldName));
            }
        }
        gdBuilder.append(StringUtils.joinWith(", ", params.toArray()));
        gdBuilder.append("}");
        return gdBuilder.toString();
    }

    private String protocol_to_string(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var params = new ArrayList<String>();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var fieldType = gdSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);
            if (fieldType.equals("Dictionary") || fieldType.startsWith("Array")) {
                params.add(StringUtils.format("JSON.stringify(self.{})", field.getName()));
            } else {
                params.add(StringUtils.format("self.{}", field.getName()));
            }
        }
        gdBuilder.append(StringUtils.joinWith(", ", params.toArray()));
        return gdBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        // 生成源代码字段的时候，按照原始定义的方式生成
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.GdScript);
            for (var fieldNote : fieldNotes) {
                gdBuilder.append(fieldNote).append(LS);
            }
            var fieldType = gdSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);
            // 生成类型的注释
            gdBuilder.append(StringUtils.format("var {}: {}", fieldName, fieldType));
            if (fieldType.equals("Dictionary") || fieldType.equals("Array")) {
                var typeNote = CodeGenerateTypeScript.toTsClassName(field.getGenericType().toString());
                gdBuilder.append(StringUtils.format(TAB_ASCII + "# {}", typeNote));
            }
            gdBuilder.append(LS);
        }
        return gdBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            gdBuilder.append("var beforeWriteIndex = buffer.getWriteOffset()").append(LS);
            gdBuilder.append(StringUtils.format("buffer.writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            gdBuilder.append("buffer.writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            gdSerializer(fieldRegistration.serializer()).writeObject(gdBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            gdBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return gdBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                gdBuilder.append("if buffer.compatibleRead(beforeReadIndex, length):").append(LS);
                var compatibleReadObject = gdSerializer(fieldRegistration.serializer()).readObject(gdBuilder, 1, field, fieldRegistration);
                gdBuilder.append(TAB_ASCII).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                continue;
            }
            var readObject = gdSerializer(fieldRegistration.serializer()).readObject(gdBuilder, 0, field, fieldRegistration);
            gdBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return gdBuilder.toString();
    }

}
