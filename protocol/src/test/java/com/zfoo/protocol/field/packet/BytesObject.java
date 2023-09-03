package com.zfoo.protocol.field.packet;



import java.util.Arrays;

public class BytesObject {
    private byte[] a;

    private byte[] b;

    private byte[] c;

    private byte[] d;

    private byte[] e;

    private byte[] f;

    private byte[] g;

    private byte[] h;

    private byte[] i;

    private byte[] j;

    public byte[] getA() {
        return a;
    }

    public void setA(byte[] a) {
        this.a = a;
    }

    public byte[] getB() {
        return b;
    }

    public void setB(byte[] b) {
        this.b = b;
    }

    public byte[] getC() {
        return c;
    }

    public void setC(byte[] c) {
        this.c = c;
    }

    public byte[] getD() {
        return d;
    }

    public void setD(byte[] d) {
        this.d = d;
    }

    public byte[] getE() {
        return e;
    }

    public void setE(byte[] e) {
        this.e = e;
    }

    public byte[] getF() {
        return f;
    }

    public void setF(byte[] f) {
        this.f = f;
    }

    public byte[] getG() {
        return g;
    }

    public void setG(byte[] g) {
        this.g = g;
    }

    public byte[] getH() {
        return h;
    }

    public void setH(byte[] h) {
        this.h = h;
    }

    public byte[] getI() {
        return i;
    }

    public void setI(byte[] i) {
        this.i = i;
    }

    public byte[] getJ() {
        return j;
    }

    public void setJ(byte[] j) {
        this.j = j;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BytesObject that = (BytesObject) o;
        return Arrays.equals(a, that.a) && Arrays.equals(b, that.b) && Arrays.equals(c, that.c) && Arrays.equals(d, that.d) && Arrays.equals(e, that.e) && Arrays.equals(f, that.f) && Arrays.equals(g, that.g) && Arrays.equals(h, that.h) && Arrays.equals(i, that.i) && Arrays.equals(j, that.j);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(a);
        result = 31 * result + Arrays.hashCode(b);
        result = 31 * result + Arrays.hashCode(c);
        result = 31 * result + Arrays.hashCode(d);
        result = 31 * result + Arrays.hashCode(e);
        result = 31 * result + Arrays.hashCode(f);
        result = 31 * result + Arrays.hashCode(g);
        result = 31 * result + Arrays.hashCode(h);
        result = 31 * result + Arrays.hashCode(i);
        result = 31 * result + Arrays.hashCode(j);
        return result;
    }
}
