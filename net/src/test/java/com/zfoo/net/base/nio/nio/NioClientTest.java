package com.zfoo.net.base.nio.nio;

import com.zfoo.protocol.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Ignore
public class NioClientTest implements Runnable {

    @Test
    public void clientTest() {
        var nioClient = new NioClientTest();
        nioClient.init();
        new Thread(nioClient, "clinetThread").start();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }


    private Selector selector;
    private SocketChannel channel;
    private volatile boolean stop;


    public void init() {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("localhost", 9999));

            this.selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Abnormal exit");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("Client started successfully!");
        while (!stop) {
            // non-blocking
            try {
                selector.select(1001);
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
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
                throw new RuntimeException(e);
            }
        }
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
            // Connection event triggered
            if (key.isConnectable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                // If still connecting, finalize the connection
                if (channel.finishConnect()) {
                    channel.finishConnect();
                }
                channel.configureBlocking(false);
                channel.write(ByteBuffer.wrap(new String("hello server!this is client!").getBytes()));
                channel.register(selector, SelectionKey.OP_READ);
            }

            if (key.isReadable()) {
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = channel.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] data = new byte[readBuffer.remaining()];
                    readBuffer.get(data);
                    String message = new String(data, "UTF-8").trim();
                    System.out.println("Client received message: " + message);
                    // Echo message back to server
                    ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
                    writeBuffer.flip();
                    channel.write(writeBuffer);
                } else if (readBytes < 0) { // Link closed
                    key.cancel();
                    channel.close();
                } else {
                    // 0 bytes read, ignore
                }
            }
        }

        if (key.isConnectable()) {
            // a connection was established with a remote server.

        }

        if (key.isWritable()) {
            // a channel is ready for writing
        }
    }

}
