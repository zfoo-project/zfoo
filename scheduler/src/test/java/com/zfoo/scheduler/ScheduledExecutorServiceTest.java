package com.zfoo.scheduler;

import com.zfoo.scheduler.manager.SchedulerThreadFactory;
import com.zfoo.util.ThreadUtils;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.*;

public class ScheduledExecutorServiceTest {

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SchedulerThreadFactory(1));

    @Test
    public void test() {
        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(new Date() + "running");
//                System.out.println(1 /0 );
            }
        }, 0, 2, TimeUnit.SECONDS);

        while (!executor.isShutdown()) {
            try {
                scheduledFuture.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {//异常捕获
                throw new RuntimeException(e);
            }
            ThreadUtils.sleep(1000);
            System.out.println("isDone:" + scheduledFuture.isDone());
            System.out.println("isCancelled" + scheduledFuture.isCancelled());
        }
    }
}
