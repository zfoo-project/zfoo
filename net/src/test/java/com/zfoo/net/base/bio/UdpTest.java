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
        // 1. Create server socket and bind to port
        DatagramSocket server = null;
        try {
            server = new DatagramSocket(8000);
            // 2. Prepare the receive buffer
            byte[] container = new byte[1024];
            // 3. Wrap buffer in a DatagramPacket
            DatagramPacket packet = new DatagramPacket(container,
                    container.length);
            // 4. Receive data (blocking call)
            server.receive(packet);
            // Process received data
            byte[] data = packet.getData();
            int length = packet.getLength();
            System.out.println(new String(data, 0, length));
        } finally {
            // 5. Release resources
            server.close();
        }

    }

    @Test
    public void clientTest() throws IOException {
        // 1. Create client socket
        DatagramSocket client = null;
        try {
            client = new DatagramSocket(6666);
            // 2. Prepare data to send
            String message = "UDP programming";
            byte[] data = message.getBytes();
            // 3. Pack into a DatagramPacket with destination address and port
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    new InetSocketAddress("localhost", 8000));
            // 4. Send the packet
            client.send(packet);
            // 5. Release resources
            client.close();
        } finally {
            IOUtils.closeIO(client);
        }
    }

}
