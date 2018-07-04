package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectStack extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (interpolation <= 0.0f || interpolation >= 1.0f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        float childW = (float) child.getMeasuredWidth();
        float halfChildH = ((float) child.getMeasuredHeight()) / 2.0f;
        float scaleY = child.getScaleY();
        float scaleX = child.getScaleX();
        float transY = child.getTranslationY();
        float pivotY = child.getPivotY();
        child.setAlpha(1.0f - interpolation);
        float scale2 = 0.6f + (0.4f * (1.0f - interpolation));
        child.setTranslationY(transY + ((pivotY - halfChildH) * (1.0f - scaleY)));
        View view = child;
        view.setTranslationX((((1.0f - scale2) * childW) * 3.0f) + (((1.0f - scaleX) * childW) / 2.0f));
        child.setScaleX(scale2 * scaleX);
        child.setScaleY(scale2 * scaleY);
        child.setPivotX(0.0f);
        child.setPivotY(halfChildH);
        child.setRotation(0.0f);
        child.setRotationX(0.0f);
        child.setRotationY(0.0f);
        child.setCameraDistance(TransitionEffectSwitcher.DEFAULT_CAMERA_DISTANCE);
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
