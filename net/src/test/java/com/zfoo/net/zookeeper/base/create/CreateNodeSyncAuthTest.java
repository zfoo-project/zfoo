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
            System.out.println("Received event: " + event);
            if (event.getState() == KeeperState.SyncConnected) {
                if (!somethingDone && event.getType() == EventType.None && null == event.getPath()) {
                    doSomething();
                }
            }
        }

        /*
         * Permission scheme: ip, digest
         * Authorization ID
         * 		ip:  ip
         * 		digest: username:Base64(SHA-1(username:password))
         * Permission flags: CREATE(C), DELETE(D), READ(R), WRITE(W), ADMIN(A)
         * 		
         *
         * ACL entry = scheme + ID + permission
         * */
        private void doSomething() {
            try {
                // ACL access control list
                ACL aclIp = new ACL(Perms.READ, new Id("ip", "192.168.1.105"));

                ACL aclDigest = new ACL(Perms.READ | Perms.WRITE, new Id("digest", DigestAuthenticationProvider.generateDigest("godotg:123456")));// username and password

                ArrayList<ACL> acls = new ArrayList<>();
                acls.add(aclDigest);
                acls.add(aclIp);
                String path = zookeeper.create("/node_test", "Hello Zookeeper!".getBytes(), acls, CreateMode.PERSISTENT);
                System.out.println("return path:" + path);
            } catch (KeeperException | InterruptedException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            try {
                // username and passwordnode_test
                zookeeper.getData("/node_test", false, stat);
                somethingDone = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                // The following lines configure which authorized users can access this node
                zookeeper.addAuthInfo("digest", "godotg:123456".getBytes());
                zookeeper.getData("/node_test", false, stat);
                somethingDone = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
