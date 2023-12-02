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
 * protocol buffer的协议数据类型
 */
public enum WireType {
    /**
     * 变长整数
     */
    VARINT(0),
    /**
     * 定长64位
     */
    FIXED64(1),
    /**
     * 指定长度的编码类型
     */
    LENGTH_DELIMITED(2),
    /**
     * 组开始标记
     */
    START_GROUP(3),
    /**
     * 组结束标记
     */
    END_GROUP(4),
    /**
     * 定长32位
     */
    FIXED32(5),
    /**
     * OBJECT类型，可以包含任何类型的数据由OBJECT编解码器进行编解码
     */
    OBJECT(6);

    WireType(int value) {
        this.value = value;
    }

    private final int value;

    public int getValue() {
        return value;
    }

    public static WireType fromValue(int value) {
        switch (value) {
            case 0:
                return VARINT;
            case 1:
                return FIXED64;
            case 2:
                return LENGTH_DELIMITED;
            case 3:
                return START_GROUP;
            case 4:
                return END_GROUP;
            case 5:
                return FIXED32;
            case 6:
                return OBJECT;
            default:
                throw new IllegalArgumentException("no enum value WireType " + value);
        }
    }


}