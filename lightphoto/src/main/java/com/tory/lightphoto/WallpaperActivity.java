package com.tory.lightphoto;

import android.app.WallpaperManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tory.library.lightness.ColorUtils;
import com.tory.library.lightness.Lightness;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.GrayImageUtil;
import com.tory.library.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WallpaperActivity extends AppCompatActivity {

    static {
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    private static final String TAG = "WallpaperActivity";
    private static final int RESULT_LOAD_IMAGE = 333;

    private static final int REUEST_CODE_STORAGE = 322;

    @BindView(R.id.main)
    protected View mMain;

    @BindView(R.id.wallpaper)
    ImageView mWallpaperView;

    @BindView(R.id.text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallpaper);
        ButterKnife.bind(this);

        setWallapers();
        
        requestPermissions();
    }

    private void setWallapers() {
        WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
        Drawable wallpaper = wm.getDrawable();
        wm.forgetLoadedWallpaper();
        Bitmap bitmap = ImageUtils.drawableToBitmap(wallpaper);
        setBitmap(bitmap);

    }

    private void requestPermissions() {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REUEST_CODE_STORAGE);
        }
    }


    @OnClick(R.id.photo_pick)
    public void pickPhoto(){
        Log.d(TAG, "pickPhoto: ");
        //PhotoPickUtils.startPick().setPhotoCount(1).start(this);
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @OnClick(R.id.wallpaper_save)
    public void saveWallpaper(){
        Log.d(TAG, "saveWallpaper: ");
        WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
        Drawable drawable = wm.getDrawable();
        Bitmap bitmap = ImageUtils.drawableToBitmap(drawable);
        wm.forgetLoadedWallpaper();

        setBitmap(bitmap);

        String filename = "wallpaper"+ new Random().nextInt(100) + ".jpg";

        File dir = new File(FileUtils.getSDPath(), "wallpapers");
        if(!dir.exists()){
            dir.mkdirs();
        }
        File file = new File(dir,filename);
        boolean suc = FileUtils.saveBitmap(bitmap,file.getAbsolutePath());
        Log.d(TAG, "saveWallpaper: suc="+suc);
    }

    @OnClick(R.id.turn_to_activity)
    public void turnToActivity(){
        startActivity(new Intent(this, VectorTestActivity.class));
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode="+requestCode+",resultCode="+resultCode+",data="+data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            pickSuccess(picturePath);
            // String picturePath contains the path of selected Image
        }

    }

    private void pickSuccess(String filepath) {
        String photoPath = filepath;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        setBitmap(bitmap);
    }


    private void setBitmap(Bitmap bitmap){
        mWallpaperView.setImageBitmap(bitmap);
        Palette palette = Palette.from(bitmap).clearFilters().generate();
        Palette.Swatch swatch = palette.getDominantSwatch();
        Log.d(TAG, "setBitmap swatch="+swatch);

        long t2 = SystemClock.uptimeMillis();
        boolean isLight = LightnessUtils.checkLightness(bitmap) == Lightness.LIGHT;
        int color = isLight ? Color.BLACK : Color.WHITE;
        int color2 = isLight ? Color.WHITE : Color.BLACK;


        StringBuilder sb = new StringBuilder();
        ArrayList<String> strs = new ArrayList<>();
        ArrayList<ForegroundColorSpan> css = new ArrayList<>();
        strs.add("这是通过checkLightness取的颜色, Hsl="+Arrays.toString(swatch.getHsl())
                +", 耗时:"+(SystemClock.uptimeMillis() - t2) + "\n");
        css.add(new ForegroundColorSpan(color));


        long t1 = SystemClock.uptimeMillis();
        //boolean isLight = checkLightness(bitmap);
        int colorMode = getBitmapColorMode(bitmap, getSampleRatio(bitmap));
        isLight = colorMode == BITMAP_COLOR_MODE_LIGHT;
        strs.add("这是通过decodeColor获取的颜色, colorMode = "+colorMode+", 耗时:"+(SystemClock.uptimeMillis() - t1) + "\n");
        css.add(new ForegroundColorSpan(isLight ? Color.BLACK : Color.WHITE));

        Map<String, Target> map = new LinkedHashMap<>(6);
        map.put("LIGHT_VIBRANT", Target.LIGHT_VIBRANT);
        map.put("VIBRANT", Target.VIBRANT);
        map.put("DARK_VIBRANT", Target.DARK_VIBRANT);
        map.put("LIGHT_MUTED", Target.LIGHT_MUTED);
        map.put("MUTED", Target.MUTED);
        map.put("DARK_MUTED", Target.DARK_MUTED);

        for(Map.Entry<String, Target> entry : map.entrySet()){
            String name = entry.getKey();
            Target target = entry.getValue();

            Palette.Swatch sw = palette.getSwatchForTarget(target);
            if(sw == null){
                continue;
            }
            strs.add("这是通过"+name+" Swatch获取的颜色titlecolor\n");
            css.add(new ForegroundColorSpan(sw.getTitleTextColor()));
            strs.add("这是通过"+name+"Swatch获取的颜色BodyColor\n");
            css.add(new ForegroundColorSpan(sw.getBodyTextColor()));
        }
        /*List<Palette.Swatch> swatchs = palette.getSwatches();
        for (Palette.Swatch swatch1 : swatchs) {
            strs.add("这是通过Swatch获取的颜色titlecolor\n");
            css.add(new ForegroundColorSpan(swatch1.getTitleTextColor()));
            strs.add("这是通过Swatch获取的颜色BodyColor\n");
            css.add(new ForegroundColorSpan(swatch1.getBodyTextColor()));
        }*/

        SpannableString st = new SpannableString(TextUtils.join("",strs));
        int index = 0;
        for (int i = 0; i < css.size(); i++) {
            String str = strs.get(i);
            st.setSpan(css.get(i),index, index + str.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            index += str.length();
        }
        mTextView.setText(st);
    }

    public static boolean checkLightness(Bitmap bitmap){
        int mode = getBitmapColorMode(bitmap, getSampleRatio(bitmap));
        return mode == BITMAP_COLOR_MODE_LIGHT;
    }

    private static int getSampleRatio(Bitmap b) {
        if (b.getWidth() < 400 || b.getHeight() < 400) {
            return 1;
        }
        return 5;
    }


    public static Bitmap scaleBitmap(Bitmap bitmap, int i, int i2) {
        if (bitmap == null) {
            return null;
        }
        if (bitmap.getWidth() == i && bitmap.getHeight() == i2) {
            return bitmap;
        }
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        if (bitmap.getConfig() != null) {
            config = bitmap.getConfig();
        }
        Bitmap createBitmap = Bitmap.createBitmap(i, i2, config);
        scaleBitmap(bitmap, createBitmap);
        return createBitmap;
    }


    public static Bitmap scaleBitmap(Bitmap bitmap, Bitmap bitmap2) {
        if (bitmap == null || bitmap2 == null) {
            return null;
        }
        if (bitmap.getWidth() == bitmap2.getWidth() && bitmap.getHeight() == bitmap2.getHeight()) {
            return bitmap;
        }
        Canvas canvas = new Canvas(bitmap2);
        canvas.drawARGB(0, 0, 0, 0);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);
        paint.setDither(true);
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()), paint);
        return bitmap2;
    }


    public static int getBitmapColorMode(Bitmap bitmap, int i) {
        int colorMode = BITMAP_COLOR_MODE_LIGHT;
        int height = bitmap.getHeight() / i;
        int width = bitmap.getWidth() / i;
        int i3 = (width * height) / 5;
        Bitmap scaleBitmap = scaleBitmap(bitmap, width, height);
        int tempColorMode = 0;
        int x = 0;
        while (x < width) {
            int darkPixelNum = tempColorMode;
            tempColorMode = colorMode;
            for (int y = 0; y < height; y++) {
                int pixel = scaleBitmap.getPixel(x, y);
                double rs = Color.red(pixel) * 0.3d;
                double gs = Color.green(pixel) * 0.59d;
                double bs = Color.blue(pixel) * 0.11d;
                if ( rs + gs + bs < 180) {
                    darkPixelNum++;
                    if (darkPixelNum > i3) {
                        tempColorMode = BITMAP_COLOR_MODE_MEDIUM;
                    }
                    if (darkPixelNum > i3 * 2) {
                        tempColorMode = BITMAP_COLOR_MODE_DARK;
                        break;
                    }
                }
            }
            x++;
            colorMode = tempColorMode;
            tempColorMode = darkPixelNum;
        }
        if (scaleBitmap != bitmap) {
            scaleBitmap.recycle();
        }
        return colorMode;
    }

    public static final int BITMAP_COLOR_MODE_DARK = 0;
    public static final int BITMAP_COLOR_MODE_LIGHT = 2;
    public static final int BITMAP_COLOR_MODE_MEDIUM = 1;


}
