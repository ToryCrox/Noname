package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import java.util.Iterator;

public class DragController {
    private static Canvas sTmpCanvas = new Canvas();
    private final int[] mCoordinatesTemp = new int[2];
    private RectF mDeleteRegion;
    private int mDistanceSinceScroll = 0;
    private DragObject mDragObject;
    private DragScroller mDragScroller;
    private boolean mDragging;
    private ArrayList<DropTarget> mDropTargets = new ArrayList();
    private Handler mHandler;
    private InputMethodManager mInputMethodManager;
    private boolean mIsScreenOrientationChanged = false;
    private DropTarget mLastDropTarget;
    private int[] mLastTouch = new int[2];
    private Launcher mLauncher;
    private ArrayList<DragListener> mListeners = new ArrayList();
    private int mMotionDownX;
    private int mMotionDownY;
    private View mMoveTarget;
    private final HolographicOutlineHelper mOutlineHelper = new HolographicOutlineHelper();
    private Rect mRectTemp = new Rect();
    private ScrollRunnable mScrollRunnable = new ScrollRunnable();
    private int mScrollState = 0;
    private View mScrollView;
    private int mScrollZone;
    private int mSecondaryPointerId = -1;
    private IBinder mWindowToken;

    interface DragListener {
        void onDragEnd();

        void onDragStart(DragSource dragSource, DragObject dragObject);
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (DragController.this.mDragScroller != null && DragController.this.mDragging && !DragController.this.mLauncher.isFolderShowing()) {
                if (this.mDirection == 0) {
                    DragController.this.mDragScroller.scrollDragingLeft();
                } else {
                    DragController.this.mDragScroller.scrollDragingRight();
                }
                DragController.this.mDistanceSinceScroll = 0;
                DragController.this.mHandler.postDelayed(DragController.this.mScrollRunnable, 800);
            }
        }

        void setDirection(int direction) {
            this.mDirection = direction;
        }
    }

    private class ViewHolder extends View {
        public ViewHolder(Context context) {
            super(context);
        }

        public void setMeasuredDimensionPub(int measuredWidth, int measuredHeight) {
            super.setMeasuredDimension(measuredWidth, measuredHeight);
        }
    }

    interface VisualizeCalibration {
        void getVisionOffset(int[] iArr);
    }

    public DragController(Context context) {
        this.mLauncher = (Launcher) context;
        this.mHandler = new Handler();
        this.mScrollZone = context.getResources().getDimensionPixelSize(R.dimen.scroll_zone);
    }

    public void startAutoDrag(View[] views, DragSource source, DropTarget target, int dragAction, int dropAction) {
        startAutoDrag(views, source, target, dragAction, dropAction, 0);
    }

    public void startAutoDrag(View[] views, DragSource source, DropTarget target, int dragAction, int dropAction, int baseDragViewIndex) {
        if (views.length > 0) {
            startDrag(views, false, 0.0f, source, dragAction, dropAction, true, baseDragViewIndex);
            drop(-1.0f, -1.0f, target);
            endDrag();
        }
    }

    private View createDrawableHolder(Drawable d, int left, int top) {
        ViewHolder v = new ViewHolder(this.mLauncher);
        v.setBackground(d);
        Rect bounds = d.copyBounds();
        v.setMeasuredDimensionPub(MeasureSpec.makeMeasureSpec(bounds.width(), 1073741824), MeasureSpec.makeMeasureSpec(bounds.height(), 1073741824));
        v.layout(left, top, bounds.width() + left, bounds.height() + top);
        return v;
    }

    public boolean startDrag(Drawable d, ItemInfo dragInfo, int left, int top, float scale, DragSource source, int dragAction) {
        View v = createDrawableHolder(d, left, top);
        v.setTag(dragInfo);
        if (createViewBitmap(v, 1.0f) == null) {
            return false;
        }
        return startDrag(new View[]{v}, true, scale, source, dragAction, 1, false, 0);
    }

    public boolean startDrag(View v, boolean createOutline, DragSource source, int dragAction) {
        this.mLauncher.onItemStartDragged(v);
        return startDrag(new View[]{v}, createOutline, 0.0f, source, dragAction, 1, false, 0);
    }

    public boolean startDrag(View[] views, boolean createOutline, float scale, DragSource source, int dragAction, int dropAction, boolean isSilence) {
        return startDrag(views, createOutline, scale, source, dragAction, dropAction, isSilence, 0);
    }

    public boolean startDrag(View[] views, boolean createOutline, float scale, DragSource source, int dragAction, int dropAction, boolean isAuto, int baseDragViewIndex) {
        if (this.mDragging || views.length <= 0 || Launcher.isChildrenModeEnabled()) {
            return false;
        }
        this.mLauncher.getDragLayer().setClipForDragging(null);
        if (!this.mLauncher.isInEditing()) {
            this.mLauncher.setEditingState(9);
        }
        if (this.mLauncher.isErrorBarShowing()) {
            this.mLauncher.forceHideErrorBar();
        }
        if (this.mInputMethodManager == null) {
            this.mInputMethodManager = (InputMethodManager) this.mLauncher.getSystemService("input_method");
        }
        this.mInputMethodManager.hideSoftInputFromWindow(this.mWindowToken, 0);
        boolean isMulti = views.length > 1;
        DragView[] dragViews = new DragView[views.length];
        for (int i = views.length - 1; i >= 0; i--) {
            int[] loc = this.mCoordinatesTemp;
            float initScale = this.mLauncher.getDragLayer().getLocationInDragLayer(views[i], loc, true);
            if (isAuto) {
                this.mMotionDownX = loc[0];
                this.mMotionDownY = loc[1];
            }
            dragViews[i] = createDragView(views[i], loc, createOutline, initScale, scale, dragAction, i + baseDragViewIndex, isAuto, isMulti);
            dragViews[i].show(this.mMotionDownX, this.mMotionDownY);
        }
        this.mDragObject = new DragObject(dragViews);
        this.mDragObject.xOffset = dragViews[0].getRegistrationX();
        this.mDragObject.yOffset = dragViews[0].getRegistrationY();
        this.mDragObject.dragSource = source;
        this.mDragObject.dragAction = dragAction;
        this.mDragObject.dropAction = dropAction;
        this.mDragObject.automatic = isAuto;
        if (!isAuto) {
            Iterator i$ = this.mListeners.iterator();
            while (i$.hasNext()) {
                ((DragListener) i$.next()).onDragStart(source, this.mDragObject);
            }
            DeviceConfig.performPickupStartVibration(this.mLauncher.getWorkspace());
        }
        this.mDragging = true;
        return true;
    }

    private DragView createDragView(View v, int[] location, boolean createOutline, float initScale, float finalScale, int dragAction, int index, boolean isAuto, boolean isMulti) {
        int i;
        v.setPressed(false);
        ItemInfo dragInfo = (ItemInfo) (v.getTag() instanceof ItemInfo ? v.getTag() : null);
        Bitmap outline = null;
        if (createOutline) {
            outline = createViewBitmap(v, 1.0f);
            if (outline == null) {
                return null;
            }
        }
        switch (dragAction) {
            case 0:
                if (v.getParent() != null) {
                    v.clearAnimation();
                    ((ViewGroup) v.getParent()).removeView(v);
                    break;
                }
                break;
            case 1:
                v.setVisibility(4);
                break;
            case 3:
                v.setAlpha(0.2f);
                break;
        }
        int registrationX = this.mMotionDownX - location[0];
        int registrationY = this.mMotionDownY - location[1];
        if (isMulti) {
            registrationX = v.getMeasuredWidth() / 2;
            registrationY = v.getMeasuredHeight() / 2;
        }
        ViewGroup dragLayer = this.mLauncher.getDragLayer();
        int i2 = this.mMotionDownX - location[0];
        int i3 = this.mMotionDownY - location[1];
        int i4 = this.mMotionDownX;
        int i5 = this.mMotionDownY;
        if (isAuto) {
            i = 0;
        } else {
            i = 1;
        }
        return new DragView(dragLayer, v, outline, dragInfo, i2, i3, i4, i5, registrationX, registrationY, initScale, finalScale, index, isMulti & i);
    }

    public boolean isDragging() {
        return this.mDragging;
    }

    public static Bitmap createViewBitmap(View v, float scale) {
        v.clearFocus();
        Bitmap snapShot = Utilities.createBitmapSafely(Math.round(((float) v.getWidth()) * scale), Math.round(((float) v.getHeight()) * scale), Config.ARGB_8888);
        if (snapShot != null) {
            Canvas canvas = sTmpCanvas;
            int saveCount = canvas.save(1);
            canvas.setBitmap(snapShot);
            canvas.scale(scale, scale);
            v.draw(canvas);
            canvas.setBitmap(null);
            canvas.restoreToCount(saveCount);
        }
        return snapShot;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.mDragging;
    }

    public void cancelDrag() {
        cancelScroll();
        if (this.mDragging) {
            if (this.mLastDropTarget != null) {
                this.mLastDropTarget.onDragExit(this.mDragObject);
            }
            this.mDragObject.onDragCompleted(true);
            this.mDragObject.dragSource.onDragCompleted(null, this.mDragObject);
        }
        endDrag();
    }

    public void cancelScroll() {
        this.mHandler.removeCallbacks(this.mScrollRunnable);
    }

    private void endDrag() {
        if (this.mDragging) {
            this.mDragging = false;
            if (!this.mDragObject.automatic) {
                Iterator i$ = this.mListeners.iterator();
                while (i$.hasNext()) {
                    ((DragListener) i$.next()).onDragEnd();
                }
            }
            this.mDragObject = null;
        }
        if (9 == this.mLauncher.getEditingState()) {
            this.mLauncher.setEditingState(7);
        }
        this.mLauncher.onItemEndDragged();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        int screenX = clamp((int) ev.getRawX(), 0, DeviceConfig.getScreenWidth());
        int screenY = clamp((int) ev.getRawY(), 0, DeviceConfig.getScreenHeight());
        boolean isDragging = this.mDragging;
        switch (action) {
            case 0:
                this.mMotionDownX = screenX;
                this.mMotionDownY = screenY;
                this.mLastDropTarget = null;
                break;
            case 1:
                if (this.mDragging) {
                    handleMoveEvent(screenX, screenY, ev);
                    drop((float) screenX, (float) screenY);
                }
                endDrag();
                break;
            case 3:
                cancelDrag();
                break;
        }
        return isDragging;
    }

    void setMoveTarget(View view) {
        this.mMoveTarget = view;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return this.mMoveTarget != null && this.mMoveTarget.dispatchUnhandledMove(focused, direction);
    }

    private void cleanLastDropTarget() {
        if (this.mLastDropTarget != null) {
            this.mLastDropTarget.onDragExit(this.mDragObject);
            this.mLastDropTarget = null;
        }
    }

    private void handleMoveEvent(int x, int y, MotionEvent ev) {
        this.mDragObject.move(x, y);
        int[] coordinates = this.mCoordinatesTemp;
        DropTarget dropTarget = findDropTarget(x, y, coordinates);
        this.mDragObject.x = coordinates[0];
        this.mDragObject.y = coordinates[1];
        if (dropTarget != null) {
            DropTarget delegate = dropTarget.getDropTargetDelegate(this.mDragObject);
            if (delegate != null) {
                dropTarget = delegate;
            }
            if (dropTarget.acceptDrop(this.mDragObject)) {
                if (this.mLastDropTarget != dropTarget) {
                    cleanLastDropTarget();
                    dropTarget.onDragEnter(this.mDragObject);
                    this.mLastDropTarget = dropTarget;
                }
                dropTarget.onDragOver(this.mDragObject);
            } else {
                cleanLastDropTarget();
            }
        } else {
            cleanLastDropTarget();
        }
        boolean inDeleteRegion = false;
        if (this.mDeleteRegion != null) {
            inDeleteRegion = this.mDeleteRegion.contains((float) x, (float) y);
        }
        int slop = ViewConfiguration.get(this.mLauncher).getScaledWindowTouchSlop();
        this.mDistanceSinceScroll = (int) (((double) this.mDistanceSinceScroll) + Math.sqrt(Math.pow((double) (this.mLastTouch[0] - x), 2.0d) + Math.pow((double) (this.mLastTouch[1] - y), 2.0d)));
        this.mLastTouch[0] = x;
        this.mLastTouch[1] = y;
        if (inDeleteRegion || x >= this.mScrollZone) {
            if (inDeleteRegion || x <= this.mScrollView.getWidth() - this.mScrollZone) {
                if (this.mScrollState == 1) {
                    this.mScrollState = 0;
                    cancelScroll();
                    this.mDragScroller.onExitScrollArea();
                } else if (ev != null && this.mSecondaryPointerId > 0) {
                    if (ev.findPointerIndex(this.mSecondaryPointerId) > 0) {
                        if (Math.abs(((float) x) - ev.getX(ev.findPointerIndex(this.mSecondaryPointerId))) > 1.0f) {
                            cancelScroll();
                            this.mDragScroller.onSecondaryPointerMove(ev, this.mSecondaryPointerId);
                            return;
                        }
                        return;
                    }
                    this.mSecondaryPointerId = -1;
                }
            } else if (this.mScrollState == 0 && this.mDistanceSinceScroll > slop) {
                this.mScrollState = 1;
                if (this.mDragScroller.onEnterScrollArea(x, y, 1)) {
                    this.mScrollRunnable.setDirection(1);
                    cancelScroll();
                    this.mHandler.postDelayed(this.mScrollRunnable, 1000);
                }
            }
        } else if (this.mScrollState == 0 && this.mDistanceSinceScroll > slop) {
            this.mScrollState = 1;
            if (this.mDragScroller.onEnterScrollArea(x, y, 0)) {
                this.mScrollRunnable.setDirection(0);
                cancelScroll();
                this.mHandler.postDelayed(this.mScrollRunnable, 1000);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        View scrollView = this.mScrollView;
        if (!this.mDragging) {
            return false;
        }
        int action = ev.getAction();
        int screenX = clamp((int) ev.getRawX(), 0, DeviceConfig.getScreenWidth());
        int screenY = clamp((int) ev.getRawY(), 0, DeviceConfig.getScreenHeight());
        switch (action & 255) {
            case 0:
                this.mMotionDownX = screenX;
                this.mMotionDownY = screenY;
                if (screenX >= this.mScrollZone && screenX <= scrollView.getWidth() - this.mScrollZone) {
                    this.mScrollState = 0;
                    break;
                }
                this.mScrollState = 1;
                cancelScroll();
                this.mHandler.postDelayed(this.mScrollRunnable, 1000);
                break;
            case 1:
                handleMoveEvent(screenX, screenY, ev);
                cancelScroll();
                if (this.mDragging) {
                    drop((float) screenX, (float) screenY);
                }
                endDrag();
                break;
            case 2:
                handleMoveEvent(screenX, screenY, ev);
                break;
            case 3:
                cancelDrag();
                break;
            case 5:
                this.mSecondaryPointerId = ev.getPointerId((65280 & action) >> 8);
                this.mDragScroller.onSecondaryPointerDown(ev, this.mSecondaryPointerId);
                break;
            case 6:
                if (this.mSecondaryPointerId >= 0) {
                    this.mDragScroller.onSecondaryPointerUp(ev, this.mSecondaryPointerId);
                    this.mSecondaryPointerId = -1;
                    break;
                }
                break;
        }
        return true;
    }

    private void drop(float x, float y) {
        if (this.mLauncher.isFolderAnimating()) {
            cancelDrag();
        } else {
            drop(x, y, null);
        }
    }

    private void drop(float x, float y, DropTarget dropTarget) {
        if (dropTarget == null) {
            int[] coordinates = this.mCoordinatesTemp;
            dropTarget = findDropTarget((int) x, (int) y, coordinates);
            this.mDragObject.x = coordinates[0];
            this.mDragObject.y = coordinates[1];
        } else {
            this.mDragObject.x = (int) x;
            this.mDragObject.y = (int) y;
        }
        if (dropTarget != null) {
            dropTarget.onDropStart(this.mDragObject);
            boolean succeeded;
            do {
                succeeded = false;
                if (dropTarget.acceptDrop(this.mDragObject)) {
                    boolean needRecord;
                    ItemInfo backup = null;
                    if (this.mDragObject.getDragInfo() != null) {
                        backup = this.mDragObject.getDragInfo().clone();
                    }
                    succeeded = dropTarget.onDrop(this.mDragObject);
                    if ((this.mDragObject.getDragInfo() instanceof ShortcutInfo) || (this.mDragObject.getDragInfo() instanceof FolderInfo)) {
                        needRecord = true;
                    } else {
                        needRecord = false;
                    }
                    if (needRecord && succeeded && backup != null && !Launcher.hasSamePosition(backup, this.mDragObject.getDragInfo())) {
                        String moveType = this.mDragObject.dragSource instanceof MultiSelectContainerView ? "multi_select" : "normal";
                        if (dropTarget instanceof Workspace) {
                            AnalyticalDataCollector.trackItemMoved("item_be_moved_to_workspace", moveType);
                        } else if (dropTarget instanceof HotSeats) {
                            AnalyticalDataCollector.trackItemMoved("item_be_moved_to_hotseats", moveType);
                        }
                    }
                }
            } while (this.mDragObject.nextDragView(succeeded));
            this.mDragObject.onDragCompleted();
            dropTarget.onDropCompleted();
            dropTarget.onDragExit(this.mDragObject);
        } else {
            this.mDragObject.onDragCompleted();
        }
        if (this.mDragObject.dragSource != null) {
            this.mDragObject.dragSource.onDragCompleted(dropTarget, this.mDragObject);
        }
        if (dropTarget != this.mLastDropTarget) {
            cleanLastDropTarget();
        }
    }

    private DropTarget findDropTarget(int x, int y, int[] dropCoordinates) {
        Rect r = this.mRectTemp;
        ArrayList<DropTarget> dropTargets = this.mDropTargets;
        for (int i = dropTargets.size() - 1; i >= 0; i--) {
            DropTarget target = (DropTarget) dropTargets.get(i);
            if (target.isDropEnabled() && ((View) target).isShown()) {
                target.getHitView().getHitRect(r);
                float scale = this.mLauncher.getDragLayer().getLocationInDragLayer(target.getHitView(), dropCoordinates, false);
                r.set(dropCoordinates[0], dropCoordinates[1], (int) (((float) dropCoordinates[0]) + (((float) r.width()) * scale)), (int) (((float) dropCoordinates[1]) + (((float) r.height()) * scale)));
                if (r.contains(x, y)) {
                    dropCoordinates[0] = (int) (((float) dropCoordinates[0]) - ((((float) ((View) target).getWidth()) * (1.0f - scale)) / 2.0f));
                    dropCoordinates[1] = (int) (((float) dropCoordinates[1]) - ((((float) ((View) target).getHeight()) * (1.0f - scale)) / 2.0f));
                    dropCoordinates[0] = x - dropCoordinates[0];
                    dropCoordinates[1] = y - dropCoordinates[1];
                    return target;
                }
            }
        }
        return null;
    }

    private static int clamp(int val, int min, int max) {
        if (val < min) {
            return min;
        }
        return val >= max ? max - 1 : val;
    }

    public void setDragScoller(DragScroller scroller) {
        this.mDragScroller = scroller;
    }

    public void setWindowToken(IBinder token) {
        this.mWindowToken = token;
    }

    public void addDragListener(DragListener l) {
        this.mListeners.add(l);
    }

    public void removeDragListener(DragListener l) {
        this.mListeners.remove(l);
    }

    public void addDropTarget(DropTarget target) {
        this.mDropTargets.add(target);
    }

    public void addDropTarget(int index, DropTarget target) {
        this.mDropTargets.add(index, target);
    }

    public void removeDropTarget(DropTarget target) {
        this.mDropTargets.remove(target);
        if (this.mDragging && this.mLastDropTarget == target) {
            this.mLastDropTarget.onDragExit(this.mDragObject);
            this.mLastDropTarget = null;
        }
    }

    public void setScrollView(View v) {
        this.mScrollView = v;
    }

    void setDeleteRegion(RectF region) {
        this.mDeleteRegion = region;
    }

    public void setIsScreenOrientationChanged(boolean isScreenOrientationChanged) {
        this.mIsScreenOrientationChanged = isScreenOrientationChanged;
    }

    public boolean getIsScreenOrientationChanged() {
        return this.mIsScreenOrientationChanged;
    }
}
