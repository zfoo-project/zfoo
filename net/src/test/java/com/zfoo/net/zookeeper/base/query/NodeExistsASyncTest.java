package com.zfoo.net.zookeeper.base.query;


import org.apache.zookeeper.AsyncCallback;
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
public class NodeExistsASyncTest {


    private static ZooKeeper zooKeeper;

    @Test
    public void test() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new NodeExistsASynccWatcher());
        System.out.println(zooKeeper.getState().toString());
        Thread.sleep(Integer.MAX_VALUE);
    }


    static class NodeExistsASynccWatcher implements Watcher {

        private void doSomething(ZooKeeper zookeeper) {
            zooKeeper.exists("/node_test", true, new IStateCallback(), null);
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
                            zooKeeper.exists(event.getPath(), true, new IStateCallback(), null);
                        } else if (event.getType() == EventType.NodeDataChanged) {
                            System.out.println(event.getPath() + " updated");
                            zooKeeper.exists(event.getPath(), true, new IStateCallback(), null);
                        } else if (event.getType() == EventType.NodeDeleted) {
                            System.out.println(event.getPath() + " deleted");
                            zooKeeper.exists(event.getPath(), true, new IStateCallback(), null);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        }
    }


    static class IStateCallback implements AsyncCallback.StatCallback {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            System.out.println("rc:" + rc);

        }
    }

}
