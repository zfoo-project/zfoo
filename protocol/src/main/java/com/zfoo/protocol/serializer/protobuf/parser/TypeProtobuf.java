/*
 * Copyright (C) 2020 The zfoo Authors
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

/**
 * @author godotg
 */
public enum TypeProtobuf {
    FLOAT("float", TypeJava.FLOAT),
    DOUBLE("double", TypeJava.DOUBLE),
    INT32("int32", TypeJava.INT),
    INT64("int64", TypeJava.LONG),
    UINT32("uint32", TypeJava.INT),
    UINT64("uint64", TypeJava.LONG),
    SINT32("sint32", TypeJava.INT),
    SINT64("sint64", TypeJava.LONG),
    FIXED32("fixed32", TypeJava.INT),
    FIXED64("fixed64", TypeJava.LONG),
    SFIXED32("sfixed32", TypeJava.INT),
    SFIXED64("sfixed64", TypeJava.LONG),
    BOOL("bool", TypeJava.BOOLEAN),
    ENUM("enum", TypeJava.ENUM),
    STRING("string", TypeJava.STRING),
    BYTES("bytes", TypeJava.BYTES),
    MESSAGE("", TypeJava.MESSAGE),
    OBJECT("OBJECT", TypeJava.OBJECT),
    GROUP("group", TypeJava.MESSAGE),
    MAP("", TypeJava.MAP);

    private final String value;
    private final TypeJava javaType;

    TypeProtobuf(String value, TypeJava javaType) {
        this.value = value;
        this.javaType = javaType;
    }

    public TypeJava javaType() {
        return javaType;
    }


    public String value() {
        return this.value;
    }
}
