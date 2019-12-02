package com.tory.debug;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;



/**
 * Compat VectorDrawable resources, which is able to use in CompoundDrawables of TextView.
 * <p>
 * Created by woxingxiao on 2017-03-19.
 *
 * link https://github.com/woxingxiao/VectorCompatTextView
 */
public class VectorCompatTextView extends AppCompatTextView {
    private static final String TAG = "VectorCompatTextView";

    private boolean isTintDrawableInTextColor;
    private ColorStateList mDrawableCompatTint;
    private boolean isDrawableAdjustTextWidth;
    private boolean isDrawableAdjustTextHeight;
    private boolean isDrawableAdjustViewWidth;
    private boolean isDrawableAdjustViewHeight;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private boolean mTextIsMarquee;

    public VectorCompatTextView(Context context) {
        this(context, null);
    }

    public VectorCompatTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VectorCompatTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VectorCompatTextView);

            Drawable dl = null;
            Drawable dt = null;
            Drawable dr = null;
            Drawable db = null;
            Drawable ds = null;
            Drawable de = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dl = a.getDrawable(R.styleable.VectorCompatTextView_drawableLeftCompat);
                dt = a.getDrawable(R.styleable.VectorCompatTextView_drawableTopCompat);
                dr = a.getDrawable(R.styleable.VectorCompatTextView_drawableRightCompat);
                db = a.getDrawable(R.styleable.VectorCompatTextView_drawableBottomCompat);

                ds = a.getDrawable(R.styleable.VectorCompatTextView_drawableStartCompat);
                de = a.getDrawable(R.styleable.VectorCompatTextView_drawableEndCompat);
            } else {
                int dlId = a.getResourceId(R.styleable.VectorCompatTextView_drawableLeftCompat, -1);
                int dtId = a.getResourceId(R.styleable.VectorCompatTextView_drawableTopCompat, -1);
                int drId = a.getResourceId(R.styleable.VectorCompatTextView_drawableRightCompat, -1);
                int dbId = a.getResourceId(R.styleable.VectorCompatTextView_drawableBottomCompat, -1);
                int dsId = a.getResourceId(R.styleable.VectorCompatTextView_drawableStartCompat, -1);
                int deId = a.getResourceId(R.styleable.VectorCompatTextView_drawableEndCompat, -1);

                if (dlId != -1)
                    dl = AppCompatResources.getDrawable(context, dlId);
                if (dtId != -1)
                    dt = AppCompatResources.getDrawable(context, dtId);
                if (drId != -1)
                    dr = AppCompatResources.getDrawable(context, drId);
                if (dbId != -1)
                    db = AppCompatResources.getDrawable(context, dbId);
                if (dsId != -1)
                    ds = AppCompatResources.getDrawable(context, dsId);
                if (deId != -1)
                    de = AppCompatResources.getDrawable(context, deId);
            }
            boolean isRtl = ViewCompat.getLayoutDirection(this)
                    == ViewCompat.LAYOUT_DIRECTION_RTL;
            if (ds != null){
                if(!isRtl){
                    dl = ds;
                } else{
                    dr = ds;
                }
            }
            if (de != null){
                if (!isRtl){
                    dr = de;
                } else{
                    dl = de;
                }
            }

            isTintDrawableInTextColor = a.getBoolean(R.styleable.VectorCompatTextView_tintDrawableInTextColor, false);
            mDrawableCompatTint = a.getColorStateList(R.styleable.VectorCompatTextView_drawableCompatTint);
            isDrawableAdjustTextWidth = a.getBoolean(R.styleable.VectorCompatTextView_drawableAdjustTextWidth, false);
            isDrawableAdjustTextHeight = a.getBoolean(R.styleable.VectorCompatTextView_drawableAdjustTextHeight, false);
            isDrawableAdjustViewWidth = a.getBoolean(R.styleable.VectorCompatTextView_drawableAdjustViewWidth, false);
            isDrawableAdjustViewHeight = a.getBoolean(R.styleable.VectorCompatTextView_drawableAdjustViewHeight, false);
            mDrawableWidth = a.getDimensionPixelSize(R.styleable.VectorCompatTextView_drawableWidth, 0);
            mDrawableHeight = a.getDimensionPixelSize(R.styleable.VectorCompatTextView_drawableHeight, 0);
            mTextIsMarquee = a.getBoolean(R.styleable.VectorCompatTextView_textIsMarquee, false);
            a.recycle();

            if (mDrawableWidth < 0)
                mDrawableWidth = 0;
            if (mDrawableHeight < 0)
                mDrawableHeight = 0;
            if (isDrawableAdjustTextWidth)
                isDrawableAdjustViewWidth = false;
            if (isDrawableAdjustTextHeight)
                isDrawableAdjustViewHeight = false;

            initDrawables(dl, dt, dr, db);

            if (mTextIsMarquee){
                setEllipsize(TextUtils.TruncateAt.MARQUEE);
                setSingleLine(true);
            }
        }
    }

    private void initDrawables(final Drawable... drawables) {
        for (Drawable drawable : drawables) {
            tintDrawable(drawable);
        }

        if (!isDrawableAdjustTextWidth && !isDrawableAdjustTextHeight && !isDrawableAdjustViewWidth &&
                !isDrawableAdjustViewHeight && mDrawableWidth == 0 && mDrawableHeight == 0) {
            setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        } else {
            if (isDrawableAdjustTextWidth || isDrawableAdjustTextHeight || isDrawableAdjustViewWidth ||
                    isDrawableAdjustViewHeight) {
                boolean invalid = (
                        (isDrawableAdjustTextWidth || isDrawableAdjustViewWidth) &&
                                (drawables[0] != null || drawables[2] != null))
                        ||
                        ((isDrawableAdjustTextHeight || isDrawableAdjustViewHeight)
                                && (drawables[1] != null || drawables[3] != null));
                if (invalid) {
                    if (mDrawableWidth > 0 || mDrawableHeight > 0) {
                        resizeDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                    } else {
                        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
                    }
                } else {
                    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT < 16) {
                                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            } else {
                                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }

                            adjustDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
                        }
                    });
                }
            } else if (mDrawableWidth > 0 || mDrawableHeight > 0) {
                resizeDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
            }
        }
    }

    private void tintDrawable(Drawable drawable) {
        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = DrawableCompat.wrap(drawable).mutate();
            }
            if (isTintDrawableInTextColor) {
                DrawableCompat.setTint(drawable, getCurrentTextColor());
            } else if (mDrawableCompatTint != null) {
                DrawableCompat.setTintList(drawable, mDrawableCompatTint);
            }
        }
    }

    private void resizeDrawables(Drawable... drawables) {
        for (Drawable drawable : drawables) {
            if (drawable == null) {
                continue;
            }

            if (mDrawableWidth > 0 && mDrawableHeight > 0) {
                drawable.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
            } else if (mDrawableWidth > 0) {
                int h = mDrawableWidth * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
                drawable.setBounds(0, 0, mDrawableWidth, h);
            } else {
                int w = mDrawableHeight * drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
                drawable.setBounds(0, 0, w, mDrawableHeight);
            }
        }

        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    @Override
    public void setScrollX(int value) {
        super.setScrollX(value);
        final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        Throwable e = new Throwable();
        e.setStackTrace(stackTrace);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        if (isDrawableAdjustTextWidth || isDrawableAdjustTextHeight) {
            Drawable[] drawables = getCompoundDrawables();
            if (drawables[0] == null && drawables[1] == null && drawables[2] == null && drawables[3] == null)
                return;

            adjustDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        }
    }

    private void adjustDrawables(Drawable dl, Drawable dt, Drawable dr, Drawable db) {
        int width = 0;
        int height = 0;

        if (isDrawableAdjustTextWidth) {
            Paint paint = new Paint();
            paint.setTextSize(getTextSize());
            CharSequence text = getText();
            Rect rect = new Rect();
            paint.getTextBounds(text.toString(), 0, text.length(), rect);

            width = rect.width();
        } else if (isDrawableAdjustViewWidth) {
            width = getMeasuredWidth();
        }
        if (isDrawableAdjustTextHeight) {
            Paint paint = new Paint();
            paint.setTextSize(getTextSize());
            CharSequence text = getText();
            Rect rect = new Rect();
            paint.getTextBounds(text.toString(), 0, text.length(), rect);

            height = rect.height();
        } else if (isDrawableAdjustViewHeight) {
            height = getMeasuredHeight();
        }

        int h = mDrawableHeight;
        int w = mDrawableWidth;

        if (dt != null) {
            if (h == 0) h = width * dt.getIntrinsicHeight() / dt.getIntrinsicWidth();
            dt.setBounds(0, 0, width, h);
        }
        if (db != null) {
            if (h == 0) h = width * db.getIntrinsicHeight() / db.getIntrinsicWidth();
            db.setBounds(0, 0, width, h);
        }

        if (dl != null) {
            if (w == 0) w = height * dl.getIntrinsicWidth() / dl.getIntrinsicHeight();
            dl.setBounds(0, 0, w, height);
        }
        if (dr != null) {
            if (w == 0) w = height * dr.getIntrinsicWidth() / dr.getIntrinsicHeight();
            dr.setBounds(0, 0, w, height);
        }

        setCompoundDrawables(dl, dt, dr, db);
    }

    @Override
    public void setTextColor(@ColorInt int color) {
        super.setTextColor(color);

        refreshCompoundDrawables();
    }

    private void refreshCompoundDrawables() {
        Drawable[] drawables = getCompoundDrawables();
        for (Drawable drawable : drawables) {
            tintDrawable(drawable);
        }

        setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    public boolean isTintDrawableInTextColor() {
        return isTintDrawableInTextColor;
    }

    public void setTintDrawableInTextColor(boolean tintDrawableInTextColor) {
        if (isTintDrawableInTextColor == tintDrawableInTextColor)
            return;

        isTintDrawableInTextColor = tintDrawableInTextColor;
        refreshCompoundDrawables();
    }

    public ColorStateList getDrawableCompatTint() {
        return mDrawableCompatTint;
    }



    public void setDrawableTint(@ColorInt int color) {
        if (mDrawableCompatTint == ColorStateList.valueOf(color))
            return;
        mDrawableCompatTint = ColorStateList.valueOf(color);
        refreshCompoundDrawables();
    }

    public void setDrawableTint(@Nullable ColorStateList drawableCompatTint) {
        if (mDrawableCompatTint == drawableCompatTint)
            return;
        mDrawableCompatTint = drawableCompatTint;
        refreshCompoundDrawables();
    }

    /*@Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);

        Drawable[] drawables = getCompoundDrawables();
        initDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    @Override
    public void toggle() {
        super.toggle();

        Drawable[] drawables = getCompoundDrawables();
        initDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }*/

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (isTintDrawableInTextColor) {
            Drawable[] drawables = getCompoundDrawables();

            boolean needRefresh = false;
            for (Drawable drawable : drawables) {
                if (drawable != null) {
                    needRefresh = true;
                    break;
                }
            }

            if (needRefresh) {
                refreshCompoundDrawables();
            }
        }
    }

    @Override
    public boolean isFocused() {
        if (mTextIsMarquee){
            return true;
        }
        return super.isFocused();
    }
}