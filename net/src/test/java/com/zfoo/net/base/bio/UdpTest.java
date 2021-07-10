package com.zfoo.net.base.bio;

import com.zfoo.protocol.util.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

@Ignore
public class UdpTest {

    @Test
    public void serverTest() throws IOException {
        // 1.创建服务端+端口
        DatagramSocket server = null;
        try {
            server = new DatagramSocket(8000);
            // 2.准备接收器
            byte[] container = new byte[1024];
            // 3.封装成包DatagramPacket
            DatagramPacket packet = new DatagramPacket(container,
                    container.length);
            // 4.接收数据，接收数据可以阻塞
            server.receive(packet);
            // 分析数据
            byte[] data = packet.getData();
            int length = packet.getLength();
            System.out.println(new String(data, 0, length));
        } finally {
            // 5.释放资源
            server.close();
        }

    }

    @Test
    public void clientTest() throws IOException {
        // 1.创建客户端+端口
        DatagramSocket client = null;
        try {
            client = new DatagramSocket(6666);
            // 2.准备数据
            String message = "udp 编程";
            byte[] data = message.getBytes();
            // 3.打包DatagramPacket,发送的地点及端口
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    new InetSocketAddress("localhost", 8000));
            // 4.发送
            client.send(packet);
            // 5.释放资源
            client.close();
        } finally {
            IOUtils.closeIO(client);
        }
    }

}
