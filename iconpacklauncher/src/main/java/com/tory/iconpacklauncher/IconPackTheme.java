package com.tory.iconpacklauncher;

import android.content.ComponentName;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author tao.xu2
 * @date 2018/1/24
 */

public class IconPackTheme {


    private static int MAX_ICON_SIZE = 192;
    int mVersionCode;
    String mPackageName;
    Resources mResources;

    int iconbackResId = 0;
    int iconuponResId = 0;
    int iconmaskResId = 0;
    float scale = 1f;
    Map<ComponentName, Integer> iconPacks ;
    Map<ComponentName, SparseIntArray> calendarPacks;
    SparseArray<String> iconResNames;

    Bitmap iconBack;
    Bitmap iconUpon;
    Bitmap iconMask;


    private static IconPackTheme sInstance;

    public IconPackTheme(){

    }

    public static IconPackTheme getInstance(){
        if(sInstance == null){
            synchronized (IconPackTheme.class){
                if(sInstance == null){
                    sInstance = new IconPackTheme();
                }
            }
        }
        return sInstance;
    }

    private boolean isBitmapAvailable(@Nullable Bitmap bitmap){
        return bitmap != null && !bitmap.isRecycled();
    }

    public Bitmap decodeBitmap(int id){
        Bitmap bitmap = null;
        try {
            InputStream is = mResources.openRawResource(id);
            if (is != null) {
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap decodeMaxIcon(int id){
        Bitmap bitmap = decodeBitmap(id);
        if(bitmap != null && bitmap.getWidth() > MAX_ICON_SIZE){
            Bitmap temp = BitmapUtils.scaleBitmap(bitmap, MAX_ICON_SIZE, MAX_ICON_SIZE);
            bitmap.recycle();
            bitmap = temp;
        }
        return bitmap;
    }

    public Bitmap getIconBack() {
        if(!isBitmapAvailable(iconBack) && iconbackResId != 0){
            iconBack = decodeMaxIcon(iconbackResId);
        }
        return iconBack;
    }

    public Bitmap getIconMask() {
        if(!isBitmapAvailable(iconMask) && iconmaskResId != 0){
            iconMask = decodeMaxIcon(iconmaskResId);
        }
        return iconMask;
    }

    public Bitmap getIconUpon() {
        if(!isBitmapAvailable(iconUpon) && iconuponResId != 0){
            iconUpon = decodeMaxIcon(iconuponResId);
        }
        return iconUpon;
    }

    public void onDestroy() {
        if(isBitmapAvailable(iconBack)){
            iconBack.recycle();
        }
        if(isBitmapAvailable(iconMask)){
            iconMask.recycle();
        }

        if(isBitmapAvailable(iconUpon)){
            iconUpon.recycle();
        }
        iconBack = null;
        iconMask = null;
        iconUpon = null;
    }


    public Bitmap getIcon(@NonNull ComponentName cn) {
        Integer val = iconPacks.get(cn);
        if(val == null || val == 0){
            return null;
        }
        return decodeBitmap(val);
    }

    public Bitmap getCalendarIcon(@NonNull ComponentName cn) {
        SparseIntArray ids = calendarPacks.get(cn);
        if(ids == null) return null;
        int day = getDayOfMonth();
        int resId = ids.get(day - 1);
        if(resId == 0) return null;
        return decodeBitmap(resId);
    }

    protected int getDayOfMonth() {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

}
