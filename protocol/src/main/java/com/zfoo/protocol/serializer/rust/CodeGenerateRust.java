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

package com.zfoo.protocol.serializer.rust;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
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
public class CodeGenerateRust implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateRust.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoorust";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, IRustSerializer> rustSerializerMap = new HashMap<>();

    public static IRustSerializer rustSerializer(ISerializer serializer) {
        return rustSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        rustSerializerMap.put(BoolSerializer.INSTANCE, new RustBoolSerializer());
        rustSerializerMap.put(ByteSerializer.INSTANCE, new RustByteSerializer());
        rustSerializerMap.put(ShortSerializer.INSTANCE, new RustShortSerializer());
        rustSerializerMap.put(IntSerializer.INSTANCE, new RustIntSerializer());
        rustSerializerMap.put(LongSerializer.INSTANCE, new RustLongSerializer());
        rustSerializerMap.put(FloatSerializer.INSTANCE, new RustFloatSerializer());
        rustSerializerMap.put(DoubleSerializer.INSTANCE, new RustDoubleSerializer());
        rustSerializerMap.put(StringSerializer.INSTANCE, new RustStringSerializer());
        rustSerializerMap.put(ArraySerializer.INSTANCE, new RustArraySerializer());
        rustSerializerMap.put(ListSerializer.INSTANCE, new RustListSerializer());
        rustSerializerMap.put(SetSerializer.INSTANCE, new RustSetSerializer());
        rustSerializerMap.put(MapSerializer.INSTANCE, new RustMapSerializer());
        rustSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new RustObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");
        var protocol_imports_manager = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports_manager.append("import * as Protocols from './Protocols';").append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, new Protocols.{}Registration());", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.set(Protocols.{}, {});", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports_manager.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated TypeScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        var protocol_imports_protocols = new StringBuilder();
        protocol_imports_protocols.append("import IByteBuffer from './IByteBuffer';").append(LS);
        var protocol_class = new StringBuilder();
        var protocol_registration = new StringBuilder();
        for (var registration : registrations) {
            protocol_class.append(protocol_class(registration).replace("class ", "export class ")).append(LS);
            protocol_registration.append(protocol_registration(registration)).append(LS);
        }
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolsTemplate.ts");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports_protocols.toString()
                , CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.ts", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated TypeScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolManagerTemplate.ts");
        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("import {} from './{}/{}';", protocol_name, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_imports.append(StringUtils.format("import { {}Registration } from './{}/{}';", protocol_name, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols.set({}, new {}Registration());", protocol_id, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocolIdMap.set({}, {});", protocol_name, protocol_id)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString());
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.ts"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated TypeScript protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());

//
//        for (var registration : registrations) {
//            var protocol_id = registration.protocolId();
//            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
//            var protocolTemplate = ClassUtils.getFileFromClassPathToString("typescript/ProtocolTemplate.ts");
//            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
//                    CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
//                    , CodeTemplatePlaceholder.protocol_name, protocol_name
//                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_fold(registration)
//                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
//                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
//            ));
//            var outputPath = StringUtils.format("{}/{}/{}.ts", protocolOutputPath, GenerateProtocolPath.protocolPathSlash(protocol_id), protocol_name);
//            var file = new File(outputPath);
//            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
//            logger.info("Generated TypeScript protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
//        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        // 生成mod文件
        var modBuilder = new StringBuilder();
        modBuilder.append("pub mod i_byte_buffer;").append(LS);
        modBuilder.append("pub mod byte_buffer;").append(LS);
        modBuilder.append("pub mod protocol_manager;").append(LS);
        for (var registration : registrations) {
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            modBuilder.append(StringUtils.format("pub mod {};", StringUtils.uncapitalize(protocol_name))).append(LS);
        }
        var modFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "mod.rs"));
        FileUtils.writeStringToFile(modFile, modBuilder.toString(), true);
        logger.info("Generated Rust mod file:[{}] is in path:[{}]", modFile.getName(), modFile.getAbsolutePath());


        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("rust/protocol_manager_template.rs");
        var protocol_imports = new StringBuilder();
        var protocol_manager_write_registrations = new StringBuilder();
        var protocol_manager_read_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("use crate::{}::{}::{write{}, read{}};", protocolOutputRootPath, StringUtils.uncapitalize(protocol_name), protocol_name, protocol_name)).append(LS);
            protocol_manager_write_registrations.append(StringUtils.format("{} => write{}(buffer, packet),", protocol_id, protocol_name)).append(LS);
            protocol_manager_read_registrations.append(StringUtils.format("{} => read{}(buffer),", protocol_id, protocol_name)).append(LS);
        }
        var placeholderMap = Map.of(CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
                , CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_manager_write_registrations.toString()
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_manager_read_registrations.toString()
        );
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "protocol_manager.rs"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Rust protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var protocolTemplate = ClassUtils.getFileFromClassPathToString("rust/protocol_template.rs");
            var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
                    , CodeTemplatePlaceholder.protocol_name, protocol_name
                    , CodeTemplatePlaceholder.protocol_imports, protocol_imports_default(registration)
                    , CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                    , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
            ));
            var outputPath = StringUtils.format("{}/{}.rs", protocolOutputPath, StringUtils.uncapitalize(protocol_name));
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Rust protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("rust/byte_buffer.rs", "rust/i_byte_buffer.rs");
        for (var fileName : list) {
            var template = ClassUtils.getFileFromClassPathToString(fileName);
            var formatTemplate = CodeTemplatePlaceholder.formatTemplate(template, Map.of(
                    CodeTemplatePlaceholder.protocol_root_path, protocolOutputRootPath
            ));
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "rust/")));
            FileUtils.writeStringToFile(createFile, formatTemplate, false);
        }
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("rust/protocol_class_template.rs");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Rust)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_registration(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("rust/protocol_registration_template.rs");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition_new(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        ));
        return formatProtocolTemplate;
    }

    private String protocol_imports_default(ProtocolRegistration registration) {
        // import IByteBuffer first
        var protocolId = registration.getId();
        var importBuilder = new StringBuilder();
        // import other sub protocols
        var subProtocols = ProtocolAnalysis.getFirstSubProtocolIds(protocolId);
        for (var subProtocolId : subProtocols) {
            var protocolName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            importBuilder.append(StringUtils.format("use crate::{}::{}::{};", protocolOutputRootPath, StringUtils.uncapitalize(protocolName), protocolName)).append(LS);
        }
        return importBuilder.toString();
    }

    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var rustBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Rust);
            for (var fieldNote : fieldNotes) {
                rustBuilder.append(fieldNote).append(LS);
            }
            var fieldTypeDefaultValue = rustSerializer(fieldRegistration.serializer()).fieldTypeDefaultValue(field, fieldRegistration);
            var fieldType = fieldTypeDefaultValue.getKey();
            var fieldDefaultValue = fieldTypeDefaultValue.getValue();
            rustBuilder.append(StringUtils.format("pub {}: {},", fieldName, fieldType)).append(LS);
        }
        return rustBuilder.toString();
    }

    private String protocol_field_definition_new(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var rustBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Rust);
            for (var fieldNote : fieldNotes) {
                rustBuilder.append(fieldNote).append(LS);
            }
            var fieldTypeDefaultValue = rustSerializer(fieldRegistration.serializer()).fieldTypeDefaultValue(field, fieldRegistration);
            var fieldType = fieldTypeDefaultValue.getKey();
            var fieldDefaultValue = fieldTypeDefaultValue.getValue();
            rustBuilder.append(StringUtils.format("{}: {},", fieldName, fieldDefaultValue)).append(LS);
        }
        return rustBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var rustBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            rustBuilder.append("let beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            rustBuilder.append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            rustBuilder.append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var serializer = rustSerializer(fieldRegistration.serializer());
            serializer.writeObject(rustBuilder, "message." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            rustBuilder.append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return rustBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var rustBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                rustBuilder.append("if buffer.compatibleRead(beforeReadIndex, length) {").append(LS);
                var compatibleReadObject = rustSerializer(fieldRegistration.serializer()).readObject(rustBuilder, 1, field, fieldRegistration);
                rustBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                rustBuilder.append("}").append(LS);
                continue;
            }
            var readObject = rustSerializer(fieldRegistration.serializer()).readObject(rustBuilder, 0, field, fieldRegistration);
            rustBuilder.append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return rustBuilder.toString();
    }

    public static String toRustClassName(String typeName) {
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
                typeName = "i8";
                return typeName;
            case "short":
            case "Short":
                typeName = "i16";
                return typeName;
            case "int":
            case "Integer":
                typeName = "i32";
                return typeName;
            case "long":
            case "Long":
                typeName = "i64";
                return typeName;
            case "float":
            case "Float":
                typeName = "f32";
                return typeName;
            case "double":
            case "Double":
                typeName = "f64";
                return typeName;
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
        typeName = typeName.replace("Byte[", "i8");
        typeName = typeName.replace("Byte>", "i8>");
        typeName = typeName.replace("<Byte", "<i8");

        // 将Short转为short
        typeName = typeName.replace("Short[", "i16");
        typeName = typeName.replace("Short>", "i16>");
        typeName = typeName.replace("<Short", "<i16");

        // 将Integer转为int
        typeName = typeName.replace("Integer[", "i32");
        typeName = typeName.replace("Integer>", "i32>");
        typeName = typeName.replace("<Integer", "<i32");


        // 将Long转为long
        typeName = typeName.replace("Long[", "i64");
        typeName = typeName.replace("Long>", "i64>");
        typeName = typeName.replace("<Long", "<i64");

        // 将Float转为float
        typeName = typeName.replace("Float[", "f32");
        typeName = typeName.replace("Float>", "f32>");
        typeName = typeName.replace("<Float", "<f32");

        // 将Double转为double
        typeName = typeName.replace("Double[", "f64");
        typeName = typeName.replace("Double>", "f64>");
        typeName = typeName.replace("<Double", "<f64");

        // 将String转为string
        typeName = typeName.replace("String[", "String");
        typeName = typeName.replace("String>", "String>");
        typeName = typeName.replace("<String", "<String");

        // 将Map转为map
        typeName = typeName.replace("Map<", "HashMap<");

        // 将Set转为set
        typeName = typeName.replace("Set<", "HashSet<");

        // 将List转为vector
        typeName = typeName.replace("List<", "Vec<");

        return typeName;
    }
}
