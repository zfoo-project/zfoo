package com.zfoo.net.zookeeper.curator.query;

import com.zfoo.net.zookeeper.ZookeeperConstantTest;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class GetChildrenTest {

    @Test
    public void test() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString(ZookeeperConstantTest.URL)
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        List<String> cList = client.getChildren().forPath("/node_test");

        System.out.println(cList.toString());
    }

}
