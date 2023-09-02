package com.zfoo.net.base.nio.nio;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

@Ignore
public class NioServerTest implements Runnable {

    @Test
    public void serverTest() {
        var nioServer = new NioServerTest();
        nioServer.init(9999);
        new Thread(nioServer, "serverThread").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    //多路复用器，一对多
    private Selector selector;

    //1.获得一个ServerSocket通道，用于监听客户端的连接，它是所有客户端连接的父管道
    private ServerSocketChannel serverChannel;

    private volatile boolean stop;

    public void init(int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            //2.绑定到port端口，设置通道为非阻塞
            //serverChannel.socket().bind(new InetSocketAddress(port));
            serverChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
            serverChannel.configureBlocking(false);
            //3.创建多路复用器
            this.selector = Selector.open();
            //4.多路复用器和通道绑定，并注册绑定事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("非正常退出");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("服务器启动成功！");
        //5.采用轮询的方式监听selector上是否有需要处理的事件(Key)
        while (!stop) {
            try {
                //事件到达selector.open()会返回；否则一直阻塞
                selector.select();
                //获得selector中选中的项的迭代器，选中的项为注册的事件
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();//删除已选的key，防止重复处理
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭，所以不需要重复释放资源
        if (selector != null) {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleInput(SelectionKey key) throws IOException {
        if (key.isValid()) {
            if (key.isAcceptable()) { //客户请求连接事件
                // 6.多路复用器监听到有新的客户端接入，处理新的接入请求，完成TCP三次握手，建立物理连接
                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                SocketChannel channel = serverChannel.accept();
                //7.设置服务端和客户端的连接为非阻塞模式，获得和客户端连接的通道
                channel.configureBlocking(false);
                //给客户端发送消息
                channel.write(ByteBuffer.wrap(new String("Hello client!This is server!").getBytes()));
                //8.将客户端链路注册到多路复用器上，和客户端连接成功后，为了接受到客户端的消息，需要给通道设置读权限
                channel.register(selector, SelectionKey.OP_READ);
            }

            if (key.isConnectable()) {
                // a connection was established with a remote server.
            }

            if (key.isReadable()) {
                //服务器可读取消息：得到事件发生的Socket通道
                SocketChannel channel = (SocketChannel) key.channel();
                //创建读取的缓冲区
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = channel.read(readBuffer);
                if (readBytes > 0) {
                    //接受
                    readBuffer.flip();//limit=position, position=0
                    byte[] data = new byte[readBuffer.remaining()];
                    readBuffer.get(data);
                    String message = new String(data).trim();
                    System.out.println("服务器收到消息:" + message);

                    //发送
                    ByteBuffer writeBuffer = ByteBuffer.wrap(String.format("北京时间：" + new Date().toString()).getBytes());//将消息返回给客户端
                    channel.write(writeBuffer);
                } else if (readBytes < 0) {//对链路关闭
                    key.cancel();
                    channel.close();
                } else {
                    //读到0字节忽略
                }
            }

            if (key.isWritable()) {
                // a channel is ready for writing
            }
        }
    }

}
