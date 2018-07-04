package com.miui.home.launcher.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.miui.home.launcher.DeviceConfig;
import java.io.IOException;
import java.lang.ref.WeakReference;
import miui.graphics.BitmapFactory;
import miui.util.InputStreamLoader;

public class ScalableImageView extends ImageView {
    private static final int MAX_PIXELS = ((DeviceConfig.getDeviceWidth() * 2) * DeviceConfig.getDeviceHeight());
    private Bitmap mBitmap = null;
    private WeakReference<Callbacks> mCallbacks;
    private int mCurrentMode = 0;
    private DisplayMetrics mDisplayMetrics;
    private final Matrix mImageMatrix = new Matrix();
    private int mImageRotation = 0;
    private int mLeftOffset;
    private final Matrix mMatrixBack = new Matrix();
    private float mMinScale;
    SparseArray<Float> mMotionDataX = new SparseArray();
    SparseArray<Float> mMotionDataY = new SparseArray();
    Paint mPaint = new Paint(3);
    private int mRightOffset;
    private PointF mTouchDownPoint = new PointF();
    private float mTransXTotal = 0.0f;

    public interface Callbacks {
        void onImageMatrixChanged();

        void onImageMatrixConfirm();
    }

    public ScalableImageView(Context context) {
        super(context);
        setLayerType(1, null);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(1, null);
    }

    public int getImageRotation() {
        return this.mImageRotation;
    }

    public Matrix getImageMatrix() {
        return this.mImageMatrix;
    }

    public Bitmap getImageBitmap() {
        return this.mBitmap;
    }

    public void setOffsets(int leftOffset, int rightOffset) {
        this.mLeftOffset = leftOffset;
        this.mRightOffset = rightOffset;
    }

    public boolean init(Context context, Uri uri, Callbacks callbacks) {
        this.mCallbacks = new WeakReference(callbacks);
        Bitmap b = null;
        try {
            b = BitmapFactory.decodeBitmap(context, uri, MAX_PIXELS, false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
            return false;
        }
        setImageBitmap(b);
        this.mBitmap = b;
        setupView();
        InputStreamLoader is = new InputStreamLoader(context, uri);
        this.mImageRotation = Utilities.getImageRotation(is.get());
        is.close();
        if (this.mBitmap != null) {
            return true;
        }
        return false;
    }

    public void recycleBitmap() {
        setImageBitmap(null);
        if (this.mBitmap != null) {
            this.mBitmap.recycle();
            this.mBitmap = null;
        }
    }

    public int getImageWidth() {
        return (this.mImageRotation == 270 || this.mImageRotation == 90) ? this.mBitmap.getHeight() : this.mBitmap.getWidth();
    }

    public int getImageHeight() {
        return (this.mImageRotation == 270 || this.mImageRotation == 90) ? this.mBitmap.getWidth() : this.mBitmap.getHeight();
    }

    public boolean setMinLayoutSize(int width, int height, boolean firstTime) {
        if (this.mBitmap == null) {
            return false;
        }
        this.mMinScale = Math.max(((float) width) / ((float) getImageWidth()), ((float) height) / ((float) getImageHeight()));
        if (this.mMinScale > 10.0f) {
            return false;
        }
        if (firstTime) {
            centerImage(width, height, false);
        } else {
            correctViewMatrix();
        }
        return true;
    }

    private void centerImage(int width, int height, boolean withAnim) {
        this.mImageMatrix.setScale(this.mMinScale, this.mMinScale);
        this.mImageMatrix.postRotate((float) this.mImageRotation);
        Matrix m = new Matrix();
        m.set(this.mImageMatrix);
        RectF rect = new RectF(0.0f, 0.0f, (float) this.mBitmap.getWidth(), (float) this.mBitmap.getHeight());
        m.mapRect(rect);
        float deltaX = (((((float) width) - (((float) getImageWidth()) * this.mMinScale)) / 2.0f) - ((float) this.mLeftOffset)) - rect.left;
        this.mImageMatrix.postTranslate(deltaX, ((((float) height) - (((float) getImageHeight()) * this.mMinScale)) / 2.0f) - rect.top);
        setImageMatrix(this.mImageMatrix);
    }

    public void setupView() {
        this.mDisplayMetrics = getContext().getResources().getDisplayMetrics();
        setScaleType(ScaleType.MATRIX);
        setImageMatrix(this.mImageMatrix);
    }

    private void onImageMatrixConfirmed() {
        Callbacks callbacks = this.mCallbacks != null ? (Callbacks) this.mCallbacks.get() : null;
        if (callbacks != null) {
            callbacks.onImageMatrixConfirm();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & 255) {
            case 0:
            case 5:
                this.mMatrixBack.set(this.mImageMatrix);
                resetData(event);
                if (event.getPointerCount() == 1) {
                    this.mTouchDownPoint.x = event.getX(0);
                    this.mTouchDownPoint.y = event.getY(0);
                    break;
                }
                break;
            case 1:
                if (!hasOnClickListeners() || disBetweenPoints(this.mTouchDownPoint, new PointF(event.getX(0), event.getY(0))) >= 10.0f) {
                    if (this.mBitmap != null) {
                        correctViewMatrix();
                        break;
                    }
                }
                performClick();
                break;
                break;
            case 2:
                if (this.mBitmap != null) {
                    this.mMatrixBack.set(this.mImageMatrix);
                    if (disBetweenPoints(this.mTouchDownPoint, new PointF(event.getX(0), event.getY(0))) > 10.0f) {
                        Callbacks callbacks = this.mCallbacks != null ? (Callbacks) this.mCallbacks.get() : null;
                        if (callbacks != null) {
                            callbacks.onImageMatrixChanged();
                        }
                    }
                    if (this.mCurrentMode == 1) {
                        dampMatrixTransAndScale(event.getX(0) - ((Float) this.mMotionDataX.get(0)).floatValue(), event.getY(0) - ((Float) this.mMotionDataY.get(0)).floatValue(), 1.0f, 1.0f);
                    } else if (this.mCurrentMode == 2 && event.getPointerCount() >= 2) {
                        float deltaX = ((Float) this.mMotionDataX.get(0)).floatValue() - ((Float) this.mMotionDataX.get(1)).floatValue();
                        float deltaY = ((Float) this.mMotionDataY.get(0)).floatValue() - ((Float) this.mMotionDataY.get(1)).floatValue();
                        float currDeltaX = event.getX(0) - event.getX(1);
                        float currDeltaY = event.getY(0) - event.getY(1);
                        float scale = ((float) Math.sqrt((double) ((currDeltaX * currDeltaX) + (currDeltaY * currDeltaY)))) / ((float) Math.sqrt((double) ((deltaX * deltaX) + (deltaY * deltaY))));
                        dampMatrixTransAndScale(0.0f, 0.0f, scale, scale);
                    }
                    setImageMatrix(this.mImageMatrix);
                    resetData(event);
                    break;
                }
                break;
        }
        return true;
    }

    private void correctTouchMode(MotionEvent event) {
        if (event.getPointerCount() == 1 || this.mMotionDataX.size() == 1 || this.mMotionDataY.size() == 1) {
            this.mCurrentMode = 1;
            return;
        }
        float deltaX0 = ((Float) this.mMotionDataX.get(0)).floatValue() - event.getX(0);
        float deltaY0 = ((Float) this.mMotionDataY.get(0)).floatValue() - event.getY(0);
        float deltaX1 = ((Float) this.mMotionDataX.get(1)).floatValue() - event.getX(1);
        float deltaY1 = ((Float) this.mMotionDataY.get(1)).floatValue() - event.getY(1);
        if (deltaX0 * deltaX1 < 0.0f || deltaY0 * deltaY1 < 0.0f || Math.abs(deltaX0 - deltaX1) >= ((float) Utilities.getDipPixelSize(4)) || Math.abs(deltaY0 - deltaY1) >= ((float) Utilities.getDipPixelSize(4))) {
            this.mCurrentMode = 2;
        } else {
            this.mCurrentMode = 1;
        }
    }

    private void resetData(MotionEvent event) {
        correctTouchMode(event);
        this.mMotionDataX.clear();
        this.mMotionDataY.clear();
        int pointCount = Math.min(2, event.getPointerCount());
        for (int i = 0; i < pointCount; i++) {
            this.mMotionDataX.put(i, Float.valueOf(event.getX(i)));
            this.mMotionDataY.put(i, Float.valueOf(event.getY(i)));
        }
    }

    protected void verifyImageOffsets(boolean horizontal, boolean vertical) {
        if (this.mBitmap != null) {
            Matrix m = new Matrix();
            m.set(this.mImageMatrix);
            RectF rect = new RectF(0.0f, 0.0f, (float) this.mBitmap.getWidth(), (float) this.mBitmap.getHeight());
            m.mapRect(rect);
            float height = rect.height();
            float deltaX = 0.0f;
            float deltaY = 0.0f;
            if (vertical) {
                int screenHeight = this.mDisplayMetrics.heightPixels;
                if (height < ((float) screenHeight)) {
                    deltaY = ((((float) screenHeight) - height) / 2.0f) - rect.top;
                } else if (rect.top > 0.0f) {
                    deltaY = -rect.top;
                } else if (rect.bottom < ((float) screenHeight)) {
                    deltaY = ((float) getHeight()) - rect.bottom;
                }
            }
            if (horizontal) {
                int screenWidth = this.mDisplayMetrics.widthPixels;
                if (rect.left > ((float) (-this.mLeftOffset))) {
                    deltaX = ((float) (-this.mLeftOffset)) - rect.left;
                } else if (rect.right < ((float) (this.mRightOffset + screenWidth))) {
                    deltaX = ((float) (this.mRightOffset + screenWidth)) - rect.right;
                }
            }
            if (deltaX == 0.0f && deltaY == 0.0f) {
                onImageMatrixConfirmed();
            } else {
                startTransAnim(deltaX, deltaY, false);
            }
        }
    }

    private void startTransAnim(float deltaX, float deltaY, final boolean moveBackToStart) {
        float[] p = new float[9];
        this.mImageMatrix.getValues(p);
        final float fromX = p[2];
        final float toX = fromX + deltaX;
        final float fromY = p[5];
        final float toY = fromY + deltaY;
        ValueAnimator transAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        transAnim.setInterpolator(new DecelerateInterpolator());
        transAnim.setDuration((long) Math.max(Math.min(500, Math.max((int) Math.abs(((toX - fromX) * 300.0f) / ((float) DeviceConfig.getDeviceWidth())), (int) Math.abs(((toY - fromY) * 300.0f) / ((float) DeviceConfig.getDeviceHeight())))), 200));
        transAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                float postX = fromX + ((toX - fromX) * value);
                float postY = fromY + ((toY - fromY) * value);
                float[] p = new float[9];
                ScalableImageView.this.mImageMatrix.getValues(p);
                ScalableImageView.this.mImageMatrix.postTranslate(postX - p[2], postY - p[5]);
                ScalableImageView.this.setImageMatrix(ScalableImageView.this.mImageMatrix);
            }
        });
        transAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (!moveBackToStart) {
                    ScalableImageView.this.onImageMatrixConfirmed();
                }
            }
        });
        transAnim.start();
    }

    private float getCurScaleX(float[] p) {
        if (this.mImageRotation == 90) {
            return p[3];
        }
        if (this.mImageRotation == 180) {
            return -p[0];
        }
        if (this.mImageRotation == 270) {
            return -p[3];
        }
        return p[0];
    }

    private void correctViewMatrix() {
        float from;
        float to;
        float[] p = new float[9];
        this.mImageMatrix.getValues(p);
        float curScale = getCurScaleX(p);
        if (curScale < this.mMinScale) {
            from = curScale;
            to = this.mMinScale;
        } else if (curScale > 10.0f) {
            from = curScale;
            to = 10.0f;
        } else {
            verifyImageOffsets(true, true);
            return;
        }
        ValueAnimator scaleAnim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        scaleAnim.setInterpolator(new DecelerateInterpolator());
        scaleAnim.setDuration((long) Math.max(Math.min(500, (int) Math.abs(300.0f * (to - from))), 200));
        scaleAnim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float toScale = from + ((to - from) * ((Float) animation.getAnimatedValue()).floatValue());
                float[] p = new float[9];
                ScalableImageView.this.mImageMatrix.getValues(p);
                float scale = toScale / ScalableImageView.this.getCurScaleX(p);
                ScalableImageView.this.mImageMatrix.postScale(scale, scale, (float) (DeviceConfig.getDeviceWidth() / 2), (float) (DeviceConfig.getDeviceHeight() / 2));
                ScalableImageView.this.setImageMatrix(ScalableImageView.this.mImageMatrix);
            }
        });
        scaleAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ScalableImageView.this.verifyImageOffsets(true, true);
            }
        });
        scaleAnim.start();
    }

    private void dampMatrixTransAndScale(float transX, float transY, float scaleX, float scaleY) {
        Matrix m = new Matrix();
        m.set(this.mImageMatrix);
        RectF rect = new RectF(0.0f, 0.0f, (float) this.mBitmap.getWidth(), (float) this.mBitmap.getHeight());
        m.mapRect(rect);
        float dampRatioX = 0.0f;
        float dampRatioY = 0.0f;
        if (rect.left > 0.0f && transX >= 0.0f) {
            dampRatioX = rect.left / 300.0f;
        } else if (rect.right < ((float) this.mDisplayMetrics.widthPixels) && transX <= 0.0f) {
            dampRatioX = (((float) this.mDisplayMetrics.widthPixels) - rect.right) / 300.0f;
        }
        dampRatioX = Math.min(1.0f, dampRatioX);
        if (rect.top > 0.0f && transY >= 0.0f) {
            dampRatioY = rect.top / 300.0f;
        } else if (rect.bottom < ((float) this.mDisplayMetrics.heightPixels) && transY <= 0.0f) {
            dampRatioY = (((float) this.mDisplayMetrics.heightPixels) - rect.bottom) / 300.0f;
        }
        dampRatioY = Math.min(1.0f, dampRatioY);
        this.mImageMatrix.postTranslate((1.0f - dampRatioX) * transX, (1.0f - dampRatioY) * transY);
        if (scaleX >= 1.0f) {
            dampRatioX = 0.0f;
        }
        if (scaleY >= 1.0f) {
            dampRatioY = 0.0f;
        }
        float scaleDamp = Math.max(dampRatioX, dampRatioY);
        this.mImageMatrix.postScale(((1.0f - scaleX) * scaleDamp) + scaleX, ((1.0f - scaleY) * scaleDamp) + scaleY, (float) (DeviceConfig.getDeviceWidth() / 2), (float) (DeviceConfig.getDeviceHeight() / 2));
    }

    private float disBetweenPoints(PointF p1, PointF p2) {
        float x = p1.x - p2.x;
        float y = p1.y - p2.y;
        return FloatMath.sqrt((x * x) + (y * y));
    }
}
