package com.zfoo.net.base.netty.udpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.27 17:27
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static String DICTIONARY = "aaa";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String req = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println(req);
        if (req.equals("client")) {
            ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(UDPServerHandler.DICTIONARY, CharsetUtil.UTF_8)
                    , packet.sender()));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }
}
