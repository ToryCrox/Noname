package com.miui.home.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.AutoFadeInImageView;
import com.miui.home.launcher.common.Utilities;

public class Background extends AutoFadeInImageView implements WallpaperColorChangedListener {
    private Drawable mEditingDrawable;
    private Drawable mEditingLandscapeDrawable;
    private boolean mIsEditingMode = false;
    private boolean mNeedShowStatusbar = true;
    private Drawable mPreviewDrawable;
    private boolean mShowUninstallBgColor = false;
    private Drawable mStatusbarDrawable;
    private Drawable mWallpaperDrawable;

    public Background(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawableAlpha(255);
        Resources res = context.getResources();
        this.mStatusbarDrawable = Utilities.loadThemeCompatibleDrawable(context, R.drawable.statusbar_bg);
        this.mWallpaperDrawable = Utilities.loadThemeCompatibleDrawable(context, R.drawable.wallpaper_mask);
        this.mEditingDrawable = Utilities.loadThemeCompatibleDrawable(context, R.drawable.editing_bg);
        if (DeviceConfig.isRotatable()) {
            this.mEditingLandscapeDrawable = Utilities.loadThemeCompatibleDrawable(context, R.drawable.editing_bg_landscape);
        }
        this.mPreviewDrawable = res.getDrawable(R.color.preview_background);
        setNormalMode();
    }

    public void onScreenOrientationChanged() {
        if (this.mIsEditingMode) {
            setEnterEditingMode();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mStatusbarDrawable != null) {
            this.mStatusbarDrawable.setBounds(0, 0, getWidth(), this.mStatusbarDrawable.getIntrinsicHeight());
        }
        if (this.mWallpaperDrawable != null) {
            int wallpaperHeight = this.mWallpaperDrawable.getIntrinsicHeight();
            if (this.mWallpaperDrawable instanceof BitmapDrawable) {
                wallpaperHeight = ((BitmapDrawable) this.mWallpaperDrawable).getBitmap().getHeight();
            }
            this.mWallpaperDrawable.setBounds(0, getHeight() - wallpaperHeight, getWidth(), getHeight());
        }
        if (!(this.mEditingDrawable == null || DeviceConfig.isScreenOrientationLandscape())) {
            this.mEditingDrawable.setBounds(0, 0, getWidth(), getHeight());
        }
        if (this.mEditingLandscapeDrawable != null && DeviceConfig.isScreenOrientationLandscape()) {
            this.mEditingLandscapeDrawable.setBounds(0, 0, getWidth(), getHeight());
        }
        this.mPreviewDrawable.setBounds(0, 0, getWidth(), getHeight());
    }

    private void setNormalMode() {
        this.mNeedShowStatusbar = true;
        changeDrawable(null, true);
    }

    public void setEnterPreviewMode() {
        this.mNeedShowStatusbar = false;
        changeDrawable(this.mPreviewDrawable, true);
    }

    public void setExitPreviewMode() {
        setNormalMode();
    }

    public void setEnterEditingMode() {
        this.mNeedShowStatusbar = false;
        this.mIsEditingMode = true;
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            changeDrawable(null, true);
        } else {
            changeDrawable(DeviceConfig.isScreenOrientationLandscape() ? this.mEditingLandscapeDrawable : this.mEditingDrawable, true);
        }
    }

    public void setExitEditingMode() {
        this.mIsEditingMode = false;
        setNormalMode();
    }

    public void onWallpaperColorChanged() {
        if (this.mNeedShowStatusbar) {
            setNormalMode();
        } else if (this.mIsEditingMode) {
            setEnterEditingMode();
        }
    }

    public void showUninstallBgColor(boolean show) {
        this.mShowUninstallBgColor = show;
        invalidate();
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mShowUninstallBgColor) {
            canvas.drawColor(getResources().getColor(R.color.uninstall_bg_dark));
        } else if (WallpaperUtils.hasAppliedProximateLightWallpaper()) {
            canvas.drawColor(getResources().getColor(R.color.wallpaper_mask_dark));
        }
        super.dispatchDraw(canvas);
        if (this.mNeedShowStatusbar && this.mStatusbarDrawable != null) {
            this.mStatusbarDrawable.draw(canvas);
        }
    }
}
