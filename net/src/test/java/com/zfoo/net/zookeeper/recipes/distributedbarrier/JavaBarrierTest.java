package com.zfoo.net.zookeeper.recipes.distributedbarrier;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author godotg
 * @version 1.0
 * @since 2018-08-03 16:13
 */
@Ignore
public class JavaBarrierTest {

    public static CyclicBarrier barrier = new CyclicBarrier(3);

    @Test
    public void test() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(new Thread(new Runner("Racer 1")));
        executor.submit(new Thread(new Runner("Racer 2")));
        executor.submit(new Thread(new Runner("Racer 3")));
        executor.shutdown();
    }

    static class Runner implements Runnable {
        private String name;

        public Runner(String name) {
            this.name = name;
        }

        public void run() {
            System.out.println(name + " is ready.");
            try {
                JavaBarrierTest.barrier.await();
            } catch (Exception e) {
            }
            System.out.println(name + " go!");
        }

    }

}

