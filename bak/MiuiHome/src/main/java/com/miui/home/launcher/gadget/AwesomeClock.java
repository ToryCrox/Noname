package com.miui.home.launcher.gadget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.gadget.Clock.ClockStyle;
import java.util.Calendar;
import miui.maml.MiAdvancedView;
import miui.maml.RenderThread;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.ScreenElementRoot.OnExternCommandListener;
import miui.maml.elements.ButtonScreenElement;
import miui.maml.elements.ButtonScreenElement.ButtonActionListener;
import miui.maml.util.Utils;
import miui.maml.util.ZipResourceLoader;
import org.w3c.dom.Element;

public class AwesomeClock extends Gadget implements ClockStyle {
    private MiAdvancedView mAwesomeView;
    private String mComponentCode;
    private ScreenContext mElementContext;
    private String mMamlConfig;
    private String mMamlPath;
    private ScreenElementRoot mRoot;
    private int mUpdateInterval;

    public AwesomeClock(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public void onDestroy() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp();
        }
    }

    public void onPause() {
        if (this.mRoot != null) {
            synchronized (this.mRoot) {
                this.mRoot.onCommand("pause");
            }
        }
        if (this.mAwesomeView != null) {
            this.mAwesomeView.invalidate();
            this.mAwesomeView.onPause();
        }
    }

    public void onResume() {
        if (this.mRoot != null) {
            synchronized (this.mRoot) {
                this.mRoot.onCommand("resume");
            }
            this.mRoot.loadConfig();
        }
        if (this.mAwesomeView != null) {
            this.mAwesomeView.onResume();
        }
    }

    public void onWallpaperColorChanged() {
        if (this.mElementContext != null && this.mRoot != null) {
            Utils.putVariableNumber("applied_light_wallpaper", this.mElementContext.mVariables, WallpaperUtils.hasAppliedLightWallpaper() ? 1.0d : 0.0d);
            this.mRoot.requestUpdate();
        }
    }

    public void updateConfig(Bundle config) {
    }

    public void initConfig(String config) {
        this.mElementContext = new ScreenContext(this.mContext, new ZipResourceLoader(config).setLocal(this.mContext.getResources().getConfiguration().locale));
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        String strPivotX;
        String strPivotY;
        super.onSizeChanged(w, h, oldw, oldh);
        if (this.mRoot != null) {
            strPivotX = this.mRoot.getRawAttr("pivotX");
        } else {
            strPivotX = null;
        }
        if (this.mRoot != null) {
            strPivotY = this.mRoot.getRawAttr("pivotY");
        } else {
            strPivotY = null;
        }
        float pivotX = 0.0f;
        float pivotY = 0.0f;
        try {
            if (!TextUtils.isEmpty(strPivotX)) {
                pivotX = Float.parseFloat(strPivotX);
            }
            if (!TextUtils.isEmpty(strPivotY)) {
                pivotY = Float.parseFloat(strPivotY);
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        View parent = (View) getParent();
        if (pivotX != 0.0f) {
            setPivotX(((float) w) * pivotX);
            parent.setPivotX(((float) parent.getMeasuredWidth()) * pivotX);
        }
        if (pivotY != 0.0f) {
            setPivotY(((float) h) * pivotY);
            parent.setPivotY(((float) parent.getMeasuredHeight()) * pivotY);
        }
    }

    public void setMamlConfigPath(String config) {
        this.mMamlConfig = config;
    }

    public void setConfigParas(String code, String path) {
        this.mComponentCode = code;
        this.mMamlPath = path;
    }

    public int getUpdateInterval() {
        int interval = 0;
        if (this.mAwesomeView != null) {
            interval = this.mUpdateInterval;
        }
        return interval > 0 ? interval : 1000;
    }

    public void updateAppearance(Calendar calendar) {
        if (this.mElementContext != null && this.mAwesomeView != null) {
            this.mRoot.requestUpdate();
        }
    }

    public void onCreate() {
        if (this.mElementContext != null) {
            Element root = this.mElementContext.mResourceManager.getManifestRoot();
            if (root != null && "clock".equalsIgnoreCase(root.getNodeName())) {
                try {
                    this.mUpdateInterval = Integer.parseInt(root.getAttribute("update_interval"));
                } catch (NumberFormatException e) {
                    this.mUpdateInterval = 60000;
                }
                try {
                    getLayoutParams().width = Integer.parseInt(root.getAttribute("width"));
                    getLayoutParams().height = Integer.parseInt(root.getAttribute("height"));
                } catch (NumberFormatException e2) {
                }
                this.mRoot = new ScreenElementRoot(this.mElementContext);
                this.mRoot.setDefaultFramerate(1000.0f / ((float) this.mUpdateInterval));
                this.mRoot.setConfig(this.mMamlConfig);
                this.mRoot.setScaleByDensity(true);
                if (this.mRoot.load()) {
                    this.mAwesomeView = new MiAdvancedView(this.mContext, this.mRoot, RenderThread.globalThread(true));
                    this.mAwesomeView.setFocusable(false);
                    addView(this.mAwesomeView, new LayoutParams(-1, -1));
                    if (this.mComponentCode != null && this.mMamlPath != null) {
                        Utils.putVariableString("__config_code", this.mRoot.getContext().mVariables, this.mComponentCode);
                        Utils.putVariableString("__config_path", this.mRoot.getContext().mVariables, this.mMamlPath);
                    }
                }
            }
        }
    }

    public boolean setClockButtonListener(ButtonActionListener listener) {
        if (this.mRoot == null) {
            return false;
        }
        ButtonScreenElement clockButton = (ButtonScreenElement) this.mRoot.findElement("clock_button");
        if (clockButton == null) {
            Log.w("AwesomeClock", "No clock button in this clock.");
            return false;
        }
        clockButton.setListener(listener);
        return true;
    }

    public boolean setOnExternCommandListener(OnExternCommandListener listener) {
        if (this.mRoot == null) {
            return false;
        }
        this.mRoot.setOnExternCommandListener(listener);
        return true;
    }

    public void onStart() {
        RenderThread.globalThread().setPaused(false);
    }

    public void onStop() {
        RenderThread.globalThread().setPaused(true);
    }

    public void onEditDisable() {
        Utils.putVariableNumber("is_editing_mode", this.mElementContext.mVariables, 0.0d);
        this.mRoot.requestUpdate();
    }

    public void onEditNormal() {
        Utils.putVariableNumber("is_editing_mode", this.mElementContext.mVariables, 1.0d);
        this.mRoot.requestUpdate();
    }

    public void onAdded() {
    }

    public void onDeleted() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp();
        }
    }
}
