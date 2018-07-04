package com.miui.home.launcher.upsidescene;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import com.miui.home.R;
import com.miui.home.launcher.upsidescene.FreeButtonState.StateUpdateListener;
import com.miui.home.launcher.upsidescene.data.Appearance.FreeButtonAppearance;
import com.miui.home.launcher.upsidescene.data.Sprite;

public class FreeButton extends View implements StateUpdateListener {
    private FreeButtonState mFreeButtonState;
    private SceneScreen mSceneScreen;
    private Sprite mSprite;

    public FreeButton(Context context, Sprite sprite, SceneScreen sceneScreen) {
        super(context);
        init(context, sprite, sceneScreen);
    }

    public void init(Context context, Sprite sprite, SceneScreen sceneScreen) {
        this.mSprite = sprite;
        this.mSceneScreen = sceneScreen;
        setClickable(true);
        this.mFreeButtonState = new FreeButtonState(this.mContext, sprite, this, sceneScreen, this);
    }

    public boolean performClick() {
        if (!this.mSceneScreen.isInEditMode()) {
            this.mFreeButtonState.trigger();
        }
        return true;
    }

    private Drawable getDrawableAnyway(String state, String pressedState) {
        if (!(this.mSprite.getAppearance() instanceof FreeButtonAppearance)) {
            return null;
        }
        FreeButtonAppearance appearance = (FreeButtonAppearance) this.mSprite.getAppearance();
        Drawable drawable = appearance.getFreeButtonInfo().getDrawable(state, this.mContext);
        if (drawable != null) {
            if (pressedState == null) {
                return drawable;
            }
            Drawable pressedDrawable = appearance.getFreeButtonInfo().getDrawable(pressedState, this.mContext);
            if (pressedDrawable == null) {
                return drawable;
            }
            Drawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(View.PRESSED_STATE_SET, pressedDrawable);
            stateListDrawable.addState(View.EMPTY_STATE_SET, drawable);
            return stateListDrawable;
        } else if ("normal".equals(state)) {
            return drawable;
        } else {
            return getDrawableAnyway("normal", "normal_pressed");
        }
    }

    public int onStateUpdated(String state, String pressedState) {
        Drawable drawable;
        int type = this.mSprite.getFunction().getType();
        if (this.mSprite.getAppearance().getType() != 0) {
            drawable = getDrawableAnyway(state, pressedState);
        } else if (type == 3) {
            drawable = getResources().getDrawable(R.drawable.free_style_icon_drawer);
        } else if (type == 8) {
            drawable = getResources().getDrawable(R.drawable.children_mode_exit_btn);
        } else {
            drawable = getDrawableAnyway(state, pressedState);
        }
        if (drawable == null) {
            return 0;
        }
        setBackground(drawable);
        return startAnimationIfNeed();
    }

    private int startAnimationIfNeed() {
        if (!(getBackground() instanceof AnimationDrawable)) {
            return 0;
        }
        AnimationDrawable animDrawable = (AnimationDrawable) getBackground();
        int totalDuration = 0;
        for (int i = animDrawable.getNumberOfFrames() - 1; i >= 0; i--) {
            totalDuration += animDrawable.getDuration(i);
        }
        animDrawable.start();
        return totalDuration;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimationIfNeed();
    }
}
