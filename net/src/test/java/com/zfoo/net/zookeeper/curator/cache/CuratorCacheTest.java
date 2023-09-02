package com.zfoo.net.zookeeper.curator.cache;


import com.zfoo.protocol.util.ThreadUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 * @version 1.0
 * @since 2019-08-25 09:43
 */
@Ignore
public class CuratorCacheTest {

    private static CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
//            .retryPolicy(new RetryNTimes(1, 1000))
            .build();

    @Test
    public void test() {
        curator.start();
        var curatorCache = CuratorCache.builder(curator, "/test")
                .withExceptionHandler(e -> e.printStackTrace())
                .build();

        curatorCache.start();

        //节点变化的监听器
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData data) {
                System.out.println("pathCache ------ Type:" + type + ",");
                System.out.println(data);
            }
        });

        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
