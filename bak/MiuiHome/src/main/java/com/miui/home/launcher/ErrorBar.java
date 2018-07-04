package com.miui.home.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;

public class ErrorBar extends TextView implements WallpaperColorChangedListener {
    private Runnable mCloseErrorBar = new Runnable() {
        public void run() {
            ErrorBar.this.hideError(true);
        }
    };
    private Animation mFadeIn = AnimationUtils.loadAnimation(getContext(), 17432576);
    private Animation mFadeOut = AnimationUtils.loadAnimation(getContext(), 17432577);
    private boolean mLastForceHide = false;
    private Launcher mLauncher;
    private boolean mShowing = false;

    public ErrorBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mFadeOut.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                ErrorBar.this.mShowing = false;
                if (!ErrorBar.this.mLastForceHide) {
                    if (ErrorBar.this.mLauncher.isShowingEditingTips()) {
                        ErrorBar.this.mLauncher.fadeInEditingTips(true);
                    }
                    if (7 == ErrorBar.this.mLauncher.getEditingState() && !ErrorBar.this.mLastForceHide) {
                        ErrorBar.this.mLauncher.showStatusBar(true);
                    }
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    void showError(int resId) {
        showErrorOrWarning(resId, false);
    }

    private void showErrorOrWarning(int resId, boolean isWaring) {
        this.mShowing = true;
        setText(resId);
        setVisibility(0);
        startAnimation(this.mFadeIn);
        removeCallbacks(this.mCloseErrorBar);
        postDelayed(this.mCloseErrorBar, (long) getContext().getResources().getInteger(R.integer.error_notification_duration));
    }

    public void forceToHide() {
        hideError(false);
    }

    void hideError(boolean withFadeOutAnimation) {
        if (this.mShowing) {
            setVisibility(4);
            if (withFadeOutAnimation) {
                this.mLastForceHide = false;
                startAnimation(this.mFadeOut);
            } else {
                this.mShowing = false;
                this.mLastForceHide = true;
                removeCallbacks(this.mCloseErrorBar);
            }
            if (!withFadeOutAnimation && this.mLauncher.isShowingEditingTips()) {
                this.mLauncher.fadeInEditingTips(false);
            } else if (this.mLauncher.getEditingState() == 7) {
                this.mLauncher.showStatusBar(true);
            }
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public void onWallpaperColorChanged() {
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.notification.dark);
        } else {
            setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.notification);
        }
    }
}
