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

/**
 * protocol buffer的协议版本
 */
public enum Syntax {

    PROTO_2("proto2"),
    PROTO_3("proto3");

    private final String value;

    Syntax(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}