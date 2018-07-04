package com.miui.home.launcher.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseIntArray;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

public class AppCategoryManager {
    private static AppCategoryManager mInstance;
    private static SparseIntArray sAppCategoryResourceList = new SparseIntArray();
    private static final HashMap<String, Integer> sCategoryList = new HashMap();

    public interface OnCategoryLoadedListener {
        void onAppCategoryIdLoaded(int i);
    }

    static {
        sAppCategoryResourceList.put(1, R.string.app_category_finance);
        sAppCategoryResourceList.put(2, R.string.app_category_social);
        sAppCategoryResourceList.put(3, R.string.app_category_travel);
        sAppCategoryResourceList.put(4, R.string.app_category_lifestyle);
        sAppCategoryResourceList.put(5, R.string.app_category_tools);
        sAppCategoryResourceList.put(6, R.string.app_category_photos);
        sAppCategoryResourceList.put(7, R.string.app_category_books);
        sAppCategoryResourceList.put(8, R.string.app_category_sport);
        sAppCategoryResourceList.put(9, R.string.app_category_shopping);
        sAppCategoryResourceList.put(10, R.string.app_category_productivity);
        sAppCategoryResourceList.put(11, R.string.app_category_news);
        sAppCategoryResourceList.put(12, R.string.app_category_education);
        sAppCategoryResourceList.put(13, R.string.app_category_entertainment);
        sAppCategoryResourceList.put(14, R.string.app_category_health);
        sAppCategoryResourceList.put(15, R.string.app_category_games);
        sAppCategoryResourceList.put(27, R.string.app_category_music_movie);
        sAppCategoryResourceList.put(209, R.string.app_category_vr);
    }

    public static AppCategoryManager getInstance() {
        if (mInstance == null) {
            mInstance = new AppCategoryManager();
        }
        return mInstance;
    }

    public void getAppCategoryId(final Context context, final OnCategoryLoadedListener callback, final String... packageNames) {
        if (packageNames != null && callback != null) {
            new AsyncTask<Void, Void, Integer>() {
                protected Integer doInBackground(Void... params) {
                    int resId = 0;
                    int category = MarketManager.getManager(context).getCategory(packageNames);
                    if (category == -1) {
                        Log.d("com.miui.home.launcher.common.AppCategoryManager", "get nothing from market");
                        synchronized (AppCategoryManager.sCategoryList) {
                            for (int i = 0; i < packageNames.length; i++) {
                                if (AppCategoryManager.sCategoryList.containsKey(packageNames[i])) {
                                    resId = AppCategoryManager.sAppCategoryResourceList.get(((Integer) AppCategoryManager.sCategoryList.get(packageNames[i])).intValue());
                                    break;
                                }
                            }
                        }
                    } else {
                        resId = AppCategoryManager.sAppCategoryResourceList.get(category);
                    }
                    if (resId == 0) {
                        resId = R.string.folder_name;
                    }
                    return Integer.valueOf(resId);
                }

                protected void onPostExecute(Integer param) {
                    callback.onAppCategoryIdLoaded(param.intValue());
                }
            }.execute(new Void[0]);
        }
    }

    public void initAppCategoryListAsync(final Context context) {
        new Thread() {
            public void run() {
                synchronized (AppCategoryManager.sCategoryList) {
                    AppCategoryManager.this.initAppCategoryList(context);
                }
            }
        }.start();
    }

    private void initAppCategoryList(Context context) {
        long startTime = System.currentTimeMillis();
        File dbFile = context.getDatabasePath("app_category.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        Scanner sc = new Scanner(new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.app_category))));
        while (sc.hasNextLine()) {
            String tempLine = sc.nextLine();
            String[] splits = tempLine.split("\\s+");
            try {
                sCategoryList.put(splits[0], Integer.valueOf(splits[1]));
            } catch (Exception e) {
                Log.d("com.miui.home.launcher.common.AppCategoryManager", "initAppCategoryList:" + tempLine, e);
            }
        }
        sc.close();
        Log.d("com.miui.home.launcher.common.AppCategoryManager", "init app category list using:" + ((System.currentTimeMillis() - startTime) / 1000));
    }
}
