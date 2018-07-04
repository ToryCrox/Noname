package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Scroller;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.transitioneffects.TransitionEffectSwitcher;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ScreenView extends ViewGroup {
    private static final int GROUP_MODE_DEFAULT_OFFSET_X = Utilities.getDipPixelSize(8);
    protected static final int INDICATOR_MEASURE_SPEC = MeasureSpec.makeMeasureSpec(0, 0);
    protected static final LayoutParams SEEK_POINT_LAYOUT_PARAMS = new LayoutParams(-1, -1, 1.0f);
    private static final float SMOOTHING_CONSTANT = ((float) (0.016d / Math.log(0.75d)));
    private final float PREVIEW_MODE_MAX_SCREEN_WIDTH;
    protected int mActivePointerId;
    private boolean mAllowLongPress;
    private ArrowIndicator mArrowLeft;
    private int mArrowLeftOffResId;
    private int mArrowLeftOnResId;
    private ArrowIndicator mArrowRight;
    private int mArrowRightOffResId;
    private int mArrowRightOnResId;
    protected Runnable mAutoHideTimer;
    private int mChildScreenLayoutMeasureDiffX;
    private int mChildScreenMeasureHeight;
    private int mChildScreenMeasureWidth;
    protected OnClickListener mClickListener;
    private int mColumnCountPerScreen;
    private int mColumnGap;
    private float mConfirmHorizontalScrollRatio;
    private boolean mCurrentGestureFinished;
    protected int mCurrentScreen;
    private View mCurrentUnfoldingHeader;
    private ArrayList<View> mCurrentUnfoldingList;
    private boolean mEnableReverseDrawingMode;
    private int mFixedGap;
    protected boolean mGestureTrigged;
    GestureVelocityTracker mGestureVelocityTracker;
    private int mGroupEndIndex;
    private int mGroupModeVisualScreenSize;
    private int mGroupStartIndex;
    private final Map<View, ArrayList<View>> mGroups;
    protected boolean mHasPrefixLinkedScreen;
    protected boolean mHasSuffixLinkedScreen;
    private boolean mIgnoreCenterY;
    private int mIndicatorCount;
    private boolean mIsGroupUnfolding;
    private boolean mIsHidingHeader;
    protected boolean mIsSlideBarAutoHide;
    private int mLastCurrentScreenBeforeUnfolding;
    private float mLastMotionX;
    private float mLastMotionY;
    protected int mLastScrollX;
    protected int mLastVisibleRange;
    private boolean mLayoutScreensSeamless;
    private int mLeftOffset;
    protected OnLongClickListener mLongClickListener;
    private int mMaximumVelocity;
    protected int mNextScreen;
    private float mOverScrollRatio;
    private float mOvershootTension;
    private View mPreviewModeFooter;
    private View mPreviewModeHeader;
    private boolean mPushGestureEnabled;
    private int mRightOccupiedCount;
    private int mRowCountPerScreen;
    private int mRowGap;
    private ScaleGestureDetector mScaleDetector;
    private int mScreenAlignment;
    private int mScreenContentHeight;
    protected int mScreenContentWidth;
    private int mScreenCounter;
    private int mScreenLayoutMode;
    private int mScreenOffset;
    private int mScreenScrollLeftBound;
    private int mScreenScrollRightBound;
    protected SeekBarIndicator mScreenSeekBar;
    private int mScreenSnapDuration;
    private ScreenViewOvershootInterpolator mScrollInterpolator;
    protected int mScrollLeftBound;
    private int mScrollOffset;
    protected int mScrollRightBound;
    private int mScrollStartX;
    private boolean mScrollWholeScreen;
    protected Scroller mScroller;
    private int mScrollingStateStartX;
    private int mSeekPointResId;
    protected SlideBar mSlideBar;
    private float mSmoothingTime;
    private boolean mTouchIntercepted;
    private int mTouchSlop;
    private int mTouchState;
    private float mTouchX;
    protected TransitionEffectSwitcher mTransitionEffect;
    private int mUniformLayoutModeCurrentGap;
    private int mUniformLayoutModeMaxGap;
    protected int mVisibleRange;

    public interface GroupModeItem {
        void onFolding();

        void onUnFolding();
    }

    private interface Indicator {
        boolean fastOffset(int i);
    }

    protected class ArrowIndicator extends ImageView implements Indicator {
        public boolean fastOffset(int offset) {
            if (this.mLeft == offset) {
                return false;
            }
            this.mRight = (this.mRight + offset) - this.mLeft;
            this.mLeft = offset;
            return true;
        }
    }

    public class GestureVelocityTracker {
        private final int DEFAULT_VERTICAL_GESTURE_CONFIRM_DIST = Math.round(50.0f * Resources.getSystem().getDisplayMetrics().density);
        private int mCounter = -1;
        private int mPointerId = -1;
        private Tracker mTx = new Tracker();
        private Tracker mTy = new Tracker();
        private VelocityTracker mVelocityTracker;
        private boolean mVerticalGestureConfirmed;

        private class Tracker {
            float fold;
            float prev;
            float start;

            public Tracker() {
                reset();
            }

            public void reset() {
                this.prev = -1.0f;
                this.fold = -1.0f;
                this.start = -1.0f;
            }
        }

        public void recycle() {
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            reset();
        }

        public void addMovement(MotionEvent ev) {
            int action = ev.getAction() & 255;
            if (action != 1 && action != 3) {
                this.mCounter++;
                if (this.mVelocityTracker == null) {
                    this.mVelocityTracker = VelocityTracker.obtain();
                }
                this.mVelocityTracker.addMovement(ev);
                float curX = ev.getX();
                float curY = ev.getY();
                if (this.mPointerId != -1) {
                    int pIndex = ev.findPointerIndex(this.mPointerId);
                    if (pIndex != -1) {
                        curX = ev.getX(pIndex);
                        curY = ev.getY(pIndex);
                    } else {
                        this.mPointerId = -1;
                    }
                }
                trackPoint(curX, this.mTx);
                trackPoint(curY, this.mTy);
            }
        }

        private void trackPoint(float p, Tracker t) {
            if (t.start < 0.0f) {
                t.start = p;
            } else if (t.prev < 0.0f) {
                t.prev = p;
            } else {
                if (t.fold < 0.0f) {
                    if (((t.prev > t.start && p < t.prev) || (t.prev < t.start && p > t.prev)) && Math.abs(p - t.start) > 3.0f) {
                        t.fold = t.prev;
                    }
                } else if (t.fold != t.prev && (((t.prev > t.fold && p < t.prev) || (t.prev < t.fold && p > t.prev)) && Math.abs(p - t.fold) > 3.0f)) {
                    t.start = t.fold;
                    t.fold = t.prev;
                }
                t.prev = p;
            }
        }

        private void reset() {
            this.mTx.reset();
            this.mTy.reset();
            this.mPointerId = -1;
            this.mCounter = 0;
            this.mVerticalGestureConfirmed = false;
        }

        public void init(int pointerId) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            } else {
                this.mVelocityTracker.clear();
            }
            reset();
            this.mPointerId = pointerId;
        }

        public int getCounter() {
            return this.mCounter;
        }

        public float getXVelocity(int units, int maxVelocity, int pointerId) {
            if (this.mVelocityTracker == null) {
                return (float) ScreenView.this.mMaximumVelocity;
            }
            this.mVelocityTracker.computeCurrentVelocity(units, (float) maxVelocity);
            return this.mVelocityTracker.getXVelocity(pointerId);
        }

        public float getYVelocity(int units, int maxVelocity, int pointerId) {
            if (this.mVelocityTracker == null) {
                return (float) ScreenView.this.mMaximumVelocity;
            }
            this.mVelocityTracker.computeCurrentVelocity(units, (float) maxVelocity);
            return this.mVelocityTracker.getYVelocity(pointerId);
        }

        public int getVerticalGesture() {
            if (this.mVerticalGestureConfirmed || getCounter() < 5 || Math.abs(getYVelocity(1000, ScreenView.this.mMaximumVelocity, 0)) < ((float) (ScreenView.this.mMaximumVelocity / 3)) || Math.abs(this.mTy.start - this.mTy.prev) <= ((float) this.DEFAULT_VERTICAL_GESTURE_CONFIRM_DIST)) {
                return 0;
            }
            this.mVerticalGestureConfirmed = true;
            return this.mTy.start > this.mTy.prev ? 10 : 11;
        }

        public int getXFlingDirection(float velocity) {
            return getFlingDirection(this.mTx, velocity);
        }

        public int getFlingDirection(Tracker t, float velocity) {
            if (velocity <= 300.0f) {
                return 4;
            }
            if (t.fold < 0.0f) {
                if (t.prev > t.start) {
                    return 1;
                }
                return 2;
            } else if (t.prev < t.fold) {
                if (ScreenView.this.mScrollX < ScreenView.this.getCurrentScreen().getLeft()) {
                    return 3;
                }
                return 2;
            } else if (t.prev <= t.fold) {
                return 3;
            } else {
                if (ScreenView.this.mScrollX > ScreenView.this.getCurrentScreen().getLeft()) {
                    return 3;
                }
                return 1;
            }
        }
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int currentScreen;

        SavedState(Parcelable superState) {
            super(superState);
            this.currentScreen = -1;
        }

        private SavedState(Parcel in) {
            super(in);
            this.currentScreen = -1;
            this.currentScreen = in.readInt();
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.currentScreen);
        }
    }

    private class ScaleDetectorListener implements OnScaleGestureListener {
        private ScaleDetectorListener() {
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return ScreenView.this.mTouchState == 0;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            ScreenView.this.finishCurrentGesture();
        }

        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            if (ScreenView.this.mTouchState == 0 && (((float) detector.getTimeDelta()) > 200.0f || scale < 0.95f || scale > 1.0526316f)) {
                ScreenView.this.setTouchState(null, 4);
            }
            if (scale < 0.8f) {
                ScreenView.this.onPinchIn(detector);
                return true;
            } else if (scale <= 1.2f) {
                return false;
            } else {
                ScreenView.this.onPinchOut(detector);
                return true;
            }
        }
    }

    private class ScreenViewOvershootInterpolator implements Interpolator {
        private float mTension;

        public ScreenViewOvershootInterpolator() {
            this.mTension = ScreenView.this.mOvershootTension;
        }

        public void setDistance(int distance, int velocity) {
            this.mTension = distance > 0 ? ScreenView.this.mOvershootTension / ((float) distance) : ScreenView.this.mOvershootTension;
        }

        public void disableSettle() {
            this.mTension = 0.0f;
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((t * t) * (((this.mTension + 1.0f) * t) + this.mTension)) + 1.0f;
        }
    }

    protected class SeekBarIndicator extends LinearLayout implements Indicator {
        public SeekBarIndicator(Context context) {
            super(context);
        }

        public boolean fastOffset(int offset) {
            if (this.mLeft == offset) {
                return false;
            }
            this.mRight = (this.mRight + offset) - this.mLeft;
            this.mLeft = offset;
            return true;
        }
    }

    protected class SlideBar extends FrameLayout implements Indicator {
        private Rect mPadding = new Rect();
        private Rect mPos = new Rect();
        private NinePatch mSlidePoint;
        private Bitmap mSlidePointBmp;

        public SlideBar(Context context, int slideDrawableId, int backgroundDrawableId) {
            super(context);
            this.mSlidePointBmp = BitmapFactory.decodeResource(getResources(), slideDrawableId);
            if (this.mSlidePointBmp != null) {
                byte[] npChunk = this.mSlidePointBmp.getNinePatchChunk();
                if (npChunk != null) {
                    this.mSlidePoint = new NinePatch(this.mSlidePointBmp, npChunk, null);
                    FrameLayout background = new FrameLayout(this.mContext);
                    background.setBackgroundResource(backgroundDrawableId);
                    addView(background, new FrameLayout.LayoutParams(-1, -2, 80));
                    this.mPadding.left = background.getPaddingLeft();
                    this.mPadding.top = background.getPaddingTop();
                    this.mPadding.right = background.getPaddingRight();
                    this.mPadding.bottom = background.getPaddingBottom();
                    this.mPos.top = this.mPadding.top;
                    this.mPos.bottom = this.mPos.top + this.mSlidePointBmp.getHeight();
                }
            }
        }

        protected int getSuggestedMinimumHeight() {
            return Math.max(this.mSlidePointBmp.getHeight(), super.getSuggestedMinimumHeight());
        }

        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (this.mSlidePoint != null) {
                this.mSlidePoint.draw(canvas, this.mPos);
            }
        }

        protected boolean setFrame(int left, int top, int right, int bottom) {
            boolean r = super.setFrame(left, top, right, bottom);
            if (this.mSlidePoint != null) {
                this.mPos.bottom = (bottom - top) - this.mPadding.bottom;
                this.mPos.top = this.mPos.bottom - this.mSlidePoint.getHeight();
            }
            return r;
        }

        public void setPosition(int left, int right) {
            this.mPos.left = left;
            this.mPos.right = right;
        }

        public int getSlideWidth() {
            return (getMeasuredWidth() - this.mPadding.left) - this.mPadding.right;
        }

        public int getSlidePaddingLeft() {
            return this.mPadding.left;
        }

        public boolean fastOffset(int offset) {
            if (this.mLeft == offset) {
                return false;
            }
            this.mRight = (this.mRight + offset) - this.mLeft;
            this.mLeft = offset;
            return true;
        }
    }

    private class SliderTouchListener implements OnTouchListener {
        private SliderTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            int sliderWidth = v.getWidth();
            float x = Math.max(0.0f, Math.min(event.getX(), (float) (sliderWidth - 1)));
            int screenCount = ScreenView.this.getScreenCount();
            int pos = (int) Math.floor((double) ((((float) screenCount) * x) / ((float) sliderWidth)));
            switch (event.getAction()) {
                case 0:
                    if (!ScreenView.this.mScroller.isFinished()) {
                        ScreenView.this.mScroller.abortAnimation();
                    }
                    ScreenView.this.setTouchState(event, 3);
                    break;
                case 1:
                case 3:
                    ScreenView.this.snapToScreen(pos);
                    ScreenView.this.updateSeekPoints(ScreenView.this.mNextScreen);
                    break;
                case 2:
                    ScreenView.this.setCurrentScreenInner(pos);
                    ScreenView.this.scrollTo((int) (((((float) (ScreenView.this.mChildScreenMeasureWidth * screenCount)) * x) / ((float) sliderWidth)) - ((float) (ScreenView.this.mChildScreenMeasureWidth / 2))), 0);
                    break;
            }
            return true;
        }
    }

    public void setFixedGap(int gap) {
        this.mFixedGap = gap;
    }

    public void ignoreCenterY(boolean ignore) {
        this.mIgnoreCenterY = ignore;
    }

    public void setOvershootTension(float tension) {
        this.mOvershootTension = tension;
        if (this.mScrollInterpolator != null) {
            this.mScrollInterpolator.mTension = tension;
        }
    }

    public void setScreenSnapDuration(int duration) {
        this.mScreenSnapDuration = duration;
    }

    public int getScreenSnapMaxDuration() {
        return (int) (((float) this.mScreenSnapDuration) * 1.5f);
    }

    public void setMaximumSnapVelocity(int velocity) {
        this.mMaximumVelocity = velocity;
    }

    public void setScrollWholeScreen(boolean wholeScreen) {
        this.mScrollWholeScreen = wholeScreen;
    }

    public void setScreenScrollRange(int leftIndex, int rightIndex) {
        if (this.mScreenLayoutMode != 5) {
            this.mScreenScrollLeftBound = leftIndex;
            this.mScreenScrollRightBound = rightIndex;
            refreshScrollBound();
            correctCurrentScreen(true);
        }
    }

    public void resetScreenScrollRange() {
        this.mScreenScrollLeftBound = 0;
        this.mScreenScrollRightBound = Integer.MAX_VALUE;
        refreshScrollBound();
        correctCurrentScreen(true);
    }

    public void setPushGestureEnabled(boolean pushGesture) {
        this.mPushGestureEnabled = pushGesture;
    }

    public ScreenView(Context context) {
        super(context);
        this.mArrowLeftOnResId = R.drawable.screen_view_arrow_left;
        this.mArrowLeftOffResId = R.drawable.screen_view_arrow_left_gray;
        this.mArrowRightOnResId = R.drawable.screen_view_arrow_right;
        this.mArrowRightOffResId = R.drawable.screen_view_arrow_right_gray;
        this.mSeekPointResId = R.drawable.workspace_seekpoint;
        this.mAutoHideTimer = new Runnable() {
            public void run() {
                ScreenView.this.startHideSlideBar();
            }
        };
        this.PREVIEW_MODE_MAX_SCREEN_WIDTH = 0.2f;
        this.mScreenLayoutMode = 0;
        this.mLayoutScreensSeamless = false;
        this.mUniformLayoutModeMaxGap = Integer.MAX_VALUE;
        this.mUniformLayoutModeCurrentGap = this.mUniformLayoutModeMaxGap;
        this.mGroups = new HashMap();
        this.mIsGroupUnfolding = false;
        this.mIsHidingHeader = false;
        this.mLastCurrentScreenBeforeUnfolding = -1;
        this.mGroupStartIndex = -1;
        this.mGroupEndIndex = -1;
        this.mLeftOffset = 0;
        this.mRightOccupiedCount = 0;
        this.mGroupModeVisualScreenSize = 0;
        this.mFixedGap = 0;
        this.mIgnoreCenterY = false;
        this.mScreenAlignment = 1;
        this.mScreenOffset = 0;
        this.mScrollOffset = 0;
        this.mLastVisibleRange = -1;
        this.mVisibleRange = -1;
        this.mChildScreenMeasureWidth = -1;
        this.mChildScreenMeasureHeight = -1;
        this.mChildScreenLayoutMeasureDiffX = 0;
        this.mScreenContentWidth = 0;
        this.mScreenContentHeight = 0;
        this.mRowCountPerScreen = -1;
        this.mColumnCountPerScreen = -1;
        this.mRowGap = -1;
        this.mColumnGap = -1;
        this.mPreviewModeHeader = null;
        this.mPreviewModeFooter = null;
        this.mCurrentScreen = -1;
        this.mNextScreen = -1;
        this.mScreenScrollLeftBound = 0;
        this.mScreenScrollRightBound = Integer.MAX_VALUE;
        this.mOverScrollRatio = 0.33333334f;
        this.mHasSuffixLinkedScreen = false;
        this.mHasPrefixLinkedScreen = false;
        this.mScrollWholeScreen = false;
        this.mScreenCounter = 0;
        this.mTouchState = 0;
        this.mPushGestureEnabled = false;
        this.mScrollStartX = 0;
        this.mScrollingStateStartX = 0;
        this.mGestureTrigged = false;
        this.mAllowLongPress = true;
        this.mActivePointerId = -1;
        this.mConfirmHorizontalScrollRatio = 0.5f;
        this.mTransitionEffect = new TransitionEffectSwitcher();
        this.mLastScrollX = 0;
        this.mOvershootTension = 1.3f;
        this.mScreenSnapDuration = 300;
        this.mEnableReverseDrawingMode = false;
        this.mGestureVelocityTracker = new GestureVelocityTracker();
        initScreenView();
    }

    public ScreenView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScreenView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mArrowLeftOnResId = R.drawable.screen_view_arrow_left;
        this.mArrowLeftOffResId = R.drawable.screen_view_arrow_left_gray;
        this.mArrowRightOnResId = R.drawable.screen_view_arrow_right;
        this.mArrowRightOffResId = R.drawable.screen_view_arrow_right_gray;
        this.mSeekPointResId = R.drawable.workspace_seekpoint;
        this.mAutoHideTimer = /* anonymous class already generated */;
        this.PREVIEW_MODE_MAX_SCREEN_WIDTH = 0.2f;
        this.mScreenLayoutMode = 0;
        this.mLayoutScreensSeamless = false;
        this.mUniformLayoutModeMaxGap = Integer.MAX_VALUE;
        this.mUniformLayoutModeCurrentGap = this.mUniformLayoutModeMaxGap;
        this.mGroups = new HashMap();
        this.mIsGroupUnfolding = false;
        this.mIsHidingHeader = false;
        this.mLastCurrentScreenBeforeUnfolding = -1;
        this.mGroupStartIndex = -1;
        this.mGroupEndIndex = -1;
        this.mLeftOffset = 0;
        this.mRightOccupiedCount = 0;
        this.mGroupModeVisualScreenSize = 0;
        this.mFixedGap = 0;
        this.mIgnoreCenterY = false;
        this.mScreenAlignment = 1;
        this.mScreenOffset = 0;
        this.mScrollOffset = 0;
        this.mLastVisibleRange = -1;
        this.mVisibleRange = -1;
        this.mChildScreenMeasureWidth = -1;
        this.mChildScreenMeasureHeight = -1;
        this.mChildScreenLayoutMeasureDiffX = 0;
        this.mScreenContentWidth = 0;
        this.mScreenContentHeight = 0;
        this.mRowCountPerScreen = -1;
        this.mColumnCountPerScreen = -1;
        this.mRowGap = -1;
        this.mColumnGap = -1;
        this.mPreviewModeHeader = null;
        this.mPreviewModeFooter = null;
        this.mCurrentScreen = -1;
        this.mNextScreen = -1;
        this.mScreenScrollLeftBound = 0;
        this.mScreenScrollRightBound = Integer.MAX_VALUE;
        this.mOverScrollRatio = 0.33333334f;
        this.mHasSuffixLinkedScreen = false;
        this.mHasPrefixLinkedScreen = false;
        this.mScrollWholeScreen = false;
        this.mScreenCounter = 0;
        this.mTouchState = 0;
        this.mPushGestureEnabled = false;
        this.mScrollStartX = 0;
        this.mScrollingStateStartX = 0;
        this.mGestureTrigged = false;
        this.mAllowLongPress = true;
        this.mActivePointerId = -1;
        this.mConfirmHorizontalScrollRatio = 0.5f;
        this.mTransitionEffect = new TransitionEffectSwitcher();
        this.mLastScrollX = 0;
        this.mOvershootTension = 1.3f;
        this.mScreenSnapDuration = 300;
        this.mEnableReverseDrawingMode = false;
        this.mGestureVelocityTracker = new GestureVelocityTracker();
        initScreenView();
    }

    private void initScreenView() {
        setAlwaysDrawnWithCacheEnabled(true);
        this.mScrollInterpolator = new ScreenViewOvershootInterpolator();
        this.mScroller = new Scroller(this.mContext, this.mScrollInterpolator);
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = (int) (((float) (configuration.getScaledTouchSlop() * this.mContext.getResources().getConfiguration().smallestScreenWidthDp)) / 320.0f);
        setMaximumSnapVelocity(configuration.getScaledMaximumFlingVelocity());
        this.mScaleDetector = new ScaleGestureDetector(this.mContext, new ScaleDetectorListener());
    }

    public void setSeekPointResource(int seekPointResId) {
        if (this.mSeekPointResId != seekPointResId) {
            this.mSeekPointResId = seekPointResId;
            if (this.mScreenSeekBar != null) {
                int count = getScreenCount();
                for (int i = 0; i < count; i++) {
                    ImageView v = (ImageView) this.mScreenSeekBar.getChildAt(i);
                    if (v != null) {
                        v.setImageResource(this.mSeekPointResId);
                        v.getDrawable().jumpToCurrentState();
                    }
                }
            }
        }
    }

    public void setSeekBarPosition(FrameLayout.LayoutParams params) {
        if (params != null) {
            if (this.mScreenSeekBar == null) {
                this.mScreenSeekBar = new SeekBarIndicator(this.mContext);
                this.mScreenSeekBar.setGravity(16);
                this.mScreenSeekBar.setAnimationCacheEnabled(false);
                addIndicator(this.mScreenSeekBar, params);
                return;
            }
            this.mScreenSeekBar.setLayoutParams(params);
        } else if (this.mScreenSeekBar != null) {
            removeIndicator(this.mScreenSeekBar);
            this.mScreenSeekBar = null;
        }
    }

    public void setSlideBarPosition(FrameLayout.LayoutParams params) {
        setSlideBarPosition(params, R.drawable.screen_view_slide_bar, R.drawable.screen_view_slide_bar_bg, false);
    }

    public void setSlideBarPosition(FrameLayout.LayoutParams params, int slideDrawableId, int backgroundDrawableId, boolean isAutoHide) {
        this.mIsSlideBarAutoHide = isAutoHide;
        if (params != null) {
            if (this.mSlideBar == null) {
                this.mSlideBar = new SlideBar(this.mContext, slideDrawableId, backgroundDrawableId);
                this.mSlideBar.setOnTouchListener(new SliderTouchListener());
                this.mSlideBar.setAnimationCacheEnabled(false);
                addIndicator(this.mSlideBar, params);
                return;
            }
            this.mSlideBar.setLayoutParams(params);
        } else if (this.mSlideBar != null) {
            removeIndicator(this.mSlideBar);
            this.mSlideBar = null;
        }
    }

    private void showSlideBar() {
        if (this.mSlideBar != null && this.mIsSlideBarAutoHide) {
            removeCallbacks(this.mAutoHideTimer);
            this.mSlideBar.animate().cancel();
            this.mSlideBar.setAlpha(1.0f);
            this.mSlideBar.setVisibility(0);
            if (this.mTouchState == 0) {
                postDelayed(this.mAutoHideTimer, 1000);
            }
        }
    }

    private void startHideSlideBar() {
        if (this.mIsSlideBarAutoHide) {
            this.mSlideBar.animate().setDuration(500).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ScreenView.this.mSlideBar.setVisibility(4);
                }

                public void onAnimationCancel(Animator animation) {
                    ScreenView.this.mSlideBar.setVisibility(4);
                    ScreenView.this.mSlideBar.setAlpha(1.0f);
                }
            });
        }
    }

    public void forceEndSlideBarHideAnim() {
        if (this.mSlideBar != null) {
            this.mSlideBar.animate().cancel();
        }
    }

    public void setIndicatorBarVisibility(int visibility) {
        setSeekBarVisibility(visibility);
        setSlideBarVisibility(visibility);
    }

    public void setSeekBarVisibility(int visibility) {
        if (this.mScreenSeekBar != null) {
            this.mScreenSeekBar.setVisibility(visibility);
        }
    }

    public void setSlideBarVisibility(int visibility) {
        if (this.mSlideBar != null) {
            this.mSlideBar.setVisibility(visibility);
        }
    }

    public void setLayoutScreenSeamless(boolean isSeamless) {
        this.mLayoutScreensSeamless = isSeamless;
        requestLayout();
    }

    public void setScreenLayoutMode(int mode) {
        if (this.mScreenLayoutMode != mode) {
            if (this.mScreenLayoutMode == 3) {
                this.mUniformLayoutModeMaxGap = Integer.MAX_VALUE;
            }
            this.mScreenLayoutMode = mode;
            requestLayout();
        }
    }

    protected void onUnfoldGroup(View header, ArrayList<View> members, boolean showAnim) {
        Iterator i$ = members.iterator();
        while (i$.hasNext()) {
            View v = (View) i$.next();
            if (showAnim && (v instanceof GroupModeItem)) {
                ((GroupModeItem) v).onUnFolding();
            }
        }
    }

    protected void onFoldGroup(View header, ArrayList<View> members, boolean showAnim) {
        Iterator i$ = members.iterator();
        while (i$.hasNext()) {
            View v = (View) i$.next();
            if (showAnim && (v instanceof GroupModeItem)) {
                ((GroupModeItem) v).onFolding();
            }
        }
    }

    protected void foldingGroupMembers() {
        if (this.mIsGroupUnfolding) {
            this.mGroups.put(this.mCurrentUnfoldingHeader, this.mCurrentUnfoldingList);
            this.mGroupModeVisualScreenSize = -1;
            onFoldGroup(this.mCurrentUnfoldingHeader, this.mCurrentUnfoldingList, true);
            this.mIsGroupUnfolding = false;
            this.mIsHidingHeader = false;
            this.mGroupStartIndex = -1;
            this.mGroupEndIndex = -1;
            this.mLeftOffset = 0;
            this.mRightOccupiedCount = 0;
            this.mCurrentScreen = this.mLastCurrentScreenBeforeUnfolding;
            refreshScrollBound();
            requestLayout();
            snapToScreen(this.mCurrentScreen);
        }
    }

    private void reComputeGroupParams() {
        int i = 0;
        if (this.mLastVisibleRange != this.mVisibleRange) {
            int i2;
            this.mIsGroupUnfolding = false;
            int visualIndex = getVisualIndexInGroupMode(this.mGroupStartIndex);
            int size = this.mCurrentUnfoldingList.size();
            if (this.mIsHidingHeader) {
                i2 = 0;
            } else {
                i2 = 1;
            }
            int groupSize = size + i2;
            if (groupSize % this.mVisibleRange != 0) {
                i = this.mVisibleRange - (groupSize % this.mVisibleRange);
            }
            this.mRightOccupiedCount = i;
            this.mLeftOffset = visualIndex % this.mVisibleRange;
            this.mIsGroupUnfolding = true;
        }
    }

    protected boolean unfoldingGroupMembers(View header, boolean hideHeader) {
        int i = 0;
        if (this.mScreenLayoutMode != 5 || this.mIsGroupUnfolding || header == null || !this.mGroups.containsKey(header)) {
            return false;
        }
        int i2;
        this.mCurrentUnfoldingHeader = header;
        this.mCurrentUnfoldingList = (ArrayList) this.mGroups.get(header);
        onUnfoldGroup(this.mCurrentUnfoldingHeader, this.mCurrentUnfoldingList, true);
        this.mGroupModeVisualScreenSize = -1;
        int indexOfChild = indexOfChild(header);
        if (hideHeader) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        int groupStartIndex = indexOfChild + i2;
        indexOfChild = this.mCurrentUnfoldingList.size();
        if (hideHeader) {
            i2 = 0;
        } else {
            i2 = 1;
        }
        int groupSize = indexOfChild + i2;
        int visualIndex = getVisualIndexInGroupMode(groupStartIndex);
        if (groupSize % this.mVisibleRange != 0) {
            i = this.mVisibleRange - (groupSize % this.mVisibleRange);
        }
        this.mRightOccupiedCount = i;
        this.mLeftOffset = visualIndex % this.mVisibleRange;
        if (DeviceConfig.isLayoutRtl()) {
            this.mRightOccupiedCount = this.mVisibleRange;
            this.mLeftOffset = groupSize > this.mVisibleRange ? groupSize : this.mVisibleRange;
        }
        if (this.mLayoutScreensSeamless && getVisualIndexInGroupMode(groupStartIndex) - getVisualIndexInGroupMode(this.mCurrentScreen) >= this.mVisibleRange) {
            this.mLeftOffset += this.mVisibleRange;
        }
        this.mGroupStartIndex = groupStartIndex;
        this.mGroupEndIndex = (groupStartIndex + groupSize) - 1;
        this.mGroups.remove(header);
        this.mIsGroupUnfolding = true;
        this.mIsHidingHeader = hideHeader;
        this.mLastCurrentScreenBeforeUnfolding = this.mCurrentScreen;
        this.mCurrentScreen = groupStartIndex;
        refreshScrollBound();
        snapToScreen(this.mGroupStartIndex);
        requestLayout();
        return true;
    }

    protected void insertGroups(Map<View, ArrayList<View>> groups) {
        this.mGroups.clear();
        for (View v : groups.keySet()) {
            ArrayList<View> members = (ArrayList) groups.get(v);
            if (members.size() > 0) {
                this.mGroups.put(v, members);
            }
        }
        this.mGroupModeVisualScreenSize = -1;
    }

    protected boolean isGroupHeader(View v) {
        if (v != null && this.mGroups.containsKey(v)) {
            return true;
        }
        return false;
    }

    protected boolean hasGroupUnfolding() {
        return this.mIsGroupUnfolding;
    }

    protected int getScreenIndexByPoint(int x, int y) {
        int index;
        int wholeScreenIndex = x / getWidth();
        if (this.mScreenLayoutMode == 4 || this.mScreenLayoutMode == 5) {
            index = ((this.mVisibleRange * wholeScreenIndex) + (this.mColumnCountPerScreen * ((y - this.mPaddingTop) / this.mChildScreenMeasureHeight))) + (((x - this.mPaddingLeft) % getWidth()) / this.mChildScreenMeasureWidth);
        } else {
            index = (this.mVisibleRange * wholeScreenIndex) + (((x - this.mPaddingLeft) % getWidth()) / this.mChildScreenMeasureWidth);
            if (DeviceConfig.isLayoutRtl()) {
                index = (getScreenCount() - index) - 1;
            }
        }
        return Math.min(index, getScreenCount() - 1);
    }

    private int getGroupBounderIndex(int direction, int currentIndex) {
        View curScreen = getScreen(currentIndex);
        if (direction == -1) {
            for (View key : this.mGroups.keySet()) {
                if (((ArrayList) this.mGroups.get(key)).contains(curScreen)) {
                    return indexOfChild(key);
                }
            }
            return currentIndex;
        } else if (this.mGroups.containsKey(curScreen)) {
            return currentIndex + ((ArrayList) this.mGroups.get(curScreen)).size();
        } else {
            return currentIndex;
        }
    }

    private int calibrateCurrentScreenIndex(int currentScreen) {
        int screenIndex = Math.max(0, Math.min(currentScreen, getScreenCount() - 1));
        int visualIndex = screenIndex;
        if (this.mScreenLayoutMode == 5) {
            visualIndex = getVisualIndexInGroupMode(screenIndex);
        }
        if (visualIndex % this.mVisibleRange != 0) {
            int steps;
            if (DeviceConfig.isLayoutRtl()) {
                steps = (this.mVisibleRange - (visualIndex % this.mVisibleRange)) - 1;
            } else {
                steps = visualIndex % this.mVisibleRange;
            }
            return getSnapToScreenIndex(screenIndex, steps, DeviceConfig.isLayoutRtl() ? 1 : -1);
        } else if (DeviceConfig.isLayoutRtl() && this.mScreenLayoutMode == 6 && visualIndex == 0) {
            return this.mVisibleRange - 1;
        } else {
            return screenIndex;
        }
    }

    private int getSnapToScreenIndex(int currentIndex, int steps, int direction) {
        int resultIndex = currentIndex;
        if (this.mScreenLayoutMode == 5) {
            int bounderIndex = getGroupBounderIndex(direction, currentIndex);
            resultIndex = bounderIndex;
            int skipCount = 0;
            int nextIndex = currentIndex;
            int i = 1;
            while (i < steps) {
                int oldIndex = nextIndex;
                nextIndex = bounderIndex + ((i + skipCount) * direction);
                if (nextIndex >= this.mScreenCounter || nextIndex < 0) {
                    return DeviceConfig.isLayoutRtl() ? oldIndex : currentIndex;
                } else {
                    View nextScreen = getScreen(nextIndex);
                    if (direction == -1) {
                        for (ArrayList<View> group : this.mGroups.values()) {
                            if (group.contains(nextScreen)) {
                                skipCount += group.indexOf(nextScreen) + 1;
                            }
                        }
                    } else if (this.mGroups.containsKey(nextScreen)) {
                        skipCount += ((ArrayList) this.mGroups.get(nextScreen)).size();
                    }
                    i++;
                }
            }
            resultIndex += (steps + skipCount) * direction;
        } else {
            resultIndex += direction * steps;
        }
        return Math.max(0, Math.min(resultIndex, getScreenCount() - 1));
    }

    private int getScreenActualIndexInGroupMode(int visualScreenIndex) {
        int finalIndex = visualScreenIndex;
        if (this.mIsGroupUnfolding) {
            finalIndex += this.mLeftOffset;
            if (visualScreenIndex > this.mGroupEndIndex) {
                finalIndex -= this.mRightOccupiedCount;
            }
        }
        for (int i = 0; i < finalIndex; i++) {
            View screen = getScreen(i);
            if (this.mGroups.containsKey(screen)) {
                finalIndex += ((ArrayList) this.mGroups.get(screen)).size();
            }
        }
        return finalIndex;
    }

    private int getGroupModeVisualScreenSize() {
        if (this.mGroupModeVisualScreenSize == -1) {
            this.mGroupModeVisualScreenSize = this.mScreenCounter;
            for (View header : this.mGroups.keySet()) {
                this.mGroupModeVisualScreenSize -= ((ArrayList) this.mGroups.get(header)).size();
            }
        }
        return this.mGroupModeVisualScreenSize;
    }

    private int getVisualIndexInGroupMode(int screenIndex) {
        int finalIndex = screenIndex;
        View targetScreen = getScreen(screenIndex);
        for (int i = 0; i < screenIndex; i++) {
            View screen = getScreen(i);
            if (this.mGroups.containsKey(screen)) {
                ArrayList<View> members = (ArrayList) this.mGroups.get(screen);
                if (members.contains(targetScreen)) {
                    finalIndex -= members.indexOf(targetScreen) + 1;
                    break;
                }
                finalIndex -= members.size();
            }
        }
        if (!this.mIsGroupUnfolding) {
            return finalIndex;
        }
        if (DeviceConfig.isLayoutRtl()) {
            if (screenIndex < this.mGroupStartIndex) {
                finalIndex -= this.mRightOccupiedCount;
            }
            return finalIndex + this.mLeftOffset;
        }
        if (screenIndex > this.mGroupEndIndex) {
            finalIndex += this.mRightOccupiedCount;
        }
        return finalIndex - this.mLeftOffset;
    }

    public int getScreenLayoutMode() {
        return this.mScreenLayoutMode;
    }

    public void setPreviewModeFooter(View footer) {
        this.mPreviewModeFooter = footer;
        requestLayout();
    }

    private void updateScreenOffset() {
        switch (this.mScreenAlignment) {
            case 0:
                this.mScrollOffset = this.mScreenOffset;
                return;
            case 1:
                this.mScrollOffset = 0;
                return;
            case 2:
                this.mScrollOffset = (this.mScreenContentWidth - this.mChildScreenMeasureWidth) / 2;
                return;
            case 3:
                this.mScrollOffset = this.mScreenContentWidth - this.mChildScreenMeasureWidth;
                return;
            default:
                return;
        }
    }

    private void updateIndicatorPositions(int scrollX, boolean requestLayout) {
        if (getWidth() > 0) {
            int indexOffset = getScreenCount();
            int screenWidth = getWidth();
            int screenHeight = getHeight();
            for (int i = 0; i < this.mIndicatorCount; i++) {
                View indicator = getChildAt(i + indexOffset);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) indicator.getLayoutParams();
                int indicatorWidth = indicator.getMeasuredWidth();
                int indicatorHeight = indicator.getMeasuredHeight();
                int indicatorLeft = 0;
                int indicatorTop = 0;
                int gravity = lp.gravity;
                if (gravity != -1) {
                    int verticalGravity = gravity & 112;
                    switch (gravity & 7) {
                        case 1:
                            indicatorLeft = (((screenWidth - indicatorWidth) / 2) + lp.leftMargin) - lp.rightMargin;
                            break;
                        case 3:
                            indicatorLeft = lp.leftMargin;
                            break;
                        case 5:
                            indicatorLeft = (screenWidth - indicatorWidth) - lp.rightMargin;
                            break;
                        default:
                            indicatorLeft = lp.leftMargin;
                            break;
                    }
                    switch (verticalGravity) {
                        case 16:
                            indicatorTop = (((screenHeight - indicatorHeight) / 2) + lp.topMargin) - lp.bottomMargin;
                            break;
                        case 48:
                            indicatorTop = lp.topMargin;
                            break;
                        case 80:
                            indicatorTop = (screenHeight - indicatorHeight) - lp.bottomMargin;
                            break;
                        default:
                            indicatorTop = lp.topMargin;
                            break;
                    }
                }
                if (requestLayout || indicator.getHeight() <= 0 || indicator.getWidth() <= 0) {
                    if (VERSION.SDK_INT > 16) {
                        scrollX = 0;
                    }
                    indicator.layout(scrollX + indicatorLeft, indicatorTop, (scrollX + indicatorLeft) + indicatorWidth, indicatorTop + indicatorHeight);
                } else if (VERSION.SDK_INT > 16) {
                    indicator.setTranslationX((float) scrollX);
                } else if (((Indicator) indicator).fastOffset(scrollX + indicatorLeft)) {
                    indicator.invalidate();
                }
            }
        }
    }

    private void updateSlidePointPosition(int scrollX) {
        int screenCount = getScreenCount();
        if (this.mSlideBar != null && screenCount > 0) {
            int visualScreenCount;
            int[] bounds = getSnapBound();
            bounds[1] = bounds[1] + this.mScreenContentWidth;
            int slideBarWidth = this.mSlideBar.getSlideWidth();
            if (this.mIsGroupUnfolding) {
                visualScreenCount = (this.mGroupEndIndex - this.mGroupStartIndex) + 1;
            } else if (this.mScreenLayoutMode == 5) {
                visualScreenCount = getGroupModeVisualScreenSize();
            } else {
                visualScreenCount = screenCount;
            }
            float wholeScreenCount = (float) (((visualScreenCount - 1) + this.mVisibleRange) / this.mVisibleRange);
            if (this.mScreenLayoutMode == 5 && this.mLayoutScreensSeamless) {
                wholeScreenCount = ((float) (this.mChildScreenMeasureWidth * visualScreenCount)) / ((float) this.mScreenContentWidth);
            }
            if (wholeScreenCount > 0.0f) {
                int slidePointX;
                float slidePointWidth = Math.min(Math.max(((float) slideBarWidth) / wholeScreenCount, 48.0f), (float) slideBarWidth);
                if (this.mChildScreenMeasureWidth * visualScreenCount <= slideBarWidth) {
                    slidePointX = 0;
                } else {
                    slidePointX = this.mSlideBar.getSlidePaddingLeft() + ((int) (((((float) scrollX) - ((float) bounds[0])) / ((float) (bounds[1] - bounds[0]))) * ((float) slideBarWidth)));
                }
                if (this.mScreenLayoutMode == 2) {
                    slidePointX = 0;
                    slidePointWidth = (float) slideBarWidth;
                }
                float slidePointRight = Math.min(((float) slidePointX) + slidePointWidth, (float) (this.mSlideBar.getSlidePaddingLeft() + slideBarWidth));
                this.mSlideBar.setPosition(slidePointX, (int) slidePointRight);
                if (slidePointRight - ((float) slidePointX) == ((float) slideBarWidth)) {
                    this.mSlideBar.setVisibility(4);
                } else {
                    this.mSlideBar.setVisibility(0);
                }
                if (isHardwareAccelerated()) {
                    this.mSlideBar.invalidate();
                }
            }
        }
    }

    private void updateArrowIndicatorResource(int x) {
        if (this.mArrowLeft == null) {
            return;
        }
        int i;
        if (this.mIsGroupUnfolding) {
            this.mArrowLeft.setImageResource(x <= this.mScrollLeftBound ? this.mArrowLeftOffResId : this.mArrowLeftOnResId);
            ArrowIndicator arrowIndicator = this.mArrowRight;
            if (x >= this.mScrollRightBound) {
                i = this.mArrowRightOffResId;
            } else {
                i = this.mArrowRightOnResId;
            }
            arrowIndicator.setImageResource(i);
            return;
        }
        int screenCount;
        this.mArrowLeft.setImageResource(x <= 0 ? this.mArrowLeftOffResId : this.mArrowLeftOnResId);
        if (this.mScreenLayoutMode == 5) {
            screenCount = getGroupModeVisualScreenSize();
        } else {
            screenCount = this.mScreenCounter;
        }
        arrowIndicator = this.mArrowRight;
        if (x >= ((this.mChildScreenMeasureWidth * screenCount) - this.mScreenContentWidth) - this.mScrollOffset) {
            i = this.mArrowRightOffResId;
        } else {
            i = this.mArrowRightOnResId;
        }
        arrowIndicator.setImageResource(i);
    }

    public void setOverScrollRatio(float ratio) {
        this.mOverScrollRatio = ratio;
        requestLayout();
    }

    public void setHasSuffixLinkedScreen(boolean hasLinkedScreen) {
        this.mHasSuffixLinkedScreen = hasLinkedScreen;
        requestLayout();
    }

    public void setHasPrefixLinkedScreen(boolean hasLinkedScreen) {
        this.mHasPrefixLinkedScreen = hasLinkedScreen;
        requestLayout();
    }

    private int[] getSnapBound() {
        int overScrollSize = (this.mIsGroupUnfolding || this.mScreenLayoutMode == 2) ? 0 : (int) (((float) this.mChildScreenMeasureWidth) * this.mOverScrollRatio);
        int[] bounds = new int[2];
        if (this.mHasSuffixLinkedScreen) {
            bounds[1] = this.mScrollRightBound - this.mScreenContentWidth;
        } else {
            bounds[1] = this.mScrollRightBound - overScrollSize;
        }
        if (this.mHasPrefixLinkedScreen) {
            bounds[0] = this.mScrollLeftBound + this.mScreenContentWidth;
        } else {
            bounds[0] = this.mScrollLeftBound + overScrollSize;
        }
        return bounds;
    }

    public boolean refreshScrollBound() {
        int screenContentWidth = this.mScreenContentWidth;
        if (this.mLayoutScreensSeamless) {
            screenContentWidth = this.mVisibleRange * this.mChildScreenMeasureWidth;
        }
        if (this.mIsGroupUnfolding) {
            reComputeGroupParams();
            this.mScrollLeftBound = getScreenSnapX(DeviceConfig.isLayoutRtl() ? this.mGroupEndIndex : this.mGroupStartIndex);
            if (this.mLayoutScreensSeamless) {
                int rightOffset = 0;
                if ((this.mGroupEndIndex - this.mGroupStartIndex) + 1 > this.mVisibleRange) {
                    rightOffset = (((this.mGroupEndIndex - this.mGroupStartIndex) + 1) * this.mChildScreenMeasureWidth) - this.mScreenContentWidth;
                }
                this.mScrollRightBound = this.mScrollLeftBound + rightOffset;
            } else {
                this.mScrollRightBound = (getVisualIndexInGroupMode(this.mGroupEndIndex) / this.mVisibleRange) * screenContentWidth;
            }
            if (this.mLastVisibleRange != this.mVisibleRange) {
                return true;
            }
            return false;
        }
        int oldLeftBound = this.mScrollLeftBound;
        int oldRightBound = this.mScrollRightBound;
        int screenLeftBound = Math.max(this.mScreenScrollLeftBound, 0);
        int screenRightBound = Math.min(this.mScreenScrollRightBound, getScreenCount() - 1);
        if (DeviceConfig.isLayoutRtl()) {
            int temp = screenLeftBound;
            screenLeftBound = screenRightBound;
            if (this.mScreenLayoutMode == 1 || this.mScreenLayoutMode == 6) {
                screenLeftBound = (((screenLeftBound / this.mVisibleRange) + 1) * this.mVisibleRange) - 1;
            }
            screenRightBound = temp;
        }
        int overScrollSize = (int) (((float) this.mChildScreenMeasureWidth) * this.mOverScrollRatio);
        if (this.mHasPrefixLinkedScreen) {
            this.mScrollLeftBound = getScreenSnapX(screenLeftBound) - this.mScreenContentWidth;
        } else {
            this.mScrollLeftBound = getScreenSnapX(screenLeftBound) - overScrollSize;
        }
        if (!this.mScrollWholeScreen) {
            this.mScrollRightBound = (getScreenSnapX(screenRightBound) + overScrollSize) + this.mScrollOffset;
        } else if (this.mScreenLayoutMode == 2) {
            int i = this.mScrollX;
            this.mScrollLeftBound = i;
            this.mScrollRightBound = i;
        } else {
            int lastScreenIndex = getVisualPosition(screenRightBound);
            if (this.mHasSuffixLinkedScreen) {
                this.mScrollRightBound = ((lastScreenIndex / this.mVisibleRange) * screenContentWidth) + this.mScreenContentWidth;
            } else {
                this.mScrollRightBound = ((lastScreenIndex / this.mVisibleRange) * screenContentWidth) + overScrollSize;
            }
            if (this.mScreenLayoutMode == 5 && this.mLayoutScreensSeamless && lastScreenIndex > this.mVisibleRange - 1) {
                this.mScrollRightBound = (((lastScreenIndex + 1) * this.mChildScreenMeasureWidth) - this.mScreenContentWidth) + overScrollSize;
            }
        }
        if (this.mScreenSeekBar != null) {
            if (DeviceConfig.isLayoutRtl()) {
                temp = screenLeftBound;
                screenLeftBound = screenRightBound;
                screenRightBound = temp;
            }
            int count = getScreenCount();
            int i2 = 0;
            while (i2 < count) {
                View v = this.mScreenSeekBar.getChildAt(i2);
                if (v != null) {
                    v.setContentDescription(this.mContext.getString(R.string.screen_number, new Object[]{Integer.valueOf(i2 + 1)}));
                    if (i2 < screenLeftBound || i2 > screenRightBound) {
                        i = 8;
                    } else {
                        i = 0;
                    }
                    v.setVisibility(i);
                }
                i2++;
            }
        }
        return (oldLeftBound == this.mScrollLeftBound && oldRightBound == this.mScrollRightBound && this.mScrollX >= this.mScrollLeftBound) ? false : true;
    }

    public void scrollToScreen(int index) {
        if (this.mScrollWholeScreen) {
            index = calibrateCurrentScreenIndex(index);
        }
        if ((this.mScreenLayoutMode == 1 || this.mScreenLayoutMode == 6) && DeviceConfig.isLayoutRtl() && index % this.mVisibleRange != this.mVisibleRange - 1) {
            index += (this.mVisibleRange - 1) - (index % this.mVisibleRange);
        }
        int toIndex = getScreenScrollX(index);
        if (((float) toIndex) > ((float) this.mScrollRightBound) - (((float) this.mChildScreenMeasureWidth) * this.mOverScrollRatio)) {
            toIndex = (int) (((float) this.mScrollRightBound) - (((float) this.mChildScreenMeasureWidth) * this.mOverScrollRatio));
        }
        scrollTo(toIndex, 0);
    }

    private final int getScreenScrollX(int index) {
        if (!isScrollable()) {
            return this.mScrollX;
        }
        if (this.mScreenLayoutMode == 1 && this.mScreenCounter < this.mVisibleRange) {
            return -this.mScrollOffset;
        }
        if (this.mScreenLayoutMode != 5 || getGroupModeVisualScreenSize() >= this.mVisibleRange) {
            return getScreenSnapX(index) - this.mScrollOffset;
        }
        return -this.mScrollOffset;
    }

    private final int getScreenSnapX(int index) {
        int x = (getScreenLayoutX(index) - this.mPaddingLeft) - this.mChildScreenLayoutMeasureDiffX;
        if (this.mScreenLayoutMode == 4) {
            return x - ((int) (1.5f * ((float) this.mColumnGap)));
        }
        if (this.mScreenLayoutMode == 1) {
            if (getScreenCount() < this.mVisibleRange) {
                return x - ((this.mScreenContentWidth - (getScreenCount() * this.mChildScreenMeasureWidth)) / 2);
            }
            return x - ((this.mScreenContentWidth - (this.mVisibleRange * this.mChildScreenMeasureWidth)) / 2);
        } else if (this.mScreenLayoutMode == 5) {
            int unfoldingGroupsSize = this.mIsGroupUnfolding ? (this.mGroupEndIndex - this.mGroupStartIndex) + 1 : 0;
            if (!this.mIsGroupUnfolding && getGroupModeVisualScreenSize() < this.mVisibleRange) {
                return x - ((this.mScreenContentWidth - (getGroupModeVisualScreenSize() * this.mChildScreenMeasureWidth)) / 2);
            }
            if (this.mIsGroupUnfolding && unfoldingGroupsSize <= this.mVisibleRange && index >= this.mGroupStartIndex && index <= this.mGroupEndIndex) {
                return x - ((this.mScreenContentWidth - (this.mChildScreenMeasureWidth * unfoldingGroupsSize)) / 2);
            }
            if (this.mLayoutScreensSeamless) {
                return x;
            }
            return x - ((this.mScreenContentWidth - (this.mVisibleRange * this.mChildScreenMeasureWidth)) / 2);
        } else if (this.mScreenLayoutMode == 6) {
            return x - (((this.mScreenContentWidth - (this.mVisibleRange * this.mChildScreenMeasureWidth)) - ((this.mVisibleRange - 1) * this.mFixedGap)) / 2);
        } else {
            return x;
        }
    }

    private final boolean isScrollable() {
        switch (this.mScreenLayoutMode) {
            case 1:
                if (!this.mScrollWholeScreen) {
                    if (this.mScreenCounter <= this.mVisibleRange) {
                        return false;
                    }
                    if (this.mScrollRightBound + this.mChildScreenMeasureWidth < this.mScreenContentWidth) {
                        return false;
                    }
                }
                break;
            case 2:
            case 3:
                return false;
        }
        return true;
    }

    protected boolean isScrolling() {
        return !this.mScroller.isFinished();
    }

    public void scrollTo(int x, int y) {
        int oldScrollX = this.mScrollX;
        if (isScrollable()) {
            this.mTouchX = (float) Math.max(this.mScrollLeftBound, Math.min(x, this.mScrollRightBound));
            this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
            boolean needTransAnim;
            if (this.mScreenLayoutMode == 1 || this.mScreenLayoutMode == 6) {
                needTransAnim = true;
            } else {
                needTransAnim = false;
            }
            if (DeviceConfig.isLayoutRtl() && oldScrollX > this.mScrollRightBound && needTransAnim) {
                setTranslationX(this.mTouchX - ((float) oldScrollX));
                animate().translationX(0.0f).start();
            }
            super.scrollTo((int) this.mTouchX, y);
        }
        if (this.mPushGestureEnabled && oldScrollX == this.mScrollX && !this.mGestureTrigged && getTouchState() == 1) {
            this.mScrollStartX += x - oldScrollX;
            int[] bounds = getSnapBound();
            if (((this.mScrollingStateStartX >= bounds[1] && x >= oldScrollX) || (this.mScrollingStateStartX <= bounds[0] && x <= oldScrollX)) && ((float) Math.abs(this.mScrollStartX - x)) / DeviceConfig.getScreenDensity() > 50.0f) {
                onPushGesture(this.mScrollStartX - x);
            }
        }
    }

    protected void skipNextAutoLayoutAnimation() {
        if (DeviceConfig.isLayoutRtl() && getScreenCount() % this.mVisibleRange == 0) {
            for (int i = 0; i < getScreenCount(); i++) {
                if (getScreen(i) instanceof HostView) {
                    ((HostView) getScreen(i)).setSkipNextAutoLayoutAnimation(true);
                }
            }
        }
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int currX = this.mScroller.getCurrX();
            this.mScrollX = currX;
            this.mTouchX = (float) currX;
            this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
            this.mScrollY = this.mScroller.getCurrY();
            postInvalidateOnAnimation();
        } else if (this.mNextScreen != -1) {
            setCurrentScreenInner(Math.max(0, Math.min(this.mNextScreen, getScreenCount() - 1)));
            this.mNextScreen = -1;
        } else if (this.mTouchState == 1) {
            float now = ((float) System.nanoTime()) / 1.0E9f;
            float dx = this.mTouchX - ((float) this.mScrollX);
            this.mScrollX = (int) (((float) this.mScrollX) + (dx * ((float) Math.exp((double) ((now - this.mSmoothingTime) / SMOOTHING_CONSTANT)))));
            this.mSmoothingTime = now;
            if (dx > 1.0f || dx < -1.0f) {
                postInvalidate();
            }
        }
        updateIndicatorPositions(this.mScrollX, false);
        updateSlidePointPosition(this.mScrollX);
        updateArrowIndicatorResource(this.mScrollX);
    }

    public boolean setUniformLayoutModeMaxGap(int gap) {
        if (this.mScreenLayoutMode != 3 || this.mUniformLayoutModeMaxGap == gap) {
            return false;
        }
        this.mUniformLayoutModeMaxGap = gap;
        requestLayout();
        return true;
    }

    public int getUniformLayoutModeCurrentGap() {
        return this.mUniformLayoutModeCurrentGap;
    }

    int getVisualPosition(int index) {
        int i = 1;
        if (this.mScreenLayoutMode == 5) {
            int maxVisualScreenIndex = getVisualIndexInGroupMode(getScreenCount() - 1);
            int visualIndex = getVisualIndexInGroupMode(index);
            if (DeviceConfig.isLayoutRtl()) {
                visualIndex = maxVisualScreenIndex - visualIndex;
            }
            return visualIndex;
        } else if (this.mScreenLayoutMode != 1 && this.mScreenLayoutMode != 6) {
            return DeviceConfig.isLayoutRtl() ? (getScreenCount() - index) - 1 : index;
        } else {
            if (!DeviceConfig.isLayoutRtl()) {
                return index;
            }
            if (getScreenCount() <= this.mVisibleRange) {
                return (getScreenCount() - index) - 1;
            }
            int screenCount = getScreenCount() / this.mVisibleRange;
            if (getScreenCount() % this.mVisibleRange == 0) {
                i = 0;
            }
            return ((this.mVisibleRange * (screenCount + i)) - 1) - index;
        }
    }

    protected int getScreenLayoutX(int index) {
        if (this.mScreenCounter <= 0) {
            return 0;
        }
        int visualIndex = getVisualPosition(index);
        int layoutX = Integer.MIN_VALUE;
        switch (this.mScreenLayoutMode) {
            case 1:
                if (this.mScreenCounter > this.mVisibleRange) {
                    layoutX = (((getMeasuredWidth() * (visualIndex / this.mVisibleRange)) + this.mPaddingLeft) + ((this.mScreenContentWidth - (this.mChildScreenMeasureWidth * this.mVisibleRange)) / 2)) + ((visualIndex % this.mVisibleRange) * this.mChildScreenMeasureWidth);
                    break;
                }
                layoutX = ((this.mScreenContentWidth - (this.mScreenCounter * this.mChildScreenMeasureWidth)) / 2) + (this.mChildScreenMeasureWidth * visualIndex);
                break;
            case 2:
                View child = getScreen(index);
                int headerFooterWidth = this.mScreenContentWidth / this.mVisibleRange;
                if (child != this.mPreviewModeHeader) {
                    if (child != this.mPreviewModeFooter) {
                        int headerWidth;
                        int footerWidth;
                        int previewChildWidth = (int) (((float) this.mChildScreenMeasureWidth) * 0.2f);
                        int previewCnt = (getScreenCount() - (this.mPreviewModeHeader == null ? 0 : 1)) - (this.mPreviewModeFooter == null ? 0 : 1);
                        if (this.mPreviewModeHeader == null) {
                            headerWidth = 0;
                        } else {
                            headerWidth = headerFooterWidth;
                        }
                        if (this.mPreviewModeFooter == null) {
                            footerWidth = 0;
                        } else {
                            footerWidth = headerFooterWidth;
                        }
                        int previewWidth = (this.mScreenContentWidth - headerWidth) - footerWidth;
                        previewChildWidth = (int) Math.min((((float) previewWidth) - ((float) this.mChildScreenMeasureWidth)) / ((float) (previewCnt - 1)), (float) previewChildWidth);
                        int i = this.mPaddingLeft + this.mScrollX;
                        int measuredWidth = (!DeviceConfig.isLayoutRtl() || this.mPreviewModeFooter == null) ? 0 : (child.getMeasuredWidth() + headerFooterWidth) / 2;
                        layoutX = ((((previewWidth - ((previewCnt - 1) * previewChildWidth)) - this.mChildScreenMeasureWidth) / 2) + ((measuredWidth + i) + headerWidth)) + ((visualIndex - (this.mPreviewModeHeader == null ? 0 : 1)) * previewChildWidth);
                        break;
                    }
                    layoutX = (((this.mScrollX + this.mScreenContentWidth) + this.mPaddingLeft) - this.mPaddingRight) - ((child.getMeasuredWidth() + headerFooterWidth) / 2);
                    if (isLayoutRequested() && DeviceConfig.isLayoutRtl()) {
                        layoutX = (this.mScrollX + this.mPaddingLeft) + (child.getMeasuredWidth() / 2);
                        break;
                    }
                }
                layoutX = this.mScrollX + this.mPaddingLeft;
                break;
                break;
            case 3:
                this.mUniformLayoutModeCurrentGap = (this.mScreenContentWidth / this.mScreenCounter) - this.mChildScreenMeasureWidth;
                if (this.mScreenCounter > 1) {
                    this.mUniformLayoutModeCurrentGap = Math.min(this.mUniformLayoutModeCurrentGap, this.mUniformLayoutModeMaxGap);
                }
                layoutX = ((int) ((((float) this.mPaddingLeft) + ((((float) visualIndex) + 0.5f) * ((float) this.mUniformLayoutModeCurrentGap))) + ((float) (this.mChildScreenMeasureWidth * visualIndex)))) + ((this.mScreenContentWidth - ((this.mChildScreenMeasureWidth + this.mUniformLayoutModeCurrentGap) * this.mScreenCounter)) / 2);
                break;
            case 4:
                layoutX = (((getMeasuredWidth() * (visualIndex / this.mVisibleRange)) + this.mPaddingLeft) + ((int) (1.5f * ((float) this.mColumnGap)))) + ((this.mChildScreenMeasureWidth + this.mColumnGap) * ((visualIndex % this.mVisibleRange) % this.mColumnCountPerScreen));
                break;
            case 5:
                int columnIndex = visualIndex % this.mVisibleRange;
                if (this.mLayoutScreensSeamless) {
                    layoutX = visualIndex * this.mChildScreenMeasureWidth;
                } else {
                    layoutX = getWidth() * (visualIndex / this.mVisibleRange);
                }
                int unfoldingGroupsSize = this.mIsGroupUnfolding ? (this.mGroupEndIndex - this.mGroupStartIndex) + 1 : 0;
                if (this.mIsGroupUnfolding || getGroupModeVisualScreenSize() >= this.mVisibleRange) {
                    if (unfoldingGroupsSize <= this.mVisibleRange && index >= this.mGroupStartIndex && index <= this.mGroupEndIndex) {
                        if (!this.mLayoutScreensSeamless) {
                            layoutX += (this.mPaddingLeft + ((this.mScreenContentWidth - (this.mChildScreenMeasureWidth * unfoldingGroupsSize)) / 2)) + ((index - this.mGroupStartIndex) * this.mChildScreenMeasureWidth);
                            break;
                        }
                        layoutX += (this.mScreenContentWidth - (this.mChildScreenMeasureWidth * unfoldingGroupsSize)) / 2;
                        break;
                    } else if (this.mLayoutScreensSeamless) {
                        if (this.mIsGroupUnfolding && !DeviceConfig.isLayoutRtl() && index > this.mGroupEndIndex) {
                            layoutX += this.mScreenContentWidth % this.mChildScreenMeasureWidth;
                            break;
                        }
                    } else {
                        layoutX += ((columnIndex >= 0 ? 1 : -1) * (this.mPaddingLeft + ((this.mScreenContentWidth - (this.mVisibleRange * this.mChildScreenMeasureWidth)) / 2))) + (this.mChildScreenMeasureWidth * columnIndex);
                        break;
                    }
                }
                layoutX = (this.mPaddingLeft + ((this.mScreenContentWidth - (this.mChildScreenMeasureWidth * getGroupModeVisualScreenSize())) / 2)) + (this.mChildScreenMeasureWidth * columnIndex);
                break;
                break;
            case 6:
                layoutX = ((this.mPaddingLeft + (((this.mScreenContentWidth - (this.mVisibleRange * this.mChildScreenMeasureWidth)) - ((this.mVisibleRange - 1) * this.mFixedGap)) / 2)) + ((visualIndex % this.mVisibleRange) * (this.mChildScreenMeasureWidth + this.mFixedGap))) + ((visualIndex / this.mVisibleRange) * this.mScreenContentWidth);
                break;
        }
        if (layoutX == Integer.MIN_VALUE) {
            layoutX = this.mPaddingLeft + (this.mChildScreenMeasureWidth * visualIndex);
            if (!this.mLayoutScreensSeamless) {
                layoutX += ((this.mPaddingLeft + this.mPaddingRight) * visualIndex) / this.mVisibleRange;
            }
        }
        return this.mChildScreenLayoutMeasureDiffX + layoutX;
    }

    protected int getScreenLayoutY(int index) {
        int top = this.mPaddingTop;
        if (this.mIgnoreCenterY) {
            return top;
        }
        switch (this.mScreenLayoutMode) {
            case 1:
            case 2:
            case 3:
            case 5:
                top += (((getHeight() - this.mChildScreenMeasureHeight) - this.mPaddingTop) - this.mPaddingBottom) / 2;
                break;
            case 4:
                top += ((int) (1.5f * ((float) this.mRowGap))) + ((this.mChildScreenMeasureHeight + this.mRowGap) * ((index % this.mVisibleRange) / this.mColumnCountPerScreen));
                break;
        }
        return top;
    }

    public int getChildScreenMeasureWidth() {
        return this.mChildScreenMeasureWidth;
    }

    public int getChildScreenMeasureHeight() {
        return this.mChildScreenMeasureHeight;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        computeScroll();
        showSlideBar();
    }

    public void setVisibility(int visibility) {
        if (visibility == 0) {
            showSlideBar();
        }
        super.setVisibility(visibility);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int maxHeight = 0;
        int maxWidth = 0;
        int count = getScreenCount();
        for (i = 0; i < this.mIndicatorCount; i++) {
            View child = getChildAt(i + count);
            ViewGroup.LayoutParams lp = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(heightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
            maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
            maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
        }
        int maxChildHeight = 0;
        int maxChildWidth = 0;
        for (i = 0; i < count; i++) {
            child = getChildAt(i);
            lp = child.getLayoutParams();
            child.measure(getChildMeasureSpec(widthMeasureSpec, this.mPaddingLeft + this.mPaddingRight, lp.width), getChildMeasureSpec(heightMeasureSpec, this.mPaddingTop + this.mPaddingBottom, lp.height));
            maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
            maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
        }
        maxWidth = Math.max(maxChildWidth, maxWidth);
        setMeasuredDimension(resolveSize(maxWidth + (this.mPaddingLeft + this.mPaddingRight), widthMeasureSpec), resolveSize(Math.max(maxChildHeight, maxHeight) + (this.mPaddingTop + this.mPaddingBottom), heightMeasureSpec));
        if (count > 0) {
            this.mChildScreenMeasureWidth = maxChildWidth;
            this.mChildScreenMeasureHeight = maxChildHeight;
            this.mChildScreenLayoutMeasureDiffX = 0;
            this.mScreenContentWidth = (MeasureSpec.getSize(widthMeasureSpec) - this.mPaddingLeft) - this.mPaddingRight;
            this.mScreenContentHeight = (MeasureSpec.getSize(heightMeasureSpec) - this.mPaddingTop) - this.mPaddingBottom;
            this.mLastVisibleRange = this.mVisibleRange;
            if (this.mChildScreenMeasureWidth > 0) {
                if (this.mScreenLayoutMode == 4) {
                    this.mRowCountPerScreen = this.mScreenContentHeight / this.mChildScreenMeasureHeight;
                    this.mColumnCountPerScreen = this.mScreenContentWidth / this.mChildScreenMeasureWidth;
                    this.mVisibleRange = this.mRowCountPerScreen * this.mColumnCountPerScreen;
                    this.mRowGap = (this.mScreenContentHeight - (this.mRowCountPerScreen * this.mChildScreenMeasureHeight)) / (this.mRowCountPerScreen + 2);
                    this.mColumnGap = (this.mScreenContentWidth - (this.mColumnCountPerScreen * this.mChildScreenMeasureWidth)) / (this.mColumnCountPerScreen + 2);
                } else if (this.mScreenLayoutMode == 6) {
                    this.mVisibleRange = Math.max(1, (this.mScreenContentWidth + this.mFixedGap) / (this.mChildScreenMeasureWidth + this.mFixedGap));
                } else {
                    this.mVisibleRange = Math.max(1, this.mScreenContentWidth / this.mChildScreenMeasureWidth);
                    if (this.mScreenLayoutMode == 1 && this.mScreenCounter > this.mVisibleRange) {
                        this.mChildScreenMeasureWidth = this.mScreenContentWidth / this.mVisibleRange;
                        this.mChildScreenLayoutMeasureDiffX = (this.mChildScreenMeasureWidth - maxChildWidth) / 2;
                    }
                }
            }
            setOverScrollRatio(this.mOverScrollRatio);
            updateScreenOffset();
        }
    }

    private void correctCurrentScreen(boolean forceCorrect) {
        int currentScreen = this.mCurrentScreen == -1 ? getDefaultScreenIndex() : this.mCurrentScreen;
        if (this.mScrollWholeScreen) {
            currentScreen = calibrateCurrentScreenIndex(currentScreen);
        }
        if (currentScreen != this.mCurrentScreen || forceCorrect) {
            setCurrentScreen(currentScreen);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        setFrame(left, top, right, bottom);
        updateIndicatorPositions(this.mScrollX, true);
        int count = getScreenCount();
        if (count > 0) {
            correctCurrentScreen(refreshScrollBound());
            int i = 0;
            while (i < count) {
                View child = getChildAt(i);
                if (child == null) {
                    return;
                }
                if (child.getVisibility() != 8) {
                    int layoutTop = getScreenLayoutY(i);
                    int layoutLeft = getScreenLayoutX(i);
                    if (this.mScreenLayoutMode == 5 && this.mGroups.containsKey(child) && ((ArrayList) this.mGroups.get(child)).size() > 0) {
                        ArrayList<View> groupMembers = (ArrayList) this.mGroups.get(child);
                        int offsetX = (int) ((0.2f * ((float) this.mChildScreenMeasureWidth)) / ((float) (groupMembers.size() > 4 ? 4 : groupMembers.size())));
                        if (offsetX > GROUP_MODE_DEFAULT_OFFSET_X) {
                            offsetX = GROUP_MODE_DEFAULT_OFFSET_X;
                        }
                        int pos = 1;
                        Iterator i$ = groupMembers.iterator();
                        while (i$.hasNext()) {
                            View v = (View) i$.next();
                            if (pos > 4) {
                                v.layout(layoutLeft, layoutTop, v.getMeasuredWidth() + layoutLeft, v.getMeasuredHeight() + layoutTop);
                            } else {
                                v.layout((pos * offsetX) + layoutLeft, layoutTop, (v.getMeasuredWidth() + layoutLeft) + (pos * offsetX), v.getMeasuredHeight() + layoutTop);
                            }
                            pos++;
                        }
                        i += groupMembers.size();
                    }
                    child.layout(layoutLeft, layoutTop, child.getMeasuredWidth() + layoutLeft, child.getMeasuredHeight() + layoutTop);
                    i++;
                } else {
                    throw new RuntimeException("child screen can't set visible as GONE.");
                }
            }
        }
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        updateChildStaticTransformation(child);
        return super.drawChild(canvas, child, drawingTime);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
        int screen = indexOfChild(child);
        if (screen >= getScreenCount()) {
            return super.requestChildRectangleOnScreen(child, rectangle, immediate);
        }
        if (screen == this.mCurrentScreen && this.mScroller.isFinished()) {
            return false;
        }
        snapToScreen(screen);
        return true;
    }

    public boolean dispatchUnhandledMove(View focused, int direction) {
        if (direction == 17) {
            if (this.mCurrentScreen > 0) {
                snapToScreen(this.mCurrentScreen - 1);
                return true;
            }
        } else if (direction == 66 && this.mCurrentScreen < getScreenCount() - 1) {
            snapToScreen(this.mCurrentScreen + 1);
            return true;
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    protected int getTouchState() {
        return this.mTouchState;
    }

    protected void setTouchState(MotionEvent ev, int touchState) {
        if (this.mTouchState != touchState && touchState == 1) {
            this.mScrollingStateStartX = this.mScrollX;
        }
        this.mTouchState = touchState;
        getParent().requestDisallowInterceptTouchEvent(this.mTouchState != 0);
        if (this.mTouchState == 0) {
            this.mActivePointerId = -1;
            this.mAllowLongPress = false;
            this.mGestureVelocityTracker.recycle();
        } else {
            if (ev != null) {
                this.mActivePointerId = ev.getPointerId(0);
            }
            if (this.mAllowLongPress) {
                this.mAllowLongPress = false;
                View currentScreen = getChildAt(this.mCurrentScreen);
                if (currentScreen != null) {
                    currentScreen.cancelLongPress();
                }
            }
            if (this.mTouchState == 1) {
                this.mLastMotionX = ev.getX(ev.findPointerIndex(this.mActivePointerId));
                this.mTouchX = (float) this.mScrollX;
                this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
            }
        }
        showSlideBar();
        if (this.mPushGestureEnabled && touchState == 1) {
            this.mGestureTrigged = false;
            this.mScrollStartX = this.mScrollX;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & 255) {
            case 0:
                ev.setAction(3);
                this.mScaleDetector.onTouchEvent(ev);
                ev.setAction(0);
                this.mGestureVelocityTracker.recycle();
                this.mCurrentGestureFinished = false;
                this.mTouchIntercepted = false;
                this.mLastMotionX = ev.getX();
                this.mLastMotionY = ev.getY();
                if (!this.mScroller.isFinished()) {
                    this.mScroller.abortAnimation();
                    setTouchState(ev, 1);
                    break;
                }
                this.mAllowLongPress = true;
                break;
            case 1:
            case 3:
                setTouchState(ev, 0);
                break;
            case 2:
                onTouchEventUnique(ev);
                break;
        }
        if (2 != (ev.getAction() & 255)) {
            onTouchEventUnique(ev);
        }
        if (this.mCurrentGestureFinished || (this.mTouchState != 0 && this.mTouchState != 3)) {
            return true;
        }
        return false;
    }

    protected boolean scrolledFarEnough(MotionEvent ev) {
        float dx = Math.abs(ev.getX(0) - this.mLastMotionX);
        if (dx <= this.mConfirmHorizontalScrollRatio * Math.abs(ev.getY(0) - this.mLastMotionY) || dx <= ((float) (this.mTouchSlop * ev.getPointerCount()))) {
            return false;
        }
        return true;
    }

    private void onTouchEventUnique(MotionEvent ev) {
        this.mGestureVelocityTracker.addMovement(ev);
        if (this.mTouchState == 0 || 4 == this.mTouchState) {
            this.mScaleDetector.onTouchEvent(ev);
        }
        if (ev.getAction() != 2 || this.mTouchState != 0) {
            return;
        }
        if (scrolledFarEnough(ev)) {
            setTouchState(ev, 1);
            return;
        }
        int vf = this.mGestureVelocityTracker.getVerticalGesture();
        if (vf != 0 && onVerticalGesture(vf, ev)) {
            setTouchState(ev, 5);
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int newPointerIndex = 0;
        if (!this.mCurrentGestureFinished) {
            if (this.mTouchIntercepted) {
                onTouchEventUnique(ev);
            }
            int pointerIndex;
            switch (ev.getAction() & 255) {
                case 1:
                    if (this.mTouchState == 1) {
                        snapByVelocity(this.mActivePointerId);
                    }
                    setTouchState(ev, 0);
                    break;
                case 2:
                    if (this.mTouchState == 0 && scrolledFarEnough(ev)) {
                        setTouchState(ev, 1);
                    }
                    if (this.mTouchState == 1) {
                        pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex == -1) {
                            setTouchState(ev, 1);
                            pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        }
                        float x = ev.getX(pointerIndex);
                        float deltaX = this.mLastMotionX - x;
                        this.mLastMotionX = x;
                        if (deltaX == 0.0f) {
                            awakenScrollBars();
                            break;
                        }
                        scrollTo(Math.round(this.mTouchX + deltaX), 0);
                        break;
                    }
                    break;
                case 3:
                    if (this.mTouchState == 1) {
                        this.mScroller.abortAnimation();
                        snapByVelocity((int) this.mGestureVelocityTracker.getXVelocity(1000, this.mMaximumVelocity, this.mActivePointerId), 3);
                    }
                    setTouchState(ev, 0);
                    break;
                case 6:
                    pointerIndex = (ev.getAction() & 65280) >> 8;
                    if (ev.getPointerId(pointerIndex) == this.mActivePointerId) {
                        if (pointerIndex == 0) {
                            newPointerIndex = 1;
                        }
                        this.mLastMotionX = ev.getX(newPointerIndex);
                        this.mActivePointerId = ev.getPointerId(newPointerIndex);
                        this.mGestureVelocityTracker.init(this.mActivePointerId);
                        break;
                    }
                    break;
            }
            this.mTouchIntercepted = true;
        }
        return true;
    }

    protected boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        if (this.mScreenLayoutMode == 5 && this.mEnableReverseDrawingMode) {
            for (ArrayList<View> members : this.mGroups.values()) {
                if (members.contains(child)) {
                    return false;
                }
            }
        }
        return super.isTransformedTouchPointInView(x, y, child, outLocalPoint);
    }

    public void onSecondaryPointerDown(MotionEvent ev, int pointerId) {
        this.mLastMotionX = ev.getX(ev.findPointerIndex(pointerId));
        this.mTouchX = (float) this.mScrollX;
        this.mSmoothingTime = ((float) System.nanoTime()) / 1.0E9f;
        this.mGestureVelocityTracker.init(pointerId);
        this.mGestureVelocityTracker.addMovement(ev);
        this.mTouchState = 1;
    }

    public void onSecondaryPointerUp(MotionEvent ev, int pointerId) {
        snapByVelocity(pointerId);
        this.mGestureVelocityTracker.recycle();
        this.mTouchState = 0;
    }

    public void onSecondaryPointerMove(MotionEvent ev, int pointerId) {
        float x = ev.getX(ev.findPointerIndex(pointerId));
        float deltaX = this.mLastMotionX - x;
        this.mLastMotionX = x;
        if (deltaX != 0.0f) {
            scrollTo((int) (this.mTouchX + deltaX), 0);
        } else {
            awakenScrollBars();
        }
        this.mGestureVelocityTracker.addMovement(ev);
    }

    private void snapByVelocity(int pointerId) {
        if (this.mChildScreenMeasureWidth > 0 && getCurrentScreen() != null && isScrollable()) {
            int velocityX = (int) this.mGestureVelocityTracker.getXVelocity(1000, this.mMaximumVelocity, pointerId);
            snapByVelocity(velocityX, this.mGestureVelocityTracker.getXFlingDirection((float) Math.abs(velocityX)));
        }
    }

    protected void snapByVelocity(int velocity, int flingDirection) {
        int snapGap;
        int i = -1;
        if (this.mScrollWholeScreen) {
            snapGap = this.mVisibleRange;
        } else {
            snapGap = 1;
        }
        int i2;
        if (flingDirection == 1) {
            i2 = this.mCurrentScreen;
            if (DeviceConfig.isLayoutRtl()) {
                i = 1;
            }
            snapToScreen(getSnapToScreenIndex(i2, snapGap, i), velocity, true);
        } else if (flingDirection == 2) {
            i2 = this.mCurrentScreen;
            if (!DeviceConfig.isLayoutRtl()) {
                i = 1;
            }
            snapToScreen(getSnapToScreenIndex(i2, snapGap, i), velocity, true);
        } else if (flingDirection == 3) {
            snapToScreen(this.mCurrentScreen, velocity, true);
        } else {
            int whichScreen = getSnapUnitIndex(snapGap);
            if (this.mScreenLayoutMode == 5) {
                whichScreen = getScreenActualIndexInGroupMode(whichScreen);
            }
            snapToScreen(whichScreen, 0, true);
        }
    }

    protected int getSnapUnitIndex(int snapGap) {
        return (this.mScrollX + ((this.mChildScreenMeasureWidth * snapGap) >> 1)) / this.mChildScreenMeasureWidth;
    }

    protected void finishCurrentGesture() {
        this.mCurrentGestureFinished = true;
        setTouchState(null, 0);
    }

    public int snapToScreen(int whichScreen) {
        return snapToScreen(whichScreen, 0, false);
    }

    protected int snapToScreen(int whichScreen, int velocity, boolean settle) {
        if (this.mScreenContentWidth <= 0) {
            return 0;
        }
        int screenDelta;
        if (this.mIsGroupUnfolding) {
            whichScreen = Math.min(Math.max(whichScreen, this.mGroupStartIndex), this.mGroupEndIndex);
        }
        if (this.mScrollWholeScreen) {
            this.mNextScreen = calibrateCurrentScreenIndex(whichScreen);
        } else {
            this.mNextScreen = Math.max(0, Math.min(whichScreen, getScreenCount() - this.mVisibleRange));
        }
        if (this.mScreenLayoutMode != 5) {
            this.mNextScreen = Math.max(this.mScreenScrollLeftBound, Math.min(this.mScreenScrollRightBound, this.mNextScreen));
        }
        if (this.mScreenLayoutMode == 5) {
            screenDelta = Math.max(1, Math.abs(getVisualIndexInGroupMode(this.mNextScreen) - getVisualIndexInGroupMode(this.mCurrentScreen)));
        } else {
            screenDelta = Math.max(1, Math.abs(this.mNextScreen - this.mCurrentScreen));
        }
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
        velocity = Math.abs(velocity);
        if (settle) {
            this.mScrollInterpolator.setDistance(screenDelta, velocity);
        } else {
            this.mScrollInterpolator.disableSettle();
        }
        int[] bounds = getSnapBound();
        if ((this.mScreenLayoutMode == 1 || this.mScreenLayoutMode == 6) && DeviceConfig.isLayoutRtl() && this.mNextScreen % this.mVisibleRange != this.mVisibleRange - 1) {
            this.mNextScreen += (this.mVisibleRange - 1) - (this.mNextScreen % this.mVisibleRange);
        }
        int delta = Math.max(bounds[0], Math.min(bounds[1], getScreenScrollX(this.mNextScreen))) - this.mScrollX;
        if (delta == 0) {
            return 0;
        }
        int duration = Math.min((Math.abs(delta) * this.mScreenSnapDuration) / this.mScreenContentWidth, getScreenSnapMaxDuration());
        if (velocity > 0) {
            duration += (int) ((((float) duration) / (((float) velocity) / 2500.0f)) * 0.4f);
        }
        duration = Math.max(this.mScreenSnapDuration, duration);
        startScroll(this.mScrollX, 0, delta, 0, duration);
        invalidate();
        return duration;
    }

    protected void startScroll(int startX, int startY, int dx, int dy, int duration) {
        this.mScroller.startScroll(startX, startY, dx, dy, duration);
    }

    public final int getScreenCount() {
        return this.mScreenCounter;
    }

    public int getChildIndex(View v) {
        for (int i = 0; i < getScreenCount(); i++) {
            if (getScreen(i) == v) {
                return i;
            }
        }
        return -1;
    }

    public int getCurrentScreenIndex() {
        if (this.mNextScreen != -1) {
            return this.mNextScreen;
        }
        return this.mCurrentScreen;
    }

    public View getCurrentScreen() {
        return getScreen(this.mCurrentScreen);
    }

    protected int getDefaultScreenIndex() {
        return 0;
    }

    public void setCurrentScreen(int screenIndex) {
        if (this.mScrollWholeScreen) {
            screenIndex = calibrateCurrentScreenIndex(screenIndex);
        } else {
            screenIndex = Math.max(0, Math.min(screenIndex, getScreenCount() - this.mVisibleRange));
        }
        setCurrentScreenInner(screenIndex);
        this.mScroller.abortAnimation();
        scrollToScreen(this.mCurrentScreen);
    }

    protected void setCurrentScreenInner(int screenIndex) {
        updateSeekPoints(screenIndex);
        this.mCurrentScreen = screenIndex;
        this.mNextScreen = -1;
    }

    public View getScreen(int screenIndex) {
        if (screenIndex < 0 || screenIndex >= getScreenCount()) {
            return null;
        }
        return getChildAt(screenIndex);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        int currentCount = getScreenCount();
        if (index < 0) {
            index = currentCount;
        } else {
            index = Math.min(index, currentCount);
        }
        if (this.mScreenSeekBar != null) {
            this.mScreenSeekBar.addView(createSeekPoint(), index, SEEK_POINT_LAYOUT_PARAMS);
        }
        this.mScreenCounter++;
        if (this.mScreenLayoutMode == 5) {
            this.mGroupModeVisualScreenSize = -1;
        }
        super.addView(child, index, params);
    }

    public void removeView(View view) {
        if (view instanceof Indicator) {
            throwRemoveIndicatorException();
        }
        super.removeView(view);
    }

    public void removeViewInLayout(View view) {
        if (view instanceof Indicator) {
            throwRemoveIndicatorException();
        }
        super.removeView(view);
    }

    public void removeViewsInLayout(int start, int count) {
        if (start + count >= getScreenCount()) {
            throwRemoveIndicatorException();
        }
        super.removeViewsInLayout(start, count);
    }

    public void removeViewAt(int index) {
        if (index >= getScreenCount()) {
            throwRemoveIndicatorException();
        }
        super.removeViewAt(index);
    }

    public void removeViews(int start, int count) {
        if (start + count >= getScreenCount()) {
            throwRemoveIndicatorException();
        }
        super.removeViews(start, count);
    }

    public void onViewRemoved(View child) {
        if (child instanceof Indicator) {
            this.mIndicatorCount--;
            return;
        }
        this.mScreenCounter--;
        if (this.mScreenLayoutMode == 5) {
            this.mGroupModeVisualScreenSize = -1;
        }
    }

    private void throwRemoveIndicatorException() {
        throw new UnsupportedOperationException("ScreenView doesn't support remove indicator directly.");
    }

    public void addIndicator(View indicator, FrameLayout.LayoutParams params) {
        this.mIndicatorCount++;
        super.addView(indicator, -1, params);
    }

    public void removeIndicator(View indicator) {
        int index = indexOfChild(indicator);
        if (index < getScreenCount()) {
            throw new InvalidParameterException("The view passed through the parameter must be indicator.");
        }
        this.mIndicatorCount--;
        super.removeViewAt(index);
    }

    public void removeScreen(int screenIndex) {
        if (screenIndex >= getScreenCount()) {
            throw new InvalidParameterException("The view specified by the index must be a screen.");
        }
        if (screenIndex == this.mCurrentScreen) {
            if (this.mScrollWholeScreen) {
                if (screenIndex != 0 && screenIndex == getScreenCount() - 1) {
                    snapToScreen(screenIndex - 1);
                }
            } else if (screenIndex == getScreenCount() - 1) {
                setCurrentScreen(Math.max(0, screenIndex - 1));
            }
        }
        if (this.mScreenSeekBar != null) {
            this.mScreenSeekBar.removeViewAt(screenIndex);
        }
        super.removeViewAt(screenIndex);
    }

    public void removeAllScreens() {
        if (getScreenCount() > 0) {
            removeScreensInLayout(0, getScreenCount());
            requestLayout();
            invalidate();
        }
    }

    public View[] removeOutAllScreens() {
        View[] children = new View[getScreenCount()];
        for (int i = 0; i < getScreenCount(); i++) {
            children[i] = getScreen(i);
        }
        removeAllScreens();
        return children;
    }

    public void removeScreensInLayout(int start, int count) {
        if (start >= 0 && start < getScreenCount()) {
            count = Math.min(count, getScreenCount() - start);
            if (this.mScreenSeekBar != null) {
                this.mScreenSeekBar.removeViewsInLayout(start, count);
            }
            super.removeViewsInLayout(start, count);
        }
    }

    public boolean allowLongPress() {
        return this.mAllowLongPress;
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mLongClickListener = l;
        int count = getScreenCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    private ImageView createSeekPoint() {
        ImageView seekPoint = new ImageView(this.mContext);
        seekPoint.setScaleType(ScaleType.CENTER);
        seekPoint.setImageResource(this.mSeekPointResId);
        seekPoint.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScreenView.this.snapToScreen(ScreenView.this.mScreenSeekBar.indexOfChild(v));
            }
        });
        return seekPoint;
    }

    private void updateSeekPoints(int newPos) {
        if (this.mScreenSeekBar != null) {
            int count = getScreenCount();
            int i = 0;
            while (i < count) {
                View v = this.mScreenSeekBar.getChildAt(i);
                if (v != null) {
                    boolean z = i >= newPos && i < this.mVisibleRange + newPos;
                    v.setSelected(z);
                }
                i++;
            }
        }
    }

    public void onResume() {
    }

    public int getScreenTransitionType() {
        return this.mTransitionEffect.getTransitionType();
    }

    public int setScreenTransitionType(int type) {
        int resultType = this.mTransitionEffect.setTransitionType(type);
        setOvershootTension(this.mTransitionEffect.getOverShotTension());
        setScreenSnapDuration(this.mTransitionEffect.getScreenSnapDuration());
        return resultType;
    }

    public void appendScreenTransitionType(int type) {
        this.mTransitionEffect.appendTransitionType(type);
        setOvershootTension(this.mTransitionEffect.getOverShotTension());
        setScreenSnapDuration(this.mTransitionEffect.getScreenSnapDuration());
    }

    public void removeScreenTransitionType(int type) {
        this.mTransitionEffect.removeTransitionType(type);
        if (this.mTransitionEffect.isValidType()) {
            setOvershootTension(this.mTransitionEffect.getOverShotTension());
            setScreenSnapDuration(this.mTransitionEffect.getScreenSnapDuration());
        }
    }

    protected void updateChildStaticTransformation(View child) {
        if (!(child instanceof Indicator)) {
            float childW = (float) child.getMeasuredWidth();
            float halfChildW = childW / 2.0f;
            this.mTransitionEffect.updateTransformation((((((float) this.mScrollX) + (((float) getMeasuredWidth()) / 2.0f)) - ((float) child.getLeft())) - halfChildW) / childW, (float) (this.mScrollX - this.mLastScrollX), this.mLastMotionX, this.mLastMotionY - ((float) getPaddingTop()), child, this);
        }
    }

    public void setEnableReverseDrawingMode(boolean isReverse) {
        setChildrenDrawingOrderEnabled(isReverse);
        this.mEnableReverseDrawingMode = isReverse;
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        if (this.mEnableReverseDrawingMode) {
            return (childCount - i) - 1;
        }
        return i;
    }

    protected int getScrollStartX() {
        return this.mScrollStartX;
    }

    protected void onPushGesture(int direction) {
    }

    protected boolean onVerticalGesture(int direction, MotionEvent event) {
        return false;
    }

    protected void onPinchIn(ScaleGestureDetector detector) {
    }

    protected void onPinchOut(ScaleGestureDetector detector) {
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = this.mCurrentScreen;
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.currentScreen != -1) {
            setCurrentScreen(savedState.currentScreen);
        }
    }
}
