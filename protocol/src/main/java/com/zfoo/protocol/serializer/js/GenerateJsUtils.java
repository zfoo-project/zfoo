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

package com.zfoo.protocol.serializer.js;

import com.zfoo.protocol.generate.GenerateProtocolDocument;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.registration.IProtocolRegistration;
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
public abstract class GenerateJsUtils {

    private static final String PROTOCOL_OUTPUT_ROOT_PATH = "jsProtocol/";

    private static Map<ISerializer, IJsSerializer> jsSerializerMap;

    public static IJsSerializer jsSerializer(ISerializer serializer) {
        return jsSerializerMap.get(serializer);
    }

    public static void init() {
        FileUtils.deleteFile(new File(PROTOCOL_OUTPUT_ROOT_PATH));
        FileUtils.createDirectory(PROTOCOL_OUTPUT_ROOT_PATH);

        jsSerializerMap = new HashMap<>();
        jsSerializerMap.put(BooleanSerializer.INSTANCE, new JsBooleanSerializer());
        jsSerializerMap.put(ByteSerializer.INSTANCE, new JsByteSerializer());
        jsSerializerMap.put(ShortSerializer.INSTANCE, new JsShortSerializer());
        jsSerializerMap.put(IntSerializer.INSTANCE, new JsIntSerializer());
        jsSerializerMap.put(LongSerializer.INSTANCE, new JsLongSerializer());
        jsSerializerMap.put(FloatSerializer.INSTANCE, new JsFloatSerializer());
        jsSerializerMap.put(DoubleSerializer.INSTANCE, new JsDoubleSerializer());
        jsSerializerMap.put(CharSerializer.INSTANCE, new JsCharSerializer());
        jsSerializerMap.put(StringSerializer.INSTANCE, new JsStringSerializer());
        jsSerializerMap.put(ArraySerializer.INSTANCE, new JsArraySerializer());
        jsSerializerMap.put(ListSerializer.INSTANCE, new JsListSerializer());
        jsSerializerMap.put(SetSerializer.INSTANCE, new JsSetSerializer());
        jsSerializerMap.put(MapSerializer.INSTANCE, new JsMapSerializer());
        jsSerializerMap.put(ObjectProtocolSerializer.INSTANCE, new JsObjectProtocolSerializer());
    }

    public static void clear() {
        jsSerializerMap = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("js/buffer/ByteBuffer.js"
                , "js/buffer/long.js"
                , "js/buffer/longbits.js");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, StringUtils.substringAfterFirst(fileName, "js/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }


        // 生成ProtocolManager.js文件
        var jsBuilder = new StringBuilder();

        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> {
                    var name = it.protocolConstructor().getDeclaringClass().getSimpleName();
                    var path = GenerateProtocolPath.getProtocolPath(it.protocolId());
                    if (StringUtils.isBlank(path)) {
                        jsBuilder.append(StringUtils.format("import {} from './{}.js';", name, name)).append(LS);
                    } else {
                        jsBuilder.append(StringUtils.format("import {} from './{}/{}.js';", name, path, name)).append(LS);
                    }
                });

        jsBuilder.append(LS).append(LS);

        var protocolManagerStr = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("js/ProtocolManager.js")));
        jsBuilder.append(protocolManagerStr);

        jsBuilder.append("ProtocolManager.initProtocol = function initProtocol() {").append(LS);
        protocolList.stream().filter(it -> Objects.nonNull(it))
                .forEach(it -> jsBuilder.append(TAB).append(StringUtils.format("protocols.set({}, {});", it.protocolId(), it.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS));
        jsBuilder.append("};").append(LS + LS);

        jsBuilder.append("export default ProtocolManager;").append(LS);

        FileUtils.writeStringToFile(new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, "ProtocolManager.js")), jsBuilder.toString());
    }

    public static void createJsProtocolFile(ProtocolRegistration registration) {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();

        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();

        // export object
        jsBuilder.append(exportFunction(registration));

        // protocolId method
        jsBuilder.append(protocolIdFunction(registration));

        // writeObject method
        jsBuilder.append(writeObject(registration));

        // readObject method
        jsBuilder.append(readObject(registration));


        jsBuilder.append(LS).append(StringUtils.format("export default {};", protocolClazzName)).append(LS);


        var protocolOutputPath = StringUtils.format("{}{}/{}.js"
                , PROTOCOL_OUTPUT_ROOT_PATH
                , GenerateProtocolPath.getProtocolPath(protocolId)
                , protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), jsBuilder.toString());
    }

    private static String exportFunction(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docTitle = protocolDocument.getKey();
        var docFieldMap = protocolDocument.getValue();

        var jsBuilder = new StringBuilder();

        if (!StringUtils.isBlank(docTitle)) {
            jsBuilder.append(docTitle).append(LS);
        }

        jsBuilder.append(StringUtils.format("const {} = function(", protocolClazzName));
        jsBuilder.append(StringUtils.joinWith(", ", Arrays.stream(fields).map(it -> it.getName()).collect(Collectors.toList()).toArray()))
                .append(") {")
                .append(LS);

        for (var field : fields) {
            var propertyName = field.getName();

            // 生成注释
            var doc = docFieldMap.get(propertyName);
            if (!StringUtils.isBlank(doc)) {
                Arrays.stream(doc.split(LS)).forEach(it -> jsBuilder.append(TAB).append(it).append(LS));
            }

            jsBuilder.append(TAB)
                    .append(StringUtils.format("this.{} = {};", propertyName, propertyName))
                    .append(" // ").append(field.getGenericType().getTypeName())// 生成类型的注释
                    .append(LS);
        }

        jsBuilder.append("};").append(LS).append(LS);
        return jsBuilder.toString();
    }

    private static String protocolIdFunction(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();
        jsBuilder.append(StringUtils.format("{}.prototype.protocolId = function() {", protocolClazzName)).append(LS);
        jsBuilder.append(TAB).append(StringUtils.format("return {};", protocolId)).append(LS);
        jsBuilder.append("};").append(LS).append(LS);

        return jsBuilder.toString();
    }


    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();
        jsBuilder.append(StringUtils.format("{}.write = function(buffer, packet) {", protocolClazzName)).append(LS);

        jsBuilder.append(TAB).append("if (buffer.writePacketFlag(packet)) {").append(LS);
        jsBuilder.append(TAB + TAB).append("return;").append(LS);
        jsBuilder.append(TAB).append("}").append(LS);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            IFieldRegistration fieldRegistration = fieldRegistrations[i];

            jsSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }

        jsBuilder.append("};").append(LS).append(LS);
        return jsBuilder.toString();
    }


    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();
        jsBuilder.append(StringUtils.format("{}.read = function(buffer) {", protocolClazzName)).append(LS);
        jsBuilder.append(TAB).append("if (!buffer.readBoolean()) {").append(LS);
        jsBuilder.append(TAB + TAB).append("return null;").append(LS);
        jsBuilder.append(TAB).append("}").append(LS);


        jsBuilder.append(TAB).append(StringUtils.format("const packet = new {}();", protocolClazzName)).append(LS);


        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            var readObject = jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
            jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }

        jsBuilder.append(TAB).append("return packet;").append(LS);

        jsBuilder.append("};").append(LS);

        return jsBuilder.toString();
    }


}
