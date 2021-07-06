package com.zfoo.net.base.netty.udpserver;

import com.zfoo.util.ThreadUtils;
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
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.27 17:24
 */
@Ignore
public class UDPServerTest {

    @Test
    public void serverTest() {
        System.out.println("hello");
        UDPServerTest server = new UDPServerTest(9999);
        server.init();
        System.out.println("hello");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    private int port;

    public UDPServerTest(int port) {
        this.port = port;
    }

    public void init() {
        //配置服务端nio线程组
        EventLoopGroup group = new NioEventLoopGroup();//服务端接受客户端连接
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true).handler(new UDPServerHandler());
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
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
