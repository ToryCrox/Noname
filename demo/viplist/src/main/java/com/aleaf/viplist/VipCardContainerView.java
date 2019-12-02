package com.aleaf.viplist;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * @author tory
 * @date 2018/12/25
 * @des:
 */
public class VipCardContainerView extends FrameLayout {

    private static final String TAG = "VipCardContainerView";

    private boolean mGradientIsDirty;
    private int[] mGradientColors;
    private int mShadowDx;
    private int mShadowDy;
    private int mShadowRadius;
    private int mShadowColor;

    private int mMinContentHeight;
    private int mCornerRadius;
    private Paint mPaint;
    private final RectF mRect = new RectF();
    private Paint mShadowPaint;
    private Path mPath;

    private boolean mOnlyHasTopCorner;
    private Drawable mCharaDrawable;
    private Drawable mMaskDrawable;
    private int mCharaDrawableOffsetEnd;

    public VipCardContainerView(@NonNull Context context) {
        this(context, null);
    }

    public VipCardContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VipCardContainerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mGradientIsDirty = true;

        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setColor(Color.TRANSPARENT);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VipCardContainerView);
        mShadowDx = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_shadow_dx, 0);
        mShadowDy = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_shadow_dy, 0);
        mShadowRadius = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_shadow_radius, 0);
        mCornerRadius = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_corner_radius, 0);
        mShadowColor = array.getColor(R.styleable.VipCardContainerView_vcc_shadow_color, 0);
        mMinContentHeight = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_min_content_height, 0);
        mCharaDrawable = array.getDrawable(R.styleable.VipCardContainerView_vcc_chara_drawable);
        mMaskDrawable = array.getDrawable(R.styleable.VipCardContainerView_vcc_mask_drawable);
        mCharaDrawableOffsetEnd = array.getDimensionPixelOffset(R.styleable.VipCardContainerView_vcc_chara_drawable_offset_end, 0);
        mOnlyHasTopCorner = array.getBoolean(R.styleable.VipCardContainerView_vcc_corner_only_has_top, false);
        setCharaDrawable(mCharaDrawable);

        int startColor = array.getColor(R.styleable.VipCardContainerView_vcc_start_color, 0);
        int endColor = array.getColor(R.styleable.VipCardContainerView_vcc_end_color, 0);

        array.recycle();

        setGradientColors(startColor, endColor);

        setShadowColor(mShadowColor);
    }

    @Override
    public void setLayerType(int layerType, @Nullable Paint paint) {
        super.setLayerType(layerType, paint);
        Log.d(TAG, "setLayerType " + layerType);
    }

    public void setGradientColors(@ColorInt int startColor, @ColorInt int endColor) {
        if (startColor != 0 || endColor != 0) {
            mGradientColors = new int[]{startColor, endColor};
        } else {
            mGradientColors = null;
        }
        mGradientIsDirty = true;
    }

    public void setShadowColor(int shadowColor) {
        if (shadowColor != 0) {
            mShadowColor = shadowColor;
            mShadowPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, shadowColor);
        }
    }

    public void setCharaDrawable(Drawable drawable) {
        mCharaDrawable = drawable;
        if (mCharaDrawable != null) {
            mCharaDrawable.setBounds(0, 0, mCharaDrawable.getIntrinsicWidth(),
                    mCharaDrawable.getIntrinsicHeight());
        }
    }

    public void setMaskDrawable(Drawable drawable) {
        mMaskDrawable = drawable;
    }

    public void setCornerRadius(int cornerRadius){
        mCornerRadius = cornerRadius;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mGradientIsDirty = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int charaHeight = mCharaDrawable != null ? mCharaDrawable.getIntrinsicHeight() : 0;
        int height = Math.max(charaHeight + getPaddingBottom(),
                mMinContentHeight + getPaddingBottom() + getPaddingTop());
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (!ensureValidRect()) {
            return;
        }
        if (mShadowColor != 0) {
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);
            } else {
                canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mShadowPaint);
            }
        }
        if (mGradientColors != null) {
            if (mPath != null) {
                canvas.drawPath(mPath, mPaint);
            } else {
                canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
            }
        }
        if (mCharaDrawable != null) {
            canvas.save();
            canvas.translate(mRect.right - mCharaDrawableOffsetEnd - mCharaDrawable.getIntrinsicWidth(), 0);
            mCharaDrawable.draw(canvas);
            canvas.restore();
        }
        if (mMaskDrawable != null) {
            canvas.save();
            canvas.translate(mRect.left, mRect.top);
            mMaskDrawable.draw(canvas);
            canvas.restore();
        }

        super.dispatchDraw(canvas);
    }

    private boolean ensureValidRect() {
        if (!mGradientIsDirty) {
            return true;
        }
        mRect.set(0, 0, getWidth(), getHeight());
        mRect.left += getPaddingLeft();
        mRect.top += getPaddingTop();
        mRect.right -= getPaddingRight();
        mRect.bottom -= getPaddingBottom();
        if (mMaskDrawable != null){
            mMaskDrawable.setBounds(0, 0, (int) mRect.width(), (int) mRect.height());
        }
        if (mOnlyHasTopCorner){
            if (mPath == null){
                mPath = new Path();
            } else {
                mPath.reset();
            }
            mPath.addRoundRect(mRect, new float[]{mCornerRadius, mCornerRadius,
                    mCornerRadius, mCornerRadius, 0, 0, 0, 0}, Path.Direction.CW);
        } else {
            mPath = null;
        }

        if (mGradientColors != null) {
            float x0 = mRect.left;
            float y0 = mRect.top;
            float x1 = mRect.right;
            float y1 = mRect.bottom;
            mPaint.setShader(new LinearGradient(x0, y0, x1, y1, mGradientColors, new float[]{0, 1},
                    Shader.TileMode.CLAMP));
        }

        return !mRect.isEmpty();
    }
}
