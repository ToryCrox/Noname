package com.miui.home.launcher.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class AutoFadeInImageView extends View {
    private ValueAnimator mAnimFadeIn = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
    private ValueAnimator mAnimFadeOut;
    private Drawable mCurrentDrawable;
    private int mDrawableAlpha = 255;
    private Drawable mNextDrawable;

    public AutoFadeInImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mAnimFadeIn.setInterpolator(new LinearInterpolator());
        this.mAnimFadeIn.setDuration(300);
        this.mAnimFadeIn.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                AutoFadeInImageView.this.mCurrentDrawable.setAlpha((int) (((float) AutoFadeInImageView.this.mDrawableAlpha) * ((Float) animation.getAnimatedValue()).floatValue()));
                AutoFadeInImageView.this.invalidate();
            }
        });
        this.mAnimFadeIn.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (AutoFadeInImageView.this.mCurrentDrawable != null) {
                    AutoFadeInImageView.this.mCurrentDrawable.setAlpha(AutoFadeInImageView.this.mDrawableAlpha);
                    AutoFadeInImageView.this.invalidate();
                }
            }
        });
        this.mAnimFadeOut = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        this.mAnimFadeOut.setInterpolator(new LinearInterpolator());
        this.mAnimFadeOut.setDuration(300);
        this.mAnimFadeOut.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                AutoFadeInImageView.this.mCurrentDrawable.setAlpha((int) (((float) AutoFadeInImageView.this.mDrawableAlpha) * ((Float) animation.getAnimatedValue()).floatValue()));
                AutoFadeInImageView.this.invalidate();
            }
        });
        this.mAnimFadeOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                AutoFadeInImageView.this.mCurrentDrawable = AutoFadeInImageView.this.mNextDrawable;
                if (AutoFadeInImageView.this.mCurrentDrawable != null) {
                    AutoFadeInImageView.this.mCurrentDrawable.setAlpha(AutoFadeInImageView.this.mDrawableAlpha);
                    AutoFadeInImageView.this.invalidate();
                    AutoFadeInImageView.this.mAnimFadeIn.start();
                }
            }
        });
    }

    public boolean inFadeAnim() {
        return this.mAnimFadeIn.isRunning() || this.mAnimFadeOut.isRunning();
    }

    public void setDrawableAlpha(int alpha) {
        this.mDrawableAlpha = alpha;
    }

    public void changeDrawable(Drawable d, boolean withAnim) {
        this.mAnimFadeOut.cancel();
        this.mAnimFadeIn.cancel();
        if (this.mCurrentDrawable == null || !withAnim) {
            this.mCurrentDrawable = d;
            if (this.mCurrentDrawable != null) {
                if (withAnim) {
                    this.mAnimFadeIn.start();
                } else {
                    this.mCurrentDrawable.setAlpha(this.mDrawableAlpha);
                }
            }
            invalidate();
            return;
        }
        this.mNextDrawable = d;
        this.mAnimFadeOut.start();
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mCurrentDrawable != null) {
            canvas.save();
            this.mCurrentDrawable.draw(canvas);
            canvas.restore();
        }
    }
}
