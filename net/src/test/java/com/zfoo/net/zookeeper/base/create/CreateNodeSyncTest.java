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

    // Synchronously create a node
    @Test
    public void test() throws IOException, InterruptedException, KeeperException {
        // Note: ZooKeeper connection is established asynchronously; wait for the latch before using the client
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000, new CreateNodeSyncWatcher());

        System.out.println(zookeeper.getState());

        connectedSemaphore.await();

        // ZooKeeper does not support recursive node creation; parent nodes must exist first.
        // If the node already exists, an exception is thrown.
        String path = zookeeper.create("/node_test", "Hello Zookeeper!".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println("return path:" + path);

        Thread.sleep(Integer.MAX_VALUE);
    }

    static class CreateNodeSyncWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            System.out.println("Received event: " + event);
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    connectedSemaphore.countDown();
                }
            }
        }
    }

}
