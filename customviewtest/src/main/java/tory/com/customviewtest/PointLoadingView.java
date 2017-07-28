package tory.com.customviewtest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import java.util.ArrayList;

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

    private Animation mAnim;
    private float mPrefraction;
    private boolean mAggregatedIsVisible;

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
        mAggregatedIsVisible = false;
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

        final int raduis = mPointRaduis;
        int color;
        Point point;
        for (CirclePoint cp : mShowCircles) {
            color = mCircleColors[cp.colorIndex];
            point = mVertexPoint[cp.index];
            mPaint.setColor(color);
            canvas.drawCircle(point.x, point.y, raduis, mPaint);
        }
        color = mCircleColors[0];
        point = mActivePoint;
        mPaint.setColor(color);
        canvas.drawCircle(point.x, point.y, raduis, mPaint);
    }


    private void initAnimator(){
        if(mAnim != null){
            return;
        }
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                super.applyTransformation(interpolatedTime, t);

                float fraction = interpolatedTime;
                if(mPrefraction > fraction + 0.5f){
                    mIndex = (mIndex + 1) % mPointSize;
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
                    }
                }

                Point start = mVertexPoint[mIndex];
                Point end = mVertexPoint[(mIndex+1) % mPointSize];
                int x = (int) (start.x + (end.x - start.x) * fraction);
                int y = (int) (start.y + (end.y - start.y) * fraction);
                mActivePoint.set(x, y);

                mPrefraction = fraction;
                postInvalidate();
            }
        };

        animation.setDuration(mEachDauration);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatMode(ValueAnimator.RESTART);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        mAnim = animation;
    }

    void startAnimation() {
        Log.d(TAG, "startAnimation ");
        if(mAnim == null){
            initAnimator();
        }
        mAnim.setStartTime(-1);
        startAnimation(mAnim);
        postInvalidate();
    }

    void stopAnimation() {
        if(mAnim != null){
            mAnim.cancel();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG, "onAttachedToWindow ");
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow ");
        stopAnimation();
        resetPointLocation();
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
