package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.core.io.NumberOutput;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数值.32 位整型
 * <p>
 * 注意和内置类不同的是，IntImpl LongImpl DoubleImpl 三者是层级实现的，避免 JSON 数值类型只有 Number 导致的麻烦。
 * </p>
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface IntValue extends NumericValue, Serializable permits IntImpl {
    int asInt();

    static IntValue zero() {
        return IntImpl.ZERO;
    }

    Value asValue();
}

// IntValue 是 LongValue 的子类型，但具体实现上无需继承关系
final class IntImpl extends NumericImpl implements DoubleValue, LongValue, IntValue {
    private final int value;

    private IntImpl(int value) {
        this.value = value;
    }

    //#region {@link IntNode#valueOf 常量优化}，不要学，正常来说是不推荐用静态代码块的

    private final static int MIN_CANONICAL = -128;
    private final static int MAX_CANONICAL = 127;
    private static final IntImpl[] CANONICALS;

    static {    // 不要学，正常来说是不推荐用静态代码块的
        int count = MAX_CANONICAL - MIN_CANONICAL + 1;
        CANONICALS = new IntImpl[count];
        for (int i = 0; i < count; ++i) {
            CANONICALS[i] = new IntImpl(MIN_CANONICAL + i);
        }
    }

    static Value of(int i) {
        if (i > MAX_CANONICAL || i < MIN_CANONICAL) return new IntImpl(i);
        return CANONICALS[i - MIN_CANONICAL];
    }

    static final IntImpl ZERO = CANONICALS[-MIN_CANONICAL];
    //#endregion

    @Override
    public @NonNull String asText() {
        return NumberOutput.toString(value);
    }

    @Override
    public int asInt() {
        return value;
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
        return value == 0;
    }

    @Override
    public boolean notZero() {
        return value != 0;
    }

    @Override
    public @NonNull Integer asNumber() {
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
                ? this.value == ((IntImpl) obj).value
                : obj instanceof IntValue that
                ? this.value == that.asInt()
                : obj instanceof LongValue that
                ? this.asLong() == that.asLong()
                : equals(value, obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(@NonNull NumericValue o) {
        return o.getClass() == this.getClass()
                ? Integer.compare(value, ((IntImpl) o).value)
                : o instanceof IntValue that
                ? Integer.compare(value, that.asInt())
                : o instanceof LongValue that
                ? Long.compare(value, that.asLong())
                : compareTo(value, o);
    }
}
