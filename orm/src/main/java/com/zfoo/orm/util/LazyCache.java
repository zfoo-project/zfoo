package com.zfoo.orm.util;

import com.zfoo.scheduler.util.TimeUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * lazy detect the expiration time
 *
 * @author godotg
 */
public class LazyCache<K, V> {

    private static class ValueCache<V> {
        public volatile V value;
        public volatile long expireTime;
    }


    private AtomicLong expireCheckTimeAtomic;
    private volatile long minExpireTime;
    private long expireAfterAccessMillis;
    private long expireCheckInterval;
    private int maximumSize;
    private ConcurrentMap<K, ValueCache<V>> cacheMap;
    private BiConsumer<K, V> removeCallback = (k, v) -> {
    };

    public LazyCache(int maximumSize, long expireAfterAccessMillis, long expireCheckIntervalMillis, BiConsumer<K, V> removeCallback) {
        this.maximumSize = maximumSize;
        this.expireAfterAccessMillis = expireAfterAccessMillis;
        this.expireCheckInterval = expireCheckIntervalMillis;
        this.expireCheckTimeAtomic = new AtomicLong(TimeUtils.now() + expireCheckInterval);
        this.cacheMap = new ConcurrentHashMap<>(Math.max(maximumSize / 16, 512));
        if (removeCallback != null) {
            this.removeCallback = removeCallback;
        }
    }

    /**
     * If the cache previously contained a value associated with the key, the old value is replaced by the new value.
     */
    public void put(K key, V value) {
        var valueCache = new ValueCache<V>();
        valueCache.value = value;
        valueCache.expireTime = TimeUtils.now();
        cacheMap.put(key, valueCache);
        checkMaximumSize();
        checkExpire();
    }

    public V get(K key) {
        checkExpire();

        var valueCache = cacheMap.get(key);
        if (valueCache == null) {
            return null;
        }
        if (valueCache.expireTime < TimeUtils.now()) {
            remove(key);
            return null;
        }
        valueCache.expireTime = TimeUtils.now() + expireAfterAccessMillis;
        return valueCache.value;
    }


    public void remove(K key) {
        if (key == null) {
            return;
        }
        var valueCache = cacheMap.remove(key);
        if (valueCache != null) {
            removeCallback.accept(key, valueCache.value);
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    private void checkMaximumSize() {
        if (cacheMap.size() <= maximumSize) {
            return;
        }
        K minKey = null;
        var minTimestamp = Long.MAX_VALUE;
        for (var entry : cacheMap.entrySet()) {
            if (entry.getValue().expireTime < minTimestamp) {
                minKey = entry.getKey();
                minTimestamp = entry.getValue().expireTime;
            }
        }
        this.minExpireTime = minTimestamp;
        remove(minKey);
        checkMaximumSize();
    }

    private void checkExpire() {
        var now = TimeUtils.now();
        var expireCheckTime = expireCheckTimeAtomic.get();
        if (now > expireCheckTime) {
            if (expireCheckTimeAtomic.compareAndSet(expireCheckTime, now + expireCheckInterval)) {
                if (now > this.minExpireTime) {
                    var minTimestamp = Long.MAX_VALUE;
                    for (var entry : cacheMap.entrySet()) {
                        var expireTime = entry.getValue().expireTime;
                        if (expireTime < now) {
                            remove(entry.getKey());
                        }
                        if (expireTime < minTimestamp) {
                            minTimestamp = expireTime;
                        }
                    }
                    this.minExpireTime = minTimestamp;
                }
            }
        }
    }

}
