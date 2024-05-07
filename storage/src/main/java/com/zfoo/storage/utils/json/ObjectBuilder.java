package com.zfoo.storage.utils.json;

import com.google.common.collect.ImmutableMap;
import com.zfoo.storage.utils.Text;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface ObjectBuilder extends TreeValue.Builder<String, Map.Entry<String, Value>>, ObjectValue permits ObjectBuilderImpl {
    static ObjectBuilder newBuilder() {
        return new ObjectBuilderImpl(new LinkedHashMap<>());
    }

    @Override
    Value path(@NonNull String key);

    //#region modify
    //#region put

    // 参数类型为 Value 的版本，用于对性能要求比较高的场景，
    // 例如可以直接用 add(Value.of(int))，这样可以避免装箱。

    // 参数类型为 Object 则会 Value.of 后重载到 value 版本，
    // 相较于参数为 Value 的版本稍方便些，但有一定性能损失：
    //     1. 如果参数为基本类型数据，会多一次装箱拆箱；
    //     2. 如果参数为 Builder，会自动进行一次深拷贝；

    ObjectBuilder put(@NonNull String key, @NonNull Value value);

    default ObjectBuilder put(@NonNull String key, @NonNull Object value) {
        return put(key, Value.of(value));
    }

    ObjectBuilder putAll(@NonNull ObjectValue objectValue);

    ObjectBuilder putAll(@NonNull Iterable<? extends Map.Entry<String, ?>> iterable);

    default ObjectBuilder putAll(@NonNull Map<String, ?> map) {
        return putAll(map.entrySet());
    }

    //#endregion

    /**
     * 移除某个 key 及对应的值
     *
     * @param key .
     * @return .
     */
    @Override
    ObjectBuilder remove(@NonNull String key);

    /**
     * 清空 map/dict
     *
     * @return .
     */
    ObjectBuilder clear();

    /**
     * 修改某个 key 及其对应的值
     * <p>
     * mapping 返回 missing 时会移除该 key 和其对应的值，否则替换该 key 的值。
     * （mapping 返回若为 builder，会拷贝成不可变的 value）
     * </p>
     *
     * @param key     .
     * @param mapping .
     * @return 注意和 Map 不同，这里为链式调用，返回的 ObjectBuilder 为 this
     */
    @Override
    default ObjectBuilder modify(@NonNull String key, Function<Value/*oldVal*/, Object/*newVal*/> mapping) {
        Value oldVal = path(key);
        Value newVal = Value.of(mapping.apply(oldVal));
        return put(key, newVal);
    }

    /**
     * 将某个 key 对应的值与 value 合并
     * <p>
     * 若 key 不存在，直接 put，否则调用 remapping 方法计算合并后的值，然后 put。
     * （若 key 存在但值为 null，也会调用 remapping，参数 oldVal 传 NullValue）
     * </p>
     *
     * @param key       .
     * @param value     .
     * @param remapping .
     * @param <V>       .
     * @return .
     */
    default <V> ObjectBuilder merge(@NonNull String key, V value, BiFunction<Value/*oldVal*/, V/*value*/, Object/*newVal*/> remapping) {
        Value oldVal = path(key);
        if (oldVal instanceof MissingValue) return put(key, value);
        Object newVal = remapping.apply(oldVal, value);
        return put(key, newVal);
    }
    //#endregion

    /**
     * {@inheritDoc}
     */
    @Override
    default ObjectValue buildTree() {
        // return (ObjectValue) Builder.super.buildTree();
        // 直接递归深拷贝，应该比 convert 快一些吧，还能复用一些不可变 Value
        return ObjectImpl.of(stream().collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                entry -> {
                    Value value = entry.getValue();
                    return value instanceof Builder<?, ?> builder
                            ? builder.buildTree() : value;
                })));
    }
}

final class ObjectBuilderImpl extends ObjectImpl<Value> implements ObjectBuilder {
    private transient final Marker<String> marker;

    ObjectBuilderImpl(LinkedHashMap<String, Value> map) {
        super(map);
        this.marker = new Marker<>(this);
    }

    @Override
    public @NonNull Value path(@NonNull String key) {
        Value value = super.path(key);
        Value linked = Marker.linkTreeToParent(this, key, value);
        if (value != linked) map.put(key, linked);
        return linked;
    }

    @Override
    public Stream<Map.Entry<String, Value>> stream() {
        return map.keySet().stream()
                .map(key -> Map.entry(key, path(key)));
    }

    @Override
    public <V> Stream<Map.Entry<String, V>> stream(@NonNull Class<V> valueType) {
        return map.keySet().stream()
                .map(key -> Map.entry(key, path(key).asType(valueType)));
    }

    @Override
    public ObjectBuilder toBuilder() {
        String error = Text.format("已经是 ObjectBuilder 了，不能再次 toBuilder，this: {}", this);
        throw new UnsupportedOperationException(error);
    }

    @Override
    public Stream<String> allRefers() {
        return map.keySet().stream();
    }

    @Override
    public Marker<String> marker() {
        return marker;
    }

    //#region modify
    @Override
    public ObjectBuilder put(@NonNull String key, @NonNull Value value) {
        if (value instanceof MissingValue) return remove(key);
        Value newVal = Marker.linkTreeToParent(this, key, value);
        Value oldVal = map.put(key, newVal);
        Marker.afterModifyValue(this, key, oldVal, newVal);
        return this;
    }

    @Override
    public ObjectBuilder putAll(@NonNull ObjectValue objectValue) {
        if (objectValue.isEmpty()) return this;
        List<Map.Entry<String, Value>> entries = objectValue.stream()
                .peek(entry -> Marker.requireNonParent(entry.getValue()))
                .toList();
        for (Map.Entry<String, Value> entry : entries) {
            String key = entry.getKey();
            Value newVal = Marker.linkTreeToParent(this, key, entry.getValue());
            Value oldVal = map.put(key, newVal);
            Marker.afterModifyValue(this, key, oldVal, newVal);
        }
        return this;
    }

    @Override
    public ObjectBuilder putAll(@NonNull Iterable<? extends Map.Entry<String, ?>> iterable) {
        List<? extends Map.Entry<String, ?>> entries = StreamSupport.stream(iterable.spliterator(), false)
                .peek(entry -> Marker.requireNonParent(entry.getValue()))
                .toList();
        for (Map.Entry<String, ?> entry : entries) {
            String key = entry.getKey();
            Value newVal = Marker.linkTreeToParent(this, key, Value.of(entry.getValue()));
            if (newVal instanceof MissingValue) {
                remove(key);
            } else {
                Value oldVal = map.put(key, newVal);
                Marker.afterModifyValue(this, key, oldVal, newVal);
            }
        }
        return this;
    }

    @Override
    public ObjectBuilder remove(@NonNull String key) {
        Value oldVal = map.remove(key);
        if (oldVal == null) return this;
        Marker.afterModifyValue(this, key, oldVal, null);
        return this;
    }

    @Override
    public ObjectBuilder clear() {
        if (isEmpty()) return this;
        marker.dirtySome(allRefers()::iterator);
        for (Iterator<Value> iterator = map.values().iterator(); iterator.hasNext(); ) {
            Value value = iterator.next();
            iterator.remove();
            if (value instanceof Builder builder) {
                Marker<?> marker = builder.marker();
                marker.linkTo(null, null);
            }
        }
        return this;
    }
    //#endregion

    @Deprecated
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        return false;
    }

    @Override
    public int hashCode() {
        return marker.hashCode();
    }
}
