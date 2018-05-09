package com.tory.library.lightness;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

/**
 *
 */
public class LightnessUtils {

    public static final int MAX_BITMAP_EXTRACTION_AREA = 384 * 216;
    private static final int GRAY_COLOR_MIDDLE = 192;

    private LightnessUtils() { }


    /**
     * see {@link android.app.WallpaperColors#fromBitmap(Bitmap)}
     * 不太准确，先用BitmapColorMode判断
     * Determines if a given bitmap Lightness
     * @param bitmap
     * @return
     */
    @Lightness.LightnessValues
    public static int checkPaltteLightness(@NonNull Bitmap bitmap){
        Palette palette = Palette.from(bitmap)
                .maximumColorCount(5)
                .clearFilters()
                .resizeBitmapArea(MAX_BITMAP_EXTRACTION_AREA)
                .generate();

        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
        if(dominantSwatch == null){
            return Lightness.UNKNOWN;
        }

        return isWhite(dominantSwatch.getRgb()) ? Lightness.LIGHT : Lightness.DARK;
    }


    public static int getGrayColor(int color){
        return  (int) (Color.red(color) * 0.3 + Color.green(color) * 0.59 + Color.blue(color) * 0.11);
    }

    /**
     * 判断是不是深颜色
     * @return
     */
    public static boolean isWhite(int color){
        return getGrayColor(color) >= GRAY_COLOR_MIDDLE;
    }


}