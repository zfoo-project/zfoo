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

package com.zfoo.protocol.serializer.javascript;

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
public class CodeGenerateJavaScript implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateJavaScript.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfoojs";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IJsSerializer> jsSerializerMap = new HashMap<>();

    public static IJsSerializer jsSerializer(ISerializer serializer) {
        return jsSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        jsSerializerMap.put(BooleanSerializer.INSTANCE, new JsBooleanSerializer());
        jsSerializerMap.put(ByteSerializer.INSTANCE, new JsByteSerializer());
        jsSerializerMap.put(ShortSerializer.INSTANCE, new JsShortSerializer());
        jsSerializerMap.put(IntSerializer.INSTANCE, new JsIntSerializer());
        jsSerializerMap.put(LongSerializer.INSTANCE, new JsLongSerializer());
        jsSerializerMap.put(FloatSerializer.INSTANCE, new JsFloatSerializer());
        jsSerializerMap.put(DoubleSerializer.INSTANCE, new JsDoubleSerializer());
        jsSerializerMap.put(StringSerializer.INSTANCE, new JsStringSerializer());
        jsSerializerMap.put(ArraySerializer.INSTANCE, new JsArraySerializer());
        jsSerializerMap.put(ListSerializer.INSTANCE, new JsListSerializer());
        jsSerializerMap.put(SetSerializer.INSTANCE, new JsSetSerializer());
        jsSerializerMap.put(MapSerializer.INSTANCE, new JsMapSerializer());
        jsSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new JsObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.js文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolManagerTemplate.js");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append("import Protocols from './Protocols.js';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, Protocols.{});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.js"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Javascript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        protocol_registration.append(LS);
        for (var registration : registrations) {
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_registration.append(StringUtils.format("Protocols.{} = {}", protocol_name, protocol_name)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolsTemplate.js");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.js", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Javascript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.js文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolManagerTemplate.js");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}/{}.js';", protocol_name, GenerateProtocolPath.protocolPathSlash(registration.protocolId()), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.js"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Javascript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolTemplate.js");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.js", protocolOutputPath, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Javascript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.js文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolManagerTemplate.js");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}.js';", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, {});", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.js"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Javascript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolTemplate.js");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.js", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Javascript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("javascript/buffer/ByteBuffer.js", "javascript/buffer/long.js", "javascript/buffer/longbits.js");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "javascript/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolClassTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolClassTemplate.js");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.JavaScript)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolClassTemplate, placeholderMap);
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolRegistrationTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolRegistrationTemplate.js");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolRegistrationTemplate, placeholderMap);
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
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.JavaScript);
            for (var fieldNote : fieldNotes) {
                fieldDefinitionBuilder.append(fieldNote).append(LS);
            }
            var triple = jsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(StringUtils.format("this.{} = {}; // {}", fieldName, triple.getRight(), triple.getLeft()))
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
            jsSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
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
                var compatibleReadObject = jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
                jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                jsBuilder.append("}").append(LS);
                continue;
            }
            var readObject = jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 0, field, fieldRegistration);
            jsBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return jsBuilder.toString();
    }
}
