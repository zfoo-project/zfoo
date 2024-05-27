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

package com.zfoo.protocol.serializer.go;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.collection.ArrayUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
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
import java.util.Objects;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateGoUtils {
    private static final Logger logger = LoggerFactory.getLogger(GenerateGoUtils.class);
    // custom configuration
    public static String protocolOutputRootPath = "zfoogo";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, IGoSerializer> goSerializerMap;

    public static IGoSerializer goSerializer(ISerializer serializer) {
        return goSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        FileUtils.deleteFile(new File(protocolOutputPath));

        goSerializerMap = new HashMap<>();
        goSerializerMap.put(BooleanSerializer.INSTANCE, new GoBooleanSerializer());
        goSerializerMap.put(ByteSerializer.INSTANCE, new GoByteSerializer());
        goSerializerMap.put(ShortSerializer.INSTANCE, new GoShortSerializer());
        goSerializerMap.put(IntSerializer.INSTANCE, new GoIntSerializer());
        goSerializerMap.put(LongSerializer.INSTANCE, new GoLongSerializer());
        goSerializerMap.put(FloatSerializer.INSTANCE, new GoFloatSerializer());
        goSerializerMap.put(DoubleSerializer.INSTANCE, new GoDoubleSerializer());
        goSerializerMap.put(StringSerializer.INSTANCE, new GoStringSerializer());
        goSerializerMap.put(ArraySerializer.INSTANCE, new GoArraySerializer());
        goSerializerMap.put(ListSerializer.INSTANCE, new GoListSerializer());
        goSerializerMap.put(SetSerializer.INSTANCE, new GoSetSerializer());
        goSerializerMap.put(MapSerializer.INSTANCE, new GoMapSerializer());
        goSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new GoObjectProtocolSerializer());
    }

    public static void clear() {
        goSerializerMap = null;
        protocolOutputRootPath = null;
        protocolOutputPath = null;
    }

    /**
     * 生成协议依赖的工具类
     */
    public static void createProtocolManager(List<ProtocolRegistration> protocolList) throws IOException {
        var list = List.of("go/ByteBuffer.go");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "go/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var initProtocolBuilder = new StringBuilder();
        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> initProtocolBuilder.append(TAB).append(StringUtils.format("Protocols[{}] = new({})", it.protocolId(), it.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS));

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("go/ProtocolManagerTemplate.go");
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, initProtocolBuilder.toString().trim());
        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.go"));
        FileUtils.writeStringToFile(file, protocolManagerTemplate, true);
        logger.info("Generated Golang protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    public static void createGoProtocolFile(ProtocolRegistration registration) throws IOException {
        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ArrayUtils.isEmpty(registration.getFields())
                ? ClassUtils.getFileFromClassPathToString("go/ProtocolTemplateEmpty.go")
                : ClassUtils.getFileFromClassPathToString("go/ProtocolTemplate.go");


        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Go, TAB, 0);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        if (ArrayUtils.isEmpty(registration.getFields())) {
            protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, fieldDefinition.trim()
                    , protocolClazzName, protocolId, protocolClazzName, protocolClazzName, protocolClazzName);
        } else {
            protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, fieldDefinition.trim()
                    , protocolClazzName, protocolId, protocolClazzName, protocolClazzName
                    , writeObject.trim(), protocolClazzName, protocolClazzName, readObject.trim());
        }

        var outputPath = StringUtils.format("{}/{}.go", protocolOutputPath, protocolClazzName);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, protocolTemplate, true);
        logger.info("Generated Golang protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.protocolId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = StringUtils.capitalize(field.getName());
            var fieldType = goSerializer(fieldRegistration.serializer()).fieldType(field, fieldRegistration);

            var propertyFullName = StringUtils.format("{} {}", fieldName, fieldType);
            // 生成注释
            var fieldNotes = GenerateProtocolNote.fieldNotes(protocolId, fieldName, CodeLanguage.Go);
            for(var fieldNote : fieldNotes) {
                goBuilder.append(TAB).append(fieldNote).append(LS);
            }
            goBuilder.append(TAB).append(propertyFullName).append(LS);
        }
        return goBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            goBuilder.append(TAB).append("var beforeWriteIndex = buffer.WriteOffset()").append(LS);
            goBuilder.append(TAB ).append(StringUtils.format("buffer.WriteInt({})", registration.getPredictionLength())).append(LS);
        } else {
            goBuilder.append(TAB).append("buffer.WriteInt(-1)").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            goSerializer(fieldRegistration.serializer()).writeObject(goBuilder, "message." + StringUtils.capitalize(field.getName()), 1, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            goBuilder.append(TAB).append(StringUtils.format("buffer.AdjustPadding({}, beforeWriteIndex)", registration.getPredictionLength())).append(LS);
        }
        return goBuilder.toString();
    }

    private static String readObject(ProtocolRegistration registration) {
        GenerateProtocolFile.localVariableId = 0;
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var goBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                goBuilder.append(TAB ).append("if buffer.CompatibleRead(beforeReadIndex, length) {").append(LS);
                var compatibleReadObject = goSerializer(fieldRegistration.serializer()).readObject(goBuilder, 2, field, fieldRegistration);
                goBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {}", StringUtils.capitalize(field.getName()), compatibleReadObject)).append(LS);
                goBuilder.append(TAB).append("}").append(LS);
                continue;
            }
            var readObject = goSerializer(fieldRegistration.serializer()).readObject(goBuilder, 1, field, fieldRegistration);
            goBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", StringUtils.capitalize(field.getName()), readObject)).append(LS);
        }
        return goBuilder.toString();
    }

}
