package com.zfoo.storage.utils.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.InlineMe;
import com.zfoo.storage.utils.DataUtils;
import com.zfoo.storage.utils.JACKSONUtils;
import com.zfoo.storage.utils.Text;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 集合的节点值，只读
 *
 * @see JsonNode 参考 Jackson 的接口，但避开 "Node" 这个词
 */
@JsonSerialize(using = ValueSerializer.class)
@JsonDeserialize(using = ValueDeserializer.class)
public sealed interface Value extends Serializable permits ArrayImpl, BooleanImpl, EmptyValue, NumericImpl,
        ObjectImpl, TextImpl {
    //#region Jackson 常用接口
    static Value missing() {
        return MissingImpl.MISSING;
    }

    // 不提供 isMissing, isNull，建议用 isPresent 代替，
    // 其他 isXxx 推荐用 Java 14 引入的带模式匹配的 instanceOf 语法，
    // 这样可以避免判断完 is 还要再手动强制类型转换。

    default @NonNull Value path(int index) {
        return MissingImpl.MISSING;
    }

    default @NonNull Value path(@NonNull String key) {
        return MissingImpl.MISSING;
    }

    @Deprecated // 弃用，统一用 asText，和 jackson 保持一致。之所以留着这个是防止有人找不到怎么取字符串
    @InlineMe(replacement = "this.asText()")
    default @NonNull String asString() {
        return asText();
    }

    default @NonNull String asText() {
        return "";
    }

    default String asText(String defaultValue) {
        return asText();
    }

    default int asInt() {
        return 0;
    }

    default int asInt(int defaultValue) {
        return asInt();
    }

    default long asLong() {
        return 0L;
    }

    default long asLong(long defaultValue) {
        return asLong();
    }

    default double asDouble() {
        return 0.0D;
    }

    default double asDouble(double defaultValue) {
        return asDouble();
    }

    default boolean asBoolean() {
        return false;
    }

    default boolean asBoolean(boolean defaultValue) {
        return asBoolean();
    }

    default @NonNull Number asNumber() {
        return 0;
    }

    default @NonNull Number asNumber(Number defaultValue) {
        return asNumber();
    }

    default <V> V asType(Class<V> cls) {
        return JACKSONUtils.convert(this, cls);
    }
    //#endregion

    //#region Optional 常用接口

    default Optional<Value> optional() {
        return Optional.of(this);
    }

    default boolean isPresent() {
        return true;
    }

    default void optIfPresent(Consumer<Value> action) {
        action.accept(this);
    }

    // 加 opt 前缀防止有人误以为是迭代 ArrayValue 的子元素
    default Value optMap(@NonNull Function<Value, ?> mapper) {
        return of(mapper.apply(this));
    }

    default Value optFlatMap(@NonNull Function<Value, Value> mapper) {
        return mapper.apply(this);
    }

    // 不提供 get 和 orElse 方法，建议用 asXxx(defaultValue) 代替。

    static @NonNull Value of(int value) {
        return IntImpl.of(value);
    }

    static @NonNull Value of(long value) {
        return LongImpl.of(value);
    }

    static @NonNull Value of(double value) {
        return DoubleImpl.of(value);
    }

    static @NonNull Value of(boolean value) {
        return BooleanImpl.of(value);
    }

    static @NonNull Value of(@NonNull List<?> list) {
        return ArrayImpl.of(list);
    }

    // 当 map 的 key 确定为 String 时，用这个重载可以避免校验 key 是否均为 String
    static @NonNull Value of(@NonNull Map<String, ?> map) {
        return ObjectImpl.of(map);
    }

    /**
     * 构造 Value
     *
     * @param value .
     * @return .
     */
    static @NonNull Value of(@Nullable Object value) {
        if (value == null) return NullImpl.NULL;
        if (value instanceof TreeValue.Builder builder) return builder.build();
        if (value instanceof Value val) return val;
        if (value instanceof TreeNode node) return ValueDeserializer.of(node);
        if (value instanceof Optional opt) return opt.isPresent() ? of(opt.get()) : MissingImpl.MISSING;

        if (value instanceof List<?> list) {
            return ArrayImpl.of(list);
        }
        if (value instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) {
                if (!(key instanceof String)) {
                    String error = Text.format("JSONObject 的 key 必须是 String 类型, map: {}, invalidKey: {}",
                                               map,
                                               key);
                    throw new IllegalArgumentException(error);
                }
            }
            //noinspection unchecked
            return ObjectImpl.of((Map<String, ?>) map);
        }
        if (value instanceof Iterable<?> iter) {
            return ArrayImpl.of(ImmutableList.copyOf(iter));
        }
        if (value instanceof Iterator<?> iter) {
            return ArrayImpl.of(ImmutableList.copyOf(iter));
        }
        if (value instanceof Spliterator<?> spliterator) {
            return ArrayImpl.of(StreamSupport.stream(spliterator, false).collect(ImmutableList.toImmutableList()));
        }
        if (value instanceof BaseStream<?, ?> stream) {
            return ArrayImpl.of(ImmutableList.copyOf(stream.iterator()));
        }
        if (value instanceof Object[] array) {
            return ArrayImpl.of(Arrays.asList(array));
        }

        if (value instanceof CharSequence s) return TextImpl.of(s.toString());
        if (value instanceof Integer i) return IntImpl.of(i);
        if (value instanceof Long l) return LongImpl.of(l);
        if (value instanceof Double d) return DoubleImpl.of(d);
        if (value instanceof Number n) return NumberImpl.of(n);
        if (value instanceof Boolean b) return BooleanImpl.of(b);
        else return JACKSONUtils.convert(value, Value.class);
    }
    //#endregion
}

class ValueSerializer extends StdSerializer<Value> {
    @/*无参构造，序列化用*/Deprecated
    public ValueSerializer() {
        this(Value.class);
    }

    public ValueSerializer(Class<Value> t) {
        super(t);
    }

    @Override
    public void serialize(Value value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value instanceof NullValue) {
            provider.defaultSerializeNull(gen); // "null"
        } else if (value instanceof MissingValue) {
            // 序列化时，MissingNode 实际上还是当作 null 处理了，结果仍然为 "null"
            provider.defaultSerializeValue(MissingNode.getInstance(), gen); // "null"
        } else if (value instanceof ArrayImpl<?> impl) {
            provider.defaultSerializeValue(impl.list(), gen);
        } else if (value instanceof ObjectImpl<?> impl) {
            provider.defaultSerializeValue(impl.map(), gen);
        } else if (value instanceof TextValue impl) {
            provider.defaultSerializeValue(impl.asText(), gen);
        } else if (value instanceof IntValue impl) {
            provider.defaultSerializeValue(impl.asInt(), gen);
        } else if (value instanceof LongValue impl) {
            provider.defaultSerializeValue(impl.asLong(), gen);
        } else if (value instanceof DoubleValue impl) {
            provider.defaultSerializeValue(impl.asDouble(), gen);
        } else if (value instanceof NumericValue impl) {
            provider.defaultSerializeValue(impl.asNumber(), gen);
        } else if (value instanceof BooleanValue impl) {
            provider.defaultSerializeValue(impl.asBoolean(), gen);
        } else if (value instanceof ArrayValue impl) {  // 万一有其他 ArrayValue 的实现，保底处理
            provider.defaultSerializeValue(impl.stream().toList(), gen);
        } else if (value instanceof ObjectValue impl) { // 万一有其他 ObjectValue 的实现，保底处理
            Map<String, Value> map = impl.stream().collect(DataUtils.toImmutableMap());
            provider.defaultSerializeValue(map, gen);
        } else {    // 万一有其他 Value 的实现，保底处理
            gen.writeRawValue(value.toString());
        }
    }
}

class ValueDeserializer extends StdDeserializer<Value> {
    public ValueDeserializer() {
        this(Value.class);
    }

    public ValueDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Value deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        return of(ctx.readTree(p));
    }

    @Override
    public Value getNullValue(DeserializationContext ctx) {
        return NullImpl.NULL;
    }

    static @NonNull Value of(TreeNode node) {
        if (node == null || node instanceof NullNode) return NullImpl.NULL;
        if (node instanceof MissingNode) return MissingImpl.MISSING;
        if (node instanceof ArrayNode values) {
            ImmutableList<Value> list = StreamSupport.stream(values.spliterator(), false)
                    .map(ValueDeserializer::of)
                    .collect(ImmutableList.toImmutableList());
            return Value.of(list);
        }
        if (node instanceof ObjectNode values) {
            Iterator<String> fieldNames = values.fieldNames();
            Map<String, Value> map = Stream.generate(fieldNames::next)
                    .limit(values.size())
                    .collect(ImmutableMap.toImmutableMap(
                            UnaryOperator.identity(),
                            key -> ValueDeserializer.of(values.get(key))
                    ));
            return Value.of(map);
        }
        if (node instanceof TextNode value) return Value.of(value.asText());
        if (node instanceof IntNode value) return Value.of(value.asInt());
        if (node instanceof LongNode value) return Value.of(value.asLong());
        if (node instanceof DoubleNode value) return Value.of(value.asDouble());
        if (node instanceof NumericNode value) return Value.of(value.numberValue());
        if (node instanceof BooleanNode value) return Value.of(value.asBoolean());
        // if (node instanceof POJONode value) return Value.of(value.getPojo());
        throw new IllegalArgumentException(Text.format("JsonValue.不支持的类型，node: {}", node));
    }
}
