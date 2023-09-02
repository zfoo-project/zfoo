package com.zfoo.net.base.netty.subscribe;

import com.zfoo.protocol.util.ThreadUtils;
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
 * @author godotg
 * @version 1.0
 * @since 2017 05.23 18:02
 */
@Ignore
public class SubscribeClientTest {

    @Test
    public void clientTest() {
        var client = new SubscribeClientTest();
        client.connect(9999, "127.0.0.1");
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

}
