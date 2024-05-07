package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.zfoo.storage.utils.JACKSONUtils;
import com.zfoo.storage.utils.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNullElse;

/**
 * JSON 树，用于包装容器类型
 *
 * @param <E> .
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface TreeValue<E> extends Iterable<E>, Serializable permits ArrayValue, ObjectValue,
        TreeValue.Builder {
    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    Stream<E> stream();

    @Override
    default Iterator<E> iterator() {
        return stream().iterator();
    }

    Builder<?, ?> toBuilder();

    Value asValue();

    /**
     * 构建工厂，可变
     *
     * @param <R> 元素引用，对 ArrayValue 而言是 index，对 ObjectValue 而言是 key
     * @param <E> 元素类型，对 ArrayValue 而言是值类型，对 ObjectValue 而言是 entry
     */
    sealed interface Builder<R, E> extends TreeValue<E> permits ArrayBuilder, ObjectBuilder {
        Value path(@NonNull R refer);

        //#region modify

        /**
         * 移除某个索引及对应的值
         *
         * @param refer .
         * @return .
         */
        Builder<R, E> remove(@NonNull R refer);

        /**
         * 清空全部索引和值
         *
         * @return .
         */
        Builder<R, E> clear();

        /**
         * 修改某个引用对应的值
         * mapping 返回 missing 时会移除该引用和其对应的值，否则替换该引用的值。
         * （mapping 返回若为 builder，会拷贝成不可变的 value）
         *
         * @param refer   .
         * @param mapping .
         * @return .
         * @see ArrayBuilder#modify(int, Function)
         * @see ObjectBuilder#modify(String, Function)
         */
        Builder<R, E> modify(R refer, Function<Value/*oldVal*/, Object/*newVal*/> mapping);
        //#endregion

        /**
         * 父节点
         *
         * @return .
         */
        default Builder<?, ?> parent() {
            return marker().parent();
        }

        /**
         * 根节点
         *
         * @return .
         */
        default Builder<?, ?> origin() {
            Builder<?, ?> parent = marker().parent();
            return parent == null ? this : parent.origin();
        }

        default boolean isDirty() {
            return !marker().dirtyRefers().isEmpty();
        }

        /**
         * 已被修改的元素的 index 或 key 的集合，可变
         *
         * @return 注意这里返回的是可变集合，方便业务逻辑在处理完 dirty 数据后顺手清理掉 dirty 标记，不要滥用
         */
        default Map<R, ReferFlag> dirtyRefers() {
            return marker().dirtyRefers();
        }

        /**
         * 所有元素的 index 或 key 的集合，不可变
         *
         * @return 返回 Stream 以避免误修改
         */
        Stream<R> allRefers();

        // 标记工厂，不对外，需要对外的方法已提供了 default 的转发方法
        Marker<R> marker();

        /**
         * 深拷贝然后返回新的不可变的容器，不会影响当前 builder
         *
         * @return .
         * @see #asValue() 跟 asValue 不同在于，asValue 并不会将 TreeBuilder 构建为 TreeValue
         */
        default Value build() {
            return buildTree().asValue();
        }

        /**
         * 深拷贝然后返回新的不可变的容器，不会影响当前 builder
         * （与 {@link #build()} 方法的区别仅在于返回值类型不同）
         *
         * @return .
         * @see #build()
         * @see ArrayBuilder#buildTree()
         * @see ObjectBuilder#buildTree()
         */
        default TreeValue<E> buildTree() {
            // 默认实现，借助 Jackson 序列化再反序列化实现拷贝
            //noinspection unchecked
            return JACKSONUtils.convert(this, TreeValue.class);
        }

        /**
         * 冲洗脏数据，清理脏标记（dirtyRefers）并执行对应回调
         *
         * @param listener .
         */
        default void flush(Listener listener) {
            for (Iterator<Map.Entry<R, ReferFlag>> iterator = dirtyRefers().entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<R, ReferFlag> entry = iterator.next();
                iterator.remove();
                R refer = entry.getKey();
                ReferFlag flag = entry.getValue();
                listener.trigger(refer, flag, path(refer));
            }
        }
    }

    /**
     * 监听脏数据工厂
     *
     * @see Builder#flush(Listener)
     */
    class Listener {
        private static final Logger log = LoggerFactory.getLogger(Listener.class);

        public static final Listener EMPTY = new Listener(ImmutableListMultimap.of(), ImmutableMap.of());

        private final Multimap<Object/*IndexOrKey*/, BiConsumer<ReferFlag, Value>> callbacks;
        private final Map<Object/*IndexOrKey*/, Listener> listeners;

        private Listener(Multimap<Object, BiConsumer<ReferFlag, Value>> callbacks, Map<Object, Listener> listeners) {
            this.callbacks = callbacks;
            this.listeners = listeners;
        }

        public Listener() {
            this(LinkedListMultimap.create(), new HashMap<>());
        }

        public Listener add(int index, BiConsumer<ReferFlag, Value> callback) {
            callbacks.put(index, callback);
            return this;
        }

        public Listener add(String key, BiConsumer<ReferFlag, Value> callback) {
            callbacks.put(key, callback);
            return this;
        }

        public Listener child(int index, Listener listener) {
            return child((Object) index, listener);
        }

        public Listener child(String key, Listener listener) {
            return child((Object) key, listener);
        }

        private Listener child(Object refer, Listener listener) {
            Listener old = listeners.get(refer);
            if (old == null) {
                listeners.put(refer, listener);
            } else {
                old.callbacks.putAll(listener.callbacks);
                for (Map.Entry<Object, Listener> entry : listener.listeners.entrySet()) {
                    old.child(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        private <R> void trigger(R refer, ReferFlag flag, Value value) {
            // 当前 listener 为空单例，直接递归冲洗子树的脏数据
            if (this == Listener.EMPTY) {
                if (value instanceof Builder<?, ?> builder) {
                    builder.flush(Listener.EMPTY);
                }
                return;
            }

            for (BiConsumer<ReferFlag, Value> callback : callbacks.removeAll(refer)) {
                try {
                    callback.accept(flag, value);
                } catch (Exception error) {
                    log.error("TreeValue.flush 脏数据时执行回调出错，refer: {}, value: {}, callback: {}",
                              refer, value, callback, error);
                }
            }

            if (value instanceof Builder<?, ?> builder) {
                if (flag == ReferFlag.DIRTY) {
                    // 当前节点已彻底改变了，比如被移除、被替换或本就是新增的节点，那监听子树没有意义
                    builder.flush(EMPTY);
                } else {
                    Listener listener = listeners.remove(refer);
                    builder.flush(listener == null ? EMPTY : listener);
                }
            }
        }
    }

    /**
     * 数据在容器中的引用的修改标识
     */
    enum ReferFlag {
        // CLEAN,  // 当前引用的值中无任何修改
        MIXED,  // 当前引用的值中有部分修改 // 具体来说，当前引用的是个子树，子树内的某些值被修改了
        DIRTY,  // 当前引用的值已被彻底改变 // 比如直接 set/put 替换当前节点的值，或是删除/新增的值
    }

    static TreeValue<?> empty() {
        return ObjectValue.empty();
    }
}

/**
 * 标记工厂，用于内部数据变动时进行标记，不对外
 */
class Marker<R> {
    static void requireNonParent(Object target) {
        if (!(target instanceof TreeValue.Builder builder)) return;
        TreeValue.Builder<?, ?> parent = builder.parent();
        if (parent == null) return;
        String error = Text.format("目标已具有父节点！target: {}, parent: {}", target, parent);
        throw new IllegalStateException(error);
    }

    static <R> Value linkTreeToParent(TreeValue.Builder<R, ?> linkTo, R refer, Value target) {
        if (target instanceof TreeValue.Builder builder) {
            TreeValue.Builder<?, ?> parent = builder.parent();
            Marker<?> marker = builder.marker();
            if (parent == null || parent == linkTo) {
                marker.linkTo(linkTo, refer);
                return target;
            } else {
                String error = Text.format("目标父节点冲突！target: {}, parent: {}, linkTo: {}",
                                           target, parent, linkTo);
                throw new IllegalStateException(error);
            }
        } else if (target instanceof TreeValue<?> treeValue) {
            TreeValue.Builder<?, ?> builder = treeValue.toBuilder();
            builder.marker().linkTo(linkTo, refer);
            return builder instanceof Value value ? value : builder.build();
        } else {
            return requireNonNullElse(target, NullImpl.NULL);
        }
    }

    static <R> void afterModifyValue(TreeValue.Builder<R, ?> parent, R refer, @Nullable Value oldVal,
                                     @Nullable Value newVal) {
        // 父元素上记录该 refer 已修改
        parent.marker().dirtyOne(refer, TreeValue.ReferFlag.DIRTY);

        if (oldVal == newVal) return;
        if (oldVal instanceof TreeValue.Builder ob) {
            // 将旧元素与父元素解绑
            Marker<?> marker = ob.marker();
            marker.linkTo(null, null);
        }
        if (newVal instanceof TreeValue.Builder<?, ?> nb) {
            // 将新元素绑定到父元素
            Marker<?> marker = nb.marker();
            marker.linkTo(parent, refer);
            // 将整个树标记为已修改
            nb.marker().dirtyTree();
        }
    }

    private TreeValue.Builder<?, ?> parent;           // 当前元素的父元素
    private Object refer;                   // 当前元素的父引用（指其在父元素中的索引或键）
    private final TreeValue.Builder<R, ?> current;    // 当前元素构造工厂
    private final Map<R, TreeValue.ReferFlag> dirtyRefers = new HashMap<>();  // 当前元素内已被修改的引用

    Marker(TreeValue.Builder<R, ?> current) {
        this.current = current;
    }

    TreeValue.Builder<?, ?> parent() {
        return parent;
    }

    <PR> void linkTo(TreeValue.Builder<PR, ?> parent, PR refer) {
        this.parent = parent;
        this.refer = refer;
    }

    public Map<R, TreeValue.ReferFlag> dirtyRefers() {
        return dirtyRefers;
    }

    public void dirtyOne(R refer, TreeValue.ReferFlag flag) {
        dirtyRefers.put(refer, flag);
        modifyParentRefer(TreeValue.ReferFlag.MIXED);
    }

    public void dirtySome(Iterable<R> refers) {
        for (R refer : refers) dirtyRefers.put(refer, TreeValue.ReferFlag.DIRTY);
        modifyParentRefer(TreeValue.ReferFlag.MIXED);
    }

    public void dirtyTree() {   // 这个方法比较特殊，是将当前节点以及其所有递归子节点全部标记为已修改，不要滥用
        current.allRefers().forEach(refer -> {
            TreeValue.ReferFlag flag = dirtyRefers.get(refer);
            if (flag == TreeValue.ReferFlag.DIRTY) return;
            dirtyRefers.put(refer, TreeValue.ReferFlag.DIRTY);
            Value value = current.path(refer);
            if (value instanceof TreeValue.Builder builder) {
                builder.marker().dirtyTree();
            }
        });
        modifyParentRefer(TreeValue.ReferFlag.DIRTY);
    }

    private void modifyParentRefer(TreeValue.ReferFlag flagNew) {
        if (parent == null) return;
        //noinspection unchecked
        Marker<Object> parentMarker = (Marker<Object>) parent.marker();
        Map<Object, TreeValue.ReferFlag> parentDirtyRefers = parentMarker.dirtyRefers();
        TreeValue.ReferFlag flagOld = parentDirtyRefers.get(refer);
        if (flagOld == null || flagOld.ordinal() < flagNew.ordinal()) parentMarker.dirtyOne(refer, flagNew);
    }
}
