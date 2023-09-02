package com.zfoo.net.base.aio.server;


import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

@Ignore
public class AioServerTest implements Runnable {

    @Test
    public void serverTest() {
        AioServerTest aioServer = new AioServerTest();
        aioServer.init();
        new Thread(aioServer, "server").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    private CountDownLatch latch = new CountDownLatch(1);

    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;


    public void init() {
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(9999));
            System.out.println("The Time server is start in port:" + 9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        asynchronousServerSocketChannel.accept(this, new AcceptCompletionHandler());
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public AsynchronousServerSocketChannel getAsynchronousServerSocketChannel() {
        return asynchronousServerSocketChannel;
    }

    public void setAsynchronousServerSocketChannel(AsynchronousServerSocketChannel asynchronousServerSocketChannel) {
        this.asynchronousServerSocketChannel = asynchronousServerSocketChannel;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

}
