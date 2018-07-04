package com.miui.home.launcher;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.FileUtils;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.MiuiSettings.ForceTouch;
import android.provider.MiuiSettings.SettingsCloudData;
import android.provider.MiuiSettings.System;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.View;
import android.view.WindowManager;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import miui.os.Build;
import miui.theme.ThemeManagerHelper;
import miui.util.FeatureParser;
import miui.util.HapticFeedbackUtil;
import org.xmlpull.v1.XmlPullParser;

public class DeviceConfig {
    private static final String DEFAULT_DATABASE_SUFFIX = (Build.IS_TABLET ? "4x6" : "4x5");
    public static final float FORCE_TOUCH_MENU_PRESSURE = FeatureParser.getFloat("force_touch_deep", 0.8f).floatValue();
    public static final float FORCE_TOUCH_TRIGGER_PRESSURE = FeatureParser.getFloat("force_touch_light", 0.4f).floatValue();
    public static final Uri GLOBAL_SEARCH_SWITCH_URI = Uri.parse("content://com.android.quicksearchbox.xiaomi/search_swipe_switch");
    public static long INVALIDATE_DEFAULT_SCREEN_ID = -1;
    private static final HashSet<String> ISLAM_REGION = new HashSet<String>() {
        {
            add("PK");
            add("SA");
            add("IQ");
            add("OM");
            add("IR");
        }
    };
    public static final boolean IS_SURRPORT_FORCE_TOUCH = FeatureParser.getBoolean("support_force_touch", false);
    public static final boolean NEED_BOOST_GPU = FeatureParser.getBoolean("boost_gpu", false);
    public static int TEMP_SHARE_MODE_FOR_WORLD_READABLE = (VERSION.SDK_INT >= 24 ? 0 : 1);
    public static int TEMP_SHARE_MODE_FOR_WORLD_WRITEABLE;
    private static boolean mAutoCellSize = false;
    private static int mCellCountX = -1;
    private static int mCellCountXR = -1;
    private static int mCellCountY = -1;
    private static int mCellCountYR = -1;
    private static int mCellHeight = -1;
    private static int mCellWidth = -1;
    private static int mCellWorkingHeight = -1;
    private static int mCellWorkingWidth = -1;
    private static int mDefaultWorkspaceId;
    private static String mDefaultWorkspaceName;
    private static int mFolderRecommendGridPaddingBottom = -1;
    private static int mHotseatCount = -1;
    private static int mIconHeight = -1;
    private static int mIconWidth = -1;
    private static boolean mIsNote720pMode = false;
    private static String mLauncherDatabaseName;
    private static int mNavigationBarHeight = -1;
    private static int mNavigationBarHeightLand = -1;
    private static boolean mRotatable = false;
    private static float mScreenDensity = -1.0f;
    private static int mScreenHeight = -1;
    private static boolean mScreenOrientationChanged = false;
    private static int mScreenWidth = -1;
    private static int mStatusBarHeight = -1;
    private static int mWidgetCellCountX;
    private static int mWidgetCellCountY;
    private static int mWidgetCellHeight;
    private static int mWidgetCellMeasureHeight;
    private static int mWidgetCellMeasureWidth;
    private static int mWidgetCellMinHeight;
    private static int mWidgetCellMinWidth;
    private static int mWidgetCellPaddingBottom;
    private static int mWidgetCellPaddingTop;
    private static int mWidgetCellWidth;
    private static int mWidgetWorkingHeight;
    private static int mWidgetWorkingWidth;
    private static int mWorkspaceBackgroundMarginBottom = -1;
    private static int mWorkspaceBackgroundMarginBottomLand = -1;
    private static int mWorkspaceCellPaddingBottom = -1;
    private static int mWorkspaceCellPaddingBottomLand = -1;
    private static int mWorkspaceCellPaddingBottomNoteLarge = -1;
    private static int mWorkspaceCellPaddingSide = -1;
    private static int mWorkspaceCellPaddingTop = -1;
    private static int mWorkspaceIndicatorMarginBottom = -1;
    private static int mWorkspaceIndicatorMarginBottomLand = -1;
    private static int mWorkspaceIndicatorMarginBottomNoteLarge = -1;
    private static boolean sAllowedSlidingUpToStartGolbalSearch = true;
    private static String sCurrentScreenCells;
    private static long sDefaultScreenId = INVALIDATE_DEFAULT_SCREEN_ID;
    private static HapticFeedbackUtil sHapticFeedbackUtil;
    private static boolean sIsLayoutRtl = false;
    private static boolean sIsShowNotification = true;
    private static Configuration sLastConfiguration = null;
    private static boolean sNeedShowMisplacedTips = false;
    public static int sRecommendBannerHeight;
    public static int sRecommendBannerWidth;
    public static String sRecommendDefaultTitle;
    private static boolean sRecommendEnableDefault = false;
    private static boolean sRecommendServerEnable = true;
    private static long sRecommendViewInterval = 0;
    private static boolean sScreenCellsChanged = false;
    private static boolean sScreenSizeChanged = false;

    static {
        int i = 0;
        if (VERSION.SDK_INT < 24) {
            i = 2;
        }
        TEMP_SHARE_MODE_FOR_WORLD_WRITEABLE = i;
    }

    public static void loadRecommendData(Context context) {
        if (Build.IS_STABLE_VERSION) {
            sRecommendEnableDefault = SettingsCloudData.getCloudDataBoolean(context.getContentResolver(), "home_recommendation", "recommendationEnableDefault_stable", false);
            sRecommendViewInterval = SettingsCloudData.getCloudDataLong(context.getContentResolver(), "home_recommendation", "recommendationViewInterval_stable", 0);
        } else {
            sRecommendEnableDefault = SettingsCloudData.getCloudDataBoolean(context.getContentResolver(), "home_recommendation", "recommendationEnableDefault_dev", false);
            sRecommendViewInterval = SettingsCloudData.getCloudDataLong(context.getContentResolver(), "home_recommendation", "recommendationViewInterval_dev", 0);
        }
        if (Build.IS_INTERNATIONAL_BUILD) {
            sRecommendServerEnable = SettingsCloudData.getCloudDataBoolean(context.getContentResolver(), "home_recommendation", "recommendationServerEnable_miui365316_i18n", false);
        } else {
            sRecommendServerEnable = SettingsCloudData.getCloudDataBoolean(context.getContentResolver(), "home_recommendation", "recommendationServerEnable_cn", true);
        }
    }

    public static boolean Init(Context context, boolean screenCellsChanged) {
        boolean z;
        Configuration currentConfig = context.getResources().getConfiguration();
        if (currentConfig.getLayoutDirection() == 1) {
            sIsLayoutRtl = true;
        } else {
            sIsLayoutRtl = false;
        }
        if (sLastConfiguration == null) {
            sLastConfiguration = new Configuration(currentConfig);
        } else {
            int changed = sLastConfiguration.diff(currentConfig);
            if ((Integer.MIN_VALUE & changed) != 0) {
                WallpaperUtils.clearWallpaperSrc();
            }
            if ((-1073741180 & changed) == 0 && !screenCellsChanged) {
                return false;
            }
            sLastConfiguration.setTo(currentConfig);
        }
        boolean cellsSizeChangedByTheme = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (getCellCountXDef(context) > getCellCountXMin(context) || getCellCountYDef(context) > getCellCountYMin(context)) {
            cellsSizeChangedByTheme = true;
            sp.edit().putBoolean("pref_key_use_theme_cells_size", true).commit();
            sp.edit().remove("pref_key_theme_cells_x").remove("pref_key_theme_cells_Y").commit();
        } else {
            sp.edit().putBoolean("pref_key_use_theme_cells_size", false).commit();
        }
        Resources res = context.getResources();
        if (Build.IS_TABLET && res.getBoolean(R.bool.config_enable_rotatable_model)) {
            z = true;
        } else {
            z = false;
        }
        mRotatable = z;
        mAutoCellSize = res.getBoolean(R.bool.config_enable_auto_cellsize);
        mIsNote720pMode = isNote720pMode(context);
        if (mIsNote720pMode) {
            mIconWidth = res.getDimensionPixelSize(R.dimen.config_note_720p_icon_width);
            mIconHeight = res.getDimensionPixelSize(R.dimen.config_note_720p_icon_height);
            mCellWidth = res.getDimensionPixelSize(R.dimen.note_720p_workspace_cell_width);
            mCellHeight = res.getDimensionPixelSize(R.dimen.note_720p_workspace_cell_height);
        } else {
            mIconWidth = res.getDimensionPixelSize(R.dimen.config_icon_width);
            mIconHeight = res.getDimensionPixelSize(R.dimen.config_icon_height);
            mCellWidth = res.getDimensionPixelSize(R.dimen.workspace_cell_width);
            mCellHeight = res.getDimensionPixelSize(R.dimen.workspace_cell_height);
        }
        mStatusBarHeight = res.getDimensionPixelSize(R.dimen.status_bar_height);
        mNavigationBarHeight = res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
        mNavigationBarHeightLand = res.getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_width);
        mWorkspaceCellPaddingSide = res.getDimensionPixelSize(isRotatable() ? R.dimen.workspace_cell_padding_side_rotatable : R.dimen.workspace_cell_padding_side);
        mWorkspaceCellPaddingTop = res.getDimensionPixelSize(R.dimen.workspace_cell_padding_top);
        mWorkspaceCellPaddingBottom = res.getDimensionPixelSize(R.dimen.workspace_cell_padding_bottom);
        mWorkspaceCellPaddingBottomLand = res.getDimensionPixelSize(R.dimen.workspace_cell_padding_bottom_land);
        mWorkspaceCellPaddingBottomNoteLarge = res.getDimensionPixelSize(R.dimen.note_720p_workspace_cell_padding_bottom);
        mWorkspaceBackgroundMarginBottom = res.getDimensionPixelSize(R.dimen.workspace_background_margin_bottom);
        mWorkspaceBackgroundMarginBottomLand = res.getDimensionPixelSize(R.dimen.workspace_background_margin_bottom_land);
        mWorkspaceIndicatorMarginBottom = res.getDimensionPixelSize(R.dimen.workspace_indicator_margin_bottom);
        mWorkspaceIndicatorMarginBottomLand = res.getDimensionPixelSize(R.dimen.workspace_indicator_margin_bottom_land);
        mWorkspaceIndicatorMarginBottomNoteLarge = res.getDimensionPixelSize(R.dimen.note_720p_workspace_indicator_margin_bottom);
        mFolderRecommendGridPaddingBottom = res.getDimensionPixelSize(R.dimen.recommend_grid_padding_bottom);
        mWidgetCellMeasureWidth = res.getDimensionPixelSize(R.dimen.workspace_widget_cell_measure_width);
        mWidgetCellMeasureHeight = res.getDimensionPixelSize(R.dimen.workspace_widget_cell_measure_height);
        mWidgetCellMinWidth = res.getDimensionPixelSize(R.dimen.workspace_widget_cell_min_width);
        mWidgetCellMinHeight = res.getDimensionPixelSize(R.dimen.workspace_widget_cell_min_height);
        mWidgetCellPaddingTop = res.getDimensionPixelSize(R.dimen.workspace_widget_cell_padding_top);
        mWidgetCellPaddingBottom = res.getDimensionPixelSize(isRotatable() ? R.dimen.workspace_widget_cell_padding_bottom_rotatable : R.dimen.workspace_widget_cell_padding_bottom);
        sNeedShowMisplacedTips = false;
        loadScreenSize(context, res);
        calcGridSize(context, res);
        if (sHapticFeedbackUtil == null) {
            sHapticFeedbackUtil = new HapticFeedbackUtil(context, false);
        }
        sRecommendDefaultTitle = context.getResources().getResourceName(R.string.recommend_apps_default_title);
        sRecommendBannerWidth = (int) (((float) context.getResources().getDimensionPixelSize(R.dimen.folder_content_width)) * 0.8f);
        sRecommendBannerHeight = (int) (((float) mCellHeight) * 0.8f);
        sScreenCellsChanged = false;
        if (cellsSizeChangedByTheme) {
            confirmCellsCount(context);
        }
        return true;
    }

    public static boolean isRotatable() {
        return mRotatable;
    }

    public static boolean isLayoutRtl() {
        return sIsLayoutRtl;
    }

    public static int getIterateDirection(boolean oppositeToLayoutDirection) {
        int i = -1;
        if (!oppositeToLayoutDirection) {
            if (!sIsLayoutRtl) {
                i = 1;
            }
            return i;
        } else if (sIsLayoutRtl) {
            return 1;
        } else {
            return -1;
        }
    }

    public static boolean isAutoCellSize() {
        return mAutoCellSize;
    }

    public static boolean checkIfIsOrientationChanged(Context context) {
        int ow = mScreenWidth;
        int oh = mScreenHeight;
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int screenWidth = !isRotatable() ? Math.min(dm.widthPixels, dm.heightPixels) : dm.widthPixels;
        int screenHeight = !isRotatable() ? Math.max(dm.widthPixels, dm.heightPixels) : dm.heightPixels;
        return (ow > oh && screenWidth < screenHeight) || (ow < oh && screenWidth > screenHeight);
    }

    public static void loadScreenSize(Context context, Resources res) {
        boolean z;
        boolean z2 = true;
        int ow = mScreenWidth;
        int oh = mScreenHeight;
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        mScreenWidth = !isRotatable() ? Math.min(dm.widthPixels, dm.heightPixels) : dm.widthPixels;
        mScreenHeight = !isRotatable() ? Math.max(dm.widthPixels, dm.heightPixels) : dm.heightPixels;
        mScreenDensity = dm.density;
        if ((ow <= oh || mScreenWidth >= mScreenHeight) && (ow >= oh || mScreenWidth <= mScreenHeight)) {
            z = false;
        } else {
            z = true;
        }
        mScreenOrientationChanged = z;
        if ((ow == mScreenWidth && oh == mScreenHeight) || mScreenOrientationChanged) {
            z2 = false;
        }
        sScreenSizeChanged = z2;
        if (isScreenOrientationChanged() || isScreenSizeChanged()) {
            calcGridSize(context, res);
        }
    }

    public static final boolean getRecommendEnableDefault() {
        return sRecommendEnableDefault;
    }

    public static final boolean isRecommendServerEnable() {
        return sRecommendServerEnable;
    }

    public static final long getRecommendViewInterval() {
        return sRecommendViewInterval;
    }

    public static final int getDeviceWidth() {
        return Math.min(mScreenWidth, mScreenHeight);
    }

    public static final int getDeviceHeight() {
        return Math.max(mScreenWidth, mScreenHeight);
    }

    public static final int getScreenWidth() {
        return mScreenWidth;
    }

    public static final int getScreenHeight() {
        return mScreenHeight;
    }

    public static final float getScreenDensity() {
        return mScreenDensity;
    }

    public static final boolean isScreenOrientationChanged() {
        return mScreenOrientationChanged;
    }

    public static boolean isScreenSizeChanged() {
        return sScreenSizeChanged;
    }

    public static final boolean isScreenOrientationLandscape() {
        return mScreenWidth > mScreenHeight;
    }

    public static final int getWorkspaceCellPaddingSide() {
        return mWorkspaceCellPaddingSide;
    }

    public static final int getWorkspaceCellPaddingTop() {
        return mWorkspaceCellPaddingTop;
    }

    public static final int getWorkspaceCellPaddingBottom() {
        if (isScreenOrientationLandscape() && isRotatable()) {
            return mWorkspaceCellPaddingBottomLand;
        }
        return isNote720pMode() ? mWorkspaceCellPaddingBottomNoteLarge : mWorkspaceCellPaddingBottom;
    }

    public static final int getWidgetCellPaddingTop() {
        return mWidgetCellPaddingTop;
    }

    public static final int getWidgetCellPaddingBottom() {
        return mWidgetCellPaddingBottom;
    }

    public static final int getIconWidth() {
        return mIconWidth;
    }

    public static final int getIconHeight() {
        return mIconHeight;
    }

    public static final int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public static final int getRecommendGridPaddingBottom() {
        return isScreenOrientationLandscape() ? 0 : mFolderRecommendGridPaddingBottom;
    }

    private static final void calcGridSize(Context context, Resources res) {
        int hCellPadding = mWorkspaceCellPaddingSide * 2;
        int vCellPadding = (mStatusBarHeight + getWorkspaceCellPaddingTop()) + getWorkspaceCellPaddingBottom();
        mCellWorkingWidth = mScreenWidth - hCellPadding;
        mCellWorkingHeight = mScreenHeight - vCellPadding;
        int vWidgetPadding = (mStatusBarHeight + mWidgetCellPaddingTop) + mWidgetCellPaddingBottom;
        mWidgetWorkingWidth = mScreenWidth - 0;
        mWidgetWorkingHeight = mScreenHeight - vWidgetPadding;
        if (mRotatable) {
            int naviBarHeight = isScreenOrientationLandscape() ? mNavigationBarHeightLand : mNavigationBarHeight;
            int min;
            if (mAutoCellSize) {
                int hCnt = mCellWorkingWidth / mCellWidth;
                int vCnt = mCellWorkingHeight / mCellHeight;
                int hCntR = ((mScreenHeight - hCellPadding) + naviBarHeight) / mCellWidth;
                int vCntR = ((mScreenWidth - vCellPadding) - naviBarHeight) / mCellHeight;
                while (hCnt * vCnt != hCntR * vCntR) {
                    if (hCnt * vCnt > hCntR * vCntR) {
                        if (hCnt > vCnt) {
                            hCnt--;
                        } else {
                            vCnt--;
                        }
                    } else if (hCntR > vCntR) {
                        hCntR--;
                    } else {
                        vCntR--;
                    }
                }
                mCellCountX = hCnt;
                mCellCountY = vCnt;
                mCellCountXR = hCntR;
                mCellCountYR = vCntR;
                min = Math.min(mWidgetWorkingWidth / mWidgetCellMinWidth, Math.min(mWidgetWorkingHeight / mWidgetCellMinHeight, Math.min(((mScreenHeight - 0) + naviBarHeight) / mWidgetCellMinWidth, ((mScreenWidth - vWidgetPadding) - naviBarHeight) / mWidgetCellMinHeight)));
                mWidgetCellCountY = min;
                mWidgetCellCountX = min;
                mWidgetCellWidth = mWidgetCellMinWidth;
                mWidgetCellHeight = mWidgetCellMinHeight;
                mLauncherDatabaseName = getDatabaseNameBySuffix("");
            } else {
                loadCellsCountConfig(context);
                mLauncherDatabaseName = getDatabaseNameBySuffix(getCellSizeVal(mCellCountX, mCellCountY));
                min = res.getInteger(R.integer.config_widget_cell_count);
                mWidgetCellCountY = min;
                mWidgetCellCountX = min;
                mWidgetCellWidth = mWidgetCellMinWidth;
                mWidgetCellHeight = mWidgetCellMinHeight;
                if (isScreenOrientationLandscape()) {
                    mCellCountX += mCellCountY;
                    mCellCountY = mCellCountX - mCellCountY;
                    mCellCountX -= mCellCountY;
                    mCellCountXR = mCellCountY;
                    mCellCountYR = mCellCountX;
                }
                tryToMigrateDefaultDatabase(context);
            }
        } else {
            loadCellsCountConfig(context);
            mWidgetCellCountX = mCellCountX;
            mWidgetCellCountY = mCellCountY;
            mWidgetCellWidth = Math.max(mScreenWidth / mCellCountX, mWidgetCellMinWidth);
            mWidgetCellHeight = Math.max((((mScreenHeight - mStatusBarHeight) - mWidgetCellPaddingTop) - mWidgetCellPaddingBottom) / mCellCountY, mWidgetCellMinHeight);
            mLauncherDatabaseName = getDatabaseNameBySuffix(getCellSizeVal(mCellCountX, mCellCountY));
            tryToMigrateDefaultDatabase(context);
        }
        mHotseatCount = mCellCountX + (Math.min(mScreenHeight, mScreenWidth) / (mIconWidth * mCellCountX));
    }

    private static final void loadCellsCountConfig(Context context) {
        Resources res = context.getResources();
        int cellCountXMin = Math.max(2, getCellCountXMin(context));
        int cellCountYMin = Math.max(2, getCellCountYMin(context));
        int cellCountX = getCellCountXDef(context);
        int cellCountY = getCellCountYDef(context);
        int calibratedCountXMax = Math.max(cellCountX, getCellCountXMax(context));
        int calibratedCountYMax = Math.max(cellCountY, getCellCountYMax(context));
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useThemeCellsSize = sp.getBoolean("pref_key_use_theme_cells_size", false);
        cellCountX = sp.getInt(useThemeCellsSize ? "pref_key_theme_cells_x" : "pref_key_cell_x", cellCountX);
        cellCountY = sp.getInt(useThemeCellsSize ? "pref_key_theme_cells_Y" : "pref_key_cell_y", cellCountY);
        int min = Math.min(calibratedCountXMax, Math.max(cellCountXMin, cellCountX));
        mCellCountYR = min;
        mCellCountX = min;
        min = Math.min(calibratedCountYMax, Math.max(cellCountYMin, cellCountY));
        mCellCountXR = min;
        mCellCountY = min;
        if (useThemeCellsSize) {
            sCurrentScreenCells = "by_theme";
        } else {
            sCurrentScreenCells = mCellCountX + "x" + mCellCountY;
        }
    }

    public static boolean setScreenCells(Context context, int cellCountX, int cellCountY) {
        boolean invalidateSize;
        if (cellCountX < getCellCountXMin(context) || cellCountX > getCellCountXMax(context) || cellCountY < getCellCountYMin(context) || cellCountY > getCellCountYMax(context)) {
            invalidateSize = true;
        } else {
            invalidateSize = false;
        }
        if (invalidateSize && (cellCountX != getCellCountXDef(context) || cellCountY != getCellCountYDef(context))) {
            return false;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useThemeCellsSize = sp.getBoolean("pref_key_use_theme_cells_size", false);
        sp.edit().putInt(useThemeCellsSize ? "pref_key_theme_cells_x" : "pref_key_cell_x", cellCountX).commit();
        sp.edit().putInt(useThemeCellsSize ? "pref_key_theme_cells_Y" : "pref_key_cell_y", cellCountY).commit();
        mCellCountX = cellCountX;
        mCellCountY = cellCountY;
        mLauncherDatabaseName = getDatabaseNameBySuffix(getCellSizeVal(mCellCountX, mCellCountY));
        sScreenCellsChanged = true;
        tryToMigrateDefaultDatabase(context);
        return true;
    }

    private static boolean isNote720pMode(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService("window");
        DisplayInfo di = new DisplayInfo();
        wm.getDefaultDisplay().getDisplayInfo(di);
        return "leo".equals(Build.DEVICE) && 720 == di.getNaturalWidth();
    }

    public static boolean isNote720pMode() {
        return mIsNote720pMode;
    }

    private static void calcDefaultWorkspaceName(Context context) {
        String cellCountSuffix = getCellSizeVal(mCellCountX, mCellCountY);
        mDefaultWorkspaceName = "";
        String currentRegion = Build.getRegion();
        if (Build.IS_INTERNATIONAL_BUILD && ISLAM_REGION.contains(currentRegion)) {
            mDefaultWorkspaceName = "islam_";
        }
        if (Build.IS_INTERNATIONAL_BUILD && TextUtils.equals(Build.getRegion(), "IN")) {
            mDefaultWorkspaceName = "in_";
        }
        if (Build.IS_INTERNATIONAL_BUILD && TextUtils.equals(Build.getRegion(), "TW")) {
            mDefaultWorkspaceName = "tw_";
        }
        if (mRotatable) {
            if (Build.IS_INTERNATIONAL_BUILD) {
                mDefaultWorkspaceName += "i18n_";
            }
            mDefaultWorkspaceName += "default_workspace_rotatable";
            if (!mAutoCellSize) {
                mDefaultWorkspaceName += cellCountSuffix;
            }
        } else {
            if (Build.IS_CM_CUSTOMIZATION) {
                mDefaultWorkspaceName += "cm_";
            } else if (Build.IS_INTERNATIONAL_BUILD) {
                mDefaultWorkspaceName += "i18n_";
            }
            mDefaultWorkspaceName += "default_workspace" + cellCountSuffix;
            if (UserHandle.myUserId() != 0) {
                mDefaultWorkspaceName += "_private_model";
            }
        }
        mDefaultWorkspaceName = context.getPackageName() + ":xml/" + mDefaultWorkspaceName;
    }

    public static final void portraitCellPosition(ContentValues values) {
        if (isScreenOrientationLandscape()) {
            Integer cx = values.getAsInteger("cellX");
            Integer cy = values.getAsInteger("cellY");
            if (cx != null && cy != null) {
                int index = (cy.intValue() * mCellCountX) + cx.intValue();
                values.put("cellX", Integer.valueOf(index % mCellCountXR));
                values.put("cellY", Integer.valueOf(index / mCellCountXR));
            }
        }
    }

    public static final void correntCellPositionRuntime(ItemInfo info, boolean ignoreScreenOrientation) {
        if (!isRotatable()) {
            return;
        }
        if ((ignoreScreenOrientation || info.isLandscapePos != isScreenOrientationLandscape()) && info.spanX == 1 && info.spanY == 1 && info != null && info.container == -100) {
            int index = (info.cellY * mCellCountXR) + info.cellX;
            info.cellX = index % mCellCountX;
            info.cellY = index / mCellCountX;
            info.isLandscapePos = isScreenOrientationLandscape();
        }
    }

    public static final int getPortraitCellCountX() {
        return mRotatable ? Math.min(mCellCountX, mCellCountXR) : mCellCountX;
    }

    public static final int getPortraitCellCountY() {
        return mRotatable ? Math.max(mCellCountY, mCellCountYR) : mCellCountY;
    }

    public static void confirmCellsCount(Context context) {
        removeInvalidateDatabase(context, true);
        System.putString(context.getContentResolver(), "miui_home_screen_cells_size", getCellCountX() + "x" + getCellCountY());
    }

    public static final int getCellCountX() {
        return mCellCountX;
    }

    public static final int getCellCountY() {
        return mCellCountY;
    }

    public static final int getCellCountXMin(Context context) {
        int resCxMin = R.integer.config_cell_count_x_min;
        if (mIsNote720pMode) {
            resCxMin = R.integer.config_note_720p_cell_count_x_min;
        }
        return context.getResources().getInteger(resCxMin);
    }

    public static final int getCellCountYMin(Context context) {
        int resCyMin = R.integer.config_cell_count_y_min;
        if (mIsNote720pMode) {
            resCyMin = R.integer.config_note_720p_cell_count_y_min;
        }
        return context.getResources().getInteger(resCyMin);
    }

    public static final int getCellCountXMax(Context context) {
        int resCxMax = R.integer.config_cell_count_x_max;
        if (mIsNote720pMode) {
            resCxMax = R.integer.config_note_720p_cell_count_x_max;
        }
        return context.getResources().getInteger(resCxMax);
    }

    public static final int getCellCountYMax(Context context) {
        int resCyMax = R.integer.config_cell_count_y_max;
        if (mIsNote720pMode) {
            resCyMax = R.integer.config_note_720p_cell_count_y_max;
        }
        return context.getResources().getInteger(resCyMax);
    }

    public static final int getCellCountXDef(Context context) {
        int resCx = R.integer.config_cell_count_x;
        if (mIsNote720pMode) {
            resCx = R.integer.config_note_720p_cell_count_x;
        }
        return context.getResources().getInteger(resCx);
    }

    public static boolean needShowCellsEntry(Context context) {
        return getCellCountXMin(context) < getCellCountXMax(context) || getCellCountYMin(context) < getCellCountYMax(context);
    }

    public static final int getCellCountYDef(Context context) {
        int resCy = R.integer.config_cell_count_y;
        if (mIsNote720pMode) {
            resCy = R.integer.config_note_720p_cell_count_y;
        }
        return context.getResources().getInteger(resCy);
    }

    public static final void recordCurrentScreenCells() {
        AnalyticalDataCollector.trackScreenCellsSize(Build.DEVICE, sCurrentScreenCells);
    }

    public static final int getCellWidth() {
        return mCellWidth;
    }

    public static final int getCellHeight() {
        return mCellHeight;
    }

    public static final int getCellWorkingWidth() {
        return mCellWorkingWidth;
    }

    public static final int getCellWorkingHeight() {
        return mCellWorkingHeight;
    }

    public static final String getCellSizeVal(int cx, int cy) {
        return cx + "x" + cy;
    }

    public static final String getDatabaseNameBySuffix(String suffix) {
        if (isRotatable() && isAutoCellSize()) {
            suffix = "";
        }
        return getDatabaseNamePrefix() + suffix + ".db";
    }

    private static final String getDatabaseNamePrefix() {
        String mid = "";
        if (isRotatable() && !isAutoCellSize()) {
            mid = "_rotatable";
        }
        return "launcher" + mid;
    }

    public static final int getHotseatCount() {
        return mHotseatCount;
    }

    public static final String getDatabaseName() {
        return mLauncherDatabaseName;
    }

    public static final int getDefaultWorkspaceXmlId(Context context) {
        calcDefaultWorkspaceName(context);
        mDefaultWorkspaceId = context.getResources().getIdentifier(mDefaultWorkspaceName, null, null);
        if (mDefaultWorkspaceId == 0) {
            mDefaultWorkspaceId = R.xml.default_workspace_none;
        }
        return mDefaultWorkspaceId;
    }

    public static final int getWidgetSpanX(int minWidth) {
        return Math.min(((Utilities.getDipPixelSize(1) + minWidth) / mWidgetCellMeasureWidth) + 1, mWidgetCellCountX);
    }

    public static final int getWidgetSpanY(int minHeight) {
        return Math.min(((Utilities.getDipPixelSize(1) + minHeight) / mWidgetCellMeasureHeight) + 1, mWidgetCellCountY);
    }

    public static final void calcWidgetSpans(LauncherAppWidgetProviderInfo info) {
        info.spanX = getWidgetSpanX(info.providerInfo.minWidth);
        info.spanY = getWidgetSpanY(info.providerInfo.minHeight);
    }

    public static final int getWidgetCellWidth() {
        return mWidgetCellWidth;
    }

    public static final int getWidgetCellHeight() {
        return mWidgetCellHeight;
    }

    public static final int getWidgetCellCountX() {
        return mWidgetCellCountX;
    }

    public static final int getWidgetCellCountY() {
        return mWidgetCellCountY;
    }

    public static final int getWidgetWorkingWidth() {
        return mWidgetWorkingWidth;
    }

    public static final int getWidgetWorkingHeight() {
        return mWidgetWorkingHeight;
    }

    public static final boolean isXLargeMode() {
        return mCellCountX == 3 && mCellCountY == 4;
    }

    public static final boolean isInvalidateCellPosition(boolean isWidget, int cellX, int cellY, int spanX, int spanY) {
        if (isRotatable() && isWidget) {
            if (cellX + spanX > getWidgetCellCountX() || cellY + spanY > getWidgetCellCountY()) {
                return true;
            }
            return false;
        } else if (cellX + spanX > getPortraitCellCountX() || cellY + spanY > getPortraitCellCountY()) {
            return true;
        } else {
            return false;
        }
    }

    public static final boolean needHideThemeManager(Context context) {
        return ThemeManagerHelper.needDisableTheme(context);
    }

    private static final void tryToMigrateDefaultDatabase(Context context) {
        if (!isXLargeMode()) {
            File currentDb = context.getDatabasePath(mLauncherDatabaseName);
            if (!currentDb.exists()) {
                File lastModifyDB = getOptimalDB(context);
                if (lastModifyDB != null) {
                    FileUtils.copyFile(lastModifyDB.getAbsoluteFile(), currentDb.getAbsoluteFile());
                    if (!sScreenCellsChanged) {
                        sNeedShowMisplacedTips = true;
                    }
                }
            }
            if (!sScreenCellsChanged) {
                removeInvalidateDatabase(context, true);
            }
        }
    }

    public static final boolean needShowMisplacedTips() {
        return sNeedShowMisplacedTips;
    }

    public static final boolean needHideLockProvider(Context context) {
        if (Build.IS_INTERNATIONAL_BUILD) {
            return needHideThemeManager(context);
        }
        return false;
    }

    public static final void removeInvalidateDatabase(Context context, boolean retainCurrentDB) {
        String currentDatabaseName = getDatabaseName();
        try {
            if (context.getDatabasePath(mLauncherDatabaseName) == null) {
                return;
            }
            if (!retainCurrentDB || context.getDatabasePath(mLauncherDatabaseName).exists()) {
                File databaseFolder = new File(context.getDatabasePath("foo").getParentFile().getCanonicalPath());
                for (String fileName : databaseFolder.list()) {
                    if (!(retainCurrentDB && !TextUtils.isEmpty(fileName) && (!fileName.endsWith(".db") || fileName.startsWith(currentDatabaseName) || fileName.startsWith("assistant")))) {
                        new File(databaseFolder, fileName).delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeDownloadInstallInfo(Context context) {
        File infoFile = new File(LauncherSettings.getDownloadInstallInfoPath(context));
        if (infoFile.exists()) {
            infoFile.delete();
        }
    }

    private static final File getOptimalDB(Context context) {
        File file = null;
        try {
            String[] databases = new File(context.getDatabasePath("foo").getParentFile().getCanonicalPath()).list(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".db");
                }
            });
            if (!(databases == null || databases.length == 0)) {
                String optimalDBName = null;
                for (String betterDBForMigrate : databases) {
                    optimalDBName = getBetterDBForMigrate(optimalDBName, betterDBForMigrate);
                }
                Log.i("Launcher.DeviceConfig", "find optimal db " + optimalDBName);
                if (!TextUtils.isEmpty(optimalDBName)) {
                    file = context.getDatabasePath(optimalDBName);
                }
            }
        } catch (Exception e) {
            Log.d("Launcher.DeviceConfig", "find db fail", e);
        }
        return file;
    }

    private static final String getBetterDBForMigrate(String dbAlpha, String dbBeta) {
        String cellCountSuffixAlpha = getCellCountSuffix(dbAlpha);
        String cellCountSuffixBeta = getCellCountSuffix(dbBeta);
        int[] cellCountAlpha = new int[2];
        int[] cellCountBeta = new int[2];
        boolean isDBAlphaLegal = ScreenUtils.parseCellsSize(cellCountSuffixAlpha, cellCountAlpha);
        boolean isDBBetaLegal = ScreenUtils.parseCellsSize(cellCountSuffixBeta, cellCountBeta);
        if (!isDBAlphaLegal && !isDBBetaLegal) {
            return null;
        }
        if (!isDBAlphaLegal && isDBBetaLegal) {
            return dbBeta;
        }
        if (isDBAlphaLegal && !isDBBetaLegal) {
            return dbAlpha;
        }
        if (cellCountAlpha[0] == cellCountBeta[0]) {
            if (Math.abs(cellCountAlpha[1] - mCellCountY) < Math.abs(cellCountBeta[1] - mCellCountY)) {
                return dbAlpha;
            }
            if (Math.abs(cellCountAlpha[1] - mCellCountY) > Math.abs(cellCountBeta[1] - mCellCountY)) {
                return dbBeta;
            }
            if (cellCountAlpha[1] >= mCellCountY) {
                return dbBeta;
            }
            return dbAlpha;
        } else if (Math.abs(cellCountAlpha[0] - mCellCountX) < Math.abs(cellCountBeta[0] - mCellCountX)) {
            return dbAlpha;
        } else {
            if (Math.abs(cellCountAlpha[0] - mCellCountX) > Math.abs(cellCountBeta[0] - mCellCountX)) {
                return dbBeta;
            }
            return cellCountAlpha[0] >= mCellCountX ? dbBeta : dbAlpha;
        }
    }

    private static final String getCellCountSuffix(String dbName) {
        String str = null;
        if (!TextUtils.isEmpty(dbName)) {
            try {
                String legalDBPrefix = getDatabaseNamePrefix();
                if (dbName.startsWith(legalDBPrefix)) {
                    str = dbName.substring(legalDBPrefix.length(), dbName.length() - ".db".length());
                }
            } catch (Exception e) {
            }
        }
        return str;
    }

    public static int getWorkspaceBackgroundMarginBottom() {
        return isScreenOrientationLandscape() ? mWorkspaceBackgroundMarginBottomLand : mWorkspaceBackgroundMarginBottom;
    }

    public static int getWorkspaceIndicatorMarginBottom() {
        if (isScreenOrientationLandscape()) {
            return mWorkspaceIndicatorMarginBottomLand;
        }
        return isNote720pMode() ? mWorkspaceIndicatorMarginBottomNoteLarge : mWorkspaceIndicatorMarginBottom;
    }

    public static void performPickupStartVibration(View host) {
        sHapticFeedbackUtil.performHapticFeedback("home_pickup_start", false);
    }

    public static void performDropFinishVibration(View host) {
        sHapticFeedbackUtil.performHapticFeedback("home_drop_finish", false);
    }

    public static synchronized long getDesignedDefaultScreenId(Context context) {
        long j;
        synchronized (DeviceConfig.class) {
            if (sDefaultScreenId != INVALIDATE_DEFAULT_SCREEN_ID) {
                j = sDefaultScreenId;
            } else {
                try {
                    XmlPullParser parser = context.getResources().getXml(getDefaultWorkspaceXmlId(context));
                    AttributeSet attrs = Xml.asAttributeSet(parser);
                    int depth = parser.getDepth();
                    while (true) {
                        int type = parser.next();
                        if ((type == 3 && parser.getDepth() <= depth) || type == 1) {
                            break;
                        } else if (type == 2) {
                            if ("default".equals(parser.getName())) {
                                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Favorite);
                                sDefaultScreenId = Long.valueOf(a.getString(2)).longValue();
                                a.recycle();
                            }
                        }
                    }
                    j = sDefaultScreenId;
                } catch (Exception e) {
                    j = INVALIDATE_DEFAULT_SCREEN_ID;
                }
            }
        }
        return j;
    }

    public static boolean isSupportForceTouch(Context context) {
        return IS_SURRPORT_FORCE_TOUCH && ForceTouch.isEnabled(context);
    }

    public static boolean isShowNotification() {
        return sIsShowNotification;
    }

    public static synchronized void setIsShowNotification(boolean isShowNotification) {
        synchronized (DeviceConfig.class) {
            sIsShowNotification = isShowNotification;
        }
    }

    public static boolean needHideMinusScreen() {
        return Build.IS_INTERNATIONAL_BUILD || mRotatable || sIsLayoutRtl;
    }

    public static void setAllowedSlidingUpToStartGolbalSearch(boolean allowed) {
        sAllowedSlidingUpToStartGolbalSearch = allowed;
    }

    public static boolean allowedSlidingUpToStartGolbalSearch() {
        return sAllowedSlidingUpToStartGolbalSearch;
    }
}
