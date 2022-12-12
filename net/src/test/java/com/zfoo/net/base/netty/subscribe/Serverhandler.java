package com.zfoo.net.base.netty.subscribe;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author godotg
 * @version 1.0
 * @since 2017 05.23 17:13
 */
public class Serverhandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SubscribeReq req = (SubscribeReq) msg;
        System.out.println("Server accept client subscribe req: " + req.toString());
        SubscribeResp resp = new SubscribeResp(req.getReqID(), 0, "subscribe successfully!");
        //*****一定要实现Serializable，不实现不会抛异常，很难查找bug
        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
