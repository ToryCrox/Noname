package com.miui.home.launcher;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

public class LinkedScreenView extends DragableScreenView {
    protected ScreenView mNextScreenView;
    protected ScreenView mPreScreenView;
    protected ValueAnimator mScrollAnim;
    protected int mScrollDeltaX;
    protected int mScrollStartX;

    public LinkedScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkedScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScrollAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mScrollAnim.setInterpolator(new LinearInterpolator());
        this.mScrollAnim.setDuration(300);
        this.mScrollAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                LinkedScreenView.this.scrollTo((int) (((float) LinkedScreenView.this.mScrollStartX) + (((float) LinkedScreenView.this.mScrollDeltaX) * ((Float) animation.getAnimatedValue()).floatValue())), 0);
            }
        });
    }

    public void setNextView(ScreenView nextView) {
        this.mNextScreenView = nextView;
        setHasSuffixLinkedScreen(this.mNextScreenView != null);
        setScrollWholeScreen(true);
    }

    public void setPreView(ScreenView preView) {
        this.mPreScreenView = preView;
        setHasPrefixLinkedScreen(this.mPreScreenView != null);
        setScrollWholeScreen(true);
    }

    public void scrollTo(int x, int y) {
        int transX;
        if (this.mNextScreenView != null) {
            transX = this.mScrollRightBound - x;
            if (transX < this.mScreenContentWidth) {
                this.mNextScreenView.setVisibility(0);
                this.mNextScreenView.setTranslationX((float) transX);
            }
        }
        if (this.mPreScreenView != null) {
            transX = this.mScrollLeftBound - x;
            if (transX > (-this.mScreenContentWidth)) {
                this.mPreScreenView.setVisibility(0);
                this.mPreScreenView.setTranslationX((float) transX);
            }
        }
        super.scrollTo(x, y);
        if (x == this.mScrollRightBound && this.mHasSuffixLinkedScreen) {
            setVisibility(4);
            this.mScroller.startScroll(this.mScrollX, 0, -this.mScreenContentWidth, 0);
            setTranslationX((float) (-this.mScreenContentWidth));
            this.mNextScreenView.snapToScreen(0);
        }
        if (x == this.mScrollLeftBound && this.mHasPrefixLinkedScreen) {
            setVisibility(4);
            this.mScroller.startScroll(this.mScrollX, 0, this.mScreenContentWidth, 0);
            setTranslationX((float) this.mScreenContentWidth);
            this.mPreScreenView.snapToScreen(this.mPreScreenView.getScreenCount() - 1);
        }
    }

    protected void snapByVelocity(int velocity, int flingDirection) {
        int toIndex = this.mCurrentScreen;
        int snapGap = isLayoutRtl() ? -this.mVisibleRange : this.mVisibleRange;
        if (flingDirection == 1 && this.mCurrentScreen >= 0) {
            toIndex = this.mCurrentScreen - snapGap;
        } else if (flingDirection == 2) {
            toIndex = this.mCurrentScreen + snapGap;
        } else if (flingDirection != 3) {
            toIndex = getSnapUnitIndex(snapGap);
        }
        if (toIndex < 0 && this.mHasPrefixLinkedScreen) {
            this.mScrollStartX = this.mScrollX;
            this.mScrollDeltaX = this.mScrollLeftBound - this.mScrollX;
            this.mScrollAnim.setDuration((long) ((int) ((((float) (-this.mScrollDeltaX)) / ((float) this.mScreenContentWidth)) * 300.0f)));
            this.mScrollAnim.start();
        } else if (toIndex < getScreenCount() || !this.mHasSuffixLinkedScreen) {
            super.snapByVelocity(velocity, flingDirection);
        } else {
            this.mScrollStartX = this.mScrollX;
            this.mScrollDeltaX = this.mScrollRightBound - this.mScrollX;
            this.mScrollAnim.setDuration((long) ((int) ((((float) this.mScrollDeltaX) / ((float) this.mScreenContentWidth)) * 300.0f)));
            this.mScrollAnim.start();
        }
    }

    protected void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mScrollStartX = startX;
        this.mScrollDeltaX = dx;
        this.mScrollAnim.setDuration((long) duration);
        this.mScrollAnim.start();
    }

    public boolean snapToNextScreen() {
        if (getVisibility() != 0) {
            return false;
        }
        if (this.mCurrentScreen + this.mVisibleRange < getScreenCount() || this.mNextScreenView == null) {
            snapToScreen(this.mCurrentScreen + this.mVisibleRange, 0, true);
        } else {
            snapByVelocity(this.mScrollX, 2);
        }
        return true;
    }
}
