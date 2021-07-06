package com.zfoo.net.base.netty.udpserver;

import com.zfoo.util.ThreadUtils;
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
 * @author jaysunxiao
 * @version 1.0
 * @since 2017 05.27 17:48
 */
@Ignore
public class UDPClientTest {

    @Test
    public void clientTest() {
        System.out.println("hello");
        UDPClientTest server = new UDPClientTest(9999);
        server.init();
        System.out.println("hello");
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    private int port;

    public UDPClientTest(int port) {
        this.port = port;
    }

    public void init() {
        //配置服务端nio线程组
        EventLoopGroup group = new NioEventLoopGroup();//服务端接受客户端连接
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true).handler(new UDPClientHandler());
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(0).sync();
            //等待服务端监听端口关闭
            Channel channel = future.channel();

            //向内网的所有机器广播UDP消息
            channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("client"
                    , CharsetUtil.UTF_8), new InetSocketAddress("127.0.0.1", port))).sync();

            if (!channel.closeFuture().await(5000)) {
                System.out.println("查询超时！");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //优雅的退出，释放线程池资源
            group.shutdownGracefully();
        }
    }

}
