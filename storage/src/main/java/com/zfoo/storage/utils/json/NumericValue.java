package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数值
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface NumericValue extends Comparable<NumericValue>, Serializable permits DoubleValue, IntValue, LongValue, NumericImpl {
    String asText();

    int asInt();

    long asLong();

    double asDouble();

    boolean isZero();

    boolean notZero();

    default boolean isPositive() {
        return zero().compareTo(this) < 0;
    }

    default boolean isNegative() {
        return this.compareTo(zero()) < 0;
    }

    Number asNumber();

    @Override
    String toString();

    boolean equals(Object object);

    int hashCode();

    @Override
    int compareTo(@NonNull NumericValue o);

    Value asValue();

    static IntValue zero() {
        return IntImpl.ZERO;
    }
}

abstract sealed class NumericImpl implements Value, NumericValue permits DoubleImpl, IntImpl, LongImpl, NumberImpl {
    public abstract @NonNull String asText();

    public abstract int asInt();

    public abstract long asLong();

    public abstract double asDouble();

    @Deprecated // 弃用，既然已知 Value 为 Numeric，请务必改用语义更明确的 isZero、notZero、isPositive、isNegative 等方法
    @Override
    public boolean asBoolean() {
        return notZero();
    }

    public abstract @NonNull Number asNumber();

    static boolean equals(double value, @NonNull Object obj) {
        return obj instanceof NumericValue that
                && Double.valueOf(value).equals(that.asDouble());
    }

    static int compareTo(double value, @NonNull NumericValue o) {
        return Double.compare(value, o.asDouble());
    }
}

final class NumberImpl extends NumericImpl {
    private final Number value;

    private NumberImpl(Number value) {
        this.value = value;
    }

    static NumberImpl of(Number value) {
        return new NumberImpl(value);
    }

    @Override
    public @NonNull String asText() {
        return value.toString();
    }

    @Override
    public int asInt() {
        return value.intValue();
    }

    @Override
    public long asLong() {
        return value.longValue();
    }

    @Override
    public double asDouble() {
        return value.doubleValue();
    }

    @Override
    public boolean isZero() {
        return Double.valueOf(value.doubleValue()).equals(0.0D);
    }

    @Override
    public boolean notZero() {
        return !Double.valueOf(value.doubleValue()).equals(0.0D);
    }

    @Override
    public @NonNull Number asNumber() {
        return value;
    }

    @Override
    public String toString() {
        return JACKSONUtils.jsonString(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        return obj.getClass() == this.getClass()
                ? Objects.equals(this.value, ((NumberImpl) obj).value)
                : equals(value.doubleValue(), obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(@NonNull NumericValue o) {
        return o.getClass() == this.getClass()
                ? Double.compare(value.doubleValue(), ((NumberImpl) o).value.doubleValue())
                : compareTo(value.doubleValue(), o);
    }

    @Override
    public Value asValue() {
        return this;
    }
}
