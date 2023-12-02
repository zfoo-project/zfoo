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
import com.zfoo.protocol.serializer.protobuf.PbBuildOption;
import com.zfoo.protocol.serializer.protobuf.internal.CodeBuilder;
import com.zfoo.protocol.serializer.protobuf.wire.*;
import com.zfoo.protocol.serializer.protobuf.wire.Field.Type;
import com.zfoo.protocol.serializer.protobuf.wire.ProtoEnum.EnumEntry;
import com.zfoo.protocol.serializer.protobuf.wire.WireFormat.JavaType;
import com.zfoo.protocol.serializer.protobuf.wire.parser.Proto;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

public class JavaBuilder {
    private static final List<String> BASE_TYPES;

    static {
        BASE_TYPES = new ArrayList<>();
        String[] types = ("double,float,int32,int64,uint32,uint64,sint32,sint64,"
                + "fixed32,fixed64,sfixed32,sfixed64,bool,string,bytes").split(",");
        BASE_TYPES.addAll(Arrays.asList(types));
    }


    private String getOptionJavaType(List<Option> options) {
        String jType = "";
        if (options != null && !options.isEmpty()) {
            for (Option option : options) {
                if ("java_type".equals(option.getName())) {
                    jType = option.getValue();
                }
            }
        }
        return jType;
    }

    public String getJavaType(Field field, List<String> imps, PbBuildOption buildOps) {
        String type = field.getTypeString();
        if (field instanceof MapField) {
            MapField mf = (MapField) field;
            type = "Map<" + getJavaType(mf.getKey().value()) + ", "
                    + getJavaType(mf.getValue()) + ">";
            return type;
        }
        String jType = getOptionJavaType(field.getOptions());
        if (!BASE_TYPES.contains(type.toLowerCase(Locale.ENGLISH))) {
            return type;
        }
        JavaType javaType = null;
        javaType = Type.valueOf(type.toUpperCase(Locale.ENGLISH)).javaType();
        if (javaType != null && javaType.getTypeString() != null) {
            String optionJavaType = getOptionJavaType(field.getOptions());
            if (optionJavaType == null || optionJavaType.isEmpty()) {
                type = javaType.getTypeString();
            } else {
                int lastDot = optionJavaType.lastIndexOf(".");
                if (lastDot == -1) {
                    type = optionJavaType;
                } else {
                    if (!imps.contains(optionJavaType)) {
                        imps.add(optionJavaType);
                    }
                    type = optionJavaType.substring(lastDot + 1);
                }
            }
        }
        if ("int64".equals(field.getType()) && jType != null) {
            switch (jType) {
                case "LocalDateTime":
                    addImport(imps, "java.time.LocalDateTime");
                    type = jType;
                    break;
                case "LocalDate":
                    type = jType;
                    addImport(imps, "java.time.LocalDate");
                    break;
                default:
                    break;
            }
        }
        return type;
    }

    private void addImport(List<String> imps, String imp) {
        if (imps == null) {
            return;
        }
        if (!imps.contains(imp)) {
            imps.add(imp);
        }
    }

    public String getJavaType(String type) {
        if (BASE_TYPES.contains(type.toLowerCase(Locale.ENGLISH))) {
            return Type.valueOf(type.toUpperCase(Locale.ENGLISH)).javaType().getTypeString();
        } else {
            return type;
        }
    }

    public String getAnnotationType(Field field) {
        String type = field.getTypeString();
        if (BASE_TYPES.contains(type.toLowerCase(Locale.ENGLISH))) {
            return "Type." + Type.valueOf(type.toUpperCase(Locale.ENGLISH));
        } else {
            return "Type." + Type.MESSAGE;
        }
    }

    private void buildMsgImps(ProtoMessage msg, List<Field> tmp, List<String> imps,
                              PbBuildOption buildOps) {
        if (msg.getFields() != null) {
            msg.getFields().forEach(e -> {
                getJavaType(e, imps, buildOps);
                tmp.add(e);
            });
        }

        for (int i = 0; i < tmp.size(); i++) {
            if (tmp.get(i) instanceof MapField) {
                addImport(imps, "java.util.Map");
            } else if (tmp.get(i).getCardinality() == Field.Cardinality.REPEATED) {
                addImport(imps, "java.util.List");
                addImport(imps, "java.util.ArrayList");
            }
        }
    }

    private void buildDocComment(CodeBuilder cb, Comment comment, int level) {
        if (comment == null || comment.getLines() == null || comment.getLines().isEmpty()) {
            return;
        }
        cb.t(level).c("/**").ln();
        comment.getLines().forEach(c -> cb.t(level).c(" * ").c(c).ln());
        cb.t(level).c(" */").ln();
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

    public String buildMessage(Proto proto, ProtoMessage msg, int indent, Map<String, String> defineMsgs, PbBuildOption buildOps, Map<String, Proto> protos) {
        int level = Math.max(indent, 1);
        final CodeBuilder cb = new CodeBuilder();
        List<Field> tmp = new ArrayList<>();
        List<String> imps = new ArrayList<>();
        buildMsgImps(msg, tmp, imps, buildOps);

        List<Field> fields = new ArrayList<>();
        tmp.stream().sorted(Comparator.comparingInt(Field::getTag))
                .forEach(fields::add);

        if (!buildOps.isIsNested()) {
            imps.stream().sorted(Comparator.naturalOrder())
                    .forEach(e -> cb.t(level - 1).e("import $cls$;").arg(e).ln());
        }
        cb.ln();
        buildDocComment(cb, msg.getComment(), level - 1);
        cb.t(level - 1).e("public class $name$ {").arg(msg.getName()).ln(2);

        CodeBuilder getCode;
        CodeBuilder setCode;

        int size = fields.size();

        List<String> getCodes = new ArrayList<>(size);
        List<String> setCodes = new ArrayList<>(size);

        CodeBuilder cons = new CodeBuilder();
        for (int i = 0; i < size; i++) {
            Field f = fields.get(i);
            getCode = new CodeBuilder();
            setCode = new CodeBuilder();

            String typeName = getAnnotationType(f);
            if (msg.getName().equals("Type")) {
                typeName = Proto.class.getPackage().getName() + ".wire.Field." + typeName;
            }
            buildDocComment(cb, f.getComment(), level);
            String type = getJavaType(f, imps, buildOps);
            String name = f.getName();
            if (f.getCardinality() == Field.Cardinality.REPEATED) {
                String boxedTypeName = getBoxedTypeName(f, buildOps);
                type = "List<" + boxedTypeName + ">";
            }

            cb.t(level).e("private $type$ $name$;")
                    .arg(type, name).ln();

            String getMethod;
            if (!"bool".equalsIgnoreCase(f.getType())) {
                getMethod = "get" +
                        f.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) +
                        f.getName().substring(1);
            } else {
                getMethod = "is" +
                        f.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) +
                        f.getName().substring(1);
            }
            getCode.t(level).e("public $type$ $getMethod$() {")
                    .arg(type, getMethod).ln();
            getCode.t(level + 1).e("return $name$;").arg(f.getName()).ln();
            getCode.t(level).c("}").ln();
            getCodes.add(getCode.toString());

            String retType = "void";
            String setMethod = "set" +
                    f.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) +
                    f.getName().substring(1);
            setCode.t(level).e("public $retType$ $setMethod$($type$ $name$) {")
                    .arg(retType, setMethod, type, name).ln();
            setCode.t(level + 1).e("this.$name$ = $name$;")
                    .arg(f.getName(), f.getName()).ln();
            setCode.t(level).c("}").ln();
            setCodes.add(setCode.toString());
        }

        cb.ln().c(cons.toString());

        for (int i = 0; i < size; i++) {
            cb.ln().c(getCodes.get(i));
            cb.ln().c(setCodes.get(i));
        }

        nestMessageMessage(proto, cb, msg.getMessages(), defineMsgs, level, protos);

        cb.t(level - 1).c("}");
        return cb.toString();
    }

    private String getBoxedTypeName(Field f, PbBuildOption buildOps) {
        String type = getJavaType(f, null, buildOps);
        if (BASE_TYPES.contains(f.getTypeString())) {
            JavaType javaType = null;
            javaType = Type.valueOf(f.getTypeString().toUpperCase(Locale.ENGLISH)).javaType();
            if (javaType != null && javaType.getTypeString() != null) {
                type = javaType.getBoxedType();
            }
        }
        return type;
    }


    private void nestMessageMessage(Proto proto, CodeBuilder cb,
                                    List<ProtoMessage> msgs, Map<String, String> defineMsgs,
                                    int level, Map<String, Proto> protos) {
        if (msgs == null || msgs.isEmpty()) {
            return;
        }
        cb.ln();
        PbBuildOption nestedOps = new PbBuildOption();
        nestedOps.setIsNested(true);
        msgs.stream().sorted(Comparator.comparing(ProtoMessage::getName))
                .forEach(e -> cb.c(buildMessage(proto, e, level + 1, defineMsgs, nestedOps, protos)));
    }

    private void nestMessageEnum(CodeBuilder cb, List<ProtoEnum> enums, int indent) {
        if (enums == null || enums.isEmpty()) {
            return;
        }
        cb.ln();
        PbBuildOption nestedOps = new PbBuildOption();
        nestedOps.setIsNested(true);
        enums.stream()
                .sorted(Comparator.comparing(ProtoEnum::getName))
                .forEach(e -> cb.c(buildEnum(e, indent + 1, nestedOps)).ln(2));

    }

    private String getFillSpaces(int count) {
        if (count <= 0) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    public String buildEnum(ProtoEnum protoEnum, final int indent, PbBuildOption buildOps) {
        int level = (indent < 1) ? 1 : indent;
        CodeBuilder cb = new CodeBuilder();

        cb.t(level - 1).e("public enum $name$ {").arg(protoEnum.getName()).ln();
        List<EnumEntry> entries = protoEnum.getEntries();
        if (entries != null && !entries.isEmpty()) {
            Optional<EnumEntry> maxLen = entries.stream()
                    .max(Comparator.comparingInt(e -> e.getLabel().length()));
            int len = maxLen.isPresent() ? maxLen.get().getLabel().length() : 1;

            List<EnumEntry> es = new ArrayList<>();
            entries.stream()
                    .sorted(Comparator.comparingInt(EnumEntry::getValue))
                    .forEach(es::add);
            CodeBuilder vsb = new CodeBuilder();
            CodeBuilder psb = new CodeBuilder();
            for (int i = 0; i < es.size(); i++) {
                EnumEntry o = es.get(i);
                String sep = (i != es.size() - 1) ? "," : ";";
                String spaces = getFillSpaces(len - o.getLabel().length());
                String[] args = new String[]{o.getLabel(), spaces,
                        String.valueOf(o.getValue()), sep};
                cb.t(level).e("$label$$spaces$($value$)$;$").arg(args).ln();

                psb.t(level).e("public static final int $lable$_VALUE = $value$;")
                        .arg(o.getLabel(), String.valueOf(o.getValue())).ln();

                vsb.t(level + 2).e("case $value$:").arg(String.valueOf(o.getValue())).ln();
                vsb.t(level + 3).e("return $name$.$label$;")
                        .arg(protoEnum.getName(), o.getLabel()).ln();
            }
            cb.ln();

            cb.c(psb.toString()).ln();

            cb.t(level).c("private final int value;").ln(2);

            cb.t(level).e("private $label$(int value) {").arg(protoEnum.getName()).ln();
            cb.t(level + 1).c("this.value = value;").ln();
            cb.t(level).c("}").ln();

            cb.t(level).c("public int getValue() {").ln();
            cb.t(level + 1).c("return this.value;").ln();
            cb.t(level).c("}").ln(2);

            cb.t(level).e("public static $name$ valueOf(int value) {")
                    .arg(protoEnum.getName()).ln();
            cb.t(level + 1).c("switch (value) {").ln();
            cb.c(vsb.toString());
            cb.t(level + 2).c("default:").ln();
            cb.t(level + 3).e("throw new IllegalArgumentException("
                            + "\"no enum value $value$ \" + value);")
                    .arg(protoEnum.getName()).ln();

            cb.t(level + 1).c("}").ln();
            cb.t(level).c("}").ln();
        }
        cb.t(level - 1).c("}");

        return cb.toString();
    }
}
