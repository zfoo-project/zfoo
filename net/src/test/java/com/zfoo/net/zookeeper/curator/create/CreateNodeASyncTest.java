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

        // 此处传入了自定义的Executor
        curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
            @Override
            public void processResult(CuratorFramework client, CuratorEvent event) throws Exception {
                System.out.println("event[code: " + event.getResultCode() + ", type: " + event.getType() + "]");
                System.out.println("Thread of processResult: " + Thread.currentThread().getName());
                semaphore.countDown();
            }
        }, executorService).forPath("/node_test", "Hello Zookeeper!".getBytes());

        // 此处没有传入自定义的Executor，会默认使用Zookeeper内部的EventThread单线程处理
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
