package com.tory.library.springview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.AbsorbRecyclerView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by tao.xu2 on 2017/6/14.
 */

public class SpringRecyclerView extends AbsorbRecyclerView implements SpringScrollable{

    protected SpringRecyclerViewHelper mSpringViewHelper;

    public SpringRecyclerView(Context context) {
        this(context, null);
    }

    public SpringRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mSpringViewHelper = new SpringRecyclerViewHelper(this);
        mSpringViewHelper.init(attrs, defStyle);
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (mSpringViewHelper != null && layout != null) {
            mSpringViewHelper.setOrientation(layout.canScrollHorizontally() ?
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
    public void absorbGlows(int velocityX, int velocityY){
        mSpringViewHelper.absorbGlows(velocityX, velocityY);
    }


    @Override
    public boolean isBeingDragged() {
        return getScrollState() == RecyclerView.SCROLL_STATE_DRAGGING;
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
