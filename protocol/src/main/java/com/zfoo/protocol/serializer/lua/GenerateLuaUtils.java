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
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.csharp.GenerateCsUtils;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateLuaUtils {

    // custom configuration
    public static String protocolOutputRootPath = "zfoolua";
    public static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, ILuaSerializer> luaSerializerMap;

    public static ILuaSerializer luaSerializer(ISerializer serializer) {
        return luaSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        // if not specify output path, then use current default path
        if (StringUtils.isEmpty(generateOperation.getProtocolPath())) {
            protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        } else {
            protocolOutputPath = generateOperation.getProtocolPath();
        }
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

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("lua/ByteBuffer.lua", "lua/Long.lua");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, StringUtils.substringAfterFirst(fileName, "lua/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

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
                        , protocolName,protocolOutputRootPath, path.replaceAll(StringUtils.SLASH, StringUtils.PERIOD), protocolName)).append(LS);
            }

            protocolBuilder.append(TAB).append(StringUtils.format("protocols[{}] = {}", protocolId, protocolName)).append(LS);
        }
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, StringUtils.EMPTY_JSON, StringUtils.EMPTY_JSON, fieldBuilder.toString().trim(), protocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.lua")), protocolManagerTemplate, true);
    }

    public static void createLuaProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();
        var protocolTemplate = ClassUtils.getFileFromClassPathToString("lua/ProtocolTemplate.lua");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Lua);
        var valueOfMethod = valueOfMethod(registration);
        var writePacket = writePacket(registration);
        var readPacket = readPacket(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, StringUtils.EMPTY_JSON, protocolClazzName
                , valueOfMethod.trim(), protocolClazzName, protocolId
                , protocolClazzName, writePacket.trim(), protocolClazzName, protocolClazzName, readPacket.trim(), protocolClazzName);

        var outputPath = StringUtils.format("{}/{}/{}.lua"
                , protocolOutputRootPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(outputPath), protocolTemplate, true);
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
