package com.miui.home.launcher.gadget;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.miui.home.R;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Clock.ClockStyle;
import com.miui.home.launcher.gadget.ConfigableGadget.BackupManager;
import com.xiaomi.analytics.Actions;
import com.xiaomi.analytics.AdAction;
import com.xiaomi.analytics.Analytics;
import java.io.File;
import miui.maml.MamlConfigSettings;
import miui.maml.ScreenElementRoot.OnExternCommandListener;
import miui.maml.elements.ButtonScreenElement.ButtonActionListener;
import org.w3c.dom.Element;

public class ClockGadgetDelegate extends ConfigableGadget implements ButtonActionListener {
    private static final float DENSITY_SCALE = (((float) Resources.getSystem().getDisplayMetrics().densityDpi) / 240.0f);
    private static String sConfigKey = getConfigKey();
    private final Context mActivity;
    Gadget mActualGadget;
    final Clock mClock;
    private String mClockType;
    private OnExternCommandListener mCommandListener;
    private ImageView mDeleteView;
    private ImageView mEditView;
    private LinearLayout mEditViewContainer;
    private View mErrorDisplay;
    private boolean mIsInEditingModel = false;
    private String mMd5;
    private final int mRequestCode;
    private boolean mRestrictClick;
    int mStatus = 0;

    private static String getConfigKey() {
        if (Utilities.isStaging()) {
            return "miuihome_gadgetstaging";
        }
        return "miuihome_gadget";
    }

    public ClockGadgetDelegate(Context a, int requestCode) {
        super(a);
        this.mActivity = a;
        this.mClock = new Clock(a);
        this.mRequestCode = requestCode;
    }

    public void onCreate() {
        super.onCreate();
        this.mStatus |= 1;
        setupViews();
        this.mClock.init();
        if (this.mActualGadget == null) {
            updateActualGadget();
        } else {
            this.mActualGadget.onCreate();
        }
        new IntentFilter("android.intent.action.MEDIA_MOUNTED").addDataScheme("file");
    }

    public void onDestroy() {
        this.mStatus &= -2;
        this.mClock.pause();
        if (this.mActualGadget != null) {
            this.mActualGadget.onDestroy();
        }
    }

    public void onAdded() {
    }

    public void onDeleted() {
        if (this.mActualGadget != null) {
            new File(getMamlConfigPath(this.mBackupManager.getBackupPath(this.mActivity, getItemId()))).delete();
            this.mActualGadget.onDeleted();
        }
        super.onDeleted();
    }

    public void onPause() {
        this.mStatus &= -5;
        this.mClock.pause();
        if (this.mActualGadget != null) {
            this.mActualGadget.onPause();
        }
    }

    public void onResume() {
        super.onResume();
        this.mStatus |= 4;
        if (this.mActualGadget != null) {
            this.mActualGadget.onResume();
            this.mClock.resume();
            if (!TextUtils.isEmpty(this.mMd5)) {
                String ex = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("gadget_ex_" + this.mMd5, null);
                if (!TextUtils.isEmpty(ex)) {
                    Log.d("Launcher.ClockGadgetDelegate", sConfigKey + " VIEW:" + this.mMd5);
                    try {
                        AdAction action = Actions.newAdAction("VIEW");
                        action.addParam("ex", ex);
                        action.addParam("e", "VIEW");
                        Analytics.trackSystem(this.mContext, sConfigKey, action);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void onStart() {
        this.mStatus |= 2;
        if (this.mActualGadget != null) {
            this.mActualGadget.onStart();
            this.mClock.onStart();
        }
    }

    public void onStop() {
        this.mStatus &= -3;
        this.mClock.pause();
        if (this.mActualGadget != null) {
            this.mActualGadget.onStop();
            this.mClock.onStop();
        }
    }

    public void updateConfig(Bundle config) {
        saveConfig(config.getString("RESPONSE_PICKED_RESOURCE"));
        updateActualGadget();
    }

    public boolean saveConfig(String config) {
        if (Utilities.extract(this.mBackupManager.getBackupPath(this.mActivity, getItemId()), config, this.mBackupManager.getSystemGadgetTheme())) {
            return super.saveConfig(config);
        }
        return false;
    }

    private void setupViews() {
        inflate(this.mContext, R.layout.gadget_error_display, this);
        this.mErrorDisplay = findViewById(R.id.error_display);
        ((ImageView) this.mErrorDisplay.findViewById(R.id.gadget_icon)).setImageResource(R.drawable.gadget_clock_error);
        this.mErrorDisplay.setVisibility(8);
        this.mEditViewContainer = new LinearLayout(this.mContext);
        LayoutParams lp = new LayoutParams(-2, -2);
        lp.gravity = 53;
        addView(this.mEditViewContainer, lp);
        this.mEditViewContainer.setOrientation(1);
        this.mEditViewContainer.setVisibility(8);
        this.mEditView = new ImageView(this.mContext);
        this.mEditViewContainer.addView(this.mEditView, new LinearLayout.LayoutParams(-2, -2));
        this.mEditView.setImageResource(R.drawable.gadget_edit_tag);
        this.mEditView.setOnClickListener(this);
        this.mDeleteView = new ImageView(this.mContext);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-2, -2);
        llp.topMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.gadget_icon_margin);
        this.mEditViewContainer.addView(this.mDeleteView, llp);
        this.mDeleteView.setImageResource(R.drawable.gadget_delete_tag);
        this.mDeleteView.setVisibility(8);
        this.mDeleteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!TextUtils.isEmpty(ClockGadgetDelegate.this.mMd5)) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ClockGadgetDelegate.this.getContext());
                    String ex = sp.getString("gadget_ex_" + ClockGadgetDelegate.this.mMd5, null);
                    if (!TextUtils.isEmpty(ex)) {
                        Log.d("Launcher.ClockGadgetDelegate", ClockGadgetDelegate.sConfigKey + " DISLIKE:" + ClockGadgetDelegate.this.mMd5);
                        try {
                            AdAction action = Actions.newAdAction("DISLIKE");
                            action.addParam("ex", ex);
                            action.addParam("e", "DISLIKE");
                            Analytics.trackSystem(ClockGadgetDelegate.this.mContext, ClockGadgetDelegate.sConfigKey, action);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        sp.edit().remove("gadget_ex_" + ClockGadgetDelegate.this.mMd5).commit();
                    }
                }
                File dstFile = new File(GadgetFactory.getGadgetDir(ClockGadgetDelegate.this.getContext()), ClockGadgetDelegate.this.mBackupManager.getEntryName());
                if (dstFile.exists() && Utilities.getFileMd5(dstFile).equals(ClockGadgetDelegate.this.mMd5)) {
                    GadgetAutoChangeService.onGadgetDisable(ClockGadgetDelegate.this.getContext(), dstFile.getAbsolutePath());
                } else {
                    GadgetAutoChangeService.reloadGadget(ClockGadgetDelegate.this.getContext(), dstFile.getAbsolutePath());
                }
            }
        });
    }

    private static String getMamlConfigPath(String path) {
        return path + ".config";
    }

    void updateActualGadget() {
        View view = null;
        if (!this.mBackupManager.prepareBackup(this.mActivity, getItemId())) {
            Log.d("Launcher.ClockGadgetDelegate", "prepare back up failed");
        }
        String backup = this.mBackupManager.getBackupPath(this.mActivity, getItemId());
        Log.d("Launcher.ClockGadgetDelegate", "updateActualGadget backup: " + backup);
        Element root = Utilities.parseManifestInZip(backup);
        if (root != null) {
            String type = root.getAttribute("type");
            this.mClockType = type;
            if ("flip".equals(type)) {
                view = inflate(this.mContext, R.layout.gadget_flipclock, null);
            } else {
                view = new AwesomeClock(this.mContext) {
                    public boolean onInterceptTouchEvent(MotionEvent event) {
                        if (ClockGadgetDelegate.this.mRestrictClick) {
                            return false;
                        }
                        return true;
                    }
                };
                view.setOnClickListener(this);
                if (MamlConfigSettings.containsConfig(backup)) {
                    ((AwesomeClock) view).setMamlConfigPath(getMamlConfigPath(backup));
                    ((AwesomeClock) view).setConfigParas(getComponentCode(), backup);
                }
            }
            this.mErrorDisplay.setVisibility(8);
            if (view instanceof ClockStyle) {
                ((ClockStyle) view).initConfig(backup);
            }
        } else {
            this.mErrorDisplay.setVisibility(0);
        }
        Gadget old = this.mActualGadget;
        if (old != null) {
            Gadget oldGadget = old;
            if ((this.mStatus & 4) != 0) {
                oldGadget.onPause();
            }
            if ((this.mStatus & 2) != 0) {
                oldGadget.onStop();
            }
            if ((this.mStatus & 1) != 0) {
                oldGadget.onDestroy();
            }
            removeView(old);
        }
        if (view instanceof Gadget) {
            addView(view);
            view.setTag(getTag());
            if (new GadgetInfo(Uri.fromFile(new File(backup))).getDate("enableTime") != null) {
                this.mDeleteView.setVisibility(0);
            } else {
                this.mDeleteView.setVisibility(8);
            }
            adjustByAttributes(root, view);
            this.mEditViewContainer.bringToFront();
            Gadget newGadget = (Gadget) view;
            if ((this.mStatus & 1) != 0) {
                newGadget.onCreate();
            }
            if ((this.mStatus & 2) != 0) {
                newGadget.onStart();
            }
            if ((this.mStatus & 4) != 0) {
                newGadget.onResume();
            }
            if (newGadget instanceof ClockStyle) {
                this.mClock.setClockStyle((ClockStyle) newGadget);
            }
            this.mActualGadget = newGadget;
        } else {
            this.mClock.setClockStyle(null);
            this.mActualGadget = null;
        }
        if ("awesome".equals(this.mClockType) && (view instanceof AwesomeClock)) {
            this.mRestrictClick = ((AwesomeClock) view).setClockButtonListener(this);
            this.mMd5 = Utilities.getFileMd5(new File(backup));
            this.mCommandListener = new OnExternCommandListener() {
                public void onCommand(String command, Double numPara, final String strPara) {
                    if ("click".equals(command)) {
                        ClockGadgetDelegate.this.post(new Runnable() {
                            public void run() {
                                if (!TextUtils.isEmpty(ClockGadgetDelegate.this.mMd5)) {
                                    String ex = PreferenceManager.getDefaultSharedPreferences(ClockGadgetDelegate.this.getContext()).getString("gadget_ex_" + ClockGadgetDelegate.this.mMd5, null);
                                    if (!TextUtils.isEmpty(ex)) {
                                        Log.d("Launcher.ClockGadgetDelegate", ClockGadgetDelegate.sConfigKey + " CLICK:" + ClockGadgetDelegate.this.mMd5 + " btn:" + strPara);
                                        try {
                                            AdAction action = Actions.newAdAction("CLICK");
                                            action.addParam("ex", ex);
                                            action.addParam("e", "CLICK");
                                            action.addParam("btn", strPara);
                                            Analytics.trackSystem(ClockGadgetDelegate.this.mContext, ClockGadgetDelegate.sConfigKey, action);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            };
            ((AwesomeClock) view).setOnExternCommandListener(this.mCommandListener);
        }
    }

    private void adjustByAttributes(Element root, View view) {
        int clockX = getIntFromElement(root, "clock_x", 0);
        int clockY = getIntFromElement(root, "clock_y", 0);
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.leftMargin = (int) (DENSITY_SCALE * ((float) clockX));
        lp.topMargin = (int) (DENSITY_SCALE * ((float) clockY));
        view.setLayoutParams(lp);
    }

    private int getIntFromElement(Element element, String key, int dftValue) {
        int ret = dftValue;
        try {
            String str = element.getAttribute(key);
            if (str != null) {
                ret = Integer.valueOf(str).intValue();
            }
        } catch (NumberFormatException e) {
        }
        return ret;
    }

    public void onWallpaperColorChanged() {
        this.mEditView.setImageResource(WallpaperUtils.hasAppliedLightWallpaper() ? R.drawable.gadget_edit_tag_dark : R.drawable.gadget_edit_tag);
        this.mDeleteView.setImageResource(WallpaperUtils.hasAppliedLightWallpaper() ? R.drawable.gadget_delete_tag_dark : R.drawable.gadget_delete_tag);
        if (this.mActualGadget instanceof Gadget) {
            this.mActualGadget.onWallpaperColorChanged();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean isRestrictClick() {
        return this.mRestrictClick;
    }

    public void onClick(View v) {
        if (this.mConfigurable.booleanValue() || !this.mIsInEditingModel) {
            super.onClick(v);
            if ("flip".equals(this.mClockType) || !this.mRestrictClick || v == this.mEditView) {
                onButtonUp(null);
            }
        }
    }

    public View getEditView() {
        return this.mEditViewContainer;
    }

    public void onEditNormal() {
        if (Utilities.canPickTheme(this.mContext)) {
            super.onEditNormal();
        } else {
            this.mConfigurable = Boolean.valueOf(false);
        }
        this.mIsInEditingModel = true;
        if (this.mActualGadget != null) {
            this.mActualGadget.onEditNormal();
        }
    }

    public void onEditDisable() {
        super.onEditDisable();
        this.mIsInEditingModel = false;
        if (this.mActualGadget != null) {
            this.mActualGadget.onEditDisable();
        }
    }

    public static void updateBackup(Context context) {
        int i = 3;
        for (int gadgetId : new int[]{4, 5, 6}) {
            BackupManager backup = new BackupManager(gadgetId);
            String key = "clock_changed_time_" + backup.getSizeDescript();
            File dir = new File(backup.getBackupDir(context));
            if (dir.isDirectory()) {
                long lastModified = System.getLong(context.getContentResolver(), key, 0);
                String prefix = backup.getBackupNamePrefix();
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName();
                        if (name.startsWith(prefix)) {
                            boolean timeout = f.lastModified() < lastModified;
                            boolean autoChange = new GadgetInfo(Uri.fromFile(f)).getBoolean("autoChange");
                            File file = new File(backup.getPathInTheme());
                            boolean themeAutoChange = true;
                            if (file.exists()) {
                                themeAutoChange = new GadgetInfo(Uri.fromFile(file)).getBoolean("autoChange");
                            }
                            Log.d("Launcher.ClockGadgetDelegate", "name:" + name + " timeout:" + timeout + " autoChange:" + autoChange + " themeAutoChange:" + themeAutoChange);
                            if (timeout || (autoChange && themeAutoChange)) {
                                try {
                                    ConfigableGadget.deleteConfig(context, backup.getBackupName(Long.valueOf(name.substring(prefix.length())).longValue()));
                                    Log.d("Launcher.ClockGadgetDelegate", String.format("delete gadget config id=%d, path=%s", new Object[]{Long.valueOf(id), f.getAbsolutePath()}));
                                } catch (NumberFormatException e) {
                                }
                                f.delete();
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean onButtonUp(String name) {
        if (this.mEditViewContainer.getVisibility() == 0) {
            showPicker();
        } else {
            LauncherApplication.startActivity(getContext(), Utilities.getDeskClockTabActivityIntent(), this);
            if (!TextUtils.isEmpty(this.mMd5)) {
                String ex = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("gadget_ex_" + this.mMd5, null);
                if (!TextUtils.isEmpty(ex)) {
                    Log.d("Launcher.ClockGadgetDelegate", sConfigKey + " CLICK:" + this.mMd5 + " btn:default");
                    try {
                        AdAction action = Actions.newAdAction("CLICK");
                        action.addParam("ex", ex);
                        action.addParam("e", "CLICK");
                        action.addParam("btn", "default");
                        Analytics.trackSystem(this.mContext, sConfigKey, action);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return true;
    }

    private void showPicker() {
        Intent intent = new Intent("miui.intent.action.PICK_GADGET");
        intent.putExtra("REQUEST_GADGET_NAME", "clock");
        intent.putExtra("REQUEST_GADGET_SIZE", this.mBackupManager.getSizeDescript());
        intent.putExtra("REQUEST_CURRENT_USING_PATH", loadConfig());
        intent.putExtra("REQUEST_TRACK_ID", String.valueOf(getItemId()));
        intent.putExtra("REQUEST_ENTRY_TYPE", this.mActivity.getPackageName());
        LauncherApplication.startActivityForResult(getContext(), intent, this.mRequestCode);
    }

    private String getComponentCode() {
        return "clock_" + this.mBackupManager.getSizeDescript();
    }

    public boolean onButtonDown(String name) {
        return false;
    }

    public boolean onButtonDoubleClick(String name) {
        return false;
    }

    public boolean onButtonLongClick(String name) {
        return false;
    }
}
