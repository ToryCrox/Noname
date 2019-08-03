package com.aleaf.viplist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author tory
 * @date 2018/12/25
 * @des:
 */
public class VipCardDrawable extends Drawable {


    private boolean mIsDirty;
    private int[] mGradientColors;
    private int mShadowDx;
    private int mShadowDy;
    private int mShadowRadius;
    private int mShadowColor;


    private int mCornerRadius;
    private Paint mPaint;
    private final RectF mRect = new RectF();
    private Paint mShadowPaint;

    private Drawable mCharaDrawable;

    public VipCardDrawable(@ColorInt int startColor, @ColorInt int endColor){
        mGradientColors = new int[]{startColor, endColor};

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mIsDirty = true;

        mShadowPaint = new Paint();
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setColor(Color.TRANSPARENT);
    }

    public void setCornerRadius(int cornerRadius){
        mCornerRadius = cornerRadius;
    }

    public void setShadow(int dx, int dy, int radius,
                          @ColorInt int shadowColor){
        mShadowDx = dx;
        mShadowDy = dy;
        mShadowRadius = radius;
        mShadowColor = shadowColor;
        mShadowPaint.setShadowLayer(radius, dx, dy, shadowColor);
        mIsDirty = true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mIsDirty = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (!ensureValidRect()){
            return;
        }
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mShadowPaint);
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
    }



    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private boolean ensureValidRect() {
        if (!mIsDirty) {
            return true;
        }
        Rect rect = getBounds();
        mRect.set(rect);
        mRect.inset(mShadowRadius, mShadowRadius);
        if (mShadowDx > 0){
            mRect.right -= mShadowDx;
        } else {
            mRect.left -= mShadowDx;
        }
        if (mShadowDy > 0){
            mRect.bottom -= mShadowDy * 2;
        } else {
            mRect.top -= mShadowDy;
        }
        float x0 = rect.left;
        float y0 = rect.top;
        float x1 = rect.right;
        float y1 = rect.bottom;
        mPaint.setShader(new LinearGradient(x0, y0, x1, y1, mGradientColors, new float[]{0, 1},
                Shader.TileMode.CLAMP));
        return !mRect.isEmpty();
    }
}
