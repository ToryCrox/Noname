package com.tory.library.springview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by tao.xu2 on 2017/6/16.
 */

public class SpringLinearLayout extends LinearLayout implements SpringScrollable{

    protected SpringViewHelper mSpringViewHelper;

    public SpringLinearLayout(Context context) {
        this(context, null);
    }

    public SpringLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSpringViewHelper = new SpringViewHelper(this);
        mSpringViewHelper.init(attrs, defStyleAttr);
        setOrientation(getOrientation());

    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if(mSpringViewHelper != null){ //此方法会在mSpringViewHelper初始化之前调用
            mSpringViewHelper.logv("SpringLinearLayout setOrientation="+orientation);
            mSpringViewHelper.setOrientation(orientation == LinearLayout.HORIZONTAL ?
                    SpringScrollable.HORIZONTAL : SpringScrollable.VERTICAL);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (mSpringViewHelper.enableSpringEffectWhenDrag()
                && mSpringViewHelper.onInterceptTouchEvent(e)) {
            return true;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (mSpringViewHelper.enableSpringEffectWhenDrag()
                && mSpringViewHelper.onTouchEvent(e)) {
            return true;
        }
        return super.onTouchEvent(e) ;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void draw(Canvas canvas) {
        mSpringViewHelper.draw(canvas);
    }

    @Override
    public boolean isBeingDragged() {
        return false;
    }

    @Override
    public void superDraw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    public boolean superOnTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public void superAwakenScrollBars() {
        super.awakenScrollBars();
    }
}
