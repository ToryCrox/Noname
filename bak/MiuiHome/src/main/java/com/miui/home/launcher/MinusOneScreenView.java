package com.miui.home.launcher;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.miui.home.R;

public class MinusOneScreenView extends ScreenView {
    private int mBackgroundColor;
    private int mCurrentScreenIndex;
    private boolean mIsSolvedByChild;
    private MotionEvent mLastDownEvent;
    private Launcher mLauncher;
    private VelocityTracker mVelocityTracker;

    public MinusOneScreenView(Context context) {
        this(context, null);
    }

    public MinusOneScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MinusOneScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentScreenIndex = -1;
        this.mIsSolvedByChild = true;
        this.mLastDownEvent = null;
        this.mLauncher = LauncherApplication.getLauncher(getContext());
        this.mBackgroundColor = context.getResources().getColor(R.color.minus_one_background_mask);
    }

    protected void setCurrentScreenInner(int screenIndex) {
        if (this.mCurrentScreenIndex != screenIndex) {
            Intent intent = new Intent("miui.intent.action.MINUS_SCREEN_UPDATE");
            if (screenIndex == 0) {
                intent.putExtra("hasLightBgForStatusBar", WallpaperUtils.hasLightBgForStatusBar());
            } else {
                intent.putExtra("leavePersonalAssistant", true);
            }
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            this.mCurrentScreenIndex = screenIndex;
        }
        super.setCurrentScreenInner(screenIndex);
        this.mLauncher.updateStatusBarClock();
    }

    public void computeScroll() {
        super.computeScroll();
        setBackgroundColor(Color.argb((int) (((float) Color.alpha(this.mBackgroundColor)) * Math.min(1.0f, 1.0f - (((float) Math.min(getScrollX(), getChildScreenMeasureWidth())) / ((float) getChildScreenMeasureWidth())))), Color.red(this.mBackgroundColor), Color.green(this.mBackgroundColor), Color.blue(this.mBackgroundColor)));
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(ev);
        if (this.mLauncher.isInNormalEditing() || this.mLauncher.getWorkspace().getCurrentScreenIndex() != 0 || this.mLauncher.getDragController().isDragging() || this.mLauncher.isFolderShowing() || this.mLauncher.getDragLayer().isWidgetBeingResized() || this.mLauncher.isSceneShowing()) {
            return getChildAt(1).dispatchTouchEvent(ev);
        }
        if (getCurrentScreenIndex() == 0) {
            return super.dispatchTouchEvent(ev);
        }
        if (ev.getPointerCount() <= 1 || getTouchState() == 1) {
            if (ev.getAction() == 0) {
                this.mIsSolvedByChild = getChildAt(1).dispatchTouchEvent(ev);
                this.mLastDownEvent = MotionEvent.obtain(ev);
            }
            if (ev.getAction() == 2 && ev.getPointerCount() == 1) {
                this.mVelocityTracker.computeCurrentVelocity(500);
                MotionEvent cancel;
                if (this.mIsSolvedByChild) {
                    if (this.mLauncher.getWorkspace().getScrollX() < 0 || (this.mLauncher.getWorkspace().getScrollX() == 0 && this.mVelocityTracker.getXVelocity() > 300.0f)) {
                        cancel = MotionEvent.obtain(ev);
                        cancel.setAction(3);
                        this.mLauncher.getWorkspace().dispatchTouchEvent(cancel);
                        if (getScrollX() > getChildScreenMeasureWidth()) {
                            setScrollX(getChildScreenMeasureWidth());
                        }
                        super.dispatchTouchEvent(this.mLastDownEvent);
                        this.mIsSolvedByChild = false;
                        cancel.recycle();
                    } else {
                        this.mIsSolvedByChild = getChildAt(1).dispatchTouchEvent(ev);
                    }
                } else if (getScrollX() > getChildScreenMeasureWidth() || (getScrollX() == getChildScreenMeasureWidth() && this.mVelocityTracker.getXVelocity() < -300.0f)) {
                    cancel = MotionEvent.obtain(ev);
                    cancel.setAction(3);
                    onTouchEvent(cancel);
                    if (this.mLauncher.getWorkspace().getScrollX() < 0) {
                        this.mLauncher.getWorkspace().setScrollX(0);
                    }
                    getChildAt(1).dispatchTouchEvent(this.mLastDownEvent);
                    this.mIsSolvedByChild = true;
                    cancel.recycle();
                }
            }
            if (ev.getAction() == 1) {
                if (this.mIsSolvedByChild) {
                    this.mIsSolvedByChild = false;
                    return getChildAt(1).dispatchTouchEvent(ev);
                }
                this.mVelocityTracker.clear();
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            if (this.mIsSolvedByChild) {
                return true;
            }
            return super.dispatchTouchEvent(ev);
        }
        this.mIsSolvedByChild = true;
        return getChildAt(1).dispatchTouchEvent(ev);
    }
}
