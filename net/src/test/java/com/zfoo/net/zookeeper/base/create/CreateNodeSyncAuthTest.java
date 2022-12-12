package com.zfoo.net.zookeeper.base.create;

import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Perms;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

@Ignore
public class CreateNodeSyncAuthTest {


    private static ZooKeeper zookeeper;
    private static boolean somethingDone = false;
    private static Stat stat = new Stat();

    @Test
    public void test() throws IOException, InterruptedException {
        zookeeper = new ZooKeeper("localhost:2181", 5000, new CreateNodeSyncAuthWatcher());
        System.out.println(zookeeper.getState());
        Thread.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void generateDigestTest() throws NoSuchAlgorithmException {
        String digest = DigestAuthenticationProvider.generateDigest("godotg:123456");
        System.out.println(digest);
    }


    static class CreateNodeSyncAuthWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            System.out.println("收到事件：" + event);
            if (event.getState() == KeeperState.SyncConnected) {
                if (!somethingDone && event.getType() == EventType.None && null == event.getPath()) {
                    doSomething();
                }
            }
        }

        /*
         * 权限模式(scheme): ip, digest
         * 授权对象(ID)
         * 		ip权限模式:  具体的ip地址
         * 		digest权限模式: username:Base64(SHA-1(username:password))
         * 权限(permission): create(C), DELETE(D),READ(R), WRITE(W), ADMIN(A)
         * 		注：单个权限，完全权限，复合权限
         *
         * 权限组合: scheme + ID + permission
         * */
        private void doSomething() {
            try {
                // ACL access control list
                ACL aclIp = new ACL(Perms.READ, new Id("ip", "192.168.1.105"));

                ACL aclDigest = new ACL(Perms.READ | Perms.WRITE, new Id("digest", DigestAuthenticationProvider.generateDigest("godotg:123456")));// 用户名+密码

                ArrayList<ACL> acls = new ArrayList<>();
                acls.add(aclDigest);
                acls.add(aclIp);
                String path = zookeeper.create("/node_test", "Hello Zookeeper!".getBytes(), acls, CreateMode.PERSISTENT);
                System.out.println("return path:" + path);
            } catch (KeeperException | InterruptedException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                // 没有取得权限之前，不能访问/node_test
                zookeeper.getData("/node_test", false, stat);
                somethingDone = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // 下面两句设置ACL的哪些author可以访问
                zookeeper.addAuthInfo("digest", "godotg:123456".getBytes());
                zookeeper.getData("/node_test", false, stat);
                somethingDone = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
