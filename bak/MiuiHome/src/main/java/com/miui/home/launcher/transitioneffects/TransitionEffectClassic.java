package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectClassic extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (this.mPreEffect == null) {
            resetView(child);
        }
    }

    public float getOverShotTension() {
        return 1.3f;
    }

    public int getScreenSnapDuration() {
        return 300;
    }

    public void resetTransformation(View child, ViewGroup group) {
        super.resetTransformationView(child);
    }
}
