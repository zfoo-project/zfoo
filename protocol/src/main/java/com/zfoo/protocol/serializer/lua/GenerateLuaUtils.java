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

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateProtocolDocument;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class GenerateLuaUtils {

    private static final String PROTOCOL_OUTPUT_ROOT_PATH = "LuaProtocol/";

    private static Map<ISerializer, ILuaSerializer> luaSerializerMap;

    public static ILuaSerializer luaSerializer(ISerializer serializer) {
        return luaSerializerMap.get(serializer);
    }

    public static void init() {
        FileUtils.deleteFile(new File(PROTOCOL_OUTPUT_ROOT_PATH));
        FileUtils.createDirectory(PROTOCOL_OUTPUT_ROOT_PATH);

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
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("lua/Buffer/ByteBuffer.lua", "lua/Buffer/Long.lua");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, StringUtils.substringAfterFirst(fileName, "lua/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        // 生成Protocol.lua文件
        var luaBuilder = new StringBuilder();

        var protocolManagerStr = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("lua/ProtocolManager.lua")));
        luaBuilder.append(protocolManagerStr);

        luaBuilder.append("function initProtocol()").append(LS);
        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> {
                    var name = it.protocolConstructor().getDeclaringClass().getSimpleName();
                    var path = GenerateProtocolPath.getCapitalizeProtocolPath(it.protocolId());

                    if (StringUtils.isBlank(path)) {
                        luaBuilder.append(TAB).append(StringUtils.format("local {} = require(\"LuaProtocol.{}\")", name, name)).append(LS);
                    } else {
                        luaBuilder.append(TAB).append(StringUtils.format("local {} = require(\"LuaProtocol.{}.{}\")"
                                , name, path.replaceAll(StringUtils.SLASH, StringUtils.PERIOD), name)).append(LS);
                    }
                });

        protocolList.stream().filter(it -> Objects.nonNull(it))
                .forEach(it -> luaBuilder.append(TAB).append(StringUtils.format("protocols[{}] = {}", it.protocolId(), it.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS));

        luaBuilder.append("end").append(LS + LS);
        luaBuilder.append("ProtocolManager.initProtocol = initProtocol").append(LS);
        luaBuilder.append("return ProtocolManager").append(LS);

        FileUtils.writeStringToFile(new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, "ProtocolManager.lua")), luaBuilder.toString());
    }

    public static void createLuaProtocolFile(ProtocolRegistration registration) {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var fieldRegistrations = registration.getFieldRegistrations();

        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var luaBuilder = new StringBuilder();

        // document
        luaBuilder.append(documentTitleAndImport(registration));

        // new object
        luaBuilder.append(newFunction(registration));

        // protocolId method
        luaBuilder.append(protocolIdFunction(registration));

        // writeObject method
        luaBuilder.append(writePacket(registration));

        // readObject method
        luaBuilder.append(readPacket(registration)).append(LS);


        luaBuilder.append(StringUtils.format("return {}", protocolClazzName)).append(LS);


        var protocolOutputPath = StringUtils.format("{}{}/{}.lua"
                , PROTOCOL_OUTPUT_ROOT_PATH
                , GenerateProtocolPath.getCapitalizeProtocolPath(protocolId)
                , protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), luaBuilder.toString());
    }

    private static String documentTitleAndImport(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var fieldRegistrations = registration.getFieldRegistrations();
        var luaBuilder = new StringBuilder();

        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docTitle = protocolDocument.getKey();

        if (!StringUtils.isBlank(docTitle)) {
            Arrays.stream(docTitle.split(LS)).forEach(it -> luaBuilder.append(docToLuaDoc(it)).append(LS));
            luaBuilder.append(LS);
        }


        // 如果协议包含子协议，则需要导入ProtocolManager
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);
        if (CollectionUtils.isNotEmpty(subProtocols)) {
            luaBuilder.append("local ProtocolManager = require(\"LuaProtocol.ProtocolManager\")").append(LS + LS);
        }

        return luaBuilder.toString();
    }

    private static String newFunction(ProtocolRegistration registration) {
        short protocolId = registration.getId();
        var fields = registration.getFields();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docFieldMap = protocolDocument.getValue();

        var luaBuilder = new StringBuilder();

        luaBuilder.append(StringUtils.format("local {} = {}", protocolClazzName)).append(LS + LS);

        luaBuilder.append(StringUtils.format("function {}:new(", protocolClazzName));
        luaBuilder.append(StringUtils.joinWith(", ", Arrays.stream(fields).map(it -> it.getName()).collect(Collectors.toList()).toArray()))
                .append(")")
                .append(LS);
        luaBuilder.append(TAB).append("local obj = {").append(LS);

        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var propertyName = field.getName();

            // 生成注释
            var doc = docFieldMap.get(propertyName);
            if (!StringUtils.isBlank(doc)) {
                Arrays.stream(doc.split(LS)).forEach(it -> luaBuilder.append(TAB + TAB).append(docToLuaDoc(it)).append(LS));
            }

            if (i == fields.length - 1) {
                luaBuilder.append(TAB + TAB)
                        .append(StringUtils.format("{} = {}", propertyName, propertyName));
            } else {
                luaBuilder.append(TAB + TAB)
                        .append(StringUtils.format("{} = {},", propertyName, propertyName));
            }

            // 生成类型的注释
            luaBuilder.append(" -- ").append(field.getGenericType().getTypeName()).append(LS);
        }
        luaBuilder.append(TAB).append("}").append(LS);
        luaBuilder.append(TAB).append("setmetatable(obj, self)").append(LS);
        luaBuilder.append(TAB).append("self.__index = self").append(LS);
        luaBuilder.append(TAB).append("return obj").append(LS);
        luaBuilder.append("end").append(LS).append(LS);
        return luaBuilder.toString();
    }

    private static String protocolIdFunction(ProtocolRegistration registration) {
        short protocolId = registration.getId();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var luaBuilder = new StringBuilder();
        luaBuilder.append(StringUtils.format("function {}:protocolId()", protocolClazzName)).append(LS);
        luaBuilder.append(TAB).append(StringUtils.format("return {}", protocolId)).append(LS);
        luaBuilder.append("end").append(LS).append(LS);

        return luaBuilder.toString();
    }


    private static String writePacket(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var luaBuilder = new StringBuilder();
        luaBuilder.append(StringUtils.format("function {}:write(byteBuffer, packet)", protocolClazzName)).append(LS);

        luaBuilder.append(TAB).append("if packet == null then").append(LS);
        luaBuilder.append(TAB + TAB).append("byteBuffer:writeBoolean(false)").append(LS);
        luaBuilder.append(TAB + TAB).append("return").append(LS);
        luaBuilder.append(TAB).append("end").append(LS);

        luaBuilder.append(TAB).append("byteBuffer:writeBoolean(true)").append(LS);


        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            luaSerializer(fieldRegistration.serializer()).writeObject(luaBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }

        luaBuilder.append("end").append(LS).append(LS);
        return luaBuilder.toString();
    }


    private static String readPacket(ProtocolRegistration registration) {
        Field[] fields = registration.getFields();
        IFieldRegistration[] fieldRegistrations = registration.getFieldRegistrations();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();
        jsBuilder.append(StringUtils.format("function {}:read(byteBuffer)", protocolClazzName)).append(LS);
        jsBuilder.append(TAB).append("if not(byteBuffer:readBoolean()) then").append(LS);
        jsBuilder.append(TAB + TAB).append("return nil").append(LS);
        jsBuilder.append(TAB).append("end").append(LS);


        jsBuilder.append(TAB).append(StringUtils.format("local packet = {}:new()", protocolClazzName)).append(LS);


        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            IFieldRegistration fieldRegistration = fieldRegistrations[i];

            String readObject = luaSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
            jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }

        jsBuilder.append(TAB).append("return packet").append(LS);

        jsBuilder.append("end").append(LS);

        return jsBuilder.toString();
    }


    private static String docToLuaDoc(String doc) {
        return doc.replaceFirst("//", "--");
    }
}
