package com.tory.library.lightness;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.MathUtils;

/**
 * Utility methods for working with colors.
 */
public class ColorUtils {

    private ColorUtils() { }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     */
    public static @CheckResult
    @ColorInt
    int modifyAlpha(@ColorInt int color,
                    @IntRange(from = 0, to = 255) int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     */
    public static @CheckResult
    @ColorInt
    int modifyAlpha(@ColorInt int color,
                    @FloatRange(from = 0f, to = 1f) float alpha) {
        return modifyAlpha(color, (int) (255f * alpha));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 0.0 will return {@code color1}, 0.5 will give an even blend,
     *              1.0 will return {@code color2}.
     */
    public static @CheckResult
    @ColorInt
    int blendColors(@ColorInt int color1,
                    @ColorInt int color2,
                    @FloatRange(from = 0f, to = 1f) float ratio) {
        final float inverseRatio = 1f - ratio;
        float a = (Color.alpha(color1) * inverseRatio) + (Color.alpha(color2) * ratio);
        float r = (Color.red(color1) * inverseRatio) + (Color.red(color2) * ratio);
        float g = (Color.green(color1) * inverseRatio) + (Color.green(color2) * ratio);
        float b = (Color.blue(color1) * inverseRatio) + (Color.blue(color2) * ratio);
        return Color.argb((int) a, (int) r, (int) g, (int) b);
    }

    /**
     * Checks if the most populous color in the given palette is dark
     * <p/>
     * Annoyingly we have to return this Lightness 'enum' rather than a boolean as palette isn't
     * guaranteed to find the most populous color.
     */
    @Lightness.LightnessValues
    public static int isDark(Palette palette) {
        Palette.Swatch mostPopulous = getMostPopulousSwatch(palette);
        if (mostPopulous == null) return Lightness.UNKNOWN;
        return isDark(mostPopulous.getHsl()) ? Lightness.DARK : Lightness.LIGHT;
    }

    /**
     * @see Palette#getDominantSwatch()
     * @param palette
     * @return
     */
    @Nullable
    public static Palette.Swatch getMostPopulousSwatch(Palette palette) {
        Palette.Swatch mostPopulous = null;
        if (palette != null) {
            int maxPop = Integer.MIN_VALUE;
            for (Palette.Swatch swatch : palette.getSwatches()) {
                if (swatch.getPopulation() > maxPop) {
                    mostPopulous = swatch;
                    maxPop = swatch.getPopulation();
                }
            }
        }
        return mostPopulous;
    }

    /**
     * Determines if a given bitmap Lightness
     * @param bitmap
     * @return
     */
    @Lightness.LightnessValues
    public static int checkLightness(@NonNull Bitmap bitmap){
        Palette palette = Palette.from(bitmap).generate();
        return isDark(palette);
    }

    /**
     * Determines if a given bitmap Lightness
     * @param bitmap
     * @return
     */
    public static int checkLightness(@NonNull Bitmap bitmap, @Lightness.LightnessValues int def){
        int light = checkLightness(bitmap);
        return (light != Lightness.UNKNOWN) ? light : def;
    }

    /**
     * Determines if a given bitmap is dark. This extracts a palette inline so should not be called
     * with a large image!!
     * <p/>
     * Note: If palette fails then check the color of the central pixel
     */
    public static boolean isDark(@NonNull Bitmap bitmap) {
        return isDark(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
    }

    /**
     * Determines if a given bitmap is dark. This extracts a palette inline so should not be called
     * with a large image!! If palette fails then check the color of the specified pixel
     */
    public static boolean isDark(@NonNull Bitmap bitmap, int backupPixelX, int backupPixelY) {
        // first try palette with a small color quant size
        Palette palette = Palette.from(bitmap).maximumColorCount(3).generate();
        if (palette != null && palette.getSwatches().size() > 0) {
            return isDark(palette) == Lightness.DARK;
        } else {
            // if palette failed, then check the color of the specified pixel
            return isDark(bitmap.getPixel(backupPixelX, backupPixelY));
        }
    }

    /**
     * Check that the com.tory.library.lightness value (0–1)
     */
    public static boolean isDark(float[] hsl) { // @Size(3)
        return hsl[2] < 0.5f;
    }

    /**
     * Convert to HSL & check that the com.tory.library.lightness value
     */
    public static boolean isDark(@ColorInt int color) {
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);
        return isDark(hsl);
    }

    /**
     * Calculate a variant of the color to make it more suitable for overlaying information. Light
     * colors will be lightened and dark colors will be darkened
     *
     * @param color the color to adjust
     * @param isDark whether {@code color} is light or dark
     * @param lightnessMultiplier the amount to modify the color e.g. 0.1f will alter it by 10%
     * @return the adjusted color
     */
    public static @ColorInt
    int scrimify(@ColorInt int color,
                 boolean isDark,
                 @FloatRange(from = 0f, to = 1f) float lightnessMultiplier) {
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);

        if (!isDark) {
            lightnessMultiplier += 1f;
        } else {
            lightnessMultiplier = 1f - lightnessMultiplier;
        }


        hsl[2] = MathUtils.constrain(0f, 1f, hsl[2] * lightnessMultiplier);
        return android.support.v4.graphics.ColorUtils.HSLToColor(hsl);
    }

    public static @ColorInt
    int scrimify(@ColorInt int color,
                 @FloatRange(from = 0f, to = 1f) float lightnessMultiplier) {
        return scrimify(color, isDark(color), lightnessMultiplier);
    }

}