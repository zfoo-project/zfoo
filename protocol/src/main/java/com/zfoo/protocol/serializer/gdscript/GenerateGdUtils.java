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

package com.zfoo.protocol.serializer.gdscript;

import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.anno.Compatible;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB_ASCII;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GenerateGdUtils {

    private static String protocolOutputRootPath = "gdProtocol/";

    private static Map<ISerializer, IGdSerializer> gdSerializerMap;

    public static IGdSerializer gdSerializer(ISerializer serializer) {
        return gdSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

        gdSerializerMap = new HashMap<>();
        gdSerializerMap.put(BooleanSerializer.INSTANCE, new GdBooleanSerializer());
        gdSerializerMap.put(ByteSerializer.INSTANCE, new GdByteSerializer());
        gdSerializerMap.put(ShortSerializer.INSTANCE, new GdShortSerializer());
        gdSerializerMap.put(IntSerializer.INSTANCE, new GdIntSerializer());
        gdSerializerMap.put(LongSerializer.INSTANCE, new GdLongSerializer());
        gdSerializerMap.put(FloatSerializer.INSTANCE, new GdFloatSerializer());
        gdSerializerMap.put(DoubleSerializer.INSTANCE, new GdDoubleSerializer());
        gdSerializerMap.put(CharSerializer.INSTANCE, new GdCharSerializer());
        gdSerializerMap.put(StringSerializer.INSTANCE, new GdStringSerializer());
        gdSerializerMap.put(ArraySerializer.INSTANCE, new GdArraySerializer());
        gdSerializerMap.put(ListSerializer.INSTANCE, new GdListSerializer());
        gdSerializerMap.put(SetSerializer.INSTANCE, new GdSetSerializer());
        gdSerializerMap.put(MapSerializer.INSTANCE, new GdMapSerializer());
        gdSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new GdObjectProtocolSerializer());
    }

    public static void clear() {
        gdSerializerMap = null;
        protocolOutputRootPath = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("gdscript/buffer/ByteBuffer.gd");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, StringUtils.substringAfterFirst(fileName, "gdscript/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("gdscript/ProtocolManagerTemplate.gd")));
        // 生成ProtocolManager.gd文件
        var importBuilder = new StringBuilder();
        var initBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.getProtocolPath(protocolId);
            importBuilder.append(StringUtils.format("const {} = preload(\"res://gdProtocol/{}/{}.gd\")", name, path, name)).append(LS);
            initBuilder.append(TAB_ASCII).append(StringUtils.format("protocols[{}] = {}", protocolId, name)).append(LS);
        }
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), StringUtils.EMPTY_JSON, initBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.gd")), protocolManagerTemplate, true);
    }

    public static void createGdProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.GdScript);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        var protocolTemplate = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("gdscript/ProtocolTemplate.gd")));
        protocolTemplate = StringUtils.format(protocolTemplate, classNote, fieldDefinition.trim(), protocolId, writeObject.trim(), readObject.trim());

        var protocolOutputPath = StringUtils.format("{}/{}/{}.gd", protocolOutputRootPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), protocolTemplate, true);
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var gdBuilder = new StringBuilder();
        for (var field : fields) {
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.GdScript);
            if (StringUtils.isNotBlank(fieldNote)) {
                gdBuilder.append(fieldNote).append(LS);
            }
            // 生成类型的注释
            gdBuilder.append(StringUtils.format("var {} # ", fieldName)).append(field.getGenericType().getTypeName()).append(LS);
        }
        return gdBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            gdSerializer(fieldRegistration.serializer()).writeObject(gdBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }
        return gdBuilder.toString();
    }

    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                gdBuilder.append(TAB_ASCII).append("if (!buffer.isReadable()):").append(LS);
                gdBuilder.append(TAB_ASCII + TAB_ASCII).append("return packet").append(LS);
            }
            var readObject = gdSerializer(fieldRegistration.serializer()).readObject(gdBuilder, 1, field, fieldRegistration);
            gdBuilder.append(TAB_ASCII).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return gdBuilder.toString();
    }

    public static StringBuilder addTab(StringBuilder builder, int deep) {
        builder.append(TAB_ASCII.repeat(Math.max(0, deep)));
        return builder;
    }
}
