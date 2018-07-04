package com.miui.home.launcher;

public class ShortcutPlaceholderProviderInfo extends ShortcutInfo {
    public final int addType;

    public ShortcutPlaceholderProviderInfo(int addType) {
        this.itemType = 8;
        this.addType = addType;
        this.spanX = 1;
        this.spanY = 1;
    }
}
