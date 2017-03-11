package com.nercms.receive;

/**
 *  H264 解码
 * Created by zsg on 2016/6/3.
 */
public class Receive {
    static {
        System.loadLibrary("H264Decoder_neon");
    }




    public native long CreateH264Packer();




    public native int PackH264Frame(long handle, byte[] pPayload, int payloadlen, int bMark, int pts, int sequence, byte[] frmbuf);





    public native void DestroyH264Packer(long handle);






    public native int CreateDecoder(int width, int height);





    public native int DecoderNal(byte[] in, int insize, byte[] out);






    public native int DestoryDecoder();

}
