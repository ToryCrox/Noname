package com.aleaf.viplist;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author tory
 * @date 2018/12/25
 * @des:
 */
public class MaskColorDrawable extends Drawable {

    private Drawable mBaseDrawable;

    public MaskColorDrawable(@NonNull Drawable drawable){
        mBaseDrawable = drawable;
        mBaseDrawable.setBounds(0, 0,
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }


    @Override
    public void draw(@NonNull Canvas canvas) {
        mBaseDrawable.draw(canvas);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
