package com.zfoo.net.zookeeper.curator.cache;

import com.zfoo.net.zookeeper.ZookeeperConstantTest;
import com.zfoo.util.ThreadUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2019-08-25 09:43
 */
@Ignore
public class PathCacheTest {

    private static CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString(ZookeeperConstantTest.URL)
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
//            .retryPolicy(new RetryNTimes(1, 1000))
            .build();

    @Test
    public void test() {
        curator.start();

        PathChildrenCache pathCache = new PathChildrenCache(curator, "/test", true);
        try {
            pathCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //节点变化的监听器
        pathCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println("pathCache ------ Type:" + event.getType() + ",");
                System.out.println(event.getData().getPath());
            }
        });

        while (true) {
            ThreadUtils.sleep(3000);
            try {
                pathCache.getCurrentData().stream()
                        .forEach(it -> System.out.println(it.getPath() + "->" + it.getData()));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
