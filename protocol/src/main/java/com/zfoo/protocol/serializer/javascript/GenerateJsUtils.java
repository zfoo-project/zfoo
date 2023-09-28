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

package com.zfoo.protocol.serializer.javascript;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
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

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateJsUtils {

    // custom configuration
    public static String protocolOutputRootPath = "zfoojs";
    public static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IJsSerializer> jsSerializerMap;


    public static IJsSerializer jsSerializer(ISerializer serializer) {
        return jsSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        // if not specify output path, then use current default path
        if (StringUtils.isEmpty(generateOperation.getProtocolPath())) {
            protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        } else {
            protocolOutputPath = generateOperation.getProtocolPath();
        }
        FileUtils.deleteFile(new File(protocolOutputPath));

        jsSerializerMap = new HashMap<>();
        jsSerializerMap.put(BooleanSerializer.INSTANCE, new JsBooleanSerializer());
        jsSerializerMap.put(ByteSerializer.INSTANCE, new JsByteSerializer());
        jsSerializerMap.put(ShortSerializer.INSTANCE, new JsShortSerializer());
        jsSerializerMap.put(IntSerializer.INSTANCE, new JsIntSerializer());
        jsSerializerMap.put(LongSerializer.INSTANCE, new JsLongSerializer());
        jsSerializerMap.put(FloatSerializer.INSTANCE, new JsFloatSerializer());
        jsSerializerMap.put(DoubleSerializer.INSTANCE, new JsDoubleSerializer());
        jsSerializerMap.put(StringSerializer.INSTANCE, new JsStringSerializer());
        jsSerializerMap.put(ArraySerializer.INSTANCE, new JsArraySerializer());
        jsSerializerMap.put(ListSerializer.INSTANCE, new JsListSerializer());
        jsSerializerMap.put(SetSerializer.INSTANCE, new JsSetSerializer());
        jsSerializerMap.put(MapSerializer.INSTANCE, new JsMapSerializer());
        jsSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new JsObjectProtocolSerializer());
    }

    public static void clear() {
        protocolOutputRootPath = null;
        protocolOutputPath = null;
        jsSerializerMap = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("javascript/buffer/ByteBuffer.js", "javascript/buffer/long.js", "javascript/buffer/longbits.js");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "javascript/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        // 生成ProtocolManager.js文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolManagerTemplate.js");

        var importBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocol.protocolId(), CodeLanguage.JavaScript);
            importBuilder.append(StringUtils.format("import {} from './{}.js';", protocolName, path)).append(LS);
            initProtocolBuilder.append(StringUtils.format("protocols.set({}, {});", protocolId, protocolName)).append(LS);
        }

        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), StringUtils.EMPTY_JSON, initProtocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.js")), protocolManagerTemplate, true);
    }

    public static void createJsProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("javascript/ProtocolTemplate.js");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.JavaScript);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName
                , fieldDefinition.trim(), protocolClazzName, protocolId, protocolClazzName
                , writeObject.trim(), protocolClazzName, protocolClazzName, readObject.trim(), protocolClazzName);
        var outputPath = StringUtils.format("{}/{}/{}.js", protocolOutputPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(outputPath), protocolTemplate, true);
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var fieldDefinitionBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            IFieldRegistration fieldRegistration = fieldRegistrations[i];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.JavaScript);
            if (StringUtils.isNotBlank(fieldNote)) {
                fieldDefinitionBuilder.append(TAB).append(fieldNote).append(LS);
            }
            var triple = jsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(TAB)
                    .append(StringUtils.format("this.{} = {}; // {}", fieldName, triple.getRight(), triple.getLeft()))
                    .append(LS);

        }
        return fieldDefinitionBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var jsBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            jsBuilder.append("const beforeWriteIndex = buffer.getWriteOffset();").append(LS);
            jsBuilder.append(TAB).append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            jsBuilder.append(TAB).append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            jsSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            jsBuilder.append(TAB).append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return jsBuilder.toString();
    }

    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var jsBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                jsBuilder.append(TAB).append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 2, field, fieldRegistration);
                jsBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                jsBuilder.append(TAB).append("}").append(LS);
                continue;
            }
            var readObject = jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
            jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return jsBuilder.toString();
    }
}
