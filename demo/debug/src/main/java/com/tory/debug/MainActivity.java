package com.tory.debug;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(MFragment.TAG);
        if (fragment != null) {
            Log.d("MFragment", "MFragment remove");
            fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, new MFragment(), MFragment.TAG)
                .commitAllowingStateLoss();

    }

    private void drawTarget() {
        View image = findViewById(R.id.img);
        Log.d("drawTarget", "drawTarget image" + image.getScrollX() + ", " + image.getScrollY());
        ImageView imageView = findViewById(R.id.img1);
        Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setColor(0xff000000);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        int c = canvas.saveLayer(100, 100, 300, 300, paint);
        Log.d("drawTarget", "saveCount=" + c);
        canvas.clipRect(100, 100, 300, 300, Region.Op.INTERSECT);

        //canvas.drawARGB(0, 0, 0, 0);
        canvas.drawOval(0, 0, 200, 200, paint);

        canvas.drawCircle(250, 250, 30, paint);

        /*Bitmap b = createBitmap(paint);
        BitmapShader shader = new BitmapShader(b, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix = new Matrix();
        matrix.postTranslate(100, 100);
        shader.setLocalMatrix(matrix);

        paint.setShader(shader);*/
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        paint.setColor(0x33000000);
        canvas.drawRect(100, 100, 300, 300, paint);
        canvas.setBitmap(null);

        canvas.restoreToCount(c);
        paint.setXfermode(null);

        paint.setColor(Color.WHITE);
        canvas.drawCircle(250, 250, 30, paint);

        imageView.setImageBitmap(bitmap);
    }

    private Bitmap createBitmap(Paint paint) {
        Bitmap bitmap = Bitmap.createBitmap(202, 202, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRect(1, 1, 201, 201, paint);
        return bitmap;
    }


    public static Bitmap drawHotBitmap(@NonNull View view, @Nullable Rect rect, @ColorInt int bgColor) {
        if (rect == null) {
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


    @SuppressLint("ValidFragment")
    public static class MFragment extends Fragment {
        private static final String TAG = "MFragment";



        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("MFragment " + hashCode(), "MFragment onCreate");
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Log.d("MFragment " + hashCode(), "MFragment onCreateView");
            return new TextView(getContext());
        }

        @Override
        public void onResume() {
            Log.d("MFragment " + hashCode(), "MFragment onResume");
            super.onResume();
        }

        @Override
        public void onPause() {
            Log.d("MFragment " + hashCode(), "MFragment onPause");
            super.onPause();
        }

        @Override
        public void onDestroy() {
            Log.d("MFragment " + hashCode(), "MFragment onDestroy");
            super.onDestroy();
        }
    }
}
