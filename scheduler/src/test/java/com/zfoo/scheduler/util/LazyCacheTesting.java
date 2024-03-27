package com.zfoo.scheduler.util;

import com.zfoo.protocol.model.Pair;
import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * @author godotg
 */
@Ignore
public class LazyCacheTesting {

    private static final Logger logger = LoggerFactory.getLogger(LazyCacheTesting.class);

    private static final BiConsumer<Pair<Integer, String>, LazyCache.RemovalCause> myRemoveCallback = new BiConsumer<Pair<Integer, String>, LazyCache.RemovalCause>() {
        @Override
        public void accept(Pair<Integer, String> pair, LazyCache.RemovalCause removalCause) {
            logger.info("remove key:[{}] value:[{}] removalCause:[{}]", pair.getKey(), pair.getValue(), removalCause);
        }
    };

    @Test
    public void putTest() {
        var lazyCache = new LazyCache<Integer, String>(10, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);

        lazyCache.put(1, "a");
        lazyCache.put(2, "b");
        lazyCache.put(3, "c");
        lazyCache.put(4, "d");
        lazyCache.put(5, "e");
        lazyCache.put(6, "f");
        lazyCache.put(7, "g");
        lazyCache.put(8, "h");
        lazyCache.put(9, "i");
        lazyCache.put(10, "j");
        lazyCache.put(11, "k");
        lazyCache.put(12, "l");
        ThreadUtils.sleep(1000);
        lazyCache.put(13, "m");
        ThreadUtils.sleep(1000);
        lazyCache.put(14, "n");
        ThreadUtils.sleep(1000);
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
    public void expire1Test() {
        var lazyCache = new LazyCache<Integer, String>(10, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);

        lazyCache.put(1, "a");
        lazyCache.put(2, "b");
        lazyCache.put(3, "c");
        lazyCache.put(4, "d");
        lazyCache.put(5, "e");
        for (int i = 0; i < 11; i++) {
            lazyCache.get(1);
            lazyCache.get(2);
            ThreadUtils.sleep(1 * TimeUtils.MILLIS_PER_SECOND);
        }
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


    // max size test
    @Test
    public void multipleThreadMaxSizeTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_00, 10000000 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            int i1 = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    var startIndex = i1 * 1_0000;
                    for (int j = i1 * 1_0000; j < startIndex + 1_0000; j++) {
                        lazyCache.put(j, String.valueOf(j));
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(1000);
        }
    }

    // check interval test
    @Test
    public void multipleThreadCheckIntervalTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_0000, 10000000 * TimeUtils.MILLIS_PER_SECOND, 0, myRemoveCallback);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            int i1 = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    var startIndex = i1 * 1_0000;
                    for (int j = i1 * 1_0000; j < startIndex + 1_0000; j++) {
                        lazyCache.put(j, String.valueOf(j));
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(1000);
        }
    }

    // expire after access test
    @Test
    public void multipleThreadExpireAfterAccessTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_0000, 0, 0, null);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            int i1 = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    var startIndex = i1 * 1_0000;
                    for (int j = i1 * 1_0000; j < startIndex + 1_0000; j++) {
                        lazyCache.put(j, String.valueOf(j));
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            lazyCache.get(-1);
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(100);
        }
    }

    // not expired test
    @Test
    public void multipleThreadNotExpiredTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_0000, 100000 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            int i1 = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    var startIndex = i1 * 1_0000;
                    for (int j = i1 * 1_0000; j < startIndex + 1_0000; j++) {
                        lazyCache.put(j, String.valueOf(j));
                    }
                    for (int j = 0; j < 10000; j++) {
                        lazyCache.get(j);
                        ThreadUtils.sleep(1);
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(1000);
        }
    }

    // remove reason for expire test
    @Test
    public void multipleThreadExpireTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_0000, 20 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            int i1 = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    var startIndex = i1 * 1_0000;
                    for (int j = i1 * 1_0000; j < startIndex + 1_0000; j++) {
                        lazyCache.put(j, String.valueOf(j));
                    }
                    while (true) {
                        lazyCache.get(-1);
                        ThreadUtils.sleep(1);
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(1000);
        }
    }

    // remove reason for replace test
    @Test
    public void multipleThreadReplaceTest() {
        int threadNum = Runtime.getRuntime().availableProcessors() + 1;
        ExecutorService[] executors = new ExecutorService[threadNum];
        for (int i = 0; i < executors.length; i++) {
            executors[i] = Executors.newSingleThreadExecutor();
        }
        var lazyCache = new LazyCache<Integer, String>(1_0000, 10 * TimeUtils.MILLIS_PER_SECOND, 5 * TimeUtils.MILLIS_PER_SECOND, myRemoveCallback);
        for (int i = 0; i < executors.length; i++) {

            var executor = executors[i];
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (int k = 0; k < 10; k++) {
                        for (int j = 0; j < 1_0000; j++) {
                            lazyCache.put(j, String.valueOf(j));
                        }
                    }

                    while (true) {
                        lazyCache.get(-1);
                        ThreadUtils.sleep(1);
                    }
                }
            });
        }
        for (int i = 0; i < 10000; i++) {
            logger.info("cache size:[{}]", lazyCache.size());
            ThreadUtils.sleep(1000);
        }
    }
}
