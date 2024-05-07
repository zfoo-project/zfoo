package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 空值.用于值缺失
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface MissingValue extends Serializable permits MissingImpl {
    static MissingValue missing() {
        return MissingImpl.MISSING;
    }

    Value asValue();
}

sealed interface EmptyValue extends Value permits MissingImpl, NullImpl {
    default String asText(String defaultValue) {
        return defaultValue;
    }

    default int asInt(int defaultValue) {
        return defaultValue;
    }

    default long asLong(long defaultValue) {
        return defaultValue;
    }

    default double asDouble(double defaultValue) {
        return defaultValue;
    }

    default boolean asBoolean(boolean defaultValue) {
        return defaultValue;
    }

    default @NonNull Number asNumber(Number defaultValue) {
        return defaultValue;
    }

    //#region Optional

    @Override
    default Optional<Value> optional() {
        return Optional.empty();
    }

    @Override
    default boolean isPresent() {
        return false;
    }

    @Override
    default void optIfPresent(Consumer<Value> action) {
        Objects.requireNonNull(action);
    }

    @Override
    default Value optMap(@NonNull Function<Value, ?> mapper) {
        Objects.requireNonNull(mapper);
        return this;
    }

    @Override
    default Value optFlatMap(@NonNull Function<Value, Value> mapper) {
        Objects.requireNonNull(mapper);
        return this;
    }
    //#endregion
}

record MissingImpl() implements EmptyValue, MissingValue {
    static final MissingImpl MISSING = new MissingImpl();

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public String toString() {
        return MissingNode.getInstance().toString();    // ""
    }
}
