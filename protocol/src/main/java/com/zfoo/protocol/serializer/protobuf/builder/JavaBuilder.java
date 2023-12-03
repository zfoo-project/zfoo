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
import com.zfoo.protocol.serializer.protobuf.wire.*;
import com.zfoo.protocol.serializer.protobuf.wire.Field.Type;
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

    public String getJavaType(Field field, List<String> imps) {
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

    private void buildMsgImps(ProtoMessage msg, List<Field> tmp, List<String> imps) {
        if (msg.getFields() != null) {
            msg.getFields().forEach(e -> {
                getJavaType(e, imps);
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

    public String buildMessage(Proto proto, ProtoMessage msg, int indent, Map<String, String> defineMsgs, Map<String, Proto> protos) {
        int level = Math.max(indent, 1);
        final CodeBuilder cb = new CodeBuilder();
        List<Field> tmp = new ArrayList<>();
        List<String> imps = new ArrayList<>();
        buildMsgImps(msg, tmp, imps);

        List<Field> fields = new ArrayList<>();
        tmp.stream().sorted(Comparator.comparingInt(Field::getTag))
                .forEach(fields::add);

        // not nested
        imps.stream().sorted(Comparator.naturalOrder())
                .forEach(e -> cb.t(level - 1).e("import $cls$;").arg(e).ln());

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
            String type = getJavaType(f, imps);
            String name = f.getName();
            if (f.getCardinality() == Field.Cardinality.REPEATED) {
                String boxedTypeName = getBoxedTypeName(f);
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

    private String getBoxedTypeName(Field f) {
        String type = getJavaType(f, null);
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
        msgs.stream().sorted(Comparator.comparing(ProtoMessage::getName))
                .forEach(e -> cb.c(buildMessage(proto, e, level + 1, defineMsgs, protos)));
    }



}
