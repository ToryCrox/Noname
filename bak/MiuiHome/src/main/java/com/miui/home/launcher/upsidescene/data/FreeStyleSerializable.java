package com.miui.home.launcher.upsidescene.data;

public interface FreeStyleSerializable {
    void clear(boolean z);

    FreeStyle load();

    void save(FreeStyle freeStyle);
}
