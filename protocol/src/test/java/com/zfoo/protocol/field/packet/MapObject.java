package com.zfoo.protocol.field.packet;



import java.util.Map;
import java.util.Objects;

public class MapObject {
    private Map<Integer, String> a;

    private Map<Integer, String> b;

    private Map<Integer, String> c;

    private Map<Integer, String> d;

    private Map<Integer, String> e;

    private Map<Integer, String> f;

    private Map<Integer, String> g;

    private Map<Integer, String> h;

    private Map<Integer, String> i;

    private Map<Integer, String> j;

    public Map<Integer, String> getA() {
        return a;
    }

    public void setA(Map<Integer, String> a) {
        this.a = a;
    }

    public Map<Integer, String> getB() {
        return b;
    }

    public void setB(Map<Integer, String> b) {
        this.b = b;
    }

    public Map<Integer, String> getC() {
        return c;
    }

    public void setC(Map<Integer, String> c) {
        this.c = c;
    }

    public Map<Integer, String> getD() {
        return d;
    }

    public void setD(Map<Integer, String> d) {
        this.d = d;
    }

    public Map<Integer, String> getE() {
        return e;
    }

    public void setE(Map<Integer, String> e) {
        this.e = e;
    }

    public Map<Integer, String> getF() {
        return f;
    }

    public void setF(Map<Integer, String> f) {
        this.f = f;
    }

    public Map<Integer, String> getG() {
        return g;
    }

    public void setG(Map<Integer, String> g) {
        this.g = g;
    }

    public Map<Integer, String> getH() {
        return h;
    }

    public void setH(Map<Integer, String> h) {
        this.h = h;
    }

    public Map<Integer, String> getI() {
        return i;
    }

    public void setI(Map<Integer, String> i) {
        this.i = i;
    }

    public Map<Integer, String> getJ() {
        return j;
    }

    public void setJ(Map<Integer, String> j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapObject mapObject = (MapObject) o;
        return Objects.equals(a, mapObject.a) && Objects.equals(b, mapObject.b) && Objects.equals(c, mapObject.c) && Objects.equals(d, mapObject.d) && Objects.equals(e, mapObject.e) && Objects.equals(f, mapObject.f) && Objects.equals(g, mapObject.g) && Objects.equals(h, mapObject.h) && Objects.equals(i, mapObject.i) && Objects.equals(j, mapObject.j);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j);
    }
}
