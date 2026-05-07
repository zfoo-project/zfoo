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

    // Multiplexer (Selector): one-to-many
    private Selector selector;

    // 1. Obtain a ServerSocketChannel for listening; it is the parent channel for all client connections
    private ServerSocketChannel serverChannel;

    private volatile boolean stop;

    public void init(int port) {
        try {
            serverChannel = ServerSocketChannel.open();
            // 2. Bind to the port and set the channel to non-blocking mode
            serverChannel.socket().bind(new InetSocketAddress("127.0.0.1", port));
            serverChannel.configureBlocking(false);
            // 3. Create a Selector (multiplexer)
            this.selector = Selector.open();
            // 4. Register the channel with the Selector for OP_ACCEPT events
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Abnormal exit");
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("Server started successfully!");
        // 5. Poll the Selector for ready keys
        while (!stop) {
            try {
                // Block until at least one event arrives; selector.select() returns when events are ready
                selector.select();
                // Iterate over the selected keys (ready events)
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    // Remove processed key to prevent duplicate handling
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
                e.printStackTrace();
            }
        }
        // After the Selector is closed, all registered Channels and Pipes are automatically
        // de-registered and closed, so no need to release them again
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
            if (key.isAcceptable()) { // Client connection request event
                // 6. Multiplexer detected new client; complete TCP three-way handshake and establish connection
                ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                SocketChannel channel = serverChannel.accept();
                // 7. Set the client channel to non-blocking mode
                channel.configureBlocking(false);
                // Send a greeting to the client
                channel.write(ByteBuffer.wrap(new String("Hello client! This is server!").getBytes()));
                // 8. Register client channel with the Selector for OP_READ to receive future messages
                channel.register(selector, SelectionKey.OP_READ);
            }

            if (key.isConnectable()) {
                // a connection was established with a remote server.
            }

            if (key.isReadable()) {
                // Server can read data: retrieve the triggered SocketChannel
                SocketChannel channel = (SocketChannel) key.channel();
                // Allocate the read buffer
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int readBytes = channel.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip(); // limit=position, position=0
                    byte[] data = new byte[readBuffer.remaining()];
                    readBuffer.get(data);
                    String message = new String(data).trim();
                    System.out.println("Server received message: " + message);

                    // Reply with current time
                    ByteBuffer writeBuffer = ByteBuffer.wrap(
                            String.format("Beijing time: " + new Date().toString()).getBytes());
                    channel.write(writeBuffer);
                } else if (readBytes < 0) { // Link closed
                    key.cancel();
                    channel.close();
                } else {
                    // 0 bytes read, ignore
                }
            }

            if (key.isWritable()) {
                // a channel is ready for writing
            }
        }
    }

}
