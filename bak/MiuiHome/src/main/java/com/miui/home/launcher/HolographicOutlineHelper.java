package com.miui.home.launcher;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.TableMaskFilter;

public class HolographicOutlineHelper {
    public static final int MAX_OUTER_BLUR_RADIUS;
    public static final int MIN_OUTER_BLUR_RADIUS;
    private static final MaskFilter sCoarseClipTable = TableMaskFilter.CreateClipTable(0, 200);
    private static final BlurMaskFilter sExtraThickInnerBlurMaskFilter;
    private static final BlurMaskFilter sExtraThickOuterBlurMaskFilter;
    private static final BlurMaskFilter sMediumInnerBlurMaskFilter;
    private static final BlurMaskFilter sMediumOuterBlurMaskFilter;
    private static final BlurMaskFilter sThickInnerBlurMaskFilter;
    private static final BlurMaskFilter sThickOuterBlurMaskFilter;
    private static final BlurMaskFilter sThinOuterBlurMaskFilter;
    private final Paint mAlphaClipPaint = new Paint();
    private final Paint mBlurPaint = new Paint();
    private final Paint mErasePaint = new Paint();
    private final Paint mHolographicPaint = new Paint();
    private int[] mTempOffset = new int[2];

    static {
        float scale = LauncherApplication.getScreenDensity();
        MIN_OUTER_BLUR_RADIUS = (int) (scale * 1.0f);
        MAX_OUTER_BLUR_RADIUS = (int) (scale * 12.0f);
        sExtraThickOuterBlurMaskFilter = new BlurMaskFilter(12.0f * scale, Blur.OUTER);
        sThickOuterBlurMaskFilter = new BlurMaskFilter(scale * 6.0f, Blur.OUTER);
        sMediumOuterBlurMaskFilter = new BlurMaskFilter(scale * 2.0f, Blur.OUTER);
        sThinOuterBlurMaskFilter = new BlurMaskFilter(scale * 1.0f, Blur.OUTER);
        sExtraThickInnerBlurMaskFilter = new BlurMaskFilter(scale * 6.0f, Blur.NORMAL);
        sThickInnerBlurMaskFilter = new BlurMaskFilter(4.0f * scale, Blur.NORMAL);
        sMediumInnerBlurMaskFilter = new BlurMaskFilter(scale * 2.0f, Blur.NORMAL);
    }

    public HolographicOutlineHelper() {
        this.mHolographicPaint.setFilterBitmap(true);
        this.mHolographicPaint.setAntiAlias(true);
        this.mBlurPaint.setFilterBitmap(true);
        this.mBlurPaint.setAntiAlias(true);
        this.mErasePaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
        this.mErasePaint.setFilterBitmap(true);
        this.mErasePaint.setAntiAlias(true);
        this.mAlphaClipPaint.setMaskFilter(TableMaskFilter.CreateClipTable(180, 255));
    }
}
