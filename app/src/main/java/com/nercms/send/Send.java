package com.nercms.send;

/**
 * H264 编码
 * Created by zsg on 2016/6/3.
 */

public class Send {


    //包含库文件
    static {
        System.loadLibrary("VideoEncoder");
    }

    //接口函数
    public native long CreateEncoder(int width, int height); //底层创建编码器，返回编码器

    //编码一帧图像，返回包的数目
    //type=编码帧的类型，frame=原始yuv图像，stream=原始图像码流，packetSize=包的尺寸
    public native int EncoderOneFrame(long encoder, int type, byte[] frame, byte[] stream, int[] packetSize);

    public native int DestroyEncoder(long encoder); //销毁编码器，释放资源


}