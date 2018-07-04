package com.miui.home.launcher.upsidescene;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog.Builder;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.miui.home.R;
import com.miui.home.launcher.AnalyticalDataCollector;
import com.miui.home.launcher.DragController;
import com.miui.home.launcher.DragObject;
import com.miui.home.launcher.DragScroller;
import com.miui.home.launcher.DragSource;
import com.miui.home.launcher.DropTarget;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.LauncherAppWidgetHost;
import com.miui.home.launcher.LauncherAppWidgetProviderInfo;
import com.miui.home.launcher.LauncherApplication;
import com.miui.home.launcher.ShortcutPlaceholderProviderInfo;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.upsidescene.data.Appearance.FreeButtonAppearance;
import com.miui.home.launcher.upsidescene.data.FreeButtonInfo;
import com.miui.home.launcher.upsidescene.data.FreeStyle;
import com.miui.home.launcher.upsidescene.data.FreeStyle.MtzGadgetInfo;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializable;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializer;
import com.miui.home.launcher.upsidescene.data.Function;
import com.miui.home.launcher.upsidescene.data.Function.AppFunction;
import com.miui.home.launcher.upsidescene.data.Function.FolderFunction;
import com.miui.home.launcher.upsidescene.data.Function.MtzGadgetFunction;
import com.miui.home.launcher.upsidescene.data.Function.SystemGadgetFunction;
import com.miui.home.launcher.upsidescene.data.Function.ToggleFunction;
import com.miui.home.launcher.upsidescene.data.Function.WidgetFunction;
import com.miui.home.launcher.upsidescene.data.Sprite;
import java.util.ArrayList;
import java.util.List;

public class SceneScreen extends FrameLayout implements DragSource, DropTarget {
    private static final Matrix sTmpReverseMatrix = new Matrix();
    private LauncherAppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
    private AppsSelectView mAppsSelectView;
    private FixedScreen mBackgroundScreen;
    private boolean mCurrentGestureFinished;
    private FixedScreen mDockScreen;
    private DragController mDragController;
    private SpriteView mDraggedSprite;
    private int[] mDropLocation;
    private SpriteView mEditFocusedSprite;
    private EditModeBottomBar mEditModeBottomBar;
    private float mEditModeScaleFactor;
    private int mEditingState;
    private BroadcastReceiver mExitSceneReceiver;
    private FixedScreen mForegroundScreen;
    private FreeStyle mFreeStyle;
    private boolean mIsStarted;
    private boolean mIsTouchExecutedThisTime;
    private int mLastGadgetId;
    private float mLastMotionX;
    private float mLastMotionY;
    private Launcher mLauncher;
    private float mOldWpOffsetX;
    private float mOldWpStepX;
    private ScaleGestureDetector mScaleDetector;
    private SceneContentView mSceneContent;
    private ScrollableScreen mScrollableScreen;
    private FreeStyleSerializable mSerializer;
    private boolean mShowHideAnimating;
    private Sprite mSpriteForDrop;
    private int mTouchSlop;

    private class ScaleDetectorListener implements OnScaleGestureListener {
        private ScaleDetectorListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return !SceneScreen.this.mScrollableScreen.isBeingDragged();
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            SceneScreen.this.mScrollableScreen.finishCurrentGesture();
            SceneScreen.this.mCurrentGestureFinished = true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (SceneScreen.this.mAppsSelectView != null) {
                return false;
            }
            float scale = detector.getScaleFactor();
            if (!SceneScreen.this.mScrollableScreen.isBeingDragged() || ((float) detector.getTimeDelta()) > 200.0f || scale < 0.95f || scale > 1.0526316f) {
            }
            if (scale < 0.8f) {
                return SceneScreen.this.onPinchIn(detector);
            }
            if (scale > 1.3f) {
                return SceneScreen.this.onPinchOut(detector);
            }
            return false;
        }
    }

    public SceneScreen(Context context) {
        this(context, null);
    }

    public SceneScreen(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SceneScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mEditingState = 1;
        this.mDropLocation = new int[2];
        this.mExitSceneReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                SceneScreen.this.mLauncher.hideSceneScreen(true);
            }
        };
        this.mSerializer = new FreeStyleSerializer(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSceneContent = (SceneContentView) findViewById(R.id.sceneContent);
        this.mEditModeBottomBar = (EditModeBottomBar) findViewById(R.id.editModeBottomBar);
        this.mForegroundScreen = (FixedScreen) findViewById(R.id.foregroundScreen);
        this.mBackgroundScreen = (FixedScreen) findViewById(R.id.backgroundScreen);
        this.mScrollableScreen = (ScrollableScreen) findViewById(R.id.scrollableScreen);
        this.mDockScreen = (FixedScreen) findViewById(R.id.dockScreen);
        this.mScaleDetector = new ScaleGestureDetector(this.mContext, new ScaleDetectorListener());
        this.mTouchSlop = ViewConfiguration.get(this.mContext).getScaledTouchSlop();
        setClickable(true);
        setLongClickable(true);
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mAppWidgetHost = launcher.getAppWidgetHost();
        this.mAppWidgetManager = AppWidgetManager.getInstance(this.mContext);
    }

    public Launcher getLauncher() {
        return this.mLauncher;
    }

    public AppWidgetHost getAppWidgetHost() {
        return this.mAppWidgetHost;
    }

    public void setFreeStyle(FreeStyle freeStyle) {
        boolean firstTime = this.mFreeStyle == null;
        this.mFreeStyle = freeStyle;
        if (firstTime) {
            this.mSceneContent.setSceneScreen(this);
            this.mScrollableScreen.setSceneScreen(this);
            this.mBackgroundScreen.setSceneScreen(this);
            this.mForegroundScreen.setSceneScreen(this);
            this.mDockScreen.setSceneScreen(this);
        }
        this.mEditModeBottomBar.setSceneScreen(this);
        this.mForegroundScreen.setScreenData(freeStyle.getForegroundScreen());
        this.mBackgroundScreen.setScreenData(freeStyle.getBackgroundScreen());
        this.mScrollableScreen.setScreenData(freeStyle.getDriftScreen());
        this.mDockScreen.setScreenData(freeStyle.getDockScreen());
    }

    public FreeStyle getFreeStyle() {
        return this.mFreeStyle;
    }

    public DragScroller getDragScroller() {
        return this.mScrollableScreen;
    }

    public void refreshChildrenFolder() {
        FreeLayout freeLayout = this.mScrollableScreen.getFreeLayout();
        for (int i = freeLayout.getChildCount() - 1; i >= 0; i--) {
            View child = freeLayout.getChildAt(i);
            if (child.getVisibility() == 0 && (child instanceof SpriteView)) {
                SpriteView spriteView = (SpriteView) child;
                if (spriteView.getSpriteData().getFunction().getType() == 9) {
                    spriteView.rebuildContentView();
                }
            }
        }
    }

    public void notifyScrollableScreenScrolling() {
        float rate = ((float) this.mScrollableScreen.getScrollX()) / ((float) (this.mScrollableScreen.getChildWidth() - this.mScrollableScreen.getWidth()));
        this.mBackgroundScreen.setScrollX((int) (((float) (this.mBackgroundScreen.getChildWidth() - this.mBackgroundScreen.getWidth())) * rate));
        this.mForegroundScreen.setScrollX((int) (((float) (this.mForegroundScreen.getChildWidth() - this.mForegroundScreen.getWidth())) * rate));
        float xStep = ((float) this.mScrollableScreen.getCurrentScreen()) / ((float) this.mScrollableScreen.getScreenCount());
        if (!this.mShowHideAnimating) {
            this.mLauncher.updateWallpaperOffset(xStep, 0.0f, rate, 0.0f);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            this.mCurrentGestureFinished = false;
            this.mLastMotionX = ev.getX();
            this.mLastMotionY = ev.getY();
        }
        if (!this.mScrollableScreen.isBeingDragged()) {
            this.mScaleDetector.onTouchEvent(ev);
        }
        if (ev.getPointerCount() > 1) {
            cancelLongPress();
        } else {
            int yDiff = (int) Math.abs(ev.getY() - this.mLastMotionY);
            if (((int) Math.abs(ev.getX() - this.mLastMotionX)) > this.mTouchSlop || yDiff > this.mTouchSlop) {
                cancelLongPress();
            }
        }
        this.mIsTouchExecutedThisTime = false;
        super.dispatchTouchEvent(ev);
        if (ev.getAction() == 0 || ev.getAction() == 1) {
            onTouchEvent(ev);
        }
        return true;
    }

    public void cancelLongPress() {
        super.cancelLongPress();
        if (isPressed()) {
            setPressed(false);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mIsTouchExecutedThisTime) {
            return true;
        }
        if ((isInEditMode() && event.getAction() == 0 && event.getY() > ((float) getHeight()) * this.mEditModeScaleFactor) || isSelectViewShowing()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            cancelLongPress();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean performClick() {
        if (!isInEditMode() || getEditFocusedSprite() == null) {
            return super.performClick();
        }
        setEditFocusedSprite(getTouchedSpriteView(this.mLastMotionX, this.mLastMotionY, false));
        return true;
    }

    private SpriteView getTouchedSpriteView(float x, float y, boolean isLongClick) {
        SpriteView spriteView = getTouchedSpriteViewCore(this.mDockScreen.getFreeLayout(), x, y, isLongClick);
        if (spriteView == null) {
            return getTouchedSpriteViewCore(this.mScrollableScreen.getFreeLayout(), x, y, isLongClick);
        }
        return spriteView;
    }

    private SpriteView getTouchedSpriteViewCore(FreeLayout freeLayout, float x, float y, boolean isLongClick) {
        List<ViewGroup> viewChain = new ArrayList();
        ViewGroup view = freeLayout;
        do {
            viewChain.add(0, view);
            view = (ViewGroup) view.getParent();
        } while (view != this);
        float[] point = new float[]{x, y};
        for (ViewGroup viewGroup : viewChain) {
            if (!checkIsTransformedTouchPointInView((ViewGroup) viewGroup.getParent(), viewGroup, point)) {
                return null;
            }
        }
        float[] childLocal = new float[2];
        for (int i = freeLayout.getChildCount() - 1; i >= 0; i--) {
            View child = freeLayout.getChildAt(i);
            if (child.getVisibility() == 0 && (child instanceof SpriteView)) {
                SpriteView spriteView = (SpriteView) child;
                if (isLongClick) {
                    if (!spriteView.isMovable()) {
                        continue;
                    }
                } else if (!spriteView.isEditable()) {
                }
                childLocal[0] = point[0];
                childLocal[1] = point[1];
                if (!checkIsTransformedTouchPointInView(freeLayout, child, childLocal)) {
                    continue;
                } else if (!isTransparent(child, (int) childLocal[0], (int) childLocal[1])) {
                    return spriteView;
                }
            }
        }
        return null;
    }

    private boolean checkIsTransformedTouchPointInView(ViewGroup parent, View child, float[] inOutLocalPoint) {
        inOutLocalPoint[0] = inOutLocalPoint[0] + ((float) (parent.getScrollX() - child.getLeft()));
        inOutLocalPoint[1] = inOutLocalPoint[1] + ((float) (parent.getScrollY() - child.getTop()));
        Matrix matrix = child.getMatrix();
        if (!matrix.isIdentity()) {
            matrix.invert(sTmpReverseMatrix);
            sTmpReverseMatrix.mapPoints(inOutLocalPoint);
        }
        if (inOutLocalPoint[0] < 0.0f || inOutLocalPoint[0] >= ((float) (child.getRight() - child.getLeft())) || inOutLocalPoint[1] < 0.0f || inOutLocalPoint[1] >= ((float) (child.getBottom() - child.getTop()))) {
            return false;
        }
        return true;
    }

    private boolean isTransparent(View view, int viewLocalX, int viewLocalY) {
        try {
            Bitmap bitmap = DragController.createViewBitmap(view, 1.0f);
            if (bitmap == null || bitmap.getConfig() != Config.ARGB_8888) {
                return false;
            }
            int bufferWidth = bitmap.getWidth() * 4;
            int xMin = Math.max(0, viewLocalX - 10);
            int yMin = Math.max(0, viewLocalY - 10);
            int xMax = Math.min(bitmap.getWidth() - 1, viewLocalX + 10);
            int checkWidth = (xMax - xMin) + 1;
            int lineStartIndex = (yMin * bufferWidth) + (xMin * 4);
            int endLineStartIndex = lineStartIndex + ((((Math.min(bitmap.getHeight() - 1, viewLocalY + 10) - yMin) + 1) - 1) * bufferWidth);
            while (lineStartIndex <= endLineStartIndex) {
                int index = lineStartIndex + 3;
                int lineEndIndex = index + ((checkWidth - 1) * 4);
                while (index <= lineEndIndex) {
                    if ((bitmap.mBuffer[index] & 255) >= 25) {
                        return false;
                    }
                    index += 4;
                }
                lineStartIndex += bufferWidth;
            }
            view.destroyDrawingCache();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean performLongClick() {
        if (!isSelectViewShowing()) {
            if (!(isInMoveMode() || isInEditMode())) {
                gotoMoveMode();
            }
            SpriteView touchedSprite = getTouchedSpriteView(this.mLastMotionX, this.mLastMotionY, true);
            if (touchedSprite != null) {
                dragSpriteView(touchedSprite);
            }
        }
        return true;
    }

    private void dragSpriteView(SpriteView spriteView) {
        this.mDraggedSprite = spriteView;
        this.mDraggedSprite.setState(6);
        setAllSpriteState(7, this.mDraggedSprite);
        getLauncher().getDragController().startDrag(new View[]{this.mDraggedSprite}, false, 1.0f, (DragSource) this, 1, 1, false);
        if (isInEditMode()) {
            setEditFocusedSprite(null);
        }
    }

    public void onExternalDragStart() {
        setAllSpriteState(7, null);
    }

    public void onExternalDragEnd() {
        setAllSpriteState(2, null);
    }

    private void setAllSpriteState(int spriteState, SpriteView exceptSprite) {
        setAllSpriteState(this.mScrollableScreen.getFreeLayout(), spriteState, exceptSprite);
        setAllSpriteState(this.mDockScreen.getFreeLayout(), spriteState, exceptSprite);
    }

    private void setAllSpriteState(FreeLayout freeLayout, int spriteState, SpriteView exceptSprite) {
        int count = freeLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            SpriteView spriteView = (SpriteView) freeLayout.getChildAt(i);
            if (spriteView != exceptSprite) {
                spriteView.setState(spriteState);
            }
        }
    }

    public float getEditModeScaleFactor() {
        return this.mEditModeScaleFactor;
    }

    public void gotoEditMode() {
        if (!this.mLauncher.isPrivacyModeEnabled() && !Launcher.isChildrenModeEnabled() && !isInEditMode()) {
            if (isInMoveMode()) {
                exitMoveMode();
            }
            this.mEditingState = 2;
            this.mEditModeBottomBar.setVisibility(0);
            this.mScrollableScreen.getFreeLayout().gotoEditMode();
            this.mDockScreen.getFreeLayout().gotoEditMode();
            setEditFocusedSprite(null);
            post(new Runnable() {
                public void run() {
                    SceneScreen.this.gotoEditModeAnimation();
                }
            });
            AnalyticalDataCollector.enterFreeStyleEditMode(this.mContext);
        }
    }

    private void gotoEditModeAnimation() {
        if (this.mEditModeScaleFactor == 0.0f) {
            this.mEditModeScaleFactor = (1.0f + ((float) (getHeight() - this.mEditModeBottomBar.calcHeight()))) / ((float) getHeight());
        }
        this.mSceneContent.setPivotX(0.0f);
        this.mSceneContent.setPivotY(0.0f);
        Animator scaleXAnimator = ObjectAnimator.ofFloat(this.mSceneContent, "scaleX", new float[]{this.mEditModeScaleFactor});
        scaleXAnimator.setInterpolator(new LinearInterpolator());
        scaleXAnimator.start();
        Animator scaleYAnimator = ObjectAnimator.ofFloat(this.mSceneContent, "scaleY", new float[]{this.mEditModeScaleFactor});
        scaleYAnimator.setInterpolator(new LinearInterpolator());
        scaleYAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                SceneScreen.this.animatedRefresh();
            }
        });
        scaleYAnimator.start();
        int newWidth = (int) (((float) this.mSceneContent.getWidth()) / this.mEditModeScaleFactor);
        this.mSceneContent.widthTo(newWidth);
        startScrollAnimation(newWidth);
        int leftEdge = (-(newWidth - this.mSceneContent.getWidth())) / 2;
        Animator dockMoveAnimator = ObjectAnimator.ofInt(this.mDockScreen, "scrollX", new int[]{leftEdge});
        dockMoveAnimator.setInterpolator(new LinearInterpolator());
        dockMoveAnimator.start();
    }

    public void gotoMoveMode() {
        if (!this.mLauncher.isPrivacyModeEnabled()) {
            this.mEditingState = 3;
            this.mScrollableScreen.getFreeLayout().gotoMoveMode();
            this.mDockScreen.getFreeLayout().gotoMoveMode();
            setAllSpriteState(5, null);
            AnalyticalDataCollector.enterFreeStyleMoveMode(this.mContext);
        }
    }

    public void exitMoveMode() {
        this.mEditingState = 1;
        save();
        this.mScrollableScreen.getFreeLayout().exitMoveMode();
        this.mDockScreen.getFreeLayout().exitMoveMode();
        setAllSpriteState(1, null);
    }

    public boolean isInMoveMode() {
        return this.mEditingState == 3;
    }

    private void startScrollAnimation(int newWidth) {
        if (this.mScrollableScreen.getCurrentScreen() > 0) {
            int scrollOffset = newWidth - this.mSceneContent.getWidth();
            if (this.mScrollableScreen.getCurrentScreen() != this.mScrollableScreen.getScreenCount() - 1) {
                scrollOffset /= 2;
            }
            Animator scrollerAnimator = ObjectAnimator.ofInt(this.mScrollableScreen, "scrollX", new int[]{this.mScrollableScreen.getScrollX() - scrollOffset});
            scrollerAnimator.setInterpolator(new LinearInterpolator());
            scrollerAnimator.start();
        }
    }

    private void animatedRefresh() {
        post(new Runnable() {
            public void run() {
                SceneScreen.this.mScrollableScreen.setCurrentScreen(SceneScreen.this.mScrollableScreen.getCurrentScreen());
                SceneScreen.this.requestLayout();
            }
        });
    }

    public void exitEditMode() {
        exitEditModeInner(true);
    }

    private void exitEditModeInner(boolean animate) {
        this.mEditingState = 1;
        save();
        if (animate) {
            Animator scaleXAnimator = ObjectAnimator.ofFloat(this.mSceneContent, "scaleX", new float[]{1.0f});
            scaleXAnimator.setInterpolator(new LinearInterpolator());
            scaleXAnimator.start();
            Animator scaleYAnimator = ObjectAnimator.ofFloat(this.mSceneContent, "scaleY", new float[]{1.0f});
            scaleYAnimator.setInterpolator(new LinearInterpolator());
            scaleYAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    SceneScreen.this.mEditModeBottomBar.setVisibility(8);
                    SceneScreen.this.animatedRefresh();
                }
            });
            scaleYAnimator.start();
            this.mSceneContent.widthTo(getWidth());
            startScrollAnimation(getWidth());
            Animator dockMoveAnimator = ObjectAnimator.ofInt(this.mDockScreen, "scrollX", new int[]{0});
            dockMoveAnimator.setInterpolator(new LinearInterpolator());
            dockMoveAnimator.start();
        } else {
            this.mSceneContent.setScaleX(1.0f);
            this.mSceneContent.setScaleY(1.0f);
            this.mEditModeBottomBar.setVisibility(8);
            animatedRefresh();
            this.mSceneContent.setOverWidth(getWidth());
            this.mDockScreen.setScrollX(0);
        }
        this.mScrollableScreen.getFreeLayout().exitEditMode();
        this.mDockScreen.getFreeLayout().exitEditMode();
    }

    public boolean isInEditMode() {
        return this.mEditingState == 2;
    }

    boolean onPinchOut(ScaleGestureDetector detector) {
        if (!isInEditMode()) {
            return false;
        }
        exitEditMode();
        return true;
    }

    boolean onPinchIn(ScaleGestureDetector detector) {
        if (isInEditMode()) {
            return false;
        }
        gotoEditMode();
        return true;
    }

    public void setEditFocusedSprite(SpriteView sprite) {
        this.mEditFocusedSprite = sprite;
        if (this.mEditFocusedSprite != null) {
            this.mEditFocusedSprite.setState(3);
            setAllSpriteState(4, this.mEditFocusedSprite);
            if (this.mEditFocusedSprite.getSpriteData().getFunction().getType() == 2 && this.mEditFocusedSprite.getSpriteData().getAppearance().getType() == 0) {
                showSelectApps(true);
            }
        } else if (!isDragging()) {
            setAllSpriteState(2, null);
        }
        this.mEditModeBottomBar.switchEditWidgetBar();
    }

    public void showSelectApps(boolean multiSelect) {
        Sprite sprite = this.mSpriteForDrop != null ? this.mSpriteForDrop : this.mEditFocusedSprite.getSpriteData();
        if (this.mAppsSelectView == null) {
            List<ComponentName> components = new ArrayList();
            if (multiSelect) {
                if (sprite.getFunction().getType() == 2) {
                    components = ((FolderFunction) sprite.getFunction()).getComponentNames(this.mContext);
                }
            } else if (sprite.getFunction().getType() == 1) {
                AppFunction appFunc = (AppFunction) sprite.getFunction();
                if (appFunc != null) {
                    components.add(appFunc.getComponentName());
                }
            }
            this.mAppsSelectView = new AppsSelectView(getContext(), this, (ComponentName[]) components.toArray(new ComponentName[0]), multiSelect);
            addView(this.mAppsSelectView);
        }
    }

    public void closeSelectApps(boolean isCancel) {
        if (this.mAppsSelectView != null) {
            removeView(this.mAppsSelectView);
            this.mAppsSelectView = null;
        }
        finishDropAddSpriteView(isCancel);
    }

    public int generateSystemGadgetId() {
        if (this.mLastGadgetId == 0) {
            this.mLastGadgetId = PreferenceManager.getDefaultSharedPreferences(this.mContext).getInt("pref_free_style_last_gadget_id", 100000000);
        }
        this.mLastGadgetId++;
        PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putInt("pref_free_style_last_gadget_id", this.mLastGadgetId).commit();
        return this.mLastGadgetId;
    }

    void addGadget(GadgetInfo info) {
        info.id = (long) generateSystemGadgetId();
        ((SystemGadgetFunction) this.mSpriteForDrop.getFunction()).setGadgetInfo(info.getGadgetId(), (int) info.id);
        finishDropAddSpriteView(false);
    }

    void addAppWidget(AppWidgetProviderInfo widgetInfo) {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        try {
            this.mAppWidgetManager.bindAppWidgetId(appWidgetId, widgetInfo.provider);
            if (widgetInfo.configure != null) {
                Intent intent = new Intent("android.appwidget.action.APPWIDGET_CONFIGURE");
                intent.setComponent(widgetInfo.configure);
                intent.putExtra("appWidgetId", appWidgetId);
                LauncherApplication.startActivityForResult(getContext(), intent, 11);
                return;
            }
            String packageName = widgetInfo.provider.getPackageName();
            String className = widgetInfo.provider.getClassName();
            Intent data = new Intent("android.intent.action.MAIN", null);
            data.addCategory("android.intent.category.DEFAULT");
            data.putExtra("appWidgetId", appWidgetId);
            if (packageName == null || className == null) {
                data.setAction("android.intent.action.CREATE_SHORTCUT");
                data.putExtra("android.intent.extra.shortcut.NAME", widgetInfo.label);
            } else {
                data.setClassName(packageName, className);
            }
            completeAddAppWidget(data);
            finishDropAddSpriteView(false);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void completeAddAppWidget(Intent data) {
        ((WidgetFunction) this.mSpriteForDrop.getFunction()).setId(data.getExtras().getInt("appWidgetId", -1));
        finishDropAddSpriteView(false);
    }

    public void completeGadgetConfig(Intent data) {
        Bundle extras = data.getExtras();
        if (!(extras == null || this.mEditFocusedSprite == null)) {
            this.mEditFocusedSprite.updateGadgetConfig(extras);
        }
        finishDropAddSpriteView(false);
    }

    public void onSelectApps(List<ComponentName> names, boolean multiSelect) {
        Sprite sprite;
        if (this.mSpriteForDrop != null) {
            sprite = this.mSpriteForDrop;
        } else if (this.mEditFocusedSprite != null) {
            sprite = this.mEditFocusedSprite.getSpriteData();
        } else {
            return;
        }
        if (multiSelect) {
            FolderFunction folderFunc;
            if (sprite.getFunction().getType() == 2) {
                folderFunc = (FolderFunction) sprite.getFunction();
            } else {
                folderFunc = (FolderFunction) Function.createFunction(2);
                sprite.setFunction(folderFunc);
            }
            folderFunc.setComponentNames(names);
        } else {
            AppFunction appFunc;
            if (sprite.getFunction().getType() == 1) {
                appFunc = (AppFunction) sprite.getFunction();
            } else {
                appFunc = (AppFunction) Function.createFunction(1);
                sprite.setFunction(appFunc);
            }
            appFunc.setComponentName((ComponentName) names.get(0));
        }
        if (sprite != this.mSpriteForDrop) {
            this.mEditFocusedSprite.rebuildContentView();
        }
        if (isInEditMode()) {
            this.mEditModeBottomBar.refreshFreeButtonCheckbox();
        }
    }

    public void completeSelectToggle(int id) {
        Sprite sprite;
        ToggleFunction toggleFunc;
        if (this.mSpriteForDrop != null) {
            sprite = this.mSpriteForDrop;
        } else if (this.mEditFocusedSprite != null) {
            sprite = this.mEditFocusedSprite.getSpriteData();
        } else {
            return;
        }
        if (sprite.getFunction().getType() == 7) {
            toggleFunc = (ToggleFunction) sprite.getFunction();
        } else {
            toggleFunc = (ToggleFunction) Function.createFunction(7);
            sprite.setFunction(toggleFunc);
        }
        toggleFunc.setToggleId(id);
        if (sprite != this.mSpriteForDrop) {
            this.mEditFocusedSprite.rebuildContentView();
        }
        if (isInEditMode()) {
            this.mEditModeBottomBar.refreshFreeButtonCheckbox();
        }
    }

    public void removeDraggedSprite() {
        if (this.mDraggedSprite != null) {
            this.mDraggedSprite.getParentLayout().removeSprite(this.mDraggedSprite);
            if (isInEditMode()) {
                setEditFocusedSprite(null);
            }
        }
    }

    public SpriteView getEditFocusedSprite() {
        return this.mEditFocusedSprite;
    }

    public boolean isCurrentGestureFinished() {
        return this.mCurrentGestureFinished;
    }

    public boolean isShowing() {
        return getVisibility() == 0 && getWindowToken() != null;
    }

    public void onShowAnimationStart() {
        this.mShowHideAnimating = true;
        setVisibility(0);
        onStart();
        this.mOldWpStepX = this.mLauncher.getDragLayer().getWpStepX();
        this.mOldWpOffsetX = this.mLauncher.getDragLayer().getWpOffsetX();
        post(new Runnable() {
            public void run() {
                float xStep = ((float) SceneScreen.this.mScrollableScreen.getCurrentScreen()) / ((float) SceneScreen.this.mScrollableScreen.getScreenCount());
                SceneScreen.this.mLauncher.updateWallpaperOffsetAnimate(xStep, 0.0f, ((float) SceneScreen.this.mScrollableScreen.getScrollX()) / ((float) (SceneScreen.this.mScrollableScreen.getChildWidth() - SceneScreen.this.mFreeStyle.getWidth())), 0.0f);
            }
        });
    }

    public void onShowAnimationEnd() {
        this.mShowHideAnimating = false;
    }

    public void onHideAnimationStart() {
        this.mShowHideAnimating = true;
        this.mLauncher.updateWallpaperOffsetAnimate(this.mOldWpStepX, 0.0f, this.mOldWpOffsetX, 0.0f);
    }

    public void onHideAnimationEnd() {
        onStop();
        this.mShowHideAnimating = false;
    }

    public boolean isSelectViewShowing() {
        if (this.mAppsSelectView == null || this.mAppsSelectView.getWindowToken() == null || this.mAppsSelectView.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    public boolean onBackPressed() {
        return exitEditableMode(false, false);
    }

    public boolean exitEditableMode(boolean isHome, boolean isEnterPrivacy) {
        if (isSelectViewShowing()) {
            closeSelectApps(true);
            if (!isEnterPrivacy) {
                return true;
            }
        }
        if (this.mEditFocusedSprite != null) {
            setEditFocusedSprite(null);
            if (!(isHome || isEnterPrivacy)) {
                return true;
            }
        }
        if (isInEditMode()) {
            exitEditMode();
            if (!isEnterPrivacy) {
                return true;
            }
        }
        if (isInMoveMode()) {
            exitMoveMode();
            if (!isEnterPrivacy) {
                return true;
            }
        }
        return false;
    }

    public void onNewIntent(Intent intent) {
        if (!exitEditableMode(true, false)) {
            this.mScrollableScreen.snapToScreen(this.mFreeStyle.getDriftScreen().getHome(), 0);
        }
    }

    public void onStop() {
        if (this.mIsStarted) {
            this.mContext.unregisterReceiver(this.mExitSceneReceiver);
            this.mIsStarted = false;
        }
    }

    public void onStart() {
        if (!this.mIsStarted) {
            this.mContext.registerReceiver(this.mExitSceneReceiver, new IntentFilter("com.miui.home.launcher.upsidescene.SceneScreen.EXIT"));
            this.mIsStarted = true;
        }
        if (Launcher.isChildrenModeEnabled()) {
            refreshChildrenFolder();
        }
    }

    public void notifyGadgets(int state) {
        this.mScrollableScreen.notifyGadgets(state);
        this.mDockScreen.notifyGadgets(state);
        if (state == 4) {
            finishDropAddSpriteView(true);
        }
    }

    private void reload() {
        setFreeStyle(this.mFreeStyle);
    }

    public void reset() {
        if (!this.mLauncher.isPrivacyModeEnabled() && this.mFreeStyle != null) {
            Builder builder = new Builder(this.mContext);
            builder.setIconAttribute(16843605);
            builder.setTitle(17039380);
            builder.setMessage(this.mContext.getString(R.string.reset_scene_prompt));
            builder.setCancelable(true);
            builder.setNegativeButton(this.mContext.getString(R.string.cancel_action), null);
            builder.setPositiveButton(this.mContext.getString(R.string.confirm_btn_label), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SceneScreen.this.exitEditModeInner(false);
                    SceneScreen.this.post(new Runnable() {
                        public void run() {
                            SceneScreen.this.removeAllSprites(true);
                            SceneScreen.this.mSerializer.clear(true);
                            SceneScreen.this.mFreeStyle = SceneScreen.this.mSerializer.load();
                            SceneScreen.this.reload();
                        }
                    });
                }
            });
            builder.create().show();
        }
    }

    private void removeAllSprites(boolean alsoClearUserData) {
        removeAllSprites(this.mDockScreen.getFreeLayout(), alsoClearUserData);
        removeAllSprites(this.mScrollableScreen.getFreeLayout(), alsoClearUserData);
    }

    private void removeAllSprites(FreeLayout freeLayout, boolean alsoClearUserData) {
        for (int i = freeLayout.getChildCount() - 1; i >= 0; i--) {
            SpriteView spriteView = (SpriteView) freeLayout.getChildAt(i);
            if (!spriteView.getSpriteData().isUserCreated() || alsoClearUserData) {
                freeLayout.removeSprite(spriteView);
            }
        }
    }

    public boolean isDragging() {
        return this.mDraggedSprite != null;
    }

    public void setDragController(DragController dragger) {
        this.mDragController = dragger;
    }

    public View getHitView() {
        return this;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        if (this.mDraggedSprite != null) {
            this.mDraggedSprite.setVisibility(0);
        }
        if (isInMoveMode()) {
            setAllSpriteState(5, null);
        } else {
            setAllSpriteState(2, null);
        }
        this.mDraggedSprite = null;
        this.mEditModeBottomBar.switchEditWidgetBar();
    }

    public boolean isDropEnabled() {
        return true;
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        if (dragObject.dragSource == this) {
            return onDropInternal(dragObject);
        }
        return onDropExternal(dragObject);
    }

    private boolean onDropInternal(DragObject dragObject) {
        getLocation(dragObject, this.mDropLocation);
        this.mDraggedSprite.moveTo(this.mDropLocation[0], this.mDropLocation[1]);
        this.mDraggedSprite.getParentLayout().bringToTop(this.mDraggedSprite);
        return true;
    }

    private boolean onDropExternal(DragObject dragObject) {
        if (((float) dragObject.y) > ((float) getHeight()) * this.mEditModeScaleFactor) {
            return false;
        }
        getLocation(dragObject, this.mDropLocation);
        this.mSpriteForDrop = this.mFreeStyle.createSpriteByUser();
        switch (dragObject.getDragInfo().itemType) {
            case 5:
                this.mSpriteForDrop.setFunction(Function.createFunction(4));
                addGadget((GadgetInfo) dragObject.getDragInfo());
                break;
            case 6:
                this.mSpriteForDrop.setFunction(Function.createFunction(5));
                addAppWidget(((LauncherAppWidgetProviderInfo) dragObject.getDragInfo()).providerInfo);
                break;
            case 8:
                switch (((ShortcutPlaceholderProviderInfo) dragObject.getDragInfo()).addType) {
                    case 1:
                        this.mSpriteForDrop.setFunction(Function.createFunction(1));
                        showSelectApps(false);
                        break;
                    case 2:
                        this.mSpriteForDrop.setFunction(Function.createFunction(2));
                        showSelectApps(true);
                        break;
                    case 3:
                        this.mSpriteForDrop.setFunction(Function.createFunction(3));
                        finishDropAddSpriteView(false);
                        break;
                    case 4:
                        this.mSpriteForDrop.setFunction(Function.createFunction(7));
                        this.mLauncher.showTogglesSelectView();
                        break;
                    default:
                        break;
                }
            case 9:
                MtzGadgetFunction mtzGadgetFunc = (MtzGadgetFunction) Function.createFunction(6);
                mtzGadgetFunc.setMtzRelativePath(((MtzGadgetInfo) dragObject.getDragInfo()).path);
                this.mSpriteForDrop.setFunction(mtzGadgetFunc);
                finishDropAddSpriteView(false);
                break;
            case 10:
                this.mSpriteForDrop.setAppearance(new FreeButtonAppearance(((FreeButtonInfo) dragObject.getDragInfo()).getPackageName(), this.mFreeStyle));
                finishDropAddSpriteView(false);
                break;
        }
        return true;
    }

    public void finishDropAddSpriteView(boolean cancel) {
        if (!(cancel || this.mSpriteForDrop == null)) {
            SpriteView spriteView = this.mScrollableScreen.getFreeLayout().addSprite(this.mSpriteForDrop);
            spriteView.moveTo(this.mDropLocation[0], this.mDropLocation[1]);
            spriteView.gotoEditMode();
        }
        this.mSpriteForDrop = null;
    }

    private int[] getLocation(DragObject dragObject, int[] location) {
        int scrollX;
        if (location == null) {
            location = new int[2];
        }
        float factor = isInEditMode() ? this.mEditModeScaleFactor : 1.0f;
        if (this.mDraggedSprite != null) {
            scrollX = ((ViewGroup) this.mDraggedSprite.getParent().getParent()).getScrollX();
        } else {
            scrollX = this.mScrollableScreen.getScrollX();
        }
        this.mLauncher.getDragLayer().getLocationInDragLayer(dragObject.getDragView(), location, false);
        location[0] = ((int) (((float) location[0]) / factor)) + scrollX;
        location[1] = (int) (((float) location[1]) / factor);
        return location;
    }

    public void onDragEnter(DragObject dragObject) {
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        if (dragObject.getDragInfo() == null || dragObject.getDragInfo().itemType != 12) {
            return true;
        }
        return false;
    }

    public void save() {
        this.mSerializer.save(this.mFreeStyle);
    }

    public void reinit() {
        notifyGadgets(15);
    }

    public void cleanUp() {
        notifyGadgets(16);
    }

    public void onDropCompleted() {
        if (Launcher.isChildrenModeEnabled()) {
            onBackPressed();
        }
    }

    public void onDropBack(DragObject d) {
    }
}
