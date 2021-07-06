package com.zfoo.net.base.netty.timeserver2;

import com.zfoo.protocol.util.FileUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

public class TimeServerHandler extends ChannelInboundHandlerAdapter {

    private int count;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("The time server receive order :" + body + count++);
        String currentTime = "QUERY TIME ORDER" + new Date().toString() + FileUtils.LS;
        ByteBuf writeBuffer = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(writeBuffer);
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
