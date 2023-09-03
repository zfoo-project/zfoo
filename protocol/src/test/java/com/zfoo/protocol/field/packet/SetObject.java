package com.zfoo.protocol.field.packet;



import java.util.Objects;
import java.util.Set;

public class SetObject {
    private Set<Integer> a;

    private Set<Integer> b;

    private Set<Integer> c;

    private Set<Integer> d;

    private Set<Integer> e;

    private Set<Integer> f;

    private Set<Integer> g;

    private Set<Integer> h;

    private Set<Integer> i;

    private Set<Integer> j;

    public Set<Integer> getA() {
        return a;
    }

    public void setA(Set<Integer> a) {
        this.a = a;
    }

    public Set<Integer> getB() {
        return b;
    }

    public void setB(Set<Integer> b) {
        this.b = b;
    }

    public Set<Integer> getC() {
        return c;
    }

    public void setC(Set<Integer> c) {
        this.c = c;
    }

    public Set<Integer> getD() {
        return d;
    }

    public void setD(Set<Integer> d) {
        this.d = d;
    }

    public Set<Integer> getE() {
        return e;
    }

    public void setE(Set<Integer> e) {
        this.e = e;
    }

    public Set<Integer> getF() {
        return f;
    }

    public void setF(Set<Integer> f) {
        this.f = f;
    }

    public Set<Integer> getG() {
        return g;
    }

    public void setG(Set<Integer> g) {
        this.g = g;
    }

    public Set<Integer> getH() {
        return h;
    }

    public void setH(Set<Integer> h) {
        this.h = h;
    }

    public Set<Integer> getI() {
        return i;
    }

    public void setI(Set<Integer> i) {
        this.i = i;
    }

    public Set<Integer> getJ() {
        return j;
    }

    public void setJ(Set<Integer> j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetObject setObject = (SetObject) o;
        return Objects.equals(a, setObject.a) && Objects.equals(b, setObject.b) && Objects.equals(c, setObject.c) && Objects.equals(d, setObject.d) && Objects.equals(e, setObject.e) && Objects.equals(f, setObject.f) && Objects.equals(g, setObject.g) && Objects.equals(h, setObject.h) && Objects.equals(i, setObject.i) && Objects.equals(j, setObject.j);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j);
    }
}
