package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectNoType extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
    }

    public float getOverShotTension() {
        return 0.0f;
    }

    public int getScreenSnapDuration() {
        return 180;
    }

    public void resetTransformation(View child, ViewGroup group) {
        super.resetTransformationView(child);
    }
}
