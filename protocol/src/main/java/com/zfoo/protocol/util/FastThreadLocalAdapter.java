package com.zfoo.protocol.util;

import io.netty.util.concurrent.FastThreadLocal;

import java.util.function.Supplier;

/**
 * @author godotg
 */
public class FastThreadLocalAdapter<T> {

    private FastThreadLocal<T> fastThreadLocal;

    private Supplier<T> supplier;

    public FastThreadLocalAdapter() {
        this.fastThreadLocal = new FastThreadLocal<>();
    }

    public FastThreadLocalAdapter(Supplier<T> supplier) {
        this.supplier = supplier;
        this.fastThreadLocal = new FastThreadLocal<>() {
            @Override
            protected T initialValue() {
                return supplier.get();
            }
        };
    }

    public T get() {
        // return Thread.currentThread().isVirtual() ? supplier.get() : fastThreadLocal.get();
        return fastThreadLocal.get();
    }

    public void set(T value) {
        // if (Thread.currentThread().isVirtual()) return;
        fastThreadLocal.set(value);
    }

}
