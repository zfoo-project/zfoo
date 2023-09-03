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

import com.zfoo.net.packet.IPacket;
import com.zfoo.protocol.anno.Protocol;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author godotg
 * @version 3.0
 */
@Protocol(id = 2070)
public class WebSocketPacketRequest implements IPacket {

    private byte a;
    private Byte aa;
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
    private long[] dd;

    private float e;
    private Float ee;
    private float[] eee;
    private Float[] eeee;

    private double f;
    private Double ff;
    private double[] fff;
    private Double[] ffff;

    private char g;
    private char[] gg;
    private List<Character> ggg;

    private String jj;
    private String[] jjj;

    private WebSocketObjectA kk;
    private WebSocketObjectA[] kkk;

    private List<Integer> l;
    private List<List<List<Integer>>> ll;
    private List<List<WebSocketObjectA>> lll;
    private List<String> llll;
    private List<Map<Integer, String>> lllll;

    private Map<Integer, String> m;
    private Map<Integer, WebSocketObjectA> mm;
    private Map<WebSocketObjectA, List<Integer>> mmm;
    private Map<List<List<WebSocketObjectA>>, List<List<List<Integer>>>> mmmm;

    private Set<Integer> s;
    private Set<Set<List<Integer>>> ss;
    private Set<Set<WebSocketObjectA>> sss;
    private Set<String> ssss;
    private Set<Map<Integer, String>> sssss;

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

    public long[] getDd() {
        return dd;
    }

    public void setDd(long[] dd) {
        this.dd = dd;
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

    public char getG() {
        return g;
    }

    public void setG(char g) {
        this.g = g;
    }

    public char[] getGg() {
        return gg;
    }

    public void setGg(char[] gg) {
        this.gg = gg;
    }

    public List<Character> getGgg() {
        return ggg;
    }

    public void setGgg(List<Character> ggg) {
        this.ggg = ggg;
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

    public WebSocketObjectA getKk() {
        return kk;
    }

    public void setKk(WebSocketObjectA kk) {
        this.kk = kk;
    }

    public WebSocketObjectA[] getKkk() {
        return kkk;
    }

    public void setKkk(WebSocketObjectA[] kkk) {
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

    public List<List<WebSocketObjectA>> getLll() {
        return lll;
    }

    public void setLll(List<List<WebSocketObjectA>> lll) {
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

    public Map<Integer, WebSocketObjectA> getMm() {
        return mm;
    }

    public void setMm(Map<Integer, WebSocketObjectA> mm) {
        this.mm = mm;
    }

    public Map<WebSocketObjectA, List<Integer>> getMmm() {
        return mmm;
    }

    public void setMmm(Map<WebSocketObjectA, List<Integer>> mmm) {
        this.mmm = mmm;
    }

    public Map<List<List<WebSocketObjectA>>, List<List<List<Integer>>>> getMmmm() {
        return mmmm;
    }

    public void setMmmm(Map<List<List<WebSocketObjectA>>, List<List<List<Integer>>>> mmmm) {
        this.mmmm = mmmm;
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

    public Set<Set<WebSocketObjectA>> getSss() {
        return sss;
    }

    public void setSss(Set<Set<WebSocketObjectA>> sss) {
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
}
