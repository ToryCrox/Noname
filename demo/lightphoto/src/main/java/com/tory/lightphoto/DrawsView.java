package com.tory.lightphoto;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.logging.XMLFormatter;


public class DrawsView extends View {

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;
    private Drawable mDistDrawable;
    private Drawable mSrcDrawable;
    private Paint mPaint;
    private Xfermode mXfermode;

    BitmapShader mBitmapShader;
    LinearGradient mLinearGradient;
    Bitmap mBitmap;
    Rect mRect = new Rect();

    public DrawsView(Context context) {
        super(context);
        init(null, 0);
    }

    public DrawsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DrawsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes

        // Set up onInterceptTouchEvent default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mDistDrawable = ContextCompat.getDrawable(getContext(), R.drawable.img006);
        mDistDrawable.setBounds(0, 0, mDistDrawable.getIntrinsicWidth(), mDistDrawable.getIntrinsicHeight());

        mSrcDrawable = ContextCompat.getDrawable(getContext(), R.drawable.mask);

        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img006);


        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.MIRROR, Shader.TileMode.MIRROR);

        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC);
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }


    private void invalidateTextPaintAndMeasurements() {

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mSrcDrawable.setBounds(0, 0, w, h);
        mLinearGradient = new LinearGradient(0, 0, w, h,
                new int[]{0x00ffffff, 0xffffffff},
                null, Shader.TileMode.REPEAT);

        mRect.set(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        //canvas.drawRect(0, 0, w, h, mPaint);

        //mPaint.setShader(mLinearGradient);
        mPaint.setColor(Color.WHITE);
        //mPaint.setStyle(Paint.Style.FILL);
        mPaint.setXfermode(mXfermode);
        canvas.drawRect(0, 0, w/2, h/2, mPaint);

        mPaint.setXfermode(null);
    }

}
