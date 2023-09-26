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


import com.zfoo.protocol.anno.Compatible;
import com.zfoo.protocol.anno.Protocol;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 */
@Protocol(id = 101)
public class NormalObject {

    private byte a;
    private byte[] aaa;

    private short b;

    private int c;

    private long d;

    private float e;

    private double f;

    private boolean g;

    private String jj;

    private ObjectA kk;


    private List<Integer> l;
    private List<Long> ll;
    private List<ObjectA> lll;
    private List<String> llll;

    private Map<Integer, String> m;
    private Map<Integer, ObjectA> mm;

    private Set<Integer> s;
    private Set<String> ssss;

//    @Compatible(1)
//    public int outCompatibleValue;
//    @Compatible(2)
//    public int outCompatibleValue2;

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public byte[] getAaa() {
        return aaa;
    }

    public void setAaa(byte[] aaa) {
        this.aaa = aaa;
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

    public float getE() {
        return e;
    }

    public void setE(float e) {
        this.e = e;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public boolean isG() {
        return g;
    }

    public void setG(boolean g) {
        this.g = g;
    }

    public String getJj() {
        return jj;
    }

    public void setJj(String jj) {
        this.jj = jj;
    }

    public ObjectA getKk() {
        return kk;
    }

    public void setKk(ObjectA kk) {
        this.kk = kk;
    }

    public List<Integer> getL() {
        return l;
    }

    public void setL(List<Integer> l) {
        this.l = l;
    }

    public List<Long> getLl() {
        return ll;
    }

    public void setLl(List<Long> ll) {
        this.ll = ll;
    }

    public List<ObjectA> getLll() {
        return lll;
    }

    public void setLll(List<ObjectA> lll) {
        this.lll = lll;
    }

    public List<String> getLlll() {
        return llll;
    }

    public void setLlll(List<String> llll) {
        this.llll = llll;
    }

    public Map<Integer, String> getM() {
        return m;
    }

    public void setM(Map<Integer, String> m) {
        this.m = m;
    }

    public Map<Integer, ObjectA> getMm() {
        return mm;
    }

    public void setMm(Map<Integer, ObjectA> mm) {
        this.mm = mm;
    }

    public Set<Integer> getS() {
        return s;
    }

    public void setS(Set<Integer> s) {
        this.s = s;
    }

    public Set<String> getSsss() {
        return ssss;
    }

    public void setSsss(Set<String> ssss) {
        this.ssss = ssss;
    }
}
