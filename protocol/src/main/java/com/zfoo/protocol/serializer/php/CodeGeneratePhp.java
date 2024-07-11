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

package com.zfoo.protocol.serializer.php;

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
public class CodeGeneratePhp implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGeneratePhp.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoophp";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IPhpSerializer> phpSerializerMap = new HashMap<>();

    public static IPhpSerializer phpSerializer(ISerializer serializer) {
        return phpSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        phpSerializerMap.put(BooleanSerializer.INSTANCE, new PhpBooleanSerializer());
        phpSerializerMap.put(ByteSerializer.INSTANCE, new PhpByteSerializer());
        phpSerializerMap.put(ShortSerializer.INSTANCE, new PhpShortSerializer());
        phpSerializerMap.put(IntSerializer.INSTANCE, new PhpIntSerializer());
        phpSerializerMap.put(LongSerializer.INSTANCE, new PhpLongSerializer());
        phpSerializerMap.put(FloatSerializer.INSTANCE, new PhpFloatSerializer());
        phpSerializerMap.put(DoubleSerializer.INSTANCE, new PhpDoubleSerializer());
        phpSerializerMap.put(StringSerializer.INSTANCE, new PhpStringSerializer());
        phpSerializerMap.put(ArraySerializer.INSTANCE, new PhpArraySerializer());
        phpSerializerMap.put(ListSerializer.INSTANCE, new PhpListSerializer());
        phpSerializerMap.put(SetSerializer.INSTANCE, new PhpSetSerializer());
        phpSerializerMap.put(MapSerializer.INSTANCE, new PhpMapSerializer());
        phpSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new PhpObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolManagerTemplate.php");
        var protocol_imports_manager = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports_manager.append("include_once 'Protocols.php';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("self::$protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("self::$protocolIdMap[{}::class] = {};", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports_manager.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.php"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Php protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolTemplate.php");
        for (var registration : registrations) {
            protocol_class.append(protocol_class(registration)).append(LS);
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.php", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Php protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolManagerTemplate.php");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("include_once '{}/{}.php';", GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("self::$protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("self::$protocolIdMap[{}::class] = {};", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.php"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Php protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolTemplate.php");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.php", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Php protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolManagerTemplate.php");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("include_once '{}.php';", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("self::$protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("self::$protocolIdMap[{}::class] = {};", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.php"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Php protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolTemplate.php");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.php", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Php protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("php/IProtocolRegistration.php", "php/ByteBuffer.php");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "php/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolClassTemplate.php");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Php)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("php/ProtocolRegistrationTemplate.php");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var phpBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Php);
            for (var fieldNote : fieldNotes) {
                phpBuilder.append(fieldNote).append(LS);
            }
            var pair = phpSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            phpBuilder.append(StringUtils.format("var {} ${} = {};", pair.getKey(), fieldName, pair.getValue())).append(LS);
        }
        return phpBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var phpBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            phpBuilder.append("$beforeWriteIndex = $buffer->getWriteOffset();").append(LS);
            phpBuilder.append(StringUtils.format("$buffer->writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            phpBuilder.append("$buffer->writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            phpSerializer(fieldRegistration.serializer()).writeObject(phpBuilder, "$packet->" + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            phpBuilder.append(StringUtils.format("$buffer->adjustPadding({}, $beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return phpBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var phpBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                phpBuilder.append("if ($buffer->compatibleRead($beforeReadIndex, $length)) {").append(LS);
                var compatibleReadObject = phpSerializer(fieldRegistration.serializer()).readObject(phpBuilder, 1, field, fieldRegistration);
                phpBuilder.append(TAB).append(StringUtils.format("$packet->{} = {};", field.getName(), compatibleReadObject)).append(LS);
                phpBuilder.append("}").append(LS);
                continue;
            }
            var readObject = phpSerializer(fieldRegistration.serializer()).readObject(phpBuilder, 0, field, fieldRegistration);
            phpBuilder.append(StringUtils.format("$packet->{} = {};", field.getName(), readObject)).append(LS);
        }
        return phpBuilder.toString();
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
