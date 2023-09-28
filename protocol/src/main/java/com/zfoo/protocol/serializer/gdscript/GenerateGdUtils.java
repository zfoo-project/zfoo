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

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.registration.field.MapField;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.serializer.typescript.GenerateTsUtils;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB_ASCII;

/**
 * @author godotg
 */
public abstract class GenerateGdUtils {

    // custom configuration
    public static String protocolOutputRootPath = "zfoogd";
    public static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IGdSerializer> gdSerializerMap;

    public static IGdSerializer gdSerializer(ISerializer serializer) {
        return gdSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        // if not specify output path, then use current default path
        if (StringUtils.isEmpty(generateOperation.getProtocolPath())) {
            protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        } else {
            protocolOutputPath = generateOperation.getProtocolPath();
        }
        FileUtils.deleteFile(new File(protocolOutputPath));

        gdSerializerMap = new HashMap<>();
        gdSerializerMap.put(BooleanSerializer.INSTANCE, new GdBooleanSerializer());
        gdSerializerMap.put(ByteSerializer.INSTANCE, new GdByteSerializer());
        gdSerializerMap.put(ShortSerializer.INSTANCE, new GdShortSerializer());
        gdSerializerMap.put(IntSerializer.INSTANCE, new GdIntSerializer());
        gdSerializerMap.put(LongSerializer.INSTANCE, new GdLongSerializer());
        gdSerializerMap.put(FloatSerializer.INSTANCE, new GdFloatSerializer());
        gdSerializerMap.put(DoubleSerializer.INSTANCE, new GdDoubleSerializer());
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
        protocolOutputPath = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var byteBufferFile = new File(StringUtils.format("{}/{}", protocolOutputPath, "ByteBuffer.gd"));
        var byteBufferTemplate = ClassUtils.getFileFromClassPathToString("gdscript/buffer/ByteBuffer.gd");
        FileUtils.writeStringToFile(byteBufferFile, StringUtils.format(byteBufferTemplate, protocolOutputRootPath), false);

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolManagerTemplate.gd");
        // 生成ProtocolManager.gd文件
        var importBuilder = new StringBuilder();
        var initList = new ArrayList<String>();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var name = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocolId, CodeLanguage.GdScript);
            importBuilder.append(StringUtils.format("const {} = preload(\"res://{}/{}.gd\")", name, protocolOutputRootPath, path)).append(LS);
            initList.add(StringUtils.format("{}{}: {}", TAB_ASCII, protocolId, name));
        }
        var initProtocols = StringUtils.joinWith(StringUtils.COMMA + LS, initList.toArray());
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), initProtocols);
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.gd")), protocolManagerTemplate, true);
    }

    public static void createGdProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var includeSubProtocol = includeSubProtocol(registration);
        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.GdScript);
        var fieldDefinition = fieldDefinition(registration);
        var toStringMethod = toStringMethod(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("gdscript/ProtocolTemplate.gd");
        protocolTemplate = StringUtils.format(protocolTemplate, protocolId, protocolClazzName, includeSubProtocol, classNote, fieldDefinition.trim(),
                StringUtils.EMPTY_JSON, toStringMethod, writeObject.trim(), readObject.trim());

        var outputPath = StringUtils.format("{}/{}/{}.gd", protocolOutputPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(outputPath), protocolTemplate, true);
    }

    private static String includeSubProtocol(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);

        if (CollectionUtils.isEmpty(subProtocols)) {
            return StringUtils.EMPTY;
        }
        var gdBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var name = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.protocolAbsolutePath(subProtocolId, CodeLanguage.GdScript);
            gdBuilder.append(StringUtils.format("const {} = preload(\"res://{}/{}.gd\")", name, protocolOutputRootPath, path)).append(LS);
        }
        return gdBuilder.toString();
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        // 生成源代码字段的时候，按照原始定义的方式生成
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.GdScript);
            if (StringUtils.isNotBlank(fieldNote)) {
                gdBuilder.append(fieldNote).append(LS);
            }
            var fieldType = gdSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);
            // 生成类型的注释
            gdBuilder.append(StringUtils.format("var {}: {}", fieldName, fieldType));
            if (fieldType.equals("Dictionary") || fieldType.equals("Array")) {
                var typeNote = GenerateTsUtils.toTsClassName(field.getGenericType().toString());
                gdBuilder.append(StringUtils.format(TAB_ASCII  + "# {}", typeNote));
            }
            gdBuilder.append(LS);
        }
        return gdBuilder.toString();
    }

    private static String toStringMethod(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var gdBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            gdBuilder.append(TAB_ASCII).append(StringUtils.format("map[\"{}\"] = {}", fieldName, fieldName));
            if (i != sequencedFields.size() - 1) {
                gdBuilder.append(LS);
            }
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

}
