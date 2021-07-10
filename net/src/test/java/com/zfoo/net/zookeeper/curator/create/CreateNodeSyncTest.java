package com.zfoo.net.zookeeper.curator.create;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class CreateNodeSyncTest {
    //RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); // /ekspəʊ'nenʃl/ adj.  指数的; 幂数的
    //RetryPolicy retryPolicy = new RetryNTimes(5, 1000); // 最多重试5次，每次一秒

    @Test
    public void test() throws Exception {
        // 重试的时间超过最大时间后，就不再重试
        // 参数说明:
        // maxElapsedTimeMs: 最大的重试时间
        // sleepMsBetweenRetries：每次重试的间隔时间
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);// elapsed /ɪ'læps/ v.  过去; 消逝

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                // .namespace(basePath); 指定namespace，该客户端的如何操作，都是基于该相对目录进行的。
                .build();

        client.start();

        String path = client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/node_test", "Hello Zookeeper!".getBytes());

        System.out.println(path);

        Thread.sleep(Integer.MAX_VALUE);


    }
}
