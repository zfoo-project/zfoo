package com.zfoo.net.base.netty.echoserver2;

import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EchoClientTest {

    @Test
    public void clientTest() {
        var client = new EchoClientTest();
        client.connect(9999, "127.0.0.1");
        System.out.println("hello");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void connect(int port, String host) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class)
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
            channel.pipeline().addLast(new FixedLengthFrameDecoder(20));
            channel.pipeline().addLast(new StringDecoder());
            channel.pipeline().addLast(new EchoClientHandler());
        }
    }

}
