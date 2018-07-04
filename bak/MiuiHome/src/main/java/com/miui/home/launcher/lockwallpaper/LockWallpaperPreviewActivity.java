package com.miui.home.launcher.lockwallpaper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.miui.home.R;
import com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo;
import java.util.ArrayList;
import java.util.List;
import miui.content.res.ThemeResources;

public class LockWallpaperPreviewActivity extends Activity {
    public static final boolean DEBUG = Log.isLoggable("LockWallpaperPreview", 3);
    private static int REQUEST_DIALOG = 1;
    private Gson mGson;
    private LockWallpaperPreviewView mMainView;
    long mShowTime;
    boolean mShowingDialog;
    List<WallpaperInfo> mWallpaperInfos = new ArrayList();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockwallpaper_activity);
        getWindow().getDecorView().setSystemUiVisibility(1792);
        this.mGson = new Gson();
        Intent intent = getIntent();
        this.mShowTime = intent.getLongExtra("showTime", 0);
        String currentWallpaperInfo = intent.getStringExtra("currentWallpaperInfo");
        String wallpaperInfos = intent.getStringExtra("wallpaperInfos");
        String adWallpaperInfos = intent.getStringExtra("adWallpaperInfos");
        final String dialogComponent = intent.getStringExtra("dialogComponent");
        if (DEBUG) {
            Log.d("LockWallpaperPreview", this.mShowTime + " time");
            Log.d("LockWallpaperPreview", "current: " + currentWallpaperInfo);
            Log.d("LockWallpaperPreview", "wallpapers: " + wallpaperInfos);
            Log.d("LockWallpaperPreview", "ads: " + adWallpaperInfos);
            Log.d("LockWallpaperPreview", "dialogComponent: " + dialogComponent);
        }
        WallpaperInfo current = (WallpaperInfo) this.mGson.fromJson(currentWallpaperInfo, WallpaperInfo.class);
        if (current == null) {
            current = new WallpaperInfo();
        }
        List<WallpaperInfo> wallpapers = (List) this.mGson.fromJson(wallpaperInfos, new TypeToken<List<WallpaperInfo>>() {
        }.getType());
        List<WallpaperInfo> ads = (List) this.mGson.fromJson(adWallpaperInfos, new TypeToken<List<WallpaperInfo>>() {
        }.getType());
        this.mWallpaperInfos.clear();
        this.mWallpaperInfos.add(current);
        if (wallpapers != null) {
            this.mWallpaperInfos.addAll(wallpapers);
        }
        if (ads != null) {
            for (int i = 0; i < ads.size(); i++) {
                WallpaperInfo info = (WallpaperInfo) ads.get(i);
                int position = info.pos;
                if (position <= 0 || position > this.mWallpaperInfos.size()) {
                    position = this.mWallpaperInfos.size();
                }
                this.mWallpaperInfos.add(position, info);
            }
        }
        this.mMainView = (LockWallpaperPreviewView) findViewById(R.id.view_pager);
        this.mMainView.setAdapter(new LockWallpaperPreviewAdapter(getApplicationContext(), this.mWallpaperInfos));
        this.mMainView.postDelayed(new Runnable() {
            public void run() {
                LockWallpaperPreviewActivity.this.getWindow().addFlags(524288);
                if (TextUtils.isEmpty(dialogComponent)) {
                    LockWallpaperPreviewActivity.this.mMainView.showHint();
                    return;
                }
                ComponentName component = ComponentName.unflattenFromString(dialogComponent);
                if (component != null) {
                    Intent intent = new Intent();
                    intent.setComponent(component);
                    intent.putExtra("StartActivityWhenLocked", true);
                    try {
                        LockWallpaperPreviewActivity.this.startActivityForResult(intent, LockWallpaperPreviewActivity.REQUEST_DIALOG);
                        LockWallpaperPreviewActivity.this.overridePendingTransition(R.anim.fade_in_dialog, 0);
                        LockWallpaperPreviewActivity.this.mShowingDialog = true;
                    } catch (Exception ex) {
                        Log.e("LockWallpaperPreview", "start activity failed.", ex);
                    }
                }
            }
        }, this.mShowTime - System.currentTimeMillis());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DIALOG) {
            this.mMainView.showHint();
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mShowingDialog) {
            this.mShowingDialog = false;
        } else {
            getWindow().clearFlags(524288);
        }
    }

    protected void onResume() {
        super.onResume();
        this.mMainView.postDelayed(new Runnable() {
            public void run() {
                LockWallpaperPreviewActivity.this.getWindow().addFlags(524288);
            }
        }, this.mShowTime - System.currentTimeMillis());
    }

    protected void onDestroy() {
        ThemeResources.clearLockWallpaperCache();
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
