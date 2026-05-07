package com.zfoo.net.base.bio;

import com.zfoo.protocol.util.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


@Ignore
public class TcpTest {

    @Test
    public void serverTest() throws IOException {
        // 1. Create server socket and bind to port
        ServerSocket server = null;
        BufferedWriter bufferedWriter = null;
        try {
            server = new ServerSocket(9999);
            // 2. Accept incoming client connection (blocking)
            Socket socket = server.accept();

            // 3. Send and receive data
            String message = "welocme to internet!!!";
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedWriter.write(message);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } finally {
            IOUtils.closeIO(bufferedWriter, server);
        }
    }

    @Test
    public void clientTest() throws IOException {
        // 1. Create client socket; connecting to server immediately
        Socket client = null;
        BufferedReader bufferedReader = null;

        try {
            // Within the same protocol (TCP), ports must be unique; TCP and UDP can share a port
            // Ports are 2-byte unsigned; ports below 1024 are system-reserved
            client = new Socket("localhost", 9999);

            // 2. Receive data
            bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String receiveMes = bufferedReader.readLine();
            System.out.println(receiveMes);

            // 3. Send data
            client.getOutputStream().write(new String("This is client!").getBytes());
            client.getOutputStream().flush();
        } finally {
            IOUtils.closeIO(client.getOutputStream(), client);
        }
    }
}
