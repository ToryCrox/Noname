package com.miui.home.launcher;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class ProgressInfoList {
    private Context mContext;
    private final Map<String, JSONObject> mProgressList = new HashMap();
    private final Map<String, JSONObject> mStatusTitleMap = new HashMap();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ProgressInfoList(android.content.Context r9) {
        /*
        r8 = this;
        r8.<init>();
        r5 = new java.util.HashMap;
        r5.<init>();
        r8.mProgressList = r5;
        r5 = new java.util.HashMap;
        r5.<init>();
        r8.mStatusTitleMap = r5;
        r6 = r8.mProgressList;
        monitor-enter(r6);
        r8.mContext = r9;	 Catch:{ all -> 0x007b }
        r3 = 0;
        r5 = r8.mProgressList;	 Catch:{ all -> 0x007b }
        r5.clear();	 Catch:{ all -> 0x007b }
        r4 = new java.io.BufferedReader;	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
        r5 = new java.io.FileReader;	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
        r7 = r8.mContext;	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
        r7 = com.miui.home.launcher.LauncherSettings.getDownloadInstallInfoPath(r7);	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
        r5.<init>(r7);	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
        r7 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;
        r4.<init>(r5, r7);	 Catch:{ FileNotFoundException -> 0x0088, IOException -> 0x0086, all -> 0x007e }
    L_0x002e:
        r1 = 0;
        r1 = r4.readLine();	 Catch:{ IOException -> 0x005e, FileNotFoundException -> 0x0057, all -> 0x0083 }
        r5 = android.text.TextUtils.isEmpty(r1);	 Catch:{ FileNotFoundException -> 0x0057, IOException -> 0x0074, all -> 0x0083 }
        if (r5 != 0) goto L_0x005f;
    L_0x0039:
        r2 = new org.json.JSONObject;	 Catch:{ JSONException -> 0x0052 }
        r2.<init>(r1);	 Catch:{ JSONException -> 0x0052 }
        r5 = "statusTitleMap";
        r5 = r2.has(r5);	 Catch:{ JSONException -> 0x0052 }
        if (r5 == 0) goto L_0x0068;
    L_0x0046:
        r5 = r8.mStatusTitleMap;	 Catch:{ JSONException -> 0x0052 }
        r7 = "progressOwner";
        r7 = r2.getString(r7);	 Catch:{ JSONException -> 0x0052 }
        r5.put(r7, r2);	 Catch:{ JSONException -> 0x0052 }
        goto L_0x002e;
    L_0x0052:
        r0 = move-exception;
        r0.printStackTrace();	 Catch:{ FileNotFoundException -> 0x0057, IOException -> 0x0074, all -> 0x0083 }
        goto L_0x002e;
    L_0x0057:
        r0 = move-exception;
        r3 = r4;
    L_0x0059:
        com.miui.home.launcher.common.Utilities.closeFileSafely(r3);	 Catch:{ all -> 0x007b }
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
    L_0x005d:
        return;
    L_0x005e:
        r0 = move-exception;
    L_0x005f:
        r4.close();	 Catch:{ FileNotFoundException -> 0x0057, IOException -> 0x0074, all -> 0x0083 }
        com.miui.home.launcher.common.Utilities.closeFileSafely(r4);	 Catch:{ all -> 0x007b }
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        r3 = r4;
        goto L_0x005d;
    L_0x0068:
        r5 = r8.mProgressList;	 Catch:{ JSONException -> 0x0052 }
        r7 = "packageName";
        r7 = r2.getString(r7);	 Catch:{ JSONException -> 0x0052 }
        r5.put(r7, r2);	 Catch:{ JSONException -> 0x0052 }
        goto L_0x002e;
    L_0x0074:
        r0 = move-exception;
        r3 = r4;
    L_0x0076:
        com.miui.home.launcher.common.Utilities.closeFileSafely(r3);	 Catch:{ all -> 0x007b }
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        goto L_0x005d;
    L_0x007b:
        r5 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x007b }
        throw r5;
    L_0x007e:
        r5 = move-exception;
    L_0x007f:
        com.miui.home.launcher.common.Utilities.closeFileSafely(r3);	 Catch:{ all -> 0x007b }
        throw r5;	 Catch:{ all -> 0x007b }
    L_0x0083:
        r5 = move-exception;
        r3 = r4;
        goto L_0x007f;
    L_0x0086:
        r0 = move-exception;
        goto L_0x0076;
    L_0x0088:
        r0 = move-exception;
        goto L_0x0059;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.ProgressInfoList.<init>(android.content.Context):void");
    }

    public boolean containsAppProgress(String packageName) {
        synchronized (this.mProgressList) {
            Iterator i$ = new ArrayList(this.mProgressList.values()).iterator();
            while (i$.hasNext()) {
                try {
                    if (packageName.equals(((JSONObject) i$.next()).get("packageName"))) {
                        return true;
                    }
                } catch (JSONException e) {
                }
            }
            return false;
        }
    }

    public boolean recordProgressInfo(ShortcutInfo info) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("packageName", info.getPackageName());
            dataJson.put("itemType", info.itemType);
            dataJson.put("progressOwner", info.appProgressServer);
            synchronized (this.mProgressList) {
                this.mProgressList.put(info.getPackageName(), dataJson);
                writeBackToFile();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean recordStatusTitleMap(String server, String titleMap) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("progressOwner", server);
            dataJson.put("statusTitleMap", titleMap);
            synchronized (this.mProgressList) {
                this.mStatusTitleMap.put(server, dataJson);
                writeBackToFile();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeProgressInfo(String packageName) {
        synchronized (this.mProgressList) {
            this.mProgressList.remove(packageName);
            writeBackToFile();
        }
    }

    public boolean getProgressInfo(String packageName, ShortcutInfo shortcutInfo) {
        boolean z = false;
        synchronized (this.mProgressList) {
            if (this.mProgressList.containsKey(packageName)) {
                JSONObject dataJson = (JSONObject) this.mProgressList.get(packageName);
                if (dataJson != null) {
                    try {
                        shortcutInfo.getAppInfo().pkgName = dataJson.getString("packageName");
                        shortcutInfo.itemType = dataJson.getInt("itemType");
                        shortcutInfo.appProgressServer = dataJson.getString("progressOwner");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!TextUtils.isEmpty(shortcutInfo.appProgressServer)) {
                    z = true;
                }
            }
        }
        return z;
    }

    public void getStatusTitleMap(HashMap<String, JSONObject> titleMap) {
        if (titleMap != null) {
            synchronized (this.mProgressList) {
                for (String server : this.mStatusTitleMap.keySet()) {
                    JSONObject dataJson = null;
                    try {
                        dataJson = new JSONObject(((JSONObject) this.mStatusTitleMap.get(server)).getString("statusTitleMap"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (dataJson != null) {
                        titleMap.put(server, dataJson);
                    }
                }
            }
        }
    }

    private void writeBackToFile() {
        synchronized (this.mProgressList) {
            File infoFile = new File(LauncherSettings.getDownloadInstallInfoPath(this.mContext));
            try {
                if (infoFile.exists() || infoFile.createNewFile()) {
                    FileOutputStream out = new FileOutputStream(infoFile, false);
                    String content = "";
                    ArrayList<JSONObject> all = new ArrayList(this.mStatusTitleMap.values());
                    all.addAll(new ArrayList(this.mProgressList.values()));
                    Iterator i$ = all.iterator();
                    while (i$.hasNext()) {
                        content = content + ((JSONObject) i$.next()).toString() + "\n";
                    }
                    out.write(content.getBytes());
                    out.flush();
                    out.close();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getAllProgressPackages() {
        ArrayList<String> arrayList;
        synchronized (this.mProgressList) {
            arrayList = new ArrayList(this.mProgressList.keySet());
        }
        return arrayList;
    }
}
