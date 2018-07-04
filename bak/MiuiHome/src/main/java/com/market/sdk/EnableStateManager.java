package com.market.sdk;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import com.market.sdk.utils.CollectionUtils;
import com.market.sdk.utils.Utils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import miui.os.Build;
import miui.os.SystemProperties;
import org.json.JSONArray;
import org.json.JSONObject;

class EnableStateManager {
    private static String mEnableSettingPath;
    private static Map<String, List<String>> sDefaultEnableRegionList = CollectionUtils.newHashMap();
    private static EnableStateManager sInstance = new EnableStateManager();
    private Context mContext = MarketManager.getContext();
    private Map<String, List<String>> mDisableSettings = CollectionUtils.newConconrrentHashMap();
    private Map<String, List<String>> mEnableSettings = CollectionUtils.newConconrrentHashMap();

    private class EnableStateUpdateReceiver extends BroadcastReceiver {
        private EnableStateUpdateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Utils.isScreenOff()) {
                EnableStateManager.this.updateApplicationEnableStateInner();
            } else {
                EnableStateManager.this.scheduleEnableStateUpdateReceiver(System.currentTimeMillis() + 86400000);
            }
        }
    }

    static {
        List<String> regionList = CollectionUtils.newArrayList();
        if (!Build.IS_STABLE_VERSION) {
            regionList.add("IN");
        }
        sDefaultEnableRegionList.put("com.xiaomi.mipicks", regionList);
    }

    private EnableStateManager() {
        mEnableSettingPath = this.mContext.getFilesDir() + "/" + "package_display_region_settings";
    }

    public static EnableStateManager getManager() {
        return sInstance;
    }

    public void updateApplicationEnableState() {
        if (Utils.isInternationalBuild() && !sDefaultEnableRegionList.isEmpty()) {
            updateApplicationEnableStateInner();
            registerReceiver();
        }
    }

    private void registerReceiver() {
        new Thread() {
            public void run() {
                MarketManager.getContext().registerReceiver(new EnableStateUpdateReceiver(), new IntentFilter("com.xiaomi.market.sdk.EnableUpdateReceiver"));
                EnableStateManager.this.scheduleEnableStateUpdateReceiver(System.currentTimeMillis() + 864000000);
            }
        }.start();
    }

    private void scheduleEnableStateUpdateReceiver(long time) {
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent intent = new Intent("com.xiaomi.market.sdk.EnableUpdateReceiver");
        intent.setPackage(this.mContext.getPackageName());
        am.setExact(1, time, PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728));
    }

    private void updateApplicationEnableStateInner() {
        updateEnableSettingsFromFile();
        for (String pkgName : sDefaultEnableRegionList.keySet()) {
            if (isAppInstalled(pkgName)) {
                updateEnableSetting(pkgName);
            }
        }
        tryUpdateEnableSettings();
    }

    private boolean isAppInstalled(String pkgName) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfo(pkgName, 0) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e("MarketManager", e.getMessage());
            return false;
        }
    }

    private void updateEnableSetting(String pkgName) {
        if (TextUtils.equals(pkgName, "com.xiaomi.mipicks") && Utils.isXSpace()) {
            tryDisablePkg(pkgName);
            return;
        }
        String region = SystemProperties.get("ro.miui.region", "CN");
        List<String> disableRegionList = (List) this.mDisableSettings.get(pkgName);
        if (CollectionUtils.isEmpty(disableRegionList)) {
            List<String> regionList = getEnableSettings(pkgName);
            if (regionList == null || !(regionList.contains(region) || regionList.contains("all"))) {
                tryDisablePkg(pkgName);
            } else {
                tryEnablePkg(pkgName);
            }
        } else if (disableRegionList.contains(region)) {
            tryDisablePkg(pkgName);
        } else {
            tryEnablePkg(pkgName);
        }
    }

    private void tryDisablePkg(String pkgName) {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            int state = this.mContext.getPackageManager().getApplicationEnabledSetting(pkgName);
            if (state == 0 || state == 1) {
                pm.setApplicationEnabledSetting(pkgName, 2, 0);
            }
        } catch (Exception e) {
            Log.d("MarketManager", e.getMessage());
        }
    }

    private void tryEnablePkg(String pkgName) {
        try {
            PackageManager pm = this.mContext.getPackageManager();
            if (this.mContext.getPackageManager().getApplicationEnabledSetting(pkgName) == 2) {
                pm.setApplicationEnabledSetting(pkgName, 1, 0);
            }
        } catch (Exception e) {
            Log.d("MarketManager", e.getMessage());
        }
    }

    private List<String> getEnableSettings(String pkgName) {
        List<String> regions = (List) this.mEnableSettings.get(pkgName);
        if (regions != null) {
            return regions;
        }
        regions = (List) sDefaultEnableRegionList.get(pkgName);
        if (regions != null) {
            return regions;
        }
        String[] regionArray = Utils.getStringArray(pkgName, "enable_regions");
        if (regionArray != null) {
            return Arrays.asList(regionArray);
        }
        return regions;
    }

    private void updateEnableSettingsFromFile() {
        Exception e;
        Throwable th;
        this.mEnableSettings.clear();
        this.mDisableSettings.clear();
        BufferedReader bufferedReader = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mEnableSettingPath)));
            try {
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String temp = reader.readLine();
                    if (temp == null) {
                        break;
                    }
                    sb.append(temp);
                }
                JSONObject json = new JSONObject(sb.toString());
                Iterator<String> pkgNameList = json.keys();
                while (pkgNameList.hasNext()) {
                    int index;
                    String pkgName = (String) pkgNameList.next();
                    JSONObject settingJson = json.getJSONObject(pkgName);
                    JSONArray enableRegionArray = settingJson.optJSONArray("enable_list");
                    if (enableRegionArray != null) {
                        List<String> enableRegionList = CollectionUtils.newArrayList();
                        for (index = 0; index < enableRegionArray.length(); index++) {
                            enableRegionList.add(enableRegionArray.getString(index));
                        }
                        this.mEnableSettings.put(pkgName, enableRegionList);
                    }
                    JSONArray disableRegionArray = settingJson.optJSONArray("disable_list");
                    if (disableRegionArray != null) {
                        List<String> disableRegionList = CollectionUtils.newArrayList();
                        for (index = 0; index < disableRegionArray.length(); index++) {
                            disableRegionList.add(disableRegionArray.getString(index));
                        }
                        this.mDisableSettings.put(pkgName, disableRegionList);
                    }
                }
                Utils.closeQuietly(reader);
                bufferedReader = reader;
            } catch (Exception e2) {
                e = e2;
                bufferedReader = reader;
                try {
                    Log.e("MarketManager", e.getMessage());
                    Utils.closeQuietly(bufferedReader);
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeQuietly(bufferedReader);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedReader = reader;
                Utils.closeQuietly(bufferedReader);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e("MarketManager", e.getMessage());
            Utils.closeQuietly(bufferedReader);
        }
    }

    private void tryUpdateEnableSettings() {
        new RemoteMethodInvoker<Object>() {
            public Object innerInvoke(IMarketService service) {
                Exception e;
                Throwable th;
                FileOutputStream fos = null;
                try {
                    String settings = service.getEnableSettings();
                    if (TextUtils.isEmpty(settings)) {
                        Utils.closeQuietly(null);
                    } else {
                        FileOutputStream fos2 = new FileOutputStream(EnableStateManager.mEnableSettingPath);
                        try {
                            fos2.write(settings.getBytes());
                            fos2.flush();
                            Utils.closeQuietly(fos2);
                            fos = fos2;
                        } catch (Exception e2) {
                            e = e2;
                            fos = fos2;
                            try {
                                Log.e("MarketManager", e.getMessage(), e);
                                Utils.closeQuietly(fos);
                                return null;
                            } catch (Throwable th2) {
                                th = th2;
                                Utils.closeQuietly(fos);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fos = fos2;
                            Utils.closeQuietly(fos);
                            throw th;
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    Log.e("MarketManager", e.getMessage(), e);
                    Utils.closeQuietly(fos);
                    return null;
                }
                return null;
            }
        }.invokeInNewThread();
    }
}
