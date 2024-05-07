package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zfoo.storage.utils.JACKSONUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * JSON 树.用于包装数组、列表等容器类型
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface ArrayValue extends TreeValue<Value> permits ArrayBuilder, ArrayImpl {
    Value path(int index);

    // 注意，因为涉及 JSON 转换，需要多次 contains 时开销会比一般的 list 的高，建议直接操作 stream
    default boolean contains(@NonNull Value value) {
        return stream().anyMatch(value::equals);
    }

    default boolean contains(@NonNull Object value) {
        return contains(Value.of(value));
    }

    // 注意，因为涉及 JSON 转换，需要多次 indexOf 时开销会比一般的 list 的高，建议直接操作 stream
    default int indexOf(@NonNull Value value) {
        Iterator<Value> iterator = stream().iterator();
        for (int index = 0; index < size(); index++) {
            Value next = iterator.next();
            if (next.equals(value)) return index;
        }
        return -1;
    }

    default int indexOf(@NonNull Object value) {
        return indexOf(Value.of(value));
    }

    @Override
    Stream<Value> stream();

    <V> Stream<V> stream(@NonNull Class<V> valueType);

    IntStream intStream();

    @Override
    ArrayBuilder toBuilder();

    static ArrayValue empty() {
        return ArrayImpl.EMPTY;
    }

    default <T> ImmutableList<T> toList(Class<T> tClass) {
        return stream(tClass).collect(ImmutableList.toImmutableList());
    }

    default <T> ImmutableSet<T> toSet(Class<T> tClass) {
        return stream(tClass).collect(ImmutableSet.toImmutableSet());
    }
}

sealed class ArrayImpl<E> implements Value, ArrayValue permits ArrayBuilderImpl {
    final @NonNull List<E> list;

    ArrayImpl(@NonNull List<E> list) {
        this.list = list;
    }

    static final ArrayImpl<?> EMPTY = new ArrayImpl<>(ImmutableList.of());

    static <E> ArrayImpl<E> of(@NonNull List<E> list) {
        //noinspection unchecked
        return list.isEmpty() ? (ArrayImpl<E>) EMPTY : new ArrayImpl<>(list);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public @NonNull Value path(int index) {
        if (index < 0 || index >= list.size()) return MissingImpl.MISSING;
        E value = list.get(index);
        return value instanceof Value v ? v : Value.of(value);
    }

    @Override
    public Stream<Value> stream() {
        return list.stream().map(Value::of);
    }

    @Override
    public <V> Stream<V> stream(@NonNull Class<V> valueType) {
        return list.stream().map(ele -> JACKSONUtils.convert(ele, valueType));
    }

    @Override
    public IntStream intStream() {
        return list.stream().mapToInt(ele -> ele instanceof Number number ? number.intValue()
                : ele instanceof Value value ? value.asInt() : 0);
    }

    @Override
    public ArrayBuilder toBuilder() {
        return new ArrayBuilderImpl(stream()
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    @Override
    public Value asValue() {
        return this;
    }

    @Override
    public String toString() {
        return JACKSONUtils.jsonString(list);
    }

    public @NonNull List<?> list() {
        return list;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        //noinspection unchecked
        var that = (ArrayImpl<Object>) obj;
        return Objects.equals(this.list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }
}
