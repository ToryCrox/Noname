package com.miui.home.launcher.upsidescene;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import com.miui.home.R;
import com.miui.home.launcher.DragScroller;
import com.miui.home.launcher.upsidescene.data.Screen;

public class ScrollableScreen extends FrameLayout implements DragScroller {
    private int mActivePointerId = -1;
    private int mCurrentScreen;
    private FreeLayout mFreeLayout;
    private GestureVelocityTracker mGestureVelocityTracker;
    private boolean mIsBeingDragged = false;
    private float mLastMotionX;
    private int mMaximumVelocity;
    private int mNextScreen;
    private SceneScreen mSceneScreen;
    private int mScreenCount = -1;
    private Scroller mScroller;
    private int mTouchSlop;

    private class GestureVelocityTracker {
        private float mFoldX;
        private int mPointerId;
        private float mPrevX;
        private float mStartX;
        private VelocityTracker mVelocityTracker;

        private GestureVelocityTracker() {
            this.mPointerId = -1;
            this.mStartX = -1.0f;
            this.mFoldX = -1.0f;
            this.mPrevX = -1.0f;
        }

        public void recycle() {
            if (this.mVelocityTracker != null) {
                this.mVelocityTracker.recycle();
                this.mVelocityTracker = null;
            }
            reset();
        }

        public void addMovement(MotionEvent ev) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(ev);
            float curX = ev.getX();
            if (this.mPointerId != -1) {
                int pIndex = ev.findPointerIndex(this.mPointerId);
                if (pIndex != -1) {
                    curX = ev.getX(pIndex);
                } else {
                    this.mPointerId = -1;
                }
            }
            if (this.mStartX < 0.0f) {
                this.mStartX = curX;
            } else if (this.mPrevX < 0.0f) {
                this.mPrevX = curX;
            } else {
                if (this.mFoldX < 0.0f) {
                    if (((this.mPrevX > this.mStartX && curX < this.mPrevX) || (this.mPrevX < this.mStartX && curX > this.mPrevX)) && Math.abs(curX - this.mStartX) > 3.0f) {
                        this.mFoldX = this.mPrevX;
                    }
                } else if (this.mFoldX != this.mPrevX && (((this.mPrevX > this.mFoldX && curX < this.mPrevX) || (this.mPrevX < this.mFoldX && curX > this.mPrevX)) && Math.abs(curX - this.mFoldX) > 3.0f)) {
                    this.mStartX = this.mFoldX;
                    this.mFoldX = this.mPrevX;
                }
                this.mPrevX = curX;
            }
        }

        private void reset() {
            this.mPointerId = -1;
            float f = (float) -1;
            this.mStartX = f;
            this.mFoldX = f;
            this.mPrevX = f;
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

        public float getXVelocity(int units, int maxVelocity, int pointerId) {
            this.mVelocityTracker.computeCurrentVelocity(units, (float) maxVelocity);
            return this.mVelocityTracker.getXVelocity(pointerId);
        }

        public int getFlingDirection(float velocity) {
            if (velocity <= 300.0f) {
                return 4;
            }
            if (this.mFoldX < 0.0f) {
                if (this.mPrevX > this.mStartX) {
                    return 1;
                }
                return 2;
            } else if (this.mPrevX < this.mFoldX) {
                if (ScrollableScreen.this.mScrollX < ScrollableScreen.this.calcScreenScrollX(ScrollableScreen.this.getCurrentScreen())) {
                    return 3;
                }
                return 2;
            } else if (this.mPrevX <= this.mFoldX) {
                return 3;
            } else {
                if (ScrollableScreen.this.mScrollX > ScrollableScreen.this.calcScreenScrollX(ScrollableScreen.this.getCurrentScreen())) {
                    return 3;
                }
                return 1;
            }
        }
    }

    private class ScrollableScreenInterpolator implements Interpolator {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((t * t) * t) + 1.0f;
        }
    }

    public ScrollableScreen(Context context) {
        super(context);
    }

    public ScrollableScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollableScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFreeLayout = (FreeLayout) findViewById(R.id.freeLayout);
        this.mScroller = new Scroller(this.mContext, new ScrollableScreenInterpolator());
        ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public void setSceneScreen(SceneScreen sceneScreen) {
        this.mSceneScreen = sceneScreen;
        this.mFreeLayout.setSceneScreen(sceneScreen);
    }

    public void setScreenData(final Screen screenData) {
        this.mFreeLayout.setScreenData(screenData);
        post(new Runnable() {
            public void run() {
                ScrollableScreen.this.setCurrentScreen(screenData.getHome());
            }
        });
    }

    public boolean isBeingDragged() {
        return this.mIsBeingDragged;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z = false;
        int action = ev.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        switch (action & 255) {
            case 0:
                this.mLastMotionX = ev.getX();
                this.mActivePointerId = ev.getPointerId(0);
                initOrResetVelocityTracker();
                this.mGestureVelocityTracker.addMovement(ev);
                this.mIsBeingDragged = !this.mScroller.isFinished();
                break;
            case 1:
            case 3:
                this.mIsBeingDragged = false;
                this.mActivePointerId = -1;
                break;
            case 2:
                checkIsBeginDrag(ev);
                break;
        }
        if (this.mIsBeingDragged || this.mSceneScreen.isCurrentGestureFinished()) {
            z = true;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.mSceneScreen.isCurrentGestureFinished()) {
            initVelocityTrackerIfNotExists();
            this.mGestureVelocityTracker.addMovement(ev);
            switch (ev.getAction() & 255) {
                case 0:
                    if (!this.mScroller.isFinished()) {
                        this.mScroller.abortAnimation();
                        this.mIsBeingDragged = true;
                        post(new Runnable() {
                            public void run() {
                                ScrollableScreen.this.mParent.requestDisallowInterceptTouchEvent(true);
                            }
                        });
                    }
                    this.mLastMotionX = ev.getX();
                    this.mActivePointerId = ev.getPointerId(0);
                    break;
                case 1:
                case 3:
                    if (this.mIsBeingDragged) {
                        handleActionUp();
                        break;
                    }
                    break;
                case 2:
                    if (!this.mIsBeingDragged) {
                        checkIsBeginDrag(ev);
                    }
                    if (this.mActivePointerId != -1) {
                        int pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                        if (pointerIndex != -1) {
                            float x = ev.getX(pointerIndex);
                            int deltaX = (int) (this.mLastMotionX - x);
                            if (this.mIsBeingDragged) {
                                this.mLastMotionX = x;
                                setScrollX(this.mScrollX + deltaX);
                                break;
                            }
                        }
                    }
                    break;
                case 6:
                    if (this.mIsBeingDragged && ev.getActionIndex() == 0) {
                        handleActionUp();
                        break;
                    }
                default:
                    break;
            }
        }
        return true;
    }

    protected void finishCurrentGesture() {
        this.mIsBeingDragged = false;
    }

    private void handleActionUp() {
        snapByVelocity(this.mActivePointerId);
        this.mActivePointerId = -1;
        this.mIsBeingDragged = false;
        recycleVelocityTracker();
    }

    void checkIsBeginDrag(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            int activePointerId = this.mActivePointerId;
            if (activePointerId != -1) {
                int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex != -1) {
                    float x = ev.getX(pointerIndex);
                    if (((int) Math.abs(x - this.mLastMotionX)) > this.mTouchSlop) {
                        this.mIsBeingDragged = true;
                        this.mLastMotionX = x;
                        initVelocityTrackerIfNotExists();
                        this.mGestureVelocityTracker.addMovement(ev);
                        this.mParent.requestDisallowInterceptTouchEvent(true);
                    }
                }
            }
        }
    }

    public void computeScroll() {
        if (this.mScroller.computeScrollOffset()) {
            if (this.mScrollX != this.mScroller.getCurrX()) {
                setScrollX(this.mScroller.getCurrX());
            }
            postInvalidate();
        } else if (this.mNextScreen != -1) {
            setCurrentScreenInner(Math.max(0, Math.min(this.mNextScreen, getScreenCount() - 1)));
        }
    }

    private void setCurrentScreenInner(int screenIndex) {
        if (this.mCurrentScreen != screenIndex) {
            this.mCurrentScreen = screenIndex;
        }
        this.mNextScreen = -1;
    }

    public void notifyGadgets(int state) {
        this.mFreeLayout.notifyGadgets(state);
    }

    private void snapByVelocity(int pointerId) {
        int velocityX = (int) this.mGestureVelocityTracker.getXVelocity(1000, this.mMaximumVelocity, pointerId);
        int flingDirection = this.mGestureVelocityTracker.getFlingDirection((float) Math.abs(velocityX));
        if (flingDirection == 1 && this.mCurrentScreen > 0) {
            snapToScreen(this.mCurrentScreen - 1, velocityX);
        } else if (flingDirection == 2 && this.mCurrentScreen < getScreenCount() - 1) {
            snapToScreen(this.mCurrentScreen + 1, velocityX);
        } else if (flingDirection == 3) {
            snapToScreen(this.mCurrentScreen, velocityX);
        } else {
            int centerX = this.mScrollX + (getScreenWidth() / 2);
            if (this.mSceneScreen.isInEditMode()) {
                centerX = (int) (((float) centerX) + ((((float) getScreenWidth()) - (((float) getScreenWidth()) * this.mSceneScreen.getEditModeScaleFactor())) / 2.0f));
            }
            snapToScreen(centerX / getScreenWidth(), 0);
        }
    }

    public void snapToScreen(int whichScreen) {
        snapToScreen(whichScreen, 0);
    }

    public void snapToScreen(int whichScreen, int velocity) {
        if (whichScreen < 0) {
            this.mNextScreen = 0;
        } else if (whichScreen > getScreenCount() - 1) {
            this.mNextScreen = getScreenCount() - 1;
        } else {
            this.mNextScreen = whichScreen;
        }
        int screenDelta = Math.max(1, Math.abs(this.mNextScreen - this.mCurrentScreen));
        if (!this.mScroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
        velocity = Math.abs(velocity);
        int delta = calcScreenScrollX(this.mNextScreen) - this.mScrollX;
        int duration = (Math.abs(delta) * 300) / getScreenWidth();
        if (velocity > 0) {
            duration += (int) ((((float) duration) / (((float) velocity) / 2500.0f)) * 0.4f);
        }
        duration = Math.max(300, duration);
        if (screenDelta <= 1) {
            duration = Math.min(duration, 600);
        }
        this.mScroller.startScroll(this.mScrollX, 0, delta, 0, duration);
        invalidate();
    }

    private int calcScreenScrollX(int screenIndex) {
        int scrollX = screenIndex * getScreenWidth();
        if (screenIndex == 0) {
            return scrollX;
        }
        if (screenIndex == getScreenCount() - 1) {
            return getScrollRange();
        }
        return scrollX - getOverlapEdge();
    }

    private int getOverlapEdge() {
        return (getWidth() - getScreenWidth()) / 2;
    }

    public int getScreenWidth() {
        return this.mFreeLayout.getScreenData().getWidth() / getScreenCount();
    }

    public int getChildWidth() {
        return this.mFreeLayout.getScreenData().getWidth();
    }

    public int getScreenCount() {
        if (this.mScreenCount == -1) {
            this.mScreenCount = this.mFreeLayout.getScreenData().getWidth() / this.mSceneScreen.getFreeStyle().getWidth();
            if (this.mFreeLayout.getScreenData().getWidth() % this.mSceneScreen.getFreeStyle().getWidth() > 0) {
                this.mScreenCount++;
            }
        }
        return this.mScreenCount;
    }

    public int getCurrentScreen() {
        return this.mCurrentScreen;
    }

    public void setCurrentScreen(int screenIndex) {
        this.mCurrentScreen = screenIndex;
        this.mNextScreen = -1;
        setScrollX(calcScreenScrollX(this.mCurrentScreen));
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    private void initOrResetVelocityTracker() {
        if (this.mGestureVelocityTracker == null) {
            this.mGestureVelocityTracker = new GestureVelocityTracker();
        } else {
            this.mGestureVelocityTracker.recycle();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mGestureVelocityTracker == null) {
            this.mGestureVelocityTracker = new GestureVelocityTracker();
        }
    }

    private void recycleVelocityTracker() {
        if (this.mGestureVelocityTracker != null) {
            this.mGestureVelocityTracker.recycle();
            this.mGestureVelocityTracker = null;
        }
    }

    public void scrollTo(int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x > getScrollRange()) {
            x = getScrollRange();
        }
        super.scrollTo(x, y);
    }

    private int getScrollRange() {
        if (getChildCount() > 0) {
            return Math.max(0, getChildAt(0).getWidth() - getWidth());
        }
        return 0;
    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        this.mSceneScreen.notifyScrollableScreenScrolling();
    }

    public FreeLayout getFreeLayout() {
        return this.mFreeLayout;
    }

    public void scrollDragingLeft() {
        if (this.mScroller.isFinished()) {
            if (this.mCurrentScreen > 0) {
                snapToScreen(this.mCurrentScreen - 1);
            }
        } else if (this.mNextScreen > 0) {
            snapToScreen(this.mNextScreen - 1);
        }
    }

    public void scrollDragingRight() {
        if (this.mScroller.isFinished()) {
            if (this.mCurrentScreen < getScreenCount() - 1) {
                snapToScreen(this.mCurrentScreen + 1);
            }
        } else if (this.mNextScreen < getScreenCount() - 1) {
            snapToScreen(this.mNextScreen + 1);
        }
    }

    public void onSecondaryPointerDown(MotionEvent ev, int pointerId) {
        initVelocityTrackerIfNotExists();
        this.mLastMotionX = ev.getX(ev.findPointerIndex(pointerId));
        this.mGestureVelocityTracker.init(pointerId);
        this.mGestureVelocityTracker.addMovement(ev);
        this.mIsBeingDragged = true;
    }

    public void onSecondaryPointerUp(MotionEvent ev, int pointerId) {
        snapByVelocity(pointerId);
        this.mGestureVelocityTracker.recycle();
        this.mIsBeingDragged = false;
    }

    public void onSecondaryPointerMove(MotionEvent ev, int pointerId) {
        int x = (int) ev.getX(ev.findPointerIndex(pointerId));
        int deltaX = (int) (this.mLastMotionX - ((float) x));
        this.mLastMotionX = (float) x;
        if (deltaX != 0) {
            scrollTo(this.mScrollX + deltaX, 0);
        } else {
            awakenScrollBars();
        }
        this.mGestureVelocityTracker.addMovement(ev);
    }

    public boolean onEnterScrollArea(int x, int y, int direction) {
        return true;
    }

    public boolean onExitScrollArea() {
        return true;
    }
}
