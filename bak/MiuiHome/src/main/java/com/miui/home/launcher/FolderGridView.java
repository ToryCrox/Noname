package com.miui.home.launcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.ListAdapter;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.common.PreciseClickConfirmor;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class FolderGridView extends GridView implements VisualizeCalibration, DropTarget {
    private ShortcutsAdapter mAdapter = null;
    private int mAutoScrollDirection;
    private HashMap<DragView, BackupDataForDragging> mBackupDataForDragIn = new HashMap();
    private PreciseClickConfirmor mClickConfirmor;
    Runnable mConfirmAutoScroll = new Runnable() {
        public void run() {
            if (FolderGridView.this.mAutoScrollDirection != 0) {
                FolderGridView.this.smoothScrollBy(FolderGridView.this.mAutoScrollDirection * 4, 2);
                FolderGridView.this.postDelayed(FolderGridView.this.mConfirmAutoScroll, 2);
            }
        }
    };
    private int mEdgeAlpha = 255;
    private Paint mEdgePaint;
    private View mForceTouchSelectedView;
    private HashMap<ItemInfo, DragView> mItemsForDropping = new HashMap();
    private View mLastHit = null;
    private HashMap<ShortcutInfo, Rect> mLastPosMap;
    private Launcher mLauncher;
    private Rect mRect = new Rect();
    private Runnable mStayConfirm = new Runnable() {
        public void run() {
            FolderGridView.this.reorderItems();
        }
    };
    private Rect mTmpRect = new Rect();
    private boolean usingDarkScrollBar = false;

    private static class BackupDataForDragging {
        public int cellX;
        public int cellY;
        public long container;

        private BackupDataForDragging() {
        }
    }

    public FolderGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(false);
        this.mClickConfirmor = new PreciseClickConfirmor(context);
        this.mEdgePaint = new Paint();
        this.mEdgePaint.setStrokeWidth((float) context.getResources().getDimensionPixelSize(R.dimen.folder_edge_height));
        int color = context.getResources().getColor(R.color.folder_edge);
        this.mEdgePaint.setColor(color);
        this.mEdgeAlpha = (-16777216 & color) >> 24;
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof ShortcutsAdapter) {
            this.mAdapter = (ShortcutsAdapter) adapter;
            super.setAdapter(adapter);
            clearDisappearingChildren();
            return;
        }
        throw new RuntimeException("adapter must be:" + ShortcutsAdapter.class.getName());
    }

    public void adapterScrollBar() {
        TypedArray a;
        if ((this.mLauncher.isInNormalEditing() || Launcher.isChildrenModeEnabled()) && WallpaperUtils.hasAppliedLightWallpaper()) {
            if (!this.usingDarkScrollBar) {
                a = this.mLauncher.obtainStyledAttributes(R.style.folder_scrollbar_vertical_dark, com.android.internal.R.styleable.View);
                initializeScrollbars(a);
                a.recycle();
                this.usingDarkScrollBar = true;
            }
        } else if (this.usingDarkScrollBar) {
            a = this.mLauncher.obtainStyledAttributes(R.style.folder_scrollbar_vertical, com.android.internal.R.styleable.View);
            initializeScrollbars(a);
            a.recycle();
            this.usingDarkScrollBar = false;
        }
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (isEnabled()) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    public View getHitView() {
        return this;
    }

    public boolean isDropEnabled() {
        return true;
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        ShortcutInfo info = (ShortcutInfo) dragObject.getDragInfo();
        this.mItemsForDropping.put(info, dragObject.getDragView());
        boolean isAutoDrag = dragObject.dropAction != 1;
        if (isAutoDrag) {
            info.cellX = getLastCellX() + 1;
            this.mAdapter.mDroppingDragViews.put(info, dragObject.getDragView());
            this.mAdapter.add(info);
            if (getCount() % getNumColumns() == 1) {
                Launcher.performLayoutNow(this.mLauncher.getFolderCling());
            } else {
                layoutChildren();
            }
        }
        View finalView = findViewWithTag(info);
        final ShortcutInfo shortcutInfo = info;
        Runnable animatorEndCallback = new Runnable() {
            public void run() {
                FolderGridView.this.mAdapter.mDroppingDragViews.remove(shortcutInfo);
            }
        };
        DragView dragView = dragObject.getDragView();
        if (finalView == null) {
            finalView = this;
            dragView.setFakeTargetMode();
        }
        dragView.setAnimateTarget(finalView);
        dragView.setOnAnimationEndCallback(animatorEndCallback);
        if (isAutoDrag) {
            dragView.setPivotX(0.0f);
            dragView.setPivotX(0.0f);
        }
        if (dragObject.isLastObject()) {
            View firstView = getChildAt(0);
            int itemHeight = firstView.getHeight();
            int itemWidth = firstView.getWidth();
            float[] loc = new float[2];
            float scale = Utilities.getDescendantCoordRelativeToAncestor(this, this.mLauncher.getDragLayer(), loc, true, false);
            float childrenLeft = loc[0] + (((float) getPaddingLeft()) * scale);
            float childrenTop = loc[1] + (((float) getPaddingTop()) * scale);
            float childrenBottom = loc[1] + (((float) (getHeight() - getPaddingBottom())) * scale);
            float targetX;
            float targetY;
            if (isAutoDrag) {
                this.mLauncher.getDragLayer().setClipForDragging(new Rect(0, (int) childrenTop, DeviceConfig.getScreenWidth(), DeviceConfig.getScreenHeight()));
                int rowCount = ((this.mAdapter.getCount() - 1) / getNumColumns()) + 1;
                for (Entry<ItemInfo, DragView> backup : this.mItemsForDropping.entrySet()) {
                    int column;
                    int cellX = ((ItemInfo) backup.getKey()).cellX;
                    if (DeviceConfig.isLayoutRtl()) {
                        column = (getNumColumns() - 1) - (cellX % getNumColumns());
                    } else {
                        column = cellX % getNumColumns();
                    }
                    targetX = childrenLeft + ((((float) ((getColumnWidth() + getHorizontalSpacing()) * column)) + (((float) (getColumnWidth() - itemWidth)) / 2.0f)) * scale);
                    targetY = Math.min(childrenTop + (((float) ((cellX / getNumColumns()) * (getVerticalSpacing() + itemHeight))) * scale), childrenBottom - (((float) ((((rowCount - (cellX / getNumColumns())) - 1) * (getVerticalSpacing() + itemHeight)) + itemHeight)) * scale));
                    ((DragView) backup.getValue()).setScaleTarget(scale);
                    ((DragView) backup.getValue()).updateAnimateTarget(new float[]{targetX - ((((float) dragObject.getDragView().getWidth()) * (1.0f - scale)) / 2.0f), targetY - ((((float) dragObject.getDragView().getHeight()) * (1.0f - scale)) / 2.0f)});
                }
                this.mItemsForDropping.clear();
                post(new Runnable() {
                    public void run() {
                        int lastPosition = FolderGridView.this.mAdapter.getCount() - 1;
                        if ((lastPosition - FolderGridView.this.getLastVisiblePosition()) / FolderGridView.this.getNumColumns() > 6) {
                            FolderGridView.this.setSelection(lastPosition);
                        } else {
                            FolderGridView.this.smoothScrollToPosition(lastPosition);
                        }
                    }
                });
            } else {
                this.mLauncher.getDragLayer().setClipForDragging(new Rect(0, 0, DeviceConfig.getScreenWidth(), (int) childrenBottom));
                for (int i = 0; i < getChildCount(); i++) {
                    View v = getChildAt(i);
                    if (this.mItemsForDropping.containsKey(v.getTag())) {
                        if ((v instanceof HostView) && ((HostView) v).getGhostView() != null) {
                            ((HostView) v).getGhostView().updateAnimateTarget(v);
                        }
                        this.mItemsForDropping.remove(v.getTag());
                    }
                }
                if (this.mItemsForDropping.size() > 0) {
                    int[] firstLoc = new int[2];
                    firstView.getLocationInWindow(firstLoc);
                    for (Entry<ItemInfo, DragView> backup2 : this.mItemsForDropping.entrySet()) {
                        targetX = (float) (((((ItemInfo) backup2.getKey()).cellX % getNumColumns()) * (getHorizontalSpacing() + itemWidth)) + firstLoc[0]);
                        targetY = (float) (firstLoc[0] - (((getFirstVisiblePosition() / getNumColumns()) - (((ItemInfo) backup2.getKey()).cellX / getNumColumns())) * (getVerticalSpacing() + itemHeight)));
                        ((DragView) backup2.getValue()).updateAnimateTarget(new float[]{targetX, targetY});
                    }
                    this.mItemsForDropping.clear();
                }
            }
            this.mAdapter.saveContentPosition();
        }
        return true;
    }

    private void saveBackupDataForDragging(final DragObject dragObject) {
        do {
            this.mBackupDataForDragIn.put(dragObject.getDragView(), new BackupDataForDragging() {
            });
        } while (dragObject.nextDragView(false));
    }

    private void restoreBackupDataForDragging(DragObject dragObject) {
        do {
            BackupDataForDragging data = (BackupDataForDragging) this.mBackupDataForDragIn.get(dragObject.getDragView());
            ItemInfo itemInfo = dragObject.getDragInfo();
            itemInfo.cellX = data.cellX;
            itemInfo.cellY = data.cellY;
            itemInfo.container = data.container;
        } while (dragObject.nextDragView(false));
    }

    private int getLastCellX() {
        if (this.mAdapter.getCount() == 0) {
            return 0;
        }
        return this.mAdapter.getItem(this.mAdapter.getCount() - 1).cellX;
    }

    private void makePositionSnapShot() {
        if (this.mLastPosMap == null) {
            this.mLastPosMap = new HashMap();
            for (int i = 0; i < getChildCount(); i++) {
                Rect r = new Rect();
                View v = getChildAt(i);
                if (v.getVisibility() == 0) {
                    v.getHitRect(r);
                    this.mLastPosMap.put((ShortcutInfo) v.getTag(), r);
                }
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int i;
        super.onLayout(changed, l, t, r, b);
        if (this.mLastPosMap != null) {
            for (i = 0; i < getChildCount(); i++) {
                View v = getChildAt(i);
                if (v.getVisibility() == 0) {
                    ShortcutInfo info = (ShortcutInfo) v.getTag();
                    Animation a;
                    if (this.mLastPosMap.containsKey(info)) {
                        Rect rect = (Rect) this.mLastPosMap.get(info);
                        if (rect.left != v.getLeft() || rect.top != v.getTop()) {
                            a = new TranslateAnimation((float) (rect.left - v.getLeft()), 0.0f, (float) (rect.top - v.getTop()), 0.0f);
                            a.setDuration(300);
                            v.startAnimation(a);
                        }
                    } else if (i == getChildCount() - 1) {
                        a = new TranslateAnimation((float) (-((v.getWidth() + getHorizontalSpacing()) * (getNumColumns() - 1))), 0.0f, (float) (v.getHeight() + getVerticalSpacing()), 0.0f);
                        a.setDuration(300);
                        v.startAnimation(a);
                    } else if (i <= getNumColumns() - 1) {
                        a = new TranslateAnimation(0.0f, 0.0f, (float) ((-v.getHeight()) - getVerticalSpacing()), 0.0f);
                        a.setDuration(300);
                        v.startAnimation(a);
                    }
                }
            }
            this.mLastPosMap = null;
        }
        if (getAdapter() != null && (getAdapter() instanceof ShortcutsAdapter)) {
            ShortcutInfo shortcutInfo = ((ShortcutsAdapter) getAdapter()).getForceTouchSelectedShortcutInfo();
            for (i = 0; i < getChildCount(); i++) {
                if (getChildAt(i).getTag() == shortcutInfo) {
                    this.mForceTouchSelectedView = getChildAt(i);
                    return;
                }
            }
        }
    }

    private void reorderItems() {
        if (this.mLastHit == null || this.mAdapter.mFirstDragItem != this.mLastHit.getTag()) {
            makePositionSnapShot();
            this.mAdapter.reorderItemByInsert(this.mLastHit == null ? null : (ShortcutInfo) this.mLastHit.getTag());
        }
    }

    public void onDragEnter(DragObject dragObject) {
        this.mAdapter.disableSaveWhenDatasetChanged(true);
        saveBackupDataForDragging(dragObject);
        appendDragObjectForDragEnter(dragObject);
        if (checkNearestViewByDrag(dragObject)) {
            reorderItems();
        }
    }

    private void appendDragObjectForDragEnter(DragObject dragObject) {
        boolean exist = this.mAdapter.getFolderInfo().contains((ShortcutInfo) dragObject.getDragInfo());
        List<ShortcutInfo> list = new ArrayList();
        int lastCellX = getLastCellX();
        int i = 0;
        do {
            ShortcutInfo info = (ShortcutInfo) dragObject.getDragInfo();
            if (i == 0) {
                this.mAdapter.mFirstDragItem = info;
            }
            if (!exist) {
                info.cellX = (lastCellX + i) + 1;
                list.add(info);
            }
            this.mAdapter.mDragOverItems.add((ShortcutInfo) dragObject.getDragInfo());
            i++;
        } while (dragObject.nextDragView(false));
        this.mAdapter.addAll(list);
        layoutChildren();
    }

    private void removeAutoScroll() {
        Handler h = getHandler();
        if (h != null) {
            h.removeCallbacks(this.mConfirmAutoScroll);
        }
    }

    public void onDragOver(DragObject dragObject) {
        if ((getTranslationY() + ((float) getHeight())) - ((float) dragObject.y) < ((float) getHeight()) * 0.25f) {
            if (this.mAutoScrollDirection != 1) {
                removeAutoScroll();
                this.mAutoScrollDirection = 1;
                post(this.mConfirmAutoScroll);
            }
        } else if (((float) dragObject.y) - getTranslationY() >= ((float) getHeight()) * 0.25f) {
            this.mAutoScrollDirection = 0;
            removeAutoScroll();
        } else if (this.mAutoScrollDirection != -1) {
            removeAutoScroll();
            this.mAutoScrollDirection = -1;
            post(this.mConfirmAutoScroll);
        }
        if (checkNearestViewByDrag(dragObject)) {
            getHandler().removeCallbacks(this.mStayConfirm);
            postDelayed(this.mStayConfirm, 300);
        }
    }

    private boolean checkNearestViewByDrag(DragObject dragObject) {
        View hitView = null;
        float minDistanceFactor = Float.MAX_VALUE;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.getHitRect(this.mTmpRect);
            float distanceFactor = (float) (Math.pow((double) (this.mTmpRect.centerX() - dragObject.x), 2.0d) + Math.pow((double) (this.mTmpRect.centerY() - dragObject.y), 2.0d));
            if (distanceFactor < minDistanceFactor) {
                minDistanceFactor = distanceFactor;
                hitView = v;
            }
        }
        if (hitView == null || hitView == this.mLastHit) {
            return false;
        }
        this.mLastHit = hitView;
        return true;
    }

    public void onDragExit(DragObject dragObject) {
        this.mAdapter.disableSaveWhenDatasetChanged(false);
        this.mAutoScrollDirection = 0;
        removeAutoScroll();
        if (!dragObject.isDroped()) {
            restoreBackupDataForDragging(dragObject);
        }
        this.mBackupDataForDragIn.clear();
        this.mLastHit = null;
        this.mLastPosMap = null;
        makePositionSnapShot();
        getHandler().removeCallbacks(this.mStayConfirm);
        if (dragObject.getDragView().isDropSucceeded()) {
            this.mAdapter.mDragOverItems.clear();
            this.mAdapter.notifyDataSetChanged();
        } else {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                getChildAt(i).setAlpha(1.0f);
            }
            this.mAdapter.removeAllDrags();
        }
        this.mAdapter.mFirstDragItem = null;
        Launcher.performLayoutNow(getRootView());
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean r = super.onTouchEvent(ev);
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
                        if (getAdapter() != null && (getAdapter() instanceof ShortcutsAdapter)) {
                            ((ShortcutsAdapter) getAdapter()).setForceTouchSelectedShortcutInfo((ShortcutInfo) child.getTag());
                        }
                    }
                }
            }
        }
        this.mClickConfirmor.onTouchEvent(ev);
        if (ev.getAction() == 1 && this.mClickConfirmor.confirmClick() && -1 == pointToPosition((int) ev.getX(), (int) ev.getY()) && !this.mLauncher.isInNormalEditing() && getScaleY() == 1.0f) {
            ViewGroup p = this;
            do {
                p = (ViewGroup) p.getParent();
                if (p == null) {
                    break;
                }
            } while (!(p instanceof Folder));
            ((Folder) p).performClick();
        }
        return r;
    }

    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }
        int top = getChildAt(0).getTop();
        return (top >= this.mPaddingTop || top <= (-this.mPaddingTop)) ? 1.0f : ((float) (-(top - this.mPaddingTop))) / ((float) (this.mPaddingTop * 2));
    }

    protected float getBottomFadingEdgeStrength() {
        int count = getChildCount();
        if (count == 0) {
            return 0.0f;
        }
        int bottom = getChildAt(count - 1).getBottom();
        int height = getHeight();
        return (bottom <= height - this.mPaddingBottom || bottom >= this.mPaddingBottom + height) ? 1.0f : ((float) ((bottom - height) + this.mPaddingBottom)) / ((float) (this.mPaddingBottom * 2));
    }

    protected void onDraw(Canvas canvas) {
        boolean isDraggingItem;
        if (this.mLauncher.getFolderCling().getFolder().getDragedItem() != null) {
            isDraggingItem = true;
        } else {
            isDraggingItem = false;
        }
        if (!isDraggingItem && getChildCount() > 0) {
            if (getChildAt(0).getTop() < 0 && !this.mLauncher.isInNormalEditing()) {
                this.mEdgePaint.setAlpha((int) (((float) this.mEdgeAlpha) * getTopFadingEdgeStrength()));
                canvas.drawLine(0.0f, 0.0f, (float) getWidth(), 0.0f, this.mEdgePaint);
            }
            if (getChildAt(getChildCount() - 1).getBottom() > getHeight() && !this.mLauncher.isInNormalEditing()) {
                this.mEdgePaint.setAlpha((int) (((float) this.mEdgeAlpha) * getBottomFadingEdgeStrength()));
                canvas.drawLine(0.0f, (float) getHeight(), (float) getWidth(), (float) getHeight(), this.mEdgePaint);
            }
        }
        super.onDraw(canvas);
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        return (dragObject.getDragInfo().itemType == 0 || dragObject.getDragInfo().itemType == 1 || dragObject.getDragInfo().itemType == 11) && dragObject.getDragInfo().container != -1;
    }

    public void onDropCompleted() {
    }

    public void removeView(View child) {
        ShortcutInfo info = (ShortcutInfo) child.getTag();
        makePositionSnapShot();
        this.mAdapter.remove(info);
        detachViewFromParent(child);
        removeDetachedView(child, false);
    }

    public void getVisionOffset(int[] offset) {
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            if (firstView instanceof VisualizeCalibration) {
                ((VisualizeCalibration) firstView).getVisionOffset(offset);
            }
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    public View getForceTouchSelectedView() {
        return this.mForceTouchSelectedView;
    }
}
