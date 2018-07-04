package com.miui.home.launcher;

import android.content.ComponentName;

class ShortcutProviderInfo extends ItemInfo {
    ComponentName mComponentName;

    ShortcutProviderInfo(String pakcageName, String className) {
        this.itemType = 7;
        this.mComponentName = new ComponentName(pakcageName, className);
        this.spanX = 1;
        this.spanY = 1;
    }
}
