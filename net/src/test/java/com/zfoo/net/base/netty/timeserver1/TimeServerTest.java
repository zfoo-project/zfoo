package com.zfoo.net.base.netty.timeserver1;

import com.zfoo.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 没有考虑到半包读写的服务器
 *
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.22 18:23
 */
public class TimeServerTest {

    private int port;

    public TimeServerTest(int port) {
        this.port = port;
    }

    public void init() {
        //配置服务端nio线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();//服务端接受客户端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//SocketChannel的网络读写
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChildChannelHandler());
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅的退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new TimeServerHandler());
        }
    }

    @Ignore
    @Test
    public void serverTest() {
        TimeServerTest server = new TimeServerTest(9999);
        server.init();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

}
