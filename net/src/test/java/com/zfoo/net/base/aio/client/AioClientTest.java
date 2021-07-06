package com.zfoo.net.base.aio.client;


import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AioClientTest implements Runnable {

    private AsynchronousSocketChannel client;
    private String host;
    private int port;
    private CountDownLatch latch;

    public AioClientTest(String host, int port, int latchNum) {
        this.host = host;
        this.port = port;
        this.latch = new CountDownLatch(latchNum);
    }

    public void init() {
        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        ConnectCompletionHandler handler = new ConnectCompletionHandler(client, latch);
        client.connect(new InetSocketAddress(host, port), handler, handler);
        try {
            latch.await();
            client.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void clientTest() {
        AioClientTest aioClient = new AioClientTest("127.0.0.1", 9999, 1);
        aioClient.init();
        new Thread(aioClient, "client").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
