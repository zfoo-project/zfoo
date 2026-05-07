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
    //RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); // exponential backoff strategy
    //RetryPolicy retryPolicy = new RetryNTimes(5, 1000); // retry at most N times, sleep 1s between each
    @Test
    public void test() throws Exception {
        // Stop retrying once the total elapsed time exceeds the maximum
        // Parameter description:
        // maxElapsedTimeMs: maximum total retry duration (ms)
        // sleepMsBetweenRetries: sleep duration between retry attempts (ms)
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);// elapsed: time that has passed
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                // .namespace(basePath); // sets namespace so all operations are relative to this base path
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
