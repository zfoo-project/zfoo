package com.zfoo.net.zookeeper.recipes.distributedbarrier;

import com.zfoo.net.zookeeper.ZookeeperConstantTest;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 使用Curator实现分布式Barrier
 *
 * @author jaysunxiao
 * @version 1.0
 * @since 2018-08-03 16:16
 */
@Ignore
public class DistributedBarrierTest {

    private static String barrier_path = "/curator_recipes_barrier_path";
    private static DistributedBarrier barrier;

    @Test
    public void test() throws Exception {
        for (int i = 0; i < 5; i++) {
            new Thread(new Runnable() {
                public void run() {
                    try {

                        CuratorFramework client = CuratorFrameworkFactory
                                .builder()
                                .connectString(ZookeeperConstantTest.URL)
                                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                                .build();
                        client.start();
                        barrier = new DistributedBarrier(client, barrier_path);
                        System.out.println(Thread.currentThread().getName() + "号barrier设置");
                        barrier.setBarrier();
                        barrier.waitOnBarrier();
                        System.err.println("启动...");

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }
        Thread.sleep(2000);
        barrier.removeBarrier();
    }

}
