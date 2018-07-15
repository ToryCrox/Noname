package com.tory.library.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

/**
 * Created by tao.xu2 on 2016/12/15.
 */

public class ImageUtils {


    public static Drawable zoomMaxDrawable(Drawable drawable, int width, int height){
        if(drawable == null) return null;
        int drawableWidth = drawable.getIntrinsicWidth();
        int drawableHeight = drawable.getIntrinsicHeight();
        if(drawableWidth < width && drawableHeight < height){
            return drawable;
        }
        return zoomDrawable(drawable,width,height);
    }


    public static Drawable zoomDrawable(Drawable drawable, int width, int height) {
        Bitmap bitmap = drawableToBitmap(drawable);
        Bitmap bmp = zoomBitmap(bitmap,width,height);
        return bmp != null ? new BitmapDrawable(bmp) : null;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int width, int height){
        if(bitmap == null || bitmap.isRecycled()) return null;
        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        if(bmpWidth == 0 || bmpHeight == 0){
            return null;
        }
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / bmpWidth);
        float scaleHeight = ((float) height / bmpHeight);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bmpWidth, bmpHeight, matrix, true);
        bitmap.recycle();
        return bmp;
    }


    public static Bitmap getDrawableBitmap(Drawable drawable){
        if(drawable instanceof BitmapDrawable){
            return ((BitmapDrawable) drawable).getBitmap();
        }else{
            return drawableToBitmap(drawable);
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
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


    public static Drawable resizeImage(Resources res,@DrawableRes int drawableId,
                                       int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//不加载bitmap到内存中
        BitmapFactory.decodeResource(res,drawableId,options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 1;

        if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
            int sampleSize=Math.max((int)(outHeight / (float) height), (int)(outWidth / (float) width));;
            options.inSampleSize = sampleSize;
        }
        options.inJustDecodeBounds = false;
        return new BitmapDrawable(res,BitmapFactory.decodeResource(res,drawableId, options));
    }

    public static void recycleBitmap(Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
    }
}
