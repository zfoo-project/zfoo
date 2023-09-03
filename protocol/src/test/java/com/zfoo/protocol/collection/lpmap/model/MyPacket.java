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

package com.zfoo.protocol.collection.lpmap.model;


import com.zfoo.protocol.anno.Protocol;

import java.util.Objects;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 1)
public class MyPacket {

    private int a;

    private String b;

    public static MyPacket valueOf(int a, String b) {
        var packet = new MyPacket();
        packet.a = a;
        packet.b = b;
        return packet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPacket myPacket = (MyPacket) o;
        return a == myPacket.a && Objects.equals(b, myPacket.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MyPacket{");
        sb.append("a=").append(a);
        sb.append(", b='").append(b).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }
}
