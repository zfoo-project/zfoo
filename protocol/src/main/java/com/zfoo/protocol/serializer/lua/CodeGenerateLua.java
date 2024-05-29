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

package com.zfoo.protocol.serializer.lua;

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
public class CodeGenerateLua implements ICodeGenerate {
    private static final Logger logger = LoggerFactory.getLogger(CodeGenerateLua.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfoolua";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static final Map<ISerializer, ILuaSerializer> luaSerializerMap = new HashMap<>();

    public static ILuaSerializer luaSerializer(ISerializer serializer) {
        return luaSerializerMap.get(serializer);
    }

    @Override
    public void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        luaSerializerMap.put(BooleanSerializer.INSTANCE, new LuaBooleanSerializer());
        luaSerializerMap.put(ByteSerializer.INSTANCE, new LuaByteSerializer());
        luaSerializerMap.put(ShortSerializer.INSTANCE, new LuaShortSerializer());
        luaSerializerMap.put(IntSerializer.INSTANCE, new LuaIntSerializer());
        luaSerializerMap.put(LongSerializer.INSTANCE, new LuaLongSerializer());
        luaSerializerMap.put(FloatSerializer.INSTANCE, new LuaFloatSerializer());
        luaSerializerMap.put(DoubleSerializer.INSTANCE, new LuaDoubleSerializer());
        luaSerializerMap.put(StringSerializer.INSTANCE, new LuaStringSerializer());
        luaSerializerMap.put(ArraySerializer.INSTANCE, new LuaArraySerializer());
        luaSerializerMap.put(ListSerializer.INSTANCE, new LuaListSerializer());
        luaSerializerMap.put(SetSerializer.INSTANCE, new LuaSetSerializer());
        luaSerializerMap.put(MapSerializer.INSTANCE, new LuaMapSerializer());
        luaSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new LuaObjectProtocolSerializer());
    }

    @Override
    public void mergerProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        protocol_imports.append(StringUtils.format("local Protocols = require(\"{}.Protocols\")", protocolOutputRootPath)).append(LS);
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = Protocols.{}", protocol_id, protocol_name)).append(LS);
        }
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolManagerTemplate.lua");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString()
        );
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.lua"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Lua protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


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
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolsTemplate.lua");
        var formatProtocolTemplate = CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class.toString()
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration.toString()
        ));
        var outputPath = StringUtils.format("{}/Protocols.lua", protocolOutputPath);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
        logger.info("Generated Lua protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());

    }

    @Override
    public void foldProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolPathPeriod(protocol_id);
            protocol_imports.append(StringUtils.format("local {} = require(\"{}.{}.{}\")", protocol_name, protocolOutputRootPath, path.replace(StringUtils.SLASH, StringUtils.PERIOD), protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}", protocol_id, protocol_name)).append(LS);
        }
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolManagerTemplate.lua");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString()
        );
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.lua"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Lua protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}/{}.lua", protocolOutputPath, GenerateProtocolPath.protocolPathPeriod(protocol_id), protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Lua protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    @Override
    public void defaultProtocol(List<ProtocolRegistration> registrations) throws IOException {
        createTemplateFile();


        var protocol_imports = new StringBuilder();
        var protocol_manager_registrations = new StringBuilder();
        for (var registration : registrations) {
            var protocol_id = registration.protocolId();
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            protocol_imports.append(StringUtils.format("local {} = require(\"{}.{}\")", protocol_name, protocolOutputRootPath, protocol_name)).append(LS);
            protocol_manager_registrations.append(StringUtils.format("protocols[{}] = {}", protocol_id, protocol_name)).append(LS);
        }
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolManagerTemplate.lua");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_imports, protocol_imports.toString()
                , CodeTemplatePlaceholder.protocol_manager_registrations, protocol_manager_registrations.toString()
        );
        var formatProtocolManagerTemplate = CodeTemplatePlaceholder.formatTemplate(protocolManagerTemplate, placeholderMap);
        var protocolManagerFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.lua"));
        FileUtils.writeStringToFile(protocolManagerFile, formatProtocolManagerTemplate, true);
        logger.info("Generated Lua protocol manager file:[{}] is in path:[{}]", protocolManagerFile.getName(), protocolManagerFile.getAbsolutePath());


        for (var registration : registrations) {
            var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
            var formatProtocolTemplate = formatProtocolTemplate(registration);
            var outputPath = StringUtils.format("{}/{}.lua", protocolOutputPath, protocol_name);
            var file = new File(outputPath);
            FileUtils.writeStringToFile(file, formatProtocolTemplate, true);
            logger.info("Generated Lua protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
        }
    }

    private void createTemplateFile() throws IOException {
        var list = List.of("lua/Long.lua");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "lua/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var byteBufferFileName = "lua/ByteBuffer.lua";
        var byteBufferTemplate = ClassUtils.getFileFromClassPathToString(byteBufferFileName);
        var byteBufferFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(byteBufferFileName, "lua/")));
        FileUtils.writeStringToFile(byteBufferFile, StringUtils.format(byteBufferTemplate, protocolOutputRootPath), false);
    }

    public String formatProtocolTemplate(ProtocolRegistration registration) {
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolTemplate.lua");

        return CodeTemplatePlaceholder.formatTemplate(protocolTemplate, Map.of(
                CodeTemplatePlaceholder.protocol_class, protocol_class(registration)
                , CodeTemplatePlaceholder.protocol_registration, protocol_registration(registration)
                , CodeTemplatePlaceholder.protocol_name, protocol_name
        ));
    }

    private String protocol_class(ProtocolRegistration registration) {
        var protocol_id = registration.protocolId();
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolClassTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolClassTemplate.lua");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_note, GenerateProtocolNote.protocol_note(protocol_id, CodeLanguage.Lua)
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
        var protocol_name = registration.protocolConstructor().getDeclaringClass().getSimpleName();

        var protocolRegistrationTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolRegistrationTemplate.lua");
        var placeholderMap = Map.of(
                CodeTemplatePlaceholder.protocol_name, protocol_name
                , CodeTemplatePlaceholder.protocol_id, String.valueOf(protocol_id)
                , CodeTemplatePlaceholder.protocol_field_definition, protocol_field_definition(registration)
                , CodeTemplatePlaceholder.protocol_write_serialization, protocol_write_serialization(registration)
                , CodeTemplatePlaceholder.protocol_read_deserialization, protocol_read_deserialization(registration)
        );
        return CodeTemplatePlaceholder.formatTemplate(protocolRegistrationTemplate, placeholderMap);
    }


    private String protocol_field_definition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();

        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Lua);
            for (var fieldNote : fieldNotes) {
                luaBuilder.append(fieldNote).append(LS);
            }
            var fieldDefaultValue = luaSerializer(fieldRegistration.serializer()).fieldDefaultValue(field, fieldRegistration);
            if (i == fields.length - 1) {
                luaBuilder.append(StringUtils.format("{} = {}", fieldName, fieldDefaultValue));
            } else {
                luaBuilder.append(StringUtils.format("{} = {},", fieldName, fieldDefaultValue));
            }
            var typeNote = CodeGenerateCsharp.toCsClassName(field.getGenericType().getTypeName());
            // 生成类型的注释
            luaBuilder.append(" -- ").append(typeNote).append(LS);
        }
        return luaBuilder.toString();
    }

    private String protocol_write_serialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            luaBuilder.append("local beforeWriteIndex = buffer:getWriteOffset()").append(LS);
            luaBuilder.append(StringUtils.format("buffer:writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            luaBuilder.append("buffer:writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            luaSerializer(fieldRegistration.serializer()).writeObject(luaBuilder, "packet." + field.getName(), 0, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            luaBuilder.append(StringUtils.format("buffer:adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return luaBuilder.toString();
    }

    private String protocol_read_deserialization(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                luaBuilder.append("if buffer:compatibleRead(beforeReadIndex, length) then").append(LS);
                var compatibleReadObject = luaSerializer(fieldRegistration.serializer()).readObject(luaBuilder, 1, field, fieldRegistration);
                luaBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                luaBuilder.append("end").append(LS);
                continue;
            }
            var readObject = luaSerializer(fieldRegistration.serializer()).readObject(luaBuilder, 0, field, fieldRegistration);
            luaBuilder.append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return luaBuilder.toString();
    }

}
