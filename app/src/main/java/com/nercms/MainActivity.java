package com.nercms;

import android.content.Intent;
import android.media.AudioRecord;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    EditText content;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        content = (EditText) findViewById(R.id.content);
    }

    public void doStart(View v) {
        String ip = editText.getText().toString();
        Intent intent = new Intent(this, VideoChatActivity.class);
        intent.putExtra("remote_ip", ip);
        intent.putExtra("remote_port", 19888);
        startActivity(intent);
    }


    public void client(View v) {
        new Thread(new ClientTask()).start();
    }


    class ClientTask implements Runnable {

        @Override
        public void run() {
            System.out.println("客户端启动");
            String host = "192.168.1.107";
            int port = 65432;
            try {
                Socket client = new Socket(host, port);
//建立连接后就可以往服务端写数据了
                Writer writer = new OutputStreamWriter(client.getOutputStream());
                writer.write(content.getText().toString().length() <= 0 ? "Hello Server!" : content.getText().toString());
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
