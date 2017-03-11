package com.nercms;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.nercms.audio.AudioWrapper;
import com.gyz.voipdemo_speex.util.Speex;
import com.nercms.receive.Receive;
import com.nercms.receive.VideoPlayView;
import com.nercms.send.Send;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 视频聊天活动
 */
public class VideoChatActivity extends Activity {


    private final static int CIF_WIDTH = 352;
    private final static int CIF_HEIGHT = 288;

    private AudioWrapper audioWrapper = AudioWrapper.getInstance();
    private VideoPlayView view = null;//接收播放流的view
    private SurfaceView surfaceView;//显示Camera的画面
    private Camera mCamera = null; //创建摄像头处理类
    private SurfaceHolder holder = null; //创建界面句柄，显示视频的窗口句柄
    private Handler handler = new Handler();
    private Send encode;      //编码器
    private Receive decode;   //解码器
    private boolean isRunning; //线程运行标志

    private RtpSocket rtp_socket = null; //创建RTP套接字

    private RtpPacket rtp_send_packet = null; //创建RTP发送包
    private RtpPacket rtp_receive_packet = null; //创建RTP接受包

    //接受 处理
    private long decoder_handle = 0; //拼帧器的句柄
    private byte[] frmbuf = new byte[65536]; //帧缓存
    private byte[] socket_receive_Buffer = new byte[65536]; //包缓存
    private byte[] buffer = new byte[65536];//缓冲区

    //发送
    private long encoder_handle = -1; //创建编码器的句柄
    private int send_packetNum = 0; //包的数目
    private int[] send_packetSize = new int[200]; //包的尺寸
    private byte[] send_stream = new byte[65536]; //码流
    private byte[] socket_send_Buffer = new byte[65536]; //缓存 stream->socketBuffer->rtp_socket

    private String remote_ip;
    private int remote_port;
    private int mCurrentCamIndex;
//    private AmrAudioEncoder amrAudioEncoder = AmrAudioEncoder.getArmAudioEncoderInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        remote_ip = getIntent().getStringExtra("remote_ip");
        remote_port = getIntent().getIntExtra("remote_port", 8080);
//        amrAudioEncoder.initArmAudioEncoder(this);
//        amrAudioEncoder.start();
        Speex.getInstance().init();
        initView();
    }

    private void initView() {
        try {
            view = (VideoPlayView) this.findViewById(R.id.video_play);
            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            encode = new Send();
            decode = new Receive();
        } catch (Exception e) {
            Log.e("lqm", e.toString());
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doStart();
            }
        }, 1000);

    }

    /**
     * 打开前置摄像头
     *
     * @return
     */
    private Camera openFrontFacingCameraGingerbread() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                    mCurrentCamIndex = camIdx;
                } catch (RuntimeException e) {
                    Log.e("lqm",
                            "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }
        return cam;
    }

    /**
     * 开启 接受 发送rtp线程  开启本地摄像头
     */
    public void doStart() {
        //初始化解码器
        if (rtp_socket == null) {
            try {
                //rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
                rtp_socket = new RtpSocket(new SipdroidSocket(19888), InetAddress.getByName(remote_ip), remote_port);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //初始化接受包
            rtp_receive_packet = new RtpPacket(socket_receive_Buffer, 0); //初始化 ,socketBuffer改变时rtp_Packet也跟着改变
            /**
             * 因为可能传输数据过大 会将一次数据分割成好几段来传输
             * 接受方 根据序列号和结束符 来将这些数据拼接成完整数据
             */
            //初始化解码器
            decoder_handle = decode.CreateH264Packer(); //创建拼帧器
            decode.CreateDecoder(CIF_WIDTH, CIF_HEIGHT); //创建解码器
            isRunning = true;
            DecoderThread decoder = new DecoderThread();
            decoder.start(); //启动一个线程
            //初始化发送包
            rtp_send_packet = new RtpPacket(socket_send_Buffer, 0);
        }
        //初始化编码器
        if (encoder_handle == -1)
            encoder_handle = encode.CreateEncoder(CIF_WIDTH, CIF_HEIGHT); //调用底层函数，创建编码器

        if (mCamera == null) {
            //摄像头设置，预览视频
            mCamera = Camera.open(1); //实例化摄像头类对象  0为后置 1为前置

            mCamera.setDisplayOrientation(90); //视频旋转90度

//            p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//            p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //设置预览视频的格式
//            p.setPreviewFormat(ImageFormat.NV21);
            Camera.Parameters p = mCamera.getParameters(); //将摄像头参数传入p中
            p.setFlashMode("off");
//            Camera.Size size = mCamera.new Size(CIF_WIDTH, CIF_HEIGHT);//设置camera显示的大小
//            if (!p.getSupportedPreviewSizes().contains(size) || !p.getSupportedPictureSizes().contains(size)) {
//                //判断手动配置的宽高是否支持显示
//                //释放摄像头资源
//                if (mCamera != null) {
//                    mCamera.setPreviewCallback(null); //停止回调函数
//                    mCamera.stopPreview(); //停止预览
//                    mCamera.release(); //释放资源
//                    mCamera = null; //重新初始化
//                }
//            }
            p.setPreviewSize(CIF_WIDTH, CIF_HEIGHT); //设置预览视频的尺寸，CIF格式352×288
//            p.setPictureSize(size.width, size.height);//设置预览大小
//            //p.setPreviewSize(800, 600);
            p.setPreviewFrameRate(15); //设置预览的帧率，15帧/秒
            mCamera.setParameters(p); //设置参数
            byte[] rawBuf = new byte[1400];
            mCamera.addCallbackBuffer(rawBuf);

            try {
                mCamera.setPreviewDisplay(holder); //预览的视频显示到指定窗口
            } catch (IOException e) {
                e.printStackTrace();
            }
//            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//            mCamera.startPreviewAsync();
            mCamera.startPreview(); //开始预览
            //获取帧
            //预览的回调函数在开始预览的时候以中断方式被调用，每秒调用15次，回调函数在预览的同时调出正在播放的帧
            Callback a = new Callback();
            mCamera.setPreviewCallback(a);
        }
        audioWrapper.startRecord(remote_ip);
        audioWrapper.startListen();
    }

    //mCamera回调的类
    class Callback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] frame, Camera camera) {
            if (encoder_handle != -1) {
                Log.d("lqm", System.currentTimeMillis() + "");
                //底层函数，返回包的数目，返回包的大小存储在数组packetSize中，返回码流在stream中
                send_packetNum = encode.EncoderOneFrame(encoder_handle, -1, frame, send_stream, send_packetSize);
                Log.d("log", "原始数据大小：" + frame.length + "  转码后数据大小：" + send_stream.length);
                Log.d("lqm", System.currentTimeMillis() + "");
                if (send_packetNum > 0) {
                    //通过RTP协议发送帧
                    final int[] pos = {0}; //从码流头部开始取
                    final long timestamp = System.currentTimeMillis(); //设定时间戳
                    /**
                     * 因为可能传输数据过大 会将一次数据分割成好几段来传输
                     * 接受方 根据序列号和结束符 来将这些数据拼接成完整数据
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int sequence = 0; //初始化序列号
                            for (int i = 0; i < send_packetNum; i++) {
                                rtp_send_packet.setPayloadType(2);//定义负载类型，视频为2
                                rtp_send_packet.setMarker(i == send_packetNum - 1 ? true : false); //是否是最后一个RTP包
                                rtp_send_packet.setSequenceNumber(sequence++); //序列号依次加1
                                rtp_send_packet.setTimestamp(timestamp); //时间戳
                                //Log.d("log", "序列号:" + sequence + " 时间：" + timestamp);
                                rtp_send_packet.setPayloadLength(send_packetSize[i]); //包的长度，packetSize[i]+头文件
                                //从码流stream的pos处开始复制，从socketBuffer的第12个字节开始粘贴，packetSize为粘贴的长度
                                System.arraycopy(send_stream, pos[0], socket_send_Buffer, 12, send_packetSize[i]); //把一个包存在socketBuffer中
                                pos[0] += send_packetSize[i]; //重定义下次开始复制的位置
                                //rtp_packet.setPayload(socketBuffer, rtp_packet.getLength());
                                //  Log.d("log", "序列号:" + sequence + " bMark:" + rtp_packet.hasMarker() + " packetSize:" + packetSize[i] + " tPayloadType:2" + " timestamp:" + timestamp);
                                try {
                                    rtp_socket.send(rtp_send_packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
            }
        }
    }

    /**
     * 接收rtp数据并解码 线程
     */
    class DecoderThread extends Thread {
        public void run() {
            while (isRunning) {
                try {
                    rtp_socket.receive(rtp_receive_packet); //接收一个包
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int packetSize = rtp_receive_packet.getPayloadLength(); //获取包的大小

                if (packetSize <= 0)
                    continue;
                if (rtp_receive_packet.getPayloadType() != 2) //确认负载类型为2
                    continue;
                /**
                 * 第一个参数:源数组
                 * 第二个参数:源数组要复制的起始位置
                 * 第三个参数:目的数组
                 * 第四个参数:目的数组放置的起始位置
                 * 第五个参数：复制的长度
                 */
                System.arraycopy(socket_receive_Buffer, 12, buffer, 0, packetSize); //socketBuffer->buffer
                int sequence = rtp_receive_packet.getSequenceNumber(); //获取序列号
                long timestamp = rtp_receive_packet.getTimestamp(); //获取时间戳
                int bMark = rtp_receive_packet.hasMarker() == true ? 1 : 0; //是否是最后一个包
                int frmSize = decode.PackH264Frame(decoder_handle, buffer, packetSize, bMark, (int) timestamp, sequence, frmbuf); //packer=拼帧器，frmbuf=帧缓存
                Log.d("log", "序列号:" + sequence + " bMark:" + bMark + " packetSize:" + packetSize + " PayloadType:" + rtp_receive_packet.getPayloadType() + " timestamp:" + timestamp + " frmSize:" + frmSize);
                if (frmSize <= 0)
                    continue;
                decode.DecoderNal(frmbuf, frmSize, view.mPixel);//解码后的图像存在mPixel中
                //Log.d("log","序列号:"+sequence+" 包大小："+packetSize+" 时间："+timestamp+"  frmbuf[30]:"+frmbuf[30]);
                view.postInvalidate();
            }
            //关闭
            if (decoder_handle != 0) {
                decode.DestroyH264Packer(decoder_handle);
                decoder_handle = 0;
            }
            if (rtp_socket != null) {
                rtp_socket.close();
                rtp_socket = null;
            }
            decode.DestoryDecoder();
        }
    }

    /**
     * 关闭摄像头 并释放资源
     */
    public void close() {
        Log.e("lqm", "释放资源");
        isRunning = false;
        //释放摄像头资源
        if (mCamera != null) {
            mCamera.setPreviewCallback(null); //停止回调函数
            mCamera.stopPreview(); //停止预览
            mCamera.release(); //释放资源
            mCamera = null; //重新初始化
        }

        if (rtp_socket != null) {
            rtp_socket.close();
            rtp_socket = null;
        }
        audioWrapper.stopRecord();
        audioWrapper.stopListen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        amrAudioEncoder.stop();
        close();
    }
}
