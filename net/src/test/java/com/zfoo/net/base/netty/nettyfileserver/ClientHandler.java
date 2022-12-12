package com.zfoo.net.base.netty.nettyfileserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author godotg
 * @version 1.0
 * @since 2017 05.31 10:22
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private String filePath;


    public ClientHandler(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        byte[] req = new String(filePath + System.getProperty("line.separator")).getBytes();
        ByteBuf message = Unpooled.buffer(req.length);
        message.writeBytes(req);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println(body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

