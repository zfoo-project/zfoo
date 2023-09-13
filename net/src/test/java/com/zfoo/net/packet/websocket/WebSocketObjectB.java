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

package com.zfoo.net.packet.websocket;

import com.zfoo.protocol.anno.Protocol;

import java.util.Objects;

/**
 * @author godotg
 */
@Protocol(id = 2072)
public class WebSocketObjectB {

    private boolean flag;

    @Override
    public String toString() {
        return "ObjectB{" + "flag=" + flag + '}';
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
        WebSocketObjectB that = (WebSocketObjectB) o;
        return flag == that.flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag);
    }
}

