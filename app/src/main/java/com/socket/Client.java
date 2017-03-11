package com.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by lqm on 2016/9/19.
 */
public class Client {


    private Socket socket;

    public static void main(String[] args) throws IOException {
        String serverIp = "127.0.0.1";
        int port = 65432;
        Client client = new Client(serverIp, port);
        client.start();
    }


    public Client(String serverIp, int port) {
        this.serverIp = serverIp;
        this.port = port;
    }


    private String serverIp;
    private int port;
    private boolean running = false;
    private long lastSentTime;

    private void start() throws IOException {
        if (running) return;
        socket = new Socket(serverIp, port);
        lastSentTime = System.currentTimeMillis();
        running = true;
        new Thread(new KeepAliveWatchDog()).start();
        new Thread(new ReceiveWatchDog()).start();
    }


    public void stop() {
        if (running) running = false;
    }


    public void sendObject(Object obj) throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        oos.writeObject(obj);
        System.out.println("发送:\t" + obj);
        oos.flush();
    }


    //用于发送的
    class KeepAliveWatchDog implements Runnable {

        long checkDelay = 10;
        long keepAliveDelay = 30000;


        @Override
        public void run() {
            while (running) {
                if (System.currentTimeMillis() - lastSentTime > keepAliveDelay) {
                    try {
                        Client.this.sendObject(new KeepAlive());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    lastSentTime = System.currentTimeMillis();
                } else {
                    try {
                        Thread.sleep(checkDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //用于接收的
    class ReceiveWatchDog implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    InputStream in = socket.getInputStream();
                    if (in.available() > 0) {
                        ObjectInputStream ois = new ObjectInputStream(in);
                        Object ovj = ois.readObject();
                        System.out.println("接收:\t" + ovj);
                    } else {
                        Thread.sleep(10);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
