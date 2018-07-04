package com.miui.home.launcher.transitioneffects;

import android.view.View;
import android.view.ViewGroup;
import com.miui.home.launcher.Workspace;

public class TransitionEffectEditingMode extends TransitionEffect {
    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (Math.abs(interpolation) > 1.0799999f) {
            resetTransformationView(child);
            return;
        }
        if (this.mPreEffect == null) {
            resetView(child);
        }
        float childW = (float) child.getMeasuredWidth();
        float childH = (float) child.getMeasuredHeight();
        float transH = childW * 0.03999999f;
        float transOffset = (2.0f * transH) * interpolation;
        float transV = childH * Workspace.SCREEN_TRANS_V_RATO;
        child.setAlpha(1.0f);
        child.setTranslationX(interpolation > 0.0f ? transOffset - transH : transOffset + transH);
        child.setTranslationY(transV);
        child.setScaleX(0.92f);
        child.setScaleY(0.92f);
        if (interpolation <= 0.0f) {
            childW = 0.0f;
        }
        child.setPivotX(childW);
        child.setPivotY(childH / 2.0f);
        child.setRotation(0.0f);
        child.setRotationX(0.0f);
        child.setRotationY(0.0f);
        child.setCameraDistance(TransitionEffectSwitcher.DEFAULT_CAMERA_DISTANCE);
    }

    public float getOverShotTension() {
        return 0.0f;
    }

    public int getScreenSnapDuration() {
        return 180;
    }

    protected void resetView(View child) {
        float childW = (float) child.getMeasuredWidth();
        float childH = (float) child.getMeasuredHeight();
        child.setTranslationY(childH * Workspace.SCREEN_TRANS_V_RATO);
        child.setTranslationX(0.0f);
        float halfChildH = childH / 2.0f;
        child.setPivotX(childW / 2.0f);
        child.setPivotY(halfChildH);
        child.setScaleX(0.92f);
        child.setScaleY(0.92f);
        child.setRotationX(0.0f);
        child.setRotationY(0.0f);
        child.setCameraDistance(TransitionEffectSwitcher.DEFAULT_CAMERA_DISTANCE);
    }

    public void resetTransformation(View child, ViewGroup group) {
        resetTransformationView(child);
    }
}
