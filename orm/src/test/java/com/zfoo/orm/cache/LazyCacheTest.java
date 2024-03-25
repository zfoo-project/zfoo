package com.zfoo.orm.cache;

import com.zfoo.orm.util.LazyCache;
import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import com.zfoo.scheduler.util.TimeUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.BiConsumer;

/**
 * @author godotg
 */
@Ignore
public class LazyCacheTest {

    private static final BiConsumer<Integer, String> myRemoveCallback = new BiConsumer<Integer, String>() {
        @Override
        public void accept(Integer key, String value) {
            System.out.println(StringUtils.format("remove key:[{}] value:[{}]", key, value));
        }
    };

    @Test
    public void putTest() {
        var lazyCache = new LazyCache<Integer, String>(3, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);

        lazyCache.put(1, "a");
        lazyCache.put(2, "b");
        ThreadUtils.sleep(1000);
        lazyCache.put(3, "c");
        ThreadUtils.sleep(1000);
        lazyCache.put(4, "d");
        ThreadUtils.sleep(1000);
        lazyCache.put(5, "e");
    }

    @Test
    public void expireTest() {
        var lazyCache = new LazyCache<Integer, String>(10, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);

        lazyCache.put(1, "a");
        lazyCache.put(2, "b");
        lazyCache.put(3, "c");
        lazyCache.put(4, "d");
        lazyCache.put(5, "e");
        ThreadUtils.sleep(11 * TimeUtils.MILLIS_PER_SECOND);
        System.out.println(lazyCache.get(1));
        System.out.println(lazyCache.get(2));
        System.out.println(lazyCache.get(3));
        System.out.println(lazyCache.get(4));
        System.out.println(lazyCache.get(5));
    }

    @Test
    public void batchTest() {
        var lazyCache = new LazyCache<Integer, String>(1_0000, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);

        for (int i = 0; i < 1000_0000; i++) {
            lazyCache.put(i, String.valueOf(i));
            if (i % 1_0000 == 0) {
                ThreadUtils.sleep(5 * TimeUtils.MILLIS_PER_SECOND);
            }
        }
    }
}
