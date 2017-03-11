package com.nercms.audio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;


public class AudioSender implements Runnable {
    String LOG = "AudioSender ";

    private boolean isSendering = false;
    private List<AudioData2> dataList;
    DatagramSocket socket;
    DatagramPacket dataPacket;
    private InetAddress ip;
    private int port;

    public AudioSender(String remote_ip) {
        dataList = Collections.synchronizedList(new LinkedList<AudioData2>());
        try {
            try {
                Log.e(LOG, "remote_ip:" + remote_ip);
                ip = InetAddress.getByName(NetConfig.SERVER_HOST);
                Log.e(LOG, "服务端地址是 " + ip.toString());
                port = NetConfig.SERVER_PORT;
                socket = new DatagramSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void addData(byte[] data, int size) {
        AudioData2 encodedData = new AudioData2();
        encodedData.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
//      System.out.println("tempData:"+Arrays.toString(tempData));
        encodedData.setRealData(tempData);
        dataList.add(encodedData);
    }

    /*
     * send data to server
     */
    private void sendData(byte[] data, int size) {
        try {
            dataPacket = new DatagramPacket(data, size, ip, port);
            dataPacket.setData(data);
            Log.e(LOG, "发送一段数据 " + data.length + ":" + size);
            socket.send(dataPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * start sending data
     */
    public void startSending() {
        new Thread(this).start();
    }

    /*
     * stop sending data
     */
    public void stopSending() {
        this.isSendering = false;
    }

    // run
    public void run() {
        this.isSendering = true;
        System.out.println(LOG + "start....");
        while (isSendering) {
            if (dataList.size() > 0) {
                AudioData2 encodedData = dataList.remove(0);
                sendData(encodedData.getRealData(), encodedData.getSize());
            }
        }
        System.out.println(LOG + "stop!!!!");
    }
}