package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;

public class TransitionEffectLeftPage extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (interpolation == 0.0f || Math.abs(interpolation) > 1.0f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        float childW = (float) child.getMeasuredWidth();
        float halfChildW = childW / 2.0f;
        float halfChildH = ((float) child.getMeasuredHeight()) / 2.0f;
        float scaleY = child.getScaleY();
        float scaleX = child.getScaleX();
        float transY = child.getTranslationY();
        float pivotY = child.getPivotY();
        child.setAlpha(1.0f - Math.abs(interpolation));
        child.setTranslationY(transY + ((pivotY - halfChildH) * (1.0f - scaleY)));
        child.setTranslationX(((childW * interpolation) - ((Math.abs(interpolation) * childW) * 0.3f)) * scaleX);
        float scale1 = 1.0f + (0.3f * interpolation);
        child.setScaleX(scale1 * scaleX);
        child.setScaleY(scale1 * scaleY);
        child.setPivotX(halfChildW);
        child.setPivotY(halfChildH);
        child.setRotation(0.0f);
        child.setRotationX(0.0f);
        child.setRotationY(45.0f * (-interpolation));
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
