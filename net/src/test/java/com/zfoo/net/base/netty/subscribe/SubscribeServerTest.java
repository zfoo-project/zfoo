package com.zfoo.net.base.netty.subscribe;

import com.zfoo.protocol.util.ThreadUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 对象读写序列化通信
 *
 * @author godotg
 * @version 1.0
 * @since 2017 05.23 16:51
 */
@Ignore
public class SubscribeServerTest {

    @Test
    public void serverTest() {
        var server = new SubscribeServerTest();
        server.init();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    public void init() {
        //配置服务端nio线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();//服务端接受客户端连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();//SocketChannel的网络读写
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChildChannelHandler());

            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(9999).sync();
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
            channel.pipeline().addLast(new ObjectDecoder(1024 * 1024
                    , ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
            //*****一定要实现Serializable，不实现不会抛异常，很难查找bug
            channel.pipeline().addLast(new ObjectEncoder());
            channel.pipeline().addLast(new Serverhandler());
        }
    }

}
