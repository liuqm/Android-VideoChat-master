package com.nercms.audio;


import android.media.AudioRecord;

import com.gyz.voipdemo_speex.util.Speex;

public class AudioRecorder implements Runnable {

    String LOG = "Recorder ";

    //是否正在录制
    private boolean isRecording = false;
    //音频录制对象
    private AudioRecord audioRecord;
    
    private int audioBufSize = 0;
    private String ip;

    /*
     * 开始录制
     */
    public void startRecording(String remote_ip) {
    	//计算缓存大小
        ip = remote_ip;
        audioBufSize = AudioRecord.getMinBufferSize(AudioConfig.SAMPLERATE,
            AudioConfig.RECORDER_CHANNEL_CONFIG, AudioConfig.AUDIO_FORMAT);
        if (null == audioRecord) {//实例化录制对象
            audioRecord = new AudioRecord(AudioConfig.AUDIO_RESOURCE,
                    AudioConfig.SAMPLERATE,
                    AudioConfig.RECORDER_CHANNEL_CONFIG,
                    AudioConfig.AUDIO_FORMAT, audioBufSize);
        }
        new Thread(this).start();
    }

    /*
     * stop
     */
    public void stopRecording() {
        this.isRecording = false;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void run() {
        // start encoder before recording
        AudioEncoder encoder = AudioEncoder.getInstance();
        encoder.startEncoding(ip);
        System.out.println(LOG + "audioRecord startRecording()");
        audioRecord.startRecording();
        System.out.println(LOG + "start recording");

        this.isRecording = true;
        int size = Speex.getInstance().getFrameSize();
        short[] samples = new short[size];
        while (isRecording) {
            int bufferRead = audioRecord.read(samples, 0, size);
            if (bufferRead > 0) {
                // add data to encoder
                encoder.addData(samples, bufferRead);
//              System.out.println("samples:"+Arrays.toString(samples));
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(LOG + "end recording");
        audioRecord.stop();
        encoder.stopEncoding();
    }
}
