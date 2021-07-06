package com.zfoo.net.base.netty.timeserver1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private int count;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        byte[] req = new byte[buffer.readableBytes()];
        buffer.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("The time server receive order :" + body + count++);
        String currentTime = "QUERY TIME ORDER" + new Date().toString();
        ByteBuf writeBuffer = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(writeBuffer);
        System.out.println(count++);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
