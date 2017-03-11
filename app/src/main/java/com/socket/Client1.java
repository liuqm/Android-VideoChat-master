package com.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

/**
 * Created by lqm on 2016/9/19.
 */
public class Client1 {

    public static void main(String[] args) throws IOException {
        String host = "192.168.1.107";
        int post = 65432;
        Socket client = new Socket(host, post);
        //建立连接后就可以往服务端写数据了
        Writer writer = new OutputStreamWriter(client.getOutputStream());
        writer.write("Hello Server.");
        writer.write("eof");
        writer.flush();
        //写完以后进行读操作
        Reader reader = new InputStreamReader(client.getInputStream());
        char chars[] = new char[64];
        int len;
        StringBuffer sb = new StringBuffer();
        String temp;
        int index;
        while ((len = reader.read(chars)) != -1) {
            temp = new String(chars, 0, len);
            if ((index = temp.indexOf("eof")) != -1) {
                sb.append(temp.substring(0, index));
                break;
            }
            sb.append(new String(chars, 0, len));
        }
        System.out.println("from server: " + sb);
        //写完后要记得flush
        writer.close();
        reader.close();
        client.close();
    }

}
