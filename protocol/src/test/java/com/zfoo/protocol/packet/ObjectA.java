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

package com.zfoo.protocol.packet;


import com.zfoo.protocol.anno.Protocol;

import java.util.Map;
import java.util.Objects;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 102)
public class ObjectA {

    private int a;

    private Map<Integer, String> m;

    private ObjectB objectB;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public Map<Integer, String> getM() {
        return m;
    }

    public void setM(Map<Integer, String> m) {
        this.m = m;
    }

    public ObjectB getObjectB() {
        return objectB;
    }

    public void setObjectB(ObjectB objectB) {
        this.objectB = objectB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectA objectA = (ObjectA) o;
        return a == objectA.a &&
                Objects.equals(m, objectA.m) &&
                Objects.equals(objectB, objectA.objectB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, m, objectB);
    }
}
