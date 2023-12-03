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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * protocol buffer协议消息体属性数据类型定义
 */
public class Field {
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

        FLOAT("float", WireFormat.JavaType.FLOAT, WireType.FIXED32),
        DOUBLE("double", WireFormat.JavaType.DOUBLE, WireType.FIXED64),
        INT32("int32", WireFormat.JavaType.INT, WireType.VARINT),
        INT64("int64", WireFormat.JavaType.LONG, WireType.VARINT),
        UINT32("uint32", WireFormat.JavaType.INT, WireType.VARINT),
        UINT64("uint64", WireFormat.JavaType.LONG, WireType.VARINT),
        SINT32("sint32", WireFormat.JavaType.INT, WireType.VARINT),
        SINT64("sint64", WireFormat.JavaType.LONG, WireType.VARINT),
        FIXED32("fixed32", WireFormat.JavaType.INT, WireType.FIXED32),
        FIXED64("fixed64", WireFormat.JavaType.LONG, WireType.FIXED64),
        SFIXED32("sfixed32", WireFormat.JavaType.INT, WireType.FIXED32),
        SFIXED64("sfixed64", WireFormat.JavaType.LONG, WireType.FIXED64),
        BOOL("bool", WireFormat.JavaType.BOOLEAN, WireType.VARINT),
        ENUM("enum", WireFormat.JavaType.ENUM, WireType.VARINT),
        STRING("string", WireFormat.JavaType.STRING, WireType.LENGTH_DELIMITED) {
            @Override
            public boolean packable() {
                return false;
            }
        },
        BYTES("bytes", WireFormat.JavaType.BYTES, WireType.LENGTH_DELIMITED) {
            @Override
            public boolean packable() {
                return false;
            }
        },
        MESSAGE("", WireFormat.JavaType.MESSAGE, WireType.LENGTH_DELIMITED) {
            @Override
            public boolean packable() {
                return false;
            }
        },
        OBJECT("OBJECT", WireFormat.JavaType.OBJECT, WireType.LENGTH_DELIMITED) {
            @Override
            public boolean packable() {
                return false;
            }
        },
        GROUP("group", WireFormat.JavaType.MESSAGE, WireType.START_GROUP) {
            @Override
            public boolean packable() {
                return false;
            }
        },
        MAP("", WireFormat.JavaType.MAP, WireType.LENGTH_DELIMITED) {
            @Override
            public boolean packable() {
                return false;
            }
        };

        private final String value;
        private final WireFormat.JavaType javaType;
        private final WireType wireType;

        Type(String value, WireFormat.JavaType javaType, WireType wireType) {
            this.value = value;
            this.javaType = javaType;
            this.wireType = wireType;
        }

        public WireFormat.JavaType javaType() {
            return javaType;
        }

        public WireType wireType() {
            return wireType;
        }

        public String value() {
            return this.value;
        }

        public boolean packable() {
            return true;
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
     * 消息属性默认值
     */
    private String defaultValue;
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
    public Field setCardinality(Cardinality cardinality) {
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
    public Field setName(String name) {
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
    public Field setType(String type) {
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
    public Field setTag(int tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 消息属性默认值
     *
     * @return the defaultValue
     */
    public String getDefaultValue() {
        if (defaultValue == null) {
            try {
                Type fieldType = Type.valueOf(type.toUpperCase(Locale.ENGLISH));
                if (fieldType.javaType() != null && fieldType.javaType().defaultValue() != null) {
                    return String.valueOf(fieldType.javaType().defaultValue());
                }
            } catch (Exception e) {

            }
        } else {
            return defaultValue;
        }
        return null;
    }

    /**
     * 消息属性默认值
     *
     * @param defaultValue the defaultValue to set
     * @return
     */
    public Field setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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
    public Field setComment(Comment comment) {
        this.comment = comment;
        return this;
    }
}