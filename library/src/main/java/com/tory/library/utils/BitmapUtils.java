package com.tory.library.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author tory
 * @create 2018/7/19
 * @Describe
 */
public class BitmapUtils {


    public static void safelyRecycle(@Nullable Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

    public static Bitmap scaleCenterCrop(@NonNull Bitmap src, int targetWidth, int targetHeight){

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        if(srcWidth == targetWidth && srcHeight == targetHeight){
            return src;
        }

        //取小的缩放比例，这样画布缩放后才能有一边比原图小
        float scaleW = srcWidth * 1.0f / targetWidth;
        float scaleH = srcHeight * 1.0f / targetHeight;
        float scale = Math.min(scaleW, scaleH);
        float dx = (srcWidth - targetWidth * scale) / 2;
        float dy = (srcHeight - targetHeight * scale) / 2;
        Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(1 / scale, 1 / scale);//这里应用是1/scale才是缩放图片以适应原图大小
        //canvas.drawBitmap(src, -dx, -dy, paint); //这是指从坐标轴的哪里开始画
        canvas.translate( -dx, -dy);//此处是移动原点坐标为负值, 绘图的位置不变，只移动的画布
        canvas.drawBitmap(src, 0, 0, paint);
        canvas.setBitmap(null);
        safelyRecycle(src);
        return bitmap;
    }
}
