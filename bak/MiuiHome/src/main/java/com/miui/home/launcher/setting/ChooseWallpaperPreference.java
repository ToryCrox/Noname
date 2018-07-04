package com.miui.home.launcher.setting;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.text.TextUtils;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.AsyncTaskExecutorHelper;
import com.miui.home.launcher.common.PermissionUtils;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import miui.os.Build;
import miui.preference.PreferenceActivity;

public class ChooseWallpaperPreference extends PreferenceActivity {
    public static String sWallpaperSettingsFlag;
    private String mLastPickerClassName = null;
    private LocalWallpaperAdapter mLocalWallpaperAdapter;
    private ImageListPreference mLocalWallpaperList;
    private String mOnLinePickName;
    private Preference mOnlineWallpaper;
    private final ArrayList<ResolveInfo> mPickList = new ArrayList();
    private OnPreferenceClickListener mPickerClickListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            if (preference.getIntent() == null) {
                return false;
            }
            ChooseWallpaperPreference.this.mLastPickerClassName = preference.getIntent().getComponent().getClassName();
            ChooseWallpaperPreference.this.startWallpaper(preference.getIntent());
            return true;
        }
    };
    private PreferenceCategory mWallpaperPickers;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.requestAccessStoragePermissions(this);
        sWallpaperSettingsFlag = getIntent().getExtras().getString("wallpaper_setting_type", "both_wallpaper");
        addPreferencesFromResource(R.xml.choose_wallpaper);
        this.mOnlineWallpaper = findPreference("online");
        String onLineTitle = getResources().getString(R.string.online_wallpaper);
        Intent onlinePickerIntent = WallpaperUtils.getThemeManagerWallpaperPickerIntent(this, onLineTitle, false);
        if (onlinePickerIntent == null) {
            ((PreferenceGroup) findPreference("preset_pick")).removePreference(this.mOnlineWallpaper);
        } else {
            this.mOnlineWallpaper.setIntent(Utilities.generateShowFragmentIntent(onlinePickerIntent, onLineTitle));
            this.mOnLinePickName = onlinePickerIntent.getComponent().getClassName();
            this.mOnlineWallpaper.setOnPreferenceClickListener(this.mPickerClickListener);
        }
        this.mLocalWallpaperList = (ImageListPreference) findPreference("wallpaper_grid");
        this.mLocalWallpaperAdapter = new LocalWallpaperAdapter(this);
        this.mLocalWallpaperList.setOnItemClickListener(this.mLocalWallpaperAdapter);
        this.mLocalWallpaperList.enableSeletMode(true);
        this.mWallpaperPickers = (PreferenceCategory) findPreference("wallpaper_picker");
        loadPickerPreferences();
        this.mLocalWallpaperList.setAdapter(this.mLocalWallpaperAdapter);
    }

    private void loadPickerPreferences() {
        if (!(Build.IS_TABLET || Build.IS_INTERNATIONAL_BUILD)) {
            Preference miWallpaperPicker = new Preference(this);
            Intent intent = WallpaperUtils.getMIWallpaperPickerIntent(this);
            if (intent != null) {
                miWallpaperPicker.setIntent(Utilities.generateShowFragmentIntent(intent, getResources().getString(R.string.mi_wallpaper)));
                miWallpaperPicker.setTitle(getResources().getString(R.string.mi_wallpaper));
                this.mWallpaperPickers.addPreference(miWallpaperPicker);
                miWallpaperPicker.setOnPreferenceClickListener(this.mPickerClickListener);
            }
        }
        WallpaperUtils.loadImagePickerList(this, this.mPickList);
        for (int i = 0; i < this.mPickList.size(); i++) {
            Preference item = new Preference(this);
            ResolveInfo info = (ResolveInfo) this.mPickList.get(i);
            item.setTitle(info.activityInfo.loadLabel(getPackageManager()));
            item.setIntent(WallpaperUtils.getIntent(info));
            this.mWallpaperPickers.addPreference(item);
            item.setOnPreferenceClickListener(this.mPickerClickListener);
        }
    }

    private void startWallpaper(Intent intent) {
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == -1) {
            WallpaperUtils.setWallpaperFromCustom(this, data);
        }
    }

    protected void onStart() {
        super.onStart();
        if (!(this.mLastPickerClassName == null || TextUtils.isEmpty(this.mOnLinePickName) || !this.mOnLinePickName.equals(this.mLastPickerClassName))) {
            this.mLocalWallpaperAdapter.refreshList();
            this.mLocalWallpaperList.update();
        }
        this.mLastPickerClassName = null;
    }

    protected void onDestroy() {
        AsyncTaskExecutorHelper.clearExcutorQueue();
        super.onDestroy();
    }
}
