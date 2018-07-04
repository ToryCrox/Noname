package com.miui.home.launcher.gadget;

import android.content.Context;
import com.miui.home.launcher.upsidescene.FreeButtonState;
import com.miui.home.launcher.upsidescene.FreeButtonState.StateUpdateListener;
import com.miui.home.launcher.upsidescene.SceneScreen;
import com.miui.home.launcher.upsidescene.data.Sprite;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot.OnExternCommandListener;
import miui.maml.util.Utils;

public class FreeButtonAwesomeGadget extends AwesomeGadget implements StateUpdateListener, OnExternCommandListener {
    private FreeButtonState mFreeButtonState;
    private SceneScreen mSceneScreen;
    private Sprite mSprite;

    public FreeButtonAwesomeGadget(Context context, ScreenContext screenContext, Sprite sprite, SceneScreen sceneScreen) {
        super(context);
        ROOT_TAG = "free_gadget";
        this.mElementContext = screenContext;
        this.mSprite = sprite;
        this.mSceneScreen = sceneScreen;
    }

    public void onCreate() {
        super.onCreate();
        if (this.mRoot != null) {
            this.mFreeButtonState = new FreeButtonState(this.mContext, this.mSprite, this, this.mSceneScreen, this);
            this.mRoot.setOnExternCommandListener(this);
        }
    }

    public int onStateUpdated(String state, String pressedState) {
        if ("normal".equals(state)) {
            Utils.putVariableNumber("FreeStyle_FreeButtonState", this.mRoot.getContext().mVariables, 0.0d);
            this.mRoot.requestUpdate();
        } else if ("open".equals(state)) {
            Utils.putVariableNumber("FreeStyle_FreeButtonState", this.mRoot.getContext().mVariables, 1.0d);
            this.mRoot.requestUpdate();
        }
        return 0;
    }

    public void onCommand(String command, Double para1, String para2) {
        if ("trigger".equalsIgnoreCase(command)) {
            this.mFreeButtonState.trigger();
        }
    }
}
