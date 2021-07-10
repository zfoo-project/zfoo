package com.zfoo.net.zookeeper.curator.query;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GetDataTest {

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

        byte[] ret = client.getData().storingStatIn(stat).forPath("/node_test");

        System.out.println(new String(ret));

        System.out.println(stat);
    }

}
