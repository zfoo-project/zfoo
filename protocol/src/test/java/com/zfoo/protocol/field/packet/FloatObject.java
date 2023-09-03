package com.zfoo.protocol.field.packet;



import java.util.Objects;

public class FloatObject {
    private float a;

    private float b;

    private float c;

    private float d;

    private float e;

    private float f;

    private float g;

    private float h;

    private float i;

    private float j;

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getC() {
        return c;
    }

    public void setC(float c) {
        this.c = c;
    }

    public float getD() {
        return d;
    }

    public void setD(float d) {
        this.d = d;
    }

    public float getE() {
        return e;
    }

    public void setE(float e) {
        this.e = e;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getI() {
        return i;
    }

    public void setI(float i) {
        this.i = i;
    }

    public float getJ() {
        return j;
    }

    public void setJ(float j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatObject that = (FloatObject) o;
        return Float.compare(that.a, a) == 0 && Float.compare(that.b, b) == 0 && Float.compare(that.c, c) == 0 && Float.compare(that.d, d) == 0 && Float.compare(that.e, e) == 0 && Float.compare(that.f, f) == 0 && Float.compare(that.g, g) == 0 && Float.compare(that.h, h) == 0 && Float.compare(that.i, i) == 0 && Float.compare(that.j, j) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f, g, h, i, j);
    }
}
