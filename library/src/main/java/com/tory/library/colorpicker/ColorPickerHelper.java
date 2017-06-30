package com.tory.library.colorpicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;

import com.tory.library.R;

import java.util.ArrayList;

/**
 * Created by tao.xu2 on 2017/5/17.
 */

public class ColorPickerHelper {

    private Dialog mDialog;

    public ColorPickerHelper(@NonNull Context context){
    }

    public void dismissPop(){
        if(mDialog != null){
            mDialog.dismiss();
        }
    }

    public boolean isDialogShowing(){
        //return mPopup != null && mPopup.isShowing();
        return mDialog != null && mDialog.isShowing();
    }


    private ArrayList<ColorItem> getColors(Context context){
        TypedArray ar = context.getResources().obtainTypedArray(R.array.preview_colors);
        ArrayList<ColorItem> items =new ArrayList<>();
        final int len = ar.length();
        for (int i = 0; i < len && i < 8; i++) {
            int color = ar.getColor(i, 0);
            ColorItem item = new ColorItem();
            item.color = color;
            items.add(item);
        }
        ar.recycle();
        return items;
    }

    public void showColorPickerDialog(final Activity activity,
                                      @ColorInt int oldColor, @StyleRes int styleResId,
                                      final OnColorChangedListener listener){
        if(isDialogShowing()){
            return;
        }
        ColorPickerDialog dialog = new ColorPickerDialog(activity, styleResId,
                oldColor, getColors(activity));
        dialog.setAlphaSliderVisible(true);
        dialog.setHexValueEnabled(true);
        dialog.setOnColorChangedListener(listener);
        dialog.show();
        mDialog = dialog;
    }

}
