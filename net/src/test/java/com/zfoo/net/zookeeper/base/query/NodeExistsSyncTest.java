package com.zfoo.net.zookeeper.base.query;


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
public class NodeExistsSyncTest {

    private static ZooKeeper zooKeeper;

    @Test
    public void test() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new NodeExistsSyncWatcher());
        System.out.println(zooKeeper.getState().toString());
        Thread.sleep(Integer.MAX_VALUE);
    }

    static class NodeExistsSyncWatcher implements Watcher {

        private void doSomething(ZooKeeper zooKeeper) {
            try {
                Stat stat = zooKeeper.exists("/node_test", true);
                System.out.println(stat);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void process(WatchedEvent event) {

            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    doSomething(zooKeeper);
                } else {
                    try {
                        if (event.getType() == EventType.NodeCreated) {
                            System.out.println(event.getPath() + " created");
                            System.out.println(zooKeeper.exists(event.getPath(), true));
                        } else if (event.getType() == EventType.NodeDataChanged) {
                            System.out.println(event.getPath() + " updated");
                            System.out.println(zooKeeper.exists(event.getPath(), true));
                        } else if (event.getType() == EventType.NodeDeleted) {
                            System.out.println(event.getPath() + " deleted");
                            System.out.println(zooKeeper.exists(event.getPath(), true));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }


}
