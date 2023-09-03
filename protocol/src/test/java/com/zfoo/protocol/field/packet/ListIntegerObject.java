package com.zfoo.protocol.field.packet;



import java.util.List;
import java.util.Objects;

public class ListIntegerObject {
    private List<Integer> a;

    private List<Integer> b;

    private List<Integer> c;

    private List<Integer> d;

    private List<Integer> e;

    private List<Integer> f;

    private List<Integer> g;

    private List<Integer> h;

    private List<Integer> i;

    private List<Integer> j;

    public List<Integer> getA() {
        return a;
    }

    public void setA(List<Integer> a) {
        this.a = a;
    }

    public List<Integer> getB() {
        return b;
    }

    public void setB(List<Integer> b) {
        this.b = b;
    }

    public List<Integer> getC() {
        return c;
    }

    public void setC(List<Integer> c) {
        this.c = c;
    }

    public List<Integer> getD() {
        return d;
    }

    public void setD(List<Integer> d) {
        this.d = d;
    }

    public List<Integer> getE() {
        return e;
    }

    public void setE(List<Integer> e) {
        this.e = e;
    }

    public List<Integer> getF() {
        return f;
    }

    public void setF(List<Integer> f) {
        this.f = f;
    }

    public List<Integer> getG() {
        return g;
    }

    public void setG(List<Integer> g) {
        this.g = g;
    }

    public List<Integer> getH() {
        return h;
    }

    public void setH(List<Integer> h) {
        this.h = h;
    }

    public List<Integer> getI() {
        return i;
    }

    public void setI(List<Integer> i) {
        this.i = i;
    }

    public List<Integer> getJ() {
        return j;
    }

    public void setJ(List<Integer> j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListIntegerObject that = (ListIntegerObject) o;
        return Objects.equals(a, that.a) && Objects.equals(b, that.b) && Objects.equals(c, that.c) && Objects.equals(d, that.d) && Objects.equals(e, that.e) && Objects.equals(f, that.f) && Objects.equals(g, that.g) && Objects.equals(h, that.h) && Objects.equals(i, that.i) && Objects.equals(j, that.j);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j);
    }
}
