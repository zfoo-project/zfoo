/*
 * Copyright 2021 The edap Project
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

package com.zfoo.protocol.serializer.protobuf;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.parser.Proto;
import com.zfoo.protocol.serializer.protobuf.parser.ProtoParser;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.util.*;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

public abstract class GeneratePbUtils {

    public static void create(PbGenerateOperation buildOption) {
        var protoPathFile = new File(buildOption.getProtoPath());
        if (!protoPathFile.exists()) {
            throw new RuntimeException(StringUtils.format("proto path:[{}] not exist", buildOption.getProtoPath()));
        }

        var protoFiles = FileUtils.getAllReadableFiles(protoPathFile)
                .stream()
                .filter(it -> it.getName().toLowerCase().endsWith(".proto"))
                .toList();

        if (CollectionUtils.isEmpty(protoFiles)) {
            throw new RuntimeException(StringUtils.format("There are no proto files to build in proto path:[{}]", buildOption.getProtoPath()));
        }

        var protos = parseProtoFile(protoFiles);
        generate(buildOption, protos);
    }

    public static List<Proto> parseProtoFile(List<File> protoFiles) {
        var protos = new ArrayList<Proto>();
        for (var protoFile : protoFiles) {
            var strs = FileUtils.readFileToStringList(protoFile)
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .toArray();
            var protoString = StringUtils.joinWith(FileUtils.LS, strs);
            if (StringUtils.isBlank(protoString)) {
                continue;
            }
            ProtoParser parser = new ProtoParser(protoString);
            Proto proto = parser.parse();
            proto.setName(FileUtils.fileSimpleName(protoFile.getName()));
            protos.add(proto);
        }
        return protos;
    }


    public static void generate(PbGenerateOperation buildOption, List<Proto> protos) {
        var messageOutputPath = buildOption.getOutputPath() + File.separator;
        if (StringUtils.isNotEmpty(buildOption.getJavaPackage())) {
            messageOutputPath = messageOutputPath + buildOption.getJavaPackage().replaceAll(StringUtils.PERIOD_REGEX, "/");
        }

        for (var proto : protos) {
            var pbMessages = proto.getPbMessages();
            if (CollectionUtils.isEmpty(pbMessages)) {
                continue;
            }
            for (var pbMessage : pbMessages) {
                var code = buildMessage(buildOption, protos, proto, pbMessage);
                var filePath = StringUtils.format("{}/{}/{}.java", messageOutputPath, proto.getName(), pbMessage.getName());
                FileUtils.writeStringToFile(new File(filePath), code, false);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------
    public static String getJavaType(PbField pbField) {
        String type = pbField.getType();
        if (pbField instanceof PbMapField) {
            var mapField = (PbMapField) pbField;
            type = StringUtils.format("Map<{}, {}>", getBoxJavaType(mapField.getKey().value()), getBoxJavaType(mapField.getValue()));
            return type;
        }
        return getJavaType(type);
    }

    public static String getJavaType(String type) {
        var typeProtobuf = PbType.typeOfProtobuf(type);
        if (typeProtobuf == null) {
            return type;
        }
        var javaType = typeProtobuf.javaType();
        return javaType.getTypeString();
    }

    private static String getBoxJavaType(PbField pbField) {
        return getBoxJavaType(pbField.getType());
    }

    private static String getBoxJavaType(String type) {
        var typeProtobuf = PbType.typeOfProtobuf(type);
        if (typeProtobuf == null) {
            return type;
        }
        var javaType = typeProtobuf.javaType();
        return javaType.getBoxedType();
    }

    private static Set<String> buildMessageImports(PbGenerateOperation buildOption, List<Proto> protos, Proto proto, PbMessage pbMessage) {
        var imports = new HashSet<String>();
        var pbFields = pbMessage.getFields();
        if (CollectionUtils.isEmpty(pbFields)) {
            return imports;
        }

        for (var pbField : pbFields) {
            if (pbField instanceof PbMapField) {
                imports.add(Map.class.getName());
                continue;
            }

            if (pbField.getCardinality() == PbField.Cardinality.REPEATED) {
                imports.add(List.class.getName());
            }

            buildImports(buildOption, protos, proto, pbField.getType(), imports);
        }
        return imports;
    }

    private static void buildImports(PbGenerateOperation buildOption, List<Proto> protos, Proto proto, String fieldType, Set<String> imports) {
        // 基本数据类型不需要导入
        var typeProtobuf = PbType.typeOfProtobuf(fieldType);
        if (typeProtobuf != null) {
            return;
        }

        // 属于同一个包不需要导入
        if (proto.getPbMessages().stream().anyMatch(it -> it.getName().equals(fieldType))) {
            return;
        }

        // 遍历其它的proto找到需要导入的类
        for (var pt : protos) {
            for (var msg : pt.getPbMessages()) {
                if (msg.getName().equals(fieldType)) {
                    if (StringUtils.isBlank(buildOption.getJavaPackage())) {
                        imports.add(StringUtils.format("{}.{}", pt.getName(), fieldType));
                    } else {
                        imports.add(StringUtils.format("{}.{}.{}", buildOption.getJavaPackage(), pt.getName(), fieldType));
                    }
                    return;
                }
            }
        }

        throw new RuntimeException(StringUtils.format("not found type:[{}] in proto:[{}]", fieldType, proto.getName()));
    }

    private static void buildDocComment(StringBuilder builder, PbMessage msg) {
        if (CollectionUtils.isEmpty(msg.getComments())) {
            return;
        }
        builder.append("/**").append(LS);
        msg.getComments().forEach(it -> builder.append(StringUtils.format(" * {}", it)).append(LS));
        builder.append(" */").append(LS);
    }

    private static void buildFieldComment(StringBuilder builder, PbField pbField) {
        if (CollectionUtils.isEmpty(pbField.getComments())) {
            return;
        }
        pbField.getComments().forEach(it -> builder.append(TAB).append(StringUtils.format("// {}", it)).append(LS));
    }

    public static String buildMessage(PbGenerateOperation buildOption, List<Proto> protos, Proto proto, PbMessage pbMessage) {
        var builder = new StringBuilder();

        // import other class
        var imports = buildMessageImports(buildOption, protos, proto, pbMessage);
        imports.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(it -> builder.append(StringUtils.format("import {};", it)).append(LS));

        buildDocComment(builder, pbMessage);
        builder.append(StringUtils.format("public class {} {", pbMessage.getName())).append(LS);

        var pbFields = pbMessage.getFields()
                .stream()
                .sorted((a, b) -> a.getTag() - b.getTag())
                .toList();

        var builderMethod = new StringBuilder();
        for (var pbField : pbFields) {
            buildFieldComment(builder, pbField);
            String type = getJavaType(pbField);
            String name = pbField.getName();
            if (pbField.getCardinality() == PbField.Cardinality.REPEATED) {
                String boxedTypeName = getBoxJavaType(pbField);
                type = "List<" + boxedTypeName + ">";
            }

            builder.append(TAB).append(StringUtils.format("private {} {};", type, name)).append(LS);

            String getMethod;
            if (!"bool".equalsIgnoreCase(pbField.getType())) {
                getMethod = StringUtils.format("get{}", StringUtils.capitalize(pbField.getName()));
            } else {
                getMethod = StringUtils.format("is{}", StringUtils.capitalize(pbField.getName()));
            }

            builderMethod.append(TAB).append(StringUtils.format("public {} {}() {", type, getMethod)).append(LS);
            builderMethod.append(TAB + TAB).append(StringUtils.format("return {};", pbField.getName())).append(LS);
            builderMethod.append(TAB).append("}").append(LS);

            String setMethod = StringUtils.format("set{}", StringUtils.capitalize(pbField.getName()));
            builderMethod.append(TAB).append(StringUtils.format("public void {}({} {}) {", setMethod, type, pbField.getName())).append(LS);
            builderMethod.append(TAB + TAB).append(StringUtils.format("this.{} = {};", pbField.getName(), pbField.getName())).append(LS);
            builderMethod.append(TAB).append("}").append(LS);
        }

        builder.append(LS).append(builderMethod);
        builder.append("}");
        return builder.toString();
    }


}
