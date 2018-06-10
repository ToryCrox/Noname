/*Transsion Top Secret*/
package com.tory.library.lightness;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;

/**
 * 获取图片的深浅，提取自miui的源代码
 * @author tao.xu2
 * @date 2017/5/31
 */

public class BitmapColorMode {

    private static final int MAX_BITMAP_EXTRACTION_AREA = 400 * 400;
    public static final int BITMAP_COLOR_MODE_DARK = Lightness.DARK;
    public static final int BITMAP_COLOR_MODE_LIGHT = Lightness.LIGHT;
    public static final int BITMAP_COLOR_MODE_MEDIUM = Lightness.UNKNOWN;

    /**
     *
     * @param bitmap
     * @return
     */
    public static int getBitmapColorMode(@NonNull Bitmap bitmap) {
        int colorMode = BITMAP_COLOR_MODE_LIGHT;
        Bitmap scaleBitmap = BitmapUtil.scaleBitmapDown(bitmap, MAX_BITMAP_EXTRACTION_AREA);
        int height = scaleBitmap.getHeight();
        int width = scaleBitmap.getWidth();
        int avPixelNum = (width * height) / 5;
        int tempColorMode = 0;
        int x = 0;
        while (x < width) {
            int darkPixelNum = tempColorMode;
            tempColorMode = colorMode;
            for (int y = 0; y < height; y++) {
                int pixel = scaleBitmap.getPixel(x, y);
                double rs = Color.red(pixel) * 0.3d;
                double gs = Color.green(pixel) * 0.59d;
                double bs = Color.blue(pixel) * 0.11d;
                if ( rs + gs + bs < 180) {
                    darkPixelNum++;
                    if (darkPixelNum > avPixelNum) {
                        tempColorMode = BITMAP_COLOR_MODE_MEDIUM;
                    }
                    if (darkPixelNum > avPixelNum * 2) {
                        tempColorMode = BITMAP_COLOR_MODE_DARK;
                        break;
                    }
                }
            }
            x++;
            colorMode = tempColorMode;
            tempColorMode = darkPixelNum;
        }
        if (scaleBitmap != bitmap) {
            scaleBitmap.recycle();
        }
        return colorMode;
    }
}
