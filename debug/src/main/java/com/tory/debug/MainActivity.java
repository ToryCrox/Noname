package com.tory.debug;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TimeRecorder.begin("MainActivity#onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TimeRecorder.end("MainActivity#onCreate");

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                drawTarget();
            }
        });
    }

    private void drawTarget() {
        View image = findViewById(R.id.img);
        Log.d("drawTarget", "drawTarget image"+image.getScrollX() + ", "+image.getScrollY());
        ImageView imageView = findViewById(R.id.img1);
        int left = image.getScrollX();
        int top = image.getScrollY();
        Rect rect = new Rect(left, top, left + image.getWidth(), top + image.getHeight());
        Bitmap bitmap = drawHotBitmap(image, rect, Color.WHITE);
        imageView.setImageBitmap(bitmap);

    }


    public static Bitmap drawHotBitmap(@NonNull View view, @Nullable Rect rect, @ColorInt int bgColor){
        if (rect == null){
            rect = new Rect(0, 0, view.getWidth(), view.getHeight());
        }
        final int radius = 16;
        Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rectF, radius, radius, paint);
        canvas.translate(-rect.left, -rect.top);
        view.draw(canvas);
        canvas.setBitmap(null);
        return bitmap;
    }
}
