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
 * UDP通信
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
        //配置服务端nio线程组
        EventLoopGroup group = new NioEventLoopGroup();//服务端接受客户端连接
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true).handler(new UDPServerHandler());
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(9999).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅的退出，释放线程池资源
            group.shutdownGracefully();
        }
    }

}
