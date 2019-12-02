package com.tory.library.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author tory
 * @create 2018/7/19
 * @Describe
 */
public class BitmapUtils {


    /**
     * 是否为合法的bitmap
     * @param bitmap
     * @return
     */
    public static boolean isAvailable(@Nullable Bitmap bitmap){
        return bitmap != null && !bitmap.isRecycled();
    }

    /**
     * recycler bitmap
     * @param bitmap
     */
    public static void safelyRecycle(@Nullable Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

    /**
     * 居中缩放并裁剪图片
     * @param src
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    @Nullable
    public static Bitmap scaleCenterCrop(@Nullable Bitmap src, int targetWidth, int targetHeight){
        if (!isAvailable(src)){
            return null;
        }

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

    /**
     * @param drawable
     * @return
     */
    @NonNull
    public static Bitmap toBitmap(@NonNull Drawable drawable) {
        int width = drawable.getIntrinsicWidth();   // 取 drawable 的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?
                Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;// 取 drawable 的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应 bitmap
        Canvas canvas = new Canvas(bitmap); // 建立对应 bitmap 的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);      // 把 drawable 内容画到画布中
        return bitmap;
    }
}
