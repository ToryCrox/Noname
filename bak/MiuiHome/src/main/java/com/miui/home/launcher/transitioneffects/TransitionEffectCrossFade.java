package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectCrossFade extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (Math.abs(interpolation) > 1.0f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        child.setAlpha(((1.0f - Math.abs(interpolation)) * 0.7f) + 0.3f);
    }

    public float getOverShotTension() {
        return 0.0f;
    }

    public int getScreenSnapDuration() {
        return 270;
    }

    public void resetTransformation(View child, ViewGroup group) {
        super.resetTransformationView(child);
    }
}
