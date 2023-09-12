/*
 * Copyright (C) 2020 The zfoo Authors
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

package com.zfoo.protocol.registration;

import com.zfoo.protocol.util.StringUtils;

/**
 * 模块id和模块名的封装
 *
 * @author godotg
 */
public class ProtocolModule {

    public static final ProtocolModule DEFAULT_PROTOCOL_MODULE = new ProtocolModule((byte) 0, "default");

    private byte id;

    private String name;

    public ProtocolModule(byte id, String name) {
        if (id < 0) {
            throw new IllegalArgumentException(StringUtils.format("模块[{}]的id[{}]必须大于0", name, id));
        }

        this.id = id;
        this.name = name;
    }


    public byte getId() {
        return id;
    }

    public void setId(byte id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProtocolModule module = (ProtocolModule) o;
        return id == module.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return StringUtils.format("[id:{}][name:{}]", id, name);
    }
}
