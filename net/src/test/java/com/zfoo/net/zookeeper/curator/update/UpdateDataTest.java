package com.zfoo.net.zookeeper.curator.update;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class UpdateDataTest {

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

        Stat stat = new Stat();
        client.getData().storingStatIn(stat).forPath("/node_test");

        client.setData().withVersion(stat.getVersion()).forPath("/node_test", "Hello Zookeeper!".getBytes());
    }

}
