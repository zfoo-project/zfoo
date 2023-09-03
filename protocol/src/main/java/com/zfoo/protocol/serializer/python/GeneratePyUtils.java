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

package com.zfoo.protocol.serializer.python;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.field.IFieldRegistration;
import com.zfoo.protocol.serializer.CodeLanguage;
import com.zfoo.protocol.serializer.csharp.GenerateCsUtils;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.ReflectionUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 * @version 3.0
 */
public abstract class GeneratePyUtils {

    private static String protocolOutputRootPath = "pyProtocol/";

    private static Map<ISerializer, IPySerializer> pySerializerMap;


    public static IPySerializer pySerializer(ISerializer serializer) {
        return pySerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);

        pySerializerMap = new HashMap<>();
        pySerializerMap.put(BooleanSerializer.INSTANCE, new PyBooleanSerializer());
        pySerializerMap.put(ByteSerializer.INSTANCE, new PyByteSerializer());
        pySerializerMap.put(ShortSerializer.INSTANCE, new PyShortSerializer());
        pySerializerMap.put(IntSerializer.INSTANCE, new PyIntSerializer());
        pySerializerMap.put(LongSerializer.INSTANCE, new PyLongSerializer());
        pySerializerMap.put(FloatSerializer.INSTANCE, new PyFloatSerializer());
        pySerializerMap.put(DoubleSerializer.INSTANCE, new PyDoubleSerializer());
        pySerializerMap.put(CharSerializer.INSTANCE, new PyCharSerializer());
        pySerializerMap.put(StringSerializer.INSTANCE, new PyStringSerializer());
        pySerializerMap.put(ArraySerializer.INSTANCE, new PyArraySerializer());
        pySerializerMap.put(ListSerializer.INSTANCE, new PyListSerializer());
        pySerializerMap.put(SetSerializer.INSTANCE, new PySetSerializer());
        pySerializerMap.put(MapSerializer.INSTANCE, new PyMapSerializer());
        pySerializerMap.put(ObjectProtocolSerializer.INSTANCE, new PyObjectProtocolSerializer());
    }

    public static void clear() {
        pySerializerMap = null;
        protocolOutputRootPath = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("python/ByteBuffer.py");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, StringUtils.substringAfterFirst(fileName, "python/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolManagerTemplate.py");

        var importBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocolId, CodeLanguage.Python);
            importBuilder.append(StringUtils.format("from {} import {}", path, protocolName)).append(LS);
            initProtocolBuilder.append(StringUtils.format("protocols[{}] = {}.{}", protocolId, protocolName, protocolName)).append(LS);
        }

        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), StringUtils.EMPTY_JSON, initProtocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.py")), protocolManagerTemplate, true);
    }

    public static void createPyProtocolFile(ProtocolRegistration registration) {
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("python/ProtocolTemplate.py");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.Python);
        var fieldDefinition = fieldDefinition(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName
                , fieldDefinition.trim(), protocolId, writeObject.trim(), protocolClazzName, readObject.trim());
        var protocolOutputPath = StringUtils.format("{}/{}/{}.py", protocolOutputRootPath
                , GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), protocolTemplate, true);
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var pyBuilder = new StringBuilder();
        // 生成源代码字段的时候，按照原始定义的方式生成
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.Python);
            if (StringUtils.isNotBlank(fieldNote)) {
                pyBuilder.append(TAB).append(fieldNote).append(LS);
            }
            var fieldDefaultValue = pySerializer(fieldRegistration.serializer()).fieldDefaultValue(field, fieldRegistration);
            // 生成类型的注释
            pyBuilder.append(StringUtils.format("{}{} = {}", TAB, fieldName, fieldDefaultValue));
            pyBuilder.append(StringUtils.format("  # {}", GenerateCsUtils.toCsClassName(field.getGenericType().getTypeName())));
            pyBuilder.append(LS);
        }
        return pyBuilder.toString();
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var pyBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            pySerializer(fieldRegistration.serializer()).writeObject(pyBuilder, "packet." + field.getName(), 2, field, fieldRegistration);
        }
        return pyBuilder.toString();
    }

    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var pyBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            if (field.isAnnotationPresent(Compatible.class)) {
                pyBuilder.append(TAB + TAB).append("if not buffer.isReadable():").append(LS);
                pyBuilder.append(TAB + TAB + TAB).append("return packet").append(LS);
                pyBuilder.append(TAB + TAB).append("pass").append(LS);
            }
            var readObject = pySerializer(fieldRegistration.serializer()).readObject(pyBuilder, 2, field, fieldRegistration);
            pyBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }
        return pyBuilder.toString();
    }

}
