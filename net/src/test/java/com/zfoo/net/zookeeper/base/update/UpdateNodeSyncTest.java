package com.zfoo.net.zookeeper.base.update;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

@Ignore
public class UpdateNodeSyncTest {

    private static ZooKeeper zooKeeper;

    @Test
    public void test() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new UpdateNodeSyncWatcher());
        System.out.println(zooKeeper.getState().toString());
        Thread.sleep(Integer.MAX_VALUE);
    }

    static class UpdateNodeSyncWatcher implements Watcher {
        private void doSomething() {
            try {
                Stat stat = zooKeeper.setData("/node_test", "Hello Zookeeper!".getBytes(), -1);
                System.out.println("stat:" + stat);
            } catch (InterruptedException | KeeperException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    doSomething();
                }

            }
        }
    }

}
