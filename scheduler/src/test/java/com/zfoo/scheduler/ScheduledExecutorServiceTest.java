package com.zfoo.scheduler;

import com.zfoo.scheduler.manager.SchedulerThreadFactory;
import com.zfoo.util.ThreadUtils;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.*;

/**
 * `FutureTask` 是 Java 并发包 (`java.util.concurrent`) 中的一个非常重要的类，用于表示异步计算的结果。它实现了 `Future` 接口和 `Runnable` 接口，因此它既可以作为 `Runnable` 被线程执行，也可以作为 `Future` 获取计算结果。`FutureTask` 的设计思路主要围绕以下几个关键点：
 *
 * 1. **封装异步计算的结果**：`FutureTask` 通过提供一个 `call()` 方法来执行异步任务，这个方法返回的结果可以通过 `get()` 方法获取。这样，即使异步任务的执行需要很长时间，调用者也不会被阻塞，直到调用 `get()` 方法时才需要等待，如果需要的话。
 *
 * 2. **状态管理**：`FutureTask` 内部通过一个状态变量来管理任务的状态，如是否已开始、是否已完成、是否被取消等。这个状态管理确保了任务生命周期的正确管理，并且允许外部通过 `isDone()` 和 `isCancelled()` 方法查询任务状态。
 *
 * 3. **支持取消操作**：`FutureTask` 提供 `cancel(boolean mayInterruptIfRunning)` 方法来支持取消正在执行或尚未执行的任务。如果任务已经完成或已经取消，调用取消操作不会有任何效果。
 *
 * 4. **结果只计算一次**：`FutureTask` 确保任务的 `call()` 方法只被执行一次。这是通过内部同步控制实现的，无论多少个线程试图启动该任务，`call()` 方法都只会执行一次，从而保证了计算结果的一致性。
 *
 * 5. **支持等待任务完成**：通过 `get()` 方法，调用者可以等待异步任务完成并获取其结果。`get()` 方法还有一个重载版本，允许调用者设置最大等待时间，这提供了更灵活的等待机制。
 *
 * 6. **异常处理**：如果异步任务在执行过程中抛出异常，这个异常会被 `FutureTask` 捕获并保存。当调用 `get()` 方法时，如果任务已经因为异常而结束，则这个异常会被封装成一个 `ExecutionException` 抛出。这样，调用者就可以适当地处理异常情况。
 *
 * 7. **与线程池的良好配合**：`FutureTask` 可以很方便地提交给线程池 (`ExecutorService`) 执行。线程池可以管理多个 `FutureTask` 的执行，调度任务的执行，以及管理线程的生命周期，这大大简化了并发程序的编写。
 */
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
