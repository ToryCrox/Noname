package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public abstract class TransitionEffect {
    public TransitionEffect mPreEffect = null;

    public abstract float getOverShotTension();

    public abstract int getScreenSnapDuration();

    public abstract void resetTransformation(View view, ViewGroup viewGroup);

    public abstract void updateTransformation(float f, float f2, float f3, float f4, View view, ViewGroup viewGroup);

    protected void resetTransformationView(View child) {
        if (this.mPreEffect == null || this.mPreEffect == this) {
            resetView(child);
        } else {
            this.mPreEffect.resetTransformationView(child);
        }
    }

    protected void resetView(View child) {
        child.setPivotX(0.0f);
        child.setPivotY(0.0f);
        child.setTranslationX(0.0f);
        child.setTranslationY(0.0f);
        child.setScaleX(1.0f);
        child.setScaleY(1.0f);
        child.setRotation(0.0f);
        child.setRotationX(0.0f);
        child.setRotationY(0.0f);
        child.setAlpha(1.0f);
    }
}
