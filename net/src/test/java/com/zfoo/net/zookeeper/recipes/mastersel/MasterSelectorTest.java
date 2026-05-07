package com.zfoo.net.zookeeper.recipes.mastersel;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author godotg
 * @version 1.0
 * @since 2018-08-03 15:32
 */
@Ignore
public class MasterSelectorTest {

    // The root ZNode for this master election
    private static String master_path = "/curator_recipes_master_path";

    private static CuratorFramework curator = CuratorFrameworkFactory.builder()
            .connectString("domain1.book.zookeeper:2181")
            .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

    @Test
    public void test() throws InterruptedException {
        curator.start();

        // Curator will invoke the listener callback when the Master role is successfully acquired
        LeaderSelector selector = new LeaderSelector(curator, master_path, new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("Became Master");
                Thread.sleep(3000);
                System.out.println("Master work done, releasing Master role");
            }
        });

        selector.autoRequeue();
        selector.start();
        Thread.sleep(Integer.MAX_VALUE);
    }

}
