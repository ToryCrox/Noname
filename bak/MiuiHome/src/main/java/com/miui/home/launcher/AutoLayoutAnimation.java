package com.miui.home.launcher;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class AutoLayoutAnimation {
    private static final Interpolator DEFAULT_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static boolean sDisableAutoLayoutAnimation = false;

    public interface GhostView {
        void updateAnimateTarget(View view);
    }

    public interface HostView {
        ViewPropertyAnimator animate();

        int getBottom();

        GhostView getGhostView();

        int getLeft();

        int getRight();

        boolean getSkipNextAutoLayoutAnimation();

        int getTop();

        float getTranslationX();

        float getTranslationY();

        int getVisibility();

        boolean isEnableAutoLayoutAnimation();

        void setEnableAutoLayoutAnimation(boolean z);

        void setGhostView(GhostView ghostView);

        void setSkipNextAutoLayoutAnimation(boolean z);

        void setTranslationX(float f);

        void setTranslationY(float f);

        boolean superSetFrame(int i, int i2, int i3, int i4);
    }

    public static boolean setFrame(HostView hv, int left, int top, int right, int bottom) {
        int l = hv.getLeft();
        int t = hv.getTop();
        int r = hv.getRight();
        int b = hv.getBottom();
        boolean changed = hv.superSetFrame(left, top, right, bottom);
        if (hv.getSkipNextAutoLayoutAnimation()) {
            hv.setSkipNextAutoLayoutAnimation(false);
        } else if (!sDisableAutoLayoutAnimation && hv.isEnableAutoLayoutAnimation() && (!(l == hv.getLeft() && t == hv.getTop()) && b - t == hv.getBottom() - hv.getTop() && r - l == hv.getRight() - hv.getLeft())) {
            if (hv.getVisibility() == 0) {
                hv.setTranslationX(((float) (l - hv.getLeft())) + hv.getTranslationX());
                hv.setTranslationY(((float) (t - hv.getTop())) + hv.getTranslationY());
                hv.animate().setDuration(300).setStartDelay(0).setInterpolator(DEFAULT_INTERPOLATOR).translationX(0.0f).translationY(0.0f).start();
            }
            if (hv.getGhostView() != null) {
                hv.getGhostView().updateAnimateTarget((View) hv);
            }
        }
        return changed;
    }
}
