package com.tory.library.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.tory.library.R;

/**
 * Created by tao.xu2 on 2017/4/19.
 */

public class TintImageView extends AppCompatImageView{

    private ColorStateList mDrawableTint;

    public TintImageView(Context context) {
        super(context);
    }

    public TintImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TintImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.TintViewDrawable, defStyleAttr, 0);

        if(a.hasValue(R.styleable.TintViewDrawable_drawableTint)){
            mDrawableTint = a.getColorStateList(R.styleable.TintViewDrawable_drawableTint);
            applyDrawableTint();
        }
    }

    public void setDrawableTint(@ColorInt int color){
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        mDrawableTint = colorStateList;
        applyDrawableTint();
    }

    public void setDrawableTint(ColorStateList colorTint){
        if(colorTint != mDrawableTint){
            mDrawableTint = colorTint;
            applyDrawableTint();
        }
    }

    private void applyDrawableTint() {
        Drawable drawable = getDrawable();
        if(mDrawableTint != null && drawable != null){
            setImageDrawable(tintDrawable(drawable,mDrawableTint));
        }
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors){
        return tintDrawable(drawable,colors,true);
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList colors, boolean mute) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(mute ? drawable.mutate() : drawable);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }
}
