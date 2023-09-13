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

import com.zfoo.protocol.anno.Protocol;

import java.util.Objects;

/**
 * @author godotg
 */
@Protocol(id = 1114)
public class CM_Object {

    private int a;

    private ObjectA b;

    @Override
    public String toString() {
        return "CM_Object{" + "a=" + a + ", b=" + b + '}';
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public ObjectA getB() {
        return b;
    }

    public void setB(ObjectA b) {
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Object cm_object = (CM_Object) o;
        return a == cm_object.a &&
                Objects.equals(b, cm_object.b);
    }

    @Override
    public int hashCode() {

        return Objects.hash(a, b);
    }
}
