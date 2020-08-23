package com.tory.library.colorpicker;

import android.graphics.drawable.Drawable;

/**
 * Created by tao.xu2 on 2017/5/17.
 */

public class ColorItem {

    public int color;

    public Drawable colorDrawable;

    @Override
    public String toString() {
        return "ColorItem{" +
                "color=" + Integer.toHexString(color) +
                ", colorDrawable=" + colorDrawable +
                '}';
    }
}
