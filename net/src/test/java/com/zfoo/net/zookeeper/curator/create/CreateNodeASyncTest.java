package com.zfoo.net.zookeeper.curator.create;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author godotg
 * @version 1.0
 * @since 2018-08-01 16:43
 */
@Ignore
public class CreateNodeASyncTest {

    private static CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString("localhost:2181")
            .sessionTimeoutMs(5000)
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();

    private static CountDownLatch semaphore = new CountDownLatch(2);

    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    public void test() throws Exception {
        curator.start();

        System.out.println("Main thread: " + Thread.currentThread().getName());

        // A custom Executor is provided here for the async callback
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
                System.out.println("Thread of processResult: " + Thread.currentThread().getName());
                semaphore.countDown();
            }
        }, executorService).forPath("/node_test", "Hello Zookeeper!".getBytes());

        // No custom Executor provided; ZooKeeper's internal EventThread will handle the callback
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
                System.out.println("Thread of processResult: " + Thread.currentThread().getName());
                semaphore.countDown();
            }
        }).forPath("/node_test", "Hello Zookeeper!".getBytes());

        semaphore.await();
        executorService.shutdown();
    }

}
