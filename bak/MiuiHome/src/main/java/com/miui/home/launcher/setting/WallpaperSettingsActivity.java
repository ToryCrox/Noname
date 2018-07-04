package com.miui.home.launcher.setting;

import android.content.ComponentName;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils;
import java.util.ArrayList;
import java.util.Iterator;
import miui.os.Build;
import miui.preference.PreferenceActivity;
import miui.preference.ValuePreference;

public class WallpaperSettingsActivity extends PreferenceActivity {
    private CurrentWallpaperAdapter mAdapter;
    private ImageListPreference mCurrentWallpaperGrid;
    private PreferenceCategory mWallpaperProviders;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wallpaper_settings);
        this.mCurrentWallpaperGrid = (ImageListPreference) findPreference("current_wallpaper");
        this.mAdapter = new CurrentWallpaperAdapter(this);
        this.mCurrentWallpaperGrid.setOnItemClickListener(this.mAdapter);
        this.mCurrentWallpaperGrid.setAdapter(this.mAdapter);
        this.mWallpaperProviders = (PreferenceCategory) findPreference("wallpaper_changer");
        loadWallpaperChangerList();
    }

    private void loadWallpaperChangerList() {
        if (Build.IS_TABLET) {
            removeProviderList();
        } else {
            new AsyncTask<Void, Void, ArrayList<Preference>>() {
                protected ArrayList<Preference> doInBackground(Void... params) {
                    ArrayList<Preference> items = new ArrayList();
                    for (ResolveInfo info : WallpaperSettingsActivity.this.getPackageManager().queryIntentContentProviders(new Intent("miui.intent.action.LOCKWALLPAPER_PROVIDER"), 0)) {
                        String authority = info.providerInfo.authority;
                        IContentProvider provider = WallpaperSettingsActivity.this.getContentResolver().acquireUnstableProvider(Uri.parse("content://" + authority));
                        if (provider != null) {
                            if (WallpaperSettingsActivity.this.isProviderEnabled(provider)) {
                                try {
                                    ComponentName cn = WallpaperSettingsActivity.this.getSettingsComponent(provider);
                                    if (cn == null) {
                                        WallpaperSettingsActivity.this.getContentResolver().releaseProvider(provider);
                                    } else {
                                        WallpaperSettingsActivity.this.getContentResolver().releaseProvider(provider);
                                        Intent target = new Intent();
                                        target.setComponent(cn);
                                        ResolveInfo resolveInfo = WallpaperSettingsActivity.this.getPackageManager().resolveActivity(target, 64);
                                        if (resolveInfo != null) {
                                            ValuePreference item = new ValuePreference(WallpaperSettingsActivity.this);
                                            item.setTitle(resolveInfo.activityInfo.loadLabel(WallpaperSettingsActivity.this.getPackageManager()));
                                            item.setIntent(target);
                                            item.setKey(authority);
                                            item.setShowRightArrow(true);
                                            items.add(item);
                                        }
                                    }
                                } finally {
                                    WallpaperSettingsActivity.this.getContentResolver().releaseProvider(provider);
                                }
                            }
                        }
                    }
                    return items;
                }

                protected void onPostExecute(ArrayList<Preference> result) {
                    Iterator i$ = result.iterator();
                    while (i$.hasNext()) {
                        WallpaperSettingsActivity.this.mWallpaperProviders.addPreference((Preference) i$.next());
                    }
                    if (WallpaperSettingsActivity.this.mWallpaperProviders.getPreferenceCount() <= 0) {
                        WallpaperSettingsActivity.this.removeProviderList();
                    }
                }
            }.execute(new Void[0]);
        }
    }

    private void removeProviderList() {
        getPreferenceScreen().removePreference(this.mWallpaperProviders);
        this.mWallpaperProviders = null;
    }

    private boolean isProviderEnabled(IContentProvider provider) {
        try {
            return provider.call(getPackageName(), "enableProvideLockWallpaper", null, null).getBoolean("result_boolean");
        } catch (RemoteException e) {
            return false;
        }
    }

    private ComponentName getSettingsComponent(IContentProvider provider) {
        ComponentName componentName = null;
        try {
            String name = provider.call(getPackageName(), "getSettingsComponent", null, null).getString("result_string");
            if (!TextUtils.isEmpty(name)) {
                componentName = ComponentName.unflattenFromString(name);
            }
        } catch (RemoteException e) {
        }
        return componentName;
    }

    protected void onStart() {
        super.onStart();
        if (this.mWallpaperProviders != null) {
            this.mWallpaperProviders.setEnabled(WallpaperUtils.isDefaultLockStyle());
        }
    }

    protected void onResume() {
        super.onResume();
        this.mCurrentWallpaperGrid.setAdapter(this.mAdapter);
    }
}
