package com.miui.home.launcher.lockwallpaper;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class CustomViewPager extends ViewPager {
    private float mInitialTouchX;
    private float mInitialTouchY;
    private LockWallpaperPreviewView mMainView;
    private ViewConfiguration mViewConfiguration;

    public CustomViewPager(Context context) {
        super(context);
        this.mViewConfiguration = ViewConfiguration.get(context);
    }

    public void setMainView(LockWallpaperPreviewView view) {
        this.mMainView = view;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isFakeDragging()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (isFakeDragging()) {
            return true;
        }
        if (ev.getAction() == 0) {
            this.mInitialTouchX = ev.getRawX();
            this.mInitialTouchY = ev.getRawY();
        } else if (ev.getAction() == 1) {
            int touchSlop = this.mViewConfiguration.getScaledTouchSlop();
            if (Math.abs(this.mInitialTouchX - ev.getRawX()) < ((float) touchSlop) && Math.abs(this.mInitialTouchY - ev.getRawY()) < ((float) touchSlop)) {
                this.mMainView.toggleMenus();
            }
        }
        return super.onTouchEvent(ev);
    }
}
