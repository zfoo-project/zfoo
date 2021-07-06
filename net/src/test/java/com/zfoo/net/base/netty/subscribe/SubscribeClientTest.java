package com.zfoo.net.base.netty.subscribe;

import com.zfoo.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.23 18:02
 */
public class SubscribeClientTest {


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
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new ObjectDecoder(1024 * 1024
                    , ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
            channel.pipeline().addLast(new ObjectEncoder());
            channel.pipeline().addLast(new ClientHandler());
        }
    }

    @Ignore
    @Test
    public void clientTest() {
        new SubscribeClientTest().connect(9999, "127.0.0.1");
        System.out.println("hello");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
