package com.zfoo.net.zookeeper.base.query;


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
public class GetDataSyncTest {

    private static ZooKeeper zooKeeper;

    private static Stat stat = new Stat();

    @Test
    public void test() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new GetDataASyncWatcher());
        System.out.println(zooKeeper.getState().toString());
        Thread.sleep(Integer.MAX_VALUE);
    }


    static class GetDataASyncWatcher implements Watcher {

        private void doSomething() {
            try {
                System.out.println(new String(zooKeeper.getData("/node_test", true, stat)));
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    doSomething();
                } else {
                    if (event.getType() == EventType.NodeDataChanged) {
                        try {
                            System.out.println(new String(zooKeeper.getData(event.getPath(), true, stat)));
                            System.out.println("stat:" + stat);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        }
    }


}
