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

import java.util.HashMap;
import java.util.Map;

public enum PbType {
    FLOAT("float", PbTypeJava.FLOAT),
    DOUBLE("double", PbTypeJava.DOUBLE),
    INT32("int32", PbTypeJava.INT),
    INT64("int64", PbTypeJava.LONG),
    UINT32("uint32", PbTypeJava.INT),
    UINT64("uint64", PbTypeJava.LONG),
    SINT32("sint32", PbTypeJava.INT),
    SINT64("sint64", PbTypeJava.LONG),
    FIXED32("fixed32", PbTypeJava.INT),
    FIXED64("fixed64", PbTypeJava.LONG),
    SFIXED32("sfixed32", PbTypeJava.INT),
    SFIXED64("sfixed64", PbTypeJava.LONG),
    BOOL("bool", PbTypeJava.BOOLEAN),
    ENUM("enum", PbTypeJava.ENUM),
    STRING("string", PbTypeJava.STRING),
    BYTES("bytes", PbTypeJava.BYTES),
    MAP("map", PbTypeJava.MAP),
    ;

    private final String value;
    private final PbTypeJava javaType;

    private static final Map<String, PbType> map = new HashMap<>();

    static {
        for (var ele : PbType.values()) {
            map.put(ele.value, ele);
        }
    }

    PbType(String value, PbTypeJava javaType) {
        this.value = value;
        this.javaType = javaType;
    }

    public PbTypeJava javaType() {
        return javaType;
    }


    public String value() {
        return this.value;
    }

    public static PbType typeOfProtobuf(String str) {
        return map.get(str);
    }
}
