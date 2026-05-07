package com.zfoo.net.base.netty.nettyfileserver;

import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Netty small file transfer server using Java's native RandomAccessFile.
 *
 * @author godotg
 * @version 1.0
 * @since 2017 05.31 09:49
 */
@Ignore
public class FileServerTest {

    @Test
    public void serverTest() {
        var server = new FileServerTest();
        server.init();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void init() {
        // Configure server-side NIO thread groups
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // Accept client connections
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // Handle SocketChannel network I/O
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChildChannelHandler());

            // Bind port and wait synchronously until the bind succeeds
            ChannelFuture future = bootstrap.bind(9999).sync();
            // Wait until the server socket is closed
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Graceful shutdown: release thread pool resources
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
            channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            channel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8)); // Together these three handlers form a text line codec
            channel.pipeline().addLast(new FileServerHandler());
        }
    }

}
