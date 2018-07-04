package com.miui.home.launcher;

public class WallpaperThumbnailInfo extends ItemInfo {
    private String mWallpaperPath;

    public WallpaperThumbnailInfo() {
        this.itemType = 12;
    }

    public WallpaperThumbnailInfo(String wallpaperPath) {
        this.itemType = 12;
        this.mWallpaperPath = wallpaperPath;
    }

    public String getWallpaperPath() {
        return this.mWallpaperPath;
    }
}
