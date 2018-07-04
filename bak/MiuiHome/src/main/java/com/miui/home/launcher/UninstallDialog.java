package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver.Stub;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.Launcher.IconContainer;
import com.miui.home.launcher.common.ParasiticDrawingFireworks;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import miui.securityspace.XSpaceUserHandle;

public class UninstallDialog extends FrameLayout {
    private final String TAG;
    private View mButtons;
    private int mCustomHeight;
    private TextView mDescription;
    private DragSource mDragSource;
    private DeleteIconContainer mIconContainer;
    private boolean mIsCnLanguage;
    private Launcher mLauncher;
    private int mLayoutHeight;
    private View mMessages;
    private IPackageManager mPackageManager;
    private RemoveItemsWorker mRemoveItemsWorker;
    private boolean mRemoveOnlyOne;
    private ValueAnimator mShakeAnim;
    private TextView mTitle;

    public static class DeleteIconContainer extends ScreenView implements IconContainer {
        public UninstallDialog mUninstallDialog;

        public DeleteIconContainer(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public DeleteIconContainer(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public int removeShortcutIcon(ShortcutIcon icon) {
            int index = indexOfChild(icon);
            removeView(icon);
            this.mUninstallDialog.refreshUninstallInfo();
            return index;
        }
    }

    private class DeleteObserver extends Stub {
        private ShortcutInfo mInfo;

        public DeleteObserver(ShortcutInfo info) {
            this.mInfo = info;
        }

        public void packageDeleted(String packageName, int result) throws RemoteException {
            if (result == 1) {
                LauncherSettings.deletePackage(UninstallDialog.this.mContext, packageName, this.mInfo.getUser());
                if (this.mInfo != null) {
                    if (UninstallDialog.this.mRemoveOnlyOne && !((UninstallDialog.this.mDragSource instanceof MultiSelectContainerView) && ((MultiSelectContainerView) UninstallDialog.this.mDragSource).hasMovedApps())) {
                        UninstallDialog.this.mLauncher.fillEmpty(this.mInfo);
                    }
                    UninstallDialog.this.mLauncher.removeFromAppsList(this.mInfo, true);
                }
            } else if (UninstallDialog.this.mRemoveItemsWorker.isWorking()) {
                UninstallDialog.this.mRemoveItemsWorker.failedRemoveCurrent();
            } else {
                UninstallDialog.this.mLauncher.runOnUiThread(new Runnable() {
                    public void run() {
                        UninstallDialog.this.mLauncher.showError(R.string.failed_to_delete_temporary);
                        if (DeleteObserver.this.mInfo != null) {
                            UninstallDialog.this.mLauncher.addItem(DeleteObserver.this.mInfo, false);
                        }
                    }
                });
            }
        }
    }

    private class RemoveItemsWorker implements Runnable {
        private int mCounter;
        ArrayList<View> mFailedList;
        private boolean mIsWorking;
        private View[] mItems;

        private RemoveItemsWorker() {
            this.mFailedList = new ArrayList();
            this.mIsWorking = false;
        }

        public boolean isWorking() {
            return this.mIsWorking;
        }

        public void remove(View[] items) {
            this.mCounter = 0;
            this.mItems = items;
            this.mIsWorking = true;
            this.mFailedList.clear();
            UninstallDialog.this.post(this);
        }

        public void failedRemoveCurrent() {
            this.mFailedList.add(this.mItems[this.mCounter]);
        }

        public void run() {
            if (!UninstallDialog.this.removeItem(this.mItems[this.mCounter])) {
                failedRemoveCurrent();
            }
            this.mCounter++;
            if (this.mCounter == this.mItems.length) {
                for (View view : this.mItems) {
                    if (!(((MultiSelectContainerView) UninstallDialog.this.mDragSource).hasMovedApps() || this.mFailedList.contains(view))) {
                        UninstallDialog.this.mLauncher.fillEmpty((ItemInfo) view.getTag());
                    }
                }
                while (this.mCounter < this.mItems.length) {
                    failedRemoveCurrent();
                    this.mCounter++;
                }
                String message;
                if (this.mFailedList.isEmpty()) {
                    message = "";
                    if (this.mItems.length == 1) {
                        message = UninstallDialog.this.mContext.getResources().getString(R.string.uninstall_result_succeeded);
                    } else if (this.mItems.length > 1) {
                        message = String.format(UninstallDialog.this.mContext.getResources().getString(R.string.uninstall_result_multi_succeeded), new Object[]{Integer.valueOf(this.mItems.length)});
                    }
                    Toast.makeText(UninstallDialog.this.mContext, message, 200).show();
                } else {
                    View[] items = new View[this.mFailedList.size()];
                    UninstallDialog.this.cancelItems((View[]) this.mFailedList.toArray(items));
                    message = "";
                    if (items.length == 1) {
                        message = String.format(UninstallDialog.this.mContext.getResources().getString(R.string.uninstall_result_failed), new Object[]{((ShortcutInfo) items[0].getTag()).getTitle(UninstallDialog.this.mContext)});
                    } else if (items.length == 2) {
                        r7 = UninstallDialog.this.mContext.getResources().getString(R.string.uninstall_result_two_failed);
                        r8 = new Object[2];
                        r8[0] = ((ShortcutInfo) items[0].getTag()).getTitle(UninstallDialog.this.mContext);
                        r8[1] = Integer.valueOf(UninstallDialog.this.mIsCnLanguage ? items.length : items.length - 1);
                        message = String.format(r7, r8);
                    } else if (items.length > 2) {
                        r7 = UninstallDialog.this.mContext.getResources().getString(R.string.uninstall_result_other_failed);
                        r8 = new Object[2];
                        r8[0] = ((ShortcutInfo) items[0].getTag()).getTitle(UninstallDialog.this.mContext);
                        r8[1] = Integer.valueOf(UninstallDialog.this.mIsCnLanguage ? items.length : items.length - 1);
                        message = String.format(r7, r8);
                    }
                    Toast.makeText(UninstallDialog.this.mContext, message, 200).show();
                }
                this.mFailedList.clear();
                this.mItems = null;
                return;
            }
            UninstallDialog.this.post(this);
        }
    }

    public UninstallDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.TAG = getClass().getSimpleName();
        this.mLayoutHeight = 0;
        this.mCustomHeight = 0;
        this.mRemoveOnlyOne = true;
        this.mIsCnLanguage = false;
        this.mRemoveItemsWorker = new RemoveItemsWorker();
        this.mIsCnLanguage = getResources().getConfiguration().locale.getLanguage().equals(Locale.CHINA.getLanguage());
        this.mPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
    }

    public int getLayoutHeight() {
        return this.mLayoutHeight - getTop();
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        this.mLayoutHeight = bottom - top;
        return super.setFrame(left, top, right, this.mCustomHeight + top);
    }

    protected void onFinishInflate() {
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mDescription = (TextView) findViewById(R.id.description_text);
        this.mIconContainer = (DeleteIconContainer) findViewById(R.id.delete_icon_container);
        this.mIconContainer.setScreenLayoutMode(2);
        this.mIconContainer.setEnableReverseDrawingMode(true);
        this.mIconContainer.mUninstallDialog = this;
        this.mMessages = findViewById(R.id.messages);
        this.mButtons = findViewById(R.id.buttons);
        this.mShakeAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f}).setDuration(200);
        this.mShakeAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Random r = new Random();
                UninstallDialog.this.mIconContainer.setTranslationX(((r.nextFloat() - 0.5f) * ((float) UninstallDialog.this.mIconContainer.getWidth())) * 0.05f);
                UninstallDialog.this.mIconContainer.setTranslationY(((r.nextFloat() - 0.5f) * ((float) UninstallDialog.this.mIconContainer.getHeight())) * 0.05f);
            }
        });
        this.mShakeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                UninstallDialog.this.mIconContainer.setTranslationX(0.0f);
                UninstallDialog.this.mIconContainer.setTranslationY(0.0f);
            }
        });
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void stretctHeightTo(int height) {
        this.mCustomHeight = height;
        super.setFrame(getLeft(), getTop(), getRight(), getTop() + this.mCustomHeight);
    }

    public boolean bindItem(DragObject d) {
        boolean isWidgetProvider;
        ItemInfo info = d.getDragInfo();
        ItemIcon icon = null;
        if (info.itemType == 6) {
            isWidgetProvider = true;
        } else {
            isWidgetProvider = false;
        }
        if (info instanceof ShortcutInfo) {
            icon = this.mLauncher.createItemIcon(this.mIconContainer, info);
        } else if (isWidgetProvider) {
            PackageManager pm = this.mContext.getPackageManager();
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(((LauncherAppWidgetProviderInfo) info).providerInfo.provider.getPackageName(), 0);
            } catch (NameNotFoundException e) {
            }
            if (appInfo == null) {
                return false;
            }
            icon = (ItemIcon) LayoutInflater.from(this.mLauncher).inflate(R.layout.application, this.mIconContainer, false);
            icon.setIcon(appInfo.loadIcon(pm), null);
            icon.setTag(info);
        }
        if (icon == null) {
            return false;
        }
        setIcon(icon);
        if (isWidgetProvider) {
            d.getDragView().setFadeoutAnimationMode();
        } else {
            d.getDragView().setAnimateTarget(icon);
        }
        if (this.mDragSource == null) {
            this.mDragSource = d.dragSource;
        }
        return true;
    }

    private void setTitle() {
        CharSequence title;
        ItemIcon first = (ItemIcon) this.mIconContainer.getScreen(0);
        int size = this.mIconContainer.getScreenCount();
        if (first.getTag() instanceof ShortcutInfo) {
            title = ((ShortcutInfo) first.getTag()).getTitle(this.mContext);
        } else {
            title = ScreenUtils.getProviderName(this.mContext, ((LauncherAppWidgetProviderInfo) first.getTag()).providerInfo.provider.getPackageName());
        }
        if (size > 1) {
            TextView textView = this.mTitle;
            String string = this.mContext.getResources().getString(R.string.uninstall_dialog_title_multi_format);
            Object[] objArr = new Object[2];
            objArr[0] = title;
            if (!this.mIsCnLanguage) {
                size--;
            }
            objArr[1] = Integer.valueOf(size);
            textView.setText(String.format(string, objArr));
            if (containRelativeXSpace()) {
                this.mDescription.setText(R.string.uninstall_with_xspace_dialog_message);
            } else {
                this.mDescription.setText(R.string.uninstall_dialog_message);
            }
        } else if (size == 1) {
            this.mTitle.setText(String.format(this.mContext.getString(R.string.uninstall_dialog_title_format), new Object[]{title}));
            this.mDescription.setText(R.string.uninstall_dialog_message);
            if (first.getTag() instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) first.getTag();
                if (XSpaceUserHandle.isXSpaceUser(info.getUser())) {
                    this.mTitle.setText(String.format(this.mContext.getResources().getString(R.string.uninstall_xspace_dialog_title_format), new Object[]{title}));
                    this.mDescription.setText(R.string.uninstall_xspace_dialog_message);
                } else if (hasRelativeXSpaceApp(info.getPackageName())) {
                    this.mDescription.setText(R.string.uninstall_with_xspace_dialog_message);
                }
            }
        }
    }

    private boolean hasRelativeXSpaceApp(String pkgName) {
        if (UserHandle.myUserId() != 0) {
            return false;
        }
        try {
            if (this.mPackageManager.getPackageInfo(pkgName, 0, 999) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean containRelativeXSpace() {
        for (int i = 0; i < this.mIconContainer.getScreenCount(); i++) {
            ItemIcon itemIcon = (ItemIcon) this.mIconContainer.getScreen(i);
            if (itemIcon.getTag() instanceof ShortcutInfo) {
                ShortcutInfo info = (ShortcutInfo) itemIcon.getTag();
                if (!XSpaceUserHandle.isXSpaceUser(info.getUser()) && hasRelativeXSpaceApp(info.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public int checkUninstallApp() {
        if (containRelativeXSpace()) {
            if (this.mIconContainer.getScreenCount() == 1) {
                return 0;
            }
            if (this.mIconContainer.getScreenCount() > 1) {
                return 1;
            }
        }
        return -1;
    }

    private void setIcon(ItemIcon icon) {
        icon.setIsHideTitle(true);
        icon.setIsHideShadow(true);
        this.mIconContainer.addView(icon);
        refreshUninstallInfo();
    }

    public int getUninstallItemCount() {
        return this.mIconContainer.getScreenCount();
    }

    public void refreshUninstallInfo() {
        if (this.mIconContainer.getScreenCount() > 0) {
            setTitle();
        } else {
            this.mLauncher.getDeleteZone().showUninstallDialog(false, true);
        }
    }

    public void onCancel() {
        cancelItems(this.mIconContainer.removeOutAllScreens());
    }

    public void cancelItems(View[] items) {
        DragSource dragSource;
        if (this.mDragSource instanceof MultiSelectContainerView) {
            dragSource = this.mDragSource;
        } else {
            dragSource = null;
        }
        MultiSelectContainerView dragSource2 = (MultiSelectContainerView) dragSource;
        for (int i = 0; i < items.length; i++) {
            if (dragSource2 == null || !this.mLauncher.isMultiSelectEnabled()) {
                ItemInfo info = (ItemInfo) items[i].getTag();
                if (info instanceof ShortcutInfo) {
                    if (info.isPresetApp() && (this.mDragSource instanceof Folder)) {
                        ((Folder) this.mDragSource).removeItem((ShortcutInfo) info);
                    }
                    this.mLauncher.addItem(info, false);
                }
            } else {
                dragSource2.pushItemBack(items[i]);
            }
        }
        this.mDragSource = null;
    }

    public void onConfirm() {
        View[] children = this.mIconContainer.removeOutAllScreens();
        if (children.length == 1) {
            this.mRemoveOnlyOne = true;
            if (!removeItem(children[0])) {
                cancelItems(children);
            }
            this.mDragSource = null;
        } else if (children.length > 1) {
            this.mRemoveOnlyOne = false;
            this.mRemoveItemsWorker.remove(children);
        }
    }

    private boolean removeItem(View item) {
        ItemInfo info = (ItemInfo) item.getTag();
        if (info.isPresetApp()) {
            LauncherModel.deleteItemFromDatabase(this.mLauncher, info);
            if (this.mRemoveOnlyOne && !((this.mDragSource instanceof MultiSelectContainerView) && ((MultiSelectContainerView) this.mDragSource).hasMovedApps())) {
                this.mLauncher.fillEmpty(info);
            }
            return true;
        } else if (info instanceof LauncherAppWidgetProviderInfo) {
            return deletePackage(((LauncherAppWidgetProviderInfo) info).providerInfo.provider.getPackageName(), null);
        } else {
            if (!(info instanceof ShortcutInfo)) {
                return false;
            }
            boolean removeSuccess = deletePackage(((ShortcutInfo) info).intent.getComponent().getPackageName(), (ShortcutInfo) info);
            if (!removeSuccess) {
                return removeSuccess;
            }
            this.mLauncher.removeFromAppsList((ShortcutInfo) info, true);
            return removeSuccess;
        }
    }

    private boolean deletePackage(String packageName, ShortcutInfo info) {
        if (DeleteZone.isSystemPackage(this.mContext, packageName)) {
            return false;
        }
        if (info == null) {
            this.mContext.getPackageManager().deletePackage(packageName, new DeleteObserver(info), 0);
            return true;
        }
        try {
            if (XSpaceUserHandle.isXSpaceUser(info.getUser())) {
                this.mPackageManager.deletePackageAsUser(packageName, new DeleteObserver(info), 999, 4);
            } else {
                if (hasRelativeXSpaceApp(packageName)) {
                    this.mPackageManager.deletePackageAsUser(packageName, null, 999, 4);
                }
                this.mPackageManager.deletePackageAsUser(packageName, new DeleteObserver(info), UserHandle.myUserId(), 4);
            }
            return true;
        } catch (RemoteException e) {
            Log.e(this.TAG, "Can not deletePackage: " + packageName, e);
            return false;
        }
    }

    public void removeDragItem(DragObject d, boolean deleteInDatabase) {
        if (d.dragSource instanceof Folder) {
            ((Folder) d.dragSource).removeItem((ShortcutInfo) d.getDragInfo());
        }
        if (deleteInDatabase) {
            if (d.getDragInfo() instanceof ShortcutInfo) {
                this.mLauncher.removeFromAppsList((ShortcutInfo) d.getDragInfo(), true);
            }
            LauncherModel.deleteItemFromDatabase(this.mLauncher, d.getDragInfo());
            ItemInfo dragInfo = d.getDragInfo();
            if ((d.dragSource instanceof Workspace) && dragInfo.spanX == 1 && dragInfo.spanY == 1) {
                this.mLauncher.fillEmpty(d.getDragInfo());
            } else if ((d.dragSource instanceof MultiSelectContainerView) && !((MultiSelectContainerView) d.dragSource).hasMovedApps()) {
                this.mLauncher.fillEmpty(d.getDragInfo());
            }
            if (dragInfo.itemType == 1 && "com.xiaomi.market".equals(((ShortcutInfo) dragInfo).getPackageName())) {
                post(new Runnable() {
                    public void run() {
                        Intent deletedIntend = new Intent("android.intent.action.ACTION_MARKET_RECOMMEND_SHORTCUT_DELETED");
                        deletedIntend.setPackage("com.xiaomi.market");
                        UninstallDialog.this.mContext.sendBroadcast(deletedIntend);
                    }
                });
            }
        }
    }

    public void setContentAlpha(float alpha) {
        this.mMessages.setAlpha(alpha);
        this.mButtons.setAlpha(alpha);
    }

    public void onShow(boolean isShow, boolean isCanceled) {
        if (isShow) {
            this.mIconContainer.setScaleX(1.0f);
            this.mIconContainer.setScaleY(1.0f);
            this.mIconContainer.setAlpha(1.0f);
        } else if (!isCanceled && Launcher.isSupportCompleteAnimation()) {
            this.mShakeAnim.start();
            this.mIconContainer.animate().setDuration(150).setStartDelay(200).scaleX(0.0f).scaleY(0.0f).alpha(0.0f).start();
            Bitmap colors = DragController.createViewBitmap(this.mIconContainer, 1.0f);
            int[] loc = new int[2];
            DragLayer dl = this.mLauncher.getDragLayer();
            dl.getLocationInDragLayer(this.mIconContainer, loc, false);
            Rect area = new Rect(loc[0], loc[1], loc[0] + this.mIconContainer.getWidth(), loc[1] + this.mIconContainer.getHeight());
            area.bottom += area.top;
            area.left = 0;
            area.top = 0;
            area.right = dl.getWidth();
            ParasiticDrawingFireworks pdo = new ParasiticDrawingFireworks(dl, colors, area);
            dl.attachParasiticDrawingObject(pdo);
            pdo.setStartDelay(200);
            pdo.setDuration(1100);
            pdo.start();
        }
    }
}
