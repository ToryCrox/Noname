package com.tory.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BlurUtil {
    private static final String TAG = "BlurUtil";

    private final Context mContext;

    public BlurUtil(Context mContext) {
        this.mContext = mContext;
    }

    public Bitmap blur(Bitmap bitmap, int width, int height, int radius, int scaleFactor){
        if(bitmap == null) return null;

        Bitmap overlay = Bitmap.createBitmap( (width / scaleFactor),
                (height / scaleFactor), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(overlay);
        //canvas.translate(-view.getLeft()/scaleFactor, -view.getTop()/scaleFactor);
        //canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        cropCanvas(canvas, bitmap, overlay);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        overlay = doBlur(overlay,radius);
        return overlay;
    }

    private void cropCanvas(Canvas canvas,Bitmap bitmap,Bitmap overlay){
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();

        //目标长宽
        int oWidth = overlay.getWidth();
        int oHeight = overlay.getHeight();

        int scaledWidth = bWidth * oHeight / bHeight;//按高度缩放后的宽度值
        int scaledHeight = bHeight * oWidth / bWidth;//按宽度缩放后的高度值

        if(scaledWidth <= oWidth){//按宽度进行缩放
            scaledWidth = oWidth;
        }else{
            scaledHeight = oHeight;
        }

        float scale = (float)scaledWidth / bWidth;

        int x = scaledWidth > oWidth ? (scaledWidth - oWidth)/2 : 0;
        int y = scaledHeight >oHeight ? (scaledHeight - oHeight)/2 : 0;
        canvas.translate(-x, -y);
        canvas.scale(scale, scale);
    }

    public Bitmap doBlur(Bitmap bitmap,int radius){
        Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // Instantiate a new Renderscript
        RenderScript rs = RenderScript.create(mContext);

        // Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs,
                Element.U8_4(rs));

        // Create the Allocations (in/out) with the Renderscript and the in/out
        // bitmaps
        Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

        // Set the radius of the blur
        blurScript.setRadius(radius);

        // Perform the Renderscript
        blurScript.setInput(allIn);
        blurScript.forEach(allOut);

        // Copy the final bitmap created by the out Allocation to the outBitmap
        allOut.copyTo(outBitmap);

        // recycle the original bitmap
        bitmap.recycle();

        // After finishing everything, we destroy the Renderscript.
        rs.destroy();

        return outBitmap;
    }

}