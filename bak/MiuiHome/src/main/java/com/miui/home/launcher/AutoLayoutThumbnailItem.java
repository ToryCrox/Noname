package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.ScreenView.GroupModeItem;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;

public class AutoLayoutThumbnailItem extends OnLongClickWrapper implements HostView, GroupModeItem, WallpaperColorChangedListener {
    public TextView contentTitle;
    public ThumbnailIcon icon;
    public ImageView iconBackground;
    public ImageView iconForeground;
    private boolean mSkipNextAutoLayoutAnimation;
    public TextView title;

    public AutoLayoutThumbnailItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mSkipNextAutoLayoutAnimation = false;
    }

    public AutoLayoutThumbnailItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.icon = (ThumbnailIcon) findViewById(R.id.icon);
        this.iconBackground = (ImageView) findViewById(R.id.background);
        this.iconForeground = (ImageView) findViewById(R.id.foreground);
        this.title = (TextView) findViewById(R.id.title);
        this.contentTitle = (TextView) findViewById(R.id.content_title);
        setLayerType(2, null);
    }

    private void startFadeAnim(final int fadeMode) {
        ValueAnimator fadeOut = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOut.setDuration(300);
        fadeOut.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                AutoLayoutThumbnailItem autoLayoutThumbnailItem = AutoLayoutThumbnailItem.this;
                if (fadeMode != -1) {
                    value = 1.0f - value;
                }
                autoLayoutThumbnailItem.setAlpha(value);
            }
        });
        fadeOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
            }
        });
        fadeOut.start();
    }

    public void setEnableAutoLayoutAnimation(boolean isEnable) {
    }

    public void onFolding() {
        startFadeAnim(1);
    }

    public void onUnFolding() {
        startFadeAnim(-1);
    }

    public boolean isEnableAutoLayoutAnimation() {
        return true;
    }

    public void setSkipNextAutoLayoutAnimation(boolean isSkip) {
        this.mSkipNextAutoLayoutAnimation = isSkip;
    }

    public boolean getSkipNextAutoLayoutAnimation() {
        return this.mSkipNextAutoLayoutAnimation;
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

    public void onWallpaperColorChanged() {
        if (this.title != null) {
            if (WallpaperUtils.hasAppliedLightWallpaper()) {
                setTitleColorMode(true, this.title);
            } else {
                setTitleColorMode(false, this.title);
            }
            invalidate();
        }
    }

    public void setTitleColorMode(boolean isDark, TextView title) {
        if (isDark) {
            title.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.Thumbnail.dark);
            title.setShadowLayer(title.getShadowRadius(), title.getShadowDx(), title.getShadowDy(), getContext().getResources().getColor(R.color.icon_title_text_shadow_light));
            return;
        }
        title.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.Thumbnail);
        title.setShadowLayer(title.getShadowRadius(), title.getShadowDx(), title.getShadowDy(), getContext().getResources().getColor(R.color.icon_title_text_shadow));
    }
}
