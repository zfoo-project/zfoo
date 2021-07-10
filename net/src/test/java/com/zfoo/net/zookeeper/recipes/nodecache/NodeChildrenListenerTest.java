package com.zfoo.net.zookeeper.recipes.nodecache;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class NodeChildrenListenerTest {

    @Test
    public void test() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        // PathChildrenCache用于监听指定Zookeeper数据节点的子节点变换的情况。
        // 注意：Zookeeper无法对二级子节点监听
        final PathChildrenCache cache = new PathChildrenCache(client, "/node_test", true);
        cache.start();
        cache.getListenable().addListener(new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println("CHILD_ADDED:" + event.getData());
                        break;
                    case CHILD_UPDATED:
                        System.out.println("CHILD_UPDATED:" + event.getData());
                        break;
                    case CHILD_REMOVED:
                        System.out.println("CHILD_REMOVED:" + event.getData());
                        break;
                    default:
                        break;
                }
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }

}
