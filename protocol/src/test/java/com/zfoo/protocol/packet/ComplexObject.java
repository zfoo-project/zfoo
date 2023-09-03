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
import com.zfoo.protocol.anno.Note;
import com.zfoo.protocol.anno.Protocol;

import java.util.*;

@Protocol(id = 100, note = "复杂的对象，包括了各种复杂的结构，数组，List，Set，Map")
public class ComplexObject {


    @Note("byte类型，最简单的整形")
    private byte a;
    @Note("byte的包装类型，优先使用基础类型，包装类型会有装箱拆箱")
    private Byte aa;
    @Note("数组类型")
    private byte[] aaa;
    private Byte[] aaaa;

    private short b;
    private Short bb;
    private short[] bbb;
    private Short[] bbbb;

    private int c;
    private Integer cc;
    private int[] ccc;
    private Integer[] cccc;

    private long d;
    private Long dd;
    private long[] ddd;
    private Long[] dddd;

    private float e;
    private Float ee;
    private float[] eee;
    private Float[] eeee;

    private double f;
    private Double ff;
    private double[] fff;
    private Double[] ffff;

    private boolean g;
    private Boolean gg;
    private boolean[] ggg;
    private Boolean[] gggg;

    private char h;
    private Character hh;
    private char[] hhh;
    private Character[] hhhh;

    private String jj;
    private String[] jjj;

    private ObjectA kk;
    private ObjectA[] kkk;


    private List<Integer> l;
    private List<List<List<Integer>>> ll;
    private List<List<ObjectA>> lll;
    private List<String> llll;
    private List<Map<Integer, String>> lllll;

    private Map<Integer, String> m;
    private Map<Integer, ObjectA> mm;
    private Map<ObjectA, List<Integer>> mmm;
    private Map<List<List<ObjectA>>, List<List<List<Integer>>>> mmmm;
    private Map<List<Map<Integer, String>>, Set<Map<Integer, String>>> mmmmm;

    private Set<Integer> s;
    private Set<Set<List<Integer>>> ss;
    private Set<Set<ObjectA>> sss;
    private Set<String> ssss;
    private Set<Map<Integer, String>> sssss;

    @Note("如果要修改协议并且兼容老协议，需要加上Compatible注解，按照增加的顺序添加order")
    @Compatible(order = 1)
    private int myCompatible;
    @Compatible(order = 2)
    private ObjectA myObject;

    public byte getA() {
        return a;
    }

    public void setA(byte a) {
        this.a = a;
    }

    public Byte getAa() {
        return aa;
    }

    public void setAa(Byte aa) {
        this.aa = aa;
    }

    public byte[] getAaa() {
        return aaa;
    }

    public void setAaa(byte[] aaa) {
        this.aaa = aaa;
    }

    public Byte[] getAaaa() {
        return aaaa;
    }

    public void setAaaa(Byte[] aaaa) {
        this.aaaa = aaaa;
    }

    public short getB() {
        return b;
    }

    public void setB(short b) {
        this.b = b;
    }

    public Short getBb() {
        return bb;
    }

    public void setBb(Short bb) {
        this.bb = bb;
    }

    public short[] getBbb() {
        return bbb;
    }

    public void setBbb(short[] bbb) {
        this.bbb = bbb;
    }

    public Short[] getBbbb() {
        return bbbb;
    }

    public void setBbbb(Short[] bbbb) {
        this.bbbb = bbbb;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    public Integer getCc() {
        return cc;
    }

    public void setCc(Integer cc) {
        this.cc = cc;
    }

    public int[] getCcc() {
        return ccc;
    }

    public void setCcc(int[] ccc) {
        this.ccc = ccc;
    }

    public Integer[] getCccc() {
        return cccc;
    }

    public void setCccc(Integer[] cccc) {
        this.cccc = cccc;
    }

    public long getD() {
        return d;
    }

    public void setD(long d) {
        this.d = d;
    }

    public Long getDd() {
        return dd;
    }

    public void setDd(Long dd) {
        this.dd = dd;
    }

    public long[] getDdd() {
        return ddd;
    }

    public void setDdd(long[] ddd) {
        this.ddd = ddd;
    }

    public Long[] getDddd() {
        return dddd;
    }

    public void setDddd(Long[] dddd) {
        this.dddd = dddd;
    }

    public float getE() {
        return e;
    }

    public void setE(float e) {
        this.e = e;
    }

    public Float getEe() {
        return ee;
    }

    public void setEe(Float ee) {
        this.ee = ee;
    }

    public float[] getEee() {
        return eee;
    }

    public void setEee(float[] eee) {
        this.eee = eee;
    }

    public Float[] getEeee() {
        return eeee;
    }

    public void setEeee(Float[] eeee) {
        this.eeee = eeee;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public Double getFf() {
        return ff;
    }

    public void setFf(Double ff) {
        this.ff = ff;
    }

    public double[] getFff() {
        return fff;
    }

    public void setFff(double[] fff) {
        this.fff = fff;
    }

    public Double[] getFfff() {
        return ffff;
    }

    public void setFfff(Double[] ffff) {
        this.ffff = ffff;
    }

    public boolean isG() {
        return g;
    }

    public void setG(boolean g) {
        this.g = g;
    }

    public Boolean getGg() {
        return gg;
    }

    public void setGg(Boolean gg) {
        this.gg = gg;
    }

    public boolean[] getGgg() {
        return ggg;
    }

    public void setGgg(boolean[] ggg) {
        this.ggg = ggg;
    }

    public Boolean[] getGggg() {
        return gggg;
    }

    public void setGggg(Boolean[] gggg) {
        this.gggg = gggg;
    }

    public char getH() {
        return h;
    }

    public void setH(char h) {
        this.h = h;
    }

    public Character getHh() {
        return hh;
    }

    public void setHh(Character hh) {
        this.hh = hh;
    }

    public char[] getHhh() {
        return hhh;
    }

    public void setHhh(char[] hhh) {
        this.hhh = hhh;
    }

    public Character[] getHhhh() {
        return hhhh;
    }

    public void setHhhh(Character[] hhhh) {
        this.hhhh = hhhh;
    }

    public String getJj() {
        return jj;
    }

    public void setJj(String jj) {
        this.jj = jj;
    }

    public String[] getJjj() {
        return jjj;
    }

    public void setJjj(String[] jjj) {
        this.jjj = jjj;
    }

    public ObjectA getKk() {
        return kk;
    }

    public void setKk(ObjectA kk) {
        this.kk = kk;
    }

    public ObjectA[] getKkk() {
        return kkk;
    }

    public void setKkk(ObjectA[] kkk) {
        this.kkk = kkk;
    }

    public List<Integer> getL() {
        return l;
    }

    public void setL(List<Integer> l) {
        this.l = l;
    }

    public List<List<List<Integer>>> getLl() {
        return ll;
    }

    public void setLl(List<List<List<Integer>>> ll) {
        this.ll = ll;
    }

    public List<List<ObjectA>> getLll() {
        return lll;
    }

    public void setLll(List<List<ObjectA>> lll) {
        this.lll = lll;
    }

    public List<String> getLlll() {
        return llll;
    }

    public void setLlll(List<String> llll) {
        this.llll = llll;
    }

    public List<Map<Integer, String>> getLllll() {
        return lllll;
    }

    public void setLllll(List<Map<Integer, String>> lllll) {
        this.lllll = lllll;
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

    public Map<ObjectA, List<Integer>> getMmm() {
        return mmm;
    }

    public void setMmm(Map<ObjectA, List<Integer>> mmm) {
        this.mmm = mmm;
    }

    public Map<List<List<ObjectA>>, List<List<List<Integer>>>> getMmmm() {
        return mmmm;
    }

    public void setMmmm(Map<List<List<ObjectA>>, List<List<List<Integer>>>> mmmm) {
        this.mmmm = mmmm;
    }

    public Map<List<Map<Integer, String>>, Set<Map<Integer, String>>> getMmmmm() {
        return mmmmm;
    }

    public void setMmmmm(Map<List<Map<Integer, String>>, Set<Map<Integer, String>>> mmmmm) {
        this.mmmmm = mmmmm;
    }

    public Set<Integer> getS() {
        return s;
    }

    public void setS(Set<Integer> s) {
        this.s = s;
    }

    public Set<Set<List<Integer>>> getSs() {
        return ss;
    }

    public void setSs(Set<Set<List<Integer>>> ss) {
        this.ss = ss;
    }

    public Set<Set<ObjectA>> getSss() {
        return sss;
    }

    public void setSss(Set<Set<ObjectA>> sss) {
        this.sss = sss;
    }

    public Set<String> getSsss() {
        return ssss;
    }

    public void setSsss(Set<String> ssss) {
        this.ssss = ssss;
    }

    public Set<Map<Integer, String>> getSssss() {
        return sssss;
    }

    public void setSssss(Set<Map<Integer, String>> sssss) {
        this.sssss = sssss;
    }

    public int getMyCompatible() {
        return myCompatible;
    }

    public void setMyCompatible(int myCompatible) {
        this.myCompatible = myCompatible;
    }

    public ObjectA getMyObject() {
        return myObject;
    }

    public void setMyObject(ObjectA myObject) {
        this.myObject = myObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexObject that = (ComplexObject) o;
        return a == that.a &&
                b == that.b &&
                c == that.c &&
                d == that.d &&
                Float.compare(that.e, e) == 0 &&
                Double.compare(that.f, f) == 0 &&
                g == that.g &&
                h == that.h &&
                Objects.equals(aa, that.aa) &&
                Arrays.equals(aaa, that.aaa) &&
                Arrays.equals(aaaa, that.aaaa) &&
                Objects.equals(bb, that.bb) &&
                Arrays.equals(bbb, that.bbb) &&
                Arrays.equals(bbbb, that.bbbb) &&
                Objects.equals(cc, that.cc) &&
                Arrays.equals(ccc, that.ccc) &&
                Arrays.equals(cccc, that.cccc) &&
                Objects.equals(dd, that.dd) &&
                Arrays.equals(ddd, that.ddd) &&
                Arrays.equals(dddd, that.dddd) &&
                Objects.equals(ee, that.ee) &&
                Arrays.equals(eee, that.eee) &&
                Arrays.equals(eeee, that.eeee) &&
                Objects.equals(ff, that.ff) &&
                Arrays.equals(fff, that.fff) &&
                Arrays.equals(ffff, that.ffff) &&
                Objects.equals(gg, that.gg) &&
                Arrays.equals(ggg, that.ggg) &&
                Arrays.equals(gggg, that.gggg) &&
                Objects.equals(hh, that.hh) &&
                Arrays.equals(hhh, that.hhh) &&
                Arrays.equals(hhhh, that.hhhh) &&
                Objects.equals(jj, that.jj) &&
                Arrays.equals(jjj, that.jjj) &&
                Objects.equals(kk, that.kk) &&
                Arrays.equals(kkk, that.kkk) &&
                Objects.equals(l, that.l) &&
                Objects.equals(ll, that.ll) &&
                Objects.equals(lll, that.lll) &&
                Objects.equals(llll, that.llll) &&
                Objects.equals(lllll, that.lllll) &&
                Objects.equals(m, that.m) &&
                Objects.equals(mm, that.mm) &&
                Objects.equals(mmm, that.mmm) &&
                Objects.equals(mmmm, that.mmmm) &&
                Objects.equals(mmmmm, that.mmmmm) &&
                Objects.equals(s, that.s) &&
                Objects.equals(ss, that.ss) &&
                Objects.equals(sss, that.sss) &&
                Objects.equals(ssss, that.ssss) &&
                Objects.equals(sssss, that.sssss);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(a, aa, b, bb, c, cc, d, dd, e, ee, f, ff, g, gg, h, hh, jj, kk, l, ll, lll, llll, lllll, m, mm, mmm, mmmm, mmmmm, s, ss, sss, ssss, sssss);
        result = 31 * result + Arrays.hashCode(aaa);
        result = 31 * result + Arrays.hashCode(aaaa);
        result = 31 * result + Arrays.hashCode(bbb);
        result = 31 * result + Arrays.hashCode(bbbb);
        result = 31 * result + Arrays.hashCode(ccc);
        result = 31 * result + Arrays.hashCode(cccc);
        result = 31 * result + Arrays.hashCode(ddd);
        result = 31 * result + Arrays.hashCode(dddd);
        result = 31 * result + Arrays.hashCode(eee);
        result = 31 * result + Arrays.hashCode(eeee);
        result = 31 * result + Arrays.hashCode(fff);
        result = 31 * result + Arrays.hashCode(ffff);
        result = 31 * result + Arrays.hashCode(ggg);
        result = 31 * result + Arrays.hashCode(gggg);
        result = 31 * result + Arrays.hashCode(hhh);
        result = 31 * result + Arrays.hashCode(hhhh);
        result = 31 * result + Arrays.hashCode(jjj);
        result = 31 * result + Arrays.hashCode(kkk);
        return result;
    }
}
