package com.miui.home.launcher.gadget;

import android.content.Context;
import com.miui.home.R;

public class PowerClearButton extends ClearButton {
    public PowerClearButton(Context context) {
        super(context);
    }

    protected void initProgressBar() {
        this.mProgressBar.setDrawablesForLevels(null, new int[]{R.drawable.gadget_clear_button_circle}, new int[]{R.drawable.gadget_power_clear_fore_normal});
    }

    protected void doClear() {
        try {
            Class cls = Class.forName("com.miui.whetstone.WhetstoneManager");
            Class configClass = Class.forName("com.miui.whetstone.WhetstoneConfig");
            cls.getDeclaredMethod("deepClean", new Class[]{configClass}).invoke(cls, new Object[]{null});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
