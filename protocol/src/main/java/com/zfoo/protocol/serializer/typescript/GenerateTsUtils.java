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

package com.zfoo.protocol.serializer.typescript;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.generate.GenerateOperation;
import com.zfoo.protocol.generate.GenerateProtocolDocument;
import com.zfoo.protocol.generate.GenerateProtocolFile;
import com.zfoo.protocol.generate.GenerateProtocolPath;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.registration.IProtocolRegistration;
import com.zfoo.protocol.registration.ProtocolAnalysis;
import com.zfoo.protocol.registration.ProtocolRegistration;
import com.zfoo.protocol.registration.anno.Compatible;
import com.zfoo.protocol.serializer.enhance.EnhanceObjectProtocolSerializer;
import com.zfoo.protocol.serializer.javascript.*;
import com.zfoo.protocol.serializer.reflect.*;
import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.IOUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

/**
 * @author jaysunxiao
 * @version 3.0
 */
public abstract class GenerateTsUtils {

    private static String protocolOutputRootPath = "tsProtocol/";

    public static void init(GenerateOperation generateOperation) {
        protocolOutputRootPath = FileUtils.joinPath(generateOperation.getProtocolPath(), protocolOutputRootPath);

        FileUtils.deleteFile(new File(protocolOutputRootPath));
        FileUtils.createDirectory(protocolOutputRootPath);
    }

    public static void clear() {
        protocolOutputRootPath = null;
    }

    public static void createProtocolManager(List<IProtocolRegistration> protocolList) throws IOException {
        var list = List.of("typescript/buffer/ByteBuffer.ts", "typescript/buffer/long.js", "typescript/buffer/longbits.js");
        for (var fileName : list) {
            var fileInputStream = ClassUtils.getFileFromClassPath(fileName);
            var createFile = new File(StringUtils.format("{}/{}", protocolOutputRootPath, StringUtils.substringAfterFirst(fileName, "typescript/")));
            FileUtils.writeInputStreamToFile(createFile, fileInputStream);
        }

        // 生成ProtocolManager.ts文件
        var protocolManagerTemplate = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("typescript/ProtocolManagerTemplate.ts")));

        var importBuilder = new StringBuilder();
        var initProtocolBuilder = new StringBuilder();
        for (var protocol : protocolList) {
            var protocolId = protocol.protocolId();
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(protocolId);

            var path = GenerateProtocolPath.getProtocolPath(protocolId);
            if (StringUtils.isBlank(path)) {
                importBuilder.append(StringUtils.format("import {} from './{}';", protocolClassName, protocolClassName)).append(LS);
            } else {
                importBuilder.append(StringUtils.format("import {} from './{}/{}';", protocolClassName, path, protocolClassName)).append(LS);
            }
            initProtocolBuilder.append(StringUtils.format("protocols.set({}, {});", protocolId, protocolClassName)).append(LS);
        }

        protocolManagerTemplate = StringUtils.format(protocolManagerTemplate, importBuilder.toString().trim(), initProtocolBuilder.toString().trim());
        FileUtils.writeStringToFile(new File(StringUtils.format("{}/{}", protocolOutputRootPath, "ProtocolManager.ts")), protocolManagerTemplate);
    }

    public static void createTsProtocolFile(ProtocolRegistration registration) throws IOException {
        // 初始化index
        GenerateProtocolFile.index.set(0);

        var protocolId = registration.protocolId();
        var registrationConstructor = registration.getConstructor();
        var protocolClazzName = registrationConstructor.getDeclaringClass().getSimpleName();

        var protocolTemplate = StringUtils.bytesToString(IOUtils.toByteArray(ClassUtils.getFileFromClassPath("typescript/ProtocolTemplate.ts")));

        var importSubProtocol = importSubProtocol(registration);
        var docTitle = docTitle(registration);
        var fieldDefinition = fieldDefinition(registration);
        var valueOfMethod = valueOfMethod(registration);
        var writeObject = writeObject(registration);
        var readObject = readObject(registration);

        protocolTemplate = StringUtils.format(protocolTemplate, importSubProtocol, docTitle, protocolClazzName, fieldDefinition.trim()
                , valueOfMethod.getKey().trim(), valueOfMethod.getValue().trim(), protocolId, protocolClazzName
                , writeObject.trim(), protocolClazzName, protocolClazzName, readObject.trim(), protocolClazzName);
        var protocolOutputPath = StringUtils.format("{}/{}/{}.ts", protocolOutputRootPath
                , GenerateProtocolPath.getProtocolPath(protocolId), protocolClazzName);
        FileUtils.writeStringToFile(new File(protocolOutputPath), protocolTemplate);
    }

    private static String importSubProtocol(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var subProtocols = ProtocolAnalysis.getAllSubProtocolIds(protocolId);

        if (CollectionUtils.isEmpty(subProtocols)) {
            return StringUtils.EMPTY;
        }
        var importBuilder = new StringBuilder();
        for (var subProtocolId : subProtocols) {
            var protocolClassName = EnhanceObjectProtocolSerializer.getProtocolClassSimpleName(subProtocolId);
            var path = GenerateProtocolPath.getProtocolPath(protocolId);
            importBuilder.append(StringUtils.format("import {} from './{}';", protocolClassName, protocolClassName)).append(LS);
        }
        return importBuilder.toString();
    }

    private static String docTitle(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docTitle = protocolDocument.getKey();
        return docTitle;
    }

    private static String fieldDefinition(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var fieldDefinitionBuilder = new StringBuilder();

        var protocolDocument = GenerateProtocolDocument.getProtocolDocument(protocolId);
        var docFieldMap = protocolDocument.getValue();

        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];

            var propertyName = field.getName();
            // 生成注释
            var doc = docFieldMap.get(propertyName);
            if (StringUtils.isNotBlank(doc)) {
                Arrays.stream(doc.split(LS)).forEach(it -> fieldDefinitionBuilder.append(TAB).append(it).append(LS));
            }

            var propertyTypeAndName = GenerateJsUtils.jsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            fieldDefinitionBuilder.append(TAB).append(StringUtils.format("{}: {};", propertyTypeAndName.getValue(), propertyTypeAndName.getKey())).append(LS);
        }
        return fieldDefinitionBuilder.toString();
    }

    private static Pair<String, String> valueOfMethod(ProtocolRegistration registration) {
        var protocolId = registration.getId();
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();

        var filedList = new ArrayList<Pair<String, String>>();
        for (int i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            var propertyTypeAndName = GenerateJsUtils.jsSerializer(fieldRegistration.serializer()).field(field, fieldRegistration);
            filedList.add(propertyTypeAndName);
        }

        var valueOfParams = filedList.stream()
                .map(it -> StringUtils.format("{}: {}", it.getValue(), it.getKey()))
                .collect(Collectors.toList());
        var valueOfParamsStr = StringUtils.joinWith(StringUtils.COMMA + " ", valueOfParams.toArray());

        var cppBuilder = new StringBuilder();
        filedList.forEach(it -> cppBuilder.append(TAB + TAB).append(StringUtils.format("this.{} = {};", it.getValue(), it.getValue())).append(LS));
        return new Pair<>(valueOfParamsStr, cppBuilder.toString());
    }

    private static String writeObject(ProtocolRegistration registration) {
        var fields = registration.getFields();
        var fieldRegistrations = registration.getFieldRegistrations();
        var jsBuilder = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            var field = fields[i];
            var fieldRegistration = fieldRegistrations[i];
            GenerateJsUtils.jsSerializer(fieldRegistration.serializer()).writeObject(jsBuilder, "packet." + field.getName(), 2, field, fieldRegistration);
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
                jsBuilder.append(TAB).append("if (!buffer.isReadable()) {").append(LS);
                jsBuilder.append(TAB + TAB).append("return packet;").append(LS);
                jsBuilder.append(TAB).append("}").append(LS);
            }
            var readObject = GenerateJsUtils.jsSerializer(fieldRegistration.serializer()).readObject(jsBuilder, 2, field, fieldRegistration);
            jsBuilder.append(TAB + TAB).append(StringUtils.format("packet.{} = {};", field.getName(), readObject)).append(LS);
        }
        return jsBuilder.toString();
    }
}
