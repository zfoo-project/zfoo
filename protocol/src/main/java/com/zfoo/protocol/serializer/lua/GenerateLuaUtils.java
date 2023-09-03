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
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateLuaUtils {

    private static String protocolOutputRootPath = "LuaProtocol/";

    private static Map<ISerializer, ILuaSerializer> luaSerializerMap;

    public static ILuaSerializer luaSerializer(ISerializer serializer) {
        return luaSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

        luaSerializerMap = new HashMap<>();
        luaSerializerMap.put(BooleanSerializer.INSTANCE, new LuaBooleanSerializer());
        luaSerializerMap.put(ByteSerializer.INSTANCE, new LuaByteSerializer());
        luaSerializerMap.put(ShortSerializer.INSTANCE, new LuaShortSerializer());
        luaSerializerMap.put(IntSerializer.INSTANCE, new LuaIntSerializer());
        luaSerializerMap.put(LongSerializer.INSTANCE, new LuaLongSerializer());
        luaSerializerMap.put(FloatSerializer.INSTANCE, new LuaFloatSerializer());
        luaSerializerMap.put(DoubleSerializer.INSTANCE, new LuaDoubleSerializer());
        luaSerializerMap.put(CharSerializer.INSTANCE, new LuaCharSerializer());
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
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("lua/Buffer/ByteBuffer.lua", "lua/Buffer/Long.lua");
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
            var name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.getCapitalizeProtocolPath(protocolId);
            if (StringUtils.isBlank(path)) {
                fieldBuilder.append(TAB).append(StringUtils.format("local {} = require(\"LuaProtocol.{}\")", name, name)).append(LS);
            } else {
                fieldBuilder.append(TAB).append(StringUtils.format("local {} = require(\"LuaProtocol.{}.{}\")"
                        , name, path.replaceAll(StringUtils.SLASH, StringUtils.PERIOD), name)).append(LS);
            }

            protocolBuilder.append(TAB).append(StringUtils.format("protocols[{}] = {}", protocolId, name)).append(LS);
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
                , valueOfMethod.getKey().trim(), valueOfMethod.getValue().trim(), protocolClazzName, protocolId
                , protocolClazzName, writePacket.trim(), protocolClazzName, protocolClazzName, readPacket.trim(), protocolClazzName);

        var protocolOutputPath = StringUtils.format("{}/{}/{}.lua"
                , protocolOutputRootPath, GenerateProtocolPath.getCapitalizeProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), protocolTemplate, true);
    }

    private static Pair<String, String> valueOfMethod(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();

        var valueOfParams = StringUtils.joinWith(", ", Arrays.stream(fields).map(it -> it.getName()).collect(Collectors.toList()).toArray());
        var luaBuilder = new StringBuilder();

        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Lua);
            if (StringUtils.isNotBlank(fieldNote)) {
                luaBuilder.append(TAB + TAB).append(fieldNote).append(LS);
            }

            if (i == fields.length - 1) {
                luaBuilder.append(TAB + TAB).append(StringUtils.format("{} = {}", fieldName, fieldName));
            } else {
                luaBuilder.append(TAB + TAB).append(StringUtils.format("{} = {},", fieldName, fieldName));
            }
            // 生成类型的注释
            luaBuilder.append(" -- ").append(field.getGenericType().getTypeName()).append(LS);
        }
        return new Pair<>(valueOfParams, luaBuilder.toString());
    }

    private static String writePacket(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            luaSerializer(fieldRegistration.serializer()).writeObject(luaBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
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
                luaBuilder.append(TAB).append("if not(buffer:isReadable()) then").append(LS);
                luaBuilder.append(TAB + TAB).append("return packet").append(LS);
                luaBuilder.append(TAB).append("end").append(LS);
            }
            var readObject = luaSerializer(fieldRegistration.serializer()).readObject(luaBuilder, 1, field, fieldRegistration);
            luaBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return luaBuilder.toString();
    }

}
