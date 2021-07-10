package com.zfoo.net.zookeeper.base.create;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;


@Ignore
public class CreateNodeSyncTest {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    // 同步创建节点
    @Test
    public void test() throws IOException, InterruptedException, KeeperException {
        // 这个创建zookeeper的链接是异步的
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000, new CreateNodeSyncWatcher());

        System.out.println(zookeeper.getState());

        connectedSemaphore.await();

        // 无论是同步还是异步，Zookeeper都不支持递归创建，既无法在父节点不存在的情况下创建一个子节点。如果创建的节点存在，则抛出异常。
        String path = zookeeper.create("/node_test", "Hello Zookeeper!".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("return path:" + path);

        Thread.sleep(Integer.MAX_VALUE);
    }

    static class CreateNodeSyncWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("接受事件" + event);
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    connectedSemaphore.countDown();
                }
            }
        }
    }

}
