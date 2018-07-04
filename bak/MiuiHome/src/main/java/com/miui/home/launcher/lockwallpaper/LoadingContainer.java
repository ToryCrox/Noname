package com.miui.home.launcher.lockwallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import com.miui.home.R;

public class LoadingContainer extends FrameLayout {
    private boolean mEnableLoadingAnim = false;
    private ObjectAnimator[] mItemAnimIn = new ObjectAnimator[4];
    private ObjectAnimator[] mItemAnimOut = new ObjectAnimator[4];
    private View[] mLoadItems = new View[4];
    private FrameLayout mLoadingView;
    private boolean mStopLoading = true;

    public LoadingContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onFinishInflate() {
        this.mLoadingView = (FrameLayout) findViewById(R.id.item_container);
        this.mLoadItems[0] = findViewById(R.id.item1);
        this.mLoadItems[1] = findViewById(R.id.item2);
        this.mLoadItems[2] = findViewById(R.id.item3);
        this.mLoadItems[3] = findViewById(R.id.item4);
        for (int i = 0; i < 4; i++) {
            this.mItemAnimIn[i] = getItemAnimIn(this.mLoadItems[i]);
            this.mItemAnimOut[i] = getItemAnimOut(this.mLoadItems[i]);
        }
    }

    public void startLoadingAnim() {
        if (this.mStopLoading && getVisibility() == 0 && this.mEnableLoadingAnim) {
            this.mStopLoading = true;
            this.mLoadingView.setVisibility(0);
            for (View alpha : this.mLoadItems) {
                alpha.setAlpha(0.5f);
            }
            this.mStopLoading = false;
            startLoading();
        }
    }

    public void stopLoadingAnim() {
        this.mStopLoading = true;
    }

    private void resetAlpha() {
        for (View alpha : this.mLoadItems) {
            alpha.setAlpha(0.9f);
        }
    }

    private void startLoading() {
        this.mItemAnimIn[0].addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!LoadingContainer.this.mEnableLoadingAnim || LoadingContainer.this.mStopLoading) {
                    LoadingContainer.this.resetAlpha();
                    return;
                }
                LoadingContainer.this.mItemAnimOut[0].start();
                LoadingContainer.this.mItemAnimIn[1].start();
            }
        });
        this.mItemAnimIn[1].addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!LoadingContainer.this.mEnableLoadingAnim || LoadingContainer.this.mStopLoading) {
                    LoadingContainer.this.resetAlpha();
                    return;
                }
                LoadingContainer.this.mItemAnimOut[1].start();
                LoadingContainer.this.mItemAnimIn[2].start();
            }
        });
        this.mItemAnimIn[2].addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!LoadingContainer.this.mEnableLoadingAnim || LoadingContainer.this.mStopLoading) {
                    LoadingContainer.this.resetAlpha();
                    return;
                }
                LoadingContainer.this.mItemAnimOut[2].start();
                LoadingContainer.this.mItemAnimIn[3].start();
            }
        });
        this.mItemAnimIn[3].addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!LoadingContainer.this.mEnableLoadingAnim || LoadingContainer.this.mStopLoading) {
                    LoadingContainer.this.resetAlpha();
                    return;
                }
                LoadingContainer.this.mItemAnimOut[3].start();
                LoadingContainer.this.mItemAnimIn[0].start();
            }
        });
        this.mItemAnimIn[0].start();
    }

    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == 0) {
            startLoadingAnim();
        } else {
            stopLoadingAnim();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mEnableLoadingAnim = false;
        stopLoadingAnim();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mEnableLoadingAnim = true;
    }

    private ObjectAnimator getItemAnimOut(View v) {
        ObjectAnimator animOut = ObjectAnimator.ofFloat(v, "alpha", new float[]{0.9f, 0.5f});
        animOut.setDuration(250);
        return animOut;
    }

    private ObjectAnimator getItemAnimIn(View v) {
        ObjectAnimator animIn = ObjectAnimator.ofFloat(v, "alpha", new float[]{0.5f, 0.9f});
        animIn.setDuration(250);
        return animIn;
    }
}
