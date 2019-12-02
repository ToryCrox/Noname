/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tory.debug;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;


/**
 * Provides static functions to work with views
 */
public class ViewUtils {
    private static final String TAG = "ViewUtils";
    private ViewUtils() {}


    /**
     * Returns a boolean indicating whether or not the view's layout direction is RTL
     *
     * @param view - A valid view
     * @return True if the view's layout direction is RTL
     */
    public static boolean isViewLayoutRtl(View view) {
        return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }


    public static void resizeText(@NonNull TextView textView,
                                  int originalTextSize, int minTextSize) {
        final Paint paint = textView.getPaint();
        final int width = textView.getWidth() - textView.getPaddingLeft()
                - textView.getPaddingRight();
        if (width == 0 || originalTextSize == 0 || minTextSize == 0) return;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize);
        float ratio = width / paint.measureText(textView.getText().toString());
        if (ratio <= 1.0f) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    Math.max(minTextSize, originalTextSize * ratio));
        }
    }

    /**
     * Calculates the height of a given string at a specific text size.
     */
    public static int calculateTextHeight(float textSizePx) {
        Paint p = new Paint();
        p.setTextSize(textSizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return (int) Math.ceil(fm.bottom - fm.top);
    }

    public static void setBgAlpha(View view, float alpha) {
        Drawable bg = view.getBackground();
        if (bg != null) {
            bg.setAlpha((int) (255 * alpha));
        }
    }

    public static Bitmap coverToBitmap(@NonNull View view){
        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0){
            //LogUtils.e(TAG, "coverToBitmap error view width="+width + ", height="+height);
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }

    public static Drawable coverToDrawable(@NonNull View view){
        Bitmap bitmap = coverToBitmap(view);
        if (bitmap == null){
            return null;
        }
        return new BitmapDrawable(view.getResources(), bitmap);
    }
}
