package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectClassicNoOverShoot extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (this.mPreEffect == null) {
            resetView(child);
        }
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
