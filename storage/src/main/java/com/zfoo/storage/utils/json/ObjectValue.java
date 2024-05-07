package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import com.zfoo.storage.utils.DataUtils;
import com.zfoo.storage.utils.JACKSONUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * JSON 树.用于包装字典、映射等容器类型
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface ObjectValue extends TreeValue<Map.Entry<String, Value>> permits ObjectBuilder, ObjectImpl {
    Value path(@NonNull String key);

    default boolean containsKey(@NonNull String key) {
        return path(key).isPresent();
    }

    // 注意，因为涉及 JSON 转换，需要多次 containsValue 时开销会比一般的 map 的高，建议直接操作 stream
    default boolean containsValue(@NonNull Value value) {
        return values().stream().anyMatch(value::equals);
    }

    default boolean containsValue(@NonNull Object value) {
        return containsValue(Value.of(value));
    }

    @Override
    Stream<Map.Entry<String, Value>> stream();

    <V> Stream<Map.Entry<String, V>> stream(@NonNull Class<V> valueType);

    <V> Map<String, V> map();

    default void forEach(BiConsumer<String, Value> action) {
        forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    ArrayValue keys();

    ArrayValue values();

    @Override
    ObjectBuilder toBuilder();

    static ObjectValue empty() {
        return ObjectImpl.EMPTY;
    }
}

sealed class ObjectImpl<E> implements Value, ObjectValue permits ObjectBuilderImpl {
    final @NonNull Map<String, E> map;

    ObjectImpl(@NonNull Map<String, E> map) {
        this.map = map;
    }

    static final ObjectImpl<?> EMPTY = new ObjectImpl<>(ImmutableMap.of());

    static <E> ObjectImpl<E> of(@NonNull Map<String, E> map) {
        //noinspection unchecked
        return map.isEmpty() ? (ObjectImpl<E>) EMPTY : new ObjectImpl<>(map);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public @NonNull Value path(@NonNull String key) {
        if (!map.containsKey(key)) return MissingImpl.MISSING;
        E value = map.get(key);
        return value instanceof Value v ? v : Value.of(value);
    }

    private static final Function<Map.Entry<String, ?>, Map.Entry<String, Value>> convertToValue
            = entry -> Map.entry(entry.getKey(), Value.of(entry.getValue()));

    @Override
    public Stream<Map.Entry<String, Value>> stream() {
        return map.entrySet().stream().map(convertToValue);
    }

    @Override
    public <V> Stream<Map.Entry<String, V>> stream(@NonNull Class<V> valueType) {
        return map.entrySet().stream()
                .map(entry -> {
                    V value = Value.of(entry.getValue()).asType(valueType);
                    return Map.entry(entry.getKey(), value);
                });
    }

    @Override
    public ArrayValue keys() {
        return (ArrayValue) Value.of(map.keySet());
    }

    @Override
    public ArrayValue values() {
        return (ArrayValue) Value.of(map.values());
    }

    @Override
    public ObjectBuilder toBuilder() {
        return new ObjectBuilderImpl(stream().collect(DataUtils.toMap(LinkedHashMap::new)));
    }

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public String toString() {
        return JACKSONUtils.jsonString(map);
    }

    public @NonNull Map<String, ?> map() {
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        //noinspection unchecked
        var that = (ObjectImpl<Object>) obj;
        return Objects.equals(this.map, that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
