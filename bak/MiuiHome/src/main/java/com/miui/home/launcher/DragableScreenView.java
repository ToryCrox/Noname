package com.miui.home.launcher;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DragableScreenView extends ScreenView implements DragScroller {
    protected Handler mDragScrollHandler;
    protected ScrollRunnable mDragScrollRunnable;
    protected int mDragScrollState;

    protected class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (this.mDirection == 0) {
                DragableScreenView.this.scrollDragingLeft();
            } else {
                DragableScreenView.this.scrollDragingRight();
            }
            DragableScreenView.this.mDragScrollState = 0;
        }

        public void setDirection(int direction) {
            this.mDirection = direction;
        }
    }

    public DragableScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragableScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mDragScrollState = 0;
        this.mDragScrollRunnable = new ScrollRunnable();
        this.mDragScrollHandler = new Handler();
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int i = 0;
        super.onTouchEvent(ev);
        switch (ev.getAction() & 255) {
            case 2:
                if (getTouchState() == 6) {
                    int x = (int) ev.getX(ev.findPointerIndex(this.mActivePointerId));
                    if (x >= 30 && x <= getWidth() - 30) {
                        if (this.mDragScrollState == 1) {
                            this.mDragScrollState = 0;
                            this.mDragScrollHandler.removeCallbacks(this.mDragScrollRunnable);
                            break;
                        }
                    } else if (this.mDragScrollState == 0) {
                        this.mDragScrollState = 1;
                        ScrollRunnable scrollRunnable = this.mDragScrollRunnable;
                        if (x >= 30) {
                            i = 1;
                        }
                        scrollRunnable.setDirection(i);
                        this.mDragScrollHandler.postDelayed(this.mDragScrollRunnable, 600);
                        break;
                    }
                }
                break;
        }
        return true;
    }

    public void scrollDragingLeft() {
        if (isScrolling()) {
            if (this.mNextScreen > 0) {
                snapToScreen(this.mNextScreen - 1);
            }
        } else if (this.mCurrentScreen > 0) {
            snapToScreen(this.mCurrentScreen - 1);
        }
    }

    public void scrollDragingRight() {
        if (isScrolling()) {
            if (this.mNextScreen < getScreenCount() - 1) {
                snapToScreen(this.mNextScreen + 1);
            }
        } else if (this.mCurrentScreen < getScreenCount() - 1) {
            snapToScreen(this.mCurrentScreen + 1);
        }
    }

    public void onSecondaryPointerDown(MotionEvent ev, int pointerId) {
        super.onSecondaryPointerDown(ev, pointerId);
    }

    public void onSecondaryPointerUp(MotionEvent ev, int pointerId) {
        super.onSecondaryPointerUp(ev, pointerId);
    }

    public void onSecondaryPointerMove(MotionEvent ev, int pointerId) {
        super.onSecondaryPointerMove(ev, pointerId);
    }

    public boolean onEnterScrollArea(int x, int y, int direction) {
        return true;
    }

    public boolean onExitScrollArea() {
        return true;
    }
}
