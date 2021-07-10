package com.zfoo.net.zookeeper.base.query;


import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Ignore
public class GetDataASyncTest {


    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
    private static ZooKeeper zk;

    // ZooKeeper API 获取节点数据内容，使用异步(async)接口。
    @Test
    public void test() throws IOException, InterruptedException, KeeperException {
        zk = new ZooKeeper("localhost:2181", 5000, new GetDataASyncWatcher());
        System.out.println(zk.getState().toString());

        String path = "/node_test";
        connectedSemaphore.await();

        zk.getData(path, true, new IDataCallback(), null);

        Thread.sleep(Integer.MAX_VALUE);
    }

    static class GetDataASyncWatcher implements Watcher {
        @Override
        public void process(WatchedEvent event) {
            if (KeeperState.SyncConnected == event.getState()) {
                if (EventType.None == event.getType() && null == event.getPath()) {
                    connectedSemaphore.countDown();
                } else if (event.getType() == EventType.NodeDataChanged) {
                    try {
                        zk.getData(event.getPath(), true, new IDataCallback(), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class IDataCallback implements AsyncCallback.DataCallback {
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            System.out.println(rc + ", " + path + ", " + new String(data));
            System.out.println(
                    stat.getCzxid() + "," +
                            stat.getMzxid() + "," +
                            stat.getVersion()
            );
        }
    }
}

