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
@Protocol(id = 1112)
public class CM_Float {

    private float a;

    private Float b;

    private double c;

    private Double d;

    @Override
    public String toString() {
        return "CM_Float{" + "a=" + a + ", b=" + b + ", c=" + c + ", d=" + d + '}';
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public Float getB() {
        return b;
    }

    public void setB(Float b) {
        this.b = b;
    }

    public double getC() {
        return c;
    }

    public void setC(double c) {
        this.c = c;
    }

    public Double getD() {
        return d;
    }

    public void setD(Double d) {
        this.d = d;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Float cm_float = (CM_Float) o;
        return Float.compare(cm_float.a, a) == 0 &&
                Double.compare(cm_float.c, c) == 0 &&
                Objects.equals(b, cm_float.b) &&
                Objects.equals(d, cm_float.d);
    }

    @Override
    public int hashCode() {

        return Objects.hash(a, b, c, d);
    }
}
