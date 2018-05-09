package com.tory.library.springview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by tao.xu2 on 2017/6/15.
 */

public class SpringScrollView extends NestedScrollView implements SpringScrollable{

    protected SpringScrollViewHelper mSpringViewHelper;

    public SpringScrollView(Context context) {
        this(context, null);
    }

    public SpringScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSpringViewHelper = new SpringScrollViewHelper(this);
        mSpringViewHelper.init(attrs, defStyle);
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


    public void absorbGlows(int velocityX, int velocityY){
        mSpringViewHelper.absorbGlows(velocityX, velocityY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSpringViewHelper.onDetachedFromWindow();
    }

    @Override
    public boolean isBeingDragged() {
        return mIsBeingDragged;
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
