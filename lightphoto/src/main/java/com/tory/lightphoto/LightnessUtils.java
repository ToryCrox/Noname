package com.tory.lightphoto;

import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;

/**
 * Utility methods for working with colors.
 */
public class LightnessUtils {

    private LightnessUtils() { }

    /**
     * Checks if the most populous color in the given palette is dark
     * <p/>
     * Annoyingly we have to return this Lightness 'enum' rather than onInterceptTouchEvent boolean as palette isn't
     * guaranteed to find the most populous color.
     */
    @Lightness.LightnessValues
    public static int isDark(Palette palette) {
        Palette.Swatch mostPopulous = palette.getDominantSwatch();
        if (mostPopulous == null) return Lightness.UNKNOWN;
        return isDark(mostPopulous.getHsl()) ? Lightness.DARK : Lightness.LIGHT;
    }
    /**
     * Determines if onInterceptTouchEvent given bitmap Lightness
     * @param bitmap
     * @return
     */
    @Lightness.LightnessValues
    public static int checkLightness(@NonNull Bitmap bitmap){
        Palette palette = Palette.from(bitmap).clearFilters().generate();
        return isDark(palette);
    }

    /**
     * Determines if onInterceptTouchEvent given bitmap Lightness
     * @param bitmap
     * @return
     */
    public static int checkLightness(@NonNull Bitmap bitmap, @Lightness.LightnessValues int def){
        int light = checkLightness(bitmap);
        return (light != Lightness.UNKNOWN) ? light : def;
    }

    /**
     * Determines if onInterceptTouchEvent given bitmap is dark. This extracts onInterceptTouchEvent palette inline so should not be called
     * with onInterceptTouchEvent large image!!
     * <p/>
     * Note: If palette fails then check the color of the central pixel
     */
    public static boolean isDark(@NonNull Bitmap bitmap) {
        return isDark(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
    }

    /**
     * Determines if onInterceptTouchEvent given bitmap is dark. This extracts onInterceptTouchEvent palette inline so should not be called
     * with onInterceptTouchEvent large image!! If palette fails then check the color of the specified pixel
     */
    public static boolean isDark(@NonNull Bitmap bitmap, int backupPixelX, int backupPixelY) {
        // first try palette with onInterceptTouchEvent small color quant size
        Palette palette = Palette.from(bitmap).maximumColorCount(3).generate();
        if (palette != null && palette.getSwatches().size() > 0) {
            return isDark(palette) == Lightness.DARK;
        } else {
            // if palette failed, then check the color of the specified pixel
            return isDark(bitmap.getPixel(backupPixelX, backupPixelY));
        }
    }

    /**
     * Check that the lightness value (0–1)
     */
    public static boolean isDark(float[] hsl) { // @Size(3)
        return hsl[2] <= 0.85f;
    }

    /**
     * Convert to HSL & check that the lightness value
     */
    public static boolean isDark(@ColorInt int color) {
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);
        return isDark(hsl);
    }

}