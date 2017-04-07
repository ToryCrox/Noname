/*Transsion Top Secret*/
package com.transsion.xlauncher.setting.base;

import android.support.annotation.IdRes;

public class TwoStatePreferenceItem extends PreferenceItem {


    public boolean checked;

    @IdRes
    public int widgetId;

    public void setChecked(boolean checked){
        this.checked = checked;
    }

    @Override
    public void onBindViewHolder(PreferenceAdapter.PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if(widgetId != 0){
            holder.setChecked(widgetId,checked);
        }

    }
}
