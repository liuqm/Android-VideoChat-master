package com.nercms.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;


/**
 * 音频编码器
 *
 * @author lqm
 */
public class AudioEncoder implements Runnable {
    String LOG = "AudioEncoder";
    //单例模式构造对象
    private static AudioEncoder encoder;
    //是否正在编码
    private boolean isEncoding = false;


    //每一帧的音频数据的集合
    private List<AudioData> dataList = null;
    private String ip;

    public static AudioEncoder getInstance() {
        if (encoder == null) {
            encoder = new AudioEncoder();
        }
        return encoder;
    }

    private AudioEncoder() {
        dataList = Collections.synchronizedList(new LinkedList<AudioData>());
    }

    public void addData(short[] data, int size) {
        AudioData rawData = new AudioData();
        rawData.setSize(size);
        short[] tempData = new short[size];
        System.arraycopy(data, 0, tempData, 0, size);
        rawData.setRealData(tempData);
        dataList.add(rawData);
    }

    /*
     * start encoding 开始编码
     */
    public void startEncoding(String remote_ip) {
        System.out.println(LOG + "start encode thread");

        ip = remote_ip;
        if (isEncoding) {
            Log.e(LOG, "encoder has been started  !!!");
            return;
        }
        //开子线程
        new Thread(this).start();
    }

    /*
     * end encoding	停止编码
     */
    public void stopEncoding() {
        this.isEncoding = false;
    }

    public void run() {
        // start sender before encoder
        AudioSender sender = new AudioSender(ip);
        sender.startSending();

        int encodeSize = 0;
        byte[] encodedData;

        isEncoding = true;
        while (isEncoding) {
            if (dataList.size() == 0) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (isEncoding) {
                AudioData rawData = dataList.remove(0);
                encodedData = new byte[Speex.getInstance().getFrameSize()];

                encodeSize = Speex.getInstance().encode(rawData.getRealData(),
                        0, encodedData, rawData.getSize());
                System.out.println();
                if (encodeSize > 0) {
                    sender.addData(encodedData, encodeSize);
                }
            }
        }
        System.out.println(LOG + "end encoding");
        sender.stopSending();
    }

}