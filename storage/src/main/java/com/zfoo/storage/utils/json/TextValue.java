package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

/**
 * JSON 文本，包装字符串
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface TextValue extends Comparable<TextValue>, Serializable permits TextImpl {
    String asText();

    static TextValue empty() {
        return TextImpl.EMPTY;
    }

    Value asValue();
}

record TextImpl(@NonNull String value) implements Value, TextValue {
    static TextImpl EMPTY = new TextImpl("");

    static TextImpl of(@NonNull String value) {
        return value.isEmpty() ? EMPTY : new TextImpl(value);
    }

    @Override
    public @NonNull String asText() {
        return value;
    }

    @Override
    public String asText(String defaultValue) {
        return value;
    }

    @Override
    public int asInt() {
        return NumberInput.parseAsInt(value, 0);
    }

    @Override
    public int asInt(int defaultValue) {
        return NumberInput.parseAsInt(value, defaultValue);
    }

    @Override
    public long asLong() {
        return NumberInput.parseAsLong(value, 0L);
    }

    @Override
    public long asLong(long defaultValue) {
        return NumberInput.parseAsLong(value, defaultValue);
    }

    @Override
    public double asDouble() {
        return NumberInput.parseAsDouble(value, 0.0D);
    }

    @Override
    public double asDouble(double defaultValue) {
        return NumberInput.parseAsDouble(value, defaultValue);
    }

    @Override
    public boolean asBoolean() {
        return asBoolean(false);
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        String v = value.trim();
        if ("true".equalsIgnoreCase(v)) {
            return true;
        }
        if ("false".equalsIgnoreCase(v)) {
            return false;
        }
        return defaultValue;
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
    public int compareTo(@NonNull TextValue o) {
        return value.compareTo(o.asText());
    }
}
