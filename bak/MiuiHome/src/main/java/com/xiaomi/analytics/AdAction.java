package com.xiaomi.analytics;

import android.text.TextUtils;
import java.util.List;

public class AdAction extends TrackAction {
    public AdAction(String str) {
        setCategory("ad");
        setAction(str);
    }

    public AdAction addAdMonitor(List<String> list) {
        if (list != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : list) {
                if (!TextUtils.isEmpty(str)) {
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("|");
                    }
                    stringBuilder.append(str);
                }
            }
            if (stringBuilder.length() > 0) {
                addExtra("_ad_monitor_", stringBuilder.toString());
            }
        }
        return this;
    }
}
