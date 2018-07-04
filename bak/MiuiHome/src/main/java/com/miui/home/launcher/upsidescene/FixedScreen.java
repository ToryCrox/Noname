package com.miui.home.launcher.upsidescene;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.miui.home.R;
import com.miui.home.launcher.upsidescene.data.Screen;

public class FixedScreen extends FrameLayout {
    private FreeLayout mFreeLayout;

    public FixedScreen(Context context) {
        super(context);
    }

    public FixedScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mFreeLayout = (FreeLayout) findViewById(R.id.freeLayout);
    }

    public void setSceneScreen(SceneScreen sceneScreen) {
        this.mFreeLayout.setSceneScreen(sceneScreen);
    }

    public void setScreenData(Screen screenData) {
        if (screenData != null) {
            this.mFreeLayout.setScreenData(screenData);
        }
    }

    public int getChildWidth() {
        return this.mFreeLayout.getWidth();
    }

    public void notifyGadgets(int state) {
        this.mFreeLayout.notifyGadgets(state);
    }

    public FreeLayout getFreeLayout() {
        return this.mFreeLayout;
    }
}
