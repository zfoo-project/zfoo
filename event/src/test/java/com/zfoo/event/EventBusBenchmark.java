package com.zfoo.event;


import com.zfoo.event.manager.EventBus;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.TimeUnit;


/**
 * Benchmark                                   Mode     Cnt         Score         Error  Units
 * EventBusBenchmark.postEvent                thrpt       5  48596988.367 ± 1980655.481  ops/s
 */
@BenchmarkMode(Mode.Throughput) // 测试吞吐量（单位时间执行次数）
@OutputTimeUnit(TimeUnit.SECONDS) // 结果单位为秒
@Warmup(iterations = 5, time = 1) // 预热3轮，每轮1秒
@Measurement(iterations = 5, time = 1) // 正式测试5轮，每轮1秒
@Fork(1) // 单进程测试
@State(Scope.Benchmark) // 共享测试状态
public class EventBusBenchmark {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(EventBusBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    private ClassPathXmlApplicationContext context;
    private BenchmarkEvent event;

    @Setup(Level.Trial) // 整个测试前初始化
    public void init() {
        context = new ClassPathXmlApplicationContext("application.xml");
        event = BenchmarkEvent.valueOf("JMH性能测试事件");
    }

    @Benchmark
//    @Threads(4) // 模拟4线程并发
    public void postEvent() {
        EventBus.post(event);
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime) // 采样执行时间分布
    public void asyncPostEvent() {
        EventBus.post(event); // 假设存在异步方法
    }

    @TearDown(Level.Trial) // 测试结束后清理
    public void close() {
        context.close();
    }

}
