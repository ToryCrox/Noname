package com.miui.home.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class NonOverlapLinearLayout extends LinearLayout {
    public NonOverlapLinearLayout(Context context) {
        super(context);
    }

    public NonOverlapLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonOverlapLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
