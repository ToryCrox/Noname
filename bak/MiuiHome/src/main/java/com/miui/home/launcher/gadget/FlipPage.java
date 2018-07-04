package com.miui.home.launcher.gadget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.miui.home.R;
import java.util.HashMap;
import miui.maml.ResourceManager;

public class FlipPage extends FrameLayout {
    private static HashMap<String, MatrixWrap> FLIP_MATRIX_CACHE = new HashMap();
    private static final String[] NUM_RESIDS = new String[]{"flip_up_0.png", "flip_down_0.png", "flip_up_1.png", "flip_down_1.png", "flip_up_2.png", "flip_down_2.png", "flip_up_3.png", "flip_down_3.png", "flip_up_4.png", "flip_down_4.png", "flip_up_5.png", "flip_down_5.png", "flip_up_6.png", "flip_down_6.png", "flip_up_7.png", "flip_down_7.png", "flip_up_8.png", "flip_down_8.png", "flip_up_9.png", "flip_down_9.png"};
    private static final String[] PAGE_BACKGROUND = new String[]{"flip_lu.9.png", "flip_ld.9.png", "flip_ru.9.png", "flip_rd.9.png"};
    private FlipAnimation mAnimation;
    private Matrix[] mFlipMatrixArr;
    private boolean mIsUpSide;
    private ImageView mLeft;
    private int mPageType;
    private ImageView mRight;

    private class FlipAnimation extends Animation {
        private FlipAnimation() {
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int mPos = (int) (30.0f * interpolatedTime);
            if (mPos >= 30) {
                mPos = 29;
            }
            switch (FlipPage.this.mPageType) {
                case 0:
                    t.getMatrix().set(FlipPage.this.mFlipMatrixArr[mPos]);
                    return;
                case 1:
                    t.getMatrix().set(FlipPage.this.mFlipMatrixArr[mPos + 30]);
                    return;
                case 2:
                    t.getMatrix().set(FlipPage.this.mFlipMatrixArr[mPos + 60]);
                    return;
                case 3:
                    t.getMatrix().set(FlipPage.this.mFlipMatrixArr[mPos + 90]);
                    return;
                default:
                    return;
            }
        }
    }

    private static class MatrixWrap {
        public final Matrix[] mMatrixArr;

        public MatrixWrap(Matrix[] matrixArr) {
            this.mMatrixArr = matrixArr;
        }
    }

    public FlipPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAnimationCacheEnabled(false);
    }

    private void setupAnimation(int flipDelay) {
        this.mFlipMatrixArr = getFlipMatrixArr(((NinePatchDrawable) getBackground()).getIntrinsicWidth(), ((NinePatchDrawable) getBackground()).getIntrinsicHeight());
        if (this.mAnimation == null) {
            this.mAnimation = new FlipAnimation();
            this.mAnimation.setDuration(300);
        }
        this.mAnimation.setStartOffset((long) flipDelay);
        this.mAnimation.setInterpolator(this.mIsUpSide ? new LinearInterpolator() : new BounceInterpolator());
        startAnimation(this.mAnimation);
    }

    public void init(ResourceManager rm, int pageType, int number, int dist, int offset, int flipDelay) {
        boolean z;
        int side;
        int i = 4;
        int i2 = 0;
        setBackground(rm.getDrawable(getResources(), PAGE_BACKGROUND[pageType]));
        this.mPageType = pageType;
        if (this.mPageType == 0 || this.mPageType == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mIsUpSide = z;
        if (this.mIsUpSide) {
            side = 0;
        } else {
            side = 1;
        }
        this.mLeft.setImageBitmap(rm.getBitmap(NUM_RESIDS[((number / 10) * 2) + side]));
        this.mRight.setImageBitmap(rm.getBitmap(NUM_RESIDS[((number % 10) * 2) + side]));
        if (this.mIsUpSide) {
            if (offset != dist) {
                i2 = 4;
            }
            setVisibility(i2);
            if (offset != dist) {
                setupAnimation((offset + 1) * flipDelay);
                return;
            }
            return;
        }
        if (offset == 0 || dist == offset) {
            i = 0;
        }
        setVisibility(i);
        if (offset != 0) {
            setupAnimation((offset * flipDelay) + 300);
        }
    }

    protected void onFinishInflate() {
        this.mLeft = (ImageView) findViewById(R.id.left);
        this.mRight = (ImageView) findViewById(R.id.right);
    }

    private static Matrix[] getFlipMatrixArr(int w, int h) {
        String key = w + "_" + h;
        MatrixWrap wrap = (MatrixWrap) FLIP_MATRIX_CACHE.get(key);
        if (wrap != null) {
            return wrap.mMatrixArr;
        }
        Matrix[] matrixArr = new Matrix[120];
        Camera cam = new Camera();
        int transX = w;
        int transY = h;
        computeMatirx(cam, matrixArr, 0, -transX, transY, false);
        computeMatirx(cam, matrixArr, 30, -transX, transY, true);
        computeMatirx(cam, matrixArr, 60, 0, transY, false);
        computeMatirx(cam, matrixArr, 90, 0, transY, true);
        FLIP_MATRIX_CACHE.put(key, new MatrixWrap(matrixArr));
        return matrixArr;
    }

    private static void computeMatirx(Camera cam, Matrix[] matrix, int matOffset, int transX, int transY, boolean isOppo) {
        int i = 0;
        while (i < 30) {
            cam.save();
            Matrix m = new Matrix();
            cam.rotateX((((float) (isOppo ? 30 - i : -(i + 1))) * 90.0f) / 30.0f);
            cam.getMatrix(m);
            m.preTranslate((float) transX, isOppo ? 0.0f : (float) (-transY));
            m.postTranslate((float) (-transX), isOppo ? 0.0f : (float) transY);
            matrix[matOffset + i] = m;
            cam.restore();
            i++;
        }
    }
}
