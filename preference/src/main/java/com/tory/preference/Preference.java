package com.tory.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @Author: Tory
 * Create: 2016/10/1
 */
public class Preference extends android.preference.Preference {

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }


    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public Preference(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int i) {
        setLayoutResource(R.layout.preference_item);
    }
}
