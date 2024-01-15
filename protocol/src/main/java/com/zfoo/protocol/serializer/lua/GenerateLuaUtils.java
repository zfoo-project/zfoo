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
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.csharp.GenerateCsUtils;
import com.zfoo.protocol.serializer.reflect.*;
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
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateLuaUtils {
    private static final Logger logger = LoggerFactory.getLogger(GenerateLuaUtils.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfoolua";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, ILuaSerializer> luaSerializerMap;

    public static ILuaSerializer luaSerializer(ISerializer serializer) {
        return luaSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        luaSerializerMap = new HashMap<>();
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

    public static void clear() {
        luaSerializerMap = null;
        protocolOutputRootPath = null;
        protocolOutputPath = null;
    }

    // All protocol files are generated in a single protocol file.
    public static void createProtocolManagerInOneFile(List<IProtocolRegistration> protocolList) throws IOException {
        createTemplateFile();

        // 生成Protocol.lua文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("lua-one/ProtocolManagerTemplate.lua");
        var builderImports = new StringBuilder();
        builderImports.append(TAB).append(StringUtils.format("local Protocols = require(\"{}.Protocols\")", protocolOutputRootPath)).append(LS);
        builderImports.append(TAB).append(StringUtils.format("local ProtocolBase = require(\"{}.ProtocolBase\")", protocolOutputRootPath)).append(LS);
        builderImports.append(TAB).append(StringUtils.format("local ProtocolWriter = require(\"{}.ProtocolWriter\")", protocolOutputRootPath)).append(LS);
        builderImports.append(TAB).append(StringUtils.format("local ProtocolReader = require(\"{}.ProtocolReader\")", protocolOutputRootPath)).append(LS);
        var protocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            protocolBuilder.append(TAB).append(StringUtils.format("protocols[{}] = Protocols.{}", protocolId, protocolName)).append(LS);
        }
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, StringUtils.EMPTY_JSON, StringUtils.EMPTY_JSON, builderImports.toString().trim(), protocolBuilder.toString().trim());
        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.lua"));
        FileUtils.writeStringToFile(file, protocolManagerTemplate, true);
        logger.info("Generated Lua protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    public static void createLuaProtocolsInOneFile(List<IProtocolRegistration> registrations) {
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("lua-one/ProtocolTemplate.lua");
        var protocolBaseTemplate = ClassUtils.getFileFromClassPathToString("lua-one/ProtocolBaseTemplate.lua");
        var protocolWriterTemplate = ClassUtils.getFileFromClassPathToString("lua-one/ProtocolWriterTemplate.lua");
        var protocolReaderTemplate = ClassUtils.getFileFromClassPathToString("lua-one/ProtocolReaderTemplate.lua");
        var builderProtocol = new StringBuilder();
        var builderProtocolBase = new StringBuilder();
        var builderProtocolWriter = new StringBuilder();
        var builderProtocolReader = new StringBuilder();

        var requireProtocols = StringUtils.format("local Protocols = require(\"{}.Protocols\")", protocolOutputRootPath);
        builderProtocolBase.append(requireProtocols).append(LS).append(LS);
        builderProtocolWriter.append(requireProtocols).append(LS).append(LS);
        builderProtocolReader.append(requireProtocols).append(LS).append(LS);

        for (var protocolRegistration : registrations) {
            var registration = (ProtocolRegistration) protocolRegistration;
            var protocolId = registration.protocolId();
            var registrationConstructor = registration.getConstructor();
            var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();
            GenerateProtocolFile.index.set(0);

            var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Lua);
            var valueOfMethod = valueOfMethod(registration);
            var toStringJsonTemplate = toStringJsonTemplate(registration);
            var toStringParams = toStringParams(registration);
            var writePacket = writePacket(registration);
            var readPacket = readPacket(registration);

            var protocol = StringUtils.format(protocolTemplate, classNote, protocolClazzName, StringUtils.EMPTY_JSON, protocolClazzName, valueOfMethod.trim());
            var protocolBase = StringUtils.format(protocolBaseTemplate, protocolClazzName, protocolId, protocolClazzName, protocolClazzName, protocolClazzName, toStringJsonTemplate, toStringParams);
            var protocolWriter = StringUtils.format(protocolWriterTemplate, protocolClazzName, writePacket.trim());
            var protocolReader = StringUtils.format(protocolReaderTemplate, protocolClazzName, protocolClazzName, readPacket.trim());

            builderProtocol.append(protocol).append(LS);
            builderProtocolBase.append(protocolBase).append(LS).append(LS);
            builderProtocolWriter.append(protocolWriter).append(LS).append(LS);
            builderProtocolReader.append(protocolReader).append(LS).append(LS);
        }

        builderProtocol.append(LS).append("local Protocols = {}").append(LS);
        for (var protocolRegistration : registrations) {
            var registration = (ProtocolRegistration) protocolRegistration;
            var registrationConstructor = registration.getConstructor();
            var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();
            builderProtocol.append(StringUtils.format("Protocols.{} = {}", protocolClazzName, protocolClazzName)).append(LS);
        }
        builderProtocol.append("return Protocols");
        var protocolsFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "Protocols.lua"));
        var protocolBaseFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolBase.lua"));
        var protocolWriterFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolWriter.lua"));
        var protocolReaderFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolReader.lua"));
        FileUtils.writeStringToFile(protocolsFile, builderProtocol.toString(), true);
        FileUtils.writeStringToFile(protocolBaseFile, builderProtocolBase.toString(), true);
        FileUtils.writeStringToFile(protocolWriterFile, builderProtocolWriter.toString(), true);
        FileUtils.writeStringToFile(protocolReaderFile, builderProtocolReader.toString(), true);
        logger.info("Generated Lua protocols file:[{}] is in path:[{}]", protocolsFile.getName(), protocolsFile.getAbsolutePath());
        logger.info("Generated Lua protocol base file:[{}] is in path:[{}]", protocolBaseFile.getName(), protocolBaseFile.getAbsolutePath());
        logger.info("Generated Lua protocol writer file:[{}] is in path:[{}]", protocolWriterFile.getName(), protocolWriterFile.getAbsolutePath());
        logger.info("Generated Lua protocol reader file:[{}] is in path:[{}]", protocolReaderFile.getName(), protocolReaderFile.getAbsolutePath());
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        createTemplateFile();

        // 生成Protocol.lua文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolManagerTemplate.lua");
        var fieldBuilder = new StringBuilder();
        var protocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.getProtocolPath(protocolId);
            if (StringUtils.isBlank(path)) {
                fieldBuilder.append(TAB).append(StringUtils.format("local {} = require(\"{}.{}\")", protocolName, protocolOutputRootPath, protocolName)).append(LS);
            } else {
                fieldBuilder.append(TAB).append(StringUtils.format("local {} = require(\"{}.{}.{}\")"
                        , protocolName, protocolOutputRootPath, path.replaceAll(StringUtils.SLASH, StringUtils.PERIOD), protocolName)).append(LS);
            }

            protocolBuilder.append(TAB).append(StringUtils.format("protocols[{}] = {}", protocolId, protocolName)).append(LS);
        }
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, StringUtils.EMPTY_JSON, StringUtils.EMPTY_JSON, fieldBuilder.toString().trim(), protocolBuilder.toString().trim());
        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.lua"));
        FileUtils.writeStringToFile(file, protocolManagerTemplate, true);
        logger.info("Generated Lua protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    private static void createTemplateFile() throws IOException {
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

    public static void createLuaProtocolFile(ProtocolRegistration registration) {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolTemplate.lua");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Lua);
        var valueOfMethod = valueOfMethod(registration);
        var toStringJsonTemplate = toStringJsonTemplate(registration);
        var toStringParams = toStringParams(registration);
        var writePacket = writePacket(registration);
        var readPacket = readPacket(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, StringUtils.EMPTY_JSON, protocolClazzName
                , valueOfMethod.trim(), protocolClazzName, protocolId, protocolClazzName, protocolClazzName
                , protocolClazzName, toStringJsonTemplate, toStringParams
                , protocolClazzName, writePacket.trim(), protocolClazzName, protocolClazzName, readPacket.trim(), protocolClazzName);

        var outputPath = StringUtils.format("{}/{}/{}.lua"
                , protocolOutputPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, protocolTemplate, true);
        logger.info("Generated Lua protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    private static String valueOfMethod(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();

        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Lua);
            if (StringUtils.isNotBlank(fieldNote)) {
                luaBuilder.append(TAB + TAB).append(fieldNote).append(LS);
            }
            var fieldDefaultValue = luaSerializer(fieldRegistration.serializer()).fieldDefaultValue(field, fieldRegistration);
            if (i == fields.length - 1) {
                luaBuilder.append(TAB + TAB).append(StringUtils.format("{} = {}", fieldName, fieldDefaultValue));
            } else {
                luaBuilder.append(TAB + TAB).append(StringUtils.format("{} = {},", fieldName, fieldDefaultValue));
            }
            var typeNote = GenerateCsUtils.toCsClassName(field.getGenericType().getTypeName());
            // 生成类型的注释
            luaBuilder.append(" -- ").append(typeNote).append(LS);
        }
        return luaBuilder.toString();
    }

    private static String toStringJsonTemplate(ProtocolRegistration registration) {
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
            params.add(StringUtils.format("{}:%s", fieldName));
        }
        gdBuilder.append(StringUtils.joinWith(", ", params.toArray()));
        gdBuilder.append("}");
        return gdBuilder.toString();
    }

    private static String toStringParams(ProtocolRegistration registration) {
        var fields = registration.getFields();
        if (ArrayUtils.isEmpty(fields)) {
            return StringUtils.EMPTY_JSON;
        }
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var params = new ArrayList<String>();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            params.add(StringUtils.format("self.{}", field.getName()));
        }
        gdBuilder.append(StringUtils.joinWith(", ", params.toArray()));
        return gdBuilder.toString();
    }

    private static String writePacket(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            luaBuilder.append("local beforeWriteIndex = buffer:getWriteOffset()").append(LS);
            luaBuilder.append(TAB).append(StringUtils.format("buffer:writeInt({})", registration.getPredictionLength())).append(LS);
        } else {
            luaBuilder.append(TAB).append("buffer:writeInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            luaSerializer(fieldRegistration.serializer()).writeObject(luaBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            luaBuilder.append(TAB).append(StringUtils.format("buffer:adjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return luaBuilder.toString();
    }

    private static String readPacket(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                luaBuilder.append(TAB).append("if buffer:compatibleRead(beforeReadIndex, length) then").append(LS);
                var compatibleReadObject = luaSerializer(fieldRegistration.serializer()).readObject(luaBuilder, 2, field, fieldRegistration);
                luaBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {}", field.getName(), compatibleReadObject)).append(LS);
                luaBuilder.append(TAB).append("end").append(LS);
                continue;
            }
            var readObject = luaSerializer(fieldRegistration.serializer()).readObject(luaBuilder, 1, field, fieldRegistration);
            luaBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return luaBuilder.toString();
    }

}
