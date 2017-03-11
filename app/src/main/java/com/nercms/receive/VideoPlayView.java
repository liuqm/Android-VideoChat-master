package com.nercms.receive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;

/**
 * 显示H264解码后视频  一帧一帧的按图片显示
 */
public class VideoPlayView extends View {
    public int width = 352;
    public int height = 288;
    public byte[] mPixel = new byte[width * height * 2];
    public ByteBuffer buffer = ByteBuffer.wrap(mPixel);
    public Bitmap VideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
    private Matrix matrix = null;
    public Bitmap VideoBit2;
    private RectF rectF;

    public VideoPlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int W = dm.widthPixels;
        int H = dm.heightPixels;
        rectF = new RectF(0, 0, W, H);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        buffer.rewind();
        try {
            VideoBit.copyPixelsFromBuffer(buffer);
            setAngle();
//        canvas.drawBitmap(adjustPhotoRotation(VideoBit, 90), 0, 0, null);

//        Bitmap b = BitmapFactory.decodeByteArray(mPixel, 0, mPixel.length);
            canvas.drawBitmap(VideoBit2, null, rectF, null);
        } catch (Exception e) {
            Log.e("lqm", e.toString());
        }

    }

    //  设置旋转比例
    private void setAngle() {
        matrix.reset();
        matrix.setRotate(-90);
        VideoBit2 = Bitmap.createBitmap(VideoBit, 0, 0, VideoBit.getWidth(), VideoBit.getHeight(), matrix, true);
    }


    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }
        final float[] values = new float[9];
        m.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        m.postTranslate(targetX - x1, targetY - y1);
        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        return bm1;
    }
}
