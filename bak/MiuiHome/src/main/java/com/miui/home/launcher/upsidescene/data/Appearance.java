package com.miui.home.launcher.upsidescene.data;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import miui.util.FileAccessable.Factory;

public abstract class Appearance {
    FreeStyle mFreeStyle;
    String mPath;

    public static class FreeButtonAppearance extends Appearance {
        FreeButtonInfo mFreeButton;

        public FreeButtonAppearance(String name, FreeStyle freeStyle) {
            super("skins/" + name, freeStyle);
            this.mFreeStyle = freeStyle;
        }

        public FreeButtonInfo getFreeButtonInfo() {
            if (this.mFreeButton == null) {
                try {
                    this.mFreeButton = this.mFreeStyle.getFreeButton(Factory.create(FreeStyleSerializer.DATA_PATH, this.mPath));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return this.mFreeButton;
        }

        public int getType() {
            return 2;
        }
    }

    public static class NoneAppearance extends Appearance {
        public NoneAppearance(FreeStyle freeStyle) {
            super("", freeStyle);
        }

        public int getType() {
            return 0;
        }
    }

    public static class StageImageAppearance extends Appearance {
        Bitmap mBitmap;
        float mSceneScale;

        public StageImageAppearance(String path, FreeStyle freeStyle) {
            super(path, freeStyle);
            this.mSceneScale = freeStyle.getSceneScale();
        }

        public Bitmap getBitmap() {
            if (this.mBitmap == null) {
                try {
                    this.mBitmap = FreeStyleSerializer.decodeBitmapFromFile(null, this.mSceneScale, Factory.create(FreeStyleSerializer.DATA_PATH, this.mPath));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return this.mBitmap;
        }

        public int getType() {
            return 1;
        }
    }

    public abstract int getType();

    private Appearance(String path, FreeStyle freeStyle) {
        this.mPath = path;
        this.mFreeStyle = freeStyle;
    }

    public void setFreeStyle(FreeStyle freeStyle) {
        this.mFreeStyle = freeStyle;
    }

    public static Appearance load(String path, FreeStyle freeStyle) {
        if (TextUtils.isEmpty(path)) {
            return new NoneAppearance(freeStyle);
        }
        path = path.replace('\\', '/');
        int slashIndex = path.indexOf(47);
        if (slashIndex == -1) {
            Log.e("FreeStyle.Appearance", "parse appearance path failed:not found /. path:" + path);
            return new NoneAppearance(freeStyle);
        }
        String prefix = path.substring(0, slashIndex);
        String name = path.substring(slashIndex + 1);
        if (prefix.equalsIgnoreCase("stage_images")) {
            return new StageImageAppearance(path, freeStyle);
        }
        if (prefix.equalsIgnoreCase("skins")) {
            return new FreeButtonAppearance(name, freeStyle);
        }
        Log.w("FreeStyle.Appearance", "unknown appearance path prefix. path:" + path);
        return new NoneAppearance(freeStyle);
    }

    public String toString() {
        return this.mPath;
    }
}
