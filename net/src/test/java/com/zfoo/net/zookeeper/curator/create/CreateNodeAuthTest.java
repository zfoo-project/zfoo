package com.zfoo.net.zookeeper.curator.create;


import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

@Ignore
public class CreateNodeAuthTest {

    @Test
    public void createTest() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        curator.start();

        ACL aclIp = new ACL(Perms.READ, new Id("ip", "192.168.1.105"));
        ACL aclDigest = new ACL(Perms.READ | Perms.WRITE, new Id("digest", DigestAuthenticationProvider.generateDigest("godotg:123456")));
        ArrayList<ACL> acls = new ArrayList<>();
        acls.add(aclDigest);
        acls.add(aclIp);

        // 使用jaysunxiao:123456创建一个授权节点
        String path = curator.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(acls)
                .forPath("/node_test", "Hello Zookeeper!".getBytes());

        System.out.println(path);

        Thread.sleep(Integer.MAX_VALUE);
    }


    @Test
    public void getDataWithAuthTest() throws Exception {
        RetryPolicy retryPolicy = new RetryUntilElapsed(5000, 1000);

        CuratorFramework curator = CuratorFrameworkFactory
                .builder()
                .connectString("localhost:2181")
                .sessionTimeoutMs(5000)
                .authorization("digest", "godotg:123456".getBytes()) // 授权访问
                .connectionTimeoutMs(5000)
                .retryPolicy(retryPolicy)
                .build();

        curator.start();

        Stat stat = new Stat();

        byte[] ret = curator.getData().storingStatIn(stat).forPath("/node_test");

        System.out.println(new String(ret));

        System.out.println(stat);
    }


}
