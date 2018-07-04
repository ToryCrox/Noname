package com.miui.home.launcher.transitioneffects;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import com.miui.home.R;
import java.util.ArrayList;
import java.util.Iterator;

public class TransitionEffectSwitcher extends TransitionEffect {
    public static final float DEFAULT_CAMERA_DISTANCE;
    public static float DEFAULT_ROTATE_CAMERA_DISTANCE;
    public static final int[] mEffectsDrawableIds = new int[9];
    private int mCurrentTypeIndex;
    private ArrayList<Integer> mCurrentTypeIndexList = new ArrayList();
    private TransitionEffect[] mEffects = new TransitionEffect[11];

    static {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        DEFAULT_CAMERA_DISTANCE = dm.density * 1280.0f;
        DEFAULT_ROTATE_CAMERA_DISTANCE = (float) ((((Math.pow(2.0d, (double) dm.density) * ((double) dm.widthPixels)) / ((double) dm.density)) / 320.0d) * 1280.0d);
        mEffectsDrawableIds[0] = R.drawable.transition_effect_classic;
        mEffectsDrawableIds[1] = R.drawable.transition_effect_classic_no_over_shoot;
        mEffectsDrawableIds[2] = R.drawable.transition_effect_fade_out;
        mEffectsDrawableIds[3] = R.drawable.transition_effect_fall_down;
        mEffectsDrawableIds[4] = R.drawable.transition_effect_cube;
        mEffectsDrawableIds[5] = R.drawable.transition_effect_left_page;
        mEffectsDrawableIds[6] = -1;
        mEffectsDrawableIds[7] = R.drawable.transition_effect_stack;
        mEffectsDrawableIds[8] = R.drawable.transition_effect_rotate;
    }

    public final boolean isValidType() {
        return this.mCurrentTypeIndex >= 0 && this.mCurrentTypeIndex < 11;
    }

    public int setTransitionType(int type) {
        this.mCurrentTypeIndex = type;
        this.mCurrentTypeIndexList.clear();
        this.mCurrentTypeIndexList.add(Integer.valueOf(this.mCurrentTypeIndex));
        addTransitionType(type, null);
        return getTransitionType();
    }

    private void addTransitionType(int type, TransitionEffect lastEffect) {
        if (isValidType() && this.mEffects[this.mCurrentTypeIndex] == null) {
            this.mEffects[this.mCurrentTypeIndex] = createEffect(type);
        }
        this.mEffects[this.mCurrentTypeIndex].mPreEffect = lastEffect;
    }

    public void appendTransitionType(int type) {
        this.mCurrentTypeIndex = type;
        TransitionEffect last = null;
        if (this.mCurrentTypeIndexList.size() > 0) {
            last = this.mEffects[((Integer) this.mCurrentTypeIndexList.get(this.mCurrentTypeIndexList.size() - 1)).intValue()];
        }
        addTransitionType(type, last);
        this.mCurrentTypeIndexList.add(Integer.valueOf(type));
    }

    public boolean removeTransitionType(int type) {
        if (type < 0 || type >= 11 || this.mEffects[type] == null) {
            return false;
        }
        this.mCurrentTypeIndex = type;
        this.mEffects[this.mCurrentTypeIndex].mPreEffect = null;
        boolean removed = this.mCurrentTypeIndexList.remove(Integer.valueOf(type));
        int size = this.mCurrentTypeIndexList.size();
        if (size <= 0) {
            return removed;
        }
        this.mCurrentTypeIndex = ((Integer) this.mCurrentTypeIndexList.get(size - 1)).intValue();
        return removed;
    }

    public int getTransitionType() {
        return this.mCurrentTypeIndex;
    }

    private TransitionEffect createEffect(int type) {
        switch (type) {
            case 0:
                return new TransitionEffectClassic();
            case 1:
                return new TransitionEffectClassicNoOverShoot();
            case 2:
                return new TransitionEffectCrossFade();
            case 3:
                return new TransitionEffectFallDown();
            case 4:
                return new TransitionEffectCube();
            case 5:
                return new TransitionEffectLeftPage();
            case 6:
                return new TransitionEffectRightPage();
            case 7:
                return new TransitionEffectStack();
            case 8:
                return new TransitionEffectRotate();
            case 9:
                return new TransitionEffectEditingMode();
            case 10:
                return new TransitionEffectNoType();
            default:
                return null;
        }
    }

    public void updateTransformation(float interpolation, float deltaX, float touchX, float touchY, View child, ViewGroup group) {
        if (isValidType()) {
            Iterator i$ = this.mCurrentTypeIndexList.iterator();
            while (i$.hasNext()) {
                this.mEffects[((Integer) i$.next()).intValue()].updateTransformation(interpolation, deltaX, touchX, touchY, child, group);
            }
        }
    }

    public float getOverShotTension() {
        if (isValidType()) {
            return this.mEffects[this.mCurrentTypeIndex].getOverShotTension();
        }
        return 0.0f;
    }

    public int getScreenSnapDuration() {
        if (isValidType()) {
            return this.mEffects[this.mCurrentTypeIndex].getScreenSnapDuration();
        }
        return 0;
    }

    public void resetTransformation(View child, ViewGroup group) {
        if (isValidType()) {
            this.mEffects[this.mCurrentTypeIndex].resetTransformation(child, group);
        }
    }

    public void onScreenOrientationChanged(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        DEFAULT_ROTATE_CAMERA_DISTANCE = (float) ((((Math.pow(2.0d, (double) dm.density) * ((double) dm.widthPixels)) / ((double) dm.density)) / 320.0d) * 1280.0d);
    }
}
