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

package com.zfoo.protocol.serializer.protobuf.wire;


import com.zfoo.protocol.serializer.protobuf.wire.TagReserved.StartEnd;
import com.zfoo.protocol.serializer.protobuf.wire.parser.ProtoParser;

import java.util.ArrayList;
import java.util.List;

/**
 * protocol buffer中预留信息的结构定义
 */
public class Reserved {
    /**
     * 消息保留Field列表的表达式
     */
    private String expression;
    /**
     * 保留Fields设置的注释信息
     */
    private Comment comment;

    public Reserved setExpression(String expression) {
        this.expression = expression;
        return this;
    }

    public String getExpression() {
        return expression;
    }

    public Reserved setComment(Comment comment) {
        this.comment = comment;
        return this;
    }

    public Comment getComment() {
        return comment;
    }

    public static Reserved parseReserved(String expression, List<Comment> comments) throws RuntimeException {
        String exp = expression.trim();
        if (exp.isEmpty()) {
            throw new RuntimeException("reserved expression is empty");
        }
        Reserved reserved;
        if (exp.charAt(0) == '\'' || exp.charAt(0) == '"') {
            reserved = new NameReserved().setFieldNames(readFieldNames(exp));
        } else {
            reserved = readRagReserved(exp);
        }
        if (!comments.isEmpty()) {
            reserved.setComment(comments.get(comments.size() - 1));
            comments.clear();
        }
        reserved.setExpression(exp);
        return reserved;
    }

    public static TagReserved readRagReserved(String expression) throws RuntimeException {
        TagReserved reserved = new TagReserved();
        String[] stags = expression.split(",");
        List<Integer> tags = new ArrayList<>();
        List<StartEnd> startEnds = new ArrayList<>();
        for (String sTag : stags) {
            int index = sTag.indexOf("to");
            if (index == -1) {
                int tag = ProtoParser.parseInt(sTag.trim());
                tags.add(tag);
            } else {
                int start = ProtoParser.parseInt(sTag.substring(0, index).trim());
                int end;
                String sEnd = sTag.substring(index + 2).trim();
                if (!"max".equalsIgnoreCase(sEnd)) {
                    end = ProtoParser.parseInt(sTag.substring(index + 2).trim());
                } else {
                    end = WireFormat.MAX_TAG_VALUE;
                }
                StartEnd se = new StartEnd();
                se.setStartTag(start).setEndTag(end);
                startEnds.add(se);
            }
        }
        reserved.setTags(tags);
        reserved.setStartEnds(startEnds);
        return reserved;
    }

    public static List<String> readFieldNames(String exp) throws RuntimeException {
        ParseFieldNameResult res = readFieldName(exp, 0);
        char endChar = res.getEndChar();
        List<String> names = new ArrayList<>();
        while (endChar != ';') {
            names.add(res.getName());
            if (res.getPos() >= exp.length()) {
                return names;
            }
            res.setPos(res.getPos() + 1);
            trim(exp, res);
            if (res.getPos() < exp.length()) {
                res = readFieldName(exp, res.getPos());
                endChar = res.getEndChar();
                if (endChar != ',' && endChar != '\'' && endChar != '"') {
                    break;
                }
            }
        }
        throw new RuntimeException("reserved not end \";\"");
    }

    private static ParseFieldNameResult readFieldName(String exp, int pos)
            throws RuntimeException {
        ParseFieldNameResult result = new ParseFieldNameResult();
        result.setPos(pos);
        trim(exp, result);
        char startChar = result.getEndChar();
        if (startChar != '\'' && startChar != '"') {
            throw new RuntimeException("field name not start with ' or \"");
        }
        StringBuilder sb = new StringBuilder();
        int start = result.getPos();
        char c;
        for (int i = start + 1; i < exp.length(); i++) {
            c = exp.charAt(i);
            if (c == startChar) {
                if (sb.length() > 0) {
                    result.setPos(i + 1);
                    trim(exp, result);
                    result.setName(sb.toString());
                    return result;
                } else {
                    result.setName(sb.toString());
                    return result;
                }
            } else {
                sb.append(c);
            }
        }
        return result;
    }

    private static void trim(String exp, final ParseFieldNameResult result) {
        int pos = result.getPos();
        char c;
        for (int i = pos; i < exp.length(); i++) {
            c = exp.charAt(i);
            if (c != ' ' && c != '\t') {
                result.setPos(i);
                result.setEndChar(c);
                return;
            }
        }
    }

    static class ParseFieldNameResult {
        private String name;
        private int pos;
        private char endChar;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public ParseFieldNameResult setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @return the pos
         */
        public int getPos() {
            return pos;
        }

        /**
         * @param pos the pos to set
         */
        public ParseFieldNameResult setPos(int pos) {
            this.pos = pos;
            return this;
        }

        /**
         * @return the endChar
         */
        public char getEndChar() {
            return endChar;
        }

        /**
         * @param endChar the endChar to set
         */
        public ParseFieldNameResult setEndChar(char endChar) {
            this.endChar = endChar;
            return this;
        }


    }
}