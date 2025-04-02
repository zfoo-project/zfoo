package com.zfoo.event;

import com.zfoo.event.anno.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class BenchmarkController {


    private static final Logger logger = LoggerFactory.getLogger(com.zfoo.event.BenchmarkController.class);
    private static final AtomicLong counter = new AtomicLong();

    // 事件会被当前线程立刻执行，注意日志打印的线程号
    @EventReceiver
    public void onBenchmarkEvent(BenchmarkEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("我");
        builder.append("我");
        builder.append("我");
        long c = counter.incrementAndGet();
        if(c % 10000000 == 0) {
            System.err.println("c:" + c + " , builder:" + builder.toString());
        }
    }


}
