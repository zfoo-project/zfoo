package com.zfoo.net.zookeeper.curator.query;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Ignore
public class NodeExistsTest {

    @Test
    public void test() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(5);

        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        client.checkExists().inBackground(new BackgroundCallback() {

            @Override
            public void processResult(CuratorFramework arg0, CuratorEvent arg1) {
                Stat stat = arg1.getStat();
                System.out.println(stat);
                System.out.println(arg1.getContext());
            }
        }, "Hello Zookeeper!", es).forPath("/node_test");

        Thread.sleep(Integer.MAX_VALUE);
    }


    @Test
    public void testNodeExist() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        // 如果没有这个节点，则stat返回空
        Stat stat = client.checkExists().forPath("/zfoo/provider");

        Thread.sleep(Integer.MAX_VALUE);
    }

}
