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

public class GeneratePbUtils {

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
            protos.add(proto);
        }
        return protos;
    }


    public static void generate(PbGenerateOperation buildOption, List<Proto> protos) {
        Map<String, Proto> allProtos = new HashMap<>();
        for (Proto proto : protos) {
            allProtos.put(proto.getName(), proto);
        }

        for (var proto : protos) {
            List<PbOption> options = proto.getOptions();
            Map<String, String> protoOptions = new HashMap<>();
            if (options != null) {
                options.forEach(o -> protoOptions.put(o.getName(), o.getValue()));
            }
            if (CollectionUtils.isEmpty(proto.getPbMessages())) {
                continue;
            }

            generateMessage(buildOption, proto, allProtos);
        }
    }


    private static void generateMessage(PbGenerateOperation buildOption, Proto proto, Map<String, Proto> protos) {
        String msgPath = buildOption.getOutputPath() + File.separator;

        Map<String, String> msgComments = new HashMap<>();

        List<PbMessage> msgs = proto.getPbMessages();
        for (var msg : msgs) {
            StringBuilder mc = new StringBuilder();
            if (CollectionUtils.isNotEmpty(msg.getComments())) {
                msg.getComments().forEach(c -> mc.append(c));
            }
            msgComments.put(msg.getName(), mc.toString());
            var code = buildMessage(proto, msg, 1, null, protos);
            var filePath = msgPath + File.separator + msg.getName() + ".java";
            FileUtils.writeStringToFile(new File(filePath), code, false);
        }
    }

    // -------------------------------------------------------------------------------------------------------------
    public static String getJavaType(PbField field) {
        String type = field.getType();
        if (field instanceof PbMapField) {
            var mapField = (PbMapField) field;
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

    private static String getBoxJavaType(PbField field) {
        return getBoxJavaType(field.getType());
    }

    private static String getBoxJavaType(String type) {
        var typeProtobuf = PbType.typeOfProtobuf(type);
        if (typeProtobuf == null) {
            return type;
        }
        var javaType = typeProtobuf.javaType();
        return javaType.getBoxedType();
    }

    private static void addImport(List<String> imps, String imp) {
        if (imps == null) {
            return;
        }
        if (!imps.contains(imp)) {
            imps.add(imp);
        }
    }


    private static void buildMsgImps(PbMessage msg, List<PbField> tmp, List<String> imps) {
        var fields = msg.getFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (var field : fields) {
                getJavaType(field);
                tmp.add(field);
            }
        }

        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i) instanceof PbMapField) {
                addImport(imps, Map.class.getName());
            } else if (tmp.get(i).getCardinality() == PbField.Cardinality.REPEATED) {
                addImport(imps, List.class.getName());
            }
        }
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

    private static String getJavaPackage(Proto proto) {
        if (CollectionUtils.isEmpty(proto.getOptions())) {
            return StringUtils.EMPTY;
        }
        for (PbOption option : proto.getOptions()) {
            if ("java_package".equalsIgnoreCase(option.getName())) {
                return option.getValue();
            }
        }
        return StringUtils.EMPTY;
    }

    public static String buildMessage(Proto proto, PbMessage msg, int indent, Map<String, String> defineMsgs, Map<String, Proto> protos) {
        var level = Math.max(indent, 1);
        var tmp = new ArrayList<PbField>();
        var imps = new ArrayList<String>();
        var builder = new StringBuilder();

        buildMsgImps(msg, tmp, imps);

        List<PbField> fields = new ArrayList<>();
        tmp.stream().sorted(Comparator.comparingInt(PbField::getTag))
                .forEach(fields::add);

        imps.stream().sorted(Comparator.naturalOrder())
                .forEach(it -> builder.append(StringUtils.format("import {};", it)).append(LS));

        buildDocComment(builder, msg);
        builder.append(StringUtils.format("public class {} {", msg.getName())).append(LS);

        int size = fields.size();

        var builderMethod = new StringBuilder();
        for (int i = 0; i < size; i++) {
            PbField f = fields.get(i);

            buildFieldComment(builder, f);
            String type = getJavaType(f);
            String name = f.getName();
            if (f.getCardinality() == PbField.Cardinality.REPEATED) {
                String boxedTypeName = getBoxJavaType(f);
                type = "List<" + boxedTypeName + ">";
            }

            builder.append(TAB).append(StringUtils.format("private {} {};", type, name)).append(LS);

            String getMethod;
            if (!"bool".equalsIgnoreCase(f.getType())) {
                getMethod = StringUtils.format("get{}", StringUtils.capitalize(f.getName()));
            } else {
                getMethod = StringUtils.format("is{}", StringUtils.capitalize(f.getName()));
            }

            builderMethod.append(TAB).append(StringUtils.format("public {} {}() {", type, getMethod)).append(LS);
            builderMethod.append(TAB + TAB).append(StringUtils.format("return {};", f.getName())).append(LS);
            builderMethod.append(TAB).append("}").append(LS);

            String setMethod = StringUtils.format("set{}", StringUtils.capitalize(f.getName()));
            builderMethod.append(TAB).append(StringUtils.format("public void {}({} {}) {", setMethod, type, f.getName())).append(LS);
            builderMethod.append(TAB + TAB).append(StringUtils.format("this.{} = {};", f.getName(), f.getName())).append(LS);
            builderMethod.append(TAB).append("}").append(LS);
        }

        builder.append(LS).append(builderMethod);
        builder.append("}");
        return builder.toString();
    }


}
