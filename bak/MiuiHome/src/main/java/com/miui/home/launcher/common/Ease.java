package com.miui.home.launcher.common;

import android.animation.TimeInterpolator;

public class Ease {

    public static class Cubic {
        public static final TimeInterpolator easeIn = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input /= 1.0f;
                return (((1.0f * input) * input) * input) + 0.0f;
            }
        };
        public static final TimeInterpolator easeInOut = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input /= 0.5f;
                if (input < 1.0f) {
                    return (((0.5f * input) * input) * input) + 0.0f;
                }
                input -= 2.0f;
                return ((((input * input) * input) + 2.0f) * 0.5f) + 0.0f;
            }
        };
        public static final TimeInterpolator easeOut = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input = (input / 1.0f) - 1.0f;
                return ((((input * input) * input) + 1.0f) * 1.0f) + 0.0f;
            }
        };
    }

    public static class Quint {
        public static final TimeInterpolator easeIn = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input /= 1.0f;
                return (((((1.0f * input) * input) * input) * input) * input) + 0.0f;
            }
        };
        public static final TimeInterpolator easeInOut = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input /= 0.5f;
                if (input < 1.0f) {
                    return (((((0.5f * input) * input) * input) * input) * input) + 0.0f;
                }
                input -= 2.0f;
                return ((((((input * input) * input) * input) * input) + 2.0f) * 0.5f) + 0.0f;
            }
        };
        public static final TimeInterpolator easeOut = new TimeInterpolator() {
            public float getInterpolation(float input) {
                input = (input / 1.0f) - 1.0f;
                return ((((((input * input) * input) * input) * input) + 1.0f) * 1.0f) + 0.0f;
            }
        };
    }
}
