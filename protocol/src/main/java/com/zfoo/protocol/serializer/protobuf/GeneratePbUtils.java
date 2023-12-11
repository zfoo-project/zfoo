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

import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.anno.Note;
import com.zfoo.protocol.anno.Protocol;
import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.serializer.protobuf.parser.Proto;
import com.zfoo.protocol.serializer.protobuf.parser.ProtoParser;
import com.zfoo.protocol.util.FileUtils;
import com.zfoo.protocol.util.StringUtils;

import java.io.File;
import java.util.*;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

public abstract class GeneratePbUtils {

    /**
     * EN: If the tag of a protobuf field exceeds this value, this field is considered to be a compatible protocol field
     * CN: 如果protobuf的字段的tag超过这个值，则视这个字段为需要兼容的协议字段
     */
    public static final int COMPATIBLE_FIELD_TAG = 1000;

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

            // parse protobuf proto
            ProtoParser parser = new ProtoParser(protoString);
            Proto proto = parser.parse();
            proto.setName(FileUtils.fileSimpleName(protoFile.getName()));
            protos.add(proto);

            // auto generate protocolId
            var startProtocolId = proto.getStartProtocolId();
            for (var pbMessage : proto.getPbMessages()) {
                pbMessage.setProtocolId(startProtocolId++);
            }
        }

        // check duplicate protocolId and protocolName
        var protocolIdMap = new HashMap<Short, Pair<Proto, PbMessage>>();
        var protocolNameMap = new HashMap<String, Pair<Proto, PbMessage>>();
        for (var proto : protos) {
            for (var pbMessage : proto.getPbMessages()) {
                var protocolId = pbMessage.getProtocolId();
                var protocolName = pbMessage.getName();
                if (protocolIdMap.containsKey(protocolId)) {
                    var pair = protocolIdMap.get(protocolId);
                    throw new RuntimeException(StringUtils.format("duplicate protocolId:[{}] in [{}]:[{}] and [{}]:[{}], consider proto start_protocol_id is too close"
                            , protocolId, pair.getKey().getName(), pair.getValue().getName(), proto.getName(), protocolName));
                }
                if (protocolNameMap.containsKey(protocolName)) {
                    var pair = protocolNameMap.get(protocolName);
                    throw new RuntimeException(StringUtils.format("duplicate protocol name in [{}]:[{}] and [{}]:[{}]"
                            , pair.getKey().getName(), pair.getValue().getName(), proto.getName(), protocolName));
                }
                protocolIdMap.put(protocolId, new Pair<>(proto, pbMessage));
                protocolNameMap.put(protocolName, new Pair<>(proto, pbMessage));
            }
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
        if (pbField.getCardinality() == PbField.Cardinality.REPEATED) {
            var boxedTypeName = getBoxJavaType(pbField);
            type = StringUtils.format("List<?>", boxedTypeName);
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
        imports.add(Protocol.class.getName());

        var pbFields = pbMessage.getFields();
        if (CollectionUtils.isEmpty(pbFields)) {
            return imports;
        }

        for (var pbField : pbFields) {
            if (CollectionUtils.isNotEmpty(pbField.getComments())) {
                imports.add(Note.class.getName());
            }

            if (pbField.getTag() >= COMPATIBLE_FIELD_TAG) {
                imports.add(Compatible.class.getName());
            }

            if (pbField instanceof PbMapField) {
                imports.add(Map.class.getName());
                var pbMapField = (PbMapField) pbField;
                buildImports(buildOption, protos, proto, pbMapField.getKey().value(), imports);
                buildImports(buildOption, protos, proto, pbMapField.getValue(), imports);
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

    private static String buildDocumentComment(PbMessage msg) {
        if (CollectionUtils.isEmpty(msg.getComments())) {
            return StringUtils.EMPTY;
        }
        var builder = new StringBuilder();
        builder.append("/**").append(LS);
        msg.getComments().forEach(it -> builder.append(StringUtils.format(" * {}", it)).append(LS));
        builder.append(" */").append(LS);
        return builder.toString();
    }

    private static String buildFieldComment(PbField pbField) {
        var comments = pbField.getComments();
        if (CollectionUtils.isEmpty(comments)) {
            return StringUtils.EMPTY;
        }
        var builder = new StringBuilder();
        if (comments.size() == 1) {
            builder.append(TAB).append(StringUtils.format("@Note(\"{}\")", comments.get(0))).append(LS);
        } else {
            builder.append(TAB).append("@Note(").append(LS);
            for (int i = 0; i < comments.size(); i++) {
                var comment = comments.get(i);
                if (i == comments.size() - 1) {
                    builder.append(TAB + TAB);
                    builder.append(StringUtils.format("\"{}\")", comment));
                } else {
                    builder.append(TAB + TAB);
                    builder.append(StringUtils.format("\"{}\\n\" + ", comment, LS));
                }
                builder.append(LS);
            }
        }
        return builder.toString();
    }

    public static String buildMessage(PbGenerateOperation buildOption, List<Proto> protos, Proto proto, PbMessage pbMessage) {
        var builder = new StringBuilder();

        // import other class
        var imports = buildMessageImports(buildOption, protos, proto, pbMessage);
        imports.stream()
                .sorted(Comparator.naturalOrder())
                .forEach(it -> builder.append(StringUtils.format("import {};", it)).append(LS));

        // document
        var documentComment = buildDocumentComment(pbMessage);
        builder.append(documentComment);

        // message
        builder.append(StringUtils.format("@Protocol(id = {})", pbMessage.getProtocolId())).append(LS);
        builder.append(StringUtils.format("public class {} {", pbMessage.getName())).append(LS);

        var pbFields = pbMessage.getFields()
                .stream()
                .sorted((a, b) -> a.getTag() - b.getTag())
                .toList();

        var builderMethod = new StringBuilder();
        for (var pbField : pbFields) {
            var type = getJavaType(pbField);
            var name = pbField.getName();

            var fieldComment = buildFieldComment(pbField);
            builder.append(fieldComment);
            if (pbField.getTag() >= COMPATIBLE_FIELD_TAG) {
                var tag = pbField.getTag() - COMPATIBLE_FIELD_TAG;
                builder.append(TAB).append(StringUtils.format("@Compatible({})", tag)).append(LS);
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
