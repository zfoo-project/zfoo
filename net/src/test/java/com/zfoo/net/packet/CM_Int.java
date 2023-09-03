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
 * @version 3.0
 */
@Protocol(id = 1110)
public class CM_Int implements IPacket {

    private boolean flag;

    private byte a;

    private short b;

    private int c;

    private long d;

    private char e;

    private String f;

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public byte getA() {
        return a;
    }

    public char getE() {
        return e;
    }

    public void setE(char e) {
        this.e = e;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public short getB() {
        return b;
    }

    public void setB(short b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public long getD() {
        return d;
    }

    public void setD(long d) {
        this.d = d;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CM_Int cm_int = (CM_Int) o;
        return flag == cm_int.flag &&
                a == cm_int.a &&
                b == cm_int.b &&
                c == cm_int.c &&
                d == cm_int.d &&
                e == cm_int.e &&
                f.equals(cm_int.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag, a, b, c, d, e, f);
    }
}
