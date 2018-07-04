package com.miui.home.launcher.upsidescene.data;

import java.util.ArrayList;
import java.util.List;

public class Screen {
    int mHeight;
    int mHome;
    int mRawWidth;
    List<Sprite> mSprites = new ArrayList();
    int mType;
    int mWidth;

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getType() {
        return this.mType;
    }

    public List<Sprite> getSprites() {
        return this.mSprites;
    }

    public int getHome() {
        return this.mHome;
    }
}
