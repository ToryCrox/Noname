package com.miui.home.launcher;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.miui.home.R;
import com.miui.home.launcher.common.Ease.Cubic;
import com.miui.home.launcher.common.Ease.Quint;

public class ForceTouchPressureCircle extends ImageView {
    private Paint mAlphaPaint;
    private float mCenterX;
    private float mCenterY;
    private ValueAnimator mFullScreenAnimator;
    private boolean mIsInFolder;
    private float mMaxPressedRadius;
    private Paint mPaint;
    private int mPressedColor;
    private int mPressedInFolderColor;
    private float mRadius;
    private int mSpreadColor;
    private int mSpreadInFolderColor;
    private float mWholeScreenRadius;
    private ValueAnimator mZoomAnimator;
    private boolean mZoomOut;

    public ForceTouchPressureCircle(Context context) {
        this(context, null);
    }

    public ForceTouchPressureCircle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceTouchPressureCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPaint = new Paint();
        this.mAlphaPaint = new Paint();
        this.mFullScreenAnimator = new ValueAnimator();
        this.mZoomAnimator = new ValueAnimator();
        this.mZoomOut = true;
        this.mMaxPressedRadius = context.getResources().getDimension(R.dimen.force_touch_circle_pressed_max_radius);
        this.mPressedColor = context.getResources().getColor(R.color.force_touch_pressure_circle_pressed);
        this.mSpreadColor = context.getResources().getColor(R.color.force_touch_pressure_circle_spread);
        this.mPressedInFolderColor = context.getResources().getColor(R.color.force_touch_pressure_circle_pressed_in_folder);
        this.mSpreadInFolderColor = context.getResources().getColor(R.color.force_touch_pressure_circle_spread_in_folder);
        this.mWholeScreenRadius = context.getResources().getDimension(R.dimen.force_touch_circle_whole_screen);
        this.mPaint.setColor(this.mPressedColor);
        this.mFullScreenAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mFullScreenAnimator.setDuration(300);
        this.mFullScreenAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ForceTouchPressureCircle.this.mRadius = ((ForceTouchPressureCircle.this.mWholeScreenRadius - ForceTouchPressureCircle.this.mMaxPressedRadius) * ((Float) animation.getAnimatedValue()).floatValue()) + ForceTouchPressureCircle.this.mMaxPressedRadius;
                ForceTouchPressureCircle.this.invalidate();
            }
        });
        this.mZoomAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float animateValue = ((Float) animation.getAnimatedValue()).floatValue();
                ForceTouchPressureCircle.this.mRadius = ForceTouchPressureCircle.this.mZoomOut ? ForceTouchPressureCircle.this.mMaxPressedRadius * animateValue : (1.0f - animateValue) * ForceTouchPressureCircle.this.mRadius;
                ForceTouchPressureCircle.this.invalidate();
            }
        });
        this.mZoomAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mAlphaPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mAlphaPaint);
        if (this.mFullScreenAnimator.isRunning() || this.mRadius > this.mMaxPressedRadius) {
            this.mPaint.setColor(this.mIsInFolder ? this.mSpreadInFolderColor : this.mSpreadColor);
        } else {
            this.mPaint.setColor(this.mIsInFolder ? this.mPressedInFolderColor : this.mPressedColor);
        }
        canvas.drawCircle(this.mCenterX, this.mCenterY, this.mRadius, this.mPaint);
    }

    public void setPressure(float pressure, float minPressure, float maxPressure) {
        if (pressure <= maxPressure && pressure >= minPressure) {
            this.mRadius = ((pressure - minPressure) / (maxPressure - minPressure)) * this.mMaxPressedRadius;
            invalidate();
        }
    }

    public void setCenterXY(float centerX, float centerY) {
        this.mCenterX = centerX;
        this.mCenterY = centerY;
    }

    public void startSpreadAnimate() {
        this.mFullScreenAnimator.start();
        performHapticFeedback(0, 1);
    }

    public void setIsInFolder(boolean isInFolder) {
        this.mIsInFolder = isInFolder;
        this.mAlphaPaint.setAlpha((int) ((this.mIsInFolder ? 0.4f : 0.1f) * 255.0f));
    }

    public void changeCircleSizeWhenShaking(boolean spread) {
        this.mZoomOut = spread;
        this.mZoomAnimator.setDuration(this.mZoomOut ? 350 : 80);
        this.mZoomAnimator.setInterpolator(this.mZoomOut ? Quint.easeOut : Cubic.easeIn);
        this.mZoomAnimator.setStartDelay(this.mZoomOut ? 0 : 60);
        this.mZoomAnimator.start();
    }

    public void setZoomOutAnimatorListenerAdapter(AnimatorListenerAdapter adapter) {
        this.mZoomAnimator.removeAllListeners();
        this.mZoomAnimator.addListener(adapter);
    }

    public void clearZoomOutAnimatorListener() {
        this.mZoomAnimator.removeAllListeners();
    }

    public void cancelZoomOutAnimation() {
        if (this.mZoomAnimator.isRunning() && this.mZoomOut) {
            this.mZoomAnimator.cancel();
        }
    }
}
