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
import com.zfoo.protocol.collection.ArrayUtils;

/**
 * java的数据类型和protocol buffer的数据类型的对应关系以及默认值
 */
public enum PbTypeJava {
    INT("int", "Integer", 0),
    LONG("long", "Long", 0L),
    FLOAT("float", "Float", 0F),
    DOUBLE("double", "Double", 0D),
    BOOLEAN("boolean", "Boolean", false),
    STRING("String", "String", ""),
    BYTES("byte[]", "byte[]", ArrayUtils.EMPTY_BYTE_ARRAY),
    ENUM("enum", "enum", null),
    MESSAGE("", "", null),
    OBJECT("Object", "Object", null),
    MAP("Map", "Map", null);

    PbTypeJava(final String typeString, final String boxedType, final Object defaultValue) {
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