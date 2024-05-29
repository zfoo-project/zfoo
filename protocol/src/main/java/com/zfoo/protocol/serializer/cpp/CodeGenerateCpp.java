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

package com.zfoo.protocol.serializer.cpp;

import com.zfoo.protocol.ProtocolManager;
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
public class CodeGenerateCpp implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateCpp.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoocpp";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, ICppSerializer> cppSerializerMap = new HashMap<>();

    public static ICppSerializer cppSerializer(ISerializer serializer) {
        return cppSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        cppSerializerMap.put(BooleanSerializer.INSTANCE, new CppBooleanSerializer());
        cppSerializerMap.put(ByteSerializer.INSTANCE, new CppByteSerializer());
        cppSerializerMap.put(ShortSerializer.INSTANCE, new CppShortSerializer());
        cppSerializerMap.put(IntSerializer.INSTANCE, new CppIntSerializer());
        cppSerializerMap.put(LongSerializer.INSTANCE, new CppLongSerializer());
        cppSerializerMap.put(FloatSerializer.INSTANCE, new CppFloatSerializer());
        cppSerializerMap.put(DoubleSerializer.INSTANCE, new CppDoubleSerializer());
        cppSerializerMap.put(StringSerializer.INSTANCE, new CppStringSerializer());
        cppSerializerMap.put(ArraySerializer.INSTANCE, new CppArraySerializer());
        cppSerializerMap.put(ListSerializer.INSTANCE, new CppListSerializer());
        cppSerializerMap.put(SetSerializer.INSTANCE, new CppSetSerializer());
        cppSerializerMap.put(MapSerializer.INSTANCE, new CppMapSerializer());
        cppSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new CppObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolManagerTemplate.h");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append(StringUtils.format("#include \"{}/Protocols.h\"", protocolOutputRootPath)).append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.h"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated C++ protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : GenerateProtocolFile.subProtocolFirst(registrations)) {
            var protocol_id = registration.protocolId();
            // protocol
            protocol_class.append(protocol_class(registration)).append(LS);
            // registration
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolsTemplate.h");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.h", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated C++ protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolManagerTemplate.h");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("#include \"{}/{}/{}.h\"", protocolOutputRootPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.h"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated C++ protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolTemplate.h");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}/{}.h", protocolOutputPath, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated C++ protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolManagerTemplate.h");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("#include \"{}/{}.h\"", protocolOutputRootPath, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = new {}Registration();", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.h"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated C++ protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolTemplate.h");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_default(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.h", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated C++ protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }


    public void createTemplateFile() throws IOException {
        var list = List.of("cpp/ByteBuffer.h");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "cpp/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolClassTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolClassTemplate.h");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Cpp)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolClassTemplate, placeholderMap);
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolRegistrationTemplate = ClassUtils.getFileFromClassPathToString("cpp/ProtocolRegistrationTemplate.h");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolRegistrationTemplate, placeholderMap);
    }


    private String protocol_imports_fold(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var cppBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("#include \"{}/{}/{}.h\"", protocolOutputRootPath, GenerateProtocolPath.capitalizeProtocolPathFold(subProtocolId), protocolClassName);
            cppBuilder.append(subProtocolPath).append(LS);
        }
        return cppBuilder.toString();
    }

    private String protocol_imports_default(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        var cppBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var subProtocolPath = StringUtils.format("#include \"{}/{}.h\"", protocolOutputRootPath, protocolClassName);
            cppBuilder.append(subProtocolPath).append(LS);
        }
        return cppBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var cppBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var propertyTypeAndName = cppSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            var propertyType = propertyTypeAndName.getKey();
            var propertyName = propertyTypeAndName.getValue();

            var propertyFullName = StringUtils.format("{} {};", propertyType, propertyName);
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Cpp);
            for (var fieldNote : fieldNotes) {
                cppBuilder.append(fieldNote).append(LS);
            }
            cppBuilder.append(propertyFullName).append(LS);
        }

        return cppBuilder.toString().trim();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var cppBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            cppBuilder.append("auto beforeWriteIndex = buffer.writerIndex();").append(LS);
            cppBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            cppBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var serializer = cppSerializer(fieldRegistration.serializer());
            if (ProtocolManager.isProtocolClass(field.getType())) {
                serializer.writeObject(cppBuilder, "&message->" + field.getName(), 0, field, fieldRegistration);
            } else {
                serializer.writeObject(cppBuilder, "message->" + field.getName(), 0, field, fieldRegistration);
            }
        }
        if (registration.isCompatible()) {
            cppBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return cppBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var cppBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                cppBuilder.append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = cppSerializer(fieldRegistration.serializer()).readObject(cppBuilder, 1, field, fieldRegistration);
                cppBuilder.append(TAB);
                if (ProtocolManager.isProtocolClass(field.getType())) {
                    cppBuilder.append(StringUtils.format("packet->{} = *{};", field.getName(), compatibleReadObject));
                } else {
                    cppBuilder.append(StringUtils.format("packet->{} = {};", field.getName(), compatibleReadObject));
                }
                cppBuilder.append(LS).append("}").append(LS);
                continue;
            }

            var readObject = cppSerializer(fieldRegistration.serializer()).readObject(cppBuilder, 0, field, fieldRegistration);
            if (ProtocolManager.isProtocolClass(field.getType())) {
                cppBuilder.append(StringUtils.format("packet->{} = *{};", field.getName(), readObject));
            } else {
                cppBuilder.append(StringUtils.format("packet->{} = {};", field.getName(), readObject));
            }

            cppBuilder.append(LS);
        }
        return cppBuilder.toString();
    }

    public static String toCppClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        // CSharp不适用基础类型的泛型，会影响性能
        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "bool";
                return typeName;
            case "byte":
            case "Byte":
                typeName = "int8_t";
                return typeName;
            case "short":
            case "Short":
                typeName = "int16_t";
                return typeName;
            case "int":
            case "Integer":
                typeName = "int32_t";
                return typeName;
            case "long":
            case "Long":
                typeName = "int64_t";
                return typeName;
            case "Float":
                typeName = "float";
                return typeName;
            case "Double":
                typeName = "double";
                return typeName;
            case "Character":
                typeName = "char";
                return typeName;
            case "String":
                typeName = "string";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replaceAll("[B|b]oolean\\[", "bool");
        typeName = typeName.replace("<Boolean", "<bool");
        typeName = typeName.replace("Boolean>", "bool>");

        // 将Byte转为byte
        typeName = typeName.replace("Byte[", "int8_t");
        typeName = typeName.replace("Byte>", "int8_t>");
        typeName = typeName.replace("<Byte", "<int8_t");

        // 将Short转为short
        typeName = typeName.replace("Short[", "int16_t");
        typeName = typeName.replace("Short>", "int16_t>");
        typeName = typeName.replace("<Short", "<int16_t");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "int32_t");
        typeName = typeName.replace("Integer>", "int32_t>");
        typeName = typeName.replace("<Integer", "<int32_t");


        // 将Long转为long
        typeName = typeName.replace("Long[", "int64_t");
        typeName = typeName.replace("Long>", "int64_t>");
        typeName = typeName.replace("<Long", "<int64_t");

        // 将Float转为float
        typeName = typeName.replace("Float[", "float");
        typeName = typeName.replace("Float>", "float>");
        typeName = typeName.replace("<Float", "<float");

        // 将Double转为double
        typeName = typeName.replace("Double[", "double");
        typeName = typeName.replace("Double>", "double>");
        typeName = typeName.replace("<Double", "<double");

        // 将Character转为Char
        typeName = typeName.replace("Character[", "char");
        typeName = typeName.replace("Character>", "char>");
        typeName = typeName.replace("<Character", "<char");

        // 将String转为string
        typeName = typeName.replace("String[", "string");
        typeName = typeName.replace("String>", "string>");
        typeName = typeName.replace("<String", "<string");

        // 将Map转为map
        typeName = typeName.replace("Map<", "map<");

        // 将Set转为set
        typeName = typeName.replace("Set<", "set<");

        // 将List转为vector
        typeName = typeName.replace("List<", "list<");

        return typeName;
    }
}
