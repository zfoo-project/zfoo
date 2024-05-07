package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.core.io.NumberOutput;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数值.双精度浮点
 * <p>
 * 注意和内置类不同的是，IntImpl LongImpl DoubleImpl 三者是层级实现的，避免 JSON 数值类型只有 Number 导致的麻烦。
 * </p>
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface DoubleValue extends NumericValue, Serializable permits DoubleImpl, IntImpl, LongImpl {
    double asDouble();

    static DoubleValue zero() {
        return IntImpl.ZERO;
    }

    Value asValue();
}

final class DoubleImpl extends NumericImpl implements DoubleValue {
    private final double value;

    private DoubleImpl(double value) {
        this.value = value;
    }

    static Value of(double value) {
        return value == 0.0D ? IntImpl.ZERO : new DoubleImpl(value);
    }

    @Override
    public @NonNull String asText() {
        return NumberOutput.toString(value);
    }

    @Override
    public int asInt() {
        return (int) value;
    }

    @Override
    public long asLong() {
        return (long) value;
    }

    @Override
    public double asDouble() {
        return value;
    }

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public boolean isZero() {
        return Double.valueOf(value).equals(0.0D);
    }

    @Override
    public boolean notZero() {
        return !Double.valueOf(value).equals(0.0D);
    }

    @Override
    public @NonNull Double asNumber() {
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
                ? Double.valueOf(this.value).equals(((DoubleImpl) obj).value)
                : equals(value, obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(@NonNull NumericValue o) {
        return o.getClass() == this.getClass()
                ? Double.compare(value, ((DoubleImpl) o).value)
                : compareTo(value, o);
    }
}
