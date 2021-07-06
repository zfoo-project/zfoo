package com.zfoo.net.base.aio.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

public class ConnectCompletionHandler implements CompletionHandler<Void, ConnectCompletionHandler> {

    private AsynchronousSocketChannel client;
    private CountDownLatch latch;

    public ConnectCompletionHandler(AsynchronousSocketChannel channel, CountDownLatch latch) {
        this.client = channel;
        this.latch = latch;
    }

    @Override
    public void completed(Void result, ConnectCompletionHandler attachment) {
        byte[] req = "QUERY TIME ORDER!".getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        client.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    client.write(buffer, buffer, this);
                } else {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    client.read(readBuffer, readBuffer, new ReadCompletionHandler(client, latch));
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
        });
    }

    @Override
    public void failed(Throwable exc, ConnectCompletionHandler attachment) {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
