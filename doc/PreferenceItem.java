/*Transsion Top Secret*/
package com.transsion.xlauncher.setting.base;

import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

import com.transsion.XOSLauncher.R;

public class PreferenceItem {

    public static final int ITEM_PREFERENCE_TYPE = 0;
    public static final int CATEGORY_PREFERENCE_TYPE = 1;

    public int itemType;
    public String title;
    public String summary;
    public Intent intent;
    @DrawableRes
    public int iconResId;

    public boolean clickable;
    public boolean itemHeader;

    @LayoutRes
    public int widgetResId;
    @LayoutRes
    public int layoutResId;
    public PreferenceAdapter.LayoutType layoutType;


    public ActivityOptions opts;

    public static PreferenceItem asCategory(String title){
        PreferenceItem item = new PreferenceItem();
        item.itemType = CATEGORY_PREFERENCE_TYPE;
        item.itemHeader = true;
        item.title = title;
        item.clickable = false;
        item.layoutResId = R.layout.preference_category;
        return item;
    }

    public static PreferenceItem asItem(PreferenceItem item,@DrawableRes int iconResId,
                                        String title, String summary,Intent intent){
        if(item == null){
            item =new PreferenceItem();
        }
        item.itemType = ITEM_PREFERENCE_TYPE;
        item.itemHeader = false;
        item.iconResId = iconResId;
        item.title = title;
        item.summary = summary;
        item.clickable = true;
        item.intent = intent;
        return item;
    }

    public static PreferenceItem asItem(@DrawableRes int iconResId,String title,
                                        String summary,Intent intent){
        PreferenceItem item = new PreferenceItem();
        asItem(item,iconResId,title,summary,intent);
        item.layoutResId = R.layout.preference_item;
        return item;
    }

    public static PreferenceItem asItemH(@DrawableRes int iconResId,String title,
                                         String summary,Intent intent){
        PreferenceItem item = asItem(iconResId,title,summary,intent);
        item.layoutResId = R.layout.preference_item_horizontal;
        return item;
    }

    public static TwoStatePreferenceItem asItemTwoState(@DrawableRes int iconResId,String title,
                                                String summary,@LayoutRes int widgetResId,
                                                @IdRes int widgetId){
        TwoStatePreferenceItem item = (TwoStatePreferenceItem)
                asItem(new TwoStatePreferenceItem(),iconResId,title,summary,null);
        item.layoutResId = R.layout.preference_item_horizontal;
        item.widgetResId = widgetResId;
        item.widgetId = widgetId;
        return item;
    }

    public void onBindViewHolder(PreferenceAdapter.PreferenceViewHolder holder) {
    }
}