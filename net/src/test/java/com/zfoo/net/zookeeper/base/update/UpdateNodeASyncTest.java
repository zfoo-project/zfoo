package com.zfoo.net.zookeeper.base.update;


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
public class UpdateNodeASyncTest {


    private static ZooKeeper zooKeeper;

    @Test
    public void test() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper("localhost:2181", 5000, new UpdateNodeASyncWatcher());
        System.out.println(zooKeeper.getState().toString());
        Thread.sleep(Integer.MAX_VALUE);
    }

    static class UpdateNodeASyncWatcher implements Watcher {
        private void doSomething(WatchedEvent event) {
            String updateContent = "new content!!!";
            zooKeeper.setData("/node_test", updateContent.getBytes(), -1, new IStatCallback(), null);
        }

        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    doSomething(event);
                }
            }
        }
    }

    static class IStatCallback implements AsyncCallback.StatCallback {
        @Override
        public void processResult(int rc, String path, Object ctx, Stat stat) {
            StringBuilder sb = new StringBuilder();
            sb.append("rc=" + rc).append("\n");
            sb.append("path" + path).append("\n");
            sb.append("ctx=" + ctx).append("\n");
            sb.append("Stat=" + stat).append("\n");
            System.out.println(sb.toString());

        }

    }

}
