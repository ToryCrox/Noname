package com.miui.home.launcher.gadget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import com.miui.home.launcher.AnalyticalDataCollector;
import com.miui.home.launcher.AutoLayoutAnimation;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;

public abstract class Gadget extends FrameLayout implements HostView, WallpaperColorChangedListener {
    private boolean mIsEnableAutoLayoutAnimation = true;
    private boolean mSkipNextAutoLayoutAnimation = false;
    private Runnable mTrackViewRunnable = new Runnable() {
        public void run() {
            Gadget.this.trackView();
        }
    };

    public abstract void onAdded();

    public abstract void onCreate();

    public abstract void onDeleted();

    public abstract void onDestroy();

    public abstract void onEditDisable();

    public abstract void onEditNormal();

    public abstract void onStart();

    public abstract void onStop();

    public abstract void updateConfig(Bundle bundle);

    public Gadget(Context context) {
        super(context);
    }

    public Gadget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Gadget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onResume() {
        postDelayed(this.mTrackViewRunnable, 500);
    }

    public void onPause() {
        removeCallbacks(this.mTrackViewRunnable);
    }

    public void onWallpaperColorChanged() {
        WallpaperUtils.varyViewGroupByWallpaper(this);
    }

    public void addView(View child, int index, LayoutParams params) {
        WallpaperUtils.onAddViewToGroup(this, child, true);
        super.addView(child, index, params);
    }

    public void setEnableAutoLayoutAnimation(boolean isEnable) {
        this.mIsEnableAutoLayoutAnimation = isEnable;
    }

    public void setSkipNextAutoLayoutAnimation(boolean isSkip) {
        this.mSkipNextAutoLayoutAnimation = isSkip;
    }

    public boolean getSkipNextAutoLayoutAnimation() {
        return this.mSkipNextAutoLayoutAnimation;
    }

    public boolean isEnableAutoLayoutAnimation() {
        return this.mIsEnableAutoLayoutAnimation;
    }

    public boolean superSetFrame(int left, int top, int right, int bottom) {
        return super.setFrame(left, top, right, bottom);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        return AutoLayoutAnimation.setFrame(this, left, top, right, bottom);
    }

    public void setGhostView(GhostView gv) {
    }

    public GhostView getGhostView() {
        return null;
    }

    public void buildDrawingCache(boolean autoScale) {
        if (getLayerType() == 1) {
            super.buildDrawingCache(autoScale);
        }
    }

    protected void trackView() {
        if (getTag() instanceof GadgetInfo) {
            AnalyticalDataCollector.trackGadgetView(((GadgetInfo) getTag()).getTitle(getContext().getApplicationContext()));
        }
    }

    protected void trackClick() {
        if (getTag() instanceof GadgetInfo) {
            AnalyticalDataCollector.trackGadgetClick(((GadgetInfo) getTag()).getTitle(getContext().getApplicationContext()));
        }
    }
}
