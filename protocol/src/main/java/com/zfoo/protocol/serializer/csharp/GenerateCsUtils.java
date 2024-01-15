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

package com.zfoo.protocol.serializer.csharp;

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolNote;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author godotg
 */
public abstract class GenerateCsUtils {
    private static final Logger logger = LoggerFactory.getLogger(GenerateCsUtils.class);

    // custom configuration
    public static String protocolOutputRootPath = "zfoocs";
    private static String protocolOutputPath = StringUtils.EMPTY;

    private static Map<ISerializer, ICsSerializer> csSerializerMap;

    public static ICsSerializer csSerializer(ISerializer serializer) {
        return csSerializerMap.get(serializer);
    }

    public static void init(GenerateOperation generateOperation) {
        // if not specify output path, then use current default path
        if (StringUtils.isNotEmpty(generateOperation.getProtocolPath())) {
            protocolOutputPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);
        } else {
            protocolOutputPath = generateOperation.getProtocolPath();
        }
        FileUtils.deleteFile(new File(protocolOutputPath));

        csSerializerMap = new HashMap<>();
        csSerializerMap.put(BooleanSerializer.INSTANCE, new CsBooleanSerializer());
        csSerializerMap.put(ByteSerializer.INSTANCE, new CsByteSerializer());
        csSerializerMap.put(ShortSerializer.INSTANCE, new CsShortSerializer());
        csSerializerMap.put(IntSerializer.INSTANCE, new CsIntSerializer());
        csSerializerMap.put(LongSerializer.INSTANCE, new CsLongSerializer());
        csSerializerMap.put(FloatSerializer.INSTANCE, new CsFloatSerializer());
        csSerializerMap.put(DoubleSerializer.INSTANCE, new CsDoubleSerializer());
        csSerializerMap.put(StringSerializer.INSTANCE, new CsStringSerializer());
        csSerializerMap.put(ArraySerializer.INSTANCE, new CsArraySerializer());
        csSerializerMap.put(ListSerializer.INSTANCE, new CsListSerializer());
        csSerializerMap.put(SetSerializer.INSTANCE, new CsSetSerializer());
        csSerializerMap.put(MapSerializer.INSTANCE, new CsMapSerializer());
        csSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new CsObjectProtocolSerializer());
    }

    public static void clear() {
        csSerializerMap = null;
        protocolOutputRootPath = null;
        protocolOutputPath = null;
    }

    /**
     * 生成协议依赖的工具类
     */
    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("csharp/IProtocolRegistration.cs"
                , "csharp/Buffer/ByteBuffer.cs"
                , "csharp/Buffer/LittleEndianByteBuffer.cs"
                , "csharp/Buffer/BigEndianByteBuffer.cs");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputPath, StringUtils.substringAfterFirst(fileName, "csharp/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        var protocolManagerTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolManagerTemplate.cs");
        var csBuilder = new StringBuilder();
        var initList = new ArrayList<String>();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolName = protocol.protocolConstructor().getDeclaringClass().getSimpleName();
            var path = GenerateProtocolPath.protocolAbsolutePath(protocolId, CodeLanguage.GdScript);
            csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("protocols[{}] = new {}Registration();", protocolId, protocolName, path)).append(LS);
            csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("protocolIdMap[typeof({})] = {};", protocolName, protocolId, path)).append(LS);
        }
        var initProtocols = StringUtils.joinWith(StringUtils.COMMA + LS, initList.toArray());
        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, csBuilder.toString().trim(), initProtocols);
        var file = new File(StringUtils.format("{}/{}", protocolOutputPath, "ProtocolManager.cs"));
        FileUtils.writeStringToFile(file, protocolManagerTemplate, true);
        logger.info("Generated C# protocol manager file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    /**
     * 生成协议类
     */
    public static void createCsProtocolFile(ProtocolRegistration registration) throws IOException {
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = ClassUtils.getFileFromClassPathToString("csharp/ProtocolTemplate.cs");

        var classNote = GenerateProtocolNote.classNote(protocolId, CodeLanguage.CSharp);
        var fieldDefinition = fieldDefinition(registration);
        var valueOfMethod = valueOfMethod(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);
        protocolTemplate = StringUtils.format(protocolTemplate, classNote, protocolClazzName, fieldDefinition.trim()
                , protocolClazzName, valueOfMethod.getKey().trim(), protocolClazzName, valueOfMethod.getValue().trim()
                , protocolClazzName, protocolId, protocolClazzName, protocolClazzName, writeObject.trim()
                , protocolClazzName, protocolClazzName, readObject.trim());

        var outputPath = StringUtils.format("{}/{}/{}.cs"
                , protocolOutputRootPath
                , GenerateProtocolPath.getCapitalizeProtocolPath(protocolId)
                , protocolClazzName);
        var file = new File(outputPath);
        FileUtils.writeStringToFile(file, protocolTemplate, true);
        logger.info("Generated C# protocol file:[{}] is in path:[{}]", file.getName(), file.getAbsolutePath());
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        var sequencedFields = ReflectionUtils.notStaticAndTransientFields(registration.getConstructor().getDeclaringClass());
        for (int i = 0; i < sequencedFields.size(); i++) {
            var field = sequencedFields.get(i);
            IFieldRegistration fieldRegistration = fieldRegistrations[GenerateProtocolFile.indexOf(fields, field)];
            var fieldName = field.getName();
            var propertyType = toCsClassName(field.getGenericType().getTypeName());
            var propertyFullName = StringUtils.format("public {} {};", propertyType, fieldName);
            // 生成注释
            var fieldNote = GenerateProtocolNote.fieldNote(protocolId, fieldName, CodeLanguage.CSharp);
            if (StringUtils.isNotBlank(fieldNote)) {
                csBuilder.append(TAB + TAB).append(fieldNote).append(LS);
            }
            csBuilder.append(TAB + TAB).append(propertyFullName).append(LS);
        }
        return csBuilder.toString();
    }

    private static Pair<String, String> valueOfMethod(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var filedList = new ArrayList<Pair<String, String>>();
        for (var field : fields) {
            var propertyType = toCsClassName(field.getGenericType().getTypeName());
            var propertyName = field.getName();
            filedList.add(new Pair<>(propertyType, propertyName));
        }

        // ValueOf()方法
        var valueOfParams = filedList.stream().map(it -> StringUtils.format("{} {}", it.getKey(), it.getValue())).toList();
        var valueOfParamsStr = StringUtils.joinWith(StringUtils.COMMA + " ", valueOfParams.toArray());

        var csBuilder = new StringBuilder();
        filedList.forEach(it -> csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("packet.{} = {};", it.getValue(), it.getValue())).append(LS));
        return new Pair<>(valueOfParamsStr, csBuilder.toString());
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        if (registration.isCompatible()) {
            csBuilder.append("int beforeWriteIndex = buffer.WriteOffset();").append(LS);
            csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("buffer.WriteInt({});", registration.getPredictionLength())).append(LS);
        } else {
            csBuilder.append(TAB + TAB + TAB).append("buffer.WriteInt(-1);").append(LS);
        }
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            csSerializer(fieldRegistration.serializer()).writeObject(csBuilder, "message." + field.getName(), 3, field, fieldRegistration);
        }
        if (registration.isCompatible()) {
            csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("buffer.AdjustPadding({}, beforeWriteIndex);", registration.getPredictionLength())).append(LS);
        }
        return csBuilder.toString();
    }


    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var csBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            if (field.isAnnotationPresent(Compatible.class)) {
                csBuilder.append(TAB + TAB + TAB).append("if (buffer.CompatibleRead(beforeReadIndex, length)) {").append(LS);
                var compatibleReadObject = csSerializer(fieldRegistration.serializer()).readObject(csBuilder, 4, field, fieldRegistration);
                csBuilder.append(TAB + TAB + TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), compatibleReadObject)).append(LS);
                csBuilder.append(TAB + TAB + TAB).append("}").append(LS);
                continue;
            }
            var readObject = csSerializer(fieldRegistration.serializer()).readObject(csBuilder, 3, field, fieldRegistration);
            csBuilder.append(TAB + TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return csBuilder.toString();
    }

    public static String toCsClassName(String typeName) {
        typeName = typeName.replaceAll("java.util.|java.lang.", StringUtils.EMPTY);
        typeName = typeName.replaceAll("[a-zA-Z0-9_.]*\\.", StringUtils.EMPTY);

        // CSharp不适用基础类型的泛型，会影响性能
        switch (typeName) {
            case "boolean":
            case "Boolean":
                typeName = "bool";
                return typeName;
            case "Byte":
                typeName = "byte";
                return typeName;
            case "Short":
                typeName = "short";
                return typeName;
            case "Integer":
                typeName = "int";
                return typeName;
            case "Long":
                typeName = "long";
                return typeName;
            case "Float":
                typeName = "float";
                return typeName;
            case "Double":
                typeName = "double";
                return typeName;
            case "Character":
                typeName = "char";
                return typeName;
            case "String":
                typeName = "string";
                return typeName;
            default:
        }

        // 将boolean转为bool
        typeName = typeName.replace(" boolean ", " bool ");
        typeName = typeName.replace(" Boolean ", " bool ");
        typeName = typeName.replaceAll("[B|b]oolean\\[", "bool[");
        typeName = typeName.replace("<Boolean", "<bool");
        typeName = typeName.replace("Boolean>", "bool>");

        // 将Byte转为byte
        typeName = typeName.replace(" Byte ", " byte ");
        typeName = typeName.replace("Byte[", "byte[");
        typeName = typeName.replace("Byte>", "byte>");
        typeName = typeName.replace("<Byte", "<byte");

        // 将Short转为short
        typeName = typeName.replace(" Short ", " short ");
        typeName = typeName.replace("Short[", "short[");
        typeName = typeName.replace("Short>", "short>");
        typeName = typeName.replace("<Short", "<short");

        // 将Integer转为int
        typeName = typeName.replace(" Integer ", " int ");
        typeName = typeName.replace("Integer[", "int[");
        typeName = typeName.replace("Integer>", "int>");
        typeName = typeName.replace("<Integer", "<int");


        // 将Long转为long
        typeName = typeName.replace(" Long ", " long ");
        typeName = typeName.replace("Long[", "long[");
        typeName = typeName.replace("Long>", "long>");
        typeName = typeName.replace("<Long", "<long");

        // 将Float转为float
        typeName = typeName.replace(" Float ", " float ");
        typeName = typeName.replace("Float[", "float[");
        typeName = typeName.replace("Float>", "float>");
        typeName = typeName.replace("<Float", "<float");

        // 将Double转为double
        typeName = typeName.replace(" Double ", " double ");
        typeName = typeName.replace("Double[", "double[");
        typeName = typeName.replace("Double>", "double>");
        typeName = typeName.replace("<Double", "<double");

        // 将Character转为Char
        typeName = typeName.replace(" Character ", " char ");
        typeName = typeName.replace("Character[", "char[");
        typeName = typeName.replace("Character>", "char>");
        typeName = typeName.replace("<Character", "<char");

        // 将String转为string
        typeName = typeName.replace("String ", "string ");
        typeName = typeName.replace("String[", "string[");
        typeName = typeName.replace("String>", "string>");
        typeName = typeName.replace("<String", "<string");

        // 将Map转为Dictionary
        typeName = typeName.replace("Map<", "Dictionary<");

        // 将Set转为HashSet
        typeName = typeName.replace("Set<", "HashSet<");

        // 将private转为public
        typeName = typeName.replace(" private ", " public ");

        return typeName;
    }
}
