package com.miui.home.launcher.upsidescene.data;

import android.graphics.Bitmap;
import com.miui.home.launcher.ItemInfo;
import java.util.ArrayList;
import java.util.List;
import miui.util.FileAccessable;

public class FreeStyle {
    List<FreeButtonInfo> mFreeButtonInfos = new ArrayList();
    int mHeight;
    List<MtzGadgetInfo> mMtzGadgets = new ArrayList();
    String mName;
    int mRawWidth;
    float mSceneScale;
    List<Screen> mScreens = new ArrayList();
    int mWidth;

    public static class MtzGadgetInfo extends ItemInfo {
        public String path;
        public Bitmap preview;
        public String title;

        public MtzGadgetInfo() {
            this.itemType = 9;
        }
    }

    public String getName() {
        return this.mName;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public Screen getForegroundScreen() {
        return getSpecifyScreen(3);
    }

    public Screen getBackgroundScreen() {
        return getSpecifyScreen(1);
    }

    public Screen getDriftScreen() {
        return getSpecifyScreen(2);
    }

    public Screen getDockScreen() {
        return getSpecifyScreen(4);
    }

    private Screen getSpecifyScreen(int type) {
        for (int i = this.mScreens.size() - 1; i >= 0; i--) {
            Screen screen = (Screen) this.mScreens.get(i);
            if (screen.getType() == type) {
                return screen;
            }
        }
        return null;
    }

    public FreeButtonInfo getFreeButton(FileAccessable file) {
        for (FreeButtonInfo freeButton : this.mFreeButtonInfos) {
            if (file.equals(freeButton.getFile())) {
                return freeButton;
            }
        }
        return null;
    }

    public List<FreeButtonInfo> getFreeButtons() {
        return this.mFreeButtonInfos;
    }

    public List<MtzGadgetInfo> getMtzGadgets() {
        return this.mMtzGadgets;
    }

    public float getSceneScale() {
        return this.mSceneScale;
    }

    public Sprite createSpriteByUser() {
        Sprite sprite = new Sprite(this);
        sprite.mIsUserCreated = true;
        return sprite;
    }

    public void bringSpriteToTop(Screen screen, Sprite sprite) {
        screen.mSprites.remove(sprite);
        screen.mSprites.add(sprite);
    }

    public void addSprite(Screen screen, Sprite sprite) {
        screen.mSprites.add(sprite);
    }

    public void removeSprite(Screen screen, Sprite sprite) {
        screen.mSprites.remove(sprite);
    }
}
