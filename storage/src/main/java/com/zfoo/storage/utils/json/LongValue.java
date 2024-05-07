package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.core.io.NumberOutput;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数值.64 位整型
 * <p>
 * 注意和内置类不同的是，IntImpl LongImpl DoubleImpl 三者是层级实现的，避免 JSON 数值类型只有 Number 导致的麻烦。
 * </p>
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface LongValue extends NumericValue, Serializable permits IntImpl, LongImpl {
    long asLong();

    static LongValue zero() {
        return IntImpl.ZERO;
    }

    Value asValue();
}

final class LongImpl extends NumericImpl implements DoubleValue, LongValue {
    private final long value;

    private LongImpl(long value) {
        this.value = value;
    }

    static Value of(long value) {
        return value == 0L ? IntImpl.ZERO : new LongImpl(value);
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
        return value;
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
        return value == 0L;
    }

    @Override
    public boolean notZero() {
        return value != 0L;
    }

    @Override
    public @NonNull Long asNumber() {
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
                ? this.value == ((LongImpl) obj).value
                : obj instanceof LongValue that
                ? this.value == that.asLong()
                : equals(value, obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(@NonNull NumericValue o) {
        return o.getClass() == this.getClass()
                ? Long.compare(value, ((LongImpl) o).value)
                : o instanceof LongValue that
                ? Long.compare(value, that.asLong())
                : compareTo(value, o);
    }
}
