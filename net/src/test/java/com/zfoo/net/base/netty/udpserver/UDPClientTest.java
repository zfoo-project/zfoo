package com.zfoo.net.base.netty.udpserver;

import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;

/**
 * @author godotg
 * @version 1.0
 * @since 2017 05.27 17:48
 */
@Ignore
public class UDPClientTest {

    @Test
    public void clientTest() {
        var client = new UDPClientTest();
        client.init();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void init() {
        // Configure single-thread NIO event loop
        EventLoopGroup group = new NioEventLoopGroup(); // Single-thread NIO event loop
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true).handler(new UDPClientHandler());
            // Bind to a random local port
            ChannelFuture future = bootstrap.bind(0).sync();
            // Get the channel and send data
            Channel channel = future.channel();

            // Broadcast a UDP message to all machines on the local network
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("client"
                    , CharsetUtil.UTF_8), new InetSocketAddress("127.0.0.1", 9999))).sync();

            channel.closeFuture().await(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Graceful shutdown: release thread pool resources
            group.shutdownGracefully();
        }
    }

}
