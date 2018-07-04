package com.miui.home.launcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import miui.app.ToggleManager;
import miui.content.res.IconCustomizer;
import miui.os.Build;

public class WidgetThumbnailViewAdapter extends ThumbnailViewAdapter {
    private static int GADGET_OFFSET;
    private static int SETTING_SHORTCUT_OFFSET;
    private static int SHORTCUT_OFFSET;
    private static int WIDGET_OFFSET;
    private static final HashMap<String, Integer> sCategoryMaps = new HashMap();
    private static final Collection<String> sDisabledComponents = new ArrayList();
    private static final ArrayList<ShortcutProviderInfo> sShortcutProviders = new ArrayList();
    private AppWidgetManager mAppWidgetManager;
    private PackageManager mPackageManager;
    private int mScreenType = 0;
    private List<ResolveInfo> mSettingShortcutIntents;
    private List<AppWidgetProviderInfo> mWidgetList;
    private List<Integer> toggleIds;

    static {
        sShortcutProviders.add(new ShortcutProviderInfo("com.android.contacts", "com.android.contacts.ContactShortcut"));
        sDisabledComponents.add("com.android.alarmclock.AnalogAppWidgetProvider");
        sCategoryMaps.put("com.android.calendar", Integer.valueOf(6));
        sCategoryMaps.put("com.miui.notes", Integer.valueOf(7));
        sCategoryMaps.put("com.miui.player", Integer.valueOf(1));
        sCategoryMaps.put("com.android.calculator2", Integer.valueOf(8));
        sCategoryMaps.put("com.miui.weather2", Integer.valueOf(4));
    }

    public WidgetThumbnailViewAdapter(Context context) {
        int size;
        super(context);
        this.mPackageManager = context.getPackageManager();
        this.mAppWidgetManager = AppWidgetManager.getInstance(context);
        this.toggleIds = ToggleManager.getAllToggles(this.mContext);
        this.toggleIds.remove(Integer.valueOf(0));
        Intent targetIntent = new Intent("android.intent.action.MAIN", null);
        targetIntent.addCategory("com.android.settings.SHORTCUT");
        targetIntent.addFlags(268435456);
        this.mSettingShortcutIntents = this.mContext.getPackageManager().queryIntentActivities(targetIntent, 0);
        if (this.toggleIds != null) {
            size = this.toggleIds.size();
        } else {
            size = 0;
        }
        SETTING_SHORTCUT_OFFSET = size;
        int i = SETTING_SHORTCUT_OFFSET;
        if (this.mSettingShortcutIntents != null) {
            size = this.mSettingShortcutIntents.size();
        } else {
            size = 0;
        }
        GADGET_OFFSET = size + i;
        reloadWidgets(false);
    }

    public void setScreenType(int screenType) {
        int old = this.mScreenType;
        this.mScreenType = screenType;
        if (this.mScreenType != old) {
            reloadWidgets(false);
        }
    }

    public void reloadWidgets(boolean refresh) {
        int i = 0;
        this.mIsRefreshing = refresh;
        stopLoading();
        int i2;
        if (this.mScreenType == 1) {
            SETTING_SHORTCUT_OFFSET = this.toggleIds != null ? this.toggleIds.size() : 0;
            i2 = SETTING_SHORTCUT_OFFSET;
            if (this.mSettingShortcutIntents != null) {
                i = this.mSettingShortcutIntents.size();
            }
            GADGET_OFFSET = i2 + i;
            SHORTCUT_OFFSET = GADGET_OFFSET + GadgetFactory.getGadgetIdList(this.mContext, true).length;
            WIDGET_OFFSET = SHORTCUT_OFFSET + sShortcutProviders.size();
            this.mWidgetList = null;
        } else {
            if (this.mScreenType == 2) {
                SETTING_SHORTCUT_OFFSET = 0;
                GADGET_OFFSET = 0;
                SHORTCUT_OFFSET = GADGET_OFFSET + GadgetFactory.getGadgetIdList(this.mContext).length;
                WIDGET_OFFSET = SHORTCUT_OFFSET;
            } else {
                if (this.toggleIds != null) {
                    i2 = this.toggleIds.size();
                } else {
                    i2 = 0;
                }
                SETTING_SHORTCUT_OFFSET = i2;
                int i3 = SETTING_SHORTCUT_OFFSET;
                if (this.mSettingShortcutIntents != null) {
                    i2 = this.mSettingShortcutIntents.size();
                } else {
                    i2 = 0;
                }
                GADGET_OFFSET = i2 + i3;
                SHORTCUT_OFFSET = GADGET_OFFSET + GadgetFactory.getGadgetIdList(this.mContext, Build.IS_TABLET).length;
                WIDGET_OFFSET = SHORTCUT_OFFSET + sShortcutProviders.size();
            }
            this.mWidgetList = this.mAppWidgetManager.getInstalledProviders();
            int i4 = this.mWidgetList.size() - 1;
            while (i4 > 0) {
                AppWidgetProviderInfo info = (AppWidgetProviderInfo) this.mWidgetList.get(i4);
                if ("com.miui.player".equals(info.provider.getPackageName())) {
                    this.mWidgetList.remove(i4);
                    this.mWidgetList.add(0, info);
                } else if (info.minWidth <= 0 && info.minHeight <= 0) {
                    this.mWidgetList.remove(i4);
                    i4--;
                } else if (sDisabledComponents.contains(info.provider.getClassName())) {
                    this.mWidgetList.remove(i4);
                    i4--;
                }
                i4--;
            }
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return (this.mWidgetList != null ? this.mWidgetList.size() : 0) + WIDGET_OFFSET;
    }

    public View getItem(int position) {
        return null;
    }

    private void setResForToggle(ShortcutPlaceholderProviderInfo toggleInfo, int toggleId, ImageView icon, TextView title) {
        Drawable drawable = toggleInfo.getIcon(this.mContext, this.mLauncher.getIconLoader(), null);
        drawable.mutate();
        icon.setImageDrawable(drawable);
        title.setText(toggleInfo.getTitle(this.mContext));
    }

    private void setResForSettingsShortcut(ShortcutPlaceholderProviderInfo shortcutInfo, ResolveInfo providerInfo, ImageView icon, TextView title) {
        ComponentName cn = new ComponentName(providerInfo.activityInfo.packageName, providerInfo.activityInfo.name);
        Drawable iconDrawable = null;
        try {
            PackageManager pm = this.mContext.getPackageManager();
            int iconId = pm.getActivityInfo(cn, 0).icon;
            if (iconId != 0) {
                iconDrawable = pm.getDrawable(cn.getPackageName(), iconId, pm.getApplicationInfo(cn.getPackageName(), 0));
            }
        } catch (NameNotFoundException e) {
        }
        CharSequence titleName = providerInfo.activityInfo.loadLabel(this.mPackageManager);
        shortcutInfo.setTitle(titleName, this.mContext);
        if (iconDrawable == null) {
            iconDrawable = new ColorDrawable();
        }
        icon.setImageDrawable(IconCustomizer.generateIconStyleDrawable(new ToggleDrawable(Utilities.loadToggleBackground(this.mContext), iconDrawable)));
        title.setText(titleName);
    }

    public void loadContent(int pos) {
        AutoLayoutThumbnailItem resultView = this.mAllThumbnailViews[pos];
        ImageView icon = resultView.icon;
        TextView title = resultView.title;
        if (resultView.getTag() != null) {
            if (pos < SETTING_SHORTCUT_OFFSET) {
                if (pos != 0) {
                    setResForToggle((ShortcutPlaceholderProviderInfo) resultView.getTag(), ((Integer) this.toggleIds.get(pos)).intValue(), icon, title);
                }
            } else if (SETTING_SHORTCUT_OFFSET > pos || pos >= GADGET_OFFSET) {
                if (GADGET_OFFSET <= pos && pos < SHORTCUT_OFFSET) {
                    return;
                }
                if (SHORTCUT_OFFSET > pos || pos >= WIDGET_OFFSET) {
                    AppWidgetProviderInfo info = (AppWidgetProviderInfo) this.mWidgetList.get(pos - WIDGET_OFFSET);
                    Drawable drawable = null;
                    try {
                        ActivityInfo ai = this.mPackageManager.getReceiverInfo(info.provider, 0);
                        if (ai.applicationInfo.icon == info.icon) {
                            drawable = ai.loadIcon(this.mPackageManager);
                        }
                    } catch (NameNotFoundException e) {
                    }
                    if (drawable == null) {
                        drawable = this.mPackageManager.getDrawable(info.provider.getPackageName(), info.icon, null);
                    }
                    icon.setImageDrawable(drawable);
                    return;
                }
                try {
                    icon.setImageDrawable(this.mPackageManager.getActivityInfo(((ShortcutProviderInfo) resultView.getTag()).mComponentName, 0).loadIcon(this.mPackageManager));
                } catch (NameNotFoundException e2) {
                }
            } else if (pos != SETTING_SHORTCUT_OFFSET) {
                setResForSettingsShortcut((ShortcutPlaceholderProviderInfo) resultView.getTag(), (ResolveInfo) this.mSettingShortcutIntents.get(pos - SETTING_SHORTCUT_OFFSET), icon, title);
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) getThumbnailView(position);
        resultView.setLauncher(this.mLauncher);
        ImageView icon = resultView.icon;
        TextView title = resultView.title;
        resultView.setVisibility(0);
        resultView.setAlpha(1.0f);
        resultView.iconBackground.setVisibility(0);
        resultView.iconForeground.setVisibility(4);
        LayoutParams lp = resultView.icon.getLayoutParams();
        lp.width = this.mContext.getResources().getDimensionPixelSize(R.dimen.thumbnail_icon_width);
        lp.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.thumbnail_icon_height);
        resultView.icon.setVisibility(0);
        resultView.title.setVisibility(0);
        resultView.setSkipNextAutoLayoutAnimation(true);
        Intent intent;
        if (position < SETTING_SHORTCUT_OFFSET) {
            int toggleId = ((Integer) this.toggleIds.get(position)).intValue();
            ShortcutPlaceholderProviderInfo toggleInfo = new ShortcutPlaceholderProviderInfo(4);
            intent = new Intent("com.miui.action.TOGGLE_SHURTCUT");
            intent.putExtra("ToggleId", toggleId);
            toggleInfo.intent = intent;
            toggleInfo.loadToggleInfo(this.mContext);
            toggleInfo.mIconType = 3;
            resultView.setTag(toggleInfo);
            if (position == 0) {
                setResForToggle(toggleInfo, toggleId, icon, title);
            }
        } else if (SETTING_SHORTCUT_OFFSET <= position && position < GADGET_OFFSET) {
            ResolveInfo providerInfo = (ResolveInfo) this.mSettingShortcutIntents.get(position - SETTING_SHORTCUT_OFFSET);
            ShortcutPlaceholderProviderInfo shortcutInfo = new ShortcutPlaceholderProviderInfo(5);
            Intent shortcutIntent = new Intent("com.miui.action.SETTINGS_SHURTCUT", null);
            shortcutIntent.setClassName(providerInfo.activityInfo.packageName, providerInfo.activityInfo.name);
            intent = new Intent();
            intent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
            intent.putExtra("android.intent.extra.shortcut.NAME", providerInfo.activityInfo.loadLabel(this.mPackageManager));
            shortcutInfo.intent = intent;
            shortcutInfo.mIconType = 5;
            resultView.setTag(shortcutInfo);
            if (position == SETTING_SHORTCUT_OFFSET) {
                setResForSettingsShortcut(shortcutInfo, providerInfo, icon, title);
            }
        } else if (GADGET_OFFSET <= position && position < SHORTCUT_OFFSET) {
            GadgetInfo info = GadgetFactory.getInfo(GadgetFactory.getGadgetIdList(this.mContext)[position - GADGET_OFFSET]);
            resultView.setTag(info);
            icon.setImageDrawable(info.getIcon(this.mContext));
            title.setText(info.getTitle(this.mContext));
        } else if (SHORTCUT_OFFSET > position || position >= WIDGET_OFFSET) {
            AppWidgetProviderInfo info2 = (AppWidgetProviderInfo) this.mWidgetList.get(position - WIDGET_OFFSET);
            LauncherAppWidgetProviderInfo itemInfo = new LauncherAppWidgetProviderInfo(info2);
            setAppWidgetCategory(itemInfo);
            DeviceConfig.calcWidgetSpans(itemInfo);
            resultView.setTag(itemInfo);
            title.setText(info2.label);
        } else {
            ShortcutProviderInfo info3 = (ShortcutProviderInfo) sShortcutProviders.get(position - SHORTCUT_OFFSET);
            resultView.setTag(info3);
            try {
                Resources resource = this.mPackageManager.getResourcesForActivity(info3.mComponentName);
                ActivityInfo activityInfo = this.mPackageManager.getActivityInfo(info3.mComponentName, 0);
                title.setText(resource.getText(activityInfo.labelRes));
                icon.setImageDrawable(activityInfo.loadIcon(this.mPackageManager));
            } catch (NameNotFoundException e) {
            }
        }
        if (this.mIsRefreshing && position == getCount() - 1) {
            startLoading();
            this.mIsRefreshing = false;
        }
        adaptIconStyle(resultView);
        return resultView;
    }

    private void setAppWidgetCategory(LauncherAppWidgetProviderInfo info) {
        String packageName = info.providerInfo.provider.getPackageName();
        if (!TextUtils.isEmpty(packageName) && sCategoryMaps.containsKey(packageName)) {
            info.mWidgetCategory = ((Integer) sCategoryMaps.get(packageName)).intValue();
        }
    }
}
