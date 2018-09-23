package com.tory.library.utils.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.renderscript.RSRuntimeException;
import android.support.annotation.NonNull;


public class BlurUtil {
    private static final String TAG = "BlurUtil";

    private static final int DEFAULT_BLUR_RADIUS = 25;
    private static final int DEFAULT_BLUR_SCALE = 8;

    /**
     * 高斯模糊
     * @param context
     * @param bitmap
     * @param width 目标宽度
     * @param height 目标高度度
     * @return
     */
    public static Bitmap blur(@NonNull Context context, Bitmap bitmap, int width, int height){
        return blur(context, bitmap, width, height ,DEFAULT_BLUR_RADIUS, DEFAULT_BLUR_SCALE);
    }

    public static Bitmap blur(@NonNull Context context, Bitmap bitmap){
        return blur(context, bitmap, bitmap.getWidth(), bitmap.getHeight() ,DEFAULT_BLUR_RADIUS, DEFAULT_BLUR_SCALE);
    }

    /**
     * 高斯模糊
     * @param context
     * @param bitmap
     * @param width
     * @param height
     * @param radius 虚化半径
     * @param scaleFactor 缩放比例
     * @return
     */
    public static Bitmap blur(Context context, Bitmap bitmap, int width, int height, int radius,
                              int scaleFactor){
        if(bitmap == null) return null;
        Bitmap overlay = Bitmap.createBitmap( (width / scaleFactor),
                (height / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        cropCanvas(canvas, bitmap, overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        //overlay = doBlur(overlay,radius);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                overlay = RSBlur.blur(context, overlay, radius);
            } catch (RSRuntimeException e) {
                overlay = FastBlur.blur(overlay, radius, true);
            }
        } else {
            overlay = FastBlur.blur(overlay, radius, true);
        }

        return overlay;
    }

    private static void cropCanvas(Canvas canvas,Bitmap bitmap,Bitmap overlay){
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        //目标长宽
        int oWidth = overlay.getWidth();
        int oHeight = overlay.getHeight();

        int scaledWidth = bWidth * oHeight / bHeight;//按高度缩放后的宽度值
        int scaledHeight = bHeight * oWidth / bWidth;//按宽度缩放后的高度值

        if(scaledWidth <= oWidth){//按宽度进行缩放
            scaledWidth = oWidth;
        }else{
            scaledHeight = oHeight;
        }

        float scale = (float)scaledWidth / bWidth;

        int x = scaledWidth > oWidth ? (scaledWidth - oWidth)/2 : 0;
        int y = scaledHeight >oHeight ? (scaledHeight - oHeight)/2 : 0;
        canvas.translate(-x, -y);
        canvas.scale(scale, scale);
    }

}