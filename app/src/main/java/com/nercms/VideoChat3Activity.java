package com.nercms;

/**
 * Created by lqm on 2016/9/20.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoChat3Activity extends Activity implements SurfaceHolder.Callback,
        Camera.PreviewCallback {

    private SurfaceView mSurfaceview = null; // SurfaceView对象：(视图组件)视频显示
    private SurfaceHolder mSurfaceHolder = null; // SurfaceHolder对象：(抽象接口)SurfaceView支持类
    private Camera mCamera = null; // Camera对象，相机预览

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        mSurfaceview = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceview.getHolder(); // 绑定SurfaceView，取得SurfaceHolder对象
        mSurfaceHolder.addCallback(this); // SurfaceHolder加入回调接口
        // mSurfaceHolder.setFixedSize(176, 144); // 预览大小設置
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 設置顯示器類型，setType必须设置
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = Camera.open(1); //实例化摄像头类对象  0为后置 1为前置
        Camera.Parameters p = mCamera.getParameters(); //将摄像头参数传入p中
        p.setFlashMode("off");
        p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        //p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //设置预览视频的格式
        p.setPreviewFormat(ImageFormat.NV21);
        p.setPreviewSize(352, 288); //设置预览视频的尺寸，CIF格式352×288
        //p.setPreviewSize(800, 600);
        p.setPreviewFrameRate(20); //设置预览的帧率，15帧/秒
        mCamera.setParameters(p); //设置参数
        byte[] rawBuf = new byte[1400];
        mCamera.addCallbackBuffer(rawBuf);
        mCamera.setDisplayOrientation(90); //视频旋转90度
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 刚刚拍照的文件名
        String fileName = "IMG_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
                .toString() + ".jpg";
        File sdRoot = Environment.getExternalStorageDirectory();
        String dir = "/Camera/";
        File mkDir = new File(sdRoot, dir);
        if (!mkDir.exists())
            mkDir.mkdirs();
        File pictureFile = new File(sdRoot, dir + fileName);
        if (!pictureFile.exists()) {
            try {
                pictureFile.createNewFile();
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(data,
                        parameters.getPreviewFormat(), size.width, size.height,
                        null);
                FileOutputStream filecon = new FileOutputStream(pictureFile);
                image.compressToJpeg(
                        new Rect(0, 0, image.getWidth(), image.getHeight()),
                        90, filecon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
