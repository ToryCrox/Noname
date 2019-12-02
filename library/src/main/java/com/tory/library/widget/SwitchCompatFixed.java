package com.tory.library.widget;

import android.content.Context;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * @Author: Tory
 *
 * http://linroid.com/2016/02/26/SwitchCompat-s-animation-not-work-in-Preference/
 */
public class SwitchCompatFixed extends SwitchCompat {
    public SwitchCompatFixed(Context context) {
        super(context);
    }

    public SwitchCompatFixed(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchCompatFixed(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isShown() {
      ViewParent parent = getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ViewGroup widgetFrame = (ViewGroup) parent;
            if (widgetFrame.getId() == android.R.id.widget_frame) {
                return true;
            }
        }
        return super.isShown();
    }

}
