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

package com.zfoo.protocol.serializer.protobuf.wire.parser;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.wire.*;
import com.zfoo.protocol.serializer.protobuf.wire.Comment.CommentType;
import com.zfoo.protocol.serializer.protobuf.wire.Field.Cardinality;
import com.zfoo.protocol.serializer.protobuf.wire.Field.Type;
import com.zfoo.protocol.serializer.protobuf.wire.ProtoEnum.EnumEntry;

import java.util.*;

import static com.zfoo.protocol.serializer.protobuf.wire.WireFormat.isValidTag;

/**
 * proto文件的解析器
 *
 * @date : 2019/12/2
 */
public class ProtoParser {
    /**
     * 需要解析的字符数组
     */
    protected char[] data;
    /**
     * 解析的游标
     */
    protected int pos;
    /**
     * 正在解析的行数
     */
    protected int row;
    /**
     * 每行开始对应的游标
     */
    protected Map<Integer, Integer> rowsPos;
    /**
     * 解析时临时存放解析的注释信息
     */
    protected List<Comment> comments;
    protected static List<String> fieldCardinalities = new ArrayList<>();

    private static final String VALUE_END_ERROR = " value not end with \";\"";
    private static final String ONEOF_MSG = "oneof ";
    private static final String ROW_MSG = "row [";
    private static final String START_MSG = " start...";


    public ProtoParser(String data) {
        this.row = 1;
        this.pos = 0;
        this.rowsPos = new HashMap<>();
        rowsPos.put(1, 0);

        this.comments = new ArrayList<>();

        fieldCardinalities.add("repeated");
        fieldCardinalities.add("optional");
        fieldCardinalities.add("required");

        this.data = data.toCharArray();
    }

    private void setSyntax(Proto proto, ProtoValue syntaxValue) {
        String syntax = null;
        if (syntaxValue != null) {
            syntax = syntaxValue.getValue();
        }
        if (syntax != null && !syntax.isEmpty()) {
            proto.setSyntax(Syntax.fromValue(syntax));
        }
    }

    private void setProtoPackage(Proto proto, ProtoValue packValue) {
        if (packValue.getValue() != null && !packValue.getValue().isEmpty()) {
            proto.setProtoPackage(packValue.getValue());
        }
    }

    private void addImport(Proto proto, ProtoValue impValue) {
        if (impValue != null && impValue.getValue() != null) {
            proto.addImport(impValue.getValue());
        }
    }

    private void addCommentLine(Comment.CommentType type, String line) {
        addCommentLines(type, Arrays.asList(line));
    }

    private void addCommentLines(Comment.CommentType type, List<String> lines) {
        Comment c;
        if (comments.isEmpty()) {
            comments.add(buildComment(type, lines));
            return;
        }
        c = comments.get(comments.size() - 1);
        if (c.getType() == type) {
            c.getLines().addAll(lines);
        } else {
            comments.add(buildComment(type, lines));
        }
    }

    private Comment buildComment(Comment.CommentType type, List<String> lines) {
        Comment c = new Comment();
        c.setType(type);
        c.setLines(lines);
        return c;
    }

    public Proto parse() {
        var proto = new Proto();
        trimLines();
        String token = readToken();
        while (token.length() > 0) {
            List<String> lines;
            switch (token) {
                case "//":
                    String line = readSingleLineComment();
                    addCommentLines(CommentType.INLINE, Arrays.asList(line));
                    nextLine();
                    break;
                case "/*":
                    lines = readMultiLineComment();
                    addCommentLines(CommentType.MULTILINE, lines);
                    nextLine();
                    break;
                case "/**":
                    lines = readMultiLineComment();
                    addCommentLines(CommentType.DOCUMENT, lines);
                    nextLine();
                    break;
                case "syntax":
                    addCommentsToProto(proto);
                    readValueSeparator('=');
                    ProtoValue syntaxValue = readValue();
                    setSyntax(proto, syntaxValue);
                    break;
                case "package":
                    addCommentsToProto(proto);
                    readValueSeparator(' ');
                    ProtoValue packValue = readValue();
                    setProtoPackage(proto, packValue);
                    comments.clear();
                    break;
                case "option":
                    addCommentsToProto(proto);
                    trim();
                    String optionLabel = readToken();
                    readValueSeparator('=');
                    ProtoValue pv = readValue();
                    String optionValue = pv.getValue();
                    Option option = new Option();
                    option.setName(optionLabel).setValue(optionValue);
                    proto.addOption(option);
                    comments.clear();
                    break;
                case "import":
                    addCommentsToProto(proto);
                    readValueSeparator(' ');
                    ProtoValue impValue = readValue();
                    addImport(proto, impValue);
                    comments.clear();
                    break;
                case "message":
                    addCommentsToProto(proto);
                    ProtoMessage msg = parseMessage();
                    proto.addMsg(msg);
                    comments.clear();
                    break;
                case "enum":
                    addCommentsToProto(proto);
                    ProtoEnum protoEnum = parseEnum();
                    proto.addEnum(protoEnum);
                    comments.clear();
                    break;
                case "extend":
                    addCommentsToProto(proto);
                    proto.addExtend(parseExtend());
                    comments.clear();
                    break;
                default:
                    break;
            }
            boolean next = nextLine();
            while (next) {
                trim();
                next = nextLine();
            }
            token = readToken();
        }

        return proto;
    }

    private void addCommentsToProto(Proto proto) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        comments.forEach(c -> proto.addComments(c.getLines()));
        comments.clear();
    }

    private boolean readValueSeparator(char separatorChar) {
        if (separatorChar == ' ') {
            return trim() > 0;
        } else {
            trim();
            if (data[pos] == separatorChar) {
                pos++;
                trim();
                return true;
            }
        }
        throw new RuntimeException(ROW_MSG + row + "] colum ["
                + (pos - rowsPos.get(row)) + "] not \"" + separatorChar + "\"");
    }

    public int enumValue(String number) {
        int v = parseInt(number);
        if (v < 0) {
            throw new RuntimeException("Enum value can't is [" + v + "]");
        }
        return v;
    }

    public Extend parseExtend() throws RuntimeException {
        trim();
        String name = readToken();
        if (name.length() == 0) {
            throw new RuntimeException(ROW_MSG + row + "] extend name not set");
        }
        Extend ext = new Extend();
        if (!comments.isEmpty()) {
            ext.setComment(comments.get(comments.size() - 1));
            comments.clear();
        }
        ext.setName(name);
        System.out.println("extend " + name + START_MSG);
        blockStarted("extend");
        String token = readToken();
        while (token.length() > 0) {
            if ("//".equals(token)) {
                String comment = readSingleLineComment();
                addCommentLines(CommentType.INLINE, Arrays.asList(comment));
                nextLine();
            } else if (fieldCardinalities.contains(token)) {
                Field field = parseField(
                        Cardinality.valueOf(token.toUpperCase(Locale.ENGLISH)),
                        null);
                ext.addField(field);
            } else {
                Field field = parseField(Cardinality.OPTIONAL, token);
                ext.addField(field);
            }
            boolean isEnd = blockEnd();
            if (isEnd) {
                fieldEnd();
                return ext;
            }
            trim();
            trimLines();
            token = readToken();
        }

        throw new RuntimeException(ROW_MSG + row + "] extend not end");
    }

    public static int parseInt(String number) throws RuntimeException {
        StringBuilder tagv = new StringBuilder();
        for (int i = 0; i < number.length(); i++) {
            char c = number.charAt(i);
            switch (c) {
                case '_':
                case ',':
                    break;
                default:
                    tagv.append(c);
            }
        }
        int tag;
        String stag = tagv.toString();
        int radix = 10;
        if (stag.startsWith("0x") || stag.startsWith("0X")) {
            radix = 16;
            stag = stag.substring(2);
        } else if (stag.startsWith("0")) {
            radix = 8;
        }
        try {
            tag = Integer.parseInt(stag, radix);
        } catch (NumberFormatException e) {
            throw new RuntimeException("tag [" + number + "] not NumberFormat", e);
        }
        return tag;
    }

    /**
     * 读取proto Field的数字tag值
     *
     * @param stag
     * @return
     * @throws RuntimeException
     */
    public static int validTag(String stag) throws RuntimeException {
        int tag = parseInt(stag);
        if (!isValidTag(tag)) {
            throw new RuntimeException("tag [" + tag + "] not enabled");
        } else {
            return tag;
        }
    }

    /**
     * 读取一个标识符
     *
     * @return
     * @throws RuntimeException
     */
    private String readToken() throws RuntimeException {
        StringBuilder token = new StringBuilder();
        char c;
        trim();
        while (pos < data.length) {
            c = data[pos];
            if (c == '/' && token.length() == 0) {
                token.append(parseCommentToken());
                return token.toString();
            } else if (isTokenEnd(c)) {
                return token.toString();
            } else if (isTokenChar(c)) {
                token.append(c);
            }
            pos++;
        }
        return token.toString();
    }

    private String readOptionNameToken() throws RuntimeException {
        StringBuilder token = new StringBuilder();
        char c;
        trim();
        while (pos < data.length) {
            c = data[pos];
            if (c == '/' && token.length() == 0) {
                throw new RuntimeException("Option name can not start with '/'");
            } else if (isOptionNameTokenEnd(c)) {
                return token.toString();
            } else if (isOptionNameTokenChar(c)) {
                token.append(c);
            }
            pos++;
        }
        throw new RuntimeException("Option name not end");
    }

    private String parseCommentToken() throws RuntimeException {
        if (pos + 1 < data.length && data[pos + 1] == '/') {
            pos += 2;
            return "//";
        } else if (pos + 1 < data.length && data[pos + 1] == '*') {
            if (pos + 2 < data.length && data[pos + 2] == '*') {
                pos += 3;
                return "/**";
            } else {
                pos += 2;
                return "/*";
            }
        } else {
            throw new RuntimeException("row " + row
                    + " token not enable start with \"/\"");
        }
    }

    private boolean isOptionNameTokenEnd(char c) {
        switch (c) {
            case ' ':
            case '=':
                return true;
            default:
                return false;
        }
    }

    private boolean isTokenEnd(char c) {
        switch (c) {
            case ' ':
            case '\t':
            case '\n':
            case '=':
            case '<':
            case '>':
            case ',':
            case '{':
            case '(':
            case ')':
                return true;
            default:
                return false;
        }
    }

    private boolean isTokenChar(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || (c == '_')
                || (c == '-')
                || (c == '.');
    }

    private boolean isOptionNameTokenChar(char c) {
        return isTokenChar(c) || (c == '(')
                || (c == ')');
    }

    private ProtoEnum parseEnum() throws RuntimeException {
        trim();
        String name = readToken();
        if (name.length() == 0) {
            throw new RuntimeException(ROW_MSG + row + "] Enum name not set");
        }
        ProtoEnum protoEnum = new ProtoEnum();
        protoEnum.setName(name);
        System.out.println("Enum " + name + START_MSG);
        blockStarted("enum");
        nextLine();
        trim();
        List<EnumEntry> entries = new ArrayList<>();
        String token = readToken();
        while (token.length() > 0) {
            if ("//".equals(token)) {
                addCommentLine(CommentType.INLINE, readSingleLineComment());
                nextLine();
            } else if ("option".equals(token)) {
                trim();
                String optionLabel = readToken();
                readValueSeparator('=');
                ProtoValue pv = readValue();
                String optionValue = pv.getValue();
                Option option = new Option();
                option.setName(optionLabel).setValue(optionValue);
                protoEnum.setOptions(Arrays.asList(option));
            } else {
                EnumEntry entry = new EnumEntry();
                entry.setLabel(token);
                trim();
                parseEnumItemValue(entry);
                entries.add(entry);
            }
            boolean isEnd = blockEnd();
            if (isEnd) {
                fieldEnd();
                System.out.println("Enum " + name + " end");
                protoEnum.setEntries(entries);
                return protoEnum;
            }
            trim();
            token = readToken();
        }
        throw new RuntimeException("Enum has't end with '}'");
    }

    private void parseEnumItemValue(EnumEntry entry) throws RuntimeException {
        try {
            readValueSeparator('=');
        } catch (Exception e) {
            throw new RuntimeException("EnumEntry has't char '='", e);
        }
        String token;
        ProtoValue pv = readValue();
        int value = enumValue(pv.getValue());
        entry.setValue(value);
        if (pv.getOptions() != null) {
            entry.setOptions(pv.getOptions());
        }
        trim();
        String comment = "";
        token = readToken();
        if ("//".equals(token)) {
            comment = readSingleLineComment();
        }
        nextLine();
        if (comment != null && comment.length() > 0) {
            List<String> cs = new ArrayList<>();
            cs.add(comment);
            addCommentLines(CommentType.INLINE, cs);
        }
        Comment cmt = null;
        if (!comments.isEmpty()) {
            cmt = comments.get(comments.size() - 1);
        }
        entry.setComment(cmt);
        comments.clear();
    }

    private ProtoMessage parseMessage() throws RuntimeException {
        ProtoMessage msg = new ProtoMessage();
        trim();
        String name = readToken();
        if (name.length() == 0) {
            throw new RuntimeException(ROW_MSG + row + "] message name not set");
        }
        msg.setName(name);
        if (!comments.isEmpty()) {
            msg.setComment(comments.get(comments.size() - 1));
            comments.clear();
        }
        System.out.println("message " + name + START_MSG);
        blockStarted("message");
        String token = readToken();
        while (token.length() > 0) {
            if ("//".equals(token)) {
                addCommentLine(CommentType.INLINE, readSingleLineComment());
                nextLine();
            } else if (fieldCardinalities.contains(token)) {
                Field field = parseField(
                        Cardinality.valueOf(token.toUpperCase(Locale.ENGLISH)),
                        null);
                msg.addField(field);
            } else if ("enum".equals(token)) {
                ProtoEnum protoEnum = parseEnum();
                msg.addEnum(protoEnum);
            } else if ("message".equals(token)) {
                msg.addMessage(parseMessage());
            } else if ("extensions".equals(token)) {
                String sextensions = parseExtensions();
                Extensions extensions = Extensions.parseExtensions(sextensions);
                msg.addExtensions(extensions);
            } else if ("reserved".equals(token)) {
                String reserved = parseReserved();
                msg.addReserved(Reserved.parseReserved(reserved, comments));
            } else if ("oneof".equals(token)) {
                msg.addOneof(parseOneof());
            } else {
                Field field = parseField(Cardinality.OPTIONAL, token);
                msg.addField(field);
            }
            boolean isEnd = blockEnd();
            if (isEnd) {
                System.out.println("message " + name + " end");
                fieldEnd();
                return msg;
            }
            trimLines();
            token = readToken();
        }
        return msg;
    }

    private void trimLines() {
        trim();
        boolean next = nextLine();
        while (next) {
            trim();
            next = nextLine();
        }
    }

    private Oneof parseOneof() throws RuntimeException {
        trim();
        String name = readToken();
        if (name.length() == 0) {
            throw new RuntimeException(ROW_MSG + row + "] oneof name not set");
        }

        Oneof oneof = new Oneof();
        if (!comments.isEmpty()) {
            oneof.setComment(comments.get(comments.size() - 1));
            comments.clear();
        }
        oneof.setName(name);
        System.out.println(ONEOF_MSG + name + START_MSG);
        blockStarted("oneof");
        String token = readToken();
        while (token.length() > 0) {
            if ("//".equals(token)) {
                String comment = readSingleLineComment();
                addCommentLines(CommentType.INLINE, Arrays.asList(comment));
                nextLine();
            } else if (fieldCardinalities.contains(token)) {
                Field field = parseField(
                        Cardinality.valueOf(token.toUpperCase(Locale.ENGLISH)),
                        null);
                oneof.addField(field);
            } else {
                Field field = parseField(Cardinality.OPTIONAL, token);
                oneof.addField(field);
            }
            boolean isEnd = blockEnd();
            if (isEnd) {
                System.out.println(ONEOF_MSG + name + " end");
                fieldEnd();
                return oneof;
            }
            trim();
            token = readToken();
        }
        throw new RuntimeException("oneof not end");
    }

    private String parseReserved() throws RuntimeException {
        return readExpression();
    }

    private String readExpression() throws RuntimeException {
        StringBuilder exp = new StringBuilder();
        trim();
        char c;
        while (pos < data.length) {
            c = data[pos];
            switch (c) {
                case '\n':
                    throw new RuntimeException(ROW_MSG + row +
                            "] expression not end with char ';'");
                case ';':
                    pos++;
                    trim();
                    return exp.toString();
                default:
                    exp.append(c);
            }
            pos++;
        }
        throw new RuntimeException(ROW_MSG + row +
                "] expression not end with char ';'");
    }

    private String parseExtensions() throws RuntimeException {
        return readExpression();
    }

    private Field parseField(Cardinality cardinality, String fieldType) throws RuntimeException {
        trim();
        if (fieldType == null) {
            fieldType = readToken();
            trim();
        }
        MapGenericType gType = null;
        if ("map".equals(fieldType)) {
            gType = parseMapGenericType();
        }
        String fieldName = readToken();
        trim();
        readValueSeparator('=');
        trim();
        ProtoValue pv = readValue();
        int tag = validTag(pv.getValue());
        trim();
        Comment comment = new Comment();
        if (!comments.isEmpty()) {
            var lastComment = comments.get(comments.size() - 1);
            comment.getLines().addAll(lastComment.getLines());
        }

        String token = readToken();
        if ("//".equals(token)) {
            comment.getLines().add(readSingleLineComment());
        }
        nextLine();
        Field field;
        if (gType != null) {
            Type type = Type.valueOf(gType.getKeyType().toUpperCase(Locale.ENGLISH));
            field = new MapField().setKey(type).setValue(gType.getValueType());
        } else {
            field = new Field();
        }
        field.setCardinality(cardinality)
                .setName(fieldName)
                .setTag(tag)
                .setType(fieldType);
        field.setComment(comment);
        if (pv.getOptions() != null && !pv.getOptions().isEmpty()) {
            field.setOptions(pv.getOptions());
        }
        comments.clear();
        return field;
    }

    /**
     * 解析判断语句块是否开始,去掉空格和空行后第一个字符为"{"的返回语句块开始
     *
     * @throws RuntimeException
     */
    private void blockStarted(String structName) throws RuntimeException {
        trim();
        boolean next = nextLine();
        while (next) {
            trim();
            next = nextLine();
        }
        if (data[pos] == '{') {
            pos++;
            trim();
            next = nextLine();
            while (next) {
                trim();
                next = nextLine();
            }
        } else {
            throw new RuntimeException(ROW_MSG + row + "] " + structName + " not start char '{'");
        }
    }

    /**
     * 解析判断语句块是否结束,语句块以"}"标示符为结束符
     *
     * @return
     */
    private boolean blockEnd() {
        trim();
        boolean next = nextLine();
        while (next) {
            trim();
            next = nextLine();
        }
        if (pos == data.length) {
            return false;
        }
        if (data[pos] == '}') {
            pos++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解析判断Field是否结束,去掉空行和空格,以";"结束表示field解析解决
     *
     * @return
     */
    private boolean fieldEnd() {
        trim();
        if (pos >= data.length) {
            return false;
        }
        if (data[pos] == ';') {
            pos++;
            trim();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 解析判断去掉空格,tab建后第一个字符是否与提供的字符相同
     *
     * @param endChar 指定的字符
     * @return
     */
    private boolean isEndChar(char endChar) {
        char c;
        while (pos < data.length) {
            c = data[pos];
            if (c != ' ' && c != '\t') {
                if (c == endChar) {
                    pos++;
                    return true;
                }
                return false;
            }
            pos++;
        }
        return false;
    }

    /**
     * 读取单行的注释内容，如果读取到换行符或者文件结束则返回注释的内容
     */
    private String readSingleLineComment() {
        var builder = new StringBuilder();
        while (pos < data.length) {
            char c = data[pos];
            if (c == '\n') {
                break;
            }
            builder.append(c);
            pos++;
        }
        return builder.toString().trim();
    }

    public List<String> readMultiLineComment() {
        List<String> lines = new ArrayList<>();
        StringBuilder comment = new StringBuilder();
        char c;
        int start = pos;
        boolean commentStart = true;
        boolean isFirst = true;
        while (pos < data.length) {
            c = data[pos];
            if (!commentStart) {
                if (c != '*') {
                    pos++;
                    continue;
                }
                commentStart = true;
                //如果"*"后为空格则跳过空格
                if (isMultiLineCommentFinish()) {
                    return lines;
                }
                start = pos + 1;
            }
            if (c == '\n') {

                comment.append(data, start, pos - start);
                String cmt = trimLnr(comment.toString());
                if (isFirst) {
                    if (cmt.length() > 0) {
                        lines.add(cmt);
                    }
                } else {
                    lines.add(cmt);
                }
                isFirst = false;
                comment.delete(0, comment.length());
                commentStart = false;
                nextLine();
                start = pos;
                continue;
            }
            pos++;
        }
        return lines;
    }

    private String trimLnr(String s) {
        if (!s.endsWith("\r")) {
            return s;
        }
        return s.substring(0, s.length() - 2);
    }

    private boolean isMultiLineCommentFinish() {
        //如果"*"后为空格则跳过空格
        if (pos + 1 < data.length) {
            char c2 = data[pos + 1];
            if (c2 == ' ') {
                pos += 2;
            } else if (c2 == '/') { //多行注释结束
                pos += 2;
                trim();
                nextLine();
                return true;
            }
        }
        return false;
    }

    class ProtoValue {
        private String value;
        private List<Option> options;

        /**
         * @return the value
         */
        public String getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public ProtoValue setValue(String value) {
            this.value = value;
            return this;
        }

        /**
         * @return the options
         */
        public List<Option> getOptions() {
            return options;
        }

        /**
         * @param options the options to set
         */
        public ProtoValue setOptions(List<Option> options) {
            this.options = options;
            return this;
        }

    }

    /**
     * 读取proto描述文件中的值
     *
     * @return
     * @throws RuntimeException
     */
    private ProtoValue readValue() throws RuntimeException {
        char c;
        c = data[pos];
        if (c == '\'' || c == '"') {
            pos++;
            return readQuoteValue(c);
        }
        StringBuilder v = new StringBuilder();
        ProtoValue pv = new ProtoValue();
        while (pos < data.length) {
            c = data[pos];
            switch (c) {
                case ';':
                    pos++;
                    pv.setValue(v.toString());
                    return pv;
                case ' ':
                    trim();
                    if (data[pos] == '[') {
                        List<Option> options = readFieldOptions();
                        pv.setValue(v.toString()).setOptions(options);
                        return pv;
                    }
                    if (data[pos] != ';') {
                        throw new RuntimeException("row " + row + VALUE_END_ERROR);
                    } else {
                        pv.setValue(v.toString());
                        return pv;
                    }
                case '[':
                    List<Option> options = readFieldOptions();
                    pv.setValue(v.toString()).setOptions(options);
                    return pv;
                case '\n':
                    throw new RuntimeException("row " + row + VALUE_END_ERROR);
                default:
                    v.append(c);
            }
            pos++;
        }
        throw new RuntimeException("row " + row + " value not end");
    }

    /**
     * 读取proto描述文件中的值
     *
     * @return
     * @throws RuntimeException
     */
    private String readOptionValue() throws RuntimeException {
        char c;
        c = data[pos];
        if (c == '\'' || c == '"') {
            pos++;
            ProtoValue pv = readQuoteValue(c);
            return pv.getValue();
        }
        StringBuilder v = new StringBuilder();
        while (pos < data.length) {
            c = data[pos];
            switch (c) {
                case ']':
                    return v.toString();
                case ',':
                    pos++;
                    trim();
                    if (pos < data.length && data[pos] == ']') {
                        pos++;
                    }
                    return v.toString();
                case ' ':
                    trim();
                    return v.toString();
                case '\n':
                    throw new RuntimeException("row " + row
                            + VALUE_END_ERROR);
                default:
                    v.append(c);
            }
            pos++;
        }
        throw new RuntimeException("row " + row + " value not end");
    }

    private MapGenericType parseMapGenericType() throws RuntimeException {
        if (data[pos] != '<') {
            throw new RuntimeException("row " + row + " GenericType not started with '<'");
        }
        MapGenericType gtype = new MapGenericType();
        pos++;
        trim();
        String keyType = readToken();
        trim();
        readValueSeparator(',');
        String valueType = readToken();
        if (isEndChar('>')) {
            gtype.setKeyType(keyType).setValueType(valueType);
            trim();
        } else {
            throw new RuntimeException("row " + row + " GenericType not end with '>'");
        }
        return gtype;
    }

    public class MapGenericType {
        private String keyType;
        private String valueType;

        /**
         * @return the keyType
         */
        public String getKeyType() {
            return keyType;
        }

        /**
         * @param keyType the keyType to set
         * @return
         */
        public MapGenericType setKeyType(String keyType) {
            this.keyType = keyType;
            return this;
        }

        /**
         * @return the valueType
         */
        public String getValueType() {
            return valueType;
        }

        /**
         * @param valueType the valueType to set
         * @return
         */
        public MapGenericType setValueType(String valueType) {
            this.valueType = valueType;
            return this;
        }

    }

    /**
     * @param quote
     * @return
     * @throws RuntimeException
     */
    private ProtoValue readQuoteValue(final char quote) throws RuntimeException {
        char c;
        StringBuilder v = new StringBuilder();
        ProtoValue pv = new ProtoValue();
        while (pos < data.length) {
            c = data[pos];
            if (c == '\n') {
                throw new RuntimeException("row " + row
                        + " value not end with \"" + quote + "\"");
            } else if (c == quote) {
                if (v.length() > 0 && v.charAt(v.length() - 1) == '\\') {
                    v.deleteCharAt(v.length() - 1);
                    v.append(c);
                } else { //如果由引号引起来的字符串第二次出现引号时值结束
                    pos++;
                    trim();
                    if (data[pos] != ';' && data[pos] != ']' && data[pos] != ',') {
                        throw new RuntimeException("row " + row
                                + VALUE_END_ERROR);
                    } else {
                        if (data[pos] != ']') {
                            pos++;
                        }
                        pv.setValue(v.toString());
                        return pv;
                    }
                }
            } else {
                v.append(c);
            }
            pos++;
        }
        throw new RuntimeException("row " + row
                + " value not end with \"\\" + quote + "\"");
    }

    private List<Option> readFieldOptions() throws RuntimeException {
        List<Option> options = new ArrayList<>();
        pos++;
        while (data[pos] != ']') {
            if (data[pos] == ';') {
                pos++;
                return options;
            }
            trim();
            String name = readOptionNameToken();

            readValueSeparator('=');
            String value = readOptionValue();
            Option option = new Option();
            option.setName(name).setValue(value);
            options.add(option);
            trim();
        }
        pos++;
        trim();
        if (data[pos] != ';') {
            throw new RuntimeException("row " + row
                    + VALUE_END_ERROR);
        } else {
            pos++;
            trim();
        }
        return options;
    }

    /**
     * 去掉space,tab,'\r'回车
     *
     * @return 去掉空格和tab的个数
     */
    private int trim() {
        char c;
        int oldPos = pos;
        while (pos < data.length) {
            c = data[pos];
            switch (c) {
                case ' ':
                case '\t':
                case '\r':
                    pos++;
                    break;
                default:
                    return pos - oldPos;
            }
        }
        return pos - oldPos;
    }

    /**
     * 判断是否当前字符为换行符，如果是换行符则进行换行
     *
     * @return
     */
    private boolean nextLine() {
        if (pos < data.length && data[pos] == '\r') {
            pos++;
        }
        if (pos < data.length && data[pos] == '\n') {
            pos++;
            row++;
            rowsPos.put(row, pos);
            return true;
        }
        return false;
    }
}