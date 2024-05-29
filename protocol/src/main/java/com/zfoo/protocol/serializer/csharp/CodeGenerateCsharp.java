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

package com.zfoo.protocol.serializer.csharp;

import com.zfoo.protocol.ProtocolManager;
import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
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
public class CodeGenerateCsharp implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateCsharp.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoocs";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, ICsSerializer> csSerializerMap = new HashMap<>();

    public static ICsSerializer csSerializer(ISerializer serializer) {
        return csSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        csSerializerMap.put(BooleanSerializer.INSTANCE, new CsBooleanSerializer());
        csSerializerMap.put(ByteSerializer.INSTANCE, new CsByteSerializer());
        csSerializerMap.put(ShortSerializer.INSTANCE, new CsShortSerializer());
        csSerializerMap.put(IntSerializer.INSTANCE, new CsIntSerializer());
        csSerializerMap.put(LongSerializer.INSTANCE, new CsLongSerializer());
        csSerializerMap.put(FloatSerializer.INSTANCE, new CsFloatSerializer());
        csSerializerMap.put(DoubleSerializer.INSTANCE, new CsDoubleSerializer());
        csSerializerMap.put(StringSerializer.INSTANCE, new CsStringSerializer());
        csSerializerMap.put(ArraySerializer.INSTANCE, new CsArraySerializer());
        csSerializerMap.put(ListSerializer.INSTANCE, new CsListSerializer());
        csSerializerMap.put(SetSerializer.INSTANCE, new CsSetSerializer());
        csSerializerMap.put(MapSerializer.INSTANCE, new CsMapSerializer());
        csSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new CsObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createProtocolManagerFile(registrations);


        var mergerProtocolPathMap = GenerateProtocolPath.mergerProtocolPathMap();
        for (var entry : mergerProtocolPathMap.entrySet()) {
            var protocol_merger_name = StringUtils.capitalize(entry.getKey());
            var protocolIds = entry.getValue().stream().sorted().toList();
            var protocol_class = new StringBuilder();
            var protocol_registration = new StringBuilder();
            for (var protocol_id : protocolIds) {
                var registration = (ProtocolRegistration) ProtocolManager.getProtocol(protocol_id);
                // protocol
                protocol_class.append(protocol_class(registration)).append(LS);

                // registration
                protocol_registration.append(protocol_registration(registration)).append(LS);
            }
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolTemplate.cs");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
            ));
            var outputPath = StringUtils.format("{}/{}.cs", protocolOutputPath, protocol_merger_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated C# protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createProtocolManagerFile(registrations);


        for (var registration : registrations) {
            var protocolId = registration.protocolId();
            var protocolClazzName = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}/{}.cs", protocolOutputPath, GenerateProtocolPath.capitalizeProtocolPathFold(protocolId), protocolClazzName);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated C# protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createProtocolManagerFile(registrations);


        for (var registration : registrations) {
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}.cs", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated C# protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createProtocolManagerFile(List<ProtocolRegistration> registrations) throws IOException {
        var list = List.of("csharp/IProtocolRegistration.cs"
                , "csharp/Buffer/ByteBuffer.cs"
                , "csharp/Buffer/LittleEndianByteBuffer.cs"
                , "csharp/Buffer/BigEndianByteBuffer.cs");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "csharp/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolManagerTemplate.cs");
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations(registrations));
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);

        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.cs"));
        FileUtils.writeStringToFile(file, formatProtocolManagerTemplate, true);
        logger.info("Generated C# protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }


    private String formatProtocolTemplate(ProtocolRegistration registration) {
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolTemplate.cs");
        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
        ));
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolClassTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolClassTemplate.cs");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.CSharp)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolClassTemplate, placeholderMap);
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolRegistrationTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolRegistrationTemplate.cs");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.CSharp)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolRegistrationTemplate, placeholderMap);
    }

    private String protocol_manager_registrations(List<ProtocolRegistration> protocolList) {
        var csBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            csBuilder.append(StringUtils.format("protocols[{}] = new {}Registration();", protocolId, protocolName, protocolName)).append(LS);
            csBuilder.append(StringUtils.format("protocolIdMap[typeof({})] = {};", protocolName, protocolId, protocolName)).append(LS);
        }
        return csBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var propertyType = toCsClassName(field.getGenericType().getTypeName());
            var propertyFullName = StringUtils.format("public {} {};", propertyType, fieldName);
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.CSharp);
            for (var fieldNote : fieldNotes) {
                csBuilder.append(fieldNote).append(LS);
            }
            csBuilder.append(propertyFullName).append(LS);
        }
        return csBuilder.toString();
    }


    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            csBuilder.append("int beforeWriteIndex = buffer.WriteOffset();").append(LS);
            csBuilder.append(StringUtils.format("buffer.WriteInt({});", registration.getPredictionLength())).append(LS);
        } else {
            csBuilder.append("buffer.WriteInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            csSerializer(fieldRegistration.serializer()).writeObject(csBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            csBuilder.append(StringUtils.format("buffer.AdjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return csBuilder.toString();
    }


    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                csBuilder.append("if (buffer.CompatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = csSerializer(fieldRegistration.serializer()).readObject(csBuilder, 1, field, fieldRegistration);
                csBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                csBuilder.append("}").append(LS);
                continue;
            }
            var readObject = csSerializer(fieldRegistration.serializer()).readObject(csBuilder, 0, field, fieldRegistration);
            csBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return csBuilder.toString();
    }

    public static String toCsClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        // CSharp不适用基础类型的泛型，会影响性能
        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "bool";
                return typeName;
            case "Byte":
                typeName = "byte";
                return typeName;
            case "Short":
                typeName = "short";
                return typeName;
            case "Integer":
                typeName = "int";
                return typeName;
            case "Long":
                typeName = "long";
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
        typeName = typeName.replace(" boolean ", " bool ");
        typeName = typeName.replace(" Boolean ", " bool ");
        typeName = typeName.replaceAll("[B|b]oolean\\[", "bool[");
        typeName = typeName.replace("<Boolean", "<bool");
        typeName = typeName.replace("Boolean>", "bool>");

        // 将Byte转为byte
        typeName = typeName.replace(" Byte ", " byte ");
        typeName = typeName.replace("Byte[", "byte[");
        typeName = typeName.replace("Byte>", "byte>");
        typeName = typeName.replace("<Byte", "<byte");

        // 将Short转为short
        typeName = typeName.replace(" Short ", " short ");
        typeName = typeName.replace("Short[", "short[");
        typeName = typeName.replace("Short>", "short>");
        typeName = typeName.replace("<Short", "<short");

        // 将Integer转为int
        typeName = typeName.replace(" Integer ", " int ");
        typeName = typeName.replace("Integer[", "int[");
        typeName = typeName.replace("Integer>", "int>");
        typeName = typeName.replace("<Integer", "<int");


        // 将Long转为long
        typeName = typeName.replace(" Long ", " long ");
        typeName = typeName.replace("Long[", "long[");
        typeName = typeName.replace("Long>", "long>");
        typeName = typeName.replace("<Long", "<long");

        // 将Float转为float
        typeName = typeName.replace(" Float ", " float ");
        typeName = typeName.replace("Float[", "float[");
        typeName = typeName.replace("Float>", "float>");
        typeName = typeName.replace("<Float", "<float");

        // 将Double转为double
        typeName = typeName.replace(" Double ", " double ");
        typeName = typeName.replace("Double[", "double[");
        typeName = typeName.replace("Double>", "double>");
        typeName = typeName.replace("<Double", "<double");

        // 将Character转为Char
        typeName = typeName.replace(" Character ", " char ");
        typeName = typeName.replace("Character[", "char[");
        typeName = typeName.replace("Character>", "char>");
        typeName = typeName.replace("<Character", "<char");

        // 将String转为string
        typeName = typeName.replace("String ", "string ");
        typeName = typeName.replace("String[", "string[");
        typeName = typeName.replace("String>", "string>");
        typeName = typeName.replace("<String", "<string");

        // 将Map转为Dictionary
        typeName = typeName.replace("Map<", "Dictionary<");

        // 将Set转为HashSet
        typeName = typeName.replace("Set<", "HashSet<");

        // 将private转为public
        typeName = typeName.replace(" private ", " public ");

        return typeName;
    }

}
