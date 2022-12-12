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

package com.zfoo.net.packet;

import com.zfoo.protocol.IPacket;

import java.util.Objects;

/**
 * @author godotg
 * @version 3.0
 */
public class ObjectB implements IPacket {

    public static final short PROTOCOL_ID = 1117;

    private boolean flag;

    @Override
    public short protocolId() {
        return PROTOCOL_ID;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectB objectB = (ObjectB) o;
        return flag == objectB.flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag);
    }
}

