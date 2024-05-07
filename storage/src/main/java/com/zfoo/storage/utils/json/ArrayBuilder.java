package com.zfoo.storage.utils.json;

import com.zfoo.storage.utils.Text;
import org.apache.curator.shaded.com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface ArrayBuilder extends TreeValue.Builder<Integer, Value>, ArrayValue permits ArrayBuilderImpl {
    Logger log = LoggerFactory.getLogger(ArrayBuilder.class);

    static ArrayBuilder newBuilder() {
        return new ArrayBuilderImpl(new ArrayList<>());
    }

    @Override
    default Value path(@NonNull Integer refer) {
        return path(refer.intValue());
    }

    //#region modify
    //#region set & add

    // 参数类型为 Value 的版本，用于对性能要求比较高的场景，
    // 例如可以直接用 add(Value.of(int))，这样可以避免装箱。

    // 参数类型为 Object 则会 Value.of 后重载到 value 版本，
    // 相较于参数为 Value 的版本稍方便些，但有一定性能损失：
    //     1. 如果参数为基本类型数据，会多一次装箱拆箱；
    //     2. 如果参数为 Builder，会自动进行一次深拷贝；

    ArrayBuilder set(int index, @NonNull Value value);

    default ArrayBuilder set(int index, @NonNull Object value) {
        return set(index, Value.of(value));
    }

    ArrayBuilder add(int index, @NonNull Value value);

    default ArrayBuilder add(int index, @NonNull Object value) {
        return add(index, Value.of(value));
    }

    ArrayBuilder addAll(int index, @NonNull ArrayValue arrayValue);

    ArrayBuilder addAll(int index, @NonNull Iterable<?> iterable);

    default ArrayBuilder addAll(int index, @NonNull Object[] array) {
        return addAll(index, Arrays.stream(array)::iterator);
    }

    default ArrayBuilder add(@NonNull Value value) {
        return add(size(), value);
    }

    default ArrayBuilder add(@NonNull Object value) {
        return add(size(), value);
    }

    default ArrayBuilder addAll(@NonNull ArrayValue arrayValue) {
        return addAll(size(), arrayValue);
    }

    default ArrayBuilder addAll(@NonNull Iterable<?> iterable) {
        return addAll(size(), iterable);
    }

    default ArrayBuilder addAll(@NonNull Object[] array) {
        return addAll(size(), array);
    }

    //#endregion

    /**
     * 移除某个位置上的值
     *
     * @param index .
     * @return .
     */
    ArrayBuilder remove(int index);

    /**
     * 移除某个位置上的值
     *
     * @param index .
     * @return .
     */
    @Override
    default ArrayBuilder remove(@NonNull Integer index) {
        return remove(index.intValue());
    }

    /**
     * 移除指定的值
     *
     * @param value .
     * @return .
     */
    default ArrayBuilder removeItem(@NonNull Object value) {
        return remove(indexOf(value));
    }

    /**
     * 移除指定的值
     *
     * @param value .
     * @return .
     */
    default ArrayBuilder removeItem(@NonNull Value value) {
        return remove(indexOf(value));
    }

    /**
     * 清空 list/array
     *
     * @return .
     */
    @Override
    ArrayBuilder clear();

    /**
     * 修改某个 index 位置上的值
     * <p>
     * mapping 返回 missing 时会移除对应位置元素，否则替换该位置的元素。
     * （mapping 返回若为 builder，会拷贝成不可变的 value）
     * （若 index 已越界，且 mapping 返回不为 missingValue，则抛出异常）
     * </p>
     *
     * @param index   .
     * @param mapping .
     * @return .
     * @throws IndexOutOfBoundsException 若 index 不存在且 mapping 返回不为 missing
     */
    default ArrayBuilder modify(int index, Function<Value/*oldVal*/, Object/*newVal*/> mapping) {
        Value oldVal = path(index);
        Value newVal = Value.of(mapping.apply(oldVal));
        return set(index, newVal);
    }

    @Override
    default ArrayBuilder modify(Integer refer, Function<Value, Object> mapping) {
        return modify(refer.intValue(), mapping);
    }
    //#endregion

    /**
     * {@inheritDoc}
     */
    @Override
    default ArrayValue buildTree() {
        // return (ArrayValue) Builder.super.buildTree();
        // 直接递归深拷贝，应该比 convert 快一些吧，还能复用一些不可变 Value
        return ArrayImpl.of(stream()
                                    .map(value -> value instanceof Builder<?, ?> builder
                                            ? builder.buildTree() : value)
                                    .collect(ImmutableList.toImmutableList()));
    }
}

final class ArrayBuilderImpl extends ArrayImpl<Value> implements ArrayBuilder {
    private transient final Marker<Integer> marker;

    ArrayBuilderImpl(ArrayList<Value> list) {
        super(list);
        this.marker = new Marker<>(this);
    }

    @Override
    public @NonNull Value path(int index) {
        Value value = super.path(index);
        Value linked = Marker.linkTreeToParent(this, index, value);
        if (value != linked) list.set(index, linked);
        return linked;
    }

    @Override
    public Stream<Value> stream() {
        return IntStream.range(0, list.size())
                .mapToObj(this::path);
    }

    @Override
    public <V> Stream<V> stream(@NonNull Class<V> valueType) {
        return stream().map(value -> value.asType(valueType));
    }

    @Override
    public ArrayBuilder toBuilder() {
        String error = Text.format("已经是 ArrayBuilder 了，不能再次 toBuilder，this: {}", this);
        throw new UnsupportedOperationException(error);
    }

    @Override
    public Stream<Integer> allRefers() {
        return IntStream.range(0, list.size()).boxed();
    }

    @Override
    public Marker<Integer> marker() {
        return marker;
    }

    //#region modify
    @Override
    public ArrayBuilder set(int index, @NonNull Value value) {
        if (value instanceof MissingValue) return remove(index);
        if (index < 0 || index >= size()) {
            throwIndexOutOfBoundsException(index, value);
        }
        Value newVal = Marker.linkTreeToParent(this, index, value);
        Value oldVal = list.set(index, newVal);
        Marker.afterModifyValue(this, index, oldVal, newVal);
        return this;
    }

    @Override
    public ArrayBuilder add(int index, @NonNull Value value) {
        if (value instanceof MissingValue) return this;
        if (index < 0 || index > size()) {
            throwIndexOutOfBoundsException(index, value);
        }
        Value newVal = Marker.linkTreeToParent(this, index, value);
        list.add(index, newVal);
        Marker.afterModifyValue(this, index, null, newVal);
        if (index == 0) marker.dirtyTree(); // 标记整个树脏了
        else marker.dirtySome(IntStream.range(1 + index, size())::iterator);
        return this;
    }

    @Override
    public ArrayBuilder addAll(int index, @NonNull ArrayValue arrayValue) {
        if (arrayValue.isEmpty()) return this;
        if (index < 0 || index > size()) {
            throwIndexOutOfBoundsException(index, arrayValue);
        }
        List<Value> values = arrayValue.stream()
                .peek(Marker::requireNonParent)
                .toList();
        for (int i = 0, valuesSize = values.size(); i < valuesSize; i++, index++) {
            Value value = values.get(i);
            Value newVal = Marker.linkTreeToParent(this, index, value);
            list.add(index, newVal);
            Marker.afterModifyValue(this, index, null, newVal);
        }
        if (index == values.size()) marker.dirtyTree(); // 标记整个树脏了
        else marker.dirtySome(IntStream.range(index, size())::iterator);
        return this;
    }

    @Override
    public ArrayBuilder addAll(int index, @NonNull Iterable<?> iterable) {
        if (index < 0 || index > size()) {
            throwIndexOutOfBoundsException(index, iterable);
        }
        List<?> values = StreamSupport.stream(iterable.spliterator(), false)
                .peek(Marker::requireNonParent)
                .toList();
        for (int i = 0, valuesSize = values.size(); i < valuesSize; i++, index++) {
            Object value = values.get(i);
            Value newVal = Marker.linkTreeToParent(this, index, Value.of(value));
            if (newVal instanceof MissingValue) {
                index--;
            } else {
                list.add(index, newVal);
                Marker.afterModifyValue(this, index, null, newVal);
            }
        }
        if (index == values.size()) marker.dirtyTree(); // 标记整个树脏了
        else marker.dirtySome(IntStream.range(index, size())::iterator);
        return this;
    }

    private void throwIndexOutOfBoundsException(int index, @NonNull Object value) {
        throw new IndexOutOfBoundsException(Text.format(
                "ArrayBuilder.索引越界，index: {}, value: {}, array: {}",
                index, value, this));
    }

    @Override
    public ArrayBuilder remove(int index) {
        if (index < 0 || index >= size()) return this;
        if (index == 0) marker.dirtyTree(); // 标记整个树脏了
        else marker.dirtySome(IntStream.range(index, size())::iterator);
        Value oldVal = list.remove(index);
        Marker.afterModifyValue(this, index, oldVal, null);
        return this;
    }

    @Override
    public ArrayBuilder clear() {
        if (isEmpty()) return this;
        marker.dirtySome(allRefers()::iterator);
        for (Iterator<Value> iterator = list.iterator(); iterator.hasNext(); ) {
            Value value = iterator.next();
            iterator.remove();
            if (value instanceof Builder builder) {
                Marker<?> marker = builder.marker();
                marker.linkTo(null, null);
            }
        }
        marker.dirtyTree(); // 标记整个树脏了
        return this;
    }
    //#endregion

    @Deprecated
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        log.warn("可变容器用 equals 比较没有意义，建议直接按引用比较", new Throwable());
        return false;
    }

    @Override
    public int hashCode() {
        return marker.hashCode();
    }
}
