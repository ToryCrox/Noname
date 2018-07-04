package com.miui.home.launcher;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import com.market.sdk.AppstoreAppInfo;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.setting.PortableUtils;
import java.net.URISyntaxException;
import miui.app.ToggleManager;
import miui.content.res.IconCustomizer;
import miui.maml.FancyDrawable;
import miui.maml.util.AppIconsHelper;

public class ShortcutInfo extends ItemInfo {
    private static Drawable sDefaultIcon;
    public String appProgressServer;
    ShortcutIconResource iconResource;
    public Intent intent;
    private AppstoreAppInfo mAppInfo;
    private ViewGroup mBuddyForParent;
    private ShortcutIcon mBuddyIconView;
    ValueAnimator mChangeProgressAnimator;
    public boolean mDisableByBackup;
    private boolean mHideApplicationMessage;
    private Drawable mIcon;
    private Bitmap mIconBitmap;
    private String mIconPackage;
    public int mIconType;
    private Boolean mIsFancyIcon;
    private String mMessageText;
    private boolean mShowDefaultIcon;
    private String mTextBg;
    private byte[] mTile;
    boolean onExternalStorage;
    public int progressPercent;
    public int progressStatus;
    public String progressTitle;
    private CharSequence title;
    public boolean usingFallbackIcon;

    public ShortcutInfo() {
        this.progressStatus = -5;
        this.progressPercent = 0;
        this.mShowDefaultIcon = false;
        this.mChangeProgressAnimator = null;
        this.mMessageText = null;
        this.mTextBg = null;
        this.mTile = null;
        this.itemType = 1;
    }

    public void load(Context context, Cursor c) {
        super.load(context, c);
        try {
            String intentStr = c.getString(1);
            if (intentStr != null) {
                this.intent = Intent.parseUri(intentStr, 0);
                this.intent.putExtra("profile", getUser());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        this.mIconPackage = c.getString(5);
        if (this.title == null) {
            this.title = loadTitle(c.getString(2));
        }
    }

    public ShortcutInfo(Context context, ResolveInfo info, UserHandle user) {
        this.progressStatus = -5;
        this.progressPercent = 0;
        this.mShowDefaultIcon = false;
        this.mChangeProgressAnimator = null;
        this.mMessageText = null;
        this.mTextBg = null;
        this.mTile = null;
        ComponentName componentName = new ComponentName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        this.itemType = 0;
        this.spanY = 1;
        this.spanX = 1;
        this.container = -1;
        setActivity(componentName, 270532608, user);
        this.title = info.activityInfo.loadLabel(context.getPackageManager());
        this.title = loadTitle(this.title.toString());
    }

    public void setUser(UserHandle u) {
        super.setUser(u);
        if (this.intent != null) {
            this.intent.putExtra("profile", u);
        }
    }

    private String loadTitle(String title) {
        return TextUtils.isEmpty(title) ? title : title.trim();
    }

    public CharSequence getTitle(Context context) {
        if (context == null) {
            return this.title;
        }
        CharSequence result = LauncherModel.loadTitle(context, this.title);
        if (result == null) {
            return this.title;
        }
        return loadTitle(result.toString());
    }

    public void setTitle(CharSequence title, Context context) {
        this.title = title;
        if (this.id != -1) {
            LauncherModel.updateTitleInDatabase(context, this.id, title);
        }
    }

    public void onLaunch(Launcher launcher) {
        if (this.itemFlags == 4) {
            this.itemFlags = 0;
            LauncherModel.updateItemInDatabase(launcher, this);
            launcher.removeFromNewInstalledList(this);
            launcher.removeShortcutInfoFromDelayNotificatoinList(this);
            cancelNotificationOfNewInstalledApp(launcher.getApplicationContext());
            if (this.mBuddyIconView != null) {
                this.mBuddyIconView.updateTitleMaxWidth();
            }
        }
    }

    public void setMessage(String text, String textBg, byte[] tile) {
        this.mMessageText = text;
        this.mTextBg = textBg;
        this.mTile = tile;
        if (this.mBuddyIconView != null && !"com.miui.backup:drawable/in_progress".equals(this.mTextBg)) {
            this.mBuddyIconView.setMessage(this.mMessageText, this.mTextBg, this.mTile);
        }
    }

    public String getMessageText() {
        return this.mMessageText;
    }

    public boolean isEmptyMessage() {
        return (this.mMessageText == null || this.mMessageText.length() == 0) && this.mTextBg == null;
    }

    public void setIconPackage(String packageName) {
        this.mIconPackage = packageName;
    }

    public String getIconPackage() {
        return this.mIconPackage;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    public Drawable getIcon() {
        return this.mIcon;
    }

    public boolean needShowProgress() {
        return this.progressStatus == -1 || this.progressStatus == -3 || this.progressStatus == -4 || (this.progressStatus >= 0 && this.progressStatus <= 100);
    }

    public void updateStatus(final Launcher launcher, int status, String title, Uri uri) {
        if (needShowProgress()) {
            if (status >= 0 && this.progressPercent != status) {
                final int originalPercent = this.progressPercent;
                final int deltaPercent = status - originalPercent;
                if (this.mChangeProgressAnimator == null) {
                    this.mChangeProgressAnimator = new ValueAnimator();
                    this.mChangeProgressAnimator.setInterpolator(new LinearInterpolator());
                    this.mChangeProgressAnimator.setFloatValues(new float[]{0.0f, 1.0f});
                } else {
                    this.mChangeProgressAnimator.removeAllUpdateListeners();
                    this.mChangeProgressAnimator.cancel();
                }
                this.mChangeProgressAnimator.setDuration(550);
                this.mChangeProgressAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = ((Float) animation.getAnimatedValue()).floatValue();
                        ShortcutInfo.this.progressPercent = (int) (((float) originalPercent) + (((float) deltaPercent) * value));
                        ShortcutInfo.this.updateProgressIcon(launcher);
                    }
                });
                this.mChangeProgressAnimator.start();
            } else if (status == -4 || status == -5) {
                this.progressPercent = 100;
            }
        }
        if (!(this.progressStatus == status && (title == null || title.equals(this.progressTitle)))) {
            this.progressTitle = title;
            this.progressStatus = status;
            updateProgressIcon(launcher);
        }
        if (uri == null) {
            return;
        }
        if (this.mAppInfo.iconUri == null || !this.mAppInfo.iconUri.equals(uri)) {
            this.mAppInfo.iconUri = uri;
            this.mIcon = null;
            LauncherModel.updateItemInDatabase(launcher, this);
            FolderInfo folderInfo = launcher.getParentFolderInfo(this);
            if (folderInfo != null) {
                folderInfo.notifyDataSetChanged();
            } else if (this.mBuddyIconView != null) {
                this.mBuddyIconView.updateInfo(launcher, this);
            }
        }
    }

    private void updateProgressIcon(Launcher launcher) {
        FolderInfo folderInfo = launcher.getParentFolderInfo(this);
        if (this.mBuddyIconView != null) {
            if (this.screenId == launcher.getWorkspace().getCurrentScreenId() || this.container == -101 || (folderInfo != null && launcher.getFolderCling().getFolderId() == folderInfo.id)) {
                this.mBuddyIconView.onProgressStatusChanged();
            }
        } else if (folderInfo == null) {
        } else {
            if (folderInfo.opened) {
                if (!launcher.isFolderAnimating()) {
                    folderInfo.notifyDataSetChanged();
                }
            } else if (folderInfo.screenId == launcher.getWorkspace().getCurrentScreenId() && folderInfo.getBuddyIconView() != null) {
                folderInfo.getBuddyIconView().invalidatePreviews();
            }
        }
    }

    private Drawable getIconFromUri(Context context, IconLoader iconLoader) {
        Bitmap source = null;
        if (!(this.mAppInfo == null || this.mAppInfo.iconUri == null)) {
            source = Utilities.getBitmapFromUri(context, this.mAppInfo.iconUri);
        }
        if (source == null) {
            this.mShowDefaultIcon = true;
            return getDefaultProgressIcon(context);
        } else if (Launcher.isDefaultThemeApplied()) {
            return new BitmapDrawable(context.getResources(), source);
        } else {
            return IconCustomizer.generateIconStyleDrawable(new BitmapDrawable(context.getResources(), source), true);
        }
    }

    public Drawable getDefaultProgressIcon(Context context) {
        if (sDefaultIcon != null) {
            sDefaultIcon.mutate();
            return sDefaultIcon;
        }
        sDefaultIcon = IconCustomizer.generateIconStyleDrawable(new BitmapDrawable(context.getResources(), ((BitmapDrawable) context.getResources().getDrawable(R.drawable.default_downloading_icon)).getBitmap()), true);
        if (sDefaultIcon instanceof BitmapDrawable) {
            this.mIconBitmap = ((BitmapDrawable) sDefaultIcon).getBitmap();
        }
        sDefaultIcon.mutate();
        return sDefaultIcon;
    }

    public Drawable getIcon(Context context, IconLoader iconLoader, Drawable src) {
        if (this.mIconType == 3) {
            this.mIcon = getToggleIcon(src, context, getToggleId());
            wrapIconWithBorder(context);
            return this.mIcon;
        }
        Drawable normalIcon = null;
        if (this.itemType == 11 || this.itemType == 13) {
            if (this.mIcon == null) {
                normalIcon = getIconFromUri(context, iconLoader);
                this.mIcon = normalIcon;
            }
        } else if (this.mIsFancyIcon == null || this.mIcon == null) {
            Drawable fancyIcon = loadFancyIcon(context);
            this.mIsFancyIcon = Boolean.valueOf(fancyIcon != null);
            if (this.mIsFancyIcon.booleanValue()) {
                this.mIcon = fancyIcon;
                normalIcon = iconLoader.getIcon(this.intent, this.itemType);
            } else if (this.mIcon == null) {
                normalIcon = PortableUtils.getUserBadgedIcon(context, iconLoader.getIcon(this.intent, this.itemType), getUser());
                if (normalIcon != null) {
                    this.mIcon = normalIcon;
                }
            }
        }
        if (normalIcon instanceof BitmapDrawable) {
            this.mIconBitmap = ((BitmapDrawable) normalIcon).getBitmap();
        } else if (this.mIcon instanceof BitmapDrawable) {
            this.mIconBitmap = ((BitmapDrawable) this.mIcon).getBitmap();
        }
        return this.mIcon;
    }

    private Drawable loadFancyIcon(Context context) {
        Drawable fancyIcon = AppIconsHelper.getIconDrawable(context, getPackageName(), getClassName(), 3600000);
        if (!(fancyIcon instanceof FancyDrawable)) {
            return null;
        }
        this.mHideApplicationMessage = Boolean.parseBoolean(((FancyDrawable) fancyIcon).getRoot().getRawAttr("hideApplicationMessage"));
        return fancyIcon;
    }

    public Bitmap getIconBitmap() {
        return this.mIconBitmap;
    }

    public void setIconBitmap(Bitmap bitmap) {
        this.mIconBitmap = bitmap;
    }

    public void setAppInfo(AppstoreAppInfo info) {
        this.mAppInfo = info;
    }

    public AppstoreAppInfo getAppInfo() {
        if (this.mAppInfo == null) {
            this.mAppInfo = new AppstoreAppInfo();
        }
        return this.mAppInfo;
    }

    public String getPackageName() {
        if (isPresetApp()) {
            return this.mIconPackage;
        }
        if (this.intent.getComponent() != null) {
            ComponentName cn = this.intent.getComponent();
            return cn == null ? this.mIconPackage : cn.getPackageName();
        } else if (this.itemType == 11) {
            return getAppInfo().pkgName;
        } else {
            return null;
        }
    }

    public String getClassName() {
        ComponentName cn = this.intent.getComponent();
        return cn == null ? null : cn.getClassName();
    }

    public boolean isPresetApp() {
        return super.isPresetApp() && this.intent.getData() != null;
    }

    public void wrapIconWithBorder(Context context) {
        if (this.mIcon != null) {
            this.mIcon = IconCustomizer.generateIconStyleDrawable(this.mIcon, false);
        }
    }

    public final void setActivity(ComponentName className, int launchFlags, UserHandle user) {
        this.intent = new Intent("android.intent.action.MAIN");
        this.intent.addCategory("android.intent.category.LAUNCHER");
        this.intent.setComponent(className);
        this.intent.setFlags(launchFlags);
        this.intent.putExtra("profile", user);
        this.itemType = 0;
        updateUser(this.intent);
    }

    public void onAddToDatabase(Context context, ContentValues values) {
        String titleStr;
        String uri;
        super.onAddToDatabase(context, values);
        if (this.title != null) {
            titleStr = this.title.toString();
        } else {
            titleStr = null;
        }
        values.put("title", titleStr);
        if (this.intent != null) {
            uri = this.intent.toUri(0);
        } else {
            uri = null;
        }
        values.put("intent", uri);
        values.put("iconType", Integer.valueOf(this.mIconType));
        if (this.itemType == 1 && !TextUtils.isEmpty(this.mIconPackage)) {
            values.put("iconPackage", this.mIconPackage);
        }
        if (1 == this.mIconType) {
            ItemInfo.writeBitmap(values, this.mIconBitmap);
        } else if (this.mIconType == 0) {
            if (this.onExternalStorage && !this.usingFallbackIcon) {
                ItemInfo.writeBitmap(values, this.mIconBitmap);
            }
            if (this.iconResource != null) {
                this.mIconPackage = this.iconResource.packageName;
                values.put("iconPackage", this.mIconPackage);
                values.put("iconResource", this.iconResource.resourceName);
            } else {
                values.put("iconResource", "");
            }
        } else if (4 == this.mIconType && this.mAppInfo.iconUri != null) {
            values.put("iconResource", this.mAppInfo.iconUri.toString());
        }
        if (this.itemType != 0 && !ProgressManager.isProgressType(this)) {
            return;
        }
        if (this.intent == null || this.intent.getComponent() == null) {
            Log.e("ShortcutInfo", "Application shortcut's intent or component is null");
        } else {
            values.put("iconPackage", this.intent.getComponent().getPackageName());
        }
    }

    public void loadToggleInfo(Context context) {
        this.title = context.getText(ToggleManager.getName(getToggleId()));
    }

    public void loadSettingsInfo(Context context) {
        PackageManager manager = context.getPackageManager();
        ResolveInfo resolveInfo = manager.resolveActivity(this.intent, 0);
        if (resolveInfo != null) {
            ComponentName cn = this.intent.getComponent();
            this.title = resolveInfo.activityInfo.loadLabel(manager);
            try {
                Drawable content = manager.getDrawable(cn.getPackageName(), manager.getActivityInfo(cn, 0).icon, manager.getApplicationInfo(cn.getPackageName(), 0));
                if (content == null) {
                    content = new ColorDrawable();
                }
                Drawable bg = Utilities.loadToggleBackground(context);
                this.mIconBitmap = bg instanceof BitmapDrawable ? ((BitmapDrawable) bg).getBitmap() : null;
                this.mIcon = new ToggleDrawable(bg, content);
            } catch (NameNotFoundException e) {
            }
        }
    }

    Drawable getToggleIcon(Drawable src, Context context, int toggleId) {
        Drawable toggle = ToggleManager.getImageDrawable(toggleId, context);
        ToggleManager.initDrawable(toggleId, toggle);
        if (src instanceof ToggleDrawable) {
            ((ToggleDrawable) src).changeToggleInfo(toggle);
            return src;
        }
        BitmapDrawable bgDrawable = (BitmapDrawable) Utilities.loadToggleBackground(context).getConstantState().newDrawable();
        this.mIconBitmap = bgDrawable.getBitmap();
        return new ToggleDrawable(bgDrawable, toggle);
    }

    public int getToggleId() {
        return this.intent.getIntExtra("ToggleId", -1);
    }

    public String toString() {
        return "ShortcutInfo(title=" + this.title + ")";
    }

    public void unbind() {
        super.unbind();
    }

    public void setBuddyIconView(ShortcutIcon icon, ViewGroup parent) {
        if (this.mBuddyIconView != null) {
            this.mBuddyIconView.stopLoading();
        }
        this.mBuddyIconView = icon;
        this.mBuddyForParent = parent;
        if (this.mBuddyIconView != null && !"com.miui.backup:drawable/in_progress".equals(this.mTextBg)) {
            this.mBuddyIconView.setMessage(this.mMessageText, this.mTextBg, this.mTile);
        }
    }

    public ShortcutIcon getBuddyIconView(ViewGroup parent) {
        if (parent == this.mBuddyForParent) {
            return this.mBuddyIconView;
        }
        return null;
    }

    public ShortcutIcon getBuddyIconView() {
        return this.mBuddyIconView;
    }

    public boolean showDefaultIcon() {
        return this.mShowDefaultIcon;
    }

    public void recycleIconRes() {
        setIcon(null);
        setIconBitmap(null);
        if (getBuddyIconView() != null) {
            getBuddyIconView().setIcon(null, null);
            setBuddyIconView(null, null);
        }
    }

    public boolean isLaunchDisabled() {
        return this.mDisableByBackup;
    }

    public boolean isHideApplicationMessage() {
        return this.mHideApplicationMessage;
    }

    public void cancelNotificationOfNewInstalledApp(Context context) {
        if (this.title != null) {
            ((NotificationManager) context.getSystemService("notification")).cancel(this.title.toString(), 0);
        }
    }
}
