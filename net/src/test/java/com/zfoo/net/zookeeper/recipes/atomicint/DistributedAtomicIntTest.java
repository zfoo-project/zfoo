package com.zfoo.net.zookeeper.recipes.atomicint;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicInteger;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryNTimes;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 使用Curator实现分布式计数器
 *
 * @author godotg
 * @version 1.0
 * @since 2018-08-03 16:09
 */
@Ignore
public class DistributedAtomicIntTest {


    static String distatomicint_path = "/node_test";
    static CuratorFramework curator = CuratorFrameworkFactory
            .builder()
            .connectString("localhost:2181")
            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
            .build();

    @Test
    public void test() throws Exception {
        curator.start();

        DistributedAtomicInteger atomicInteger = new DistributedAtomicInteger(curator, distatomicint_path,
                new RetryNTimes(3, 1000));

        AtomicValue<Integer> atomicValue = atomicInteger.add(8);

        System.out.println("Result: " + atomicValue.succeeded());
    }

}
