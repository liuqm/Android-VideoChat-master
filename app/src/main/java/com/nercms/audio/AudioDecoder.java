package com.nercms.audio;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

import com.gyz.voipdemo_speex.util.Speex;


public class AudioDecoder implements Runnable {

    String LOG = "AudioDecoder";
    private static AudioDecoder decoder;

    private static final int MAX_BUFFER_SIZE = 2048;

    private short[] decodedData;// data of decoded
    private boolean isDecoding = false;
    private List<AudioData2> dataList = null;

    public static AudioDecoder getInstance() {
        if (decoder == null) {
            decoder = new AudioDecoder();
        }
        return decoder;
    }

    private AudioDecoder() {
        this.dataList = Collections
                .synchronizedList(new LinkedList<AudioData2>());
    }

    /*
     * add Data to be decoded
     * @ data:the data recieved from server
     * @ size:data size
     */
    public void addData(byte[] data, int size) {
        AudioData2 adata = new AudioData2();
        adata.setSize(size);
        byte[] tempData = new byte[size];
        System.arraycopy(data, 0, tempData, 0, size);
        adata.setRealData(tempData);
        dataList.add(adata);
    }

    /*
     * start decode AMR data
     */
    public void startDecoding() {
        System.out.println(LOG + "开始解码");
        if (isDecoding) {
            return;
        }
        new Thread(this).start();
    }

    public void run() {
        // start player first
        AudioPlayer player = AudioPlayer.getInstance();
        player.startPlaying();
        //
        this.isDecoding = true;

        Log.d(LOG, LOG + "initialized decoder");
        int decodeSize = 0;
        while (isDecoding) {
            while (dataList.size() > 0) {
                AudioData2 encodedData = dataList.remove(0);
                decodedData = new short[Speex.getInstance().getFrameSize()];
                byte[] data = encodedData.getRealData();
//                decodeSize = Codec.instance().decode(data, 0, encodedData.getSize(), decodedData, 0);
                decodeSize = Speex.getInstance().decode(data, decodedData, data.length);
                Log.e(LOG, "解码一次" + data.length + " 解码后的长度 " + decodeSize);
                if (decodeSize > 0) {
                    // add decoded audio to player
                    player.addData(decodedData, decodeSize);
                }
            }
        }
        System.out.println(LOG + "stop decoder");
        // stop playback audio
        player.stopPlaying();
    }

    public void stopDecoding() {
        this.isDecoding = false;
    }
}