package com.zfoo.net.zookeeper.base.delete;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Ignore
public class DeleteNodeASyncTest {


    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    @Test
    public void test() throws IOException, InterruptedException {
        ZooKeeper zooKeeper = new ZooKeeper("localhost:2181", 5000, new DeleteNodeASyncWatcher());
        System.out.println(zooKeeper.getState().toString());

        connectedSemaphore.await();

        zooKeeper.delete("/node_test", -1, new IVoidCallback(), null);

        Thread.sleep(Integer.MAX_VALUE);
    }


    static class DeleteNodeASyncWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected) {
                if (event.getType() == EventType.None && null == event.getPath()) {
                    connectedSemaphore.countDown();
                }
            }
        }
    }

    static class IVoidCallback implements AsyncCallback.VoidCallback {
        @Override
        public void processResult(int rc, String path, Object ctx) {
            StringBuilder sb = new StringBuilder();
            sb.append("rc=" + rc).append("\n");
            sb.append("path" + path).append("\n");
            sb.append("ctx=" + ctx).append("\n");
            System.out.println(sb.toString());

        }

    }

}
