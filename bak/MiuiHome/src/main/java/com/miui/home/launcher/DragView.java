package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.common.Utilities;

public class DragView extends View implements GhostView {
    private View mAnimateTarget = null;
    private boolean mCanAutoAlign = true;
    private boolean mCanceledMode = false;
    private View mContent;
    private TimeInterpolator mCubicEaseOutInterpolator = new DecelerateInterpolator(1.5f);
    private DragObject mDragGroup = null;
    private ItemInfo mDragInfo;
    private int[] mDragVisualizeOffset = new int[]{0, 0};
    private int mDropAnimationDuration = -1;
    private boolean mDropSucceeded = false;
    private DropTargetContainer mDropTargetContainer = null;
    private boolean mFadeoutAnimation = false;
    private boolean mFakeTargetMode = false;
    private boolean mHasDrawn = false;
    private int mMaxDropAnimationDistance;
    private int mMaxDropAnimationDuration;
    private int mMinDropAnimationDuration;
    private boolean mMultiAndNotAuto;
    private int mMyIndex;
    private Runnable mOnAnimationEndCallback = null;
    private Runnable mOnRemoveCallback = null;
    private Bitmap mOutline;
    private ViewGroup mOwner = null;
    private Paint mPaint;
    private View mPrevAnimateTarget = null;
    private int mRegistrationX;
    private int mRegistrationY;
    private float mScaleTarget = Float.NaN;
    private ValueAnimator mTargetAnimator = null;
    private float[] mTargetLoc = null;
    private float[] mTmpPos = new float[]{0.0f, 0.0f};

    public interface DropTargetContainer {
        void performDropFinishAnimation(View view);

        void setDropAnimating(boolean z);
    }

    public DragView(ViewGroup owner, View content, Bitmap outline, ItemInfo dragInfo, int initOffsetX, int initOffsetY, int touchX, int touchY, int registrationX, int registrationY, float sourceScale, float dragScale, int myIndex, boolean multiAndNotAuto) {
        super(owner.getContext());
        this.mOwner = owner;
        this.mOutline = outline;
        this.mDragInfo = dragInfo;
        this.mMyIndex = myIndex;
        this.mMultiAndNotAuto = multiAndNotAuto;
        Resources res = getResources();
        final float alpha = ((float) res.getInteger(R.integer.config_dragViewAlpha)) / 255.0f;
        setScaleX(sourceScale);
        setScaleY(sourceScale);
        if (dragScale == 0.0f) {
            dragScale = res.getFraction(R.fraction.config_dragViewScale, 1, 1);
        }
        final float finalScale = dragScale * sourceScale;
        ValueAnimator initAnimator = new ValueAnimator();
        initAnimator.setDuration((long) res.getInteger(17694720));
        initAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        final float f = sourceScale;
        final boolean z = multiAndNotAuto;
        final int i = touchX;
        final int i2 = initOffsetX;
        final int i3 = touchY;
        final int i4 = initOffsetY;
        initAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                float currentScale = f + ((finalScale - f) * value);
                DragView.this.setScaleX(currentScale);
                DragView.this.setScaleY(currentScale);
                DragView.this.setAlpha(alpha + ((1.0f - alpha) * (1.0f - value)));
                if (z) {
                    DragView.this.setTranslationX(((float) (i - DragView.this.mRegistrationX)) + ((1.0f - value) * ((float) (DragView.this.mRegistrationX - i2))));
                    DragView.this.setTranslationY(((float) (i3 - DragView.this.mRegistrationY)) + ((1.0f - value) * ((float) (DragView.this.mRegistrationY - i4))));
                }
            }
        });
        initAnimator.start();
        this.mMaxDropAnimationDuration = res.getInteger(R.integer.config_dropAnimMaxDuration);
        this.mMinDropAnimationDuration = res.getInteger(R.integer.config_dropAnimMinDuration);
        this.mMaxDropAnimationDistance = res.getInteger(R.integer.config_dropAnimMaxDist);
        this.mContent = content;
        this.mRegistrationX = registrationX;
        this.mRegistrationY = registrationY;
        if (content instanceof VisualizeCalibration) {
            ((VisualizeCalibration) content).getVisionOffset(this.mDragVisualizeOffset);
        } else {
            this.mDragVisualizeOffset[0] = 0;
            this.mDragVisualizeOffset[1] = 0;
        }
        setLayerType("cancro".equals(Build.DEVICE) ? 1 : 2, null);
    }

    public void setDragGroup(DragObject d) {
        this.mDragGroup = d;
    }

    public View getContent() {
        return this.mContent;
    }

    public Bitmap getOutline() {
        return this.mOutline;
    }

    public ItemInfo getDragInfo() {
        return this.mDragInfo;
    }

    public int getRegistrationX() {
        return this.mRegistrationX;
    }

    public int getRegistrationY() {
        return this.mRegistrationY;
    }

    public void setScaleTarget(float scale) {
        if (!Float.isInfinite(scale)) {
            this.mScaleTarget = scale;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mContent.getMeasuredWidth(), this.mContent.getMeasuredHeight());
    }

    protected void onDraw(Canvas canvas) {
        this.mHasDrawn = true;
        if (this.mMultiAndNotAuto && (this.mContent instanceof ItemIcon)) {
            ItemIcon itemIcon = this.mContent;
            boolean isHideTitle = itemIcon.getIsHideTitle();
            boolean isHideShadow = itemIcon.getIsHideShadow();
            itemIcon.setIsHideTitle(true);
            itemIcon.setIsHideShadow(true);
            this.mContent.draw(canvas);
            itemIcon.setIsHideTitle(isHideTitle);
            itemIcon.setIsHideShadow(isHideShadow);
            return;
        }
        this.mContent.draw(canvas);
    }

    public boolean hasDrawn() {
        return this.mHasDrawn;
    }

    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        if (this.mPaint == null) {
            this.mPaint = new Paint();
        }
        this.mPaint.setAlpha((int) (255.0f * alpha));
        invalidate();
    }

    public void show(int touchX, int touchY) {
        this.mOwner.addView(this, new LayoutParams(-1, -1, 51));
        move(touchX, touchY);
    }

    void move(final int touchX, final int touchY) {
        if (this.mMyIndex == 0 || (getTranslationX() == 0.0f && getTranslationY() == 0.0f)) {
            setTranslationX((float) (touchX - this.mRegistrationX));
            setTranslationY((float) (touchY - this.mRegistrationY));
            return;
        }
        postDelayed(new Runnable() {
            public void run() {
                if (DragView.this.mCanAutoAlign) {
                    DragView.this.setTranslationX((float) (touchX - DragView.this.mRegistrationX));
                    DragView.this.setTranslationY((float) (touchY - DragView.this.mRegistrationY));
                }
            }
        }, (long) (this.mMyIndex * 15));
    }

    void setOnRemoveCallback(Runnable callback) {
        this.mOnRemoveCallback = callback;
    }

    void setDropSucceed() {
        this.mDropSucceeded = true;
    }

    boolean isDropSucceeded() {
        return this.mDropSucceeded;
    }

    public boolean setAnimateTarget(View target) {
        if (this.mAnimateTarget == target) {
            return false;
        }
        this.mPrevAnimateTarget = this.mAnimateTarget;
        this.mAnimateTarget = target;
        if (this.mAnimateTarget instanceof HostView) {
            ((HostView) this.mAnimateTarget).setGhostView(this);
            ((HostView) this.mAnimateTarget).setSkipNextAutoLayoutAnimation(true);
        }
        if (!(this.mAnimateTarget == null || this.mPrevAnimateTarget == this.mAnimateTarget)) {
            if (this.mPrevAnimateTarget instanceof HostView) {
                ((HostView) this.mPrevAnimateTarget).setGhostView(null);
            }
            if (this.mPrevAnimateTarget != null) {
                this.mPrevAnimateTarget.setVisibility(0);
            }
        }
        return true;
    }

    public void setFakeTargetMode() {
        this.mFakeTargetMode = true;
    }

    public void setFadeoutAnimationMode() {
        this.mFadeoutAnimation = true;
    }

    public void setCanceledMode() {
        this.mCanceledMode = true;
    }

    public boolean isCanceledMode() {
        return this.mCanceledMode;
    }

    void remove() {
        this.mOwner.removeView(this);
        this.mOwner = null;
        if (this.mAnimateTarget instanceof HostView) {
            ((HostView) this.mAnimateTarget).setGhostView(null);
        }
        if (this.mAnimateTarget != null) {
            this.mAnimateTarget.setVisibility(0);
        }
        if (this.mOnRemoveCallback != null) {
            this.mOnRemoveCallback.run();
            this.mOnRemoveCallback = null;
        }
    }

    private void onDropAnimationFinished() {
        this.mContent.setVisibility(0);
        if (this.mOnAnimationEndCallback != null) {
            this.mOnAnimationEndCallback.run();
        }
        if (!(this.mDropTargetContainer == null || this.mCanceledMode)) {
            this.mDropTargetContainer.performDropFinishAnimation(this.mAnimateTarget);
            this.mDropTargetContainer.setDropAnimating(false);
        }
        this.mDragGroup.onDropAnimationFinished(this);
    }

    private void animateToTargetInner(boolean changeLocation) {
        DropTargetContainer animTargetContainer = null;
        if (this.mAnimateTarget != null && (this.mAnimateTarget.getParent() instanceof DropTargetContainer)) {
            animTargetContainer = (DropTargetContainer) this.mAnimateTarget.getParent();
        }
        if (animTargetContainer != null) {
            if (this.mDropTargetContainer == null) {
                animTargetContainer.setDropAnimating(true);
            } else if (this.mDropTargetContainer != animTargetContainer) {
                this.mDropTargetContainer.setDropAnimating(false);
                this.mDropTargetContainer = animTargetContainer;
            }
        }
        this.mDropTargetContainer = animTargetContainer;
        if (!(this.mAnimateTarget == null || this.mFakeTargetMode)) {
            this.mAnimateTarget.setVisibility(4);
        }
        if (changeLocation) {
            calcAndStartAnimate(true);
        }
    }

    private void calcAndStartAnimate(boolean needDelay) {
        float[] loc = this.mTmpPos;
        final float initialScale = getScaleX();
        float scale = 1.0f;
        if (this.mTargetLoc != null) {
            loc = this.mTargetLoc;
        } else {
            scale = Utilities.getDescendantCoordRelativeToAncestor(this.mAnimateTarget, this.mOwner, loc, !Launcher.isEditingModeExiting(), true);
        }
        final float finalScale = Float.isNaN(this.mScaleTarget) ? scale : this.mScaleTarget;
        final float initialAlpha = getAlpha();
        final float finalAlpha = this.mFadeoutAnimation ? 0.0f : 1.0f;
        loc[0] = loc[0] - (((float) this.mDragVisualizeOffset[0]) * finalScale);
        loc[1] = loc[1] - (((float) this.mDragVisualizeOffset[1]) * finalScale);
        if (this.mAnimateTarget instanceof VisualizeCalibration) {
            int[] iArr = new int[2];
            iArr = new int[]{0, 0};
            ((VisualizeCalibration) this.mAnimateTarget).getVisionOffset(iArr);
            loc[0] = loc[0] + (((float) iArr[0]) * finalScale);
            loc[1] = loc[1] + (((float) iArr[1]) * finalScale);
        }
        if (this.mFadeoutAnimation && this.mAnimateTarget != null) {
            if (this.mAnimateTarget.getWidth() > getWidth()) {
                loc[0] = loc[0] + ((float) ((this.mAnimateTarget.getWidth() - getWidth()) / 2));
            }
            if (this.mAnimateTarget.getHeight() > getHeight()) {
                loc[1] = loc[1] + ((float) ((this.mAnimateTarget.getHeight() - getHeight()) / 2));
            }
        }
        final float fromX = getTranslationX();
        final float fromY = getTranslationY();
        final float toX = loc[0];
        final float toY = loc[1];
        float dist = (float) Math.sqrt(Math.pow((double) (toX - fromX), 2.0d) + Math.pow((double) (toY - fromY), 2.0d));
        int calcedDuration = this.mDropAnimationDuration;
        if (calcedDuration == -1) {
            calcedDuration = this.mMaxDropAnimationDuration;
            if (dist < ((float) this.mMaxDropAnimationDistance)) {
                calcedDuration = (int) (((float) calcedDuration) * this.mCubicEaseOutInterpolator.getInterpolation(dist / ((float) this.mMaxDropAnimationDistance)));
            }
            calcedDuration = Math.max(calcedDuration, this.mMinDropAnimationDuration);
        }
        if (this.mTargetAnimator != null) {
            ValueAnimator oldAnim = this.mTargetAnimator;
            this.mTargetAnimator = null;
            oldAnim.cancel();
        }
        int delay = needDelay ? this.mMyIndex * 50 : 0;
        int finalDuration = calcedDuration;
        this.mTargetAnimator = new ValueAnimator();
        this.mTargetAnimator.setInterpolator(this.mCubicEaseOutInterpolator);
        this.mTargetAnimator.setDuration((long) finalDuration);
        this.mTargetAnimator.setStartDelay((long) delay);
        this.mTargetAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mTargetAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = ((Float) animation.getAnimatedValue()).floatValue();
                DragView.this.setTranslationX(fromX + ((toX - fromX) * percent));
                DragView.this.setTranslationY(fromY + ((toY - fromY) * percent));
                float scale = ((1.0f - percent) * (initialScale - finalScale)) + finalScale;
                DragView.this.setScaleX(scale);
                DragView.this.setScaleY(scale);
                DragView.this.setAlpha((finalAlpha * percent) + ((1.0f - percent) * initialAlpha));
            }
        });
        this.mTargetAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                if (!(fromX == DragView.this.getTranslationX() && fromY == DragView.this.getTranslationY())) {
                    DragView.this.calcAndStartAnimate(false);
                }
                DragView.this.mCanAutoAlign = false;
            }

            public void onAnimationEnd(Animator animation) {
                if (DragView.this.mTargetAnimator == animation) {
                    DragView.this.onDropAnimationFinished();
                }
            }

            public void onAnimationCancel(Animator animation) {
                DragView.this.mCanAutoAlign = true;
            }
        });
        this.mTargetAnimator.start();
    }

    public boolean isTargetAnimating() {
        return this.mTargetAnimator != null && this.mTargetAnimator.isStarted();
    }

    public void setOnAnimationEndCallback(Runnable endCallback) {
        this.mOnAnimationEndCallback = endCallback;
    }

    public boolean animateToTarget() {
        if (isTargetAnimating()) {
            return false;
        }
        this.mDragGroup.onDropAnimationStart(this);
        if (this.mCanceledMode) {
            onDropAnimationFinished();
            return true;
        } else if (this.mAnimateTarget == null || Launcher.isResumeWithUninstalling()) {
            fadeOut();
            return true;
        } else {
            animateToTargetInner(true);
            return true;
        }
    }

    private boolean fadeOut() {
        float scale = getScaleX() * 0.9f;
        animate().alpha(0.0f).scaleX(scale).scaleY(scale).setDuration(100).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                DragView.this.onDropAnimationFinished();
            }
        });
        return true;
    }

    public void updateAnimateTarget(View target) {
        if (isTargetAnimating()) {
            setAnimateTarget(target);
            if (this.mTargetLoc == null) {
                animateToTargetInner(true);
            }
        }
    }

    public void updateAnimateTarget(float[] targetLoc) {
        this.mTargetLoc = targetLoc;
        if (isTargetAnimating()) {
            animateToTargetInner(true);
        }
    }
}
