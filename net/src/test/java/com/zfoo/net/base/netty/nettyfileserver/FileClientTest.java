package com.zfoo.net.base.netty.nettyfileserver;

import com.zfoo.util.ThreadUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.31 10:22
 */
@Ignore
public class FileClientTest {

    @Test
    public void clientTest() {
        FileClientTest client = new FileClientTest(9999, "127.0.0.1", "rainbow.txt");
        client.connect();
        System.out.println("hello");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    private int port;
    private String host;
    private String filePath;

    public FileClientTest(int port, String host, String filePath) {
        this.port = port;
        this.host = host;
        this.filePath = filePath;
    }

    public void connect() {
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
            channel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
            channel.pipeline().addLast(new LineBasedFrameDecoder(1024));
            channel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));//三者组合起来就是文本换行编码解码器
            channel.pipeline().addLast(new ClientHandler(filePath));
        }
    }


}
