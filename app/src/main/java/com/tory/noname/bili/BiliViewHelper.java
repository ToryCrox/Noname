package com.tory.noname.bili;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tory.library.recycler.BaseViewHolder;
import com.tory.noname.R;

/**
 * Created by tao.xu2 on 2016/9/26.
 */

public class BiliViewHelper {

    public static void tintTextDrawables(BaseViewHolder holder, int color, int ...resIds){
        for(int resId : resIds){
            if(Boolean.TRUE.equals(holder.getView(resId).getTag(R.id.text_view_drawable_tint))){
                continue;
            }
            tintDrawable(holder.getView(resId),color);
        }
    }

    //http://chuansong.me/n/400689551333
    public  static void tintDrawable(View view, int color) {
        if(view instanceof TextView){
            TextView tv = (TextView) view;
            Drawable[] drawables = tv.getCompoundDrawablesRelative();
            if(drawables == null ) return;
            Drawable icon = drawables[0];
            if (icon != null) {
                Drawable.ConstantState state = icon.getConstantState();
                icon = DrawableCompat.wrap(state == null ? icon : state.newDrawable()).mutate();
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                DrawableCompat.setTintList(icon, ColorStateList.valueOf(color));
            }
            TextViewCompat.setCompoundDrawablesRelative(tv, icon, null, null, null);
            tv.setTag(R.id.text_view_drawable_tint,Boolean.TRUE);
        }else if(view instanceof ImageView){
            ImageView iv = (ImageView) view;
            Drawable d = DrawableCompat.wrap(iv.getDrawable());
            DrawableCompat.setTintList(d, ColorStateList.valueOf(color));
            iv.setImageDrawable(d);
            iv.setTag(R.id.text_view_drawable_tint,Boolean.TRUE);
        }

    }
}
