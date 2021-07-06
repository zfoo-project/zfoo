package com.zfoo.net.base.aio.server;


import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AioServerTest implements Runnable {

    private int port;

    private CountDownLatch latch;

    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AioServerTest(int port, int latchNum) {
        this.port = port;
        latch = new CountDownLatch(latchNum);
    }

    public void init() {
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
            System.out.println("The Time server is start in port:" + port);
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

    @Ignore
    @Test
    public void serverTest() {
        AioServerTest aioServer = new AioServerTest(9999, 1);
        aioServer.init();
        new Thread(aioServer, "server").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
