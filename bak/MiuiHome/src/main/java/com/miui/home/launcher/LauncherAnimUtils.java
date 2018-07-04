package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import java.util.HashSet;
import java.util.Iterator;

public class LauncherAnimUtils {
    static HashSet<Animator> sAnimators = new HashSet();
    static AnimatorListener sEndAnimListener = new AnimatorListener() {
        public void onAnimationStart(Animator animation) {
            LauncherAnimUtils.sAnimators.add(animation);
        }

        public void onAnimationRepeat(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            LauncherAnimUtils.sAnimators.remove(animation);
        }

        public void onAnimationCancel(Animator animation) {
            LauncherAnimUtils.sAnimators.remove(animation);
        }
    };

    public static void cancelOnDestroyActivity(Animator a) {
        a.addListener(sEndAnimListener);
    }

    public static void onDestroyActivity() {
        Iterator i$ = new HashSet(sAnimators).iterator();
        while (i$.hasNext()) {
            Animator a = (Animator) i$.next();
            if (a.isRunning()) {
                a.cancel();
            } else {
                sAnimators.remove(a);
            }
        }
    }

    public static AnimatorSet createAnimatorSet() {
        AnimatorSet anim = new AnimatorSet();
        cancelOnDestroyActivity(anim);
        return anim;
    }

    public static ObjectAnimator ofFloat(View target, String propertyName, float... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setPropertyName(propertyName);
        anim.setFloatValues(values);
        cancelOnDestroyActivity(anim);
        FirstFrameAnimatorHelper firstFrameAnimatorHelper = new FirstFrameAnimatorHelper(anim, target);
        return anim;
    }

    public static ObjectAnimator ofPropertyValuesHolder(Object target, View view, PropertyValuesHolder... values) {
        ObjectAnimator anim = new ObjectAnimator();
        anim.setTarget(target);
        anim.setValues(values);
        cancelOnDestroyActivity(anim);
        FirstFrameAnimatorHelper firstFrameAnimatorHelper = new FirstFrameAnimatorHelper(anim, view);
        return anim;
    }
}
