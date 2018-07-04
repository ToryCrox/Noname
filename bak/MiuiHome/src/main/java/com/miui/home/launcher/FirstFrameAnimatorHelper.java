package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;

public class FirstFrameAnimatorHelper extends AnimatorListenerAdapter implements AnimatorUpdateListener {
    private static long sGlobalFrameCounter;
    private static boolean sVisible;
    private boolean mAdjustedSecondFrameTime;
    private boolean mHandlingOnAnimationUpdate;
    private long mStartFrame;
    private long mStartTime = -1;
    private View mTarget;

    public FirstFrameAnimatorHelper(ValueAnimator animator, View target) {
        this.mTarget = target;
        animator.addUpdateListener(this);
    }

    public void onAnimationStart(Animator animation) {
        ValueAnimator va = (ValueAnimator) animation;
        va.addUpdateListener(this);
        onAnimationUpdate(va);
    }

    public void onAnimationUpdate(final ValueAnimator animation) {
        long currentTime = System.currentTimeMillis();
        if (this.mStartTime == -1) {
            this.mStartFrame = sGlobalFrameCounter;
            this.mStartTime = currentTime;
        }
        if (!this.mHandlingOnAnimationUpdate && sVisible && animation.getCurrentPlayTime() < animation.getDuration()) {
            this.mHandlingOnAnimationUpdate = true;
            long frameNum = sGlobalFrameCounter - this.mStartFrame;
            if (frameNum == 0 && currentTime < this.mStartTime + 1000) {
                this.mTarget.getRootView().invalidate();
                animation.setCurrentPlayTime(0);
            } else if (frameNum == 1 && currentTime < this.mStartTime + 1000 && !this.mAdjustedSecondFrameTime && currentTime > this.mStartTime + 16) {
                animation.setCurrentPlayTime(16);
                this.mAdjustedSecondFrameTime = true;
            } else if (frameNum > 1) {
                this.mTarget.post(new Runnable() {
                    public void run() {
                        animation.removeUpdateListener(FirstFrameAnimatorHelper.this);
                    }
                });
            }
            this.mHandlingOnAnimationUpdate = false;
        }
    }
}
