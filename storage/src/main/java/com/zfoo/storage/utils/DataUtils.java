package com.zfoo.storage.utils;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface DataUtils {

    //#region Map & Param

    /**
     * 检测 params 是否符合 key-value 格式，且 key 是否均为 String
     *
     * @param params .
     * @throws IllegalArgumentException 不符合格式
     */
    static Object[] requireParamPairs(Object... params) {
        if ((params.length & 1) != 0) throw new IllegalArgumentException();
        for (int i = 0; i < params.length; i += 2) {
            if (params[i] instanceof String) continue;
            throw new IllegalArgumentException();
        }
        return params;
    }

    static ImmutableMap<String, Object> ofMap(@NonNull Object... params) {
        if (params.length == 0) return ImmutableMap.of();
        if ((params.length & 1) != 0) throw new IllegalArgumentException();
        Iterator<Object> it = Stream.of(params).iterator();
        return Stream.generate(() -> Map.entry(String.valueOf(Objects.requireNonNull(it.next())), it.next()))
                .limit(params.length / 2)
                .collect(DataUtils.toImmutableMap());
    }

    static ImmutableMap<String, String> ofMap(@NonNull String... params) {
        return zipToEntries(params.length, Arrays.stream(params).iterator()).collect(toImmutableMap());
    }

    static Stream<Map.Entry<String, String>> zipToEntries(int length, Iterator<String> params) {
        if (length == 0) return Stream.empty();
        if ((length & 1) != 0) throw new IllegalArgumentException();
        return Stream.generate(() -> {
                    String key = Objects.requireNonNull(params.next());
                    String value = params.next();
                    return Map.entry(key, value);
                })
                .limit(length >> 1);
    }

    static <K, V> Collector<Map.Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
        return ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    static <K, V, M extends Map<K, V>> Collector<Map.Entry<K, V>, ?, M> toMap(Supplier<M> mapFactory) {
        return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, throwingMerger(), mapFactory);
    }

    // 从 Java8 源码摘出来的，虽然比较垃圾，但用着方便
    static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }

    static Object[] toArray(Map<?, ?> map) {
        return map.entrySet().stream()
                .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
                .toArray();
    }

    static <E, M extends Map<E, Integer>> Collector<E, ?, M> toCountMap(Supplier<M> mapFactory) {
        return Collectors.toMap(value -> value, value -> 1, Integer::sum, mapFactory);
    }
    //#endregion

    /**
     * 遍历线框上的格子
     *
     * @param nx     数组顶点.最小 x 索引（包含）
     * @param ny     数组顶点.最小 y 索引（包含）
     * @param px     数组顶点.最大 x 索引（包含）
     * @param py     数组顶点.最大 y 索引（包含）
     * @param mapper .
     * @param <T>    .
     * @return .
     */
    static <T> Stream<T> iterRectBox(int nx, int ny, int px, int py, IntBiFunction<T> mapper) {
        // 注意和下面的 iterSpiral 不同，这里 px py 是包含的，故意这么设计的，
        // 因为这里是遍历线框上的格子，下面的 iterSpiral 是遍历矩阵内部的格子。
        Stream<T> left = IntStream.rangeClosed(ny, py).mapToObj(y -> mapper.apply(nx, y));
        Stream<T> right = IntStream.rangeClosed(ny, py).mapToObj(y -> mapper.apply(px, y));
        Stream<T> top = IntStream.range(nx + 1, px).mapToObj(x -> mapper.apply(x, ny));
        Stream<T> bottom = IntStream.range(nx + 1, px).mapToObj(x -> mapper.apply(x, py));
        return Stream.of(left, right, top, bottom).flatMap(UnaryOperator.identity());
    }

    /**
     * 螺线顺序遍历二维数组格子
     *
     * @param nx     数组顶点.最小 x 索引（包含）
     * @param ny     数组顶点.最小 y 索引（包含）
     * @param px     数组顶点.最大 x 索引（不包含）
     * @param py     数组顶点.最大 y 索引（不包含）
     * @param mapper .
     * @param <T>    .
     * @return .
     */
    static <T> Stream<T> iterSpiral(int nx, int ny, int px, int py, IntBiFunction<T> mapper) {
        // TODO 改成螺旋遍历
        return IntStream.range(nx, px).boxed()
                .flatMap(x -> IntStream.range(ny, py)
                        .mapToObj(y -> mapper.apply(x, y)));
    }

    @FunctionalInterface
    public
    interface IntBiFunction<T> {
        T apply(int x, int y);
    }
}
