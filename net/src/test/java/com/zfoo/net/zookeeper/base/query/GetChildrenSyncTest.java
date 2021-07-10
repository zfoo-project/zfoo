package com.zfoo.net.zookeeper.base.query;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Ignore
public class GetChildrenSyncTest {

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk = null;

    @Test
    public void test() throws IOException, InterruptedException, KeeperException {
        zk = new ZooKeeper("localhost:2181", 5000, new GetChildrenSyncWatcher());
        System.out.println(zk.getState().toString());

        connectedSemaphore.await();

        List<String> childrenList = zk.getChildren("/", true);
        System.out.println(childrenList);

        Thread.sleep(Integer.MAX_VALUE);
    }

    static class GetChildrenSyncWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (KeeperState.SyncConnected == event.getState()) {
                if (EventType.None == event.getType() && null == event.getPath()) {
                    connectedSemaphore.countDown();
                } else if (event.getType() == EventType.NodeChildrenChanged) {
                    try {
                        System.out.println("ReGet Child:" + zk.getChildren(event.getPath(), true));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
