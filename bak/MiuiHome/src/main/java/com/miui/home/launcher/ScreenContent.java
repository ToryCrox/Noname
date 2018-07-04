package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class ScreenContent extends FitSystemWindowView {
    private Launcher mLauncher;

    public ScreenContent(Context context) {
        super(context);
    }

    public ScreenContent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScreenContent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    protected boolean fitSystemWindows(Rect insets) {
        boolean ret = super.fitSystemWindows(insets);
        if (this.mLauncher == null || !this.mLauncher.isFolderShowing()) {
            return ret;
        }
        return false;
    }
}
