package com.miui.home.launcher;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class RetainedList {
    public static final Uri MIHOME_MANAGER_URI = Uri.parse("content://com.xiaomi.mihomemanager.whitelistProvider/packageName");
    private final HashSet<String> mList = new HashSet();

    public RetainedList(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(MIHOME_MANAGER_URI, null, null, null, null);
            while (cursor != null && cursor.moveToNext()) {
                String name = cursor.getString(1);
                if (!TextUtils.isEmpty(name)) {
                    this.mList.add(name);
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(LauncherSettings.OPERATOR_RETAINED_LIST_PATH), 1024);
            while (true) {
                try {
                    String packageName = reader.readLine();
                    if (TextUtils.isEmpty(packageName)) {
                        break;
                    }
                    this.mList.add(packageName);
                } catch (IOException e2) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            BufferedReader bufferedReader = reader;
        } catch (FileNotFoundException e4) {
        }
    }

    public boolean contain(String packageName) {
        return this.mList.contains(packageName);
    }

    public boolean contain(Intent intent) {
        return (intent == null || intent.getComponent() == null) ? false : contain(intent.getComponent().getPackageName());
    }
}
