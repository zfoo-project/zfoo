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
@Protocol(id = 1111)
public class SM_Int {

    private Boolean flag;

    private Byte a;

    private Short b;

    private Integer c;

    private Long d;

    private char e;

    private String f;

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Byte getA() {
        return a;
    }

    public void setA(Byte a) {
        this.a = a;
    }

    public Short getB() {
        return b;
    }

    public void setB(Short b) {
        this.b = b;
    }

    public Integer getC() {
        return c;
    }

    public void setC(Integer c) {
        this.c = c;
    }

    public Long getD() {
        return d;
    }

    public void setD(Long d) {
        this.d = d;
    }

    public char getE() {
        return e;
    }

    public void setE(char e) {
        this.e = e;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SM_Int sm_int = (SM_Int) o;
        return e == sm_int.e &&
                flag.equals(sm_int.flag) &&
                a.equals(sm_int.a) &&
                b.equals(sm_int.b) &&
                c.equals(sm_int.c) &&
                d.equals(sm_int.d) &&
                f.equals(sm_int.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, a, b, c, d, e, f);
    }
}

