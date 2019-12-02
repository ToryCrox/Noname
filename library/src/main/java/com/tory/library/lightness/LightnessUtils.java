package com.tory.library.lightness;

import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.palette.graphics.Palette;


/**
 * Utility methods for working with colors.
 * @author tao.xu2
 */
public class LightnessUtils {

    public static final int MAX_BITMAP_EXTRACTION_AREA = 384 * 216;
    private static final int GRAY_COLOR_MIDDLE = 180;

    private LightnessUtils() { }


    /**
     *
     * @param bitmap
     * @return
     */
    @Lightness.LightnessValues
    public static int checkLightness(@NonNull Bitmap bitmap){
        Bitmap bmp = BitmapUtil.scaleBitmapDown(bitmap, MAX_BITMAP_EXTRACTION_AREA);
        int lightness = BitmapColorMode.getBitmapColorMode(bmp);
        if(lightness == Lightness.UNKNOWN){
            lightness = checkPaltteLightness(bmp);
        }
        if(bmp != bitmap && !bmp.isRecycled()){
            bmp.recycle();
        }
        return lightness;
    }



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
        int grayLevel = (int) (Color.red(color) * 0.3 + Color.green(color) * 0.59 + Color.blue(color) * 0.11);
        if(grayLevel >= GRAY_COLOR_MIDDLE){
            return true;
        }
        return false;
    }



    private static final float DARK_PIXEL_LUMINANCE = 0.45f;
    private static final float MAX_DARK_AREA = 0.05f;
    private static final float BRIGHT_IMAGE_MEAN_LUMINANCE = 0.75f;
    private static final float DARK_THEME_MEAN_LUMINANCE = 0.25f;
    public static final int HINT_SUPPORTS_DARK_TEXT = 1 << 0;
    public static final int HINT_SUPPORTS_DARK_THEME = 1 << 1;
    public static int calculateDarkHints(Bitmap source) {
        if (source == null) {
            return 0;
        }

        int[] pixels = new int[source.getWidth() * source.getHeight()];
        double totalLuminance = 0;
        final int maxDarkPixels = (int) (pixels.length * MAX_DARK_AREA);
        int darkPixels = 0;
        source.getPixels(pixels, 0 /* offset */, source.getWidth(), 0 /* x */, 0 /* y */,
                source.getWidth(), source.getHeight());

        // This bitmap was already resized to fit the maximum allowed area.
        // Let's just loop through the pixels, no sweat!
        float[] tmpHsl = new float[3];
        for (int i = 0; i < pixels.length; i++) {
            ColorUtils.colorToHSL(pixels[i], tmpHsl);
            final float luminance = tmpHsl[2];
            final int alpha = Color.alpha(pixels[i]);
            // Make sure we don't have a dark pixel mass that will
            // make text illegible.
            if (luminance < DARK_PIXEL_LUMINANCE && alpha != 0) {
                darkPixels++;
            }
            totalLuminance += luminance;
        }

        int hints = 0;
        double meanLuminance = totalLuminance / pixels.length;
        if (meanLuminance > BRIGHT_IMAGE_MEAN_LUMINANCE && darkPixels < maxDarkPixels) {
            hints |= HINT_SUPPORTS_DARK_TEXT;
        }
        if (meanLuminance < DARK_THEME_MEAN_LUMINANCE) {
            hints |= HINT_SUPPORTS_DARK_THEME;
        }
        return hints;
    }

}