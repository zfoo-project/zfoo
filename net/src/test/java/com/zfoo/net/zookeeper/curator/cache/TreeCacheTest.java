package com.zfoo.net.zookeeper.curator.cache;


import com.zfoo.util.ThreadUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2019-08-25 09:43
 */
@Ignore
public class TreeCacheTest {

    private static CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
//            .retryPolicy(new RetryNTimes(1, 1000))
            .build();

    @Test
    public void test() {
        curator.start();

        TreeCache treeCache = new TreeCache(curator, "/test");
        try {
            treeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //添加错误监听器
        treeCache.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            public void unhandledError(String s, Throwable throwable) {
                System.out.println(".错误原因：" + throwable.getMessage() + "\n==============\n");
            }
        });

        //节点变化的监听器
        treeCache.getListenable().addListener(new TreeCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {
                System.out.println("treeCache ------ Type:" + treeCacheEvent.getType() + ",");
                System.out.println(treeCacheEvent.getData().getPath());

            }
        });

        while (true) {
            ThreadUtils.sleep(3000);
            try {
                System.out.println("-------->" + treeCache.getCurrentData("/test/a"));

                treeCache.getCurrentChildren("/test").entrySet().stream()
                        .forEach(it -> System.out.println(it.getKey() + "->" + it.getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
