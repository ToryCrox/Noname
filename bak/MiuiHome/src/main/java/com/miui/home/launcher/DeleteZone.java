package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import java.io.File;

public class DeleteZone extends FrameLayout implements OnClickListener, DragListener, DropTarget, WallpaperColorChangedListener {
    private Runnable mConfirmUninstall;
    private boolean mDraggingObjectCanBeDeleted;
    private TextView mEditingTips;
    private Animation mEditingTipsShrinkToTop;
    private Animation mEditingTipsStretchFromTop;
    private Animation mFadeIn;
    private ValueAnimator mIndicateBgAnimator;
    private int mIndicatePanelBgHeight;
    private Launcher mLauncher;
    private RetainedList mRetainedList;
    private ValueAnimator mShowTrashBgAnimator;
    private ValueAnimator mShowUninstallDialogAnimator;
    private int mShowUninstallDialogStartSize;
    private int mTrashBgDeltaY;
    private ImageView mTrashIcon;
    private Animation mTrashShrinkToTop;
    private Animation mTrashStretchFromTop;
    private boolean mUninstallAnimShowing;
    private UninstallDialog mUninstallDialog;
    private boolean mUninstallDialogShowing;

    public DeleteZone(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndicateBgAnimator = new ValueAnimator();
        this.mShowUninstallDialogAnimator = new ValueAnimator();
        this.mShowTrashBgAnimator = new ValueAnimator();
        this.mUninstallDialogShowing = false;
        this.mUninstallAnimShowing = false;
        this.mDraggingObjectCanBeDeleted = false;
        this.mIndicatePanelBgHeight = context.getResources().getDimensionPixelSize(R.dimen.delete_indicate_panel_height);
        setAnimationCacheEnabled(false);
        this.mRetainedList = new RetainedList(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mTrashIcon = (ImageView) findViewById(R.id.trash);
        this.mTrashIcon.getBackground().setAlpha(0);
        this.mTrashBgDeltaY = (this.mTrashIcon.getDrawable().getIntrinsicHeight() - this.mTrashIcon.getBackground().getIntrinsicHeight()) / 2;
        this.mTrashIcon.setTranslationY((float) this.mTrashBgDeltaY);
        this.mEditingTips = (TextView) findViewById(R.id.editing_tips);
        this.mUninstallDialog = (UninstallDialog) findViewById(R.id.uninstall_dialog);
        findViewById(R.id.btnCancel).setOnClickListener(this);
        findViewById(R.id.btnOk).setOnClickListener(this);
        this.mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        this.mTrashShrinkToTop = AnimationUtils.loadAnimation(getContext(), R.anim.shrink_to_top);
        this.mEditingTipsShrinkToTop = AnimationUtils.loadAnimation(getContext(), R.anim.shrink_to_top);
        this.mTrashStretchFromTop = AnimationUtils.loadAnimation(getContext(), R.anim.stretch_from_top);
        this.mEditingTipsStretchFromTop = AnimationUtils.loadAnimation(getContext(), R.anim.stretch_from_top);
        Resources res = getContext().getResources();
        this.mIndicateBgAnimator.setDuration((long) res.getInteger(17694720));
        this.mIndicateBgAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) animation.getAnimatedValue()).intValue();
                DeleteZone.this.mLauncher.getScreen().setTranslationY((float) value);
                DeleteZone.this.mUninstallDialog.stretctHeightTo(value);
            }
        });
        this.mShowUninstallDialogAnimator.setDuration((long) res.getInteger(17694721));
        this.mShowUninstallDialogAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Integer) animation.getAnimatedValue()).intValue();
                FrameLayout screen = DeleteZone.this.mLauncher.getScreen();
                float ratio = ((float) (value - DeleteZone.this.mShowUninstallDialogStartSize)) / ((float) (DeleteZone.this.mUninstallDialog.getLayoutHeight() - DeleteZone.this.mShowUninstallDialogStartSize));
                float contentAlpha = 1.0f - (0.7f * ratio);
                if (DeleteZone.this.mLauncher.isFolderShowing()) {
                    DeleteZone.this.mLauncher.getFolderCling().setContentAlpha(contentAlpha);
                }
                DeleteZone.this.mLauncher.getScreenContent().setAlpha(contentAlpha);
                if (DeleteZone.this.mLauncher.isInNormalEditing() || !DeleteZone.this.mLauncher.isFolderShowing()) {
                    screen.setTranslationY((float) value);
                } else {
                    DeleteZone.this.mLauncher.getFolderCling().getFolder().setTranslationY((float) value);
                    DeleteZone.this.mLauncher.getFolderCling().getRecommendScreen().setTranslationY((float) value);
                }
                DeleteZone.this.mUninstallDialog.setContentAlpha(ratio);
                DeleteZone.this.mUninstallDialog.stretctHeightTo(value);
            }
        });
        this.mShowUninstallDialogAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (DeleteZone.this.mConfirmUninstall != null) {
                    DeleteZone.this.mConfirmUninstall.run();
                }
                DeleteZone.this.mUninstallAnimShowing = false;
            }
        });
    }

    public void onWallpaperColorChanged() {
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mEditingTips.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.notification.dark);
            this.mTrashIcon.setImageResource(R.drawable.trash_dark);
            return;
        }
        this.mEditingTips.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.notification);
        this.mTrashIcon.setImageResource(R.drawable.trash);
    }

    public boolean acceptDrop(DragObject d) {
        if (!this.mLauncher.isSceneShowing() && d.getDragInfo().container == -1 && d.getDragInfo().itemType == 2) {
            return false;
        }
        return true;
    }

    private boolean checkDeletedOperationPermission(DragObject dragObject) {
        if (this.mLauncher.isSceneShowing()) {
            return true;
        }
        boolean canDelete = false;
        for (int i = 0; i < dragObject.getDraggingSize(); i++) {
            canDelete |= canItemBeDeleted(dragObject.getDragInfo(i));
        }
        return canDelete;
    }

    private boolean canItemBeDeleted(ItemInfo info) {
        if (info.isRetained || ((info instanceof ShortcutInfo) && this.mRetainedList.contain(((ShortcutInfo) info).intent))) {
            return false;
        }
        if (info.itemType == 12) {
            if (WallpaperUtils.isSystemPresetWallpaper(((WallpaperThumbnailInfo) info).getWallpaperPath())) {
                return false;
            }
            return true;
        } else if (info.container == -1 && info.itemType != 6) {
            return false;
        } else {
            if (info.itemType == 2 && ((FolderInfo) info).count() != 0) {
                return false;
            }
            if (info.itemType == 0 && isSystemPackage(this.mContext, ((ShortcutInfo) info).intent.getComponent().getPackageName())) {
                return false;
            }
            if (info.itemType == 6) {
                if (isSystemPackage(this.mContext, ((LauncherAppWidgetProviderInfo) info).providerInfo.provider.getPackageName())) {
                    return false;
                }
            }
            return true;
        }
    }

    public void onDropStart(DragObject dragObject) {
        dragObject.removeDragViewsAtLast = true;
    }

    public boolean onDrop(DragObject d) {
        Rect r = new Rect();
        getHitView().getHitRect(r);
        if (!r.contains(d.x, d.y)) {
            return false;
        }
        if (this.mLauncher.isSceneShowing()) {
            this.mLauncher.getSceneScreen().removeDraggedSprite();
            return true;
        } else if (!this.mDraggingObjectCanBeDeleted) {
            return false;
        } else {
            final ItemInfo dragInfo = d.getDragInfo();
            if (dragInfo.itemType == 2) {
                FolderInfo userFolderInfo = (FolderInfo) dragInfo;
                if (userFolderInfo.count() == 0 || !(d.dragSource instanceof MultiSelectContainerView)) {
                    LauncherModel.deleteUserFolderContentsFromDatabase(this.mLauncher, userFolderInfo);
                    this.mLauncher.removeFolder(userFolderInfo);
                    if (d.dragSource instanceof Workspace) {
                        this.mLauncher.fillEmpty(userFolderInfo);
                    }
                } else {
                    MultiSelectContainerView dragSource = d.dragSource;
                    dragSource.pushItemBack(this.mLauncher.createItemIcon(dragSource, userFolderInfo));
                }
            } else if (dragInfo.itemType == 4) {
                LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) dragInfo;
                this.mLauncher.removeAppWidget(launcherAppWidgetInfo);
                LauncherAppWidgetHost appWidgetHost = this.mLauncher.getAppWidgetHost();
                if (appWidgetHost != null) {
                    appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
                }
                this.mUninstallDialog.removeDragItem(d, true);
            } else if (dragInfo.itemType == 0) {
                ShortcutInfo info = (ShortcutInfo) dragInfo;
                if (info.intent == null || info.intent.getComponent() == null) {
                    LauncherModel.deleteItemFromDatabase(this.mLauncher, dragInfo);
                } else if (this.mContext.getPackageManager().resolveActivity(info.intent, 0) == null) {
                    this.mUninstallDialog.removeDragItem(d, true);
                } else {
                    this.mUninstallDialog.removeDragItem(d, false);
                    this.mUninstallDialog.bindItem(d);
                }
            } else if (dragInfo.itemType == 6) {
                this.mUninstallDialog.bindItem(d);
            } else if (dragInfo.itemType == 12) {
                File wallpaper = new File(((WallpaperThumbnailInfo) dragInfo).getWallpaperPath());
                if (wallpaper.exists()) {
                    wallpaper.delete();
                }
            } else {
                if (dragInfo.itemType == 5) {
                    d.getDragView().setOnRemoveCallback(new Runnable() {
                        public void run() {
                            DeleteZone.this.mLauncher.removeGadget(dragInfo);
                        }
                    });
                }
                if (dragInfo.isPresetApp()) {
                    this.mUninstallDialog.bindItem(d);
                } else {
                    this.mUninstallDialog.removeDragItem(d, true);
                }
            }
            if (d.isLastObject()) {
                startUninstallDialog(d);
            }
            if (true) {
                return true;
            }
            this.mLauncher.showError(R.string.failed_to_delete_temporary);
            return false;
        }
    }

    private void startUninstallDialog(DragObject d) {
        if (this.mUninstallDialog.getUninstallItemCount() > 0) {
            Launcher.performLayoutNow(getRootView());
            showUninstallDialog(true, false);
        }
    }

    private void showIndicateBackground(boolean isShow) {
        FrameLayout screen = this.mLauncher.getScreen();
        if (isShow) {
            this.mIndicateBgAnimator.setIntValues(new int[]{(int) screen.getTranslationY(), this.mIndicatePanelBgHeight});
            this.mIndicateBgAnimator.start();
        } else if (!this.mUninstallDialogShowing) {
            this.mIndicateBgAnimator.setIntValues(new int[]{(int) screen.getTranslationY(), 0});
            this.mIndicateBgAnimator.start();
        }
    }

    public boolean showUninstallDialog(boolean isShow, boolean isCanceled) {
        if (this.mUninstallDialogShowing == isShow) {
            return false;
        }
        boolean z;
        this.mUninstallDialogShowing = isShow;
        this.mUninstallAnimShowing = true;
        this.mLauncher.blurBehindWithAnim(isShow);
        Launcher launcher = this.mLauncher;
        if (isShow) {
            z = false;
        } else {
            z = true;
        }
        launcher.enableFolderInteractive(z);
        this.mUninstallDialog.onShow(isShow, isCanceled);
        if (isShow) {
            this.mConfirmUninstall = null;
            this.mShowUninstallDialogStartSize = this.mUninstallDialog.getHeight();
            this.mShowUninstallDialogAnimator.setIntValues(new int[]{this.mShowUninstallDialogStartSize, this.mUninstallDialog.getLayoutHeight()});
            this.mShowUninstallDialogAnimator.setStartDelay(0);
        } else {
            this.mShowUninstallDialogStartSize = 0;
            this.mShowUninstallDialogAnimator.setIntValues(new int[]{this.mUninstallDialog.getHeight(), 0});
            ValueAnimator valueAnimator = this.mShowUninstallDialogAnimator;
            long j = (isCanceled || !Launcher.isSupportCompleteAnimation()) ? 0 : 600;
            valueAnimator.setStartDelay(j);
            if (!this.mLauncher.isInNormalEditing()) {
                this.mLauncher.showStatusBar(true);
            } else if (this.mLauncher.isShowingEditingTips()) {
                showEditingTips(true, true);
            }
        }
        this.mShowUninstallDialogAnimator.start();
        return true;
    }

    private void showTrashIcon(boolean isShow, boolean showBackgroundAnim) {
        if (isShow) {
            if (this.mTrashIcon.getVisibility() == 4) {
                this.mTrashIcon.startAnimation(this.mTrashStretchFromTop);
                this.mTrashIcon.setVisibility(0);
            }
        } else if (!isShow && this.mTrashIcon.getVisibility() == 0) {
            this.mTrashIcon.startAnimation(this.mTrashShrinkToTop);
            this.mTrashIcon.setVisibility(4);
        }
    }

    public void showEditingTips(boolean isShow) {
        showEditingTips(isShow, true);
    }

    private void showEditingTips(boolean isShow, boolean showBackgroundAnim) {
        if (isShow) {
            if (this.mEditingTips.getVisibility() == 4) {
                this.mEditingTips.startAnimation(this.mEditingTipsStretchFromTop);
                this.mEditingTips.setVisibility(0);
            }
        } else if (!isShow && this.mEditingTips.getVisibility() == 0) {
            this.mEditingTips.startAnimation(this.mEditingTipsShrinkToTop);
            this.mEditingTips.setVisibility(4);
        }
    }

    public static boolean isSystemPackage(Context context, String packageName) {
        try {
            if ((context.getPackageManager().getApplicationInfo(packageName, 0).flags & 1) != 0) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void showTrashBg(final boolean show) {
        this.mShowTrashBgAnimator.cancel();
        this.mShowTrashBgAnimator.removeAllListeners();
        this.mShowTrashBgAnimator.removeAllUpdateListeners();
        this.mShowTrashBgAnimator.setDuration((long) this.mContext.getResources().getInteger(17694721));
        this.mShowTrashBgAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mShowTrashBgAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                DeleteZone.this.mTrashIcon.getBackground().setAlpha((int) ((show ? value : 1.0f - value) * 255.0f));
                DeleteZone.this.mTrashIcon.setTranslationY(show ? ((float) DeleteZone.this.mTrashBgDeltaY) * (1.0f - value) : ((float) DeleteZone.this.mTrashBgDeltaY) * value);
            }
        });
        this.mShowTrashBgAnimator.start();
    }

    public void onDragEnter(DragObject d) {
        if (acceptDrop(d) && this.mDraggingObjectCanBeDeleted) {
            showTrashBg(true);
            if (!this.mLauncher.isFolderShowing()) {
                showIndicateBackground(true);
            }
        }
    }

    public void onDragOver(DragObject d) {
    }

    public void onDragExit(DragObject d) {
        if (acceptDrop(d) && this.mDraggingObjectCanBeDeleted) {
            if (this.mUninstallDialogShowing) {
                this.mShowTrashBgAnimator.cancel();
                this.mTrashIcon.getBackground().setAlpha(0);
                this.mTrashIcon.setTranslationY((float) this.mTrashBgDeltaY);
                this.mTrashIcon.setVisibility(4);
            } else {
                showTrashBg(false);
            }
            showIndicateBackground(false);
        }
    }

    public void onDragStart(DragSource source, DragObject dragObject) {
        if (!(source instanceof WorkspaceThumbnailView) && !Launcher.isChildrenModeEnabled()) {
            this.mDraggingObjectCanBeDeleted = checkDeletedOperationPermission(dragObject);
            if (this.mDraggingObjectCanBeDeleted) {
                hideEditingTips();
                this.mLauncher.showStatusBar(false);
                showTrashIcon(true, true);
                this.mLauncher.getDragController().addDropTarget(this);
            }
        }
    }

    public void onDragEnd() {
        boolean z = true;
        if (this.mDraggingObjectCanBeDeleted) {
            if (!this.mLauncher.isShowingEditingTips()) {
                hideEditingTips();
            } else if (!(this.mLauncher.isErrorBarShowing() || this.mUninstallDialogShowing)) {
                fadeInEditingTips(true);
            }
            if (!this.mLauncher.isShowingEditingTips()) {
                z = false;
            }
            showTrashIcon(false, z);
            this.mLauncher.getDragController().removeDropTarget(this);
        }
    }

    public void fadeInEditingTips(boolean fadeIn) {
        if (fadeIn) {
            this.mEditingTips.startAnimation(this.mFadeIn);
        }
        this.mEditingTips.setVisibility(0);
    }

    public void setEditingTips(CharSequence tips) {
        this.mEditingTips.setText(tips);
    }

    public void hideEditingTips() {
        this.mEditingTips.clearAnimation();
        this.mEditingTips.setVisibility(4);
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mUninstallDialog.setLauncher(launcher);
    }

    void setDragController(DragController dragController) {
    }

    public boolean isDropEnabled() {
        return true;
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public View getHitView() {
        return this.mTrashIcon;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                if (this.mConfirmUninstall == null) {
                    onCancelUninstall();
                    return;
                }
                return;
            case R.id.btnOk:
                confirmUninstall();
                return;
            default:
                return;
        }
    }

    public boolean onCancelUninstall() {
        if (!showUninstallDialog(false, true)) {
            return false;
        }
        this.mUninstallDialog.onCancel();
        return true;
    }

    private boolean onConfirmUninstall() {
        if (!showUninstallDialog(false, false)) {
            return false;
        }
        this.mConfirmUninstall = new Runnable() {
            public void run() {
                DeleteZone.this.mUninstallDialog.onConfirm();
            }
        };
        return true;
    }

    private void confirmUninstall() {
        int flag = this.mUninstallDialog.checkUninstallApp();
        if (flag == -1) {
            onConfirmUninstall();
            return;
        }
        Builder builder = new Builder(this.mContext);
        builder.setTitle(R.string.confirm_uninstall_dialog_title);
        if (flag == 0) {
            builder.setMessage(R.string.confirm_uninstall_dialog_has_xspace_app_msg);
        } else if (flag == 1) {
            builder.setMessage(R.string.confirm_uninstall_dialog_contain_xspace_app_msg);
        }
        builder.setNegativeButton(R.string.cancel_btn_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteZone.this.onCancelUninstall();
            }
        });
        builder.setPositiveButton(R.string.confirm_btn_label, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteZone.this.onConfirmUninstall();
            }
        });
        builder.create().show();
    }

    public boolean isUninstallDialogShowing() {
        return this.mUninstallDialogShowing;
    }

    public boolean isUninstallAnimShowing() {
        return this.mUninstallAnimShowing;
    }

    public void onDropCompleted() {
    }
}
