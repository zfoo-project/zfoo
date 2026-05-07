package com.zfoo.net.base.netty.udpserver;

import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.junit.Ignore;
import org.junit.Test;

/**
 * UDP communication test
 *
 * @author godotg
 * @version 1.0
 * @since 2017 05.27 17:24
 */
@Ignore
public class UDPServerTest {

    @Test
    public void serverTest() {
        var server = new UDPServerTest();
        server.init();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void init() {
        // Configure single-thread NIO event loop
        EventLoopGroup group = new NioEventLoopGroup(); // Single-thread NIO event loop
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true).handler(new UDPServerHandler());
            // Bind port and wait synchronously until the bind succeeds
            ChannelFuture future = bootstrap.bind(9999).sync();
            // Wait until the server socket is closed
            future.channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Graceful shutdown: release thread pool resources
            group.shutdownGracefully();
        }
    }

}
