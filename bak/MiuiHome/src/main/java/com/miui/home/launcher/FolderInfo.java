package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import com.market.sdk.AdsBannerInfo;
import com.market.sdk.AppstoreAppInfo;
import com.market.sdk.DesktopRecommendCallback;
import com.market.sdk.DesktopRecommendInfo;
import com.market.sdk.ImageCallback;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.Folder.FolderCallback;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class FolderInfo extends ItemInfo {
    ArrayList<ShortcutInfo> contents;
    public FolderCallback icon;
    private ShortcutsAdapter mAdapter;
    private FolderIcon mBuddyIconView;
    public DesktopRecommendCallback mCallback;
    private boolean mEnbaleRecommendAppsView;
    private String mKeyEnbaleRecommendAppsView;
    private RecommendInfo mRecommendAppsInfo;
    boolean opened;
    private CharSequence title;

    public class RecommendInfo {
        public Uri mBackgroundUri;
        private long mCacheEndTime;
        private Context mContext;
        private DesktopRecommendInfo mDesktopRecommendInfo;
        private RecommendShortcutsAdapter mRecommendAppsAdapter = null;
        public ArrayList<ShortcutInfo> mRecommendAppsContents = new ArrayList();
        public ArrayList<ShortcutInfo> mRecommendDefaultContents = new ArrayList();
        public String mRecommendTitle;
        public Runnable mRefreshRunnable = new Runnable() {
            public void run() {
                Launcher launcher = Application.getLauncherApplication(RecommendInfo.this.mContext).getLauncher();
                if (launcher != null) {
                    if (launcher.getFolderCling().isRecommendScreenAnimating()) {
                        launcher.getFolderCling().postDelayed(this, 50);
                        return;
                    }
                    RecommendInfo.this.clearContents(false);
                    for (AppstoreAppInfo appInfo : new ArrayList(RecommendInfo.this.mDesktopRecommendInfo.appInfoList)) {
                        if (!(appInfo == null || TextUtils.isEmpty(appInfo.pkgName))) {
                            if (RecommendInfo.this.hasDuplicateRecommendApps(appInfo.pkgName) || launcher.getFirstAppInfo(appInfo.pkgName, true) != null) {
                                RecommendInfo.this.mDesktopRecommendInfo.appInfoList.remove(appInfo);
                            } else {
                                ShortcutInfo shortcutInfo = new ShortcutInfo();
                                shortcutInfo.itemType = 13;
                                shortcutInfo.setTitle(appInfo.title, launcher);
                                shortcutInfo.setAppInfo(appInfo);
                                Intent appIntent = new Intent();
                                appIntent.setComponent(new ComponentName(appInfo.pkgName, "invalidClassName"));
                                shortcutInfo.intent = appIntent;
                                shortcutInfo.container = FolderInfo.this.id;
                                shortcutInfo.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
                                RecommendInfo.this.add(shortcutInfo, false);
                            }
                        }
                    }
                    RecommendInfo.this.getAdapter().refreshList(false);
                }
            }
        };

        public RecommendInfo(Context context) {
            this.mContext = context;
        }

        public void initDefaultDisplayContents(ArrayList<ShortcutInfo> list) {
            this.mRecommendDefaultContents.addAll(list);
        }

        private void setCacheEndTime(long endTime) {
            this.mCacheEndTime = endTime;
        }

        public long getCacheEndTime() {
            return this.mCacheEndTime;
        }

        public void add(ShortcutInfo item, boolean refreshNow) {
            if (!this.mRecommendAppsContents.contains(item)) {
                this.mRecommendAppsContents.add(item);
                if (refreshNow) {
                    getAdapter().refreshList(false);
                }
                if (!this.mDesktopRecommendInfo.appInfoList.contains(item.getAppInfo())) {
                    this.mDesktopRecommendInfo.appInfoList.add(item.getAppInfo());
                }
            }
        }

        public boolean contains(ShortcutInfo item) {
            if (this.mRecommendDefaultContents == null) {
                return false;
            }
            return this.mRecommendDefaultContents.contains(item);
        }

        public void remove(ShortcutInfo item) {
            this.mRecommendAppsContents.remove(item);
            String appId = item.getAppInfo().appId;
            if (!TextUtils.isEmpty(appId)) {
                for (AppstoreAppInfo appInfo : this.mDesktopRecommendInfo.appInfoList) {
                    if (appId.equals(appInfo.appId)) {
                        this.mDesktopRecommendInfo.appInfoList.remove(appInfo);
                        return;
                    }
                }
            }
        }

        int count() {
            return this.mRecommendAppsContents.size();
        }

        public RecommendShortcutsAdapter getAdapter() {
            if (this.mRecommendAppsAdapter == null) {
                this.mRecommendAppsAdapter = new RecommendShortcutsAdapter(this.mContext, FolderInfo.this.getRecommendInfo(this.mContext));
            }
            return this.mRecommendAppsAdapter;
        }

        public void clearContents(boolean refreshNow) {
            Iterator i$ = this.mRecommendAppsContents.iterator();
            while (i$.hasNext()) {
                ((ShortcutInfo) i$.next()).recycleIconRes();
            }
            this.mRecommendAppsContents.clear();
            this.mBackgroundUri = null;
            this.mRecommendTitle = null;
            if (refreshNow) {
                getAdapter().refreshList(false);
            }
        }

        public void changedViewOnLoadFailed() {
            final Launcher launcher = Application.getLauncherApplication(this.mContext).getLauncher();
            launcher.getFolderCling().post(new Runnable() {
                public void run() {
                    if (launcher != null) {
                        if (launcher.getFolderCling().isRecommendScreenAnimating()) {
                            launcher.getFolderCling().postDelayed(this, 100);
                            return;
                        }
                        RecommendInfo.this.clearContents(false);
                        launcher.getFolderCling().getRecommendScreen().getRecommendTitle().setText(R.string.recommend_apps_notice_nodata);
                        RecommendInfo.this.getAdapter().refreshList(false);
                    }
                }
            });
        }

        public void setRecommendInfo(DesktopRecommendInfo info) {
            Launcher launcher = Application.getLauncherApplication(this.mContext).getLauncher();
            if (launcher != null && info.appInfoList.size() > 0) {
                loadBannerImage(info.bannerList);
                loadBackground(info.backgroundImageUrl);
                this.mRecommendTitle = info.description;
                this.mDesktopRecommendInfo = info;
                setCacheEndTime(System.currentTimeMillis() + info.cacheTime);
                launcher.getFolderCling().post(this.mRefreshRunnable);
            }
        }

        public void loadBackground(String backgroundKey) {
            if (!TextUtils.isEmpty(backgroundKey)) {
                MarketManager.getManager(this.mContext).loadImage(backgroundKey, DeviceConfig.getScreenWidth(), DeviceConfig.getScreenHeight(), new ImageCallback() {
                    public void onImageLoadFailed(String url) {
                    }

                    public void onImageLoadSuccess(String url, Uri uri) {
                        RecommendInfo.this.mBackgroundUri = uri;
                    }
                });
            }
        }

        public void loadBannerImage(List<AdsBannerInfo> bannerList) {
            for (AdsBannerInfo bannerInfo : bannerList) {
                final AdsBannerInfo info = bannerInfo;
                MarketManager.getManager(this.mContext).loadImage(bannerInfo.iconUrl, DeviceConfig.sRecommendBannerWidth, DeviceConfig.sRecommendBannerHeight, new ImageCallback() {
                    public void onImageLoadFailed(String url) {
                    }

                    public void onImageLoadSuccess(String url, Uri uri) {
                        info.iconUri = uri;
                    }
                });
            }
        }

        public List<AdsBannerInfo> getBannerList() {
            if (this.mDesktopRecommendInfo == null) {
                return null;
            }
            return this.mDesktopRecommendInfo.bannerList;
        }

        public DesktopRecommendInfo getDesktopRecommendInfo() {
            return this.mDesktopRecommendInfo;
        }

        public FolderInfo getFolderInfo() {
            return FolderInfo.this;
        }

        public boolean isDefaultItem(ShortcutInfo info) {
            return DeviceConfig.sRecommendDefaultTitle != null && DeviceConfig.sRecommendDefaultTitle.equals(info.getTitle(null));
        }

        public boolean removedRecommendAppsByPackageName(String packageName) {
            Iterator i$ = this.mRecommendAppsContents.iterator();
            while (i$.hasNext()) {
                ShortcutInfo shortcutInfo = (ShortcutInfo) i$.next();
                if (shortcutInfo.getPackageName().equals(packageName)) {
                    remove(shortcutInfo);
                    if (this.mRecommendAppsAdapter != null) {
                        this.mRecommendAppsAdapter.remove(shortcutInfo);
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean hasDuplicateRecommendApps(String packageName) {
            Iterator i$ = this.mRecommendAppsContents.iterator();
            while (i$.hasNext()) {
                if (((ShortcutInfo) i$.next()).getPackageName().equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        public void requestDataFromMarket() {
            DeviceConfig.loadRecommendData(this.mContext);
            if ((count() <= 4 || this.mCacheEndTime < System.currentTimeMillis()) && FolderInfo.this.mEnbaleRecommendAppsView && DeviceConfig.isRecommendServerEnable()) {
                CharSequence folderName = this.mContext.getResources().getString(R.string.default_folder_title_recommend).equals(FolderInfo.this.getTitle(this.mContext)) ? "recommend" : FolderInfo.this.getTitle(this.mContext);
                try {
                    MarketManager.getManager(this.mContext).loadDesktopRecommendInfo(FolderInfo.this.id, folderName == null ? "null" : folderName.toString(), FolderInfo.this.getPackageNameList(), FolderInfo.this.mCallback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void initRecommendViewAndRequest() {
            Application.getLauncherApplication(this.mContext).getLauncher().getFolderCling().getRecommendScreen().getRecommendTitle().setText(R.string.recommend_apps_notice);
            getAdapter().initRecommendList();
            getAdapter().notifyDataSetChanged();
            requestDataFromMarket();
        }
    }

    public FolderInfo() {
        this.icon = null;
        this.contents = new ArrayList();
        this.mAdapter = null;
        this.mKeyEnbaleRecommendAppsView = "key_enable_recommend_apps_view_";
        this.mEnbaleRecommendAppsView = false;
        this.mCallback = new DesktopRecommendCallback() {
            public void onLoadFailed() {
                FolderInfo.this.getRecommendInfo(null).changedViewOnLoadFailed();
            }

            public void onLoadSuccess(DesktopRecommendInfo info) {
                if (info == null || info.appInfoList.size() == 0) {
                    onLoadFailed();
                } else {
                    FolderInfo.this.getRecommendInfo(null).setRecommendInfo(info);
                }
            }
        };
        this.itemType = 2;
    }

    public void load(Context context, Cursor c) {
        super.load(context, c);
        this.title = c.getString(2);
        Iterator i$ = this.contents.iterator();
        while (i$.hasNext()) {
            ((ShortcutInfo) i$.next()).container = this.id;
        }
    }

    public void add(ShortcutInfo item) {
        add(item, false);
    }

    public void add(ShortcutInfo item, boolean atLast) {
        if (atLast) {
            int maxCellX = -1;
            Iterator i$ = this.contents.iterator();
            while (i$.hasNext()) {
                maxCellX = Math.max(maxCellX, ((ShortcutInfo) i$.next()).cellX);
            }
            item.cellX = maxCellX + 1;
        }
        this.contents.add(item);
        item.container = this.id;
    }

    public void clear() {
        this.contents.clear();
        notifyDataSetChanged();
    }

    public boolean contains(ShortcutInfo item) {
        if (this.contents == null) {
            return false;
        }
        return this.contents.contains(item);
    }

    public void preLoadContentView(final Launcher launcher) {
        Iterator i$ = this.contents.iterator();
        while (i$.hasNext()) {
            launcher.createItemIcon(launcher.getFolderCling().getFolder().getContent(), (ShortcutInfo) i$.next());
        }
        this.mKeyEnbaleRecommendAppsView += this.id;
        this.mBuddyIconView.post(new Runnable() {
            public void run() {
                if (FolderInfo.this.id != -1) {
                    FolderInfo.this.initRecommendEnableState(launcher);
                    FolderInfo.this.getRecommendInfo(launcher).initDefaultDisplayContents(launcher.getFolderCling().getRecommendScreen().getRecommendDefaultContents());
                    FolderInfo.this.getRecommendInfo(launcher).requestDataFromMarket();
                }
            }
        });
    }

    public void initRecommendEnableState(Launcher launcher) {
        boolean enable = false;
        Context context = launcher.getApplicationContext();
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(this.mKeyEnbaleRecommendAppsView)) {
            enable = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(this.mKeyEnbaleRecommendAppsView, false);
        } else {
            List<String> qualityBusinessDeviceList = Arrays.asList(context.getResources().getStringArray(R.array.quality_business_devices));
            if (context.getResources().getString(R.string.default_folder_title_recommend).equals(getTitle(context)) && !qualityBusinessDeviceList.contains(Build.DEVICE) && System.currentTimeMillis() - launcher.getHomeDataCreateTime() > FolderCling.RECOMMEND_DISABLE_INTERVAL && DeviceConfig.getRecommendEnableDefault()) {
                enable = true;
            }
        }
        setRecommendAppsViewEnable(enable);
    }

    public void setBuddyIconView(FolderIcon icon) {
        this.mBuddyIconView = icon;
    }

    public FolderIcon getBuddyIconView() {
        return this.mBuddyIconView;
    }

    public void remove(ShortcutInfo item) {
        this.contents.remove(item);
    }

    public boolean remove(long id) {
        Iterator i$ = this.contents.iterator();
        while (i$.hasNext()) {
            ShortcutInfo info = (ShortcutInfo) i$.next();
            if (info.id == id) {
                this.contents.remove(info);
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    void notifyDataSetChanged() {
        if (this.mAdapter != null) {
            this.mAdapter.notifyDataSetChanged();
        } else {
            refreshPreviewIcons();
        }
    }

    void refreshPreviewIcons() {
        if (this.icon != null) {
            this.icon.loadItemIcons();
        }
    }

    int count() {
        return this.contents.size();
    }

    public ShortcutsAdapter getAdapter(Context context) {
        if (this.mAdapter == null) {
            this.mAdapter = new ShortcutsAdapter(context, this);
        }
        return this.mAdapter;
    }

    public CharSequence getTitle(Context context) {
        if (context == null) {
            return this.title;
        }
        return LauncherModel.loadTitle(context, this.title);
    }

    public void setTitle(CharSequence title, Context context) {
        this.title = title;
        if (this.icon != null) {
            this.icon.setTitle(getTitle(context));
        }
        if (this.id != -1) {
            LauncherModel.updateTitleInDatabase(context, this.id, title);
        }
    }

    public void onAddToDatabase(Context context, ContentValues values) {
        super.onAddToDatabase(context, values);
        values.put("title", this.title.toString());
    }

    public ItemInfo clone() {
        FolderInfo ufi = (FolderInfo) super.clone();
        ufi.contents = new ArrayList();
        return ufi;
    }

    public ArrayList<String> getPackageNameList() {
        ArrayList<String> nameList = new ArrayList();
        for (int i = 0; i < this.contents.size(); i++) {
            nameList.add(((ShortcutInfo) this.contents.get(i)).getPackageName());
        }
        return nameList;
    }

    public RecommendInfo getRecommendInfo(Context context) {
        if (this.mRecommendAppsInfo == null) {
            this.mRecommendAppsInfo = new RecommendInfo(context);
        }
        return this.mRecommendAppsInfo;
    }

    public void setRecommendAppsViewEnable(boolean enable) {
        this.mEnbaleRecommendAppsView = enable;
    }

    public void recordRecommendAppsSwitchState(Context context, boolean enable) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(this.mKeyEnbaleRecommendAppsView, enable).commit();
    }

    public boolean isRecommendAppsViewEnable(Context context) {
        return Utilities.isRecommendationEnabled(context) && this.mEnbaleRecommendAppsView;
    }

    public void removeRecommendAppsViewKey(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().remove(this.mKeyEnbaleRecommendAppsView).commit();
    }
}
