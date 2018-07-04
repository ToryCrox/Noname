package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetHostView;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.R;
import com.miui.home.launcher.LauncherSettings.Screens;
import com.miui.home.launcher.ScreenView.SavedState;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.GadgetInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import miui.maml.RenderThread;
import miui.os.Build;

public class Workspace extends DragableScreenView implements DragSource, DropTarget, WallpaperColorChangedListener {
    public static final float SCREEN_TRANS_V_RATO = (Build.IS_TABLET ? -0.08f : -0.025f);
    private final AnimationListener ENTER_PREVIEW_ANIMATION_LISTENER;
    private final AnimationListener EXIT_PREVIEW_ANIMATION_LISTENER;
    private AccessibilityManager mAccessibilityManager;
    private Runnable mAutoScrollBack;
    private Runnable mAutoScrollEnd;
    private Runnable mCallbackAfterNextLayout;
    private long mCurrentScreenId;
    private long mDefaultScreenId;
    private DragController mDragController;
    private CellInfo mDragInfo;
    private boolean mEditingModeAnimating;
    private CellScreen mEditingNewScreenLeft;
    private CellScreen mEditingNewScreenRight;
    private boolean mEditingScreenChanging;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private boolean mInAutoInsertOrDeleteAnimation;
    private boolean mInDraggingMode;
    private int mInEditingMode;
    private int mIndicatorMarginBottom;
    private int mIndicatorOffsetBottomPortrait;
    private int mIndicatorShrinkBottom;
    private final LayoutInflater mInflater;
    private float mInitThreePinchSize;
    private long mLastDragScreenID;
    private int mLastTouchPointerCount;
    private View mLastWidgetView;
    private Launcher mLauncher;
    private int mMediumAnimTime;
    private long mNewScreenId;
    private int mOldTransitionType;
    private int mPreviousScreen;
    private Runnable mResetEditingViewsAfterScreenOrientationChanged;
    private ContentResolver mResolver;
    private ValueAnimator mScaleAnimator;
    private LongSparseArray<Integer> mScreenIdMap;
    private ArrayList<Long> mScreenIds;
    private int mShortAnimTime;
    private boolean mShowEditingIndicator;
    private boolean mShowingTransitionEffectDemo;
    private boolean mSkipDrawingChild;
    private int[] mTempCell;
    private WorkspaceThumbnailView mThumbnailView;
    private ValueAnimator mTransAnimator;
    private final WallpaperManager mWallpaperManager;

    public static final class CellInfo {
        int cellX;
        int cellY;
        long screenId;
        int screenOrder;
        int spanX;
        int spanY;

        boolean isWidgetFinding() {
            return DeviceConfig.isRotatable() && (this.spanX > 1 || this.spanY > 1);
        }

        void nextScreen() {
            if (!DeviceConfig.isRotatable() || !isWidgetFinding()) {
                this.screenOrder++;
            } else if (this.screenOrder > 0) {
                this.screenOrder--;
            } else {
                this.screenOrder = -1;
            }
        }
    }

    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPreviousScreen = -1;
        this.mLastDragScreenID = -1;
        this.mTempCell = new int[2];
        this.mCurrentScreenId = -1;
        this.mSkipDrawingChild = true;
        this.mInEditingMode = 7;
        this.mShowEditingIndicator = false;
        this.mInDraggingMode = false;
        this.mEditingModeAnimating = false;
        this.mEditingScreenChanging = false;
        this.mInAutoInsertOrDeleteAnimation = false;
        this.mTransAnimator = new ValueAnimator();
        this.mScaleAnimator = new ValueAnimator();
        this.mLastWidgetView = null;
        this.mNewScreenId = -1;
        this.mLastTouchPointerCount = 0;
        this.mInitThreePinchSize = 0.0f;
        this.mAutoScrollEnd = new Runnable() {
            public void run() {
                Workspace.this.mShowingTransitionEffectDemo = false;
            }
        };
        this.mScreenIds = new ArrayList();
        this.mScreenIdMap = new LongSparseArray();
        this.mResetEditingViewsAfterScreenOrientationChanged = new Runnable() {
            public void run() {
                if (Workspace.this.isInNormalEditingMode()) {
                    for (int i = 0; i < Workspace.this.getScreenCount(); i++) {
                        Workspace.this.getCellScreen(i).setEditMode(true, Integer.MIN_VALUE);
                    }
                }
            }
        };
        this.ENTER_PREVIEW_ANIMATION_LISTENER = new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                Workspace.this.setTouchState(null, 7);
                Workspace.this.setIndicatorBarVisibility(8);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                Workspace.this.setVisibility(4);
                Workspace.this.setTouchState(null, 0);
            }
        };
        this.EXIT_PREVIEW_ANIMATION_LISTENER = new AnimationListener() {
            public void onAnimationStart(Animation animation) {
                Workspace.this.setTouchState(null, 8);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                Workspace.this.mThumbnailView.setVisibility(4);
                Workspace.this.setTouchState(null, 0);
                Workspace.this.setIndicatorBarVisibility(0);
            }
        };
        this.mResolver = context.getContentResolver();
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService("accessibility");
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        Resources r = getResources();
        this.mShortAnimTime = r.getInteger(17694720);
        this.mMediumAnimTime = r.getInteger(17694721);
        loadIndicatorMarginBottom();
        String indicator = r.getString(R.string.home_indicator);
        LayoutParams params = new LayoutParams(0, r.getDimensionPixelSize(R.dimen.slide_bar_height));
        if (indicator.equals("bottom_point")) {
            params.width = -2;
            params.gravity = 81;
            params.bottomMargin = this.mIndicatorMarginBottom;
            setSeekPointResource(R.drawable.workspace_seekpoint);
            setSeekBarPosition(params);
            this.mShowEditingIndicator = true;
        } else if (indicator.equals("top_point")) {
            params.width = -2;
            params.gravity = 49;
            params.topMargin = r.getDimensionPixelSize(R.dimen.status_bar_height);
            setSeekBarPosition(params);
            this.mShowEditingIndicator = false;
        } else if (indicator.equals("slider")) {
            params.width = -1;
            params.gravity = 80;
            params.bottomMargin = this.mIndicatorMarginBottom;
            setSlideBarPosition(params);
            this.mShowEditingIndicator = false;
        }
        setAnimationCacheEnabled(false);
        setMaximumSnapVelocity(6000);
        if (Launcher.isClipTransitionDevice()) {
            setClip(false);
        }
    }

    boolean isDefaultScreenShowing() {
        if (!isScrolling() && this.mCurrentScreen == getDefaultScreenIndex()) {
            return true;
        }
        return false;
    }

    protected boolean isScrolling() {
        return !this.mScroller.isFinished();
    }

    public boolean isTouchStateNotInScroll() {
        return getTouchState() == 0 || getTouchState() == 4;
    }

    private boolean isInNormalEditingMode() {
        return (this.mInEditingMode == 7 || this.mInEditingMode == 9) ? false : true;
    }

    private boolean isInQuickEditingMode() {
        return this.mInEditingMode == 9;
    }

    public boolean isScreenHasClockGadget(long screenId) {
        ArrayList<Gadget> gadgets = this.mLauncher.mGadgets;
        for (int i = gadgets.size() - 1; i >= 0; i--) {
            GadgetInfo info = (GadgetInfo) ((Gadget) gadgets.get(i)).getTag();
            if (info.screenId == screenId && info.getCategoryId() == 2) {
                return true;
            }
        }
        return false;
    }

    protected void setCurrentScreenInner(int screenIndex) {
        long screenId = getScreenIdByIndex(screenIndex);
        if (!(screenId == this.mCurrentScreenId || this.mLauncher == null || this.mEditingScreenChanging)) {
            if (this.mLauncher.mGadgets != null) {
                long currentId = this.mCurrentScreenId;
                long nextId = screenId;
                ArrayList<Gadget> gadgets = this.mLauncher.mGadgets;
                for (int i = gadgets.size() - 1; i >= 0; i--) {
                    Gadget gadget = (Gadget) gadgets.get(i);
                    GadgetInfo info = (GadgetInfo) gadget.getTag();
                    if (info.screenId == currentId) {
                        gadget.onPause();
                    } else if (info.screenId == nextId) {
                        gadget.onResume();
                    }
                }
            }
            CellScreen cs = getCellScreen(getScreenIndexById(this.mCurrentScreenId));
            if (cs != null) {
                cs.onInvisible();
            }
            cs = getCellScreen(screenIndex);
            if (cs != null) {
                cs.onVisible();
            }
            this.mCurrentScreenId = screenId;
        }
        RenderThread.globalThread().setPaused(false);
        announceForAccessibilityIfNeed(screenIndex);
        super.setCurrentScreenInner(screenIndex);
        this.mLauncher.updateStatusBarClock();
    }

    private void announceForAccessibilityIfNeed(int currentScreenIndex) {
        if (this.mAccessibilityManager.isEnabled()) {
            int count = 0;
            if (!this.mLauncher.isWorkspaceLoading()) {
                CellLayout currentScreen = getCellLayout(currentScreenIndex);
                for (int i = 0; i < currentScreen.getChildCount(); i++) {
                    View item = currentScreen.getChildAt(i);
                    if ((item instanceof ShortcutIcon) || (item instanceof FolderIcon)) {
                        count++;
                    }
                }
            }
            announceForAccessibility(String.format(getResources().getString(R.string.scroll_tip_format_when_announce_accessibility), new Object[]{Integer.valueOf(currentScreenIndex + 1), Integer.valueOf(count)}));
        }
    }

    public void setCurrentScreenById(long screenId) {
        setCurrentScreen(Math.max(0, getScreenIndexById(screenId)));
    }

    long getCurrentScreenId() {
        if (getCurrentScreenIndex() == -1 || getCurrentCellLayout() == null) {
            return -1;
        }
        return getCurrentCellLayout().getScreenId();
    }

    public int getScreenType(int index) {
        return getCellLayout(index).getScreenType();
    }

    public int getCurrentScreenType() {
        return getScreenType(getCurrentScreenIndex());
    }

    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, getScreenIdByIndex(this.mCurrentScreen), x, y, spanX, spanY, insert);
        if (child instanceof AppWidgetHostView) {
            this.mLastWidgetView = child;
        }
    }

    void addInScreen(View child, long screenId, int x, int y, int spanX, int spanY) {
        addInScreen(child, screenId, x, y, spanX, spanY, false);
    }

    void addInScreen(View child, long screenId, int x, int y, int spanX, int spanY, boolean insert) {
        int i = 0;
        int screen = getScreenIndexById(screenId);
        if (screen < 0) {
            loadScreens(false, true);
            screen = getScreenIndexById(screenId);
            if (screen < 0) {
                Log.e("Launcher.Workspace", "The screen must be >= 0; skipping child");
                return;
            }
        }
        CellLayout group = getCellLayout(screen);
        if (!insert) {
            i = -1;
        }
        group.addView(child, i, new CellLayout.LayoutParams());
        if (screenId == getCurrentScreenId()) {
            if (this.mLauncher.isInEditing() && this.mLauncher.isFolderShowing()) {
                child.setAlpha(0.0f);
            }
            if (child instanceof AppWidgetHostView) {
                this.mLastWidgetView = child;
            }
        }
        getCellScreen(screen).updateLayout();
    }

    public void addInScreen(View child, long screenId, int x, int y, int spanX, int spanY, boolean insert, boolean showAnimation, boolean showInstallAnim) {
        addInScreen(child, screenId, x, y, spanX, spanY, insert);
        if (!showAnimation) {
            return;
        }
        if (isInNormalEditingMode() && this.mLauncher.isFolderShowing()) {
            child.setAlpha(0.0f);
        } else if ((child instanceof ShortcutIcon) && showInstallAnim) {
            ((ShortcutIcon) child).showInstallingAnim();
        } else if (this.mLauncher.isResumed()) {
            child.setAlpha(0.0f);
            child.animate().alpha(1.0f).start();
        }
    }

    public boolean updateWallpaperOffsetDuringSwitchingPreview() {
        IBinder token = getWindowToken();
        Animation currentAnimation = getScreen(this.mCurrentScreen).getAnimation();
        if (token == null || currentAnimation == null || currentAnimation.getStartTime() == -1) {
            return false;
        }
        float percentage = Math.max(0.0f, Math.min(((float) (SystemClock.uptimeMillis() - currentAnimation.getStartTime())) / ((float) currentAnimation.getDuration()), 1.0f));
        float xStep = getScreenCount() == 1 ? 0.0f : 1.0f / ((float) (getScreenCount() - 1));
        return this.mLauncher.updateWallpaperOffset(xStep, 0.0f, Math.max(0.0f, Math.min(getScreenCount() == 1 ? 0.0f : xStep * ((((float) this.mPreviousScreen) * (1.0f - percentage)) + (((float) this.mCurrentScreen) * percentage)), 1.0f)), 0.0f);
    }

    private boolean updateWallpaperOffset() {
        int i = 0;
        if (getScreenCount() <= 0 || getWidth() <= 0) {
            return false;
        }
        if (getTouchState() == 8) {
            return updateWallpaperOffsetDuringSwitchingPreview();
        }
        if (!DeviceConfig.isLayoutRtl()) {
            i = getScreenCount() - 1;
        }
        return updateWallpaperOffset(getScreen(i).getRight() - ((isInNormalEditingMode() ? 3 : 1) * getWidth()));
    }

    private boolean updateWallpaperOffset(int scrollRange) {
        int i = 0;
        if (getWindowToken() == null) {
            return false;
        }
        float offsetX;
        float steps = getScreenCount() == 1 ? 0.0f : 1.0f / ((float) (getScreenCount() - 1));
        if (getScreenCount() == 1) {
            offsetX = 0.0f;
        } else {
            int i2 = this.mScrollX;
            if (isInNormalEditingMode()) {
                i = getWidth();
            }
            offsetX = Math.max(0.0f, Math.min(((float) (i2 - i)) / ((float) scrollRange), 1.0f));
        }
        return this.mLauncher.updateWallpaperOffset(steps, 0.0f, offsetX, 0.0f);
    }

    private void updateHotseatPosition() {
        if (DeviceConfig.isRotatable() && isScrolling() && !this.mLauncher.isInEditing() && !this.mLauncher.getFolderCling().isOpened() && !inEditingModeAnimating() && getChildScreenMeasureWidth() > 0) {
            int currentIndex = getScreenIndexByPoint(this.mScrollX, 0);
            int currentPos = getScreenLayoutX(currentIndex);
            CellLayout current = getCellLayout(currentIndex);
            CellLayout next = getCellLayout(getNextScreenIndex(currentIndex));
            if (next != null) {
                HotSeats hs = this.mLauncher.getHotSeats();
                float interpolation = ((float) (this.mScrollX - currentPos)) / ((float) getChildScreenMeasureWidth());
                if (current.getScreenType() != 2 || next.getScreenType() == 2) {
                    if (current.getScreenType() == 2 || next.getScreenType() != 2) {
                        interpolation = current.getScreenType() == 2 ? 0.0f : 1.0f;
                    } else {
                        interpolation = 1.0f - interpolation;
                    }
                }
                hs.setAlpha(interpolation);
                hs.setTranslationY(((float) hs.getHeight()) * (1.0f - interpolation));
                float indicatorTy = ((float) this.mIndicatorShrinkBottom) * (1.0f - interpolation);
                if (this.mScreenSeekBar != null) {
                    this.mScreenSeekBar.setTranslationY(indicatorTy);
                }
                if (this.mSlideBar != null) {
                    this.mSlideBar.setTranslationY(indicatorTy);
                }
            }
        }
    }

    private int getNextScreenIndex(int currentIndex) {
        if (!DeviceConfig.isLayoutRtl()) {
            return currentIndex + 1;
        }
        if (this.mScrollX < getScreenLayoutX(currentIndex)) {
            return currentIndex + 1;
        }
        return currentIndex - 1;
    }

    public void computeScroll() {
        super.computeScroll();
        updateWallpaperOffset();
        updateHotseatPosition();
    }

    protected void dispatchGetDisplayList() {
        ensureChildrenVisibility();
        super.dispatchGetDisplayList();
    }

    protected void dispatchDraw(Canvas canvas) {
        ensureChildrenVisibility();
        super.dispatchDraw(canvas);
    }

    private void ensureChildrenVisibility() {
        boolean onlyCurrentVisible;
        int leftScreen = -1;
        int touchState = getTouchState();
        if (this.mNextScreen == -1 && (touchState == 0 || touchState == 7 || touchState == 8)) {
            onlyCurrentVisible = true;
        } else {
            onlyCurrentVisible = false;
        }
        int screenCount = getScreenCount();
        int i;
        int needVisibility;
        View v;
        if (onlyCurrentVisible) {
            for (i = 0; i < screenCount; i++) {
                if (i == getCurrentScreenIndex()) {
                    needVisibility = 0;
                } else {
                    needVisibility = 4;
                }
                v = getScreen(i);
                if (v.getVisibility() != needVisibility) {
                    v.setVisibility(needVisibility);
                }
            }
            return;
        }
        float scrollPos = ((float) this.mScrollX) / ((float) getWidth());
        if (scrollPos >= 0.0f) {
            leftScreen = (int) scrollPos;
        }
        int rightScreen = leftScreen + 1;
        i = 0;
        while (i < screenCount) {
            if (i == leftScreen || i == rightScreen) {
                needVisibility = 0;
            } else {
                needVisibility = 4;
            }
            v = getScreen(getVisualPosition(i));
            if (v.getVisibility() != needVisibility) {
                v.setVisibility(needVisibility);
            }
            i++;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mDragController.setWindowToken(getWindowToken());
    }

    protected void onFinishInflate() {
        this.mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        this.mFadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        this.mEditingNewScreenLeft = (CellScreen) this.mInflater.inflate(R.layout.cell_screen, this, false);
        this.mEditingNewScreenLeft.setEditingNewScreenMode();
        this.mEditingNewScreenRight = (CellScreen) this.mInflater.inflate(R.layout.cell_screen, this, false);
        this.mEditingNewScreenRight.setEditingNewScreenMode();
        if (DeviceConfig.isRotatable()) {
            this.mEditingNewScreenLeft.setScreenType(2);
            this.mEditingNewScreenRight.setScreenType(1);
        } else {
            this.mEditingNewScreenLeft.setScreenType(0);
            this.mEditingNewScreenRight.setScreenType(0);
        }
        this.mTransAnimator.setInterpolator(new LinearInterpolator());
        this.mTransAnimator.setDuration((long) this.mShortAnimTime);
        this.mScaleAnimator.setInterpolator(new LinearInterpolator());
        this.mScaleAnimator.setDuration((long) this.mShortAnimTime);
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = (SavedState) super.onSaveInstanceState();
        state.currentScreen = this.mLauncher.isInNormalEditing() ? this.mCurrentScreen - 1 : this.mCurrentScreen;
        return state;
    }

    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if (getScreenCount() == 0) {
            return false;
        }
        View openFolder = this.mLauncher.getCurrentOpenedFolder();
        if (openFolder != null) {
            return openFolder.requestFocus(direction, previouslyFocusedRect);
        }
        int focusableScreen;
        if (this.mNextScreen != -1) {
            focusableScreen = this.mNextScreen;
        } else {
            focusableScreen = this.mCurrentScreen;
        }
        if (focusableScreen == -1) {
            return false;
        }
        getScreen(focusableScreen).requestFocus(direction, previouslyFocusedRect);
        return false;
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (this.mLauncher.getCurrentOpenedFolder() == null && getCurrentScreen() != null) {
            getCurrentScreen().addFocusables(views, direction);
            View other = null;
            if (direction == 17) {
                other = getScreen(this.mCurrentScreen - 1);
            } else if (direction == 66) {
                other = getScreen(this.mCurrentScreen + 1);
            }
            if (other != null) {
                other.addFocusables(views, direction);
            }
        }
    }

    private float getThreePinchSize(MotionEvent ev) {
        return (float) (((((Math.pow((double) (ev.getX(0) - ev.getX(1)), 2.0d) + Math.pow((double) (ev.getY(0) - ev.getY(1)), 2.0d)) + Math.pow((double) (ev.getX(1) - ev.getX(2)), 2.0d)) + Math.pow((double) (ev.getY(1) - ev.getY(2)), 2.0d)) + Math.pow((double) (ev.getX(2) - ev.getX(0)), 2.0d)) + Math.pow((double) (ev.getY(2) - ev.getY(0)), 2.0d));
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        int pointerCount = ev.getPointerCount();
        if (!(DeviceConfig.isRotatable() || this.mEditingModeAnimating || this.mThumbnailView.isShowing() || getTouchState() != 0 || 3 != pointerCount)) {
            if (this.mLastTouchPointerCount != pointerCount) {
                this.mInitThreePinchSize = getThreePinchSize(ev);
            } else if (!this.mLauncher.isInEditing() && this.mInitThreePinchSize * 0.9f > getThreePinchSize(ev)) {
                finishCurrentGesture();
                this.mLauncher.showPreview(true, true);
            }
        }
        this.mLastTouchPointerCount = pointerCount;
        if (ev.getAction() == 0 && this.mLauncher.isWorkspaceLocked()) {
            return false;
        }
        switch (ev.getAction()) {
            case 0:
                RenderThread.globalThread().setPaused(true);
                break;
            case 1:
            case 3:
                if (getTouchState() != 1) {
                    RenderThread.globalThread().setPaused(false);
                    break;
                }
                break;
            case 2:
                if (this.mInAutoInsertOrDeleteAnimation) {
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public void finishCurrentGesture() {
        super.finishCurrentGesture();
    }

    protected boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        if (!(child instanceof CellScreen) || getCurrentCellScreen() == child) {
            return super.isTransformedTouchPointInView(x, y, child, outLocalPoint);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mLauncher.isWorkspaceLocked() || this.mLauncher.isFolderShowing()) {
            return false;
        }
        switch (ev.getAction() & 255) {
            case 1:
            case 3:
                if (getTouchState() == 0 && !getCellLayout(this.mCurrentScreen).lastDownOnOccupiedCell()) {
                    getLocationOnScreen(this.mTempCell);
                    this.mWallpaperManager.sendWallpaperCommand(getWindowToken(), "android.wallpaper.tap", this.mTempCell[0] + ((int) ev.getX(0)), this.mTempCell[1] + ((int) ev.getY(0)), 0, null);
                    break;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    protected void onPinchIn(ScaleGestureDetector detector) {
        if (!(this.mLauncher.isInNormalEditing() || this.mThumbnailView.isShowing())) {
            finishCurrentGesture();
            post(new Runnable() {
                public void run() {
                    AnalyticalDataCollector.trackEditMode("pinch");
                    Workspace.this.mLauncher.setEditingState(8);
                }
            });
        }
        super.onPinchIn(detector);
    }

    protected void onPinchOut(ScaleGestureDetector detector) {
        if (this.mLauncher.isInNormalEditing() && !this.mThumbnailView.isShowing()) {
            finishCurrentGesture();
            post(new Runnable() {
                public void run() {
                    Workspace.this.mLauncher.setEditingState(7);
                }
            });
        }
        super.onPinchOut(detector);
    }

    public void onSecondaryPointerDown(MotionEvent ev, int pointerId) {
        if (!this.mLauncher.isFolderShowing()) {
            super.onSecondaryPointerDown(ev, pointerId);
        }
    }

    public void onSecondaryPointerUp(MotionEvent ev, int pointerId) {
        if (!this.mLauncher.isFolderShowing()) {
            super.onSecondaryPointerUp(ev, pointerId);
        }
    }

    public void onSecondaryPointerMove(MotionEvent ev, int pointerId) {
        if (!this.mLauncher.isFolderShowing()) {
            super.onSecondaryPointerMove(ev, pointerId);
        }
    }

    protected boolean onVerticalGesture(int direction, MotionEvent event) {
        if (direction != 11) {
            if (direction == 10 && !Build.IS_INTERNATIONAL_BUILD) {
                this.mLauncher.onSearchRequested();
            }
            return false;
        } else if (event.getPointerCount() != 1) {
            return true;
        } else {
            this.mLauncher.expandStatusBar();
            return true;
        }
    }

    public void focusableViewAvailable(View focused) {
        View current = getScreen(this.mCurrentScreen);
        View v = focused;
        while (v != current) {
            if (v != this && (v.getParent() instanceof View)) {
                v = (View) v.getParent();
            } else {
                return;
            }
        }
        super.focusableViewAvailable(focused);
    }

    public boolean isShowingTransitionEffectDemo() {
        return this.mShowingTransitionEffectDemo;
    }

    public void autoShowTransitionEffectDemo() {
        int duration;
        getHandler().removeCallbacks(this.mAutoScrollBack);
        this.mShowingTransitionEffectDemo = true;
        int count = getScreenCount();
        final int finalScreen = this.mCurrentScreen;
        if (this.mCurrentScreen < count - 1) {
            duration = snapToScreen(this.mCurrentScreen + 1, 0, true);
        } else {
            duration = snapToScreen(this.mCurrentScreen - 1, 0, true);
        }
        this.mAutoScrollBack = new Runnable() {
            public void run() {
                Workspace.this.postDelayed(Workspace.this.mAutoScrollEnd, (long) Workspace.this.snapToScreen(finalScreen, 0, true));
            }
        };
        postDelayed(this.mAutoScrollBack, (long) (duration + 200));
    }

    protected int snapToScreen(int whichScreen, int velocity, boolean settle) {
        whichScreen = Math.max(0, Math.min(whichScreen, getScreenCount() - 1));
        this.mNextScreen = whichScreen;
        View focusedChild = getFocusedChild();
        if (!(focusedChild == null || whichScreen == this.mCurrentScreen || focusedChild != getScreen(this.mCurrentScreen))) {
            focusedChild.clearFocus();
        }
        return super.snapToScreen(whichScreen, velocity, settle);
    }

    void startDrag(CellInfo cellInfo) {
        View child = cellInfo.cell;
        if (child.isInTouchMode() && !this.mLauncher.isFolderShowing()) {
            this.mDragInfo = cellInfo;
            child.clearFocus();
            child.setPressed(false);
            if (this.mDragController.startDrag(child, true, this, 0)) {
                CellLayout current = getCurrentCellLayout();
                this.mDragInfo.screenId = current.getScreenId();
                current.onDragChild(child);
            }
            invalidate();
        }
    }

    public void onDropStart(DragObject dragObject) {
        CellScreen cs = getCurrentCellScreen();
        cs.clearDraggingState();
        if (cs.isEditingNewScreenMode()) {
            insertNewScreen(-1, false);
            cs = getCurrentCellScreen();
        }
        cs.onDropStart(dragObject);
    }

    public boolean onDrop(DragObject d) {
        CellScreen cellScreen;
        if (d.dropAction == 3) {
            cellScreen = getCellScreen(getScreenIndexById(d.getDragInfo().screenId));
            if (cellScreen == null) {
                return false;
            }
        }
        cellScreen = getCurrentCellScreen();
        if (d.dragSource != this) {
            return onDropExternal(cellScreen, d);
        }
        if (this.mDragInfo == null) {
            return true;
        }
        View cell = this.mDragInfo.cell;
        boolean r = cellScreen.onDrop(d, cell);
        if (r) {
            if (d.getDragInfo().screenId != this.mDragInfo.screenId) {
                getCellScreen(getScreenIndexById(this.mDragInfo.screenId)).updateLayout();
                if (d.getDragInfo().container == -100) {
                    cellScreen.updateLayout();
                    if (cell instanceof Gadget) {
                        ((Gadget) cell).onResume();
                        onAlertGadget(d.getDragInfo());
                    }
                }
            }
            if (!isInNormalEditingMode() && (cell instanceof LauncherAppWidgetHostView)) {
                final CellLayout cellLayout = cellScreen.getCellLayout();
                final ItemInfo info = d.getDragInfo();
                final LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) cell;
                if (hostView.getAppWidgetInfo().resizeMode != 0) {
                    post(new Runnable() {
                        public void run() {
                            Workspace.this.mLauncher.getDragLayer().addResizeFrame(info, hostView, cellLayout);
                        }
                    });
                }
            }
        } else {
            this.mLauncher.showError(R.string.failed_to_drop);
        }
        if (!d.getDragView().hasDrawn() || cell.getParent() == null) {
            cell.setVisibility(0);
            return r;
        } else if (cell.getParent() == null) {
            return r;
        } else {
            d.getDragView().setAnimateTarget(cell);
            return r;
        }
    }

    public void onDragEnter(DragObject d) {
        this.mInDraggingMode = true;
    }

    public void getHitRect(Rect outRect) {
        super.getHitRect(outRect);
        outRect.bottom -= this.mIndicatorMarginBottom;
    }

    public View getHitView() {
        return this;
    }

    public void onDragOver(DragObject d) {
        if (acceptDrop(d)) {
            CellScreen cs = getCurrentCellScreen();
            CellLayout cl = cs.getCellLayout();
            if (this.mLastDragScreenID != cs.getCellLayout().getScreenId()) {
                if (this.mLastDragScreenID != -1) {
                    getCellScreen(getScreenIndexById(this.mLastDragScreenID)).onDragExit(d);
                }
                cs.onDragEnter(d);
                this.mLastDragScreenID = cl.getScreenId();
            }
            getCurrentCellScreen().onDragOver(d);
        }
    }

    public void onDragExit(DragObject d) {
        if (this.mInDraggingMode) {
            this.mInDraggingMode = false;
        }
        if (!(this.mLastDragScreenID == -1 || this.mLastDragScreenID == getCurrentCellLayout().getScreenId())) {
            getCellScreen(getScreenIndexById(this.mLastDragScreenID)).onDragExit(d);
        }
        this.mLastDragScreenID = -1;
        getCurrentCellScreen().onDragExit(d);
    }

    public DropTarget getDropTargetDelegate(DragObject d) {
        return null;
    }

    public void onDropBack(DragObject d) {
        ItemInfo info = d.getDragInfo();
        DragView dragView = d.getDragView();
        View v = d.getDragView().getContent();
        getCellLayout(getScreenIndexById(info.screenId)).addView(v, -1, v.getLayoutParams());
        dragView.setAnimateTarget(v);
    }

    public void onDropCompleted() {
        CellLayout cellLayout = getCurrentCellLayout();
        if (cellLayout != null) {
            cellLayout.onDropCompleted();
        }
    }

    private boolean onDropExternal(CellScreen cellScreen, DragObject d) {
        boolean r = false;
        View view = null;
        ItemInfo info = d.getDragInfo();
        CellLayout cellLayout = cellScreen.getCellLayout();
        info.isLandscapePos = DeviceConfig.isScreenOrientationLandscape();
        if (info.itemType != 0 && info.itemType != 1 && info.itemType != 11 && (info.itemType != 2 || info.id == -1)) {
            int[] slot = cellScreen.findDropTargetPosition(d);
            if (slot != null) {
                info.cellX = slot[0];
                info.cellY = slot[1];
                switch (info.itemType) {
                    case 2:
                        ((FolderInfo) d.getDragInfo()).setBuddyIconView(null);
                        view = this.mLauncher.addFolderToCurrentScreen((FolderInfo) info, slot[0], slot[1]);
                        r = true;
                        break;
                    case 5:
                        info.container = -100;
                        info.screenId = getCurrentScreenId();
                        view = this.mLauncher.addGadget((GadgetInfo) info, true);
                        r = true;
                        break;
                    case 6:
                        int id = this.mLauncher.addAppWidget((LauncherAppWidgetProviderInfo) info);
                        if (this.mLastWidgetView != null && id == ((LauncherAppWidgetInfo) this.mLastWidgetView.getTag()).appWidgetId) {
                            view = this.mLastWidgetView;
                        }
                        this.mLastWidgetView = null;
                        r = true;
                        break;
                    case 7:
                        Intent intent = new Intent("android.intent.action.CREATE_SHORTCUT");
                        intent.setComponent(((ShortcutProviderInfo) info).mComponentName);
                        this.mLauncher.onDropShortcut(d, intent);
                        r = true;
                        break;
                    case 8:
                        if (((ShortcutPlaceholderProviderInfo) info).addType != 4) {
                            if (((ShortcutPlaceholderProviderInfo) info).addType == 5) {
                                view = this.mLauncher.onDropSettingShortcut(d);
                                break;
                            }
                        }
                        view = this.mLauncher.onDropToggleShortcut(d);
                        break;
                        break;
                    default:
                        throw new IllegalStateException("Unknown item type: " + info.itemType);
                }
                if (info.itemType != 2) {
                    AnalyticalDataCollector.trackAddWidget();
                }
            } else {
                this.mLauncher.showError(R.string.failed_to_drop_widget_nospace);
            }
        } else if (d.getDragInfo().container == -1) {
            r = true;
        } else {
            if (d.getDragInfo() instanceof ShortcutInfo) {
                ((ShortcutInfo) d.getDragInfo()).setBuddyIconView(null, null);
            } else if (d.getDragInfo() instanceof FolderInfo) {
                ((FolderInfo) d.getDragInfo()).setBuddyIconView(null);
            }
            view = this.mLauncher.createItemIcon(cellLayout, d.getDragInfo());
            view.setVisibility(4);
            if (cellScreen.onDrop(d, view)) {
                r = true;
            } else if (d.dropAction != 4) {
                if (!(d.dropAction == 3 || d.atLeastOneDropSucceeded())) {
                    this.mLauncher.showError(R.string.failed_to_drop);
                }
                view = null;
            } else if (getLastCellScreen().onDrop(d, view)) {
                r = true;
            } else {
                insertNewScreen(getScreenCount() - 2, false);
                CellScreen lastScreen = getLastCellScreen();
                lastScreen.clearDraggingState();
                if (lastScreen.onDrop(d, view)) {
                    r = true;
                } else {
                    this.mLauncher.showError(R.string.failed_to_drop);
                }
            }
        }
        if (view != null) {
            view.setHapticFeedbackEnabled(false);
            if (info.container == -100) {
                getCellScreen(getScreenIndexById(info.screenId)).updateLayout();
            }
            DragView dv = d.getDragView();
            if (!(d == null || view.getParent() == null)) {
                dv.setAnimateTarget(view);
                if (d.dragSource instanceof WidgetThumbnailView) {
                    dv.setFadeoutAnimationMode();
                }
            }
        }
        return r;
    }

    public boolean createUserFolderWithDragOverlap(DragObject dragObject, ShortcutInfo overItem) {
        CellLayout cl = getCellLayout(getScreenIndexById(overItem.screenId));
        if (cl == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("overItem.screenId=").append(overItem.screenId);
            sb.append(",currScreenId=").append(getCurrentScreenId());
            sb.append(",mScreenIdMap=");
            for (int i = 0; i < this.mScreenIdMap.size(); i++) {
                long key = this.mScreenIdMap.keyAt(i);
                sb.append(key).append(":");
                sb.append(this.mScreenIdMap.get(key));
            }
            throw new NullPointerException(sb.toString());
        }
        int[] cellXY = this.mTempCell;
        FolderIcon folder = null;
        if (cl.getChildVisualPosByTag(overItem, cellXY)) {
            folder = this.mLauncher.createNewFolder(overItem.screenId, cellXY[0], cellXY[1]);
        }
        if (folder == null) {
            return false;
        }
        folder.updateFolderTilte((ShortcutInfo) dragObject.getDragInfo(), overItem);
        overItem.cellX = cellXY[0];
        overItem.cellY = cellXY[1];
        cl.removeChild((ItemInfo) overItem);
        cl.clearBackupLayout();
        addInScreen(folder, overItem.screenId, cellXY[0], cellXY[1], 1, 1);
        if (LauncherModel.dropDragObjectIntoFolder(this.mContext, overItem, dragObject, (FolderInfo) folder.getTag()) == 0) {
            return false;
        }
        folder.onDragExit(null);
        return true;
    }

    public boolean isDropEnabled() {
        return !this.mLauncher.isFolderShowing();
    }

    public boolean acceptDrop(DragObject d) {
        boolean isWidgetScreen = true;
        if (this.mInAutoInsertOrDeleteAnimation || !this.mScroller.isFinished() || d.getDragInfo().itemType == 12) {
            return false;
        }
        ItemInfo info = d.getDragInfo();
        if ((info.spanX > 1 || info.spanY > 1) && getCurrentScreenType() == 1) {
            return false;
        }
        if (getCurrentScreenType() != 2) {
            isWidgetScreen = false;
        }
        if (info.spanX > (isWidgetScreen ? DeviceConfig.getWidgetCellCountX() : DeviceConfig.getCellCountX())) {
            return false;
        }
        if (info.spanY > (isWidgetScreen ? DeviceConfig.getWidgetCellCountY() : DeviceConfig.getCellCountY())) {
            return false;
        }
        if (((info instanceof FolderInfo) || (info instanceof ShortcutInfo) || d.isMultiDrag()) && getCurrentScreenType() == 2 && d.dropAction != 3) {
            return false;
        }
        return this.mScroller.isFinished();
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void setDragController(DragController dragController) {
        this.mDragController = dragController;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        if (!d.isAllDropedSuccess()) {
            if (this.mDragInfo != null) {
                getCellLayout(getScreenIndexById(this.mDragInfo.screenId)).onDropAborted(this.mDragInfo.cell);
            }
            if (target == this && getCurrentScreenId() != this.mDragInfo.screenId) {
                this.mLauncher.showError(R.string.failed_to_drop);
            }
        } else if (!(target == this || this.mDragInfo == null)) {
            getCurrentCellScreen().updateLayout();
        }
        this.mDragInfo = null;
    }

    public void onWallpaperColorChanged() {
        setSeekPointResource(WallpaperUtils.hasAppliedLightWallpaper() ? R.drawable.workspace_seekpoint_dark : R.drawable.workspace_seekpoint);
        for (int i = 0; i < getScreenCount(); i++) {
            final View child = getScreen(i);
            if (child instanceof WallpaperColorChangedListener) {
                post(new Runnable() {
                    public void run() {
                        ((WallpaperColorChangedListener) child).onWallpaperColorChanged();
                    }
                });
            }
        }
    }

    public void removeShortcuts(ArrayList<ShortcutInfo> shortcuts) {
        Iterator i$ = shortcuts.iterator();
        while (i$.hasNext()) {
            ShortcutInfo info = (ShortcutInfo) i$.next();
            CellLayout cl = getCellLayoutById(info.screenId);
            if (cl != null) {
                cl.removeChild(info.id);
                this.mLauncher.fillEmpty(info);
            }
        }
    }

    public void removeGadgets(ArrayList<GadgetInfo> gadgets) {
        Iterator i$ = gadgets.iterator();
        while (i$.hasNext()) {
            GadgetInfo info = (GadgetInfo) i$.next();
            CellLayout cl = getCellLayoutById(info.screenId);
            if (cl != null) {
                cl.removeChild(info.id);
                if (info.spanX == 1 && info.spanY == 1) {
                    this.mLauncher.fillEmpty(info);
                }
            }
        }
    }

    public void fillEmptyCellAuto(ItemInfo deletedInfo) {
        CellLayout layout = getCellLayout(getScreenIndexById(deletedInfo.screenId));
        if (layout != null) {
            layout.fillEmptyCellAuto(deletedInfo.cellX, deletedInfo.cellY);
        }
    }

    private CellInfo findEmptyCell(CellInfo cellInfo, int spanX, int spanY) {
        CellLayout cl;
        boolean isRotatableWidgetFind = cellInfo.isWidgetFinding();
        if (isRotatableWidgetFind) {
            cl = getFirstNotEmptyScreen().getCellLayout();
            cellInfo.screenOrder = getScreenIndexById(cl.getScreenId());
        } else {
            cl = getLastNotEmptyScreen().getCellLayout();
            cellInfo.screenOrder = getScreenIndexById(cl.getScreenId());
        }
        int[] pos = cl.findLastEmptyCell(spanX, spanY);
        if (pos != null) {
            cellInfo.cellX = pos[0];
            cellInfo.cellY = pos[1];
            cellInfo.screenId = cl.getScreenId();
            return cellInfo;
        }
        int cellCountX;
        cellInfo.nextScreen();
        cellInfo.screenId = getScreenIdByIndex(cellInfo.screenOrder);
        if (isLayoutRtl()) {
            cellCountX = DeviceConfig.getCellCountX() - spanX;
        } else {
            cellCountX = 0;
        }
        cellInfo.cellX = cellCountX;
        cellInfo.cellY = 0;
        if (cellInfo.screenOrder < 30) {
            return cellInfo;
        }
        int to;
        int from = isRotatableWidgetFind ? 0 : getScreenCount() - 1;
        if (isRotatableWidgetFind) {
            to = getScreenCount() - 1;
        } else {
            to = 0;
        }
        int direction = isRotatableWidgetFind ? 1 : -1;
        for (int i = from; i >= to; i += direction) {
            cellInfo.screenOrder = i;
            cl = getCellLayout(i);
            if (cl != null) {
                pos = cl.findLastVacantArea(spanX, spanY);
                if (pos != null) {
                    cellInfo.cellX = pos[0];
                    cellInfo.cellY = pos[1];
                    cellInfo.screenId = cl.getScreenId();
                    return cellInfo;
                }
            }
        }
        return null;
    }

    public CellInfo findEmptyCell(ItemInfo item) {
        CellInfo cellInfo = new CellInfo();
        cellInfo.screenId = -1;
        cellInfo.screenOrder = -1;
        cellInfo.cellX = 0;
        cellInfo.cellY = 0;
        cellInfo.spanX = item.spanX;
        cellInfo.spanY = item.spanY;
        if (DeviceConfig.isInvalidateCellPosition(item.isWidget(), 0, 0, item.spanX, item.spanY)) {
            return null;
        }
        cellInfo = findEmptyCell(cellInfo, item.spanX, item.spanY);
        if (cellInfo == null) {
            Log.e("Launcher.Workspace", "Too many apps installed, not adding to home screen");
            return null;
        }
        if (cellInfo.screenId == -1) {
            cellInfo.screenId = insertNewScreen(Math.max(0, isInNormalEditingMode() ? cellInfo.screenOrder - 1 : cellInfo.screenOrder), false).getCellLayout().getScreenId();
        }
        return cellInfo;
    }

    public boolean isPosValidate(ItemInfo info) {
        if (info.isLandscapePos != DeviceConfig.isScreenOrientationLandscape()) {
            DeviceConfig.correntCellPositionRuntime(info, false);
        }
        if (info.container == -100) {
            CellLayout cl = getCellLayoutById(info.screenId);
            if (cl == null) {
                return false;
            }
            if (cl.isCellOccupied(info.cellX, info.cellY, info.spanX, info.spanY)) {
                return false;
            }
            return true;
        } else if (!this.mLauncher.isFolderIdValid(info.container)) {
            return false;
        } else {
            info.screenId = -1;
            return true;
        }
    }

    protected int getDefaultScreenIndex() {
        return Math.max(0, Math.min(getScreenIndexById(this.mDefaultScreenId), getScreenCount() - 1));
    }

    public boolean isDefaultScreen(long screenId) {
        return screenId == this.mDefaultScreenId;
    }

    public void setDefaultScreenId(long screenId) {
        this.mDefaultScreenId = screenId;
        Editor editor = this.mLauncher.getWorldReadableSharedPreference().edit();
        editor.putLong("pref_default_screen", this.mDefaultScreenId);
        editor.commit();
    }

    void moveToDefaultScreen(boolean animate) {
        int defaultScreenIndex = getDefaultScreenIndex();
        if (animate) {
            snapToScreen(defaultScreenIndex);
        } else {
            setCurrentScreen(defaultScreenIndex);
        }
        getScreen(defaultScreenIndex).requestFocus();
    }

    public View getScreenById(long id) {
        int index = getScreenIndexById(id);
        return (index < 0 || index >= getScreenCount()) ? null : getScreen(index);
    }

    View getDefaultScreen() {
        return getScreenById(this.mDefaultScreenId);
    }

    int getScreenIndexById(long id) {
        int index = ((Integer) this.mScreenIdMap.get(id, Integer.valueOf(-1))).intValue();
        if (index == -1) {
            return -1;
        }
        return isInNormalEditingMode() ? index + 1 : index;
    }

    long getScreenIdByIndex(int index) {
        if (index == -1) {
            return -1;
        }
        if (isInNormalEditingMode()) {
            if (index > this.mScreenIds.size() || index == 0) {
                return -1;
            }
            return ((Long) this.mScreenIds.get(index - 1)).longValue();
        } else if (index < this.mScreenIds.size()) {
            return ((Long) this.mScreenIds.get(index)).longValue();
        } else {
            return -1;
        }
    }

    public CellScreen getCellScreen(int screenIndex) {
        View v = getScreen(screenIndex);
        if (v instanceof CellScreen) {
            return (CellScreen) v;
        }
        return null;
    }

    public CellLayout getCellLayoutById(long screenId) {
        return getCellLayout(getScreenIndexById(screenId));
    }

    public CellLayout getCellLayout(int screenIndex) {
        CellScreen cs = getCellScreen(screenIndex);
        if (cs != null) {
            return cs.getCellLayout();
        }
        return null;
    }

    public CellScreen getCurrentCellScreen() {
        return (CellScreen) getCurrentScreen();
    }

    public CellLayout getCurrentCellLayout() {
        CellScreen cs = getCurrentCellScreen();
        if (cs != null) {
            return cs.getCellLayout();
        }
        return null;
    }

    public CellScreen getLastCellScreen() {
        return getCellScreen((getScreenCount() - 1) - (isInNormalEditingMode() ? 1 : 0));
    }

    public CellScreen getLastNotEmptyScreen() {
        for (int i = 1; i <= getScreenCount(); i++) {
            CellScreen cs = getCellScreen((getScreenCount() - i) - (isInNormalEditingMode() ? 1 : 0));
            CellLayout cl = cs.getCellLayout();
            if (cl != null && !cl.isEmpty()) {
                return cs;
            }
        }
        return getCellScreen(0);
    }

    public CellScreen getFirstNotEmptyScreen() {
        int i = isInNormalEditingMode() ? 1 : 0;
        while (i <= getScreenCount() - 1) {
            CellScreen cs = getCellScreen(i);
            CellLayout cl = cs.getCellLayout();
            if (cl != null && !cl.isEmpty()) {
                return cs;
            }
            i++;
        }
        return getCellScreen(0);
    }

    public void changeTargetScreenOrder(int fromIndex, int toIndex) {
        View targetScreen = getScreen(fromIndex);
        int previousCurrentScreen = this.mCurrentScreen;
        removeScreen(fromIndex);
        addView(targetScreen, toIndex);
        if (previousCurrentScreen == fromIndex) {
            setCurrentScreen(toIndex);
        }
    }

    public void clearScreens() {
        this.mScreenIds.clear();
        this.mScreenIdMap.clear();
        removeAllScreens();
    }

    public void loadScreens(boolean firstTime, boolean reLoadViews) {
        if (!firstTime) {
            Log.d("Launcher.Workspace", "Screens before reload " + this.mScreenIds);
            if (this.mCurrentScreenId == -1) {
                long currentScreenId = getCurrentScreenId();
                this.mCurrentScreenId = currentScreenId;
                if (currentScreenId == -1) {
                    this.mCurrentScreenId = this.mNewScreenId;
                    this.mNewScreenId = -1;
                }
            }
            this.mScreenIds.clear();
            this.mScreenIdMap.clear();
        }
        Cursor c = this.mResolver.query(Screens.CONTENT_URI, null, null, null, "screenOrder ASC");
        if (c != null) {
            ArrayList<Integer> typeList = null;
            try {
                long screenId;
                int idIndex = c.getColumnIndex("_id");
                int typeIndex = c.getColumnIndex("screenType");
                if (typeIndex != -1) {
                    typeList = new ArrayList();
                }
                while (c.moveToNext()) {
                    screenId = c.getLong(idIndex);
                    this.mScreenIdMap.put(screenId, Integer.valueOf(this.mScreenIds.size()));
                    this.mScreenIds.add(Long.valueOf(screenId));
                    if (typeList != null) {
                        typeList.add(Integer.valueOf(c.getInt(typeIndex)));
                    }
                }
                if (reLoadViews) {
                    int index;
                    CellScreen cs;
                    Log.d("Launcher.Workspace", "Screens loaded " + this.mScreenIds);
                    HashMap<Long, CellScreen> screens = new HashMap();
                    for (index = getScreenCount() - 1; index >= 0; index--) {
                        cs = (CellScreen) getScreen(index);
                        cs.clearAnimation();
                        cs.getCellLayout().clearAnimation();
                        screens.put(Long.valueOf(cs.getCellLayout().getScreenId()), cs);
                    }
                    removeScreensInLayout(0, getScreenCount());
                    for (index = this.mScreenIds.size() - 1; index >= 0; index--) {
                        screenId = ((Long) this.mScreenIds.get(index)).longValue();
                        cs = (CellScreen) screens.get(Long.valueOf(screenId));
                        if (cs == null) {
                            cs = generateEmptyCellScreen(screenId, typeList != null ? ((Integer) typeList.get(index)).intValue() : 0);
                        }
                        addView(cs, 0);
                    }
                    setEditModeIfNeeded();
                }
                if (firstTime) {
                    this.mDefaultScreenId = this.mLauncher.getWorldReadableSharedPreference().getLong("pref_default_screen", -1);
                    if (this.mDefaultScreenId == -1) {
                        this.mDefaultScreenId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getLong("pref_default_screen", -1);
                        setDefaultScreenId(this.mDefaultScreenId);
                    }
                    if (!this.mScreenIds.contains(Long.valueOf(this.mDefaultScreenId))) {
                        this.mDefaultScreenId = ((Long) this.mScreenIds.get(0)).longValue();
                        setDefaultScreenId(this.mDefaultScreenId);
                    }
                } else if (!firstTime && ((Integer) this.mScreenIdMap.get(this.mCurrentScreenId, Integer.valueOf(-1))).intValue() != -1) {
                    setCurrentScreenById(this.mCurrentScreenId);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
    }

    public ArrayList<Long> getScreenIds() {
        return this.mScreenIds;
    }

    void reorderScreens(boolean needLoadScreen) {
        Log.d("Launcher.Workspace", "Screens before reorder " + this.mScreenIds);
        int count = this.mScreenIds.size();
        ArrayList<String> bulkValue = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            bulkValue.add(String.valueOf(this.mScreenIds.get(i)));
        }
        ContentValues values = new ContentValues();
        values.putStringArrayList("screenOrder", bulkValue);
        if (this.mResolver.update(Screens.CONTENT_URI, values, null, null) <= 0) {
            Log.e("Launcher.Workspace", "Failed to update screens table for reorder, aborting");
            return;
        }
        loadScreens(false, needLoadScreen);
        Log.d("Launcher.Workspace", "Screens after reorder " + this.mScreenIds);
    }

    void setThumbnailView(WorkspaceThumbnailView thumbnailView) {
        this.mThumbnailView = thumbnailView;
    }

    public void showPreview(boolean show, boolean withAnim) {
        if (!this.mLauncher.isWorkspaceLocked() && show != this.mThumbnailView.isShowing()) {
            if (show) {
                this.mThumbnailView.setVisibility(0);
                this.mPreviousScreen = this.mCurrentScreen;
                this.mDragController.setDragScoller(this.mThumbnailView);
            } else {
                setVisibility(0);
                this.mDragController.setDragScoller(this);
            }
            this.mThumbnailView.show(show, withAnim);
            if (!withAnim) {
                setIndicatorBarVisibility(0);
            }
        }
    }

    public void onStop() {
    }

    public void onResume() {
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mCallbackAfterNextLayout != null) {
            post(this.mCallbackAfterNextLayout);
            this.mCallbackAfterNextLayout = null;
        }
    }

    public void onScreenOrientationChanged() {
        loadIndicatorMarginBottom();
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cs = getCellScreen(i);
            if (cs != null) {
                cs.onScreenOrientationChanged();
            }
        }
        if (isInNormalEditingMode()) {
            this.mCallbackAfterNextLayout = this.mResetEditingViewsAfterScreenOrientationChanged;
            if (this.mShowEditingIndicator && this.mScreenSeekBar != null) {
                this.mScreenSeekBar.setTranslationY(getCurrentScreenType() != 2 ? (float) this.mIndicatorOffsetBottomPortrait : 0.0f);
            }
        }
        this.mTransitionEffect.onScreenOrientationChanged(this.mContext);
    }

    public void onScreenSizeChanged() {
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cs = getCellScreen(i);
            if (cs != null) {
                cs.onScreenSizeChanged();
            }
        }
    }

    private void loadIndicatorMarginBottom() {
        this.mIndicatorMarginBottom = DeviceConfig.getWorkspaceIndicatorMarginBottom();
        this.mIndicatorShrinkBottom = (int) (((float) this.mIndicatorMarginBottom) * 0.9f);
        this.mIndicatorOffsetBottomPortrait = (-this.mIndicatorShrinkBottom) / 2;
        View indicator = getScreenIndicator();
        if (indicator != null) {
            ((MarginLayoutParams) indicator.getLayoutParams()).bottomMargin = this.mIndicatorMarginBottom;
        }
    }

    private void setClip(boolean clipAll) {
        setClipChildren(clipAll);
        setClipToPadding(clipAll);
    }

    void onEditingModeEnterStart() {
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cell = getCellScreen(i);
            if (cell != null) {
                cell.onEditingAnimationEnterStart();
            }
        }
    }

    void onEditingModeExitStart() {
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cell = getCellScreen(i);
            if (cell != null) {
                cell.onEditingAnimationExitStart();
            }
        }
    }

    void onEditingModeEnterEnd() {
        this.mEditingModeAnimating = false;
        if (this.mScreenSeekBar != null) {
            this.mScreenSeekBar.invalidate();
        }
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cell = getCellScreen(i);
            if (cell != null) {
                cell.onEditingAnimationEnterEnd();
            }
        }
    }

    void onEditingModeExitEnd() {
        this.mEditingModeAnimating = false;
        setScreenTransitionType(this.mOldTransitionType);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putString("pref_key_transformation_type", String.valueOf(this.mOldTransitionType));
        editor.commit();
        invalidate();
        for (int i = 0; i < getScreenCount(); i++) {
            CellScreen cell = getCellScreen(i);
            if (cell != null) {
                cell.onEditingAnimationExitEnd();
                cell.setVisibility(0);
            }
        }
    }

    public int getPreviousScreenTransitionType() {
        return this.mOldTransitionType;
    }

    public void setTransitionEffectEditingMode() {
        if (getScreenTransitionType() != 9) {
            this.mOldTransitionType = getScreenTransitionType();
            setScreenTransitionType(9);
            invalidate();
        }
    }

    private int getLastScreenIndexByType(int type, int fromIndex, boolean isForward) {
        int direction = isForward ? 1 : -1;
        int current = fromIndex;
        while (getScreen(current) != null && (getScreenType(current) == type || getCellScreen(current).isEditingNewScreenMode())) {
            current += direction;
        }
        return current == fromIndex ? fromIndex : current - direction;
    }

    public int getNextTypeScreenIndex() {
        int lastIdx = getLastScreenIndexByType(getCurrentScreenType(), getCurrentScreenIndex(), true);
        return lastIdx < getScreenCount() + -1 ? lastIdx + 1 : -1;
    }

    public void setScreenScrollRangeByCurrentScreenType() {
        if (getCurrentCellLayout() != null) {
            int typeStart = getLastScreenIndexByType(getCurrentScreenType(), getCurrentScreenIndex(), false);
            int typeEnd = getLastScreenIndexByType(getCurrentScreenType(), getCurrentScreenIndex(), true);
            setScreenScrollRange(typeStart, typeEnd);
            int i = getScreenCount() - 1;
            while (i >= 0) {
                int i2;
                CellScreen cellScreen = getCellScreen(i);
                if (i < typeStart || i > typeEnd) {
                    i2 = 4;
                } else {
                    i2 = 0;
                }
                cellScreen.setVisibility(i2);
                i--;
            }
        }
    }

    private void setupEditingScreen(boolean isEditing, boolean isEntering) {
        this.mEditingScreenChanging = true;
        if (isEditing) {
            addView(this.mEditingNewScreenLeft, 0);
            addView(this.mEditingNewScreenRight, getScreenCount());
            Launcher.performLayoutNow(getRootView());
            if (isEntering) {
                setCurrentScreen(getCurrentScreenIndex() + 1);
            }
            setScreenScrollRangeByCurrentScreenType();
        } else {
            if (this.mInAutoInsertOrDeleteAnimation) {
                cancelDeleteOrInsertAnim();
            }
            resetScreenScrollRange();
            removeScreen(0);
            getHandler().removeCallbacks(this.mAutoScrollBack);
            this.mShowingTransitionEffectDemo = false;
            setCurrentScreen(getCurrentScreenIndex() - 1);
            removeScreen(getScreenCount() - 1);
        }
        this.mEditingScreenChanging = false;
    }

    private void setEditModeIfNeeded() {
        if (isInNormalEditingMode()) {
            setEditMode(this.mInEditingMode, false);
        }
    }

    public boolean inEditingModeAnimating() {
        return this.mEditingModeAnimating;
    }

    public void setEditMode(int editingMode, boolean quickMode) {
        boolean isEntering;
        boolean isKeepEditing;
        boolean z;
        int i;
        boolean z2 = true;
        int i2 = 0;
        boolean alreadyInNormalEditing = isInNormalEditingMode();
        this.mInEditingMode = editingMode;
        boolean isInNormalEditing = isInNormalEditingMode();
        if (alreadyInNormalEditing || !isInNormalEditing) {
            isEntering = false;
        } else {
            isEntering = true;
        }
        if (alreadyInNormalEditing && isInNormalEditing) {
            isKeepEditing = true;
        } else {
            isKeepEditing = false;
        }
        if (quickMode || isKeepEditing) {
            z = false;
        } else {
            z = true;
        }
        this.mEditingModeAnimating = z;
        if (!quickMode) {
            setupEditingScreen(isInNormalEditing, isEntering);
            if (!isKeepEditing) {
                if (isInNormalEditing) {
                    z2 = false;
                }
                this.mSkipDrawingChild = z2;
                if (this.mShowEditingIndicator && this.mScreenSeekBar != null) {
                    switch (getCurrentCellLayout().getScreenType()) {
                        case 0:
                        case 1:
                            this.mScreenSeekBar.animate().translationY(isInNormalEditing ? (float) this.mIndicatorOffsetBottomPortrait : 0.0f).setDuration((long) this.mMediumAnimTime).setStartDelay(0).setInterpolator(Utilities.getDefaultAnimatorInterPolator()).start();
                            break;
                        case 2:
                            this.mScreenSeekBar.animate().translationY(isInNormalEditing ? (float) (this.mIndicatorMarginBottom - this.mIndicatorShrinkBottom) : (float) this.mIndicatorShrinkBottom).setDuration((long) this.mMediumAnimTime).setStartDelay(0).setInterpolator(Utilities.getDefaultAnimatorInterPolator()).start();
                            break;
                    }
                }
                if (isInNormalEditing) {
                    i2 = 4;
                }
                setIndicatorBarVisibility(i2);
                if (this.mSlideBar != null) {
                    this.mSlideBar.startAnimation(isInNormalEditing ? this.mFadeOut : this.mFadeIn);
                }
                if (this.mScreenSeekBar != null) {
                    this.mScreenSeekBar.startAnimation(isInNormalEditing ? this.mFadeOut : this.mFadeIn);
                }
                if (isEntering) {
                    setTransitionEffectEditingMode();
                    Application.getLauncherApplication(this.mContext).startShakeMonitor();
                } else {
                    Application.getLauncherApplication(this.mContext).stopShakeMonitor();
                }
            }
        } else if (editingMode == 9) {
            setScreenScrollRangeByCurrentScreenType();
        } else {
            resetScreenScrollRange();
            for (i = getScreenCount() - 1; i >= 0; i--) {
                getCellScreen(i).setVisibility(0);
            }
        }
        i = 0;
        while (i < getScreenCount()) {
            CellScreen cell = getCellScreen(i);
            if (cell != null) {
                if (quickMode) {
                    cell.onQuickEditingModeChanged(isInQuickEditingMode());
                } else {
                    this.mTransitionEffect.resetTransformation(cell, this);
                    cell.setEditMode(isInNormalEditing, !isKeepEditing ? i - getCurrentScreenIndex() : Integer.MIN_VALUE);
                }
            }
            i++;
        }
    }

    boolean isDeleteableScreen(long screenId) {
        if (this.mLauncher.isPrivacyModeEnabled() || this.mInAutoInsertOrDeleteAnimation) {
            return false;
        }
        if ((isInNormalEditingMode() ? getScreenCount() - 2 : getScreenCount()) == 1) {
            return false;
        }
        int index = getScreenIndexById(screenId);
        int type = getCellLayout(index).getScreenType();
        if (type == 2) {
            CellLayout next = getCellLayout(index + 1);
            if (next != null && next.getScreenType() == 1) {
                return false;
            }
        } else if (type == 1) {
            CellLayout prev = getCellLayout(index - 1);
            if (prev != null && prev.getScreenType() == 2) {
                return false;
            }
        }
        return true;
    }

    public void removeScreen(int screenIndex) {
        super.removeScreen(screenIndex);
        if (isInNormalEditingMode()) {
            setScreenScrollRangeByCurrentScreenType();
        }
    }

    void deleteScreen(long screenId, boolean withAnimation) {
        if (isDeleteableScreen(screenId) && !this.mInAutoInsertOrDeleteAnimation && getCellScreen(getScreenIndexById(screenId)).getCellLayout().getChildCount() == 0) {
            if (isInNormalEditingMode() && withAnimation) {
                autoDeleteOrInsertAnim((CellScreen) getCurrentScreen(), (CellScreen) getScreen(this.mCurrentScreen + 1), 1, true, this.mCurrentScreen);
            }
            this.mResolver.delete(Screens.CONTENT_URI, "_id=" + screenId, null);
            this.mLauncher.onScreenDeleted(screenId);
            if (!isInNormalEditingMode()) {
                removeScreen(isInNormalEditingMode() ? ((Integer) this.mScreenIdMap.get(screenId)).intValue() + 1 : ((Integer) this.mScreenIdMap.get(screenId)).intValue());
            }
            reorderScreens(false);
            if (this.mDefaultScreenId == screenId) {
                setDefaultScreenId(((Long) this.mScreenIds.get(0)).longValue());
                if (this.mThumbnailView.isShowing()) {
                    this.mThumbnailView.updateHomeMark(this.mDefaultScreenId);
                }
            }
            if (this.mThumbnailView.isShowing()) {
                this.mThumbnailView.updateCurrentScreen(getCurrentScreenId());
            }
        }
    }

    private CellScreen generateEmptyCellScreen(long screenId, int screenType) {
        CellScreen cs = (CellScreen) this.mInflater.inflate(R.layout.cell_screen, this, false);
        cs.setScreenType(screenType);
        cs.onScreenOrientationChanged();
        CellLayout cl = cs.getCellLayout();
        cl.setScreenId(screenId);
        cl.setContainerId(-100);
        cl.setOnLongClickListener(this.mLongClickListener);
        return cs;
    }

    CellScreen insertNewScreen(int pos, boolean withAnimation) {
        if (this.mInAutoInsertOrDeleteAnimation) {
            return null;
        }
        int i;
        ContentValues values = new ContentValues();
        int screenType = 0;
        if (pos == -1) {
            pos = Math.max(0, getCurrentScreenIndex() - (isInNormalEditingMode() ? 1 : 0));
        }
        if (DeviceConfig.isRotatable()) {
            screenType = pos == 0 ? 2 : 1;
        }
        values.put("screenOrder", Integer.valueOf(0));
        values.put("screenType", Integer.valueOf(screenType));
        Long screenId = Long.valueOf(this.mResolver.insert(Screens.CONTENT_URI, values).getLastPathSegment());
        this.mNewScreenId = screenId.longValue();
        this.mScreenIds.add(pos, screenId);
        CellScreen cs = generateEmptyCellScreen(screenId.longValue(), screenType);
        if (isInNormalEditingMode()) {
            i = pos + 1;
        } else {
            i = pos;
        }
        addView(cs, i);
        reorderScreens(false);
        if (!isInNormalEditingMode()) {
            return cs;
        }
        cs.setEditMode(isInNormalEditingMode(), Integer.MIN_VALUE);
        Launcher.performLayoutNow(this);
        updateChildStaticTransformation(cs);
        int direction = 1;
        if (this.mCurrentScreen == 1) {
            direction = -1;
        }
        CellScreen insertCs = (CellScreen) getCurrentScreen();
        CellScreen nextCs = (CellScreen) getScreen(this.mCurrentScreen + direction);
        if (withAnimation) {
            autoDeleteOrInsertAnim(insertCs, nextCs, direction, false, -1);
        }
        setScreenScrollRangeByCurrentScreenType();
        return cs;
    }

    private void autoDeleteOrInsertAnim(final CellScreen centerCs, final CellScreen nextCs, int direction, boolean deleting, int deleteIndex) {
        final float transX = ((float) ((-direction) * nextCs.getWidth())) * 0.03999999f;
        final float transY = ((float) nextCs.getHeight()) * SCREEN_TRANS_V_RATO;
        nextCs.setPivotX(direction == -1 ? (float) nextCs.getMeasuredWidth() : 0.0f);
        final float deltaTrans = ((float) (nextCs.getWidth() * direction)) * 0.92f;
        this.mTransAnimator.removeAllListeners();
        this.mTransAnimator.removeAllUpdateListeners();
        this.mScaleAnimator.removeAllListeners();
        this.mScaleAnimator.removeAllUpdateListeners();
        final CellScreen cellScreen = nextCs;
        final CellScreen cellScreen2 = centerCs;
        final boolean z = deleting;
        final int i = deleteIndex;
        this.mTransAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                cellScreen.autoScrolling = true;
                cellScreen2.autoScrolling = true;
                Workspace.this.mInAutoInsertOrDeleteAnimation = true;
            }

            public void onAnimationEnd(Animator animation) {
                cellScreen.autoScrolling = false;
                cellScreen2.autoScrolling = false;
                Workspace.this.mInAutoInsertOrDeleteAnimation = false;
                if (z) {
                    Workspace.this.removeScreen(i);
                } else {
                    cellScreen2.onEditingAnimationEnterEnd();
                }
            }
        });
        this.mTransAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                nextCs.setTranslationX(transX - (deltaTrans * ((Float) animation.getAnimatedValue()).floatValue()));
            }
        });
        this.mScaleAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                centerCs.setPivotY((float) (nextCs.getHeight() / 2));
                centerCs.setPivotX((float) (nextCs.getWidth() / 2));
                centerCs.setTranslationX(0.0f);
                centerCs.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
            }
        });
        this.mScaleAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                centerCs.setAlpha(1.0f - value);
                centerCs.setTranslationY(transY);
                centerCs.setScaleX(0.92f - (value * 0.1f));
                centerCs.setScaleY(0.92f - (value * 0.1f));
            }
        });
        if (deleting) {
            this.mTransAnimator.setFloatValues(new float[]{0.0f, 1.0f});
            this.mScaleAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        } else {
            this.mTransAnimator.setFloatValues(new float[]{1.0f, 0.0f});
            this.mScaleAnimator.setFloatValues(new float[]{1.0f, 0.0f});
        }
        this.mScaleAnimator.start();
        this.mTransAnimator.start();
    }

    public void cancelDeleteOrInsertAnim() {
        this.mScaleAnimator.cancel();
        this.mTransAnimator.cancel();
    }

    protected void updateChildStaticTransformation(View child) {
        if (!(child instanceof CellScreen) || !((CellScreen) child).autoScrolling) {
            super.updateChildStaticTransformation(child);
        }
    }

    public void onStart() {
        int tt = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this.mLauncher).getString("pref_key_transformation_type", String.valueOf(1))).intValue();
        if (isInNormalEditingMode()) {
            this.mOldTransitionType = tt;
        } else {
            setScreenTransitionType(tt);
        }
    }

    public void onAlertGadget(ItemInfo info) {
        if ((info instanceof GadgetInfo) && info.screenId == this.mCurrentScreenId) {
            this.mLauncher.updateStatusBarClock();
        }
    }

    protected View getScreenIndicator() {
        if (this.mScreenSeekBar != null) {
            return this.mScreenSeekBar;
        }
        return this.mSlideBar;
    }

    protected void setTouchState(MotionEvent ev, int touchState) {
        if (touchState != 0 && this.mLauncher.isFolderShowing()) {
            this.mLauncher.closeFolder();
        }
        super.setTouchState(ev, touchState);
    }

    public void exitPreview(long screenID) {
        setTouchState(null, 8);
        setCurrentScreenById(screenID);
        this.mLauncher.showPreview(false, true);
    }

    public AnimationListener getEnterAnimationListener() {
        return this.ENTER_PREVIEW_ANIMATION_LISTENER;
    }

    public AnimationListener getExitAnimationListener() {
        return this.EXIT_PREVIEW_ANIMATION_LISTENER;
    }

    public void onDestroy() {
    }

    public void scrollDragingLeft() {
        if (DeviceConfig.isLayoutRtl()) {
            super.scrollDragingRight();
        } else {
            super.scrollDragingLeft();
        }
    }

    public void scrollDragingRight() {
        if (DeviceConfig.isLayoutRtl()) {
            super.scrollDragingLeft();
        } else {
            super.scrollDragingRight();
        }
    }

    public void addView(View child, int index) {
        super.addView(child, index);
        if (child instanceof WallpaperColorChangedListener) {
            ((WallpaperColorChangedListener) child).onWallpaperColorChanged();
        }
    }
}
