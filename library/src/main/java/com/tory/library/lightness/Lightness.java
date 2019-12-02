package com.tory.library.lightness;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tao.xu2 on 2017/4/27.
 */

public class Lightness {

    public static final int LIGHT = 0;
    public static final int DARK = 1;
    public static final int UNKNOWN = 2;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LIGHT, DARK, UNKNOWN})
    public @interface LightnessValues {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LIGHT, DARK})
    public @interface LightnessNoUnknown {
    }
}
