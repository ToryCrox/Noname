package com.tory.lightphoto;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tory.library.lightness.ColorUtils;
import com.tory.library.utils.FileUtils;
import com.tory.library.utils.ImageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.iwf.photopicker.PhotoPickUtils;


public class WallpaperActivity extends AppCompatActivity {

    private static final String TAG = "WallpaperActivity";

    @BindView(R.id.main)
    protected View mMain;

    @BindView(R.id.wallpaper)
    ImageView mWallpaperView;

    @BindView(R.id.text)
    TextView mTextView;
    @BindView(R.id.text2)
    TextView mTextView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wallpaper);
        ButterKnife.bind(this);

        PhotoPickUtils.init(getApplicationContext());
    }


    @OnClick(R.id.photo_pick)
    public void pickPhoto(){
        Log.d(TAG, "pickPhoto: ");
        PhotoPickUtils.startPick().setPhotoCount(1).start(this);
    }

    @OnClick(R.id.wallpaper_save)
    public void saveWallpaper(){
        Log.d(TAG, "saveWallpaper: ");
        WallpaperManager wm = WallpaperManager.getInstance(getApplicationContext());
        Drawable drawable = wm.getDrawable();
        Bitmap bitmap = ImageUtils.drawableToBitmap(drawable);

        setBitmap(bitmap);

        String filename = "wallpaper"+ new Random().nextInt(100) + ".jpg";
        File file = new File(FileUtils.getSDPath(),"DCIM/Screenshots/"+filename);
        boolean suc = FileUtils.saveBitmap(bitmap,file.getAbsolutePath());
        Log.d(TAG, "saveWallpaper: suc="+suc);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode="+requestCode+",resultCode="+resultCode+",data="+data);
        PhotoPickUtils.onActivityResult(requestCode, resultCode, data, new PhotoPickUtils.PickHandler() {
            @Override
            public void onPickSuccess(ArrayList<String> arrayList, int requestCode) {
                Log.d(TAG, "onActivityResult onPickSuccess arrayList="+arrayList);
                pickSuccess(arrayList);
            }

            @Override
            public void onPreviewBack(ArrayList<String> arrayList, int requestCode) {
                Log.d(TAG, "onActivityResult onPreviewBack arrayList="+arrayList);
            }

            @Override
            public void onPickFail(String s, int requestCode) {
                Log.d(TAG, "onActivityResult onPickFail s="+s);
            }

            @Override
            public void onPickCancle(int requestCode) {
                Log.d(TAG, "onActivityResult onPickCancle requestCode="+requestCode);
            }

        });
    }

    private void pickSuccess(ArrayList<String> filepath) {
        String photoPath = filepath.get(0);
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        setBitmap(bitmap);
    }


    private void setBitmap(Bitmap bitmap){
        mWallpaperView.setImageBitmap(bitmap);

        boolean isDark = ColorUtils.isDark(bitmap);
        int color = isDark ? Color.WHITE : Color.BLACK;
        int color2 = isDark ? Color.BLACK : Color.WHITE;
        mTextView.setTextColor(color);
        mTextView2.setTextColor(color2);
        Toast.makeText(this,"photo is dark="+isDark,Toast.LENGTH_SHORT).show();
    }
}
