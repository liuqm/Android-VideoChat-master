package com.nercms.audio;


/**
 * @author lqm
 *         音频包装器
 */
public class AudioWrapper {
    //音频录制器
    private AudioRecorder audioRecorder;
    //音频接收器
    private AudioReceiver audioReceiver;

    private static AudioWrapper instanceAudioWrapper;//静态变量

    //私有化构造参数
    private AudioWrapper() {
    }


    //单例模式
    public static AudioWrapper getInstance() {
        if (null == instanceAudioWrapper) {
            instanceAudioWrapper = new AudioWrapper();
        }
        return instanceAudioWrapper;
    }

    //开始录制音频
    public void startRecord(String remote_ip) {
        if (null == audioRecorder) {
            audioRecorder = new AudioRecorder();
        }
        audioRecorder.startRecording(remote_ip);
    }


    //停止录制
    public void stopRecord() {
        if (audioRecorder != null)
            audioRecorder.stopRecording();
    }


    //开始监听接收音频
    public void startListen() {
        if (null == audioReceiver) {
            audioReceiver = new AudioReceiver();
        }
        audioReceiver.startRecieving();
    }


    //停止监听接收音频
    public void stopListen() {
        if (audioRecorder != null)
            audioRecorder.stopRecording();
    }
}
