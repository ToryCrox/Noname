package com.miui.home.launcher;

import android.app.WallpaperManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView.ScaleType;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.DragView.DropTargetContainer;
import com.miui.home.launcher.Launcher.IconContainer;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.AsyncTaskExecutorHelper.RejectedExecutionPolicy;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import java.util.Iterator;

public class HotSeats extends ScreenView implements OnLongClickListener, DragSource, DropTargetContainer, DropTarget, ForceTouchTriggeredListener, IconContainer, WallpaperColorChangedListener {
    private static int MAX_SEATS = -1;
    private CellBackground mCellBackground;
    private Context mContext;
    private Drawable mDefaultCellBackground;
    private DragController mDragController;
    private ItemInfo mDraggingItem;
    private View mForceTouchSelectedView;
    private int mInsertPos = -1;
    private boolean mIsLoading = true;
    private final boolean mIsReplaceSupported = false;
    private int mLastPlaceHolder = -1;
    private int mLastReplacedPos = -1;
    private Launcher mLauncher;
    private int[] mLocation = new int[2];
    private Rect mRect = new Rect();
    private int mReplacedPos = -1;

    public HotSeats(Context context, AttributeSet attrs) {
        super(context, attrs);
        MAX_SEATS = DeviceConfig.getHotseatCount();
        this.mContext = context;
        setScreenTransitionType(10);
        setScreenLayoutMode(3);
        setMaximumSnapVelocity(6000);
        this.mCellBackground = new CellBackground(context);
        this.mCellBackground.setImageAlpha(51);
        this.mDefaultCellBackground = getResources().getDrawable(R.drawable.cell_bg);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setBackground(Utilities.loadThemeCompatibleDrawable(this.mContext, R.drawable.hotseat_background));
        Resources res = getContext().getResources();
        setPadding(res.getDimensionPixelSize(R.dimen.hotseats_padding_side), res.getDimensionPixelSize(R.dimen.hotseats_padding_top), res.getDimensionPixelSize(R.dimen.hotseats_padding_side), res.getDimensionPixelSize(R.dimen.hotseats_padding_bottom));
    }

    public void buildDrawingCache(boolean autoScale) {
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    private void saveSeats() {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        int screenCount = getScreenCount();
        for (int i = 0; i < screenCount; i++) {
            View v = getChildAt(i);
            if (v != null && (v.getTag() instanceof ItemInfo)) {
                ItemInfo info = (ItemInfo) v.getTag();
                info.cellX = i;
                ops.add(LauncherModel.makeMoveItemOperation(info, -101, 0, -1, i, 0));
            }
        }
        if (!ops.isEmpty()) {
            LauncherModel.applyBatch(this.mContext, "com.miui.home.launcher.settings", ops);
        }
        new AsyncTask<Void, Void, Boolean>() {
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                WallpaperManager wpm = (WallpaperManager) HotSeats.this.mLauncher.getSystemService("wallpaper");
                WallpaperUtils.refreshHotseatsTitleBgMode(HotSeats.this.mLauncher, wpm.getBitmap());
                wpm.forgetLoadedWallpaper();
                return Boolean.valueOf(true);
            }

            protected void onPostExecute(Boolean result) {
                if (result.booleanValue()) {
                    HotSeats.this.onWallpaperColorChanged();
                    RejectedExecutionPolicy.executeRejectedTaskIfNeeded();
                }
            }
        }.execute(new Void[0]);
    }

    public boolean isDropEnabled() {
        return true;
    }

    public boolean acceptDrop(DragObject d) {
        return isDropAllowed(d.dragSource, d.getDragInfo());
    }

    private boolean isDropAllowed(DragObject dragObject) {
        if (isDropAllowed(dragObject.dragSource, dragObject.getDragInfo())) {
            return true;
        }
        return false;
    }

    private boolean isDropAllowed(DragSource source, ItemInfo dragInfo) {
        boolean replaceSupported;
        if (!this.mIsReplaceSupported || source == this || dragInfo.container == -1) {
            replaceSupported = false;
        } else {
            replaceSupported = true;
        }
        return !this.mIsLoading && ((replaceSupported || getVisibleScreenCount() < MAX_SEATS) && acceptItem(dragInfo));
    }

    public boolean acceptItem(ItemInfo info) {
        return info.itemType == 0 || info.itemType == 1 || info.itemType == 11 || info.itemType == 2;
    }

    private int getVisibleScreenCount() {
        int count = getScreenCount();
        if (this.mLastPlaceHolder != -1) {
            return count - 1;
        }
        return count;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getLayoutParams().height = DeviceConfig.isNote720pMode() ? getContext().getResources().getDimensionPixelSize(R.dimen.note_720p_hotseats_height) : getContext().getResources().getDimensionPixelSize(R.dimen.hotseats_height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(getLayoutParams().height, 1073741824));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int i;
        Workspace ws = this.mLauncher.getWorkspace();
        int screenCount = ws.getScreenCount();
        if (this.mLauncher.isInNormalEditing()) {
            i = 2;
        } else {
            i = 1;
        }
        CellLayout cl = ws.getCellLayout(screenCount - i);
        if (cl != null) {
            setUniformLayoutModeMaxGap(cl.getCellWidthGap());
        }
        super.onLayout(changed, left, top, right, bottom);
        this.mLauncher.getDragLayer().getLocationInDragLayer(this, this.mLocation, true);
        this.mLauncher.getDragController().setDeleteRegion(new RectF((float) left, (float) top, (float) right, (float) bottom));
    }

    private int getWorkingWidth() {
        return (getWidth() - getPaddingLeft()) - getPaddingRight();
    }

    private int getSeatWidth(int seatCount) {
        return seatCount == 0 ? getWorkingWidth() : getChildScreenMeasureWidth() + getUniformLayoutModeCurrentGap();
    }

    private int getSeatPosByX(int x, int seatCount) {
        if (seatCount == 0) {
            return 0;
        }
        return Math.max(0, Math.min(((x - getScreenLayoutX(0)) + (getUniformLayoutModeCurrentGap() / 2)) / getSeatWidth(getVisibleScreenCount()), seatCount - 1));
    }

    private boolean isDropAllowed(int x, ItemInfo dragInfo) {
        View v = getChildAt(getSeatPosByX(x, getScreenCount()));
        if (v == null || !(v.getTag() instanceof FolderInfo) || dragInfo.container <= 0) {
            return true;
        }
        return false;
    }

    private void setupCellBackground(Bitmap b) {
        if (b != null) {
            this.mCellBackground.setImageBitmap(b);
            this.mCellBackground.setScaleType(ScaleType.CENTER);
        } else {
            this.mCellBackground.setImageDrawable(this.mDefaultCellBackground);
            this.mCellBackground.setScaleType(ScaleType.FIT_XY);
        }
        this.mCellBackground.setSkipNextAutoLayoutAnimation(true);
    }

    public void onDragEnter(DragObject dragObject) {
        addView(this.mCellBackground, 0);
        this.mLastPlaceHolder = 0;
        setupCellBackground(dragObject.getOutline());
    }

    private void removeCellBackground() {
        removeView(this.mCellBackground);
        this.mLastPlaceHolder = -1;
    }

    private void setReplacedViewVisible() {
        if (this.mLastReplacedPos != -1) {
            setChildVisible(this.mLastReplacedPos, 0);
            this.mLastReplacedPos = -1;
        }
    }

    public void onDragExit(DragObject dragObject) {
        if (isDropAllowed(dragObject)) {
            removeCellBackground();
            setReplacedViewVisible();
        }
    }

    private void setChildVisible(int index, int isVisible) {
        if (getChildAt(index) != null) {
            getChildAt(index).setVisibility(isVisible);
        }
    }

    public void onDragOver(DragObject dragObject) {
        if (isDropAllowed(dragObject)) {
            int pushMode = setSeats(dragObject);
            if (pushMode == -1) {
                removeCellBackground();
                setReplacedViewVisible();
            } else if (pushMode == -2) {
                setReplacedViewVisible();
                if (this.mLastPlaceHolder != this.mInsertPos) {
                    removeCellBackground();
                    addView(this.mCellBackground, this.mInsertPos);
                    this.mLastPlaceHolder = this.mInsertPos;
                }
            } else {
                removeCellBackground();
                if (this.mLastReplacedPos != this.mReplacedPos) {
                    setReplacedViewVisible();
                    setChildVisible(this.mReplacedPos, 4);
                    this.mLastReplacedPos = this.mReplacedPos;
                }
            }
        }
    }

    private int setSeats(DragObject d) {
        int i = 0;
        int visibleSeatCount = getVisibleScreenCount();
        if (visibleSeatCount == 0) {
            this.mInsertPos = 0;
            return -2;
        } else if (visibleSeatCount == MAX_SEATS) {
            this.mReplacedPos = getSeatPosByX(d.x, visibleSeatCount);
            if (d.getDragInfo().container <= 0 || !(getScreen(this.mReplacedPos) instanceof FolderIcon)) {
                return -3;
            }
            return -1;
        } else {
            int seatWidth = getSeatWidth(visibleSeatCount);
            int i2 = d.x;
            if (DeviceConfig.isLayoutRtl()) {
                i = getScreenCount() - 1;
            }
            this.mInsertPos = ((i2 - getScreenLayoutX(i)) + (getUniformLayoutModeCurrentGap() / 2)) / seatWidth;
            if (DeviceConfig.isLayoutRtl()) {
                this.mInsertPos = (getScreenCount() - 1) - this.mInsertPos;
            }
            return -2;
        }
    }

    public View getHitView() {
        return this;
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        if (!isDropAllowed(dragObject.x, dragObject.getDragInfo())) {
            return false;
        }
        ItemInfo dragInfo = dragObject.getDragInfo();
        int pushMode = setSeats(dragObject);
        if (pushMode == -1) {
            return false;
        }
        ItemIcon icon;
        removeCellBackground();
        setReplacedViewVisible();
        if (pushMode == -2) {
            icon = pushItem(dragInfo, this.mInsertPos, false, false);
        } else {
            View v = getScreen(this.mReplacedPos);
            removeView(v);
            ItemInfo changedItemInfo = null;
            if (v != null) {
                Object tag;
                if (v.getTag() instanceof ItemInfo) {
                    tag = v.getTag();
                } else {
                    tag = null;
                }
                changedItemInfo = (ItemInfo) tag;
            }
            if (changedItemInfo != null) {
                if (changedItemInfo.container != dragInfo.container) {
                    ((ItemIcon) v).setSkipNextAutoLayoutAnimation(true);
                    ((ItemIcon) v).setDockViewMode(false);
                }
                changedItemInfo.container = dragInfo.container;
                changedItemInfo.screenId = dragInfo.screenId;
                changedItemInfo.cellX = dragInfo.cellX;
                changedItemInfo.cellY = dragInfo.cellY;
                this.mLauncher.addItem(changedItemInfo, false);
                LauncherModel.moveItemInDatabase(this.mContext, changedItemInfo, changedItemInfo.container, changedItemInfo.screenId, changedItemInfo.cellX, changedItemInfo.cellY);
            }
            icon = pushItem(dragInfo, this.mReplacedPos);
        }
        dragObject.getDragView().setAnimateTarget(icon);
        saveSeats();
        this.mDraggingItem = null;
        return true;
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public void setLaucher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setDragController(DragController dragger) {
        this.mDragController = dragger;
    }

    public boolean onLongClick(View v) {
        if (Utilities.isScreenCellsLocked(this.mLauncher) || this.mIsLoading) {
            return false;
        }
        if (this.mDraggingItem != null) {
            return false;
        }
        if ((this.mDraggingItem instanceof FolderInfo) && ((FolderInfo) this.mDraggingItem).opened) {
            return false;
        }
        if (this.mLauncher.isPrivacyModeEnabled()) {
            return false;
        }
        if (this.mLauncher.isFolderShowing()) {
            return false;
        }
        if (this.mLauncher.isInNormalEditing()) {
            return false;
        }
        this.mDraggingItem = (ItemInfo) v.getTag();
        if (this.mDraggingItem == null) {
            return false;
        }
        if (this.mDragController.startDrag(v, true, this, 0)) {
            setupCellBackground(DragController.createViewBitmap(v, 1.0f));
        } else {
            this.mDraggingItem = null;
        }
        return true;
    }

    public ItemIcon pushItem(ItemInfo info, int cellX) {
        return pushItem(info, cellX, true, false);
    }

    public ItemIcon pushItem(ItemInfo info, int cellX, boolean showInstallAnim) {
        return pushItem(info, cellX, true, showInstallAnim);
    }

    public ItemIcon pushItem(ItemInfo info, int cellX, boolean checkPos, boolean showInstallAnim) {
        if (info == null || cellX < 0) {
            return null;
        }
        ItemIcon icon = this.mLauncher.createItemIcon(this, info);
        icon.setOnLongClickListener(this);
        icon.setDockViewMode(true);
        if (icon instanceof FolderIcon) {
            this.mDragController.addDropTarget((DropTarget) icon);
        }
        int screenCount = getScreenCount();
        if (screenCount == 0) {
            cellX = 0;
        } else if (checkPos) {
            for (int i = 0; i < screenCount; i++) {
                View v = getScreen(i);
                if (v != null && (v.getTag() instanceof ItemInfo) && ((ItemInfo) v.getTag()).cellX >= cellX) {
                    cellX = i;
                    break;
                }
            }
        }
        icon.setEnableAutoLayoutAnimation(!this.mIsLoading);
        addView(icon, cellX);
        if (!(icon instanceof ShortcutIcon) || !showInstallAnim) {
            return icon;
        }
        ((ShortcutIcon) icon).showInstallingAnim();
        return icon;
    }

    public int removeShortcutIcon(ShortcutIcon icon) {
        int index = indexOfChild(icon);
        removeView(icon);
        return index;
    }

    void removeShortcuts(ArrayList<ShortcutInfo> shortcuts) {
        Iterator i$ = shortcuts.iterator();
        while (i$.hasNext()) {
            ShortcutInfo sinfo = (ShortcutInfo) i$.next();
            for (int i = getScreenCount() - 1; i >= 0; i--) {
                View v = getScreen(i);
                ItemInfo info = (ItemInfo) v.getTag();
                if (!(info instanceof ShortcutInfo)) {
                    if ((info instanceof FolderInfo) && ((FolderInfo) info).remove(info.id)) {
                        break;
                    }
                } else if (info.id == sinfo.id) {
                    removeView(v);
                    break;
                }
            }
        }
    }

    public ItemIcon getItemIcon(FolderInfo fi) {
        View v = findViewWithTag(fi);
        if (v == null || !(v instanceof ItemIcon)) {
            return null;
        }
        return (ItemIcon) v;
    }

    public void startLoading() {
        removeAllViews();
        this.mIsLoading = true;
    }

    public void finishLoading() {
        this.mIsLoading = false;
        for (int i = getScreenCount() - 1; i >= 0; i--) {
            ((HostView) getScreen(i)).setEnableAutoLayoutAnimation(true);
        }
        saveSeats();
    }

    public int[] getHotseatsTitleBgMode(Launcher launcher, Bitmap wallpaper) {
        if (wallpaper == null) {
            return null;
        }
        float[] coord = new float[2];
        Utilities.getDescendantCoordRelativeToAncestor(this, launcher.getDragLayer(), coord, false, true);
        int[] hotseatsIconTitleBgMode = new int[getScreenCount()];
        for (int i = 0; i < hotseatsIconTitleBgMode.length; i++) {
            int left = Math.max(0, ((int) coord[0]) + getScreenLayoutX(i));
            int bottom = (((int) coord[1]) + getScreenLayoutY(i)) + getChildScreenMeasureHeight();
            hotseatsIconTitleBgMode[i] = WallpaperUtils.getTitleBgMode(new Rect(left, (bottom - launcher.getResources().getDimensionPixelSize(R.dimen.icon_title_padding_bottom)) - launcher.getResources().getDimensionPixelSize(R.dimen.workspace_icon_text_size), Math.min(getChildScreenMeasureWidth() + left, DeviceConfig.getScreenWidth()), bottom), wallpaper);
        }
        return hotseatsIconTitleBgMode;
    }

    public void onWallpaperColorChanged() {
        WallpaperUtils.varyViewGroupByWallpaper(this);
    }

    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        removeCellBackground();
        setReplacedViewVisible();
        if (this.mDraggingItem != null) {
            saveSeats();
        }
        this.mDraggingItem = null;
    }

    public void onDropBack(DragObject d) {
        removeCellBackground();
        pushItem(d.getDragInfo(), d.getDragInfo().cellX, false, false);
        ItemIcon icon = this.mLauncher.createItemIcon(this, d.getDragInfo());
        icon.setOnLongClickListener(this);
        d.getDragView().setAnimateTarget(icon);
    }

    public void onDropCompleted() {
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (!super.setFrame(left, top, right, bottom)) {
            return false;
        }
        disableNextAutoLayoutAnimation();
        return true;
    }

    private void disableNextAutoLayoutAnimation() {
        for (int i = getScreenCount() - 1; i >= 0; i--) {
            View v = getScreen(i);
            if (v instanceof HostView) {
                ((HostView) v).setSkipNextAutoLayoutAnimation(true);
            }
        }
    }

    public void onScreenOrientationChanged() {
    }

    public void performDropFinishAnimation(View child) {
        DeviceConfig.performDropFinishVibration(this);
    }

    public void setDropAnimating(boolean isAnimating) {
    }

    protected boolean scrolledFarEnough(MotionEvent ev) {
        return false;
    }

    protected boolean onVerticalGesture(int direction, MotionEvent event) {
        return this.mLauncher.getWorkspace().onVerticalGesture(direction, event);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mForceTouchSelectedView = null;
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child.getVisibility() == 0) {
                    child.getHitRect(this.mRect);
                    if (this.mRect.contains(x, y)) {
                        this.mForceTouchSelectedView = child;
                        break;
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public View getForceTouchSelectedView() {
        return this.mForceTouchSelectedView;
    }

    public void onForceTouchTriggered() {
        if (this.mForceTouchSelectedView != null) {
            this.mForceTouchSelectedView.setVisibility(4);
        }
    }

    public void onForceTouchFinish() {
        if (this.mForceTouchSelectedView != null) {
            this.mForceTouchSelectedView.setVisibility(0);
            this.mForceTouchSelectedView = null;
        }
    }
}
