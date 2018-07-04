package com.miui.home.launcher;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class ToggleDrawable extends LayerDrawable {
    public ToggleDrawable(Drawable bgDrawable, Drawable toggle) {
        super(getArray(bgDrawable, toggle));
        setId(0, 0);
        setId(1, 1);
        if (toggle != null) {
            int left = (DeviceConfig.getIconWidth() - toggle.getIntrinsicWidth()) / 2;
            int top = (DeviceConfig.getIconHeight() - toggle.getIntrinsicHeight()) / 2;
            setLayerInset(1, left, top, left, top);
        }
    }

    private static Drawable[] getArray(Drawable bgDrawable, Drawable toggle) {
        return new Drawable[]{bgDrawable, toggle};
    }

    public void changeToggleInfo(Drawable toggle) {
        setDrawableByLayerId(1, toggle);
        invalidateSelf();
    }
}
