package com.gyz.voipdemo_speex.util;

public class Speex {
    private static final int DEFAULT_COMPRESSION = 8;
    private static final Speex speex = new Speex();//创建一个对象，单例模式

    public native int open(int compression);//打开编解码库

    public native int getFrameSize();//获取帧的大小

    public native int decode(byte encoded[], short lin[], int size);//解码

    public native int encode(short lin[], int offset, byte encoded[], int size);//编码

    public native void close();//关闭编解码库

    private Speex() {

    }

    public static Speex getInstance() {
        return speex;
    }

    public void init() {
        load();//加载.so文件
        open(DEFAULT_COMPRESSION);
    }

    private void load() {
        try {
            System.loadLibrary("speex");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
