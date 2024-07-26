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

package com.zfoo.protocol.serializer.dart;

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
public class CodeGenerateDart implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateDart.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoodart";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IDartSerializer> dartSerializerMap = new HashMap<>();

    public static IDartSerializer dartSerializer(ISerializer serializer) {
        return dartSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        dartSerializerMap.put(BoolSerializer.INSTANCE, new DartBoolSerializer());
        dartSerializerMap.put(ByteSerializer.INSTANCE, new DartByteSerializer());
        dartSerializerMap.put(ShortSerializer.INSTANCE, new DartShortSerializer());
        dartSerializerMap.put(IntSerializer.INSTANCE, new DartIntSerializer());
        dartSerializerMap.put(LongSerializer.INSTANCE, new DartLongSerializer());
        dartSerializerMap.put(FloatSerializer.INSTANCE, new DartFloatSerializer());
        dartSerializerMap.put(DoubleSerializer.INSTANCE, new DartDoubleSerializer());
        dartSerializerMap.put(StringSerializer.INSTANCE, new DartStringSerializer());
        dartSerializerMap.put(ArraySerializer.INSTANCE, new DartArraySerializer());
        dartSerializerMap.put(ListSerializer.INSTANCE, new DartListSerializer());
        dartSerializerMap.put(SetSerializer.INSTANCE, new DartSetSerializer());
        dartSerializerMap.put(MapSerializer.INSTANCE, new DartMapSerializer());
        dartSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new DartObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolManagerTemplate.dart");
        var protocol_imports_manager = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports_manager.append("import 'Protocols.dart';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap[{}] = {};", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports_manager.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.dart"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Dart protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_imports_protocols = new StringBuilder();
        protocol_imports_protocols.append("import './IByteBuffer.dart';").append(LS);
        protocol_imports_protocols.append("import './IProtocolRegistration.dart';").append(LS);
        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            protocol_class.append(protocol_class(registration)).append(LS);
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolsTemplate.dart");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports_protocols.toString()
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.dart", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Dart protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolManagerTemplate.dart");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import '{}/{}.dart';", GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap[{}] = {};", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.dart"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Dart protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolTemplate.dart");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.dart", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Dart protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolManagerTemplate.dart");
        var protocol_manager_registrations = new StringBuilder();
        var protocol_imports = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import '{}.dart';", protocol_name, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}Registration();", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap[{}] = {};", protocol_name, protocol_id)).append(LS);
        }

        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.dart"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Dart protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolTemplate.dart");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_imports, protocol_imports_default(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.dart", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Dart protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("dart/IProtocolRegistration.dart", "dart/IByteBuffer.dart", "dart/ByteBuffer.dart");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "dart/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolClassTemplate.dart");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Dart)
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
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("dart/ProtocolRegistrationTemplate.dart");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_imports_default(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var importBuilder = new StringBuilder();
        importBuilder.append("import 'IProtocolRegistration.dart';").append(LS);
        importBuilder.append("import 'IByteBuffer.dart';").append(LS);
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("import '{}.dart';", protocolName);
            importBuilder.append(subProtocolPath).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_imports_fold(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var importBuilder = new StringBuilder();
        var protocolPath = GenerateProtocolPath.protocolPathPeriod(protocolId);
        var splits = protocolPath.split(StringUtils.PERIOD_REGEX);
        importBuilder.append(StringUtils.format("import '{}IProtocolRegistration.dart';", "../".repeat(splits.length))).append(LS);
        importBuilder.append(StringUtils.format("import '{}IByteBuffer.dart';", "../".repeat(splits.length))).append(LS);
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.relativePath(protocolId, subProtocolId);
            var subProtocolPath = StringUtils.format("import '{}/{}.dart';", path, protocolName);
            importBuilder.append(subProtocolPath).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var dartBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Dart);
            for (var fieldNote : fieldNotes) {
                dartBuilder.append(fieldNote).append(LS);
            }
            var fieldTypeDefaultValue = dartSerializer(fieldRegistration.serializer()).fieldTypeDefaultValue(field, fieldRegistration);
            var fieldType = fieldTypeDefaultValue.getKey();
            var fieldDefaultValue = fieldTypeDefaultValue.getValue();
            dartBuilder.append(StringUtils.format("{} {} = {};", fieldType, fieldName, fieldDefaultValue)).append(LS);
        }
        return dartBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var dartBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            dartBuilder.append("var beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            dartBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            dartBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            dartSerializer(fieldRegistration.serializer()).writeObject(dartBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            dartBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return dartBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var dartBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                dartBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = dartSerializer(fieldRegistration.serializer()).readObject(dartBuilder, 1, field, fieldRegistration);
                dartBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                dartBuilder.append("}").append(LS);
                continue;
            }
            var readObject = dartSerializer(fieldRegistration.serializer()).readObject(dartBuilder, 0, field, fieldRegistration);
            dartBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return dartBuilder.toString();
    }

    public static String toDartClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "bool";
                return typeName;
            case "byte":
            case "Byte":
            case "short":
            case "Short":
            case "int":
            case "Integer":
            case "long":
            case "Long":
                typeName = "int";
                return typeName;
            case "float":
            case "Float":
            case "double":
            case "Double":
                typeName = "double";
                return typeName;
            case "char":
            case "Character":
            case "String":
                typeName = "String";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "bool");
        typeName = typeName.replace("<Boolean", "<bool");
        typeName = typeName.replace("Boolean>", "bool>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "int");
        typeName = typeName.replace("Byte>", "int>");
        typeName = typeName.replace("<Byte", "<int");

        // 将Short转为short
        typeName = typeName.replace("Short[", "int");
        typeName = typeName.replace("Short>", "int>");
        typeName = typeName.replace("<Short", "<int");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "int");
        typeName = typeName.replace("Integer>", "int>");
        typeName = typeName.replace("<Integer", "<int");

        // 将Long转为long
        typeName = typeName.replace("Long[", "int");
        typeName = typeName.replace("Long>", "int>");
        typeName = typeName.replace("<Long", "<int");

        // 将Float转为float
        typeName = typeName.replace("Float[", "double");
        typeName = typeName.replace("Float>", "double>");
        typeName = typeName.replace("<Float", "<double");

        // 将Double转为double
        typeName = typeName.replace("Double[", "double");
        typeName = typeName.replace("Double>", "double>");
        typeName = typeName.replace("<Double", "<double");

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
