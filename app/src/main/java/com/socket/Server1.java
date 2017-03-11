package com.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lqm on 2016/9/19.
 */
public class Server1 {
    public static void main(String[] args) {
        int port = 65432;
        try {
            //创建一个serversocket对象，服务端专用
            ServerSocket server = new ServerSocket(port);
            System.out.println("服务器开始监听.....");
            //accept()阻塞监听返回socket

            //跟客户端建立好连接之后，我们就可以获取socket的InputStream，并从中读取客户端发过来的信息了。
            while (true) {
                Socket socket = server.accept();
                new Thread(new Task(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static class Task implements Runnable {

        private Socket socket;

        public Task(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Reader reader = null;
            try {
                reader = new InputStreamReader(socket.getInputStream());

                char chars[] = new char[64];
                int len;
                StringBuilder sb = new StringBuilder();
                String temp;
                int index;
                while ((len = reader.read(chars)) != -1) {
                    temp = new String(chars, 0, len);
                    if ((index = temp.indexOf("eof")) != -1) {//遇到eof时就结束接收
                        sb.append(temp.substring(0, index));
                        break;
                    }
                    sb.append(temp);
                }
                System.out.println("from client " + socket.getInetAddress().getHostName() + ":" + sb);
                //读完后写一句
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write("Hello Client.");
                writer.write("eof\n");
                writer.flush();
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
