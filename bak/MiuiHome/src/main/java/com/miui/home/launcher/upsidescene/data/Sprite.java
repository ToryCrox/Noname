package com.miui.home.launcher.upsidescene.data;

import com.miui.home.launcher.upsidescene.data.Appearance.NoneAppearance;

public class Sprite {
    Appearance mAppearance;
    FreeStyle mFreeStyle;
    Function mFunction;
    int mHeight;
    int mIndex;
    boolean mIsUserCreated;
    int mLeft;
    int mRawHeight;
    int mRawLeft;
    int mRawTop;
    int mRawWidth;
    float mRotation;
    float mScaleX;
    float mScaleY;
    int mTop;
    int mWidth;

    Sprite(FreeStyle freeStyle) {
        this.mFreeStyle = freeStyle;
    }

    public int getLeft() {
        return this.mLeft;
    }

    public int getTop() {
        return this.mTop;
    }

    public float getRotation() {
        return this.mRotation;
    }

    public float getScaleX() {
        return this.mScaleX;
    }

    public float getScaleY() {
        return this.mScaleY;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setLocation(int left, int top) {
        this.mLeft = left;
        this.mTop = top;
        this.mRawLeft = (int) ((((float) left) / this.mFreeStyle.getSceneScale()) + 0.5f);
        this.mRawTop = (int) ((((float) top) / this.mFreeStyle.getSceneScale()) + 0.5f);
    }

    public boolean isUserCreated() {
        return this.mIsUserCreated;
    }

    public Appearance getAppearance() {
        if (this.mAppearance == null) {
            this.mAppearance = new NoneAppearance(null);
        }
        return this.mAppearance;
    }

    public void setAppearance(Appearance appearance) {
        this.mAppearance = appearance;
    }

    public Function getFunction() {
        if (this.mFunction == null) {
            this.mFunction = Function.createFunction(0);
        }
        return this.mFunction;
    }

    public void setFunction(Function function) {
        this.mFunction = function;
    }
}
