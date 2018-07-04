package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import com.miui.home.launcher.common.Utilities;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class RemovedComponentInfoList {
    private Context mContext;
    private final Map<String, JSONObject> mList = new HashMap();

    public RemovedComponentInfoList(Context context) {
        this.mContext = context;
        this.mList.clear();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(LauncherSettings.getRemovedComponentInfoPath(this.mContext)), 1024);
            while (true) {
                try {
                    String info = reader.readLine();
                    if (TextUtils.isEmpty(info)) {
                        break;
                    }
                    try {
                        JSONObject json = new JSONObject(info);
                        this.mList.put(json.getString("componentName"), json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e2) {
                }
            }
            if (reader != null) {
                Utilities.closeFileSafely(reader);
            }
            BufferedReader bufferedReader = reader;
        } catch (FileNotFoundException e3) {
        }
    }

    public boolean recordRemovedInfo(Cursor c, ComponentName cn) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("componentName", cn.flattenToShortString());
            dataJson.put("cellX", c.getInt(11));
            dataJson.put("cellY", c.getInt(12));
            dataJson.put("screen", c.isNull(10) ? -1 : c.getLong(10));
            dataJson.put("container", c.getInt(7));
            this.mList.put(cn.flattenToShortString(), dataJson);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ShortcutInfo getRemovedInfo(ComponentName cn) {
        ShortcutInfo info = null;
        synchronized (this.mList) {
            try {
                for (String name : this.mList.keySet()) {
                    ComponentName component = ComponentName.unflattenFromString(name);
                    if (component != null && component.getPackageName().equals(cn.getPackageName())) {
                        JSONObject dataJson = (JSONObject) this.mList.get(name);
                        if (dataJson != null) {
                            ShortcutInfo info2 = new ShortcutInfo();
                            try {
                                info2.cellX = dataJson.getInt("cellX");
                                info2.cellY = dataJson.getInt("cellY");
                                info2.screenId = (long) dataJson.getInt("screen");
                                info2.container = (long) dataJson.getInt("container");
                                this.mList.remove(name);
                                writeBackToFile();
                                info = info2;
                            } catch (JSONException e) {
                                JSONException e2 = e;
                                info = info2;
                                e2.printStackTrace();
                                return info;
                            } catch (Throwable th) {
                                Throwable th2 = th;
                                info = info2;
                                throw th2;
                            }
                        }
                    }
                }
            } catch (JSONException e3) {
                e2 = e3;
                e2.printStackTrace();
                return info;
            } catch (Throwable th3) {
                th2 = th3;
                throw th2;
            }
            return info;
        }
    }

    public void writeBackToFile() {
        File infoFile = new File(LauncherSettings.getRemovedComponentInfoPath(this.mContext));
        try {
            if (infoFile.exists() || infoFile.createNewFile()) {
                FileOutputStream out = new FileOutputStream(infoFile, false);
                for (JSONObject data : this.mList.values()) {
                    out.write(data.toString().getBytes());
                    out.write(10);
                }
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
