package com.zfoo.net.base.netty.echoserver1;

import com.zfoo.protocol.util.StringUtils;
import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EchoClientTest {

    @Test
    public void clientTest() {
        var client = new EchoClientTest();
        client.connect(9999, "127.0.0.1");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void connect(int port, String host) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChildChannelHandler());
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            ByteBuf delimiter = Unpooled.copiedBuffer(StringUtils.DOLLAR.getBytes());
            channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
            channel.pipeline().addLast(new StringDecoder());
            channel.pipeline().addLast(new EchoClientHandler());
        }
    }

}
