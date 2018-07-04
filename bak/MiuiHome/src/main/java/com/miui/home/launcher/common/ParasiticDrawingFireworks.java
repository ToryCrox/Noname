package com.miui.home.launcher.common;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import java.util.Random;

public class ParasiticDrawingFireworks extends ValueAnimator implements ParasiticDrawingObject {
    private static final Interpolator DEFAULT_INTERPOLATOR = new AccelerateInterpolator(0.6f);
    private static final float SPARK_INIT_SIZE = ((float) Utilities.getDipPixelSize(2));
    private static final float SPARK_MAX_SIZE = ((float) Utilities.getDipPixelSize(5));
    private static final float SPARK_MIN_SIZE = ((float) Utilities.getDipPixelSize(1));
    private static final float SPARK_START_POS_OFFSET = ((float) Utilities.getDipPixelSize(20));
    private Rect mArea;
    private View mHost;
    private Paint mPaint = new Paint();
    private Spark[] mSparks = new Spark[225];

    private class Spark {
        float a;
        float alpha;
        float b;
        int color;
        float edelay;
        float h;
        float r;
        float ri;
        float sdelay;
        float v;
        float x;
        float xi;
        float y;
        float yi;

        private Spark() {
        }

        public void update(float value) {
            float f = 0.0f;
            float f2 = value / 1.4f;
            if (f2 < this.sdelay || f2 > 1.0f - this.edelay) {
                this.alpha = 0.0f;
                return;
            }
            f2 = (f2 - this.sdelay) / ((1.0f - this.sdelay) - this.edelay);
            value = f2 * 1.4f;
            if (f2 >= 0.7f) {
                f = (f2 - 0.7f) / 0.3f;
            }
            this.alpha = 1.0f - f;
            float d = this.h * value;
            this.x = this.xi + d;
            this.y = (float) ((((double) this.yi) - (((double) this.a) * Math.pow((double) d, 2.0d))) - ((double) (this.b * d)));
            this.r = ParasiticDrawingFireworks.SPARK_INIT_SIZE + ((this.ri - ParasiticDrawingFireworks.SPARK_INIT_SIZE) * value);
        }
    }

    public ParasiticDrawingFireworks(View host, Bitmap colors, Rect area) {
        this.mArea = new Rect(area);
        Random r = new Random(System.currentTimeMillis());
        int xStep = colors.getWidth() / 17;
        int yStep = colors.getHeight() / 17;
        for (int y = 0; y < 15; y++) {
            for (int x = 0; x < 15; x++) {
                this.mSparks[(y * 15) + x] = generateSpark(colors.getPixel((x + 1) * xStep, (y + 1) * yStep), r);
            }
        }
        this.mHost = host;
        setFloatValues(new float[]{0.0f, 1.4f});
        setInterpolator(DEFAULT_INTERPOLATOR);
    }

    public void start() {
        super.start();
        this.mHost.invalidate(this.mArea);
    }

    private Spark generateSpark(int color, Random r) {
        Spark s = new Spark();
        s.color = color;
        s.r = SPARK_INIT_SIZE;
        if (r.nextFloat() < 0.2f) {
            s.ri = SPARK_INIT_SIZE + ((SPARK_MAX_SIZE - SPARK_INIT_SIZE) * r.nextFloat());
        } else {
            s.ri = SPARK_MIN_SIZE + ((SPARK_INIT_SIZE - SPARK_MIN_SIZE) * r.nextFloat());
        }
        float t = r.nextFloat();
        s.v = ((float) this.mArea.height()) * ((0.17999999f * r.nextFloat()) + 0.2f);
        s.v = t < 0.2f ? s.v : s.v + ((s.v * 0.2f) * r.nextFloat());
        s.h = (((float) this.mArea.height()) * (r.nextFloat() - 0.5f)) * 1.8f;
        float f = t < 0.2f ? s.h : t < 0.8f ? s.h * 0.6f : s.h * 0.3f;
        s.h = f;
        s.b = (4.0f * s.v) / s.h;
        s.a = (-s.b) / s.h;
        f = ((float) this.mArea.centerX()) + (SPARK_START_POS_OFFSET * (r.nextFloat() - 0.5f));
        s.xi = f;
        s.x = f;
        f = ((float) this.mArea.centerY()) + (SPARK_START_POS_OFFSET * (r.nextFloat() - 0.5f));
        s.yi = f;
        s.y = f;
        s.sdelay = 0.14f * r.nextFloat();
        s.edelay = 0.4f * r.nextFloat();
        s.alpha = 1.0f;
        return s;
    }

    public boolean draw(Canvas canvas) {
        if (!isStarted()) {
            return false;
        }
        for (Spark spark : this.mSparks) {
            spark.update(((Float) getAnimatedValue()).floatValue());
            if (spark.alpha > 0.0f) {
                this.mPaint.setColor(spark.color);
                this.mPaint.setAlpha((int) (((float) Color.alpha(spark.color)) * spark.alpha));
                canvas.drawCircle(spark.x, spark.y, spark.r, this.mPaint);
            }
        }
        this.mHost.invalidate();
        return true;
    }
}
