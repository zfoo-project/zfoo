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

package com.zfoo.protocol.serializer.protobuf.parser;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.serializer.protobuf.*;
import com.zfoo.protocol.serializer.protobuf.PbField.Cardinality;
import com.zfoo.protocol.util.NumberUtils;
import com.zfoo.protocol.util.StringUtils;

import java.util.*;

/**
 * proto文件的解析器
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
    protected Map<Integer, Integer> rowsPos = new HashMap<>();
    /**
     * 解析时临时存放解析的注释信息
     */
    protected List<String> comments = new ArrayList<>();


    public ProtoParser(String data) {
        this.row = 1;
        this.pos = 0;
        this.rowsPos.put(1, 0);
        this.data = data.toCharArray();
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
                    line = StringUtils.trim(line);
                    comments.add(line);
                    nextLine();
                    break;
                case "/*":
                    lines = readMultiLineComment();
                    comments.addAll(lines);
                    nextLine();
                    break;
                case "/**":
                    lines = readMultiLineComment();
                    comments.addAll(lines);
                    nextLine();
                    break;
                case "syntax":
                    addCommentsToProto(proto);
                    readValueSeparator('=');
                    readValueUtilSemicolon();
                    break;
                case "package":
                    addCommentsToProto(proto);
                    readValueSeparator(' ');
                    ProtoValue packValue = readValueUtilSemicolon();
                    proto.setProtoPackage(packValue.getValue());
                    comments.clear();
                    break;
                case "option":
                    addCommentsToProto(proto);
                    trim();
                    String optionLabel = readToken();
                    readValueSeparator('=');
                    ProtoValue pv = readValueUtilSemicolon();
                    String optionValue = pv.getValue();
                    PbOption pbOption = new PbOption();
                    pbOption.setName(optionLabel);
                    pbOption.setValue(optionValue);
                    proto.getOptions().add(pbOption);
                    comments.clear();
                    break;
                case "import":
                    addCommentsToProto(proto);
                    readValueSeparator(' ');
                    ProtoValue impValue = readValueUtilSemicolon();
                    proto.getImports().add(impValue.getValue());
                    comments.clear();
                    break;
                case "message":
                    PbMessage msg = parseMessage();
                    proto.getPbMessages().add(msg);
                    comments.clear();
                    break;
                case "enum":
                    notSupportEnum();
                    break;
                case "extend":
                    notSupportExtend();
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

    private PbMessage parseMessage() throws RuntimeException {
        PbMessage msg = new PbMessage();
        trim();
        String name = readToken();
        if (StringUtils.isBlank(name)) {
            throw new RuntimeException(StringUtils.format("row [{}] message name not set", name));
        }
        msg.setName(name);
        if (CollectionUtils.isNotEmpty(comments)) {
            msg.getComments().addAll(comments);
            comments.clear();
        }
        blockStarted("message");
        String token = readToken();
        while (token.length() > 0) {
            if ("//".equals(token)) {
                var singleLine = readSingleLineComment();
                comments.add(singleLine);
                nextLine();
            } else if ("enum".equals(token)) {
                notSupportEnum();
            } else if ("extend".equals(token)) {
                notSupportExtend();
            } else if ("oneof".equals(token)) {
                notSupportOneof();
            } else if ("reserved".equals(token)) {
                notSupportReserved();
            } else if ("message".equals(token)) {
                // 为了让用法简单，屏蔽内部类的消息定义
                // msg.addMessage(parseMessage());
                notSupportInnerMessage();
            } else if (Cardinality.cardinalityOf(token) != null) {
                PbField field = parseField(Cardinality.cardinalityOf(token), null);
                msg.getFields().add(field);
            } else {
                PbField field = parseField(Cardinality.OPTIONAL, token);
                msg.getFields().add(field);
            }
            boolean isEnd = blockEnd();
            if (isEnd) {
                fieldEnd();
                return msg;
            }
            trimLines();
            token = readToken();
        }
        return msg;
    }

    private void notSupportEnum() {
        var name = readToken();
        throw new RuntimeException(StringUtils.format("zfoo not support enum definition in message:[{}]", name));
    }

    private void notSupportExtend() {
        var name = readToken();
        throw new RuntimeException(StringUtils.format("zfoo not support extend syntax in message:[{}]", name));
    }

    private void notSupportOneof() {
        var name = readToken();
        throw new RuntimeException(StringUtils.format("zfoo not support oneof field in message:[{}]", name));
    }

    private void notSupportReserved() {
        var name = readToken();
        throw new RuntimeException(StringUtils.format("zfoo not support reserved syntax in message:[{}]", name));
    }

    private void notSupportInnerMessage() {
        var name = readToken();
        throw new RuntimeException(StringUtils.format("zfoo not support inner message:[{}]", name));
    }

    // --------------------------------------------------------------------------------------------------------------

    private void addCommentsToProto(Proto proto) {
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        proto.getComments().addAll(comments);
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
        throw new RuntimeException(StringUtils.format("row [{}] colum [{}] not [{}]", row, pos - rowsPos.get(row), separatorChar));
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
     * 读取一个标识符
     */
    private String readToken() throws RuntimeException {
        trim();
        StringBuilder token = new StringBuilder();
        char c;
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

    private void trimLines() {
        trim();
        boolean next = nextLine();
        while (next) {
            trim();
            next = nextLine();
        }
    }

    private PbField parseField(Cardinality cardinality, String fieldType) throws RuntimeException {
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
        ProtoValue pv = readValueUtilSemicolon();
        int tag = parseInt(pv.getValue());
        trim();
        String token = readToken();
        if ("//".equals(token)) {
            comments.add(readSingleLineComment());
        }
        nextLine();
        PbField field;
        if (gType != null) {
            PbType type = PbType.valueOf(gType.getKeyType().toUpperCase(Locale.ENGLISH));
            var mapField = new PbMapField();
            mapField.setKey(type);
            mapField.setValue(gType.getValueType());
            field = mapField;
        } else {
            field = new PbField();
        }
        field.setCardinality(cardinality);
        field.setName(fieldName);
        field.setTag(tag);
        field.setType(fieldType);
        field.getComments().addAll(comments);
        comments.clear();
        return field;
    }

    /**
     * 解析判断语句块是否开始,去掉空格和空行后第一个字符为"{"的返回语句块开始
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
            throw new RuntimeException(StringUtils.format("[{}] {} not start char '{'", structName));
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
        trim();

        List<String> lines = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        while (pos < data.length) {
            var c = data[pos];
            builder.append(c);

            if (isMultiLineCommentFinish()) {
                return lines;
            }

            if (c == '\n') {
                var str = builder.toString().trim();
                builder.setLength(0);
                if (StringUtils.isNotEmpty(str)) {
                    lines.add(str);
                }
                nextLine();
                continue;
            }

            pos++;
        }
        return lines;
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
        private List<PbOption> options;

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
        public List<PbOption> getOptions() {
            return options;
        }

        /**
         * @param options the options to set
         */
        public ProtoValue setOptions(List<PbOption> options) {
            this.options = options;
            return this;
        }

    }

    /**
     * 读取proto描述文件中的值，知道分号结束
     */
    private ProtoValue readValueUtilSemicolon() throws RuntimeException {
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
                        List<PbOption> options = readFieldOptions();
                        pv.setValue(v.toString()).setOptions(options);
                        return pv;
                    }
                    if (data[pos] != ';') {
                        throw new RuntimeException(StringUtils.format("row [{}] value not end with ';'", row));
                    } else {
                        pv.setValue(v.toString());
                        return pv;
                    }
                case '[':
                    List<PbOption> options = readFieldOptions();
                    pv.setValue(v.toString()).setOptions(options);
                    return pv;
                case '\n':
                    throw new RuntimeException(StringUtils.format("row [{}] value not end with ';'", row));
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
                    throw new RuntimeException(StringUtils.format("row [{}] value not end with ';'", row));
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

        public String getKeyType() {
            return keyType;
        }

        public MapGenericType setKeyType(String keyType) {
            this.keyType = keyType;
            return this;
        }

        public String getValueType() {
            return valueType;
        }

        public MapGenericType setValueType(String valueType) {
            this.valueType = valueType;
            return this;
        }

    }

    private ProtoValue readQuoteValue(final char quote) throws RuntimeException {
        char c;
        StringBuilder v = new StringBuilder();
        ProtoValue pv = new ProtoValue();
        while (pos < data.length) {
            c = data[pos];
            if (c == '\n') {
                throw new RuntimeException("row " + row + " value not end with \"" + quote + "\"");
            } else if (c == quote) {
                if (v.length() > 0 && v.charAt(v.length() - 1) == '\\') {
                    v.deleteCharAt(v.length() - 1);
                    v.append(c);
                } else { //如果由引号引起来的字符串第二次出现引号时值结束
                    pos++;
                    trim();
                    if (data[pos] != ';' && data[pos] != ']' && data[pos] != ',') {
                        throw new RuntimeException(StringUtils.format("row [{}] value not end with ';'", row));
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
        throw new RuntimeException("row " + row + " value not end with \"\\" + quote + "\"");
    }

    private List<PbOption> readFieldOptions() throws RuntimeException {
        List<PbOption> options = new ArrayList<>();
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
            PbOption option = new PbOption();
            option.setName(name);
            option.setValue(value);
            options.add(option);
            trim();
        }
        pos++;
        trim();
        if (data[pos] != ';') {
            throw new RuntimeException(StringUtils.format("row [{}] value not end with ';'", row));
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