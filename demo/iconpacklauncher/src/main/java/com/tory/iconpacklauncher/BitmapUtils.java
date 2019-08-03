package com.tory.iconpacklauncher;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author tao.xu2
 * @date 2018/1/30
 */

public class BitmapUtils {


    public static Bitmap scaleBitmap(Bitmap srcBitmap, int targetWidth, int targetHeight) {
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        float scaleWidth = ((float) targetWidth) / srcWidth;
        float scaleHeight = ((float) targetHeight) / srcHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight, matrix, true);
        return srcBitmap;
    }
}
