package com.zfoo.orm.util;

import com.zfoo.protocol.model.Pair;
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

    private static class CacheValue<V> {
        public volatile V value;
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
    private long expireAfterAccessMillis;
    private long expireCheckInterval;
    private AtomicLong expireCheckTimeAtomic;
    private volatile long minExpireTime;
    private ConcurrentMap<K, CacheValue<V>> cacheMap;
    private BiConsumer<Pair<K, V>, RemovalCause> removeCallback = (pair, removalCause) -> {
    };

    public LazyCache(int maximumSize, long expireAfterAccessMillis, long expireCheckIntervalMillis, BiConsumer<Pair<K, V>, RemovalCause> removeCallback) {
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
        var cacheValue = new CacheValue<V>();
        cacheValue.value = value;
        cacheValue.expireTime = TimeUtils.now();
        var oldCacheValue = cacheMap.put(key, cacheValue);
        if (oldCacheValue != null) {
            removeCallback.accept(new Pair<>(key, oldCacheValue.value), RemovalCause.REPLACED);
        }
        checkMaximumSize();
        checkExpire();
    }

    public V get(K key) {
        checkExpire();

        var cacheValue = cacheMap.get(key);
        if (cacheValue == null) {
            return null;
        }
        if (cacheValue.expireTime < TimeUtils.now()) {
            remove(key, RemovalCause.EXPIRED);
            return null;
        }
        cacheValue.expireTime = TimeUtils.now() + expireAfterAccessMillis;
        return cacheValue.value;
    }


    public void remove(K key) {
        remove(key, RemovalCause.EXPLICIT);
    }

    public void remove(K key, RemovalCause removalCause) {
        if (key == null) {
            return;
        }
        var cacheValue = cacheMap.remove(key);
        if (cacheValue != null) {
            removeCallback.accept(new Pair<>(key, cacheValue.value), removalCause);
        }
    }

    public void forEach(BiConsumer<K, V> biConsumer) {
        for (var entry : cacheMap.entrySet()) {
            biConsumer.accept(entry.getKey(), entry.getValue().value);
        }
    }

    public int size() {
        return cacheMap.size();
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
        remove(minKey, RemovalCause.SIZE);
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
                            remove(entry.getKey(), RemovalCause.EXPIRED);
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
