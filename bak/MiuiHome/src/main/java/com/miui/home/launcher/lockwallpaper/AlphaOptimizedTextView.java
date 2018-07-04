package com.miui.home.launcher.lockwallpaper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AlphaOptimizedTextView extends TextView {
    public AlphaOptimizedTextView(Context context) {
        super(context);
    }

    public AlphaOptimizedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphaOptimizedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
