package com.zfoo.scheduler.util;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.AssertionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

/**
 * lazy detect the expiration time
 *
 * @author godotg
 */
public class LazyCache<K, V> {

    private static final float DEFAULT_BACK_PRESSURE_FACTOR = 0.13f;

    public static class Cache<K, V> {
        public K k;
        public V v;
        public volatile long expireTime;
    }

    public static enum RemovalCause {
        /**
         * The entry was manually removed by the user. This can result from the user invoking any of the
         * following methods on the cache or map view.
         * remove()
         */
        EXPLICIT,

        /**
         * The entry itself was not actually removed, but its value was replaced by the user. This can
         * result from the user invoking any of the following methods on the cache or map view.
         * put()
         */
        REPLACED,


        /**
         * The entry's expiration timestamp has passed.
         */
        EXPIRED,

        /**
         * The entry was evicted due to size constraints.
         */
        SIZE;
    }

    private int maximumSize;
    private int backPressureSize;
    private long expireAfterAccessMillis;
    private long expireCheckIntervalMillis;
    private volatile long minExpireTime;
    private AtomicLong expireCheckTimeAtomic;
    private ConcurrentMap<K, Cache<K, V>> cacheMap;
    private BiConsumer<List<Cache<K, V>>, RemovalCause> removeListener = (removes, removalCause) -> {
    };
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public LazyCache(int maximumSize, long expireAfterAccessMillis, long expireCheckIntervalMillis, BiConsumer<List<Cache<K, V>>, RemovalCause> removeListener) {
        AssertionUtils.ge1(maximumSize);
        AssertionUtils.ge0(expireAfterAccessMillis);
        AssertionUtils.ge0(expireCheckIntervalMillis);
        this.maximumSize = maximumSize;
        this.backPressureSize = Math.max(maximumSize, maximumSize + (int) (maximumSize * DEFAULT_BACK_PRESSURE_FACTOR));
        this.expireAfterAccessMillis = expireAfterAccessMillis;
        this.expireCheckIntervalMillis = Math.max(1, expireCheckIntervalMillis);
        this.minExpireTime = TimeUtils.now();
        this.expireCheckTimeAtomic = new AtomicLong(TimeUtils.now());
        this.cacheMap = new ConcurrentHashMap<>(Math.max(maximumSize / 16, 512));
        if (removeListener != null) {
            this.removeListener = removeListener;
        }
    }

    /**
     * If the cache previously contained a value associated with the key, the old value is replaced by the new value.
     */
    public void put(K key, V value) {
        checkExpire();

        var cache = new Cache<K, V>();
        cache.k = key;
        cache.v = value;
        cache.expireTime = TimeUtils.now() + expireAfterAccessMillis;
        var oldCache = cacheMap.put(key, cache);
        if (oldCache != null) {
            removeListener.accept(List.of(oldCache), RemovalCause.REPLACED);
        }

        checkMaximumSize();
    }

    public V get(K key) {
        checkExpire();

        var cache = cacheMap.get(key);
        if (cache == null) {
            return null;
        }
        if (cache.expireTime < TimeUtils.now()) {
            removeForCause(key, RemovalCause.EXPIRED);
            return null;
        }
        cache.expireTime = TimeUtils.now() + expireAfterAccessMillis;
        return cache.v;
    }


    public void remove(K key) {
        checkExpire();

        removeForCause(key, RemovalCause.EXPLICIT);
    }

    public void forEach(BiConsumer<K, V> biConsumer) {
        checkExpire();

        for (var cache : cacheMap.values()) {
            biConsumer.accept(cache.k, cache.v);
        }
    }

    public int size() {
        checkExpire();

        return cacheMap.size();
    }


    // -----------------------------------------------------------------------------------------------------------------
    private void removeForCause(K key, RemovalCause removalCause) {
        if (key == null) {
            return;
        }
        var cache = cacheMap.remove(key);
        if (cache != null) {
            removeListener.accept(List.of(cache), removalCause);
        }
    }

    private void removeForCause(List<Cache<K, V>> list, RemovalCause removalCause) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        var removeList = list.stream()
                .filter(it -> cacheMap.remove(it.k) != null)
                .toList();
        removeListener.accept(removeList, removalCause);
    }

    private void checkMaximumSize() {
        if (rwLock.writeLock().tryLock()) { // 获取写锁
            try {
                if (cacheMap.size() > backPressureSize) {
                    var removeList = cacheMap.values()
                            .stream()
                            .sorted((a, b) -> Long.compare(a.expireTime, b.expireTime))
                            .limit(Math.max(0, cacheMap.size() - maximumSize))
                            .toList();
                    removeForCause(removeList, RemovalCause.SIZE);
                }
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }


    private void checkExpire() {
        var now = TimeUtils.now();
        var expireCheckTime = expireCheckTimeAtomic.get();
        if (now > expireCheckTime) {
            if (expireCheckTimeAtomic.compareAndSet(expireCheckTime, now + expireCheckIntervalMillis)) {
                if (now > this.minExpireTime) {
                    var minTimestamp = Long.MAX_VALUE;
                    var removeList = new ArrayList<Cache<K, V>>();
                    for (var cache : cacheMap.values()) {
                        var expireTime = cache.expireTime;
                        if (expireTime < now) {
                            removeList.add(cache);
                            continue;
                        }
                        if (expireTime < minTimestamp) {
                            minTimestamp = expireTime;
                        }
                    }
                    removeForCause(removeList, RemovalCause.EXPIRED);
                    if (minTimestamp < Long.MAX_VALUE) {
                        this.minExpireTime = minTimestamp;
                    }
                }
            }
        }
    }

}
