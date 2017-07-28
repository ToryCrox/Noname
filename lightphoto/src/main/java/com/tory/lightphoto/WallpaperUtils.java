/*Transsion Top Secret*/
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tory.lightphoto;

import android.annotation.TargetApi;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.Pair;
import android.view.WindowManager;

/**
 * Utility methods for wallpaper management.
 */
public final class WallpaperUtils {

    public static final String WALLPAPER_WIDTH_KEY = "wallpaper.width";
    public static final String WALLPAPER_HEIGHT_KEY = "wallpaper.height";
    public static final float WALLPAPER_SCREENS_SPAN = 2f;

    private static Point sDefaultWallpaperSize;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Point getDefaultWallpaperSize(Resources res, WindowManager windowManager) {
        if (sDefaultWallpaperSize == null) {
            Point minDims = new Point();
            Point maxDims = new Point();
            windowManager.getDefaultDisplay().getCurrentSizeRange(minDims, maxDims);

            int maxDim = Math.max(maxDims.x, maxDims.y);
            int minDim = Math.max(minDims.x, minDims.y);

            Point realSize = new Point();
            windowManager.getDefaultDisplay().getRealSize(realSize);
            maxDim = Math.max(realSize.x, realSize.y);
            minDim = Math.min(realSize.x, realSize.y);

            // We need to ensure that there is enough extra space in the wallpaper
            // for the intended parallax effects
            final int defaultWidth, defaultHeight;
            defaultWidth = minDim;
            defaultHeight = maxDim;
            sDefaultWallpaperSize = new Point(defaultWidth, defaultHeight);
        }
        return sDefaultWallpaperSize;
    }

    public static Drawable getLowResWallpaper(Context context) {
        Context appContext = context.getApplicationContext();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
        Point wallpaperSize = getDefaultWallpaperSize(context.getResources(), (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE));
        int with = wallpaperSize.x / 8;
        int height = wallpaperSize.y / 8;

        Drawable currentDrawable = null;
        try {
            WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();
            if (wallpaperInfo != null) {
                currentDrawable = wallpaperInfo.loadThumbnail(appContext.getPackageManager());
            }
            if (currentDrawable == null) {
                currentDrawable = wallpaperManager.getDrawable();
            }
        } catch (Exception e) {
        }
        wallpaperManager.forgetLoadedWallpaper();
        return currentDrawable;
    }
}
