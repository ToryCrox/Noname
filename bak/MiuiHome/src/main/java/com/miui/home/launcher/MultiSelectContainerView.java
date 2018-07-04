package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.DragView.DropTargetContainer;
import com.miui.home.launcher.Launcher.IconContainer;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;

public class MultiSelectContainerView extends ScreenView implements OnClickListener, OnLongClickListener, DragSource, DropTargetContainer, DropTarget, IconContainer, WallpaperColorChangedListener {
    private Animation mAnimFadeIn;
    private Animation mAnimFadeOut;
    private DragController mDragController;
    private int mDropAnimationCounter = 0;
    private boolean mDropRestoring = false;
    private FolderIcon mEmptyFolder = null;
    private FolderInfo mEmptyFolderInfo = null;
    private boolean mHasMoveApps = false;
    private boolean mIsShown = false;
    private Launcher mLauncher;
    private TextView mMultiSelectTips;
    private boolean mMultiSelectTipsShowing = false;
    private Rect mTmpRect = new Rect();

    public MultiSelectContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScrollWholeScreen(true);
        setPushGestureEnabled(true);
        setScreenTransitionType(10);
        setEnableReverseDrawingMode(true);
        setOverScrollRatio(0.0f);
        setOvershootTension(0.0f);
        setChildrenDrawingOrderEnabled(true);
        setScreenLayoutMode(1);
        LayoutParams params = new LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.slide_bar_height));
        params.width = -1;
        params.gravity = 80;
        setSlideBarPosition(params, R.drawable.editing_mode_slidebar_fg, 0, false);
        if (this.mSlideBar != null) {
            this.mSlideBar.setOnTouchListener(null);
        }
        this.mAnimFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        this.mAnimFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    public int removeShortcutIcon(ShortcutIcon icon) {
        int index = indexOfChild(icon);
        removeView(icon);
        updateEditingTips();
        return index;
    }

    public void setDragController(DragController dragger) {
        this.mDragController = dragger;
    }

    public int getItemCount() {
        return super.getScreenCount() - (this.mEmptyFolder == null ? 0 : 1);
    }

    public View getHitView() {
        return this;
    }

    public boolean isDropEnabled() {
        return true;
    }

    public void onDropStart(DragObject dragObject) {
        if (dragObject.isMultiDrag()) {
            dragObject.removeDragViewsAtLast = true;
        }
    }

    public boolean onDrop(DragObject dragObject) {
        if (this.mMultiSelectTipsShowing) {
            show(false);
        }
        ItemIcon icon = this.mLauncher.createItemIcon(this, dragObject.getDragInfo());
        if (dragObject.isMultiDrag()) {
            pushItem(icon, 2, -1);
        } else if (dragObject.dropAction == 1) {
            pushItem(icon, dragObject.dropAction, getPushIndex(dragObject.x));
        } else {
            pushItem(icon, dragObject.dropAction, -1);
        }
        dragObject.getDragView().setAnimateTarget(icon);
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
        if (this.mLauncher.isFolderShowing()) {
            Folder folder = this.mLauncher.getFolderCling().getFolder();
            folder.removeItem(folder.getDragedItem());
        }
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        if (dragObject.getDragInfo() == this.mEmptyFolderInfo) {
            return false;
        }
        int itemType = dragObject.getDragInfo().itemType;
        if (itemType == 0 || itemType == 1 || itemType == 11 || itemType == 2) {
            return true;
        }
        return false;
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        if (this.mLauncher != null && this.mEmptyFolder == null) {
            this.mEmptyFolderInfo = new FolderInfo();
            this.mEmptyFolderInfo.setTitle(getResources().getString(R.string.folder_name), launcher);
            this.mEmptyFolder = (FolderIcon) this.mLauncher.createItemIcon(null, this.mEmptyFolderInfo);
            this.mEmptyFolder.setEnableAutoLayoutAnimation(true);
        }
        this.mMultiSelectTips = new TextView(launcher);
        this.mMultiSelectTips.setText(R.string.multi_selecting_tips);
    }

    public void onWallpaperColorChanged() {
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mMultiSelectTips.setTextAppearance(this.mLauncher, R.style.WorkspaceIconTitle.notification.dark);
        } else {
            this.mMultiSelectTips.setTextAppearance(this.mLauncher, R.style.WorkspaceIconTitle.notification);
        }
        WallpaperUtils.varyViewGroupByWallpaper(this);
    }

    public void pushItem(View v, int dropAction, int index) {
        boolean z = true;
        if (this.mMultiSelectTipsShowing && this.mMultiSelectTips.getParent() == this) {
            show(false);
        }
        skipNextAutoLayoutAnimation();
        v.setScaleX(1.0f);
        v.setScaleY(1.0f);
        v.setOnClickListener(this);
        v.setOnLongClickListener(this);
        WallpaperUtils.onAddViewToGroup(this, v, true);
        switch (dropAction) {
            case 0:
                addView(v, 0);
                break;
            case 1:
                addView(v, index);
                break;
            case 2:
            case 3:
                addView(v, getItemCount());
                break;
        }
        if (getScreenLayoutMode() != 2) {
            z = false;
        }
        hideTitleAndShadow(v, z);
        updateEditingTips();
    }

    private int getPushIndex(int dragX) {
        if (getScreenLayoutMode() == 2) {
            return 0;
        }
        int ci = getCurrentScreenIndex();
        while (ci < getScreenCount()) {
            View child = getScreen(ci);
            if (child == this.mEmptyFolder) {
                return ci;
            }
            child.getHitRect(this.mTmpRect);
            if (this.mTmpRect.centerX() - this.mScrollX > dragX) {
                return ci;
            }
            ci++;
        }
        return ci;
    }

    private void hideTitleAndShadow(View v, boolean isHide) {
        if (v instanceof ItemIcon) {
            ((ItemIcon) v).setIsHideTitle(isHide);
            ((ItemIcon) v).setIsHideShadow(isHide);
            v.invalidate();
        }
    }

    private void updateEditingTips() {
        this.mLauncher.setEditingTips(String.format(getContext().getResources().getString(R.string.editing_mode_title), new Object[]{Integer.valueOf(getItemCount())}));
    }

    private void popItem(View v) {
        popItemsInner(new View[]{v}, 0);
        tryToClose();
    }

    private void tryToClose() {
        if (getItemCount() == 0) {
            int predictedEditingState = this.mLauncher.getPredictedEditingState();
            Launcher launcher = this.mLauncher;
            if (predictedEditingState == 10) {
                this.mLauncher.setEditingState(8);
            }
            setScreenLayoutMode(1);
        }
    }

    public void clearAll() {
        if (getItemCount() > 0) {
            for (View v : getChildren()) {
                removeView(v);
            }
        }
    }

    public void removeView(View v) {
        removeScreen(indexOfChild(v));
        v.setScaleX(1.0f);
        v.setScaleY(1.0f);
        updateEditingTips();
    }

    public void popAll(int dropAction) {
        if (!isDropAnimating() && getItemCount() > 0) {
            popItemsInner(getChildren(), dropAction);
        }
    }

    private void popItemsInner(View[] views, int dropAction) {
        if (dropAction == 3) {
            int i = 0;
            this.mDropRestoring = true;
            for (View view : views) {
                ItemInfo itemInfo = (ItemInfo) view.getTag();
                if (itemInfo.container == -100) {
                    this.mLauncher.getDragController().startAutoDrag(new View[]{view}, this, this.mLauncher.getWorkspace(), 0, dropAction, i);
                } else if (itemInfo instanceof ShortcutInfo) {
                    Workspace workspace;
                    ViewGroup folderIcon = this.mLauncher.getParentFolderIcon((ShortcutInfo) itemInfo);
                    DragController dragController = this.mLauncher.getDragController();
                    View[] viewArr = new View[]{view};
                    if (folderIcon == null) {
                        workspace = this.mLauncher.getWorkspace();
                    } else {
                        ViewGroup viewGroup = folderIcon;
                    }
                    dragController.startAutoDrag(viewArr, this, workspace, 0, dropAction, i);
                }
                i++;
            }
        } else if (views.length != 1 || views[0] != this.mEmptyFolder) {
            DropTarget dropTarget;
            if (this.mLauncher.isFolderShowing()) {
                dropTarget = this.mLauncher.getFolderCling().getFolder().getContent();
            } else {
                dropTarget = this.mLauncher.getWorkspace();
            }
            this.mLauncher.getDragController().startAutoDrag(views, this, dropTarget, 0, dropAction);
        } else if (!this.mLauncher.isFolderShowing()) {
            this.mLauncher.getDragController().startAutoDrag(views, this, this.mLauncher.getWorkspace(), 2, dropAction);
            return;
        } else {
            return;
        }
        this.mDropRestoring = false;
        skipNextAutoLayoutAnimation();
        tryToClose();
    }

    public void onScreenDeleted(long screenId) {
        for (int i = 0; i < getItemCount(); i++) {
            View child = getScreen(i);
            if (child.getTag() instanceof ItemInfo) {
                ItemInfo info = (ItemInfo) child.getTag();
                if (info.screenId == screenId) {
                    info.screenId = -1;
                    info.cellX = -1;
                    info.cellY = -1;
                }
            }
        }
    }

    private View[] getChildren() {
        View[] children = new View[getItemCount()];
        for (int i = 0; i < children.length; i++) {
            children[i] = getScreen(i);
        }
        return children;
    }

    public boolean onLongClick(View v) {
        if (this.mLauncher.getEditingState() != 10) {
            return false;
        }
        if (v == this.mEmptyFolder) {
            this.mDragController.startDrag(v, true, this, 2);
            return true;
        } else if (getScreenLayoutMode() == 2) {
            this.mDragController.startDrag(getChildren(), false, 0.0f, (DragSource) this, 0, 1, false);
            return true;
        } else {
            this.mDragController.startDrag(v, true, this, 0);
            return true;
        }
    }

    public void onClick(View v) {
        if (!isDropAnimating() && this.mLauncher.getEditingState() == 10) {
            this.mHasMoveApps = true;
            if (v == this.mEmptyFolder) {
                popItem(v);
            } else if (getScreenLayoutMode() == 2) {
                popAll(0);
            } else {
                popItem(v);
            }
        }
    }

    protected void onPinchIn(ScaleGestureDetector detector) {
        if (getItemCount() > 1) {
            for (int i = getItemCount() - 1; i >= 0; i--) {
                hideTitleAndShadow(getScreen(i), true);
            }
            setScreenLayoutMode(2);
        }
    }

    protected void onPinchOut(ScaleGestureDetector detector) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            hideTitleAndShadow(getScreen(i), false);
        }
        setScreenLayoutMode(1);
    }

    public void performDropFinishAnimation(View child) {
    }

    public void setDropAnimating(boolean isAnimating) {
        this.mDropAnimationCounter = (isAnimating ? 1 : -1) + this.mDropAnimationCounter;
    }

    public boolean isDropAnimating() {
        return this.mDropAnimationCounter != 0;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isDropAnimating()) {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (isDropAnimating()) {
            ev.setAction(3);
        }
        return super.onTouchEvent(ev);
    }

    public void show(boolean showTips) {
        popAll(3);
        removeAllScreens();
        if (showTips) {
            pushItem(this.mMultiSelectTips, 2, -1);
        } else {
            pushItem(this.mEmptyFolder, 2, -1);
            setPreviewModeFooter(this.mEmptyFolder);
            this.mHasMoveApps = false;
        }
        this.mMultiSelectTipsShowing = showTips;
        if (!this.mIsShown) {
            this.mIsShown = true;
            setVisibility(0);
            startAnimation(this.mAnimFadeIn);
            this.mLauncher.getDragController().addDropTarget(this);
        }
    }

    public boolean isShowingTips() {
        return this.mMultiSelectTipsShowing;
    }

    public boolean hide() {
        this.mIsShown = false;
        if (isDropAnimating()) {
            return false;
        }
        popAll(3);
        setVisibility(4);
        startAnimation(this.mAnimFadeOut);
        setCurrentScreen(0);
        this.mLauncher.getDragController().removeDropTarget(this);
        return true;
    }

    public boolean hasMovedApps() {
        return this.mHasMoveApps;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        if (target instanceof Workspace) {
            this.mHasMoveApps = true;
        }
        if (getItemCount() <= 1) {
            onPinchOut(null);
        }
        tryToClose();
    }

    public void onDropBack(DragObject d) {
        if (d.getDragInfo() != this.mEmptyFolderInfo) {
            if (this.mDropRestoring) {
                d.dropAction = 4;
                if (this.mLauncher.getWorkspace().onDrop(d)) {
                    LauncherModel.updateItemInDatabase(this.mLauncher, d.getDragInfo());
                    return;
                }
                return;
            }
            if (d.isMultiDrag() && !d.removeDragViewsAtLast) {
                d.removeDragViewsAtLast = true;
            }
            onDrop(d);
        }
    }

    public void pushItemBack(View icon) {
        this.mLauncher.setEditingState(10);
        pushItem(icon, 2, -1);
        if (getChildCount() > 1 && getScreenLayoutMode() != 2) {
            setScreenLayoutMode(2);
            View[] children = getChildren();
            for (View hideTitleAndShadow : children) {
                hideTitleAndShadow(hideTitleAndShadow, getScreenLayoutMode() == 2);
            }
        }
    }

    public void onDropCompleted() {
    }

    public void onScreenOrientationChanged() {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            DeviceConfig.correntCellPositionRuntime((ItemInfo) getScreen(i).getTag(), true);
        }
    }

    protected void onPushGesture(int direction) {
        this.mGestureTrigged = true;
        if (getScreenLayoutMode() == 2) {
            onPinchOut(null);
        } else {
            onPinchIn(null);
        }
    }
}
