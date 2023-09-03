package com.zfoo.protocol.field.packet;



import java.util.Objects;

public class InnerObjectObject {
    private InnerObject a;

    private InnerObject b;

    private InnerObject c;

    private InnerObject d;

    private InnerObject e;

    private InnerObject f;

    private InnerObject g;

    private InnerObject h;

    private InnerObject i;

    private InnerObject j;

    public InnerObject getA() {
        return a;
    }

    public void setA(InnerObject a) {
        this.a = a;
    }

    public InnerObject getB() {
        return b;
    }

    public void setB(InnerObject b) {
        this.b = b;
    }

    public InnerObject getC() {
        return c;
    }

    public void setC(InnerObject c) {
        this.c = c;
    }

    public InnerObject getD() {
        return d;
    }

    public void setD(InnerObject d) {
        this.d = d;
    }

    public InnerObject getE() {
        return e;
    }

    public void setE(InnerObject e) {
        this.e = e;
    }

    public InnerObject getF() {
        return f;
    }

    public void setF(InnerObject f) {
        this.f = f;
    }

    public InnerObject getG() {
        return g;
    }

    public void setG(InnerObject g) {
        this.g = g;
    }

    public InnerObject getH() {
        return h;
    }

    public void setH(InnerObject h) {
        this.h = h;
    }

    public InnerObject getI() {
        return i;
    }

    public void setI(InnerObject i) {
        this.i = i;
    }

    public InnerObject getJ() {
        return j;
    }

    public void setJ(InnerObject j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InnerObjectObject that = (InnerObjectObject) o;
        return Objects.equals(a, that.a) && Objects.equals(b, that.b) && Objects.equals(c, that.c) && Objects.equals(d, that.d) && Objects.equals(e, that.e) && Objects.equals(f, that.f) && Objects.equals(g, that.g) && Objects.equals(h, that.h) && Objects.equals(i, that.i) && Objects.equals(j, that.j);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j);
    }
}
