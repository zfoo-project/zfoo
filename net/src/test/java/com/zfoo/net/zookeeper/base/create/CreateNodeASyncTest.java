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
public class CreateNodeASyncTest {


    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    // 异步创建一个节点
    @Test
    public void test() throws IOException, InterruptedException {
        ZooKeeper zookeeper = new ZooKeeper("localhost:2181", 5000, new CreateNodeASyncWatcher());

        System.out.println(zookeeper.getState());

        connectedSemaphore.await();

        String content = "Hello Zookeeper!";

        // 异步创建，会回调processResult()
        zookeeper.create("/node_test", content.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, new StringCallback(), "hello");

        Thread.sleep(Integer.MAX_VALUE);
    }

    static class CreateNodeASyncWatcher implements Watcher {
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

    static class StringCallback implements AsyncCallback.StringCallback {
        @Override
        public void processResult(int rc, String path, Object ctx, String name) {
            StringBuilder sb = new StringBuilder();
            sb.append("rc=" + rc).append("\n");
            sb.append("path=" + path).append("\n");
            sb.append("ctx=" + ctx).append("\n");
            sb.append("name=" + name);
            System.out.println(sb.toString());

        }


    }

}
