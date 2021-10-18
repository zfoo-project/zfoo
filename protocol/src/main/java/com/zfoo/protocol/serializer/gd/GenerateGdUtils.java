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

package com.zfoo.protocol.serializer.gd;

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

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class GenerateGdUtils {

    private static final String PROTOCOL_OUTPUT_ROOT_PATH = "gdProtocol/";

    private static Map<ISerializer, IGdSerializer> gdSerializerMap;

    public static IGdSerializer gdSerializer(ISerializer serializer) {
        return gdSerializerMap.get(serializer);
    }

    public static void init() {
        FileUtils.deleteFile(new File(PROTOCOL_OUTPUT_ROOT_PATH));
        FileUtils.createDirectory(PROTOCOL_OUTPUT_ROOT_PATH);

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
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("gd/buffer/ByteBuffer.gd");

        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, StringUtils.substringAfterFirst(fileName, "gd/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }


        // 生成ProtocolManager.gd文件
        var gdBuilder = new StringBuilder();

        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> {
                    var name = it.protocolConstructor().getDeclaringClass().getSimpleName();
                    var path = GenerateProtocolPath.getProtocolPath(it.protocolId());
                    gdBuilder.append(StringUtils.format("const {} = preload(\"res://gdProtocol/{}/{}.gd\")", name, path, name)).append(LS);
                });


        gdBuilder.append(LS);

        var protocolManagerStr = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("gd/ProtocolManager.gd")));
        gdBuilder.append(protocolManagerStr);

        gdBuilder.append("static func initProtocol():").append(LS);
        protocolList.stream()
                .filter(it -> Objects.nonNull(it))
                .forEach(it -> gdBuilder.append(TAB).append(StringUtils.format("protocols[{}] = {}", it.protocolId(), it.protocolConstructor().getDeclaringClass().getSimpleName())).append(LS));

        FileUtils.writeStringToFile(new File(StringUtils.format("{}{}", PROTOCOL_OUTPUT_ROOT_PATH, "ProtocolManager.gd")), gdBuilder.toString());
    }

    public static void createGdProtocolFile(ProtocolRegistration registration) {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();

        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var gdBuilder = new StringBuilder();

        // 协议的属性生成
        gdBuilder.append(protocolClass(registration));

        // protocolId
        gdBuilder.append(protocolIdField(registration));

        // writeObject method
        gdBuilder.append(writeObject(registration));

        // readObject method
        gdBuilder.append(readObject(registration));

        var protocolOutputPath = StringUtils.format("{}{}/{}.gd"
                , PROTOCOL_OUTPUT_ROOT_PATH
                , GenerateProtocolPath.getProtocolPath(protocolId)
                , protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), gdBuilder.toString());
    }

    private static String protocolClass(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docTitle = protocolDocument.getKey();
        var docFieldMap = protocolDocument.getValue();

        var gdBuilder = new StringBuilder();

        if (!StringUtils.isBlank(docTitle)) {
            gdBuilder.append(gdDocument(docTitle)).append(LS);
        }

        for (var field : fields) {
            var propertyName = field.getName();

            // 生成注释
            var doc = docFieldMap.get(propertyName);
            if (!StringUtils.isBlank(doc)) {
                Arrays.stream(doc.split(LS)).forEach(it -> gdBuilder.append(gdDocument(it)).append(LS));
            }

            gdBuilder.append(StringUtils.format("var {}", propertyName))
                    .append(" # ").append(field.getGenericType().getTypeName()) // 生成类型的注释
                    .append(LS);
        }

        gdBuilder.append(LS);
        return gdBuilder.toString();
    }

    private static String protocolIdField(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var gdBuilder = new StringBuilder();
        gdBuilder.append(StringUtils.format("const PROTOCOL_ID = {}", protocolId)).append(LS).append(LS);
        return gdBuilder.toString();
    }


    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var gdBuilder = new StringBuilder();
        gdBuilder.append(StringUtils.format("static func write(buffer, packet):")).append(LS);

        gdBuilder.append(TAB).append("if (buffer.writePacketFlag(packet)):").append(LS);
        gdBuilder.append(TAB + TAB).append("return").append(LS);

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            IFieldRegistration fieldRegistration = fieldRegistrations[i];

            gdSerializer(fieldRegistration.serializer()).writeObject(gdBuilder, "packet." + field.getName(), 1, field, fieldRegistration);
        }

        gdBuilder.append(LS).append(LS);
        return gdBuilder.toString();
    }


    private static String readObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var protocolClazzName = registration.getConstructor().getDeclaringClass().getSimpleName();

        var jsBuilder = new StringBuilder();
        jsBuilder.append(StringUtils.format("static func read(buffer):")).append(LS);
        jsBuilder.append(TAB).append("if (!buffer.readBool()):").append(LS);
        jsBuilder.append(TAB + TAB).append("return null").append(LS);


        jsBuilder.append(TAB).append(StringUtils.format("var packet = buffer.newInstance({})", registration.protocolId())).append(LS);


        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            var readObject = gdSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 1, field, fieldRegistration);
            jsBuilder.append(TAB).append(StringUtils.format("packet.{} = {}", field.getName(), readObject)).append(LS);
        }

        jsBuilder.append(TAB).append("return packet").append(LS);
        return jsBuilder.toString();
    }

    private static String gdDocument(String doc) {
        return doc.replaceAll("//", "#");
    }

}
