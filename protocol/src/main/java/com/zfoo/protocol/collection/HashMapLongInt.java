/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.protocol.collection;

import com.zfoo.protocol.util.StringUtils;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.internal.MathUtil;

import java.util.*;

import static com.zfoo.protocol.collection.HashMapIntInt.*;


/**
 * @author godotg
 */
public class HashMapLongInt implements Map<Long, Integer> {

    private long[] keys;
    private int[] values;
    private byte[] statuses;
    private int size;
    private int maxSize;
    private int mask;


    public HashMapLongInt() {
        this(IntObjectHashMap.DEFAULT_CAPACITY);
    }

    public HashMapLongInt(int initialCapacity) {
        var capacity = MathUtil.safeFindNextPositivePowerOfTwo(initialCapacity);
        initCapacity(capacity);
    }

    private void initCapacity(int capacity) {
        mask = capacity - 1;

        keys = new long[capacity];
        values = new int[capacity];
        statuses = new byte[capacity];

        maxSize = calcMaxSize(capacity);
    }

    private void ensureCapacity() {
        if (size > maxSize) {
            if (keys.length == Integer.MAX_VALUE) {
                throw new IllegalStateException("Max capacity reached at size=" + size);
            }
            // Double the capacity.
            rehash(keys.length << 1);
        }
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsKeyPrimitive(int key) {
        return indexOf(key) >= 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKeyPrimitive(ArrayUtils.intValue((Integer) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return containsValuePrimitive(ArrayUtils.intValue((Integer) value));
    }

    public boolean containsValuePrimitive(int value) {
        for (var i = 0; i < statuses.length; i++) {
            if (statuses[i] == FILLED && values[i] == value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer get(Object key) {
        var index = indexOf(ArrayUtils.longValue((Long) key));
        return index == -1 ? null : values[index];
    }

    @Override
    public Integer put(Long key, Integer value) {
        return putPrimitive(ArrayUtils.longValue(key), ArrayUtils.intValue(value));
    }

    public Integer putPrimitive(long key, int value) {
        var startIndex = hashIndex(key);
        var index = startIndex;

        var firstRemoveIndex = -1;
        for (; ; ) {
            var status = statuses[index];
            if (status == FREE) {
                index = firstRemoveIndex < 0 ? index : firstRemoveIndex;
                set(index, key, value, FILLED);
                size++;
                ensureCapacity();
                return null;
            } else if (status == REMOVED) {
                firstRemoveIndex = firstRemoveIndex < 0 ? index : firstRemoveIndex;
            } else if (keys[index] == key) { // status == FILLED
                // Found existing entry with this key, just replace the value.
                var previousValue = values[index];
                values[index] = value;
                return previousValue;
            }

            // Conflict, keep probing ...
            if ((index = probeNext(index, mask)) == startIndex) {
                if (firstRemoveIndex < 0) {
                    throw new IllegalStateException("Unable to insert, the map was full at MAX_ARRAY_SIZE and couldn't grow");
                } else {
                    set(firstRemoveIndex, key, value, FILLED);
                    size++;
                    ensureCapacity();
                    return null;
                }
            }
        }
    }

    @Override
    public Integer remove(Object key) {
        return removePrimitive(ArrayUtils.longValue((Long) key));
    }

    public Integer removePrimitive(long key) {
        var index = indexOf(key);
        if (index == -1) {
            return null;
        }
        var prev = values[index];
        removeAt(index);
        return prev;
    }

    private void removeAt(int index) {
        set(index, 0, 0, REMOVED);
        size--;
    }

    @Override
    public void putAll(Map<? extends Long, ? extends Integer> m) {
        for (Entry<? extends Long, ? extends Integer> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        Arrays.fill(keys, 0);
        Arrays.fill(values, 0);
        Arrays.fill(statuses, FREE);
        size = 0;
    }

    @Override
    public Set<Long> keySet() {
        return new KeySet();
    }

    @Override
    public Collection<Integer> values() {
        return new ValueSet();
    }

    @Override
    public Set<Entry<Long, Integer>> entrySet() {
        return new EntrySet();
    }

    private int hashIndex(long key) {
        return ((int) (key ^ (key >>> 32))) & mask;
    }

    private void set(int index, long key, int value, byte status) {
        keys[index] = key;
        values[index] = value;
        statuses[index] = status;
    }

    private void rehash(int newCapacity) {
        var oldKeys = keys;
        var oldValues = values;
        var oldStatuses = statuses;

        initCapacity(newCapacity);

        for (var i = 0; i < oldStatuses.length; ++i) {
            var oldStatus = oldStatuses[i];
            if (oldStatus == FILLED) {
                var oldKey = oldKeys[i];
                var oldValue = oldValues[i];
                int index = hashIndex(oldKey);

                for (; ; ) {
                    if (statuses[index] == FREE) {
                        set(index, oldKey, oldValue, FILLED);
                        break;
                    }

                    index = probeNext(index, mask);
                }
            }
        }
    }

    private int indexOf(long key) {
        int startIndex = hashIndex(key);
        int index = startIndex;

        for (; ; ) {
            var status = statuses[index];
            if (status == FREE) {
                // It's available, so no chance that this value exists anywhere in the map.
                return -1;
            }
            if (key == keys[index] && status == FILLED) {
                return index;
            }

            // Conflict, keep probing ...
            if ((index = probeNext(index, mask)) == startIndex) {
                return -1;
            }
        }
    }

    private class PrimitiveEntry implements Entry<Long, Integer> {
        int entryIndex;

        PrimitiveEntry(int entryIndex) {
            this.entryIndex = entryIndex;
        }

        @Override
        public Long getKey() {
            return keys[entryIndex];
        }

        @Override
        public Integer getValue() {
            return values[entryIndex];
        }

        @Override
        public Integer setValue(Integer value) {
            var prevValue = values[entryIndex];
            values[entryIndex] = ArrayUtils.intValue(value);
            return prevValue;
        }
    }

    private class FastIterator implements Iterator<Entry<Long, Integer>> {
        int lastCursor = -1;
        int cursor = -1;

        private void scanNext() {
            while (++cursor != statuses.length && statuses[cursor] != FILLED) {
            }
        }

        @Override
        public boolean hasNext() {
            if (cursor == -1) {
                scanNext();
            }
            return cursor != statuses.length;
        }

        @Override
        public Entry<Long, Integer> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            lastCursor = cursor;
            scanNext();

            return new PrimitiveEntry(lastCursor);
        }

        @Override
        public void remove() {
            if (lastCursor == -1) {
                throw new IllegalStateException("next must be called before each remove.");
            }
            removeAt(lastCursor);
            cursor = -1;
            lastCursor = -1;
        }
    }

    private final class KeySet extends AbstractSet<Long> {
        FastIterator fastIterator = new FastIterator();

        @Override
        public Iterator<Long> iterator() {
            return new Iterator<Long>() {
                @Override
                public boolean hasNext() {
                    return fastIterator.hasNext();
                }

                @Override
                public Long next() {
                    return fastIterator.next().getKey();
                }

                @Override
                public void remove() {
                    fastIterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return HashMapLongInt.this.size();
        }
    }

    private final class ValueSet extends AbstractSet<Integer> {
        FastIterator fastIterator = new FastIterator();

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                @Override
                public boolean hasNext() {
                    return fastIterator.hasNext();
                }

                @Override
                public Integer next() {
                    return fastIterator.next().getValue();
                }

                @Override
                public void remove() {
                    fastIterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return HashMapLongInt.this.size();
        }
    }

    private final class EntrySet extends AbstractSet<Entry<Long, Integer>> {
        @Override
        public Iterator<Entry<Long, Integer>> iterator() {
            return new FastIterator();
        }

        @Override
        public int size() {
            return HashMapLongInt.this.size();
        }
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return StringUtils.EMPTY_JSON;
        }
        var builder = new StringBuilder(4 * size);
        builder.append('{');
        var first = true;
        for (int i = 0; i < values.length; ++i) {
            if (statuses[i] != FILLED) {
                continue;
            }
            if (!first) {
                builder.append(", ");
            }
            builder.append(keys[i]).append('=').append(values[i]);
            first = false;
        }
        return builder.append('}').toString();
    }
}
