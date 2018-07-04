package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteException;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView.ScaleType;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.DragView.DropTargetContainer;
import com.miui.home.launcher.Launcher.IconContainer;
import com.miui.home.launcher.OnLongClickAgent.VersionTagGenerator;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.GadgetInfo;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

public class CellLayout extends ViewGroup implements DropTargetContainer, ForceTouchTriggeredListener, IconContainer, VersionTagGenerator, WallpaperColorChangedListener {
    static final /* synthetic */ boolean $assertionsDisabled;
    private static final int SHAKE_OFFSET = Utilities.getDipPixelSize(1.5f);
    private static int[] sShakeAnimScaler = null;
    private CellBackground mCellBackground;
    private int mCellHeight;
    private final CellInfo mCellInfo;
    private int mCellPaddingLeft;
    private int mCellPaddingRight;
    private int mCellPaddingTop;
    private int mCellWidth;
    private int[] mCellXIterateRange;
    private int[] mCellXY;
    private Drawable mDefaultCellBackground;
    private boolean mDisableTouch;
    private int mDropAnimationCounter;
    private int[] mDstPos;
    private ValueAnimator mEmptyCellMarkAnimator;
    private BitmapDrawable mEmptyCellMarkDrawable;
    private int mEmptyCellMarkDrawableAlpha;
    private LayoutParams mEmptyCellMarkLP;
    private int mEmptyCellMarkStartAlpha;
    private int mHCells;
    private int mHeightGap;
    private boolean mIsShowEmptyCellMark;
    private DropTarget mLastCoveringView;
    private boolean mLastDownOnOccupiedCell;
    DragPos mLastDragPos;
    private long mLastRelayoutTime;
    private Launcher mLauncher;
    private boolean mLayoutBackupValid;
    private ValueAnimator mMoveAwayShakeAnim;
    private ArrayList<View> mMoveAwayViewList;
    private View[][] mOccupiedCell;
    private View[][] mOccupiedCellBak;
    private OnLongClickAgent mOnLongClickAgent;
    private float[] mRandomOffset;
    private final Rect mRect;
    private Rect mRectTmp;
    private ValueAnimator mShakeAnim;
    private View mShakeSource;
    private StayConfirm mStayConfirm;
    private int mStayConfirmSize;
    private int[] mTmpCellLR;
    DragPos mTmpDragPos;
    private int[] mTmpXY;
    private int mTotalCells;
    private int mVCells;
    private final WallpaperManager mWallpaperManager;
    private int mWidgetCellHeight;
    private int mWidgetCellPaddingBottom;
    private int mWidgetCellPaddingLeft;
    private int mWidgetCellPaddingRight;
    private int mWidgetCellPaddingTop;
    private int mWidgetCellWidth;
    private int mWidthGap;

    static final class CellInfo implements ContextMenuInfo {
        View cell;
        int cellX;
        int cellY;
        int container;
        long screenId = -1;
        int screenType = -1;
        int spanX;
        int spanY;

        CellInfo() {
        }

        public String toString() {
            return "Cell[view=" + (this.cell == null ? "null" : this.cell.getClass()) + ", x=" + this.cellX + ", y=" + this.cellY + "]";
        }
    }

    class DragPos {
        int[] cellXY = new int[2];
        int stayType = 5;

        public DragPos() {
            reset();
        }

        void reset() {
            int[] iArr = this.cellXY;
            this.cellXY[1] = -1;
            iArr[0] = -1;
            this.stayType = 5;
        }

        void set(DragPos d) {
            this.cellXY[0] = d.cellXY[0];
            this.cellXY[1] = d.cellXY[1];
            this.stayType = d.stayType;
        }

        boolean equal(DragPos d) {
            return this.cellXY[0] == d.cellXY[0] && this.cellXY[1] == d.cellXY[1] && this.stayType == d.stayType;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        long accessTag;
        boolean dropped;
        public boolean isDragging;
        @ExportedProperty
        int x;
        @ExportedProperty
        int y;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams() {
            super(-1, -1);
        }

        public void setup(int cellX, int cellY, int spanX, int spanY, int cellWidth, int cellHeight, int widthGap, int heightGap, int hStartPadding, int vStartPadding) {
            this.width = (((spanX * cellWidth) + ((spanX - 1) * widthGap)) - this.leftMargin) - this.rightMargin;
            this.height = (((spanY * cellHeight) + ((spanY - 1) * heightGap)) - this.topMargin) - this.bottomMargin;
            this.x = (((cellWidth + widthGap) * cellX) + hStartPadding) + this.leftMargin;
            this.y = (((cellHeight + heightGap) * cellY) + vStartPadding) + this.topMargin;
        }
    }

    private class StayConfirm implements Runnable {
        private DragObject lastDragObject;

        private StayConfirm() {
            this.lastDragObject = null;
        }

        public void run() {
            if (CellLayout.this.mLastDragPos != null) {
                View coveringView = CellLayout.this.mOccupiedCell[CellLayout.this.mLastDragPos.cellXY[0]][CellLayout.this.mLastDragPos.cellXY[1]];
                DropTarget targetView = coveringView instanceof DropTarget ? (DropTarget) coveringView : null;
                if (this.lastDragObject.getDragInfo().spanX != 1 || this.lastDragObject.getDragInfo().spanY != 1) {
                    CellLayout.this.rollbackLayout();
                    CellLayout.this.makeEmptyCellsAt(CellLayout.this.mLastDragPos.cellXY[0], CellLayout.this.mLastDragPos.cellXY[1], this.lastDragObject.getDragInfo().spanX, this.lastDragObject.getDragInfo().spanY);
                } else if (CellLayout.this.mLastDragPos.stayType == 2) {
                    if (CellLayout.this.getScreenType() == 2 || targetView == null || !targetView.isDropEnabled() || !targetView.acceptDrop(this.lastDragObject) || targetView == CellLayout.this.mLastCoveringView) {
                        CellLayout.this.rollbackLayout();
                        CellLayout.this.makeEmptyCellsAt(CellLayout.this.mLastDragPos.cellXY[0], CellLayout.this.mLastDragPos.cellXY[1], this.lastDragObject.getDragInfo().spanX, this.lastDragObject.getDragInfo().spanY);
                    } else {
                        targetView.onDragEnter(this.lastDragObject);
                        CellLayout.this.mLastCoveringView = targetView;
                    }
                } else if (CellLayout.this.mLastDragPos.stayType == 0) {
                    if (this.lastDragObject.isMultiDrag()) {
                        CellLayout.this.makeEmptyCellsAt(this.lastDragObject.getDraggingSize(), CellLayout.this.cellToGapIndex(CellLayout.this.mLastDragPos.cellXY[0], CellLayout.this.mLastDragPos.cellXY[1], CellLayout.this.mLastDragPos.stayType));
                    } else if (CellLayout.this.mOccupiedCellBak[CellLayout.this.mLastDragPos.cellXY[0]][CellLayout.this.mLastDragPos.cellXY[1]] == null) {
                        CellLayout.this.rollbackLayout();
                    }
                } else if (CellLayout.this.mLastDragPos.stayType == 5) {
                    CellLayout.this.rollbackLayout();
                } else if (this.lastDragObject.isMultiDrag()) {
                    CellLayout.this.makeEmptyCellsAt(this.lastDragObject.getDraggingSize(), CellLayout.this.cellToGapIndex(CellLayout.this.mLastDragPos.cellXY[0], CellLayout.this.mLastDragPos.cellXY[1], CellLayout.this.mLastDragPos.stayType));
                } else {
                    CellLayout.this.rollbackLayout();
                    CellLayout.this.makeEmptyCellAt(CellLayout.this.cellToGapIndex(CellLayout.this.mLastDragPos.cellXY[0], CellLayout.this.mLastDragPos.cellXY[1], CellLayout.this.mLastDragPos.stayType));
                }
                clear();
            }
        }

        public void clear() {
            this.lastDragObject = null;
        }
    }

    private class ViewConfiguration {
        SpanComparator comparator;
        HashMap<View, ItemInfo> map;
        ArrayList<View> sortedViews;

        class SpanComparator implements Comparator<View> {
            int whichDirection = 0;

            SpanComparator() {
            }

            public int compare(View left, View right) {
                ItemInfo l = (ItemInfo) ViewConfiguration.this.map.get(left);
                ItemInfo r = (ItemInfo) ViewConfiguration.this.map.get(right);
                switch (this.whichDirection) {
                    case 0:
                        return r.spanX - l.spanX;
                    case 1:
                        return r.spanY - l.spanY;
                    case 2:
                        return (r.spanX * r.spanY) - (l.spanX * l.spanY);
                    default:
                        return 0;
                }
            }
        }

        private ViewConfiguration() {
            this.map = new HashMap();
            this.sortedViews = new ArrayList();
            this.comparator = new SpanComparator();
        }

        void addView(View v) {
            if (!this.map.keySet().contains(v)) {
                ItemInfo info = CellLayout.this.getChildInfo(v);
                this.map.put(v, new ItemInfo(info.cellX, info.cellY, info.spanX, info.spanY));
                this.sortedViews.add(v);
            }
        }

        void resetViewConfig() {
            Iterator i$ = this.sortedViews.iterator();
            while (i$.hasNext()) {
                View v = (View) i$.next();
                ItemInfo info = CellLayout.this.getChildInfo(v);
                ItemInfo original = (ItemInfo) this.map.get(v);
                info.cellX = original.cellX;
                info.cellY = original.cellY;
            }
        }

        void clear() {
            this.map.clear();
            this.sortedViews.clear();
        }

        public void sortConfigurationByDirection(int direction) {
            this.comparator.whichDirection = direction;
            Collections.sort(this.sortedViews, this.comparator);
        }
    }

    static {
        boolean z;
        if (CellLayout.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        $assertionsDisabled = z;
    }

    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRect = new Rect();
        this.mCellInfo = new CellInfo();
        this.mCellXY = new int[2];
        this.mTmpXY = new int[2];
        this.mLastDragPos = new DragPos();
        this.mTmpDragPos = new DragPos();
        this.mLastDownOnOccupiedCell = false;
        this.mLastCoveringView = null;
        this.mDisableTouch = false;
        this.mMoveAwayViewList = new ArrayList();
        this.mShakeSource = null;
        this.mCellXIterateRange = new int[2];
        this.mStayConfirm = new StayConfirm();
        this.mTmpCellLR = new int[2];
        this.mLastRelayoutTime = 0;
        this.mLayoutBackupValid = false;
        this.mRectTmp = new Rect();
        this.mDstPos = new int[2];
        this.mDropAnimationCounter = 0;
        this.mIsShowEmptyCellMark = false;
        this.mEmptyCellMarkDrawable = null;
        this.mEmptyCellMarkDrawableAlpha = 0;
        this.mEmptyCellMarkStartAlpha = 0;
        this.mEmptyCellMarkLP = new LayoutParams();
        this.mEmptyCellMarkAnimator = null;
        Resources resources = context.getResources();
        this.mCellPaddingTop = DeviceConfig.getWorkspaceCellPaddingTop();
        this.mCellPaddingLeft = DeviceConfig.getWorkspaceCellPaddingSide();
        this.mCellPaddingRight = this.mCellPaddingLeft;
        this.mWidgetCellPaddingTop = DeviceConfig.getWidgetCellPaddingTop();
        this.mWidgetCellPaddingBottom = DeviceConfig.getWidgetCellPaddingBottom();
        this.mWallpaperManager = WallpaperManager.getInstance(getContext());
        this.mLauncher = (Launcher) context;
        this.mOnLongClickAgent = new OnLongClickAgent(this, this.mLauncher, this);
        this.mCellBackground = new CellBackground(context);
        this.mCellBackground.setLayoutParams(new LayoutParams());
        this.mCellBackground.setImageAlpha(51);
        this.mCellBackground.setTag(new ItemInfo());
        this.mDefaultCellBackground = resources.getDrawable(R.drawable.cell_bg);
        if (sShakeAnimScaler == null) {
            TypedArray ta = resources.obtainTypedArray(R.array.item_shake_animation_scaler);
            sShakeAnimScaler = new int[ta.length()];
            for (int i = 0; i < sShakeAnimScaler.length; i++) {
                sShakeAnimScaler[i] = ta.getDimensionPixelSize(i, 0);
            }
            ta.recycle();
        }
        this.mMoveAwayShakeAnim = ValueAnimator.ofFloat(new float[]{0.0f, 4.0f});
        this.mMoveAwayShakeAnim.setInterpolator(new LinearInterpolator());
        this.mMoveAwayShakeAnim.setDuration((long) resources.getInteger(R.integer.config_itemMoveAwayShakeTime));
        this.mMoveAwayShakeAnim.setRepeatMode(1);
        this.mMoveAwayShakeAnim.setRepeatCount(-1);
        LauncherAnimUtils.cancelOnDestroyActivity(this.mMoveAwayShakeAnim);
        this.mMoveAwayShakeAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float scaleValue = ((Float) animation.getAnimatedValue()).floatValue();
                for (int i = 0; i < CellLayout.this.mMoveAwayViewList.size(); i++) {
                    View v = (View) CellLayout.this.mMoveAwayViewList.get(i);
                    float value = (CellLayout.this.mRandomOffset[i] + scaleValue) % 4.0f;
                    if (value > 1.0f) {
                        if (value <= 2.0f) {
                            value = 2.0f - value;
                        } else if (value <= 3.0f) {
                            value = 2.0f - value;
                        } else if (value <= 4.0f) {
                            value -= 4.0f;
                        }
                    }
                    float needBiger = ((float) CellLayout.SHAKE_OFFSET) * value;
                    if (v != null) {
                        v.setScaleX((((float) v.getWidth()) + needBiger) / ((float) v.getWidth()));
                        v.setScaleY((((float) v.getHeight()) + needBiger) / ((float) v.getHeight()));
                        v.setTranslationY((-needBiger) / 2.0f);
                    }
                }
            }
        });
        this.mMoveAwayShakeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                CellLayout.this.mRandomOffset = null;
                Iterator i$ = CellLayout.this.mMoveAwayViewList.iterator();
                while (i$.hasNext()) {
                    View v = (View) i$.next();
                    if (v != null) {
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                        v.setTranslationY(0.0f);
                    }
                }
                CellLayout.this.mMoveAwayViewList.clear();
            }
        });
        this.mShakeAnim = new ValueAnimator();
        this.mShakeAnim.setInterpolator(new LinearInterpolator());
        final int totalTimes = resources.getInteger(R.integer.config_itemShakeTimesForDelay);
        final int maxValue = sShakeAnimScaler.length - 1;
        this.mShakeAnim.setDuration((long) (resources.getInteger(R.integer.config_itemShakeTime) * totalTimes));
        this.mShakeAnim.setFloatValues(new float[]{0.0f, (float) (maxValue * totalTimes)});
        this.mShakeAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float ratio = ((Float) animation.getAnimatedValue()).floatValue();
                int srcX = CellLayout.this.mShakeSource.getLeft() + (CellLayout.this.mShakeSource.getWidth() / 2);
                int srcY = CellLayout.this.mShakeSource.getTop() + (CellLayout.this.mShakeSource.getHeight() / 2);
                int maxDistanceSquare = (CellLayout.this.getWidth() * CellLayout.this.getWidth()) + (CellLayout.this.getHeight() * CellLayout.this.getHeight());
                for (int i = CellLayout.this.getChildCount() - 1; i >= 0; i--) {
                    View v = CellLayout.this.getChildAt(i);
                    if (!(v == null || v == CellLayout.this.mShakeSource)) {
                        int distanceX = (v.getLeft() + (v.getWidth() / 2)) - srcX;
                        int distanceY = (v.getTop() + (v.getHeight() / 2)) - srcY;
                        float thisRatio = ratio - ((((float) maxValue) * (((float) ((distanceX * distanceX) + (distanceY * distanceY))) / ((float) maxDistanceSquare))) * ((float) (totalTimes - 1)));
                        if (thisRatio > 0.0f) {
                            if (thisRatio >= ((float) maxValue)) {
                                thisRatio = (float) maxValue;
                            }
                            int floor = (int) FloatMath.floor(thisRatio);
                            int ceil = (int) FloatMath.ceil(thisRatio);
                            int floorValue = CellLayout.sShakeAnimScaler[floor];
                            int scaleSize = Math.round(((float) floorValue) + (((float) (CellLayout.sShakeAnimScaler[ceil] - floorValue)) * (thisRatio - ((float) floor))));
                            v.setScaleX(((float) (v.getWidth() + scaleSize)) / ((float) v.getWidth()));
                            v.setScaleY(((float) (v.getHeight() + scaleSize)) / ((float) v.getHeight()));
                        }
                    }
                }
            }
        });
        this.mShakeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                for (int i = CellLayout.this.getChildCount() - 1; i >= 0; i--) {
                    View v = CellLayout.this.getChildAt(i);
                    if (!(v == null || v == CellLayout.this.mShakeSource)) {
                        v.setScaleX(1.0f);
                        v.setScaleY(1.0f);
                    }
                    CellLayout.this.mShakeSource = null;
                }
            }
        });
        setWillNotCacheDrawing(true);
        setTag(this.mCellInfo);
    }

    private ItemInfo getChildInfo(View child) {
        return (ItemInfo) child.getTag();
    }

    private void loadGridConfig() {
        int i;
        View[][] oldOccupied = this.mOccupiedCell;
        if (isScreenRotatable()) {
            int oldHCells = this.mHCells;
            if (getScreenType() == 1) {
                this.mHCells = DeviceConfig.getCellCountX();
                this.mVCells = DeviceConfig.getCellCountY();
                this.mCellWidth = DeviceConfig.getCellWidth();
                this.mCellHeight = DeviceConfig.getCellHeight();
            } else {
                this.mHCells = DeviceConfig.getWidgetCellCountX();
                this.mVCells = DeviceConfig.getWidgetCellCountY();
                int widgetCellWidth = DeviceConfig.getWidgetCellWidth();
                this.mWidgetCellWidth = widgetCellWidth;
                this.mCellWidth = widgetCellWidth;
                widgetCellWidth = DeviceConfig.getWidgetCellHeight();
                this.mWidgetCellHeight = widgetCellWidth;
                this.mCellHeight = widgetCellWidth;
            }
            this.mOccupiedCell = (View[][]) Array.newInstance(View.class, new int[]{this.mHCells, this.mVCells});
            if (oldOccupied != null) {
                if (getScreenType() == 1) {
                    for (int y = 0; y < this.mVCells; y++) {
                        for (int x = 0; x < this.mHCells; x++) {
                            int index = (this.mHCells * y) + x;
                            this.mOccupiedCell[x][y] = oldOccupied[index % oldHCells][index / oldHCells];
                        }
                    }
                    for (i = getChildCount() - 1; i >= 0; i--) {
                        DeviceConfig.correntCellPositionRuntime(getChildInfo(getChildAt(i)), true);
                    }
                } else if (getScreenType() == 2) {
                    copyOccupiedCells(oldOccupied, this.mOccupiedCell);
                }
            }
        } else {
            this.mHCells = DeviceConfig.getCellCountX();
            this.mVCells = DeviceConfig.getCellCountY();
            if (!(this.mOccupiedCell != null && this.mOccupiedCell.length == this.mHCells && this.mOccupiedCell[0].length == this.mVCells)) {
                this.mOccupiedCell = (View[][]) Array.newInstance(View.class, new int[]{this.mHCells, this.mVCells});
            }
            this.mCellWidth = DeviceConfig.getCellWidth();
            this.mCellHeight = DeviceConfig.getCellHeight();
            this.mWidgetCellWidth = DeviceConfig.getWidgetCellWidth();
            this.mWidgetCellHeight = DeviceConfig.getWidgetCellHeight();
            this.mWidgetCellPaddingRight = 0;
            this.mWidgetCellPaddingLeft = 0;
        }
        this.mOccupiedCellBak = (View[][]) Array.newInstance(View.class, new int[]{this.mHCells, this.mVCells});
        this.mStayConfirmSize = (int) ((((float) this.mCellWidth) * 0.1f) + 0.5f);
        if (oldOccupied == null) {
            this.mTotalCells = this.mHCells * this.mVCells;
        }
        for (i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof HostView) {
                ((HostView) child).setSkipNextAutoLayoutAnimation(true);
            }
        }
        requestLayout();
    }

    public void cancelLongPress() {
        this.mOnLongClickAgent.cancelCustomziedLongPress();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).cancelLongPress();
        }
    }

    void setScreenId(long id) {
        this.mCellInfo.screenId = id;
    }

    void setContainerId(int container) {
        this.mCellInfo.container = container;
    }

    long getScreenId() {
        return this.mCellInfo.screenId;
    }

    void setScreenType(int screenType) {
        if (this.mCellInfo.screenType != screenType) {
            this.mCellInfo.screenType = screenType;
            loadGridConfig();
        }
    }

    int getScreenType() {
        return this.mCellInfo.screenType;
    }

    public void setEditMode(boolean isEditing) {
    }

    public void onWallpaperColorChanged() {
        WallpaperUtils.varyViewGroupByWallpaper(this);
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mDefaultCellBackground = this.mLauncher.getResources().getDrawable(R.drawable.cell_bg_dark);
        } else {
            this.mDefaultCellBackground = this.mLauncher.getResources().getDrawable(R.drawable.cell_bg);
        }
    }

    public int removeShortcutIcon(ShortcutIcon icon) {
        removeViewInLayout(icon);
        if (getParent() instanceof CellScreen) {
            ((CellScreen) getParent()).updateLayout();
        }
        invalidate();
        return -1;
    }

    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        if (child instanceof ItemIcon) {
            ((ItemIcon) child).setEnableAutoLayoutAnimation(true);
        }
        if (getScreenType() == 1 && !this.mLauncher.getDragController().getIsScreenOrientationChanged()) {
            DeviceConfig.correntCellPositionRuntime(getChildInfo(child), false);
        }
        if (child != this.mCellBackground) {
            updateCellOccupiedMarks(child, false);
        }
        WallpaperUtils.onAddViewToGroup(this, child, true);
        super.addView(child, index, params);
    }

    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mShakeAnim.isRunning()) {
            this.mShakeAnim.cancel();
        }
        int action = ev.getAction();
        CellInfo cellInfo = this.mCellInfo;
        if (getScreenId() != -1) {
            if (action == 0) {
                Rect frame = this.mRect;
                int x = ((int) ev.getX()) + this.mScrollX;
                int y = ((int) ev.getY()) + this.mScrollY;
                boolean found = false;
                for (int i = getChildCount() - 1; i >= 0; i--) {
                    View child = getChildAt(i);
                    if (child.getVisibility() == 0 || child.getAnimation() != null) {
                        child.getHitRect(frame);
                        if (frame.contains(x, y)) {
                            ItemInfo info = getChildInfo(child);
                            cellInfo.cell = child;
                            cellInfo.cellX = info.cellX;
                            cellInfo.cellY = info.cellY;
                            cellInfo.spanX = info.spanX;
                            cellInfo.spanY = info.spanY;
                            found = true;
                            break;
                        }
                    }
                }
                this.mLastDownOnOccupiedCell = found;
                if (!found) {
                    int[] cellXY = this.mCellXY;
                    pointToCell(x, y, cellXY);
                    cellInfo.cell = null;
                    cellInfo.cellX = cellXY[0];
                    cellInfo.cellY = cellXY[1];
                    cellInfo.spanX = 1;
                    cellInfo.spanY = 1;
                }
                if (cellInfo.cell == null) {
                    this.mOnLongClickAgent.setTimeOut(1000);
                }
            } else if (action == 1) {
                resetTouchCellInfo();
            }
            if (this.mOnLongClickAgent.onDispatchTouchEvent(ev)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void resetTouchCellInfo() {
        this.mCellInfo.cell = null;
        this.mCellInfo.cellX = -1;
        this.mCellInfo.cellY = -1;
        this.mCellInfo.spanX = 0;
        this.mCellInfo.spanY = 0;
    }

    void setDisableTouch(boolean isDisable) {
        this.mDisableTouch = isDisable;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.mDisableTouch) {
            return true;
        }
        this.mOnLongClickAgent.cancelCustomziedLongPress();
        return false;
    }

    public void buildDrawingCache(boolean autoScale) {
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mOnLongClickAgent.setOnLongClickListener(l);
    }

    public Object getVersionTag() {
        return Integer.valueOf(getWindowAttachCount());
    }

    void pointToCell(int x, int y, int[] result) {
        result[0] = (x - this.mCellPaddingLeft) / (this.mCellWidth + this.mWidthGap);
        result[1] = (y - this.mCellPaddingTop) / (this.mCellHeight + this.mHeightGap);
        result[0] = Math.max(0, Math.min(result[0], this.mHCells - 1));
        result[1] = Math.max(0, Math.min(result[1], this.mVCells - 1));
    }

    void cellToPoint(int cellX, int cellY, int[] result) {
        result[0] = this.mCellPaddingLeft + ((this.mCellWidth + this.mWidthGap) * cellX);
        result[1] = this.mCellPaddingTop + ((this.mCellHeight + this.mHeightGap) * cellY);
    }

    int getCellWidth() {
        return this.mCellWidth;
    }

    int getCellHeight() {
        return this.mCellHeight;
    }

    int getCellWidthGap() {
        return this.mWidthGap;
    }

    int getCellHeightGap() {
        return this.mHeightGap;
    }

    int getWidgetCellWidth() {
        return this.mWidgetCellWidth;
    }

    int getWidgetCellHeight() {
        return this.mWidgetCellHeight;
    }

    Rect getWidgetMeasureSpec(int spanX, int spanY, Rect size) {
        if (size == null) {
            size = new Rect();
        }
        int widgetCellWidth = (int) (((float) (getWidgetCellWidth() * spanX)) / DeviceConfig.getScreenDensity());
        size.right = widgetCellWidth;
        size.left = widgetCellWidth;
        widgetCellWidth = (int) (((float) (getWidgetCellHeight() * spanY)) / DeviceConfig.getScreenDensity());
        size.bottom = widgetCellWidth;
        size.top = widgetCellWidth;
        return size;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i = 0;
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == 0 || heightSpecMode == 0) {
            throw new RuntimeException("CellLayout cannot have UNSPECIFIED dimensions");
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        if (getScreenType() == 2) {
            int widgetWorkingWidth;
            if (DeviceConfig.isAutoCellSize()) {
                widgetWorkingWidth = (DeviceConfig.getWidgetWorkingWidth() - (this.mWidgetCellWidth * this.mHCells)) / 2;
                this.mCellPaddingRight = widgetWorkingWidth;
                this.mCellPaddingLeft = widgetWorkingWidth;
                this.mWidgetCellPaddingRight = widgetWorkingWidth;
                this.mWidgetCellPaddingLeft = widgetWorkingWidth;
                widgetWorkingWidth = (DeviceConfig.getWidgetWorkingHeight() - (this.mWidgetCellHeight * this.mVCells)) / 2;
                this.mCellPaddingTop = widgetWorkingWidth;
                this.mWidgetCellPaddingTop = widgetWorkingWidth;
            } else {
                widgetWorkingWidth = (DeviceConfig.getWidgetWorkingWidth() - (this.mWidgetCellWidth * this.mHCells)) / 2;
                this.mCellPaddingRight = widgetWorkingWidth;
                this.mCellPaddingLeft = widgetWorkingWidth;
                this.mWidgetCellPaddingRight = widgetWorkingWidth;
                this.mWidgetCellPaddingLeft = widgetWorkingWidth;
                widgetWorkingWidth = DeviceConfig.getWidgetCellPaddingTop() + ((DeviceConfig.getWidgetWorkingHeight() - (this.mWidgetCellHeight * this.mVCells)) / 2);
                this.mCellPaddingTop = widgetWorkingWidth;
                this.mWidgetCellPaddingTop = widgetWorkingWidth;
            }
            this.mHeightGap = 0;
            this.mWidthGap = 0;
        } else {
            this.mWidgetCellPaddingTop = 0;
            this.mWidgetCellPaddingLeft = 0;
            this.mWidthGap = this.mHCells <= 1 ? 0 : (DeviceConfig.getCellWorkingWidth() - (this.mCellWidth * this.mHCells)) / (this.mHCells - 1);
            if (this.mVCells > 1) {
                i = (DeviceConfig.getCellWorkingHeight() - (this.mCellHeight * this.mVCells)) / (this.mVCells - 1);
            }
            this.mHeightGap = i;
        }
        int count = getChildCount();
        for (int i2 = 0; i2 < count; i2++) {
            measureChild(getChildAt(i2));
        }
    }

    public void measureChild(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        ItemInfo info = (ItemInfo) child.getTag();
        if (((child.getTag() instanceof LauncherAppWidgetInfo) || (child.getTag() instanceof GadgetInfo) || (child == this.mCellBackground && !this.mCellBackground.isIconCellBackground())) && (!(info.spanX == 1 && info.spanY == 1) && getScreenType() == 2)) {
            lp.setup(info.cellX, info.cellY, info.spanX, info.spanY, getWidgetCellWidth(), getWidgetCellHeight(), 0, 0, this.mWidgetCellPaddingLeft, this.mWidgetCellPaddingTop);
        } else {
            lp.setup(info.cellX, info.cellY, info.spanX, info.spanY, this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCellPaddingLeft, this.mCellPaddingTop);
        }
        child.measure(MeasureSpec.makeMeasureSpec(lp.width, 1073741824), MeasureSpec.makeMeasureSpec(lp.height, 1073741824));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childLeft = lp.x;
                int childTop = lp.y;
                child.layout(childLeft, childTop, lp.width + childLeft, lp.height + childTop);
                if (lp.dropped) {
                    lp.dropped = false;
                    int[] cellXY = this.mCellXY;
                    getLocationOnScreen(cellXY);
                    this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.home.drop", (cellXY[0] + childLeft) + (lp.width / 2), (cellXY[1] + childTop) + (lp.height / 2), 0, null);
                }
            }
        }
    }

    void updateCellOccupiedMarks(View cell, boolean remove) {
        updateCellOccupiedMarks(cell, getChildInfo(cell), remove);
    }

    void updateCellOccupiedMarks(View cell, ItemInfo info, boolean remove) {
        for (int x = (info.cellX + info.spanX) - 1; x >= info.cellX; x--) {
            for (int y = (info.cellY + info.spanY) - 1; y >= info.cellY; y--) {
                if (remove) {
                    if (cell == this.mOccupiedCell[x][y]) {
                        this.mOccupiedCell[x][y] = null;
                    }
                } else if (this.mOccupiedCell[x][y] == null) {
                    this.mOccupiedCell[x][y] = cell;
                } else {
                    this.mOccupiedCell[x][y] = cell;
                }
            }
        }
    }

    boolean isItemPosInvalid(int cellX, int cellY, int spanX, int spanY) {
        return cellX < 0 || cellY < 0 || cellX + spanX > this.mHCells || cellY + spanY > this.mVCells;
    }

    boolean isCellOccupied(int cellX, int cellY, int spanX, int spanY) {
        int i = 0;
        while (i < spanX) {
            if (cellX + i >= this.mHCells) {
                return true;
            }
            int j = 0;
            while (j < spanY) {
                if (cellY + j >= this.mVCells || this.mOccupiedCell[cellX + i][cellY + j] != null) {
                    return true;
                }
                j++;
            }
            i++;
        }
        return false;
    }

    private void calCellXRange(int direction) {
        int i = 0;
        this.mCellXIterateRange[0] = direction == 1 ? 0 : this.mHCells - 1;
        int[] iArr = this.mCellXIterateRange;
        if (direction == 1) {
            i = this.mHCells - 1;
        }
        iArr[1] = i;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int[] findLastEmptyCell(int r22, int r23) {
        /*
        r21 = this;
        r0 = r21;
        r5 = r0.mTmpXY;
        r13 = com.miui.home.launcher.DeviceConfig.isLayoutRtl();
        r18 = 0;
        r19 = -1;
        r5[r18] = r19;
        r18 = 1;
        r19 = -1;
        r5[r18] = r19;
        r18 = 1;
        r11 = com.miui.home.launcher.DeviceConfig.getIterateDirection(r18);
        r0 = r21;
        r0.calCellXRange(r11);
        r0 = r21;
        r0 = r0.mVCells;
        r18 = r0;
        r17 = r18 + -1;
    L_0x0027:
        if (r17 < 0) goto L_0x0141;
    L_0x0029:
        r0 = r21;
        r0 = r0.mCellXIterateRange;
        r18 = r0;
        r19 = 0;
        r16 = r18[r19];
    L_0x0033:
        r18 = 1;
        r0 = r18;
        if (r11 != r0) goto L_0x00bc;
    L_0x0039:
        r0 = r21;
        r0 = r0.mCellXIterateRange;
        r18 = r0;
        r19 = 1;
        r18 = r18[r19];
        r0 = r16;
        r1 = r18;
        if (r0 > r1) goto L_0x00cc;
    L_0x0049:
        r0 = r21;
        r0 = r0.mOccupiedCell;
        r18 = r0;
        r18 = r18[r16];
        r18 = r18[r17];
        if (r18 == 0) goto L_0x013d;
    L_0x0055:
        r0 = r21;
        r0 = r0.mOccupiedCell;
        r18 = r0;
        r18 = r18[r16];
        r15 = r18[r17];
        r12 = r15.getTag();
        r12 = (com.miui.home.launcher.ItemInfo) r12;
        r0 = r12.spanY;
        r18 = r0;
        r18 = r18 - r23;
        if (r18 >= 0) goto L_0x00d0;
    L_0x006d:
        r18 = 0;
        r0 = r12.cellY;
        r19 = r0;
        r0 = r12.spanY;
        r20 = r0;
        r19 = r19 + r20;
        r19 = r19 - r23;
        r14 = java.lang.Math.max(r18, r19);
    L_0x007f:
        if (r13 == 0) goto L_0x00d3;
    L_0x0081:
        r0 = r12.cellX;
        r18 = r0;
        r7 = r18 + -1;
    L_0x0087:
        if (r13 == 0) goto L_0x00de;
    L_0x0089:
        r6 = 0;
    L_0x008a:
        r18 = 0;
        r8 = com.miui.home.launcher.DeviceConfig.getIterateDirection(r18);
        r9 = r7;
    L_0x0091:
        if (r13 == 0) goto L_0x00e7;
    L_0x0093:
        if (r9 < r6) goto L_0x00e9;
    L_0x0095:
        r0 = r12.cellY;
        r18 = r0;
        r0 = r12.spanY;
        r19 = r0;
        r18 = r18 + r19;
        r10 = r18 + -1;
    L_0x00a1:
        if (r10 < r14) goto L_0x00af;
    L_0x00a3:
        r0 = r21;
        r0 = r0.mOccupiedCell;
        r18 = r0;
        r18 = r18[r9];
        r18 = r18[r10];
        if (r18 == 0) goto L_0x011f;
    L_0x00af:
        r18 = 0;
        r18 = r5[r18];
        if (r18 < 0) goto L_0x0137;
    L_0x00b5:
        r18 = 1;
        r18 = r5[r18];
        if (r18 < 0) goto L_0x0137;
    L_0x00bb:
        return r5;
    L_0x00bc:
        r0 = r21;
        r0 = r0.mCellXIterateRange;
        r18 = r0;
        r19 = 1;
        r18 = r18[r19];
        r0 = r16;
        r1 = r18;
        if (r0 >= r1) goto L_0x0049;
    L_0x00cc:
        r17 = r17 + -1;
        goto L_0x0027;
    L_0x00d0:
        r14 = r12.cellY;
        goto L_0x007f;
    L_0x00d3:
        r0 = r12.cellX;
        r18 = r0;
        r0 = r12.spanX;
        r19 = r0;
        r7 = r18 + r19;
        goto L_0x0087;
    L_0x00de:
        r0 = r21;
        r0 = r0.mHCells;
        r18 = r0;
        r6 = r18 + -1;
        goto L_0x008a;
    L_0x00e7:
        if (r9 <= r6) goto L_0x0095;
    L_0x00e9:
        r19 = 0;
        if (r13 == 0) goto L_0x013a;
    L_0x00ed:
        r0 = r21;
        r0 = r0.mHCells;
        r18 = r0;
        r18 = r18 - r22;
    L_0x00f5:
        r5[r19] = r18;
        r18 = 1;
        r0 = r12.cellY;
        r19 = r0;
        r0 = r12.spanY;
        r20 = r0;
        r19 = r19 + r20;
        r5[r18] = r19;
        r18 = 0;
        r18 = r5[r18];
        r19 = 1;
        r19 = r5[r19];
        r0 = r21;
        r1 = r18;
        r2 = r19;
        r3 = r22;
        r4 = r23;
        r18 = r0.isCellOccupied(r1, r2, r3, r4);
        if (r18 == 0) goto L_0x00bb;
    L_0x011d:
        r5 = 0;
        goto L_0x00bb;
    L_0x011f:
        r0 = r21;
        r1 = r22;
        r2 = r23;
        r18 = r0.isCellOccupied(r9, r10, r1, r2);
        if (r18 != 0) goto L_0x0133;
    L_0x012b:
        r18 = 0;
        r5[r18] = r9;
        r18 = 1;
        r5[r18] = r10;
    L_0x0133:
        r10 = r10 + -1;
        goto L_0x00a1;
    L_0x0137:
        r9 = r9 + r8;
        goto L_0x0091;
    L_0x013a:
        r18 = 0;
        goto L_0x00f5;
    L_0x013d:
        r16 = r16 + r11;
        goto L_0x0033;
    L_0x0141:
        r19 = 0;
        if (r13 == 0) goto L_0x0157;
    L_0x0145:
        r0 = r21;
        r0 = r0.mHCells;
        r18 = r0;
        r18 = r18 - r22;
    L_0x014d:
        r5[r19] = r18;
        r18 = 1;
        r19 = 0;
        r5[r18] = r19;
        goto L_0x00bb;
    L_0x0157:
        r18 = 0;
        goto L_0x014d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.miui.home.launcher.CellLayout.findLastEmptyCell(int, int):int[]");
    }

    int[] findLastVacantArea(int spanX, int spanY) {
        int[] bestXY = this.mTmpXY;
        bestXY[0] = -1;
        int direction = DeviceConfig.getIterateDirection(true);
        calCellXRange(direction);
        for (int y = this.mVCells - 1; y >= 0; y--) {
            int x = this.mCellXIterateRange[0];
            while (true) {
                if (direction != 1) {
                    if (x < this.mCellXIterateRange[1]) {
                        continue;
                        break;
                    }
                } else if (x > this.mCellXIterateRange[1]) {
                    continue;
                    break;
                }
                if (!isCellOccupied(x, y, spanX, spanY)) {
                    bestXY[0] = x;
                    bestXY[1] = y;
                } else if (bestXY[0] != -1) {
                    return bestXY;
                }
                x += direction;
            }
        }
        return bestXY[0] == -1 ? null : bestXY;
    }

    int[] findFirstVacantArea(int spanX, int spanY) {
        int[] bestXY = this.mTmpXY;
        bestXY[0] = -1;
        int direction = DeviceConfig.getIterateDirection(false);
        calCellXRange(direction);
        for (int y = 0; y < this.mVCells; y++) {
            int x = this.mCellXIterateRange[0];
            while (true) {
                if (direction != 1) {
                    if (x < this.mCellXIterateRange[1]) {
                        continue;
                        break;
                    }
                } else if (x > this.mCellXIterateRange[1]) {
                    continue;
                    break;
                }
                if (isCellOccupied(x, y, spanX, spanY)) {
                    x += direction;
                } else {
                    bestXY[0] = x;
                    bestXY[1] = y;
                    return bestXY;
                }
            }
        }
        return null;
    }

    int[] findNearestLinearVacantArea(int pixelx, int pixelY, int spanX, int spanY, boolean ignoreOccupied) {
        int[] bestXY = this.mTmpXY;
        pointToCell(pixelx, pixelY, bestXY);
        if (ignoreOccupied) {
            return bestXY;
        }
        int current = cellToPositionIndex(bestXY[0], bestXY[1]);
        for (int forward = current; forward < this.mTotalCells; forward++) {
            positionIndexToCell(forward, bestXY);
            if (!isCellOccupied(bestXY[0], bestXY[1], spanX, spanY)) {
                return bestXY;
            }
        }
        for (int backward = current - 1; backward >= 0; backward--) {
            positionIndexToCell(backward, bestXY);
            if (!isCellOccupied(bestXY[0], bestXY[1], spanX, spanY)) {
                return bestXY;
            }
        }
        return null;
    }

    int[] findNearestVacantAreaByCellPos(int cellX, int cellY, int spanX, int spanY, boolean ignoreOccupied) {
        cellToPoint(cellX, cellY, this.mCellXY);
        return findNearestVacantArea(this.mCellXY[0], this.mCellXY[1], spanX, spanY, ignoreOccupied);
    }

    int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY, boolean ignoreOccupied) {
        if (!ignoreOccupied && spanX * spanY > this.mTotalCells) {
            return null;
        }
        int[] bestXY = this.mTmpXY;
        int[] cellXY = this.mCellXY;
        double bestDistance = Double.MAX_VALUE;
        int y = this.mVCells - spanY;
        while (y >= 0) {
            int x = this.mHCells - spanX;
            while (x >= 0) {
                cellToPoint(x, y, cellXY);
                double distance = Math.pow((double) (cellXY[0] - pixelX), 2.0d) + Math.pow((double) (cellXY[1] - pixelY), 2.0d);
                if (distance < bestDistance && (ignoreOccupied || !isCellOccupied(x, y, spanX, spanY))) {
                    bestDistance = distance;
                    bestXY[0] = x;
                    bestXY[1] = y;
                }
                x--;
            }
            y--;
        }
        return bestDistance >= Double.MAX_VALUE ? null : bestXY;
    }

    boolean getChildVisualPosByTag(Object tag, int[] outPos) {
        for (int y = 0; y < this.mVCells; y++) {
            int x = 0;
            while (x < this.mHCells) {
                View v = this.mOccupiedCell[x][y];
                if (v == null || !v.getTag().equals(tag)) {
                    x++;
                } else {
                    outPos[0] = x;
                    outPos[1] = y;
                    return true;
                }
            }
        }
        return false;
    }

    public int[] findDropTargetPosition(DragObject d) {
        ItemInfo dragInfo = d.getDragInfo();
        switch (d.dropAction) {
            case 0:
                return findFirstVacantArea(dragInfo.spanX, dragInfo.spanY);
            case 1:
                if (d.dragSource instanceof MultiSelectContainerView) {
                    return findNearestLinearVacantArea(d.x - d.xOffset, d.y - d.yOffset, dragInfo.spanX, dragInfo.spanY, false);
                }
                return findNearestVacantArea(d.x - d.xOffset, d.y - d.yOffset, dragInfo.spanX, dragInfo.spanY, false);
            case 2:
            case 4:
                return findLastVacantArea(dragInfo.spanX, dragInfo.spanY);
            case 3:
                if (isCellOccupied(dragInfo.cellX, dragInfo.cellY, dragInfo.spanX, dragInfo.spanY)) {
                    return null;
                }
                this.mTmpXY[0] = dragInfo.cellX;
                this.mTmpXY[1] = dragInfo.cellY;
                return this.mTmpXY;
            default:
                return null;
        }
    }

    boolean onDrop(DragObject d, View v) {
        Handler h = getHandler();
        if (h == null) {
            return false;
        }
        if (getScreenType() == 2 && (v instanceof ItemIcon)) {
            return false;
        }
        if (getScreenType() == 1 && d.getDragInfo().isNeedLargeCell()) {
            return false;
        }
        h.removeCallbacks(this.mStayConfirm);
        if (this.mLastCoveringView != null) {
            boolean r = this.mLastCoveringView.onDrop(d);
            if (!d.isLastObject()) {
                return r;
            }
            this.mLastCoveringView.onDragExit(d);
            this.mLastCoveringView = null;
            rollbackLayout();
            return r;
        }
        int[] targetXY = findDropTargetPosition(d);
        if (targetXY == null) {
            return false;
        }
        ItemInfo dragInfo = d.getDragInfo();
        if (v == null) {
            dragInfo.cellX = targetXY[0];
            dragInfo.cellY = targetXY[1];
            return true;
        } else if (!(d.dragSource instanceof MultiSelectContainerView) && dragInfo.container == ((long) this.mCellInfo.container) && targetXY[0] == this.mCellInfo.cellX && targetXY[1] == this.mCellInfo.cellY && dragInfo.screenId == this.mCellInfo.screenId) {
            rollbackLayout();
            updateCellOccupiedMarks(v, false);
            addView(v, -1, v.getLayoutParams());
            return true;
        } else {
            if (v != null) {
                ItemInfo info = getChildInfo(v);
                info.screenId = getScreenId();
                info.cellX = targetXY[0];
                info.cellY = targetXY[1];
                info.container = -100;
                LayoutParams lp = (LayoutParams) v.getLayoutParams();
                lp.isDragging = false;
                lp.dropped = true;
                if (v.getParent() == null) {
                    addView(v, -1, lp);
                } else {
                    v.requestLayout();
                    updateCellOccupiedMarks(v, false);
                }
            }
            return true;
        }
    }

    void onDropCompleted() {
        saveCurrentLayout();
        clearBackupLayout();
    }

    void onDropAborted(View child) {
        Handler h = getHandler();
        if (h != null) {
            h.removeCallbacks(this.mStayConfirm);
        }
        if (child != null) {
            ((LayoutParams) child.getLayoutParams()).isDragging = false;
            rollbackLayout();
        }
    }

    void onDragChild(View child) {
        ((LayoutParams) child.getLayoutParams()).isDragging = true;
        updateCellOccupiedMarks(child, true);
    }

    void clearCellBackground() {
        removeView(this.mCellBackground);
    }

    public void onForceTouchTriggered() {
        CellInfo cellInfo = (CellInfo) getTag();
        if (cellInfo != null && cellInfo.cell != null) {
            cellInfo.cell.setVisibility(4);
        }
    }

    public void onForceTouchFinish() {
        CellInfo cellInfo = (CellInfo) getTag();
        if (cellInfo != null && cellInfo.cell != null) {
            cellInfo.cell.setVisibility(0);
        }
    }

    private void makeEmptyCellAt(int gap) {
        int startPos;
        int delta;
        int[] cellLR = this.mTmpCellLR;
        int[] cellXY = this.mCellXY;
        gapToCellIndexes(gap, cellLR);
        int forward = cellLR[1];
        int backward = cellLR[0];
        while (forward != -1 && forward < this.mTotalCells) {
            positionIndexToCell(forward, cellXY);
            if (this.mOccupiedCell[cellXY[0]][cellXY[1]] == null) {
                break;
            }
            forward++;
        }
        if (forward == this.mTotalCells) {
            forward = -1;
        }
        while (backward != -1 && backward >= 0) {
            positionIndexToCell(backward, cellXY);
            if (this.mOccupiedCell[cellXY[0]][cellXY[1]] == null) {
                break;
            }
            backward--;
        }
        if (backward < 0) {
            backward = -1;
        }
        if (forward == -1 || backward == -1) {
            if (forward != -1) {
                startPos = cellLR[1];
            } else if (backward != -1) {
                startPos = cellLR[0];
            } else {
                return;
            }
        } else if (forward - backward == 2) {
            if (forward != cellLR[1]) {
                startPos = cellLR[1];
            } else {
                startPos = cellLR[0];
            }
        } else if (forward - cellLR[1] < cellLR[0] - backward) {
            startPos = cellLR[1];
        } else {
            startPos = cellLR[0];
        }
        View previous = null;
        if (startPos == cellLR[0]) {
            delta = -1;
        } else {
            delta = 1;
        }
        while (startPos < this.mTotalCells) {
            ItemInfo info;
            positionIndexToCell(startPos, cellXY);
            startPos += delta;
            View v = this.mOccupiedCell[cellXY[0]][cellXY[1]];
            if (v != null) {
                info = getChildInfo(v);
                if (info.spanX != 1) {
                    continue;
                } else if (info.spanY != 1) {
                    continue;
                }
            }
            this.mOccupiedCell[cellXY[0]][cellXY[1]] = previous;
            if (previous != null) {
                this.mOccupiedCell[cellXY[0]][cellXY[1]] = previous;
                info = getChildInfo(previous);
                info.cellX = cellXY[0];
                info.cellY = cellXY[1];
            }
            if (v == null) {
                break;
            }
            previous = v;
        }
        requestLayout();
        this.mLastRelayoutTime = System.currentTimeMillis();
    }

    private void makeEmptyCellsAt(int num, int gap) {
        View v;
        ItemInfo info;
        int[] cellLR = this.mTmpCellLR;
        int[] cellXY = this.mCellXY;
        gapToCellIndexes(gap, cellLR);
        int forward = cellLR[1];
        int forwardLast = 0;
        int backward = cellLR[0];
        int backwardLast = 0;
        int fCounter = 0;
        int bCounter = 0;
        while (forward != -1 && forward < this.mTotalCells) {
            positionIndexToCell(forward, cellXY);
            if (this.mOccupiedCell[cellXY[0]][cellXY[1]] == null) {
                forwardLast = forward;
                fCounter++;
                if (fCounter == num) {
                    break;
                }
            }
            forward++;
        }
        if (fCounter < num) {
            while (backward != -1 && backward >= 0) {
                positionIndexToCell(backward, cellXY);
                if (this.mOccupiedCell[cellXY[0]][cellXY[1]] == null) {
                    bCounter++;
                    backwardLast = backward;
                }
                if (bCounter + fCounter == num) {
                    break;
                }
                backward--;
            }
        }
        int p = forwardLast;
        int i = 0;
        if (fCounter > 0) {
            do {
                positionIndexToCell(p, cellXY);
                v = this.mOccupiedCell[cellXY[0]][cellXY[1]];
                if (v != null) {
                    info = getChildInfo(v);
                    if (info.spanX == 1 && info.spanY == 1) {
                        this.mOccupiedCell[cellXY[0]][cellXY[1]] = null;
                        int i2 = i + 1;
                        positionIndexToCell(forwardLast - i, cellXY);
                        this.mOccupiedCell[cellXY[0]][cellXY[1]] = v;
                        i = i2;
                    }
                }
                p--;
            } while (p >= cellLR[1]);
        }
        p = backwardLast;
        i = 0;
        if (bCounter > 0) {
            do {
                positionIndexToCell(p, cellXY);
                v = this.mOccupiedCell[cellXY[0]][cellXY[1]];
                if (v != null) {
                    info = getChildInfo(v);
                    if (info.spanX == 1 && info.spanY == 1) {
                        this.mOccupiedCell[cellXY[0]][cellXY[1]] = null;
                        i2 = i + 1;
                        positionIndexToCell(backwardLast + i, cellXY);
                        this.mOccupiedCell[cellXY[0]][cellXY[1]] = v;
                        i = i2;
                    }
                }
                p++;
            } while (p <= cellLR[0]);
        }
        relayoutByOccupiedCells();
    }

    private boolean checkEmptyAreaSize(int cellX, int cellY, int spanX, int spanY) {
        int coveredCellNums = spanX * spanY;
        int emptyCellsNum = 0;
        for (int i = 0; i < this.mTotalCells; i++) {
            positionIndexToCell(i, this.mCellXY);
            if (this.mOccupiedCell[this.mCellXY[0]][this.mCellXY[1]] == null) {
                emptyCellsNum++;
            }
        }
        if (emptyCellsNum < coveredCellNums) {
            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.mHCells; i++) {
            for (int j = 0; j < this.mVCells; j++) {
                if (this.mOccupiedCell[i][j] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeEmptyCellsAt(int cellX, int cellY, int spanX, int spanY) {
        if (cellX >= 0 && cellY >= 0 && checkEmptyAreaSize(cellX, cellY, spanX, spanY)) {
            if (this.mMoveAwayShakeAnim.isRunning()) {
                this.mMoveAwayShakeAnim.cancel();
            }
            Rect coveringRect = new Rect(cellX, cellY, cellX + spanX, cellY + spanY);
            CellLayout cellLayout = this;
            ViewConfiguration currentState = new ViewConfiguration();
            saveCurrentConfiguration(currentState);
            View[][] tmpOccupiedCell = (View[][]) Array.newInstance(View.class, new int[]{this.mHCells, this.mVCells});
            copyOccupiedCells(this.mOccupiedCell, tmpOccupiedCell);
            ArrayList<View> cannotPushViews = new ArrayList();
            for (int i = cellY; i < cellY + spanY; i++) {
                for (int j = cellX; j < cellX + spanX; j++) {
                    View coveringView = this.mOccupiedCell[j][i];
                    if (!(coveringView == null || pushView(coveringView, coveringRect, 0) || pushView(coveringView, coveringRect, 2) || pushView(coveringView, coveringRect, 1) || pushView(coveringView, coveringRect, 3))) {
                        cannotPushViews.add(coveringView);
                        updateCellOccupiedMarks(coveringView, true);
                    }
                }
            }
            ItemInfo dragPos = new ItemInfo(cellX, cellY, spanX, spanY);
            updateCellOccupiedMarks(this.mCellBackground, dragPos, false);
            boolean isMoveSuccess = true;
            SpanComparator comparator = currentState.comparator;
            comparator.whichDirection = 0;
            Collections.sort(cannotPushViews, comparator);
            if (cannotPushViews.size() > 0) {
                Iterator i$ = cannotPushViews.iterator();
                while (i$.hasNext()) {
                    View insertView = (View) i$.next();
                    ItemInfo info = getChildInfo(insertView);
                    int[] slot = findNearestVacantAreaByCellPos(info.cellX, info.cellY, info.spanX, info.spanY, false);
                    if (slot == null) {
                        copyOccupiedCells(tmpOccupiedCell, this.mOccupiedCell);
                        currentState.resetViewConfig();
                        isMoveSuccess = false;
                        break;
                    }
                    updateCellOccupiedMarks(insertView, true);
                    info.cellX = slot[0];
                    info.cellY = slot[1];
                    updateCellOccupiedMarks(insertView, false);
                }
            }
            if (isMoveSuccess) {
                updateCellOccupiedMarks(this.mCellBackground, dragPos, true);
            } else if (!reorderAllViews(cellX, cellY, spanX, spanY, currentState)) {
                copyOccupiedCells(tmpOccupiedCell, this.mOccupiedCell);
                currentState.resetViewConfig();
                return;
            }
            loadMoveAwayViews(currentState);
            this.mMoveAwayShakeAnim.start();
            requestLayout();
            this.mLastRelayoutTime = System.currentTimeMillis();
        }
    }

    private void loadMoveAwayViews(ViewConfiguration lastState) {
        Iterator i$ = lastState.sortedViews.iterator();
        while (i$.hasNext()) {
            View v = (View) i$.next();
            ItemInfo info = getChildInfo(v);
            ItemInfo original = (ItemInfo) lastState.map.get(v);
            if (info.cellX != original.cellX || info.cellY != original.cellY) {
                this.mMoveAwayViewList.add(v);
            }
        }
        this.mRandomOffset = new float[this.mMoveAwayViewList.size()];
        int index = 0;
        while (index < this.mMoveAwayViewList.size()) {
            int index2 = index + 1;
            this.mRandomOffset[index] = ((float) Math.random()) * 4.0f;
            index = index2;
        }
    }

    private boolean pushView(View coveringView, Rect occupiedRect, int direction) {
        int pushDistance;
        ArrayList<View> cluster = new ArrayList();
        ItemInfo info = getChildInfo(coveringView);
        if (direction == 0) {
            pushDistance = (info.cellX + info.spanX) - occupiedRect.left;
        } else if (direction == 2) {
            pushDistance = occupiedRect.right - info.cellX;
        } else if (direction == 1) {
            pushDistance = (info.cellY + info.spanY) - occupiedRect.top;
        } else {
            pushDistance = occupiedRect.bottom - info.cellY;
        }
        if (pushDistance <= 0) {
            return false;
        }
        View[][] tmpOccupiedCell = (View[][]) Array.newInstance(View.class, new int[]{this.mHCells, this.mVCells});
        copyOccupiedCells(this.mOccupiedCell, tmpOccupiedCell);
        ViewConfiguration currentConfig = new ViewConfiguration();
        while (pushDistance > 0 && !false) {
            ArrayList<View> newAddingViews = new ArrayList();
            newAddingViews.add(coveringView);
            cluster.clear();
            while (newAddingViews.size() > 0) {
                View v = (View) newAddingViews.get(0);
                newAddingViews.remove(0);
                findViewsTouchEdge(v, direction, newAddingViews);
                cluster.add(v);
                currentConfig.addView(v);
            }
            pushDistance--;
            if (!shiftViews(cluster, direction, 1)) {
                copyOccupiedCells(tmpOccupiedCell, this.mOccupiedCell);
                currentConfig.resetViewConfig();
                return false;
            }
        }
        return true;
    }

    private boolean shiftViews(ArrayList<View> cluster, int direction, int step) {
        Iterator i$ = cluster.iterator();
        while (i$.hasNext()) {
            View v = (View) i$.next();
            ItemInfo info = getChildInfo(v);
            updateCellOccupiedMarks(v, true);
            switch (direction) {
                case 0:
                    info.cellX -= step;
                    break;
                case 1:
                    info.cellY -= step;
                    break;
                case 2:
                    info.cellX += step;
                    break;
                case 3:
                    info.cellY += step;
                    break;
            }
            if (isItemPosInvalid(info.cellX, info.cellY, info.spanX, info.spanY)) {
                return false;
            }
            updateCellOccupiedMarks(v, false);
        }
        return true;
    }

    private void findViewsTouchEdge(View v, int direction, ArrayList<View> newAddingViews) {
        ItemInfo info = getChildInfo(v);
        int i;
        switch (direction) {
            case 0:
                int leftEdgeIndex = info.cellX - 1;
                if (leftEdgeIndex >= 0) {
                    i = info.cellY;
                    while (i < info.cellY + info.spanY) {
                        if (!(this.mOccupiedCell[leftEdgeIndex][i] == null || newAddingViews.contains(this.mOccupiedCell[leftEdgeIndex][i]))) {
                            newAddingViews.add(this.mOccupiedCell[leftEdgeIndex][i]);
                        }
                        i++;
                    }
                    return;
                }
                return;
            case 1:
                int topEdgeIndex = info.cellY - 1;
                if (topEdgeIndex >= 0) {
                    i = info.cellX;
                    while (i < info.cellX + info.spanX) {
                        if (!(this.mOccupiedCell[i][topEdgeIndex] == null || newAddingViews.contains(this.mOccupiedCell[i][topEdgeIndex]))) {
                            newAddingViews.add(this.mOccupiedCell[i][topEdgeIndex]);
                        }
                        i++;
                    }
                    return;
                }
                return;
            case 2:
                int rightEdgeIndex = info.cellX + info.spanX;
                if (rightEdgeIndex < this.mHCells) {
                    i = info.cellY;
                    while (i < info.cellY + info.spanY) {
                        if (!(this.mOccupiedCell[rightEdgeIndex][i] == null || newAddingViews.contains(this.mOccupiedCell[rightEdgeIndex][i]))) {
                            newAddingViews.add(this.mOccupiedCell[rightEdgeIndex][i]);
                        }
                        i++;
                    }
                    return;
                }
                return;
            case 3:
                int bottomEdgeIndex = info.cellY + info.spanY;
                if (bottomEdgeIndex < this.mVCells) {
                    i = info.cellX;
                    while (i < info.cellX + info.spanX) {
                        if (!(this.mOccupiedCell[i][bottomEdgeIndex] == null || newAddingViews.contains(this.mOccupiedCell[i][bottomEdgeIndex]))) {
                            newAddingViews.add(this.mOccupiedCell[i][bottomEdgeIndex]);
                        }
                        i++;
                    }
                    return;
                }
                return;
            default:
                return;
        }
    }

    void saveCurrentConfiguration(ViewConfiguration solution) {
        solution.clear();
        int[] cellXY = this.mCellXY;
        for (int i = 0; i < this.mTotalCells; i++) {
            positionIndexToCell(i, cellXY);
            if (this.mOccupiedCell[cellXY[0]][cellXY[1]] != null) {
                solution.addView(this.mOccupiedCell[cellXY[0]][cellXY[1]]);
            }
        }
    }

    private boolean reorderAllViews(int cellX, int cellY, int spanX, int spanY, ViewConfiguration currentState) {
        currentState.sortConfigurationByDirection(0);
        for (int i = 0; i < this.mHCells; i++) {
            Arrays.fill(this.mOccupiedCell[i], null);
        }
        ItemInfo dragViewInfo = getChildInfo(this.mCellBackground);
        dragViewInfo.cellX = cellX;
        dragViewInfo.cellY = cellY;
        dragViewInfo.spanX = spanX;
        dragViewInfo.spanY = spanY;
        updateCellOccupiedMarks(this.mCellBackground, false);
        Iterator i$ = currentState.sortedViews.iterator();
        while (i$.hasNext()) {
            if (!insertView((View) i$.next())) {
                return false;
            }
        }
        updateCellOccupiedMarks(this.mCellBackground, true);
        return true;
    }

    public void fillEmptyCellAuto(int cellX, int cellY) {
        if (cellX < this.mHCells && cellY < this.mVCells && this.mOccupiedCell[cellX][cellY] == null) {
            backupLayout();
            int currentEmptyIndex = cellToPositionIndex(cellX, cellY);
            for (int i = currentEmptyIndex + 1; i < this.mTotalCells; i++) {
                positionIndexToCell(i, this.mCellXY);
                View v = this.mOccupiedCell[this.mCellXY[0]][this.mCellXY[1]];
                if (v != null && (v.getTag() instanceof ItemInfo)) {
                    ItemInfo info = getChildInfo(v);
                    if (info.spanX <= 1 && info.spanY <= 1) {
                        this.mOccupiedCell[this.mCellXY[0]][this.mCellXY[1]] = null;
                        positionIndexToCell(currentEmptyIndex, this.mCellXY);
                        info.cellX = this.mCellXY[0];
                        info.cellY = this.mCellXY[1];
                        this.mOccupiedCell[this.mCellXY[0]][this.mCellXY[1]] = v;
                        currentEmptyIndex = i;
                    }
                }
            }
            saveCurrentLayout();
            post(new Runnable() {
                public void run() {
                    CellLayout.this.requestLayout();
                }
            });
        }
    }

    private boolean insertView(View v) {
        ItemInfo info = getChildInfo(v);
        boolean success = false;
        int[] result = findNearestVacantAreaByCellPos(info.cellX, info.cellY, info.spanX, info.spanY, false);
        if (result != null && result[0] >= 0 && result[1] >= 0) {
            info.cellX = result[0];
            info.cellY = result[1];
            success = true;
        }
        updateCellOccupiedMarks(v, false);
        return success;
    }

    private void rollbackLayout() {
        if (this.mLayoutBackupValid && copyOccupiedCells(this.mOccupiedCellBak, this.mOccupiedCell)) {
            relayoutByOccupiedCells();
        }
    }

    private void relayoutByOccupiedCells() {
        long accessTag = SystemClock.currentThreadTimeMillis();
        for (int y = 0; y < this.mVCells; y++) {
            for (int x = 0; x < this.mHCells; x++) {
                View v = this.mOccupiedCell[x][y];
                if (v != null) {
                    LayoutParams lp = (LayoutParams) v.getLayoutParams();
                    if (lp.accessTag != accessTag) {
                        ItemInfo info = getChildInfo(v);
                        info.cellX = x;
                        info.cellY = y;
                        lp.accessTag = accessTag;
                    }
                }
            }
        }
        requestLayout();
    }

    private void backupLayout() {
        copyOccupiedCells(this.mOccupiedCell, this.mOccupiedCellBak);
        this.mLayoutBackupValid = true;
    }

    private boolean copyOccupiedCells(View[][] src, View[][] dst) {
        boolean modified = false;
        for (int y = 0; y < this.mVCells; y++) {
            for (int x = 0; x < this.mHCells; x++) {
                if (dst[x][y] != src[x][y]) {
                    dst[x][y] = src[x][y];
                    modified = true;
                }
            }
        }
        return modified;
    }

    void onDragEnter(DragObject d) {
        this.mLastDragPos.reset();
        backupLayout();
        if (d.getOutline() != null) {
            this.mCellBackground.setImageBitmap(d.getOutline());
            this.mCellBackground.setScaleType(ScaleType.CENTER);
        } else {
            this.mCellBackground.setImageDrawable(this.mDefaultCellBackground);
            this.mCellBackground.setScaleType(ScaleType.FIT_XY);
        }
        this.mCellBackground.bindDragObject(d);
        this.mCellBackground.setSkipNextAutoLayoutAnimation(true);
        if (getScreenType() == 2) {
            showEmptyCellMark(true);
        }
    }

    void onDragExit(DragObject d) {
        clearDraggingState();
        if (!d.isDroped()) {
            rollbackLayout();
            clearBackupLayout();
            if (this.mLastCoveringView != null) {
                this.mLastCoveringView.onDragExit(d);
                this.mLastCoveringView = null;
            }
        }
        this.mCellBackground.setImageDrawable(null);
        this.mCellBackground.unbindDragObject(d);
        if (getScreenType() == 2) {
            showEmptyCellMark(false);
        }
    }

    void onDropStart(DragObject d) {
        if (!this.mLayoutBackupValid) {
            backupLayout();
        }
    }

    void clearDraggingState() {
        Handler h = getHandler();
        if (h != null) {
            h.removeCallbacks(this.mStayConfirm);
        }
        this.mStayConfirm.clear();
        clearCellBackground();
        this.mCellBackground.setImageDrawable(null);
        if (this.mMoveAwayShakeAnim.isRunning()) {
            this.mMoveAwayShakeAnim.cancel();
        }
    }

    private void saveCurrentLayout() {
        if (this.mLayoutBackupValid) {
            ArrayList<ContentProviderOperation> ops = new ArrayList();
            long accessTag = SystemClock.currentThreadTimeMillis();
            int y = 0;
            while (y < this.mVCells) {
                int x = 0;
                while (x < this.mHCells) {
                    View v = this.mOccupiedCell[x][y];
                    if (v != null) {
                        LayoutParams lp = (LayoutParams) v.getLayoutParams();
                        if (lp.accessTag != accessTag) {
                            lp.accessTag = accessTag;
                            ItemInfo info = getChildInfo(v);
                            if (v != this.mOccupiedCellBak[x][y] || v != this.mOccupiedCellBak[(info.spanX + x) - 1][(info.spanY + y) - 1]) {
                                info.cellX = x;
                                info.cellY = y;
                                ops.add(LauncherModel.makeMoveItemOperation(info, -100, getScreenId(), getScreenType(), x, y));
                            }
                        }
                    }
                    x++;
                }
                y++;
            }
            if (!ops.isEmpty()) {
                LauncherModel.applyBatch(this.mContext, "com.miui.home.launcher.settings", ops);
            }
            resetTouchCellInfo();
        }
    }

    void clearBackupLayout() {
        if (this.mLayoutBackupValid) {
            for (int y = 0; y < this.mVCells; y++) {
                for (int x = 0; x < this.mHCells; x++) {
                    this.mOccupiedCellBak[x][y] = null;
                }
            }
            this.mLayoutBackupValid = false;
        }
    }

    void onDragOver(DragObject d) {
        int spanX = d.getDragInfo().spanX;
        int spanY = d.getDragInfo().spanY;
        int[] cellPos = findNearestVacantArea(d.x - d.xOffset, d.y - d.yOffset, spanX, spanY, false);
        if (cellPos != null) {
            if (this.mLastCoveringView != null || d.isMultiDrag()) {
                removeView(this.mCellBackground);
            } else {
                ItemInfo info = getChildInfo(this.mCellBackground);
                if (!(info.cellX == cellPos[0] && info.cellY == cellPos[1] && info.spanX == spanX && info.spanY == spanY)) {
                    info.cellX = cellPos[0];
                    info.cellY = cellPos[1];
                    info.spanX = spanX;
                    info.spanY = spanY;
                    requestLayout();
                }
                if (this.mCellBackground.getParent() == null) {
                    addView(this.mCellBackground, 0, new LayoutParams());
                }
            }
            if (d.dragSource instanceof WidgetThumbnailView) {
                d.getDragInfo().cellX = cellPos[0];
                d.getDragInfo().cellY = cellPos[1];
            }
        }
        if (!(d.getDragInfo() instanceof LauncherAppWidgetProviderInfo)) {
            int i;
            this.mTmpDragPos.cellXY = findNearestVacantArea(d.x - d.xOffset, d.y - d.yOffset, 1, 1, true);
            View occupiedView = this.mOccupiedCell[this.mTmpDragPos.cellXY[0]][this.mTmpDragPos.cellXY[1]];
            if (spanX > 1 || spanY > 1) {
                if (this.mTmpDragPos.cellXY[0] + spanX > this.mHCells) {
                    this.mTmpDragPos.cellXY[0] = this.mHCells - spanX;
                }
                if (this.mTmpDragPos.cellXY[1] + spanY > this.mVCells) {
                    this.mTmpDragPos.cellXY[1] = this.mVCells - spanY;
                }
                this.mTmpDragPos.stayType = 4;
            } else {
                if (occupiedView != null && (occupiedView instanceof ItemIcon)) {
                    occupiedView.getHitRect(this.mRectTmp);
                    if (d.getDragInfo() instanceof FolderInfo) {
                        DragPos dragPos = this.mTmpDragPos;
                        if (d.x < this.mRectTmp.centerX()) {
                            i = 1;
                        } else {
                            i = 3;
                        }
                        dragPos.stayType = i;
                    } else if (d.x < this.mRectTmp.left + this.mStayConfirmSize) {
                        this.mTmpDragPos.stayType = 1;
                    } else if (d.x > this.mRectTmp.right - this.mStayConfirmSize) {
                        this.mTmpDragPos.stayType = 3;
                    } else if (this.mRectTmp.contains(d.x, (int) (((float) d.y) - (((float) this.mRectTmp.height()) * 0.4f)))) {
                        this.mTmpDragPos.stayType = 2;
                    } else {
                        this.mTmpDragPos.stayType = 0;
                        occupiedView = null;
                    }
                } else if (occupiedView == null) {
                    this.mTmpDragPos.stayType = 0;
                } else {
                    this.mTmpDragPos.stayType = 2;
                }
                if (!(this.mLastCoveringView == null || occupiedView == this.mLastCoveringView)) {
                    this.mLastCoveringView.onDragExit(d);
                    this.mLastCoveringView = null;
                }
            }
            if (System.currentTimeMillis() - this.mLastRelayoutTime > 300 && !this.mLastDragPos.equal(this.mTmpDragPos)) {
                long j;
                this.mLastDragPos.set(this.mTmpDragPos);
                getHandler().removeCallbacks(this.mStayConfirm);
                this.mStayConfirm.lastDragObject = d;
                Runnable runnable = this.mStayConfirm;
                if (this.mLastDragPos.stayType == 2) {
                    if (occupiedView instanceof FolderIcon) {
                        i = 100;
                    } else {
                        i = 100;
                    }
                    j = (long) i;
                } else {
                    j = 150;
                }
                postDelayed(runnable, j);
            }
        }
    }

    private int cellToPositionIndex(int cellX, int cellY) {
        if (isLayoutRtl()) {
            cellX = (this.mHCells - cellX) - 1;
        }
        return (this.mHCells * cellY) + cellX;
    }

    private void positionIndexToCell(int index, int[] cellXY) {
        cellXY[0] = isLayoutRtl() ? (this.mHCells - 1) - (index % this.mHCells) : index % this.mHCells;
        cellXY[1] = index / this.mHCells;
    }

    private int cellToGapIndex(int cellX, int cellY, int stayType) {
        return (stayType == 3 ? 1 : 0) + (((this.mHCells + 1) * cellY) + cellX);
    }

    private void gapToCellIndexes(int gap, int[] cellLR) {
        int i = -1;
        int gapV = gap % (this.mHCells + 1);
        int gapH = gap / (this.mHCells + 1);
        cellLR[0] = gapV == 0 ? -1 : ((this.mHCells * gapH) + gapV) - 1;
        if (gapV != this.mHCells) {
            i = (this.mHCells * gapH) + gapV;
        }
        cellLR[1] = i;
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public boolean lastDownOnOccupiedCell() {
        return this.mLastDownOnOccupiedCell;
    }

    public void removeChild(ItemInfo info) {
        View v = this.mOccupiedCell[info.cellX][info.cellY];
        if ($assertionsDisabled || v.getTag().equals(info)) {
            removeView(v);
            return;
        }
        throw new AssertionError();
    }

    public void removeChild(long id) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (getChildInfo(v).id == id) {
                removeView(v);
            }
        }
    }

    public void removeView(View view) {
        onRemoveViews(indexOfChild(view), 1);
        super.removeView(view);
    }

    public void removeViewInLayout(View view) {
        onRemoveViews(indexOfChild(view), 1);
        super.removeViewInLayout(view);
    }

    public void removeViewsInLayout(int start, int count) {
        onRemoveViews(start, count);
        super.removeViewsInLayout(start, count);
    }

    public void removeAllViewsInLayout() {
        onRemoveViews(0, getChildCount());
        super.removeAllViewsInLayout();
    }

    public void removeViewAt(int index) {
        onRemoveViews(index, 1);
        super.removeViewAt(index);
    }

    public void removeViews(int start, int count) {
        onRemoveViews(start, count);
        super.removeViews(start, count);
    }

    private void onRemoveViews(int start, int count) {
        if (start >= 0) {
            int count2 = count;
            while (true) {
                count = count2 - 1;
                if (count2 > 0) {
                    View child = getChildAt(start + count);
                    if (child != this.mCellBackground && getChildInfo(child).screenId == getScreenId()) {
                        updateCellOccupiedMarks(child, true);
                    }
                    count2 = count;
                } else {
                    return;
                }
            }
        }
    }

    public void onScreenOrientationChanged() {
        loadGridConfig();
    }

    public void onScreenSizeChanged() {
        loadGridConfig();
    }

    public boolean isScreenRotatable() {
        return getScreenType() != 0;
    }

    public void preRemoveView(View v) {
        if (this.mLastCoveringView == v) {
            this.mLastCoveringView = null;
        }
        updateCellOccupiedMarks(v, true);
        backupLayout();
    }

    private void calNextDstPos(int direction) {
        int[] iArr = this.mDstPos;
        iArr[0] = iArr[0] + direction;
        if (direction == -1 && this.mDstPos[0] < 0) {
            this.mDstPos[0] = this.mHCells - 1;
            iArr = this.mDstPos;
            iArr[1] = iArr[1] + 1;
        } else if (direction == 1 && this.mDstPos[0] >= this.mHCells) {
            this.mDstPos[0] = 0;
            iArr = this.mDstPos;
            iArr[1] = iArr[1] + 1;
        }
    }

    public void alignIconsToTop() {
        ArrayList<ContentProviderOperation> ops = new ArrayList();
        this.mDstPos[1] = 0;
        int srcH = 0;
        int direction = DeviceConfig.getIterateDirection(false);
        calCellXRange(direction);
        this.mDstPos[0] = this.mCellXIterateRange[0];
        int srcV = 0;
        while (srcV < this.mVCells) {
            srcH = this.mCellXIterateRange[0];
            while (true) {
                if (direction != 1) {
                    if (srcH < this.mCellXIterateRange[1]) {
                        break;
                    }
                } else if (srcH > this.mCellXIterateRange[1]) {
                    break;
                }
                View sv = this.mOccupiedCell[srcH][srcV];
                if (sv != null) {
                    ItemInfo info = getChildInfo(sv);
                    if (info.spanX == 1 && info.spanY == 1) {
                        while (this.mOccupiedCell[this.mDstPos[0]][this.mDstPos[1]] != null && (getChildInfo(this.mOccupiedCell[this.mDstPos[0]][this.mDstPos[1]]).spanX != 1 || getChildInfo(this.mOccupiedCell[this.mDstPos[0]][this.mDstPos[1]]).spanY != 1)) {
                            calNextDstPos(direction);
                        }
                        if (!(srcH == this.mDstPos[0] && srcV == this.mDstPos[1])) {
                            this.mOccupiedCell[this.mDstPos[0]][this.mDstPos[1]] = sv;
                            this.mOccupiedCell[srcH][srcV] = null;
                            info.cellX = this.mDstPos[0];
                            info.cellY = this.mDstPos[1];
                            ops.add(LauncherModel.makeMoveItemOperation(getChildInfo(sv), -100, getScreenId(), getScreenType(), this.mDstPos[0], this.mDstPos[1]));
                        }
                        calNextDstPos(direction);
                    }
                }
                srcH += direction;
            }
            srcV++;
        }
        if (!ops.isEmpty()) {
            performHapticFeedback(0, 1);
            try {
                this.mContext.getContentResolver().applyBatch("com.miui.home.launcher.settings", ops);
            } catch (RemoteException e) {
            } catch (OperationApplicationException e2) {
            } catch (SQLiteException e3) {
            }
            requestLayout();
        }
    }

    public void performDropFinishAnimation(View child) {
        if (this.mShakeAnim.isRunning()) {
            this.mShakeAnim.cancel();
        }
        this.mShakeSource = child;
        if (this.mShakeSource != null) {
            this.mShakeAnim.start();
        }
        DeviceConfig.performDropFinishVibration(this);
    }

    public void setDropAnimating(boolean isAnimating) {
        this.mDropAnimationCounter = (isAnimating ? 1 : -1) + this.mDropAnimationCounter;
    }

    public boolean isDropAnimating() {
        return this.mDropAnimationCounter != 0;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mEmptyCellMarkDrawableAlpha != 0 && this.mCellInfo.screenType != 0) {
            int y = this.mVCells;
            while (y >= 0) {
                int x = this.mHCells;
                while (x >= 0) {
                    ItemInfo info = null;
                    if (x < this.mHCells && y < this.mVCells) {
                        View child = this.mOccupiedCell[x][y];
                        if (child != null) {
                            info = getChildInfo(child);
                        }
                    }
                    if (info == null && this.mCellBackground.getParent() != null) {
                        info = getChildInfo(this.mCellBackground);
                    }
                    if (info == null || x <= info.cellX || x >= info.cellX + info.spanX || y <= info.cellY || y >= info.cellY + info.spanY) {
                        if (this.mCellInfo.screenType == 2) {
                            this.mEmptyCellMarkLP.setup(x, y, 1, 1, getWidgetCellWidth(), getWidgetCellHeight(), 0, 0, this.mWidgetCellPaddingLeft, this.mWidgetCellPaddingTop);
                        } else {
                            this.mEmptyCellMarkLP.setup(x, y, 1, 1, this.mCellWidth, this.mCellHeight, this.mWidthGap, this.mHeightGap, this.mCellPaddingLeft, this.mCellPaddingTop);
                        }
                        int left = this.mEmptyCellMarkLP.x - (this.mEmptyCellMarkDrawable.getIntrinsicWidth() / 2);
                        int top = this.mEmptyCellMarkLP.y - (this.mEmptyCellMarkDrawable.getIntrinsicHeight() / 2);
                        this.mEmptyCellMarkDrawable.setBounds(left, top, this.mEmptyCellMarkDrawable.getIntrinsicWidth() + left, this.mEmptyCellMarkDrawable.getIntrinsicHeight() + top);
                        this.mEmptyCellMarkDrawable.draw(canvas);
                    }
                    x--;
                }
                y--;
            }
        }
    }

    private void setEmptyCellMarkDrawableAlpha(int alpha) {
        if (this.mEmptyCellMarkDrawable != null) {
            this.mEmptyCellMarkDrawable.setAlpha(alpha);
            this.mEmptyCellMarkDrawableAlpha = alpha;
        }
    }

    public void showEmptyCellMark(boolean isShow) {
        if (this.mIsShowEmptyCellMark != isShow) {
            this.mIsShowEmptyCellMark = isShow;
            if (this.mEmptyCellMarkDrawable == null) {
                this.mEmptyCellMarkDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.editing_mode_empty_cell_mark);
                setEmptyCellMarkDrawableAlpha(0);
            }
            this.mEmptyCellMarkStartAlpha = this.mEmptyCellMarkDrawableAlpha;
            if (this.mEmptyCellMarkAnimator == null) {
                this.mEmptyCellMarkAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                this.mEmptyCellMarkAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float val = ((Float) animation.getAnimatedValue()).floatValue();
                        CellLayout.this.setEmptyCellMarkDrawableAlpha((int) (CellLayout.this.mIsShowEmptyCellMark ? ((float) (255 - CellLayout.this.mEmptyCellMarkStartAlpha)) * val : ((float) CellLayout.this.mEmptyCellMarkStartAlpha) * (1.0f - val)));
                        CellLayout.this.invalidate();
                    }
                });
                this.mEmptyCellMarkAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        if (!CellLayout.this.mIsShowEmptyCellMark) {
                            CellLayout.this.setWillNotDraw(true);
                        }
                    }
                });
            }
            setWillNotDraw(false);
            this.mEmptyCellMarkAnimator.start();
        }
    }
}
