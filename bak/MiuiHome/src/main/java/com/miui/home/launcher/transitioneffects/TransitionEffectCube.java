package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectCube extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (interpolation == 0.0f || Math.abs(interpolation) > 1.0f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        float childW = (float) child.getMeasuredWidth();
        float halfChildH = ((float) child.getMeasuredHeight()) / 2.0f;
        float scaleY = child.getScaleY();
        float transY = child.getTranslationY();
        float pivotY = child.getPivotY();
        child.setAlpha(1.0f);
        child.setTranslationY(transY + ((pivotY - halfChildH) * (1.0f - scaleY)));
        if (interpolation < 0.0f) {
            childW = 0.0f;
        }
        child.setPivotX(childW);
        child.setPivotY(halfChildH);
        child.setRotation(0.0f);
        child.setRotationX(0.0f);
        child.setRotationY(-90.0f * interpolation);
        child.setCameraDistance(TransitionEffectSwitcher.DEFAULT_ROTATE_CAMERA_DISTANCE);
    }

    public float getOverShotTension() {
        return 0.0f;
    }

    public int getScreenSnapDuration() {
        return 330;
    }

    public void resetTransformation(View child, ViewGroup group) {
        super.resetTransformationView(child);
    }
}
