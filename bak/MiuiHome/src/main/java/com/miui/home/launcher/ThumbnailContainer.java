package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ThumbnailContainer extends FrameLayout {
    private CellScreen mContent;
    private float mScaleX;
    private float mScaleY;
    private float mTranslateX;
    private float mTranslateY;

    public ThumbnailContainer(Context context) {
        super(context);
    }

    public ThumbnailContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ThumbnailContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public void setContent(CellScreen cs, float scaleX, float scaleY, float translateX, float translateY) {
        this.mContent = cs;
        this.mScaleX = scaleX;
        this.mScaleY = scaleY;
        this.mTranslateX = translateX;
        this.mTranslateY = translateY;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mContent != null) {
            canvas.scale(this.mScaleX, this.mScaleY);
            canvas.translate(this.mTranslateX / this.mScaleX, this.mTranslateY / this.mScaleY);
            this.mContent.draw(canvas);
            return;
        }
        super.draw(canvas);
    }
}
