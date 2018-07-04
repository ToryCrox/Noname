package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectFallDown extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (interpolation == 0.0f || Math.abs(interpolation) > 1.0f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        float childH = (float) child.getMeasuredHeight();
        float halfChildW = ((float) child.getMeasuredWidth()) / 2.0f;
        float scaleY = child.getScaleY();
        float scaleX = child.getScaleX();
        float transX = child.getTranslationX();
        float pivotX = child.getPivotX();
        float transY = child.getTranslationY();
        float pivotY = child.getPivotY();
        child.setAlpha(1.0f);
        transY += (pivotY - childH) * (1.0f - scaleY);
        child.setTranslationX(transX + ((pivotX - halfChildW) * (1.0f - scaleX)));
        child.setTranslationY(transY);
        child.setPivotX(halfChildW);
        child.setPivotY(childH);
        child.setRotation((-interpolation) * 30.0f);
        child.setRotationX(0.0f);
        child.setRotationY(0.0f);
        child.setCameraDistance(TransitionEffectSwitcher.DEFAULT_CAMERA_DISTANCE);
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
