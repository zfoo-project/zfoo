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

package com.zfoo.protocol.serializer.es;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
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
public abstract class GenerateEsUtils {
    private static final Logger logger = LoggerFactory.getLogger(GenerateEsUtils.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfooes";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IEsSerializer> esSerializerMap;


    public static IEsSerializer esSerializer(ISerializer serializer) {
        return esSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        esSerializerMap = new HashMap<>();
        esSerializerMap.put(BooleanSerializer.INSTANCE, new EsBooleanSerializer());
        esSerializerMap.put(ByteSerializer.INSTANCE, new EsByteSerializer());
        esSerializerMap.put(ShortSerializer.INSTANCE, new EsShortSerializer());
        esSerializerMap.put(IntSerializer.INSTANCE, new EsIntSerializer());
        esSerializerMap.put(LongSerializer.INSTANCE, new EsLongSerializer());
        esSerializerMap.put(FloatSerializer.INSTANCE, new EsFloatSerializer());
        esSerializerMap.put(DoubleSerializer.INSTANCE, new EsDoubleSerializer());
        esSerializerMap.put(StringSerializer.INSTANCE, new EsStringSerializer());
        esSerializerMap.put(ArraySerializer.INSTANCE, new EsArraySerializer());
        esSerializerMap.put(ListSerializer.INSTANCE, new EsListSerializer());
        esSerializerMap.put(SetSerializer.INSTANCE, new EsSetSerializer());
        esSerializerMap.put(MapSerializer.INSTANCE, new EsMapSerializer());
        esSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new EsObjectProtocolSerializer());
    }

    public static void clear() {
        protocolOutputRootPath = null;
        protocolOutputPath = null;
        esSerializerMap = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("es/buffer/ByteBuffer.mjs", "es/buffer/long.mjs", "es/buffer/longbits.mjs");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var outputPath = StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "es/"));
            var createFile = new File(outputPath);
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        // 生成ProtocolManager.mjs文件
        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("es/ProtocolManagerTemplate.mjs");

        var importBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocol.protocolId(), CodeLanguage.ES);
            importBuilder.append(StringUtils.format("import {} from './{}.mjs';", protocolName, path)).append(LS);
            initProtocolBuilder.append(StringUtils.format("protocols.set({}, {});", protocolId, protocolName)).append(LS);
        }

        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), initProtocolBuilder.toString().trim());
        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.mjs"));
        FileUtils.writeStringToFile(file, protocolManagerTemplate, true);
        logger.info("Generated ES protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    public static void createEsProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("es/ProtocolTemplate.mjs");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.ES, TAB, 0);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName
                , fieldDefinition.trim(), protocolId, protocolClazzName
                , writeObject.trim(), protocolClazzName, readObject.trim(), protocolClazzName);
        var outputPath = StringUtils.format("{}/{}/{}.mjs", protocolOutputPath, GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, protocolTemplate, true);
        logger.info("Generated ES protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        // when generate source code fields, use origin fields sort
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        var fieldDefinitionBuilder = new StringBuilder();
        for (var field : sequencedFields) {
            var fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.ES);
            for (var fieldNote : fieldNotes) {
                fieldDefinitionBuilder.append(TAB).append(fieldNote).append(LS);
            }
            var triple = esSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(TAB)
                    .append(StringUtils.format("{} = {}; // {}", fieldName, triple.getRight(), triple.getLeft()))
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
            jsBuilder.append(TAB + TAB).append(StringUtils.format("buffer.writeInt({});", registration.getPredictionLength())).append(LS);
        } else {
            jsBuilder.append(TAB + TAB).append("buffer.writeInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            esSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 2, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            jsBuilder.append(TAB + TAB).append(StringUtils.format("buffer.adjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
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
                jsBuilder.append(TAB + TAB).append("if (buffer.compatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = esSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 3, field, fieldRegistration);
                jsBuilder.append(TAB + TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                jsBuilder.append(TAB + TAB).append("}").append(LS);
                continue;
            }
            var readObject = esSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 2, field, fieldRegistration);
            jsBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return jsBuilder.toString();
    }
}
