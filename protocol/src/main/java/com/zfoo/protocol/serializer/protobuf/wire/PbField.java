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

import com.zfoo.protocol.serializer.protobuf.parser.JavaType;

/**
 * protocol buffer协议消息体属性数据类型定义
 */
public class PbField {
    /**
     * 属性是否赋值类型
     */
    public enum Cardinality {
        /**
         * 未知类型
         */
        UNKNOWN("unknown"),
        /**
         * 必须赋值
         */
        REQUIRED("required"),
        /**
         * 可以不赋值
         */
        OPTIONAL("optional"),
        /**
         * 可以赋多个值
         */
        REPEATED("repeated");

        private final String value;

        public String getValue() {
            return this.value;
        }

        Cardinality(String value) {
            this.value = value;
        }
    }

    public enum Type {

        FLOAT("float", JavaType.FLOAT),
        DOUBLE("double", JavaType.DOUBLE),
        INT32("int32", JavaType.INT),
        INT64("int64", JavaType.LONG),
        UINT32("uint32", JavaType.INT),
        UINT64("uint64", JavaType.LONG),
        SINT32("sint32", JavaType.INT),
        SINT64("sint64", JavaType.LONG),
        FIXED32("fixed32", JavaType.INT),
        FIXED64("fixed64", JavaType.LONG),
        SFIXED32("sfixed32", JavaType.INT),
        SFIXED64("sfixed64", JavaType.LONG),
        BOOL("bool", JavaType.BOOLEAN),
        ENUM("enum", JavaType.ENUM),
        STRING("string", JavaType.STRING),
        BYTES("bytes", JavaType.BYTES),
        MESSAGE("", JavaType.MESSAGE),
        OBJECT("OBJECT", JavaType.OBJECT),
        GROUP("group", JavaType.MESSAGE),
        MAP("", JavaType.MAP);

        private final String value;
        private final JavaType javaType;

        Type(String value,JavaType javaType) {
            this.value = value;
            this.javaType = javaType;
        }

        public JavaType javaType() {
            return javaType;
        }


        public String value() {
            return this.value;
        }

    }

    /**
     * 消息属性的规则
     */
    private Cardinality cardinality;
    /**
     * 消息属性名称
     */
    private String name;
    /**
     * 消息属性值的类型
     */
    private String type;
    /**
     * 属性的顺序标记一个消息内不允许重复
     */
    private int tag;
    /**
     * 消息属性的注释信息
     */
    private Comment comment;

    public String getTypeString() {
        return this.getType();
    }

    /**
     * 消息属性的规则
     *
     * @return the cardinality
     */
    public Cardinality getCardinality() {
        return cardinality;
    }

    /**
     * 消息属性的规则
     *
     * @param cardinality the cardinality to set
     * @return
     */
    public PbField setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
        return this;
    }

    /**
     * 消息属性名称
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * 消息属性名称
     *
     * @param name the name to set
     * @return
     */
    public PbField setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * 消息属性值的类型
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * 消息属性值的类型
     *
     * @param type the type to set
     * @return
     */
    public PbField setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * 属性的顺序标记一个消息内不允许重复
     *
     * @return the tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * 属性的顺序标记一个消息内不允许重复
     *
     * @param tag the tag to set
     * @return
     */
    public PbField setTag(int tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 消息属性的注释信息
     *
     * @return the comment
     */
    public Comment getComment() {
        return comment;
    }

    /**
     * 消息属性的注释信息
     *
     * @param comment the comment to set
     * @return
     */
    public PbField setComment(Comment comment) {
        this.comment = comment;
        return this;
    }
}