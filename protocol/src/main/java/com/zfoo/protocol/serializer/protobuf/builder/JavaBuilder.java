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

package com.zfoo.protocol.serializer.protobuf.builder;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.parser.Proto;
import com.zfoo.protocol.serializer.protobuf.parser.TypeJava;
import com.zfoo.protocol.serializer.protobuf.parser.TypeProtobuf;
import com.zfoo.protocol.serializer.protobuf.wire.MapField;
import com.zfoo.protocol.serializer.protobuf.wire.Option;
import com.zfoo.protocol.serializer.protobuf.wire.PbField;
import com.zfoo.protocol.serializer.protobuf.wire.ProtoMessage;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

import static com.zfoo.protocol.util.FileUtils.LS;
import static com.zfoo.protocol.util.StringUtils.TAB;

public class JavaBuilder {

    public String getJavaType(PbField field) {
        String type = field.getType();
        if (field instanceof MapField) {
            var mapField = (MapField) field;
            type = StringUtils.format("Map<{}, {}>", getBoxJavaType(mapField.getKey().value()), getBoxJavaType(mapField.getValue()));
            return type;
        }
        return getJavaType(type);
    }

    public String getJavaType(String type) {
        var typeProtobuf = TypeProtobuf.typeOfProtobuf(type);
        if (typeProtobuf == null) {
            return type;
        }
        var javaType = typeProtobuf.javaType();
        return javaType.getTypeString();
    }

    private String getBoxJavaType(PbField field) {
        return getBoxJavaType(field.getType());
    }

    private String getBoxJavaType(String type) {
        var typeProtobuf = TypeProtobuf.typeOfProtobuf(type);
        if (typeProtobuf == null) {
            return type;
        }
        var javaType = typeProtobuf.javaType();
        return javaType.getBoxedType();
    }

    private void addImport(List<String> imps, String imp) {
        if (imps == null) {
            return;
        }
        if (!imps.contains(imp)) {
            imps.add(imp);
        }
    }


    private void buildMsgImps(ProtoMessage msg, List<PbField> tmp, List<String> imps) {
        var fields = msg.getFields();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (var field : fields) {
                getJavaType(field);
                tmp.add(field);
            }
        }

        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i) instanceof MapField) {
                addImport(imps, Map.class.getName());
            } else if (tmp.get(i).getCardinality() == PbField.Cardinality.REPEATED) {
                addImport(imps, List.class.getName());
            }
        }
    }

    private void buildDocComment(StringBuilder builder, ProtoMessage msg) {
        if (CollectionUtils.isEmpty(msg.getComments())) {
            return;
        }
        builder.append("/**").append(LS);
        msg.getComments().forEach(it -> builder.append(StringUtils.format(" * {}", it)).append(LS));
        builder.append(" */").append(LS);
    }

    private void buildFieldComment(StringBuilder builder, PbField pbField) {
        if (CollectionUtils.isEmpty(pbField.getComments())) {
            return;
        }
        pbField.getComments().forEach(it -> builder.append(TAB).append(StringUtils.format("// {}", it)).append(LS));
    }

    private String getJavaPackage(Proto proto) {
        if (CollectionUtils.isEmpty(proto.getOptions())) {
            return StringUtils.EMPTY;
        }
        for (Option option : proto.getOptions()) {
            if ("java_package".equalsIgnoreCase(option.getName())) {
                return option.getValue();
            }
        }
        return StringUtils.EMPTY;
    }

    public String buildMessage(Proto proto, ProtoMessage msg, int indent, Map<String, String> defineMsgs, Map<String, Proto> protos) {
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
