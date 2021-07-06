package com.zfoo.net.base.aio.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AioServerTest> {
    @Override
    public void completed(AsynchronousSocketChannel result, AioServerTest attachment) {
        attachment.getAsynchronousServerSocketChannel().accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AioServerTest attachment) {
        exc.printStackTrace();
        attachment.getLatch().countDown();
    }
}
