package com.tory.library.lightness;

import android.graphics.Bitmap;

public class BitmapUtil {

    /**
     * 对bitmap进行适当缩放
     * @param bitmap
     * @param resizeArea
     * @return
     */
    public static Bitmap scaleBitmapDown(final Bitmap bitmap, final int resizeArea) {
        double scaleRatio = -1;

        final int bitmapArea = bitmap.getWidth() * bitmap.getHeight();
        if (bitmapArea > resizeArea) {
            scaleRatio = Math.sqrt(resizeArea / (double) bitmapArea);
        }

        if (scaleRatio <= 0) {
            // Scaling has been disabled or not needed so just return the Bitmap
            return bitmap;
        }

        return Bitmap.createScaledBitmap(bitmap,
                (int) Math.ceil(bitmap.getWidth() * scaleRatio),
                (int) Math.ceil(bitmap.getHeight() * scaleRatio),
                false);
    }
}