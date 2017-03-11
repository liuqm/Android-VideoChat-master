package com.nercms;

import java.io.BufferedReader;
import java.io.DataInputStream;

import java.io.File;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.app.Activity;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.AudioRecord;
import android.os.Bundle;

import android.graphics.PixelFormat;

import android.media.MediaRecorder;

import android.net.LocalServerSocket;

import android.net.LocalSocket;

import android.net.LocalSocketAddress;

import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;

import android.view.SurfaceView;

import android.view.View;

import android.view.Window;

import android.view.WindowManager;

import org.zoolu.tools.Random;


/**
 */

public class VideoCameraActivity extends Activity implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    private LocalServerSocket lss;
    private LocalSocket receiver;
    private LocalSocket sender;
    private MediaRecorder mMediaRecorder;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private boolean bIfRecInProcess = false;
    private boolean bIfNativeORRemote = false;
    private File mRecVideoFile;
    private String strRecVideoFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localsocket);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        strRecVideoFilePath = getExternalCacheDir().getAbsolutePath();
        initLocalSocket();
        initializeVideo();
    }


    private void initLocalSocket() {
        receiver = new LocalSocket();
        try {
            lss = new LocalServerSocket("H264");
            receiver.connect(new LocalSocketAddress("H264"));
            receiver.setReceiveBufferSize(500000);
            receiver.setSendBufferSize(500000);
            sender = lss.accept();
            sender.setReceiveBufferSize(500000);
            sender.setSendBufferSize(500000);
        } catch (IOException e1) {
            e1.printStackTrace();
            Log.e("", "localSocket error:" + e1.getMessage());
        }
    }


    private boolean initializeVideo() {
        try {
            Log.i("lqm", "##initializeVideo....");
            // 〇state: Initial 实例化MediaRecorder对象
            if (mSurfaceView == null) {
                Log.e("lqm", "mSurfaceView is null in initializeVideo");
                return false;
            }
            if (mMediaRecorder == null)
                mMediaRecorder = new MediaRecorder();
            else
                mMediaRecorder.reset();

            // 〇state: Initial=>Initialized
            // set audio source as Microphone, video source as camera
            // specified before settings Recording-parameters or encoders，called only before setOutputFormat
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            // 〇state: Initialized=>DataSourceConfigured
            // 设置錄製視頻输出格式
            //     THREE_GPP:    3gp格式，H263视频ARM音频编码
            //    MPEG-4:        MPEG4 media file format
            //    RAW_AMR:    只支持音频且音频编码要求为AMR_NB
            //    AMR_NB:
            //    ARM_MB:
            //    Default:
            // 3gp or mp4
            //Android支持的音频编解码仅为AMR_NB；支持的视频编解码仅为H263，H264只支持解码；支持对JPEG编解码；输出格式仅支持.3gp和.mp4

            String lVideoFileFullPath;
            lVideoFileFullPath = ".3gp"; //.mp4
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            Log.i("lqm", "Video：Current container format: " + "3GP\n");

            // 设置視頻/音频文件的编码：AAC/AMR_NB/AMR_MB/Default
            //    video: H.263, MP4-SP, or H.264
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H263);
            //mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            Log.i("lqm", "Video：Current encoding format: " + "H264\n");
            // audio: AMR-NB
            //mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            // 设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
            //mMediaRecorder.setVideoSize(176, 144);
            mMediaRecorder.setVideoSize(320, 240);
            //mMediaRecorder.setVideoSize(720, 480);
            Log.i("lqm", "Video：Current Video Size: " + "320*240\n");

            // 设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
            mMediaRecorder.setVideoFrameRate(15);

            // 预览
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
            // 设置输出文件方式： 直接本地存储   or LocalSocket远程输出
            if (bIfNativeORRemote)    //Native
            {
                lVideoFileFullPath = strRecVideoFilePath + String.valueOf(System.currentTimeMillis()) + lVideoFileFullPath;
                mRecVideoFile = new File(lVideoFileFullPath);
                // mMediaRecorder.setOutputFile(mRecVideoFile.getAbsolutePath());
                mMediaRecorder.setOutputFile(mRecVideoFile.getPath());    //called after set**Source before prepare
                Log.i("lqm", "start write into file~");
            } else    //Remote
            {
                mMediaRecorder.setOutputFile(sender.getFileDescriptor()); //设置以流方式输出
                Log.i("lqm", "start send into sender~");
            }

            //
            mMediaRecorder.setMaxDuration(0);//called after setOutputFile before prepare,if zero or negation,disables the limit
            mMediaRecorder.setMaxFileSize(0);//called after setOutputFile before prepare,if zero or negation,disables the limit
            try {
                mMediaRecorder.setOnInfoListener(this);
                mMediaRecorder.setOnErrorListener(this);
                // 〇state: DataSourceConfigured => prepared
                mMediaRecorder.prepare();
                // 〇state: prepared => recording
                mMediaRecorder.start();
                bIfRecInProcess = true;
                Log.i("lqm", "initializeVideo Start!");
            } catch (Exception e) {
                releaseMediaRecorder();
                finish();
                e.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 释放mediarecorder对象
     */
    private void releaseMediaRecorder() {

    }


    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {

    }

    @Override
    public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {

    }
}