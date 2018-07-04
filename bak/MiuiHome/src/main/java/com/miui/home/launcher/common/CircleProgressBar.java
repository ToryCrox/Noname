package com.miui.home.launcher.common;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import com.miui.home.R;

public class CircleProgressBar extends ProgressBar {
    private RectF mArcRect;
    private Bitmap mBitmapForSoftLayer;
    private Canvas mCanvasForSoftLayer;
    private Animator mChangeProgressAnimator;
    private int mCurrentLevel;
    public boolean mDrawTouchMask;
    private Drawable[] mLevelsBackDrawable;
    private Drawable[] mLevelsForeDrawable;
    private Drawable[] mLevelsMiddleDrawable;
    private int mMaskColor;
    private Paint mPaint;
    private int mPrevAlpha;
    private int mPrevLevel;
    private OnProgressChangedListener mProgressChangedListener;
    private int[] mProgressLevels;
    private int mRotateVelocity;
    private Drawable mThumb;

    public interface OnProgressChangedListener {
        void onProgressChanged();
    }

    public CircleProgressBar(Context context) {
        this(context, null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRotateVelocity = 300;
        this.mPaint = new Paint();
        this.mPaint.setColor(-16777216);
        if (System.currentTimeMillis() == 0) {
            setPrevAlpha(getPrevAlpha());
        }
        this.mDrawTouchMask = false;
        this.mMaskColor = context.getResources().getColor(R.color.folder_foreground_mask);
        setIndeterminate(false);
    }

    public int getProgressLevelCount() {
        return this.mProgressLevels == null ? 1 : this.mProgressLevels.length + 1;
    }

    public void setDrawablesForLevels(Drawable[] backs, Drawable[] middles, Drawable[] fores) {
        this.mLevelsBackDrawable = backs;
        this.mLevelsMiddleDrawable = middles;
        this.mLevelsForeDrawable = fores;
        if (backs != null) {
            for (Drawable drawable : backs) {
                drawable.mutate();
            }
        }
        if (middles != null) {
            for (Drawable drawable2 : middles) {
                drawable2.mutate();
            }
        }
        if (fores != null) {
            for (Drawable drawable22 : fores) {
                drawable22.mutate();
            }
        }
        for (Drawable drawable222 : middles) {
            if (drawable222 instanceof BitmapDrawable) {
                ((BitmapDrawable) drawable222).getPaint().setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            } else if (drawable222 instanceof NinePatchDrawable) {
                ((NinePatchDrawable) drawable222).getPaint().setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            } else {
                throw new IllegalArgumentException("'middles' must a bitmap or nine patch drawable.");
            }
        }
        this.mArcRect = new RectF((float) (middles[0].getBounds().left - 5), (float) (middles[0].getBounds().top - 5), (float) (middles[0].getBounds().right + 5), (float) (middles[0].getBounds().bottom + 5));
    }

    public void setDrawablesForLevels(int[] resourceIdBacks, int[] resourceIdMiddles, int[] resourceIdFores) {
        setDrawablesForLevels(getDrawables(resourceIdBacks), getDrawables(resourceIdMiddles), getDrawables(resourceIdFores));
    }

    private Drawable[] getDrawables(int[] resourceIds) {
        if (resourceIds == null) {
            return null;
        }
        Resources resources = getContext().getResources();
        Drawable[] drawables = new Drawable[resourceIds.length];
        for (int i = 0; i < resourceIds.length; i++) {
            drawables[i] = resources.getDrawable(resourceIds[i]);
            drawables[i].setBounds(0, 0, drawables[i].getIntrinsicWidth(), drawables[i].getIntrinsicHeight());
        }
        return drawables;
    }

    private Drawable getBackDrawable(int level) {
        return this.mLevelsBackDrawable == null ? null : this.mLevelsBackDrawable[level];
    }

    private Drawable getMiddleDrawable(int level) {
        return this.mLevelsMiddleDrawable == null ? null : this.mLevelsMiddleDrawable[level];
    }

    private Drawable getForeDrawable(int level) {
        return this.mLevelsForeDrawable == null ? null : this.mLevelsForeDrawable[level];
    }

    public void setRotateVelocity(int velocity) {
        this.mRotateVelocity = velocity;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener progressChangedListener) {
        this.mProgressChangedListener = progressChangedListener;
    }

    public void setProgressByAnimator(int progress, AnimatorListener listener) {
        stopProgressAnimator();
        int offsetAngle = Math.abs((int) ((((float) (progress - getProgress())) / ((float) getMax())) * 360.0f));
        this.mChangeProgressAnimator = ObjectAnimator.ofInt(this, "progress", new int[]{progress});
        this.mChangeProgressAnimator.setDuration((long) calcDuration(offsetAngle));
        this.mChangeProgressAnimator.setInterpolator(getInterpolator());
        if (listener != null) {
            this.mChangeProgressAnimator.addListener(listener);
        }
        this.mChangeProgressAnimator.start();
    }

    public void stopProgressAnimator() {
        if (this.mChangeProgressAnimator != null && this.mChangeProgressAnimator.isRunning()) {
            this.mChangeProgressAnimator.cancel();
        }
    }

    private int calcDuration(int angle) {
        return (angle * 1000) / this.mRotateVelocity;
    }

    protected void drawableStateChanged() {
        int[] stateSets = getDrawableState();
        boolean drawMask = StateSet.stateSetMatches(PRESSED_STATE_SET, stateSets) || StateSet.stateSetMatches(FOCUSED_WINDOW_FOCUSED_STATE_SET, stateSets);
        if (this.mDrawTouchMask != drawMask) {
            this.mDrawTouchMask = drawMask;
            ((View) getParent()).invalidate();
        }
        super.drawableStateChanged();
        int size = getProgressLevelCount();
        for (int i = 0; i < size; i++) {
            if (this.mLevelsBackDrawable != null) {
                this.mLevelsBackDrawable[i].setState(getDrawableState());
            }
            if (this.mLevelsMiddleDrawable != null) {
                this.mLevelsMiddleDrawable[i].setState(getDrawableState());
            }
            if (this.mLevelsForeDrawable != null) {
                this.mLevelsForeDrawable[i].setState(getDrawableState());
            }
        }
        invalidate();
    }

    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        int newLevel = -1;
        if (this.mProgressLevels == null) {
            newLevel = 0;
        } else {
            int size = this.mProgressLevels.length;
            for (int i = 0; i < size; i++) {
                if (progress < this.mProgressLevels[i]) {
                    newLevel = i;
                    break;
                }
            }
            if (newLevel == -1) {
                newLevel = size;
            }
        }
        if (newLevel != this.mCurrentLevel) {
            this.mPrevLevel = this.mCurrentLevel;
            this.mCurrentLevel = newLevel;
            setPrevAlpha(255);
            Animator fadeOutAnimator = ObjectAnimator.ofInt(this, "prevAlpha", new int[]{0});
            fadeOutAnimator.setDuration(300);
            fadeOutAnimator.setInterpolator(new LinearInterpolator());
            fadeOutAnimator.start();
        }
        if (this.mProgressChangedListener != null) {
            this.mProgressChangedListener.onProgressChanged();
        }
    }

    private float getRate() {
        return ((float) getProgress()) / ((float) getMax());
    }

    private int getIntrinsicWidth() {
        int minWidth = getMiddleDrawable(0).getIntrinsicWidth();
        if (this.mLevelsForeDrawable != null) {
            minWidth = Math.max(minWidth, this.mLevelsForeDrawable[0].getIntrinsicWidth());
        }
        if (this.mLevelsBackDrawable != null) {
            return Math.max(minWidth, this.mLevelsBackDrawable[0].getIntrinsicWidth());
        }
        return minWidth;
    }

    private int getIntrinsicHeight() {
        int minHeight = getMiddleDrawable(0).getIntrinsicHeight();
        if (this.mLevelsForeDrawable != null) {
            minHeight = Math.max(minHeight, this.mLevelsForeDrawable[0].getIntrinsicHeight());
        }
        if (this.mLevelsBackDrawable != null) {
            return Math.max(minHeight, this.mLevelsBackDrawable[0].getIntrinsicHeight());
        }
        return minHeight;
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getIntrinsicWidth(), getIntrinsicHeight());
    }

    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        if (this.mDrawTouchMask) {
            canvas.drawColor(this.mMaskColor, Mode.SRC_ATOP);
        }
        canvas.restore();
        drawLayer(canvas, getBackDrawable(this.mCurrentLevel), getForeDrawable(this.mCurrentLevel), getMiddleDrawable(this.mCurrentLevel), getRate(), 255 - this.mPrevAlpha);
        if (this.mPrevAlpha >= 10) {
            drawLayer(canvas, getBackDrawable(this.mPrevLevel), getForeDrawable(this.mPrevLevel), getMiddleDrawable(this.mPrevLevel), getRate(), this.mPrevAlpha);
        }
    }

    private void drawLayer(Canvas canvas, Drawable back, Drawable fore, Drawable middle, float rate, int alpha) {
        if (back != null) {
            back.setAlpha(alpha);
            back.draw(canvas);
        }
        if (canvas.isHardwareAccelerated()) {
            canvas.saveLayer((float) middle.getBounds().left, (float) middle.getBounds().top, (float) middle.getBounds().right, (float) middle.getBounds().bottom, null, 16);
            canvas.drawArc(this.mArcRect, -90.0f, 360.0f * rate, true, this.mPaint);
            middle.setAlpha(alpha);
            middle.draw(canvas);
            canvas.restore();
        } else {
            if (this.mBitmapForSoftLayer == null) {
                this.mBitmapForSoftLayer = Utilities.createBitmapSafely(middle.getBounds().width(), middle.getBounds().height(), Config.ARGB_8888);
                if (this.mBitmapForSoftLayer != null) {
                    this.mCanvasForSoftLayer = new Canvas(this.mBitmapForSoftLayer);
                } else {
                    return;
                }
            }
            this.mBitmapForSoftLayer.eraseColor(0);
            this.mCanvasForSoftLayer.save();
            this.mCanvasForSoftLayer.translate((float) (-middle.getBounds().left), (float) (-middle.getBounds().top));
            this.mCanvasForSoftLayer.drawArc(this.mArcRect, -90.0f, 360.0f * rate, true, this.mPaint);
            middle.setAlpha(alpha);
            middle.draw(this.mCanvasForSoftLayer);
            this.mCanvasForSoftLayer.restore();
            canvas.drawBitmap(this.mBitmapForSoftLayer, (float) middle.getBounds().left, (float) middle.getBounds().top, null);
        }
        Drawable thumb = this.mThumb;
        if (thumb != null) {
            canvas.save();
            int x = ((getWidth() - getPaddingLeft()) - getPaddingRight()) / 2;
            int y = ((getHeight() - getPaddingTop()) - getPaddingBottom()) / 2;
            int w = thumb.getIntrinsicWidth();
            int h = thumb.getIntrinsicHeight();
            canvas.rotate((360.0f * ((float) getProgress())) / ((float) getMax()), (float) x, (float) y);
            thumb.setBounds(x - (w / 2), y - (h / 2), (w / 2) + x, (h / 2) + y);
            thumb.draw(canvas);
            canvas.restore();
        }
        if (fore != null) {
            fore.setAlpha(alpha);
            fore.draw(canvas);
        }
    }

    public void setPrevAlpha(int alpha) {
        this.mPrevAlpha = alpha;
        invalidate();
    }

    public int getPrevAlpha() {
        return this.mPrevAlpha;
    }
}
