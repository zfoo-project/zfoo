package com.zfoo.net.base.aio.client;


import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CountDownLatch;

@Ignore
public class AioClientTest implements Runnable {

    @Test
    public void clientTest() {
        var aioClient = new AioClientTest();
        aioClient.init();
        new Thread(aioClient, "client").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    private AsynchronousSocketChannel client;
    private CountDownLatch latch = new CountDownLatch(1);

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
        client.connect(new InetSocketAddress("127.0.0.1", 9999), handler, handler);
        try {
            latch.await();
            client.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
