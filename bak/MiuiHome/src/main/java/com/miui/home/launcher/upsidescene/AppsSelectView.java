package com.miui.home.launcher.upsidescene;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.Application;
import com.miui.home.launcher.IconLoader;
import com.miui.home.launcher.IconsSelectView;
import com.miui.home.launcher.IconsSelectView.MyAdapter;
import com.miui.home.launcher.ShortcutInfo;
import java.util.ArrayList;
import java.util.List;

public class AppsSelectView extends IconsSelectView {
    private ComponentName[] mComponents;
    private IconLoader mIconLoader;
    private SceneScreen mSceneScreen;

    public AppsSelectView(Context context, SceneScreen owner, ComponentName[] components, boolean multiSelect) {
        super(context, multiSelect);
        this.mSceneScreen = owner;
        this.mIconLoader = Application.getLauncherApplication(context).getIconLoader();
        this.mComponents = components;
        init();
    }

    private void init() {
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> installedApps = pm.queryIntentActivities(mainIntent, 0);
        List<ShortcutInfo> installed = new ArrayList();
        for (ResolveInfo resolveInfo : installedApps) {
            installed.add(getShortcutInfo(pm, this.mIconLoader, (ResolveInfo) i$.next()));
        }
        MyAdapter adapter = new MyAdapter(installed);
        this.mAppsGrid.setAdapter(adapter);
        this.mAppsGrid.setOnItemClickListener(adapter);
        if (this.mComponents != null) {
            ComponentName[] names = this.mComponents;
            for (int i = 0; i < this.mAppsGrid.getCount(); i++) {
                ShortcutInfo info = (ShortcutInfo) this.mAppsGrid.getItemAtPosition(i);
                for (ComponentName name : names) {
                    if (info.intent.getComponent().equals(name)) {
                        this.mAppsGrid.setItemChecked(i, true);
                        break;
                    }
                }
            }
        }
        updateTitle();
    }

    protected View getItemView(int position, View convertView, ViewGroup parent, Object obj) {
        View app;
        ShortcutInfo shortcut = (ShortcutInfo) obj;
        if (convertView != null) {
            app = convertView;
        } else {
            app = LayoutInflater.from(getContext()).inflate(R.layout.free_style_apps_application, null, false);
        }
        TextView label = (TextView) app.findViewById(R.id.title);
        label.setText(shortcut.getTitle(this.mContext));
        label.setCompoundDrawablesWithIntrinsicBounds(null, shortcut.getIcon(this.mContext, this.mIconLoader, shortcut.getIcon()), null, null);
        setSelected((ViewGroup) app, this.mAppsGrid.isItemChecked(position));
        app.setTag(shortcut);
        return app;
    }

    public void ok() {
        ArrayList<ComponentName> names;
        if (canMultiSelect()) {
            SparseBooleanArray selected = this.mAppsGrid.getCheckedItemPositions();
            if (selected.size() > 0) {
                names = new ArrayList();
                for (int i = 0; i < selected.size(); i++) {
                    if (selected.valueAt(i)) {
                        names.add(((ShortcutInfo) this.mAppsGrid.getItemAtPosition(selected.keyAt(i))).intent.getComponent());
                    }
                }
                this.mSceneScreen.onSelectApps(names, canMultiSelect());
            }
        } else {
            names = new ArrayList();
            names.add(((ShortcutInfo) this.mSelectedObject).intent.getComponent());
            this.mSceneScreen.onSelectApps(names, canMultiSelect());
        }
        this.mSceneScreen.closeSelectApps(false);
    }

    public void cancel() {
        this.mSceneScreen.closeSelectApps(true);
    }

    protected void updateTitle() {
        this.mTitle.setText(this.mContext.getText(R.string.free_style_apps_title) + " (" + this.mAppsGrid.getCheckedItemCount() + ")");
    }

    public ShortcutInfo getShortcutInfo(PackageManager manager, IconLoader iconLoader, ResolveInfo resolveInfo) {
        ShortcutInfo info = new ShortcutInfo();
        String packageName = resolveInfo.activityInfo.packageName;
        String className = resolveInfo.activityInfo.name;
        ComponentName cn = null;
        if (!(TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className))) {
            cn = new ComponentName(packageName, className);
        }
        info.intent = Intent.makeMainActivity(cn);
        Drawable icon = iconLoader.getIcon(cn, resolveInfo);
        if (icon == null) {
            icon = iconLoader.getDefaultIcon();
            info.usingFallbackIcon = true;
        }
        info.setIcon(icon);
        info.setTitle(resolveInfo.activityInfo.loadLabel(manager), this.mContext);
        if (info.getTitle(this.mContext) == null) {
            info.setTitle(cn.getClassName(), this.mContext);
        }
        info.itemType = 0;
        return info;
    }
}
