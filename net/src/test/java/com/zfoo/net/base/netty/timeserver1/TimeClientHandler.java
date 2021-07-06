package com.zfoo.net.base.netty.timeserver1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {


    public TimeClientHandler() {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf firstMessage;
        byte[] req = "QUERY TIME ORDER".getBytes();
        for (int i = 0; i < 100; i++) {
            firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            ctx.writeAndFlush(firstMessage);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf readBuffer = (ByteBuf) msg;
        byte[] read = new byte[readBuffer.readableBytes()];
        readBuffer.readBytes(read);
        String body = new String(read, "UTF-8");
        System.out.println("Now is :" + body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
