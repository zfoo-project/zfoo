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

/**
 * 协议格式定义相关的常量等
 */
public class WireFormat {

    private WireFormat() {
    }

    /**
     * 定长32位占用的字节长度
     */
    public static final int FIXED_32_SIZE = 4;
    /**
     * 定长64位占用的字节长度
     */
    public static final int FIXED_64_SIZE = 8;
    /**
     * 变长整型数据最大査勇的字节数
     */
    public static final int MAX_VARINT_SIZE = 5;
    /**
     * 定长长整型最大占用的字节数
     */
    public static final int MAX_VARLONG_SIZE = 10;
    /**
     * 最小的Tag值
     */
    public static final int MIN_TAG_VALUE = 1;
    /**
     * 最大的Tag值
     */
    public static final int MAX_TAG_VALUE = (1 << 29) - 1; // 536,870,911
    /**
     * 系统预留Tag的开始值
     */
    public static final int RESERVED_TAG_VALUE_START = 19000;
    /**
     * 系统预留Tag的结束值
     */
    public static final int RESERVED_TAG_VALUE_END = 19999;
    /**
     * Tag的数据类型占用的bit个数
     */
    static final int TAG_TYPE_BITS = 3;
    /**
     * Tag类型的掩码
     */
    static final int TAG_TYPE_MASK = (1 << TAG_TYPE_BITS) - 1;
    /**
     * 空的byte数组
     */
    static final byte[] EMPTY_BYTES = new byte[0];

    /**
     * Tag值是否符合规则
     *
     * @param tag 给定的Tag值
     * @return 是否符合规则
     */
    public static boolean isValidTag(int tag) {
        return (tag >= MIN_TAG_VALUE && tag < RESERVED_TAG_VALUE_START)
                || (tag > RESERVED_TAG_VALUE_END && tag <= MAX_TAG_VALUE);
    }

    /**
     * 从int值盅获取该Tag的数据类型的int值
     *
     * @param tag 给定的int值
     * @return
     */
    public static int getTagWireType(final int tag) {
        return tag & TAG_TYPE_MASK;
    }

    /**
     * 获取给定的int值中Tag的值
     *
     * @param tag 解码出的tag的int值
     * @return
     */
    public static int getTagFieldNumber(final int tag) {
        return tag >>> TAG_TYPE_BITS;
    }

    /**
     * 根据属性的Tag值，以及数据类型，获取一个编码后的int值
     *
     * @param tagNum   tag的编号
     * @param wireType 数据类型
     * @return
     */
    public static int makeTag(final int tagNum, final WireType wireType) {
        return (tagNum << TAG_TYPE_BITS) | wireType.getValue();
    }

    /**
     * java的数据类型和protocol buffer的数据类型的对应关系以及默认值
     */
    public enum JavaType {
        INT("int", "Integer", 0),
        LONG("long", "Long", 0L),
        FLOAT("float", "Float", 0F),
        DOUBLE("double", "Double", 0D),
        BOOLEAN("boolean", "Boolean", false),
        STRING("String", "String", ""),
        BYTES("byte[]", "byte[]", EMPTY_BYTES),
        ENUM("enum", "enum", null),
        MESSAGE("", "", null),
        OBJECT("Object", "Object", null),
        MAP("Map", "Map", null);

        JavaType(final String typeString, final String boxedType, final Object defaultValue) {
            this.typeString = typeString;
            this.boxedType = boxedType;
            this.defaultValue = defaultValue;
        }

        public String getTypeString() {
            return this.typeString;
        }

        /**
         * The default value for fields of this type, if it's a primitive type.
         */
        public Object defaultValue() {
            return this.defaultValue;
        }

        public String getBoxedType() {
            return this.boxedType;
        }

        private final Object defaultValue;
        private final String typeString;

        private final String boxedType;
    }
}