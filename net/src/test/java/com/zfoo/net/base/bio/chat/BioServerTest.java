package com.zfoo.net.base.bio.chat;

import com.zfoo.protocol.util.IOUtils;
import com.zfoo.util.ThreadUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Ignore
public class BioServerTest {

    @Test
    public void bioTest() {
        new BioServerTest().launchServer();
        ThreadUtils.sleep(Long.MAX_VALUE);
    }

    private List<ServerChannel> listClient = new ArrayList<ServerChannel>();

    private class ServerChannel implements Runnable {
        // 发送数据
        private DataOutputStream dataOut;
        // 接受数据
        private DataInputStream dataIn;
        private String name;
        private boolean isRunning = true;

        public ServerChannel(Socket client) {
            try {
                dataOut = new DataOutputStream(client.getOutputStream());
                dataIn = new DataInputStream(client.getInputStream());
                this.name = dataIn.readUTF();
                System.out.println(name);
                send("欢迎你进入聊天室！");
                sendToAll(this.name + "进入聊天室！");

            } catch (IOException e) {
                quit();
            }

        }

        private String receive() {
            String message = null;

            try {
                message = dataIn.readUTF();
            } catch (IOException e) {
                quit();
            }
            return message;
        }

        private void send(String message) {
            if (message == null || message.equals(""))
                return;

            try {
                System.out.println(message);
                dataOut.writeUTF(message);
                dataOut.flush();
            } catch (IOException e) {
                quit();
            }

        }

        private void sendToAll(String message) {
            if (message.startsWith("@") && message.contains(":")) {
                String name = message.substring(1, message.indexOf(":"));
                String content = message.substring(message.indexOf(":") + 1);
                for (ServerChannel other : listClient) {
                    if (other.name.equals(name)) {
                        other.send(this.name + "悄悄对你说：" + content);
                    }
                }
            } else {

                for (ServerChannel other : listClient) {
                    if (other == this) {
                        continue;
                    }
                    other.send(message);
                }
            }
        }

        private void quit() {
            IOUtils.closeIO(dataOut);
            isRunning = false;
            listClient.remove(this);
        }

        @Override
        public void run() {
            while (isRunning) {
                sendToAll(receive());
            }
        }

    }

    public void launchServer() {
        // 1.穿件服务器，指定端口
        ServerSocket server = null;
        try {
            server = new ServerSocket(9999);
            while (true) {
                // 2.接受客户端连接，阻塞式
                Socket client = server.accept();
                ServerChannel serverChannel = new ServerChannel(client);
                // 3.发送数据+接受数据
                Thread clienThread = new Thread(serverChannel);
                listClient.add(serverChannel);
                clienThread.start();
                System.out.println("server started! Connect to client successfully");
            }

        } catch (IOException e) {
        }
    }

}
