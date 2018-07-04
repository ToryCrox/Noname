package com.market.sdk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.market.internal.DesktopRecommendManager;
import com.market.sdk.utils.Utils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import miui.os.Build;
import org.json.JSONObject;

public class MarketManager {
    public static final String MARKET_PACKAGE_NAME = (Build.IS_INTERNATIONAL_BUILD ? "com.xiaomi.discover" : "com.xiaomi.market");
    private static volatile MarketManager sManager;
    private final String DETAIL_CLASS_NAME = "com.xiaomi.market.ui.AppDetailActivity";
    public final String MARKET_SERVICE_CLASS_NAME = "com.xiaomi.market.data.MarketService";
    private final String MARKET_USER_AGREEMENT_CLASS = "com.xiaomi.market.ui.UserAgreementActivity";
    private Context mContext;

    private MarketManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static MarketManager getManager(Context context) {
        if (sManager == null) {
            synchronized (MarketManager.class) {
                if (sManager == null) {
                    sManager = new MarketManager(context);
                }
            }
        }
        return sManager;
    }

    public static Context getContext() {
        return sManager.mContext;
    }

    private void ensureNotUiThread() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new UnsupportedOperationException("Can't call the method on ui thread");
        }
    }

    public static String getMarketPackageName() {
        return MARKET_PACKAGE_NAME;
    }

    public boolean isAppStoreInstalled() {
        return isAppStoreInstalled(true);
    }

    public boolean isAppStoreEnabled() {
        PackageManager pm = this.mContext.getPackageManager();
        try {
            if (!isAppStoreInstalled()) {
                return false;
            }
            int enable = pm.getApplicationEnabledSetting(MARKET_PACKAGE_NAME);
            if (enable == 0 || enable == 1) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            Log.e("MarketManager", "IllegalArgmentException when get enabled state of appstore : " + e);
            return false;
        }
    }

    public boolean isAppStoreInstalled(boolean needRomMarket) {
        try {
            ApplicationInfo aInfo = this.mContext.getPackageManager().getApplicationInfo(MARKET_PACKAGE_NAME, 0);
            if (aInfo != null) {
                if (needRomMarket && (aInfo.flags & 1) == 0) {
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public boolean allowConnectToNetwork() {
        ensureNotUiThread();
        if (!isAppStoreInstalled(true)) {
            return false;
        }
        Boolean result = (Boolean) new RemoteMethodInvoker<Boolean>() {
            public Boolean innerInvoke(IMarketService service) {
                try {
                    return Boolean.valueOf(service.allowConnectToNetwork());
                } catch (Exception e) {
                    Log.e("MarketManager", "Exception: " + e);
                    return Boolean.valueOf(false);
                }
            }
        }.invoke();
        if (result != null) {
            return result.booleanValue();
        }
        return false;
    }

    public boolean startUserAgreementActivity(Activity resultActivity, int requestCode) {
        if (!isAppStoreInstalled(true) || resultActivity == null) {
            return false;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(MARKET_PACKAGE_NAME, "com.xiaomi.market.ui.UserAgreementActivity"));
        resultActivity.startActivityForResult(intent, requestCode);
        return true;
    }

    public void loadIcon(String appId, String iconMask, ImageCallback callback) {
        ImageManager.loadIcon(appId, iconMask, callback);
    }

    public void loadImage(String url, int width, int height, ImageCallback callback) {
        ImageManager.loadImage(url, width, height, callback);
    }

    public void loadDesktopRecommendInfo(long folderId, String folderName, List<String> pkgNameList, DesktopRecommendCallback callback) {
        DesktopRecommendManager.loadDesktopRecommendInfo(folderId, folderName, pkgNameList, callback);
    }

    public DownloadResponse startDownload(String appId, String pkgName, String ref, Map<String, String> extraParams) {
        DownloadResponse response = new DownloadResponse();
        if (TextUtils.isEmpty(appId) && TextUtils.isEmpty(pkgName)) {
            response.code = -1;
        } else if (Utils.isConnected(this.mContext)) {
            Intent intent = new Intent("com.xiaomi.market.service.AppDownloadInstallService");
            intent.setPackage(MARKET_PACKAGE_NAME);
            intent.putExtra("appId", appId);
            intent.putExtra("packageName", pkgName);
            intent.putExtra("senderPackageName", this.mContext.getPackageName());
            intent.putExtra("ref", ref);
            if (extraParams != null) {
                Set<String> keys = extraParams.keySet();
                JSONObject json = new JSONObject();
                try {
                    for (String key : keys) {
                        json.put(key, (String) extraParams.get(key));
                    }
                    intent.putExtra("extra_query_params", json.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.mContext.startService(intent);
            response.code = 0;
        } else {
            response.code = -2;
            response.msg = Utils.getStringResources("install_no_network_description");
        }
        return response;
    }

    public void updateApplicationEnableState() {
        try {
            EnableStateManager.getManager().updateApplicationEnableState();
        } catch (Exception e) {
            Log.w("MarketManager", e.getMessage(), e);
        }
    }

    public int getCategory(final String... pkgList) {
        ensureNotUiThread();
        if (!isAppStoreInstalled(true) || pkgList.length == 0) {
            return -1;
        }
        Integer result = (Integer) new RemoteMethodInvoker<Integer>() {
            public Integer innerInvoke(IMarketService service) {
                try {
                    return Integer.valueOf(service.getCategory(pkgList));
                } catch (Exception e) {
                    Log.e("MarketManager", "Exception: " + e);
                    return Integer.valueOf(-1);
                }
            }
        }.invoke();
        if (result != null) {
            return result.intValue();
        }
        return -1;
    }
}
