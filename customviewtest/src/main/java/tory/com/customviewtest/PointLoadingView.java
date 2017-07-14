package tory.com.customviewtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class PointLoadingView extends View {

    private static final String TAG = "PointLoadingView";


    private Paint mPaint;
    private int mPointRaduis;
    private Point mActivePoint;
    private Point[] mVertexPoint;
    private int mPointSize;
    private int mIndex;
    private Rect mPointRect;

    private long mEachDauration;

    private int[] mCircleColors;
    private ArrayList<CirclePoint> mHideCircles;
    private ArrayList<CirclePoint> mShowCircles;

    private ValueAnimator mAnim;

    public PointLoadingView(Context context) {
        super(context);
        init(null, 0);
    }

    public PointLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PointLoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mPointRaduis = dp2px(getContext(), 5);
        mEachDauration = 350;

        initPoints();
    }

    private void initPoints(){
        int[] colors = new int[]{0xff0bc459, 0xfff6bb3c, 0xff1381e8, 0xfff15e3c};

        mCircleColors = colors;
        mPointRect = new Rect();
        mPointSize = colors.length;
        mActivePoint = new Point();
        mVertexPoint = new Point[mPointSize];
        for (int i = 0; i < mPointSize; i++) {
            mVertexPoint[i] = new Point();
        }
        mHideCircles = new ArrayList<>(mPointSize - 1);
        mShowCircles = new ArrayList<>(mPointSize - 1);
        resetPointLocation();
    }

    private void resetPointLocation(){
        mIndex = 0;
        mHideCircles.clear();
        mShowCircles.clear();
        for (int i = 1; i < mPointSize; i++) {
            CirclePoint cp = new CirclePoint();
            cp.index = i;
            cp.colorIndex = i;
            mShowCircles.add(cp);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int pointRaduis = mPointRaduis;

        mPointRect.set(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom);
        mPointRect.inset(pointRaduis, pointRaduis);

        mVertexPoint[0].set(mPointRect.left, mPointRect.top);
        mVertexPoint[1].set(mPointRect.right, mPointRect.top);
        mVertexPoint[2].set(mPointRect.right, mPointRect.bottom);
        mVertexPoint[3].set(mPointRect.left, mPointRect.bottom);

        mActivePoint.set(mVertexPoint[0].x, mVertexPoint[0].y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        int color = mCircleColors[0];
        Point point = mActivePoint;
        int raduis = mPointRaduis;

        mPaint.setColor(color);
        canvas.drawCircle(point.x, point.y, raduis, mPaint);
        for (CirclePoint cp : mShowCircles) {
            color = mCircleColors[cp.colorIndex];
            point = mVertexPoint[cp.index];
            mPaint.setColor(color);
            canvas.drawCircle(point.x, point.y, raduis, mPaint);
        }
        color = mCircleColors[0];
        point = mActivePoint;
        raduis = mPointRaduis;

        mPaint.setColor(color);
        canvas.drawCircle(point.x, point.y, raduis, mPaint);
    }


    private void easureAnimator(){
        if(mAnim != null){
            return;
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                Point start = mVertexPoint[mIndex];
                Point end = mVertexPoint[(mIndex+1) % mPointSize];
                int x = (int) (start.x + (end.x - start.x) * fraction);
                int y = (int) (start.y + (end.y - start.y) * fraction);
                mActivePoint.set(x, y);

                Log.d(TAG, "fraction="+fraction+", mActivePoint="+mActivePoint);
                if(fraction >= 1){

                }
                postInvalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mIndex = (mIndex + 1) % mPointSize;
                //CirclePoint cp = mShowCircles.remove(0);
                CirclePoint cp = mShowCircles.get(0);
                if(cp.index == mIndex){
                    mShowCircles.remove(0);
                    mHideCircles.add(cp);
                    if(mShowCircles.isEmpty()){
                        cp  = mHideCircles.remove(0);
                        cp.index = mIndex;
                        mShowCircles.add(cp);
                    }
                }else if(!mHideCircles.isEmpty()){
                    cp  = mHideCircles.remove(0);
                    cp.index = mIndex;
                    mShowCircles.add(cp);
                }else if(mHideCircles.isEmpty()){
                    Log.e(TAG, "repeat mHideCircles.isEmpty() mIndex="+mIndex );
                }
                Log.d(TAG, "repeat mIndex="+mIndex + ", mShowCircles="+mShowCircles);

            }
        });
        animator.setDuration(mEachDauration);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        mAnim = animator;
    }

    void startAnimation() {
        if (getVisibility() != VISIBLE || getWindowVisibility() != VISIBLE) {
            return;
        }
        //resetPointLocation();
        easureAnimator();
        //mAnim.cancel();
        mAnim.start();
        postInvalidate();
    }

    void stopAnimation() {
        if(mAnim != null){
            mAnim.end();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public static class CirclePoint{
        public int index;
        public int colorIndex;

        @Override
        public String toString() {
            return "CirclePoint{" +
                    "index=" + index +
                    ", colorIndex=" + colorIndex +
                    '}';
        }
    }

}
