package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import miui.content.res.IconCustomizer;

public class IconLoader {
    private final LauncherApplication mContext;
    private Drawable mDefaultIcon = makeDefaultIcon();
    private int mIconMaskCenterPixel;
    private int mIconMaskEdgePixel;
    private int mIconMaskHeight;
    private int mIconMaskWidth;
    private int[] mMaskIconPixels;
    private final PackageManager mPackageManager;

    public IconLoader(LauncherApplication context) {
        this.mContext = context;
        this.mPackageManager = context.getPackageManager();
        Bitmap iconMask = IconCustomizer.getRawIcon("icon_mask.png");
        if (iconMask != null) {
            this.mIconMaskWidth = iconMask.getWidth();
            this.mIconMaskHeight = iconMask.getHeight();
            this.mMaskIconPixels = new int[(this.mIconMaskWidth * this.mIconMaskHeight)];
            iconMask.getPixels(this.mMaskIconPixels, 0, this.mIconMaskWidth, 0, 0, this.mIconMaskWidth, this.mIconMaskHeight);
            this.mIconMaskCenterPixel = this.mMaskIconPixels[((this.mIconMaskWidth * this.mIconMaskHeight) / 2) + (this.mIconMaskWidth / 2)];
            this.mIconMaskEdgePixel = this.mMaskIconPixels[0];
        }
    }

    private Drawable makeDefaultIcon() {
        return this.mPackageManager.getDefaultActivityIcon();
    }

    public void updateDefaultIcon() {
        this.mDefaultIcon = makeDefaultIcon();
    }

    public Drawable getDefaultIcon() {
        return this.mDefaultIcon;
    }

    public Drawable getIcon(Intent intent, int itemType) {
        boolean z = true;
        ResolveInfo resolveInfo = this.mPackageManager.resolveActivity(intent, 0);
        ComponentName component = intent.getComponent();
        if (resolveInfo == null || component == null) {
            return this.mDefaultIcon;
        }
        if (itemType != 1) {
            z = false;
        }
        return getIcon(component, resolveInfo, z);
    }

    public Drawable getIcon(ComponentName component, ResolveInfo resolveInfo) {
        if (resolveInfo == null || component == null) {
            return null;
        }
        return getIcon(component, resolveInfo, false);
    }

    private Drawable getIcon(ComponentName componentName, ResolveInfo info, boolean isShortcut) {
        if (isShortcut) {
            try {
                return IconCustomizer.generateShortcutIconDrawable(this.mContext.getPackageManager().getDrawable(info.activityInfo.packageName, info.activityInfo.icon, this.mPackageManager.getApplicationInfo(info.activityInfo.packageName, 0)));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        Drawable icon = info.activityInfo.loadIcon(this.mPackageManager);
        if (isIconValid(icon)) {
            return icon;
        }
        IconCustomizer.clearCustomizedIcons(componentName.getPackageName());
        return info.activityInfo.loadIcon(this.mPackageManager);
    }

    private boolean isIconValid(Drawable icon) {
        if (!(icon instanceof BitmapDrawable) || !Launcher.isDefaultThemeApplied() || this.mMaskIconPixels == null) {
            return true;
        }
        int width = icon.getIntrinsicWidth();
        int height = icon.getIntrinsicHeight();
        if (width != this.mIconMaskWidth || height != this.mIconMaskHeight) {
            return true;
        }
        Bitmap b = ((BitmapDrawable) icon).getBitmap();
        if (b.getPixel(0, 0) != this.mIconMaskEdgePixel || b.getPixel(width - 1, 0) != this.mIconMaskEdgePixel || b.getPixel(0, height - 1) != this.mIconMaskEdgePixel || b.getPixel(width - 1, height - 1) != this.mIconMaskEdgePixel) {
            return false;
        }
        if (b.getPixel(width / 2, height / 2) != this.mIconMaskCenterPixel) {
            return true;
        }
        int[] pixels = new int[(width * height)];
        b.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixels[(width * j) + i] != this.mMaskIconPixels[(width * j) + i]) {
                    return true;
                }
            }
        }
        return false;
    }
}
