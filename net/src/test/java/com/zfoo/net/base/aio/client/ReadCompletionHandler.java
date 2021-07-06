package com.zfoo.net.base.aio.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2017/5/19 0019.
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private AsynchronousSocketChannel client;
    private CountDownLatch latch;

    public ReadCompletionHandler(AsynchronousSocketChannel channel, CountDownLatch latch) {
        this.client = channel;
        this.latch = latch;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] bytes = new byte[attachment.remaining()];
        attachment.get(bytes);
        String body;
        try {
            body = new String(bytes, "UTF-8");
            System.out.println("Now is :" + body);
            latch.countDown();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
