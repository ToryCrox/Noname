package com.miui.home.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.market.sdk.AppstoreAppInfo;
import com.market.sdk.DownloadResponse;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.FolderInfo.RecommendInfo;
import com.miui.home.launcher.common.Utilities;
import com.xiaomi.analytics.Actions;
import com.xiaomi.analytics.AdAction;
import com.xiaomi.analytics.Analytics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import miui.os.Build;

public class RecommendAppsThumbnailView extends ThumbnailView implements OnClickListener {
    private ArrayList<ShortcutInfo> mCurrentDisplayList;
    private Launcher mLauncher;

    public RecommendAppsThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecommendAppsThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCurrentDisplayList = new ArrayList();
        setOnClickListener(this);
        setScrollWholeScreen(true);
        setPushGestureEnabled(true);
        setScreenTransitionType(10);
        setScreenLayoutMode(6);
        setFixedGap((int) Math.floor((double) (((float) (context.getResources().getDimensionPixelSize(R.dimen.folder_content_width) - (DeviceConfig.getCellWidth() * 4))) / 3.0f)));
        setScreenSnapDuration(250);
        this.mChangeAlongWallpaper = false;
    }

    public void setVisibility(int visibility) {
        if (visibility != 0) {
            this.mCurrentDisplayList.clear();
        } else {
            onDisplayViewChanged(this.mCurrentScreen);
        }
        super.setVisibility(visibility);
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    protected void reLoadThumbnails() {
        boolean startFromHead;
        int i = 0;
        int lastCurrentScreen = this.mCurrentScreen;
        if (getScreenCount() == 0) {
            startFromHead = true;
        } else {
            startFromHead = false;
        }
        super.reLoadThumbnails();
        if (this.mAdapter != null) {
            if (!startFromHead) {
                i = Math.min(lastCurrentScreen, getScreenCount() - 1);
            }
            setCurrentScreen(i);
        }
    }

    protected void reorderAndAddAllViews(ArrayList<View> allViews) {
        super.reorderAndAddAllViews(allViews);
    }

    protected void clearThumbnail(View resultView) {
        if (resultView != null && getChildIndex(resultView) >= 4) {
            ((ShortcutInfo) resultView.getTag()).recycleIconRes();
        }
    }

    public void setAdapter(ThumbnailViewAdapter adapter) {
        this.mCurrentDisplayList.clear();
        super.setAdapter(adapter);
    }

    public void scrollToScreen(int index) {
        super.scrollToScreen(index);
        onDisplayViewChanged(index);
    }

    protected int snapToScreen(int whichScreen, int velocity, boolean settle) {
        int result = super.snapToScreen(whichScreen, velocity, settle);
        onDisplayViewChanged(this.mNextScreen);
        if (this.mCurrentScreen != this.mNextScreen) {
            AnalyticalDataCollector.trackFolderRecommendAppRefresh();
        }
        return result;
    }

    private void onDisplayViewChanged(int screenStartIndex) {
        if (getVisibility() == 0) {
            ArrayList<ShortcutInfo> oldDisplay = new ArrayList(this.mCurrentDisplayList);
            this.mCurrentDisplayList.clear();
            int direction = DeviceConfig.getIterateDirection(false);
            int i = screenStartIndex;
            while (true) {
                if (direction == -1) {
                    if (i < screenStartIndex - (screenStartIndex % this.mVisibleRange)) {
                        return;
                    }
                } else if (i >= this.mVisibleRange + screenStartIndex) {
                    return;
                }
                View v = getScreen(i);
                if (v != null) {
                    ShortcutInfo info = (ShortcutInfo) v.getTag();
                    if (!oldDisplay.contains(info)) {
                        addToDisplayList(info, i);
                    }
                    this.mCurrentDisplayList.add(info);
                }
                i += direction;
            }
        }
    }

    private void addToDisplayList(final ShortcutInfo info, final int pos) {
        RecommendInfo recommendInfo = this.mLauncher.getFolderCling().getRecommendScreen().getRecommendInfo();
        long viewInterval = DeviceConfig.getRecommendViewInterval();
        if (!recommendInfo.isDefaultItem(info)) {
            final AppstoreAppInfo appInfo = info.getAppInfo();
            Application.getLauncherApplication(this.mContext).getLauncher().getWorkspace().postDelayed(new Runnable() {
                public void run() {
                    if (RecommendAppsThumbnailView.this.mCurrentDisplayList.contains(info)) {
                        try {
                            AdAction action = Actions.newAdAction("VIEW");
                            String viewKey = "miuihome_recommendationview1";
                            if (Build.IS_INTERNATIONAL_BUILD) {
                                action.addAdMonitor(appInfo.viewMonitorUrls);
                                viewKey = "miuihome_globalrecommendation";
                                action.addParam("ex", appInfo.adInfoPassback);
                            }
                            action.addParam("appId", appInfo.appId);
                            action.addParam("e", "APP_VIEW");
                            action.addParam("ads", appInfo.ads);
                            action.addParam("digest", appInfo.digest);
                            action.addParam("ei", appInfo.experimentalId);
                            action.addParam("pn", appInfo.pkgName);
                            action.addParam("pos", pos % RecommendAppsThumbnailView.this.mVisibleRange);
                            action.addParam("appKey", viewKey);
                            Analytics.trackSystem(RecommendAppsThumbnailView.this.mContext, viewKey, action);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, viewInterval);
            if ((info.getAppInfo().iconUri == null || info.showDefaultIcon()) && info.getBuddyIconView() != null) {
                info.getBuddyIconView().loadIconFromMarket(this.mLauncher, info);
            }
        }
    }

    public void onClick(View v) {
        RecommendInfo recommendInfo = this.mLauncher.getFolderCling().getRecommendScreen().getRecommendInfo();
        if (!recommendInfo.isDefaultItem((ShortcutInfo) v.getTag())) {
            AppstoreAppInfo appInfo = ((ShortcutInfo) v.getTag()).getAppInfo();
            if (appInfo.appUri != null) {
                startAppDetailActivity(v, recommendInfo);
            } else {
                installItem(v, recommendInfo);
            }
            AnalyticalDataCollector.trackFolderRecommendAppClick();
            if (Build.IS_INTERNATIONAL_BUILD) {
                try {
                    AdAction action = Actions.newAdAction("CLICK");
                    action.addAdMonitor(appInfo.clickMonitorUrls);
                    action.addParam("appId", appInfo.appId);
                    action.addParam("e", "APP_CLICK");
                    action.addParam("ads", appInfo.ads);
                    action.addParam("digest", appInfo.digest);
                    action.addParam("ei", appInfo.experimentalId);
                    action.addParam("pn", appInfo.pkgName);
                    action.addParam("ex", appInfo.adInfoPassback);
                    action.addParam("appKey", "miuihome_globalrecommendation");
                    Analytics.trackSystem(this.mContext, "miuihome_globalrecommendation", action);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void startAppDetailActivity(View v, RecommendInfo recommendInfo) {
        ShortcutInfo shortcutInfo = (ShortcutInfo) v.getTag();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(shortcutInfo.getAppInfo().appUri);
        List<ResolveInfo> list = this.mContext.getPackageManager().queryIntentActivities(intent, 65536);
        String marketPackageName = Utilities.getMarketPackageName(this.mLauncher);
        for (ResolveInfo resolveInfo : list) {
            if (!TextUtils.isEmpty(marketPackageName) && marketPackageName.equals(resolveInfo.activityInfo.packageName)) {
                intent.putExtra("folderId", recommendInfo.getFolderInfo().id);
                intent.putExtra("desktopRecommendInfo", recommendInfo.getDesktopRecommendInfo());
                intent.putExtra("currentAppIndex", getChildIndex(v));
                intent.addFlags(65536);
            }
        }
        this.mLauncher.startActivityForResult(intent, 1002);
        this.mLauncher.overridePendingTransition(0, 0);
    }

    public void onAppStartDownload(String appId) {
        for (int i = 0; i < getScreenCount(); i++) {
            View v = getChildAt(i);
            if (appId.equals(((ShortcutInfo) v.getTag()).getAppInfo().appId)) {
                installItem(v, this.mLauncher.getFolderCling().getRecommendScreen().getRecommendInfo());
                return;
            }
        }
    }

    public void snapToAppView(String appId) {
        if (!TextUtils.isEmpty(appId)) {
            for (int i = 0; i < getScreenCount(); i++) {
                if (appId.equals(((ShortcutInfo) getChildAt(i).getTag()).getAppInfo().appId)) {
                    snapToScreen(i);
                    return;
                }
            }
        }
    }

    private void installItem(View v, RecommendInfo info) {
        int refPosition = indexOfChild(v) % this.mVisibleRange;
        AppstoreAppInfo appInfo = ((ShortcutInfo) v.getTag()).getAppInfo();
        Map<String, String> data = new HashMap();
        data.put("ad", Integer.toString(appInfo.ads));
        data.put("di", appInfo.digest);
        data.put("expid", appInfo.experimentalId);
        data.put("packageName", appInfo.pkgName);
        data.put("refPosition", Integer.toString(refPosition));
        data.put("sid", info.getDesktopRecommendInfo().sid);
        DownloadResponse response = MarketManager.getManager(this.mLauncher).startDownload(appInfo.appId, appInfo.pkgName, "desktopRecommend", data);
        if (response.code != 0) {
            Toast.makeText(this.mLauncher, response.msg, 200).show();
        } else if (this.mLauncher.addRecommendAppToWorkspace(v)) {
            removeView(v);
            skipNextAutoLayoutAnimation();
            info.remove((ShortcutInfo) v.getTag());
            ((RecommendShortcutsAdapter) this.mAdapter).remove((ShortcutInfo) v.getTag());
        }
    }
}
