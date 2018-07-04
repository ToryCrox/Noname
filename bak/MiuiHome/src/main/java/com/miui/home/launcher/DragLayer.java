package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.miui.home.R;
import com.miui.home.launcher.common.ParasiticDrawingObject;
import com.miui.home.launcher.common.Utilities;
import java.util.ArrayList;
import java.util.Iterator;

public class DragLayer extends FrameLayout {
    private Runnable OffsetUpdater = new Runnable() {
        public void run() {
            DragLayer.this.updateWallpaperOffset();
        }
    };
    private Rect mClipForDragging;
    private AppWidgetResizeFrame mCurrentResizeFrame;
    private DragController mDragController;
    private Launcher mLauncher;
    private int mOldOffsetX = 0;
    private float[] mOldPositions;
    ArrayList<ParasiticDrawingObject> mPdoList = new ArrayList();
    private final ArrayList<AppWidgetResizeFrame> mResizeFrames = new ArrayList();
    private int mScaledUpsideScreenOutTouch = 40;
    private Point mScreenSize;
    private WallpaperManager mWallpaperManager;
    private int mWpHeight = 0;
    private float mWpOffsetX = 0.0f;
    private float mWpOffsetY = 0.0f;
    private boolean mWpScrolling = true;
    private float mWpStepX = 0.0f;
    private float mWpStepY = 0.0f;
    private int mWpWidth = 0;
    private int mXDown;
    private int mYDown;

    public static class LayoutParams extends android.widget.FrameLayout.LayoutParams {
        public boolean customPosition = false;
        public int x;
        public int y;

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mWallpaperManager = WallpaperManager.getInstance(context);
        this.mScreenSize = new Point();
        this.mScaledUpsideScreenOutTouch = (int) (getResources().getDisplayMetrics().density * ((float) this.mScaledUpsideScreenOutTouch));
    }

    public void setDragController(DragController controller) {
        this.mDragController = controller;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.mDragController.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    private boolean handleTouchDown(MotionEvent ev, boolean intercept) {
        Rect hitRect = new Rect();
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        Iterator i$ = this.mResizeFrames.iterator();
        while (i$.hasNext()) {
            AppWidgetResizeFrame child = (AppWidgetResizeFrame) i$.next();
            child.getHitRect(hitRect);
            if (hitRect.contains(x, y) && child.beginResizeIfPointInRegion(x - child.getLeft(), y - child.getTop())) {
                this.mCurrentResizeFrame = child;
                this.mXDown = x;
                this.mYDown = y;
                requestDisallowInterceptTouchEvent(true);
                return true;
            }
        }
        return false;
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0 && handleTouchDown(ev, true)) {
            return true;
        }
        if (ev.getPointerCount() == 2) {
            if (this.mOldPositions == null) {
                this.mOldPositions = new float[]{ev.getY(0), ev.getY(1)};
            } else if (!this.mLauncher.isSceneAnimating()) {
                if (this.mLauncher.isSceneShowing()) {
                    if (!this.mLauncher.isFolderShowing() && ev.getY(0) - this.mOldPositions[0] < ((float) (-this.mScaledUpsideScreenOutTouch)) && ev.getY(1) - this.mOldPositions[1] < ((float) (-this.mScaledUpsideScreenOutTouch))) {
                        this.mOldPositions = null;
                        this.mLauncher.hideSceneScreen(true);
                        return true;
                    }
                } else if (!this.mLauncher.isInEditing() && !this.mLauncher.isFolderShowing() && !this.mLauncher.isPreviewShowing() && ev.getY(0) - this.mOldPositions[0] > ((float) this.mScaledUpsideScreenOutTouch) && ev.getY(1) - this.mOldPositions[1] > ((float) this.mScaledUpsideScreenOutTouch) && this.mLauncher.isFreeStyleExists() && this.mLauncher.getWorkspace().isTouchStateNotInScroll()) {
                    this.mOldPositions = null;
                    this.mLauncher.getWorkspace().finishCurrentGesture();
                    this.mLauncher.showSceneScreen();
                    return true;
                }
            }
        } else if (this.mOldPositions != null) {
            this.mOldPositions = null;
        }
        clearAllResizeFrames();
        return this.mDragController.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        int action = ev.getAction();
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if (ev.getAction() == 0 && handleTouchDown(ev, false)) {
            return true;
        }
        if (this.mCurrentResizeFrame != null) {
            handled = true;
            switch (action) {
                case 1:
                case 3:
                    this.mCurrentResizeFrame.onTouchUp();
                    this.mCurrentResizeFrame = null;
                    break;
                case 2:
                    this.mCurrentResizeFrame.visualizeResizeForDelta(x - this.mXDown, y - this.mYDown);
                    break;
            }
        }
        if (handled) {
            return true;
        }
        return this.mDragController.onTouchEvent(ev);
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        return this.mDragController.dispatchUnhandledMove(focused, direction);
    }

    public float getLocationInDragLayer(View child, int[] loc, boolean ignoreScale) {
        float[] locate = new float[]{0.0f, 0.0f};
        float scale = Utilities.getDescendantCoordRelativeToAncestor(child, this, locate, true, ignoreScale);
        loc[0] = Math.round(locate[0]);
        loc[1] = Math.round(locate[1]);
        return scale;
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mLauncher.getWindowManager().getDefaultDisplay().getSize(this.mScreenSize);
    }

    public void updateWallpaper() {
        String scrollCfg = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString("pref_key_wallpaper_scroll_type", "byTheme");
        if (scrollCfg.equals("byTheme")) {
            scrollCfg = getResources().getString(R.string.wallpaper_scrolling);
        }
        this.mWpScrolling = false;
        if (scrollCfg.equals("left")) {
            this.mWpOffsetX = 0.0f;
        } else if (scrollCfg.equals("center")) {
            this.mWpOffsetX = 0.5f;
        } else if (scrollCfg.equals("right")) {
            this.mWpOffsetX = 1.0f;
        } else if (!scrollCfg.equals("none")) {
            this.mWpScrolling = true;
        }
        updateWallpaperOffset();
    }

    public boolean updateWallpaperOffset(float xStep, float yStep, float xOffset, float yOffset) {
        if (!this.mWpScrolling || this.mWpOffsetX == xOffset) {
            return false;
        }
        this.mWpStepX = xStep;
        this.mWpStepY = yStep;
        this.mWpOffsetX = xOffset;
        this.mWpOffsetY = yOffset;
        updateWallpaperOffset();
        return true;
    }

    public int getWallpaperLayer() {
        String dump = Utilities.dumpsys(this.mLauncher, "window", new String[]{"visible"});
        if (dump == null) {
            return -1;
        }
        int servicePos = dump.indexOf("mIsWallpaper=true");
        if (servicePos == -1) {
            return -1;
        }
        String prefix = " layer=";
        int layerPos = dump.indexOf(" layer=", servicePos);
        if (layerPos == -1) {
            return -1;
        }
        int endPos = dump.indexOf(" ", " layer=".length() + layerPos);
        if (endPos == -1) {
            return -1;
        }
        int numPos = layerPos;
        while (numPos < endPos && !Character.isDigit(dump.charAt(numPos))) {
            numPos++;
        }
        if (numPos != endPos) {
            return Integer.parseInt(dump.substring(numPos, endPos));
        }
        return -1;
    }

    public float getWpStepX() {
        return this.mWpStepX;
    }

    public float getWpOffsetX() {
        return this.mWpOffsetX;
    }

    public boolean updateWallpaperOffsetAnimate(float xStep, float yStep, float xOffset, float yOffset) {
        final float xStepDelta = xStep - this.mWpStepX;
        final float yStepDelta = yStep - this.mWpStepY;
        final float mWpOffsetXDelta = xOffset - this.mWpOffsetX;
        final float mWpOffsetYDelta = yOffset - this.mWpOffsetY;
        if (xStepDelta == 0.0f || yStepDelta == 0.0f || mWpOffsetXDelta == 0.0f || mWpOffsetYDelta == 0.0f) {
            return false;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
        final float f = xStep;
        final float f2 = yStep;
        final float f3 = xOffset;
        final float f4 = yOffset;
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                DragLayer.this.updateWallpaperOffset(f - (xStepDelta * value), f2 - (yStepDelta * value), f3 - (mWpOffsetXDelta * value), f4 - (mWpOffsetYDelta * value));
            }
        });
        animator.start();
        return true;
    }

    public void updateWallpaperOffset() {
        this.mWallpaperManager.setWallpaperOffsetSteps(this.mWpStepX, this.mWpStepY);
        if (getWindowToken() != null) {
            this.mWallpaperManager.setWallpaperOffsets(getWindowToken(), this.mWpOffsetX, this.mWpOffsetY);
        } else {
            removeCallbacks(this.OffsetUpdater);
            postDelayed(this.OffsetUpdater, 50);
        }
        this.mOldOffsetX = (int) (((float) (this.mWpWidth - this.mScreenSize.x)) * this.mWpOffsetX);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        DeviceConfig.loadScreenSize(getContext(), getResources());
        if (DeviceConfig.isRotatable() && DeviceConfig.isScreenOrientationChanged()) {
            this.mLauncher.onScreenOrientationChanged();
        }
        if (DeviceConfig.isScreenSizeChanged()) {
            this.mLauncher.onScreenSizeChanged();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            android.widget.FrameLayout.LayoutParams flp = (android.widget.FrameLayout.LayoutParams) child.getLayoutParams();
            if (flp instanceof LayoutParams) {
                LayoutParams lp = (LayoutParams) flp;
                if (lp.customPosition) {
                    child.layout(lp.x, lp.y, lp.x + lp.width, lp.y + lp.height);
                }
            }
        }
    }

    public void clearAllResizeFrames() {
        if (this.mResizeFrames.size() > 0) {
            Iterator i$ = this.mResizeFrames.iterator();
            while (i$.hasNext()) {
                AppWidgetResizeFrame frame = (AppWidgetResizeFrame) i$.next();
                frame.commitResize();
                removeView(frame);
            }
            this.mResizeFrames.clear();
        }
    }

    public boolean isWidgetBeingResized() {
        return this.mCurrentResizeFrame != null;
    }

    public void addResizeFrame(ItemInfo itemInfo, LauncherAppWidgetHostView widget, CellLayout cellLayout) {
        AppWidgetResizeFrame resizeFrame = new AppWidgetResizeFrame(getContext(), widget, cellLayout, this);
        LayoutParams lp = new LayoutParams(-1, -1);
        lp.customPosition = true;
        addView(resizeFrame, lp);
        this.mResizeFrames.add(resizeFrame);
        resizeFrame.snapToWidget(false);
    }

    public boolean gatherTransparentRegion(Region region) {
        region.setEmpty();
        return false;
    }

    public void setClipForDragging(Rect rect) {
        this.mClipForDragging = rect;
    }

    public void attachParasiticDrawingObject(ParasiticDrawingObject pdj) {
        this.mPdoList.add(pdj);
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!this.mPdoList.isEmpty()) {
            Iterator<ParasiticDrawingObject> i = this.mPdoList.iterator();
            while (i.hasNext()) {
                if (!((ParasiticDrawingObject) i.next()).draw(canvas)) {
                    i.remove();
                }
            }
        }
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean isClip = this.mClipForDragging != null && (child instanceof DragView);
        if (isClip) {
            canvas.save();
            canvas.clipRect(this.mClipForDragging);
        }
        boolean r = super.drawChild(canvas, child, drawingTime);
        if (isClip) {
            canvas.restore();
        }
        return r;
    }

    public int highlightLocatedApp(ItemIcon icon, boolean highlightFolder) {
        TypedArray ta = getResources().obtainTypedArray(R.array.app_locating_animation_scaler);
        float[] animScales = new float[ta.length()];
        for (int i = 0; i < animScales.length; i++) {
            animScales[i] = ta.getFloat(i, 0.0f);
        }
        final View maskView = new View(this.mContext);
        maskView.setBackgroundColor(getResources().getColor(R.color.app_locate_mask_color));
        addView(maskView, -1, -1);
        maskView.setAlpha(0.0f);
        maskView.animate().setDuration((long) getResources().getInteger(R.integer.config_app_locate_mask_anim_duration)).alpha(1.0f).start();
        icon.setVisibility(4);
        int[] iconLoc = new int[2];
        icon.getLocationOnScreen(iconLoc);
        final ItemIcon itemIcon = icon;
        final View wrapIconView = new View(this.mContext) {
            protected void onDraw(Canvas canvas) {
                itemIcon.draw(canvas);
            }
        };
        wrapIconView.setLayerType(1, null);
        android.widget.FrameLayout.LayoutParams lp = new android.widget.FrameLayout.LayoutParams(icon.getWidth(), icon.getHeight(), 51);
        lp.leftMargin = iconLoc[0];
        lp.topMargin = iconLoc[1];
        addView(wrapIconView, lp);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(animScales);
        valueAnimator.setDuration((long) getResources().getInteger(R.integer.config_app_locate_animation_duration));
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                wrapIconView.setScaleX(value);
                wrapIconView.setScaleY(value);
            }
        });
        itemIcon = icon;
        final boolean z = highlightFolder;
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                itemIcon.setVisibility(0);
                DragLayer.this.removeView(wrapIconView);
                if (!z) {
                    DragLayer.this.mLauncher.onFinishHighlightLocatedApp();
                }
            }
        });
        valueAnimator.start();
        maskView.postDelayed(new Runnable() {
            public void run() {
                maskView.animate().setDuration((long) DragLayer.this.getResources().getInteger(R.integer.config_app_locate_mask_anim_duration)).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        DragLayer.this.removeView(maskView);
                    }
                }).start();
            }
        }, valueAnimator.getDuration() - maskView.animate().getDuration());
        return (int) valueAnimator.getDuration();
    }
}
