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

import java.util.Arrays;

/**
 * @author godotg
 */
@Protocol(id = 1119)
public class CM_Array {

    private int[] a;

    private ObjectA[] b;


    @Override
    public String toString() {
        return "CM_Array{" + "a=" + Arrays.toString(a) + ", b=" + Arrays.toString(b) + '}';
    }

    public int[] getA() {
        return a;
    }

    public void setA(int[] a) {
        this.a = a;
    }

    public ObjectA[] getB() {
        return b;
    }

    public void setB(ObjectA[] b) {
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Array cm_array = (CM_Array) o;
        return Arrays.equals(a, cm_array.a) && Arrays.equals(b, cm_array.b);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(a);
        result = 31 * result + Arrays.hashCode(b);
        return result;
    }
}
