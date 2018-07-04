package com.miui.home.launcher;

import android.content.Context;
import android.widget.ImageView;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;

public class CellBackground extends ImageView implements HostView {
    private boolean mIsIconCellBackground = false;
    private boolean mSkipNextAutoLayoutAnimation = false;

    public CellBackground(Context context) {
        super(context);
    }

    public void setEnableAutoLayoutAnimation(boolean isEnable) {
    }

    public boolean isEnableAutoLayoutAnimation() {
        return true;
    }

    public void bindDragObject(DragObject d) {
        this.mIsIconCellBackground = d.getDragView().getContent() instanceof ItemIcon;
    }

    public void unbindDragObject(DragObject d) {
    }

    public boolean isIconCellBackground() {
        return this.mIsIconCellBackground;
    }

    public void setSkipNextAutoLayoutAnimation(boolean isSkip) {
        this.mSkipNextAutoLayoutAnimation = isSkip;
    }

    public boolean getSkipNextAutoLayoutAnimation() {
        return this.mSkipNextAutoLayoutAnimation;
    }

    public boolean superSetFrame(int left, int top, int right, int bottom) {
        return super.setFrame(left, top, right, bottom);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        return AutoLayoutAnimation.setFrame(this, left, top, right, bottom);
    }

    public void setGhostView(GhostView gv) {
    }

    public GhostView getGhostView() {
        return null;
    }
}
