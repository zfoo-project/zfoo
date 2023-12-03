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
 * protocol buffer中Map的Field的结构体定义
 */
public class MapField extends PbField {

    private Type key;
    private String value;

    @Override
    public String getTypeString() {
        StringBuilder sb = new StringBuilder();
        String keyStr = "";
        if (getKey() != null) {
            keyStr = getKey().value();
        }
        sb.append("map<").append(keyStr).append(", ");
        sb.append(getValue()).append(">");
        return sb.toString();
    }

    /**
     * @return the key
     */
    public Type getKey() {
        return key;
    }

    /**
     * @param key the key to set
     * @return
     */
    public MapField setKey(Type key) {
        this.key = key;
        return this;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     * @return
     */
    public MapField setValue(String value) {
        this.value = value;
        return this;
    }
}