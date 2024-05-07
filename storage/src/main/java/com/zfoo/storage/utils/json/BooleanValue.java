package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface BooleanValue extends Comparable<BooleanValue>, Serializable permits BooleanImpl {
    String asText();

    boolean asBoolean();

    Value asValue();

    static Value trueValue() {
        return BooleanImpl.TRUE;
    }

    static Value falseValue() {
        return BooleanImpl.FALSE;
    }
}

record BooleanImpl(boolean value) implements Value, BooleanValue {
    static BooleanImpl of(boolean value) {
        return value ? TRUE : FALSE;
    }

    static final BooleanImpl TRUE = new BooleanImpl(true);
    static final BooleanImpl FALSE = new BooleanImpl(false);

    @Override
    public @NonNull String asText() {
        return value ? "true" : "false";
    }

    @Override
    public String asText(String defaultValue) {
        return value ? "true" : "false";
    }

    @Override
    public int asInt() {
        return value ? 1 : 0;
    }

    @Override
    public long asLong() {
        return value ? 1L : 0L;
    }

    @Override
    public double asDouble() {
        return value ? 1.0 : 0.0D;
    }

    @Override
    public @NonNull Number asNumber() {
        return value ? 1 : 0;
    }

    @Override
    public boolean asBoolean() {
        return value;
    }

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public String toString() {
        return JACKSONUtils.jsonString(value);
    }

    @Override
    public int compareTo(@NonNull BooleanValue o) {
        return Boolean.compare(value, o.asBoolean());
    }
}
