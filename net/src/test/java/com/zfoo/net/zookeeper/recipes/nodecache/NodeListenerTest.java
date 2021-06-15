package com.zfoo.net.zookeeper.recipes.nodecache;

import com.zfoo.net.zookeeper.ZookeeperConstantTest;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryUntilElapsed;
import org.junit.Ignore;
import org.junit.Test;


/**
 * recipe [rec·i·pe || 'resɪpɪ] n.  食谱; 处方; 烹饪法; 制作法
 * <p>
 * 数据发布/订阅（Publish/Subscribe）系统，一般有两种方式推（Push）模式；客户端定时轮询，发现有改变就拉（Pull）。
 * <p>
 * Zookeeper采用两者相结合，客户端订阅，节点有变化服务器就推送
 */
@Ignore
public class NodeListenerTest {

    @Test
    public void test() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString(ZookeeperConstantTest.URL)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        curator.start();

        // Cache是Curator中对事件监听的包装，其对事件的监听其实可以看做本地缓存视图和远程Zookeeper视图的对比过程。
        // 同时Recipes能够反复注册监听，从而大大简化了原生API开发的繁琐程度。
        final NodeCache cache = new NodeCache(curator, "/node_test");
        cache.start();

        // 节点创建，数据改变都能检测到；如果节点被删除则不能检测。
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] ret = cache.getCurrentData().getData();
                System.out.println("new data:" + new String(ret));
            }
        });

        Thread.sleep(Integer.MAX_VALUE);
    }

}
