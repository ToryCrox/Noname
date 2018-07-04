package com.miui.home.launcher.upsidescene;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import com.miui.home.R;

public class SceneContentView extends FrameLayout {
    private int mOverWidth;
    private SceneScreen mSceneScreen;
    private ScrollableScreen mScrollableScreen;

    public SceneContentView(Context context) {
        super(context);
    }

    public SceneContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SceneContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (System.currentTimeMillis() == 0) {
            setOverWidth(getOverWidth());
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mScrollableScreen = (ScrollableScreen) findViewById(R.id.scrollableScreen);
    }

    public void setSceneScreen(SceneScreen sceneScreen) {
        this.mSceneScreen = sceneScreen;
    }

    public void setOverWidth(int width) {
        this.mOverWidth = width;
        getLayoutParams().width = width;
        this.mSceneScreen.requestLayout();
    }

    public int getOverWidth() {
        return this.mOverWidth == 0 ? getWidth() : this.mOverWidth;
    }

    public void widthTo(int targetWidth) {
        Animator animator = ObjectAnimator.ofInt(this, "overWidth", new int[]{targetWidth});
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }
}
