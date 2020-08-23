package com.tory.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Pair;

import java.util.WeakHashMap;

public class GrayImageUtil {

    // Amount (max is 255) that two channels can differ before the color is no longer "gray".
    private static final int TOLERANCE = 30;

    // Alpha amount for which values below are considered transparent.
    private static final int ALPHA_TOLERANCE = 130;

    // Size of the smaller bitmap we're actually going to scan.
    private static final int COMPACT_BITMAP_SIZE = 72; // pixels

    private int[] mTempBuffer;
    private Bitmap mTempCompactBitmap;
    private Canvas mTempCompactBitmapCanvas;
    private Paint mTempCompactBitmapPaint;
    private final Matrix mTempMatrix = new Matrix();
    private int mBaseR;
    private int mBaseG;
    private int mBaseB;
    private static GrayImageUtil mInstance;


    private static final Object sLock = new Object();
    private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache =
            new WeakHashMap<Bitmap, Pair<Boolean, Integer>>();

    private GrayImageUtil(Context context){
    }

    public static GrayImageUtil getInstance(Context context){
        if(mInstance == null){
            mInstance = new GrayImageUtil(context);
        }
        return mInstance;
    }


    public boolean isGrayscale(Drawable d) {
        if (d == null) {
            return false;
        } else if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            return bd.getBitmap() != null && isGrayscale(bd.getBitmap());
        } else if (d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            int count = ad.getNumberOfFrames();
            return count > 0 && isGrayscale(ad.getFrame(0));
        } else if (d instanceof VectorDrawable) {
            // We just assume you're doing the right thing if using vectors
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks whether a Bitmap is a small grayscale icon.
     * Grayscale here means "very close to a perfect gray"; icon means "no larger than 64dp".
     *
     * @param bitmap The bitmap to test.
     * @return True if the bitmap is grayscale; false if it is color or too large to examine.
     */
    public boolean isGrayscale(Bitmap bitmap) {
        // quick test: reject large bitmaps
        if (bitmap == null || bitmap.isRecycled()) {
            return false;
        }

        synchronized (sLock) {
            Pair<Boolean, Integer> cached = mGrayscaleBitmapCache.get(bitmap);
            if (cached != null) {
                if (cached.second == bitmap.getGenerationId()) {
                    return cached.first;
                }
            }
        }
        boolean result;
        int generationId;
        synchronized (GrayImageUtil.this) {
            result = isGrayscaleBitmap(bitmap);

            // generationId and the check whether the Bitmap is grayscale can't be read atomically
            // here. However, since the thread is in the process of posting the notification, we can
            // assume that it doesn't modify the bitmap while we are checking the pixels.
            generationId = bitmap.getGenerationId();
        }
        synchronized (sLock) {
            mGrayscaleBitmapCache.put(bitmap, Pair.create(result, generationId));
        }
        return result;
    }


    public boolean isGrayscaleBitmap(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        // shrink to a more manageable (yet hopefully no more or less colorful) size
        if (height > COMPACT_BITMAP_SIZE || width > COMPACT_BITMAP_SIZE) {
            if (mTempCompactBitmap == null) {
                mTempCompactBitmap = Bitmap.createBitmap(
                        COMPACT_BITMAP_SIZE, COMPACT_BITMAP_SIZE, Bitmap.Config.ARGB_8888
                );
                mTempCompactBitmapCanvas = new Canvas(mTempCompactBitmap);
                mTempCompactBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mTempCompactBitmapPaint.setFilterBitmap(true);
            }
            mTempMatrix.reset();
            mTempMatrix.setScale(
                    (float) COMPACT_BITMAP_SIZE / width,
                    (float) COMPACT_BITMAP_SIZE / height,
                    0, 0);
            mTempCompactBitmapCanvas.drawColor(0, PorterDuff.Mode.SRC); // select all, erase
            mTempCompactBitmapCanvas.drawBitmap(bitmap, mTempMatrix, mTempCompactBitmapPaint);
            bitmap = mTempCompactBitmap;
            width = height = COMPACT_BITMAP_SIZE;
        }

        final int size = height*width;
        ensureBufferSize(size);
        bitmap.getPixels(mTempBuffer, 0, width, 0, 0, width, height);

        mBaseR = mBaseG = mBaseB = 0;
        for(int i = 0; i < size - 6; i+=5){
            int alpha = 0xFF & (mTempBuffer[i] >> 24);
            int r = 0xFF & (mTempBuffer[i] >> 16);
            int g = 0xFF & (mTempBuffer[i] >> 8);
            int b = 0xFF & mTempBuffer[i];

            if(alpha < ALPHA_TOLERANCE){
                continue;
            }

            if(mBaseR == 0 && mBaseG == 0 && mBaseB == 0){
                mBaseR = r;
                mBaseG = g;
                mBaseB = b;
            }

            if(Math.abs(r - mBaseR) > TOLERANCE
                   ||Math.abs(g - mBaseG) > TOLERANCE
                   ||Math.abs(b - mBaseB) > TOLERANCE){
               return false;
            }

        }
        return true;
    }

    private void ensureBufferSize(int size) {
        if (mTempBuffer == null || mTempBuffer.length < size) {
            mTempBuffer = new int[size];
        }
    }

    public static boolean isAlpha(int color) {
        int alpha = 0xFF & (color >> 24);
        if (alpha < ALPHA_TOLERANCE) {
            return true;
        }
        return false;
    }
    public static boolean isSameColor(int color , int colorOffset) {
        int r = 0xFF & (color >> 16);
        int g = 0xFF & (color >> 8);
        int b = 0xFF & color;

        int ro = 0xFF & (colorOffset >> 16);
        int go = 0xFF & (colorOffset >> 8);
        int bo = 0xFF & colorOffset;

        return Math.abs(r - ro) < TOLERANCE
                && Math.abs(g - go) < TOLERANCE
                && Math.abs(b - bo) < TOLERANCE; 
    }
}
