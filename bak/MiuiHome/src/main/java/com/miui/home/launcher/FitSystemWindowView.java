package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class FitSystemWindowView extends FrameLayout {
    public FitSystemWindowView(Context context) {
        super(context);
    }

    public FitSystemWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FitSystemWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected boolean fitSystemWindows(Rect insets) {
        boolean ret = super.fitSystemWindows(insets);
        setPadding(0, 0, 0, getPaddingBottom());
        return ret;
    }
}
