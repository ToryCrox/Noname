package com.miui.home.launcher.gadget;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.Clock.ClockStyle;
import java.io.File;
import java.util.Calendar;
import miui.maml.RenderThread;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.ScreenElementRoot.OnExternCommandListener;
import miui.maml.util.ZipResourceLoader;
import org.w3c.dom.Element;

public class AwesomeGadget extends Gadget implements ClockStyle, OnExternCommandListener {
    private static String LOG_TAG = "AwesomeGadget";
    protected static String ROOT_TAG = "gadget";
    protected AwesomeGadgetView mAwesomeView;
    protected ScreenContext mElementContext;
    protected ScreenElementRoot mRoot;
    protected int mUpdateInterval;

    public AwesomeGadget(Context context) {
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
        }
    }

    public void onResume() {
        if (this.mRoot != null) {
            synchronized (this.mRoot) {
                this.mRoot.onCommand("resume");
            }
            this.mRoot.requestUpdate();
        }
        if (this.mAwesomeView == null) {
        }
    }

    public void updateConfig(Bundle config) {
    }

    public void initConfig(String config) {
        this.mElementContext = new ScreenContext(this.mContext, new ZipResourceLoader(config));
    }

    public void initConfig(String rootPath, String subpath) {
        if (new File(rootPath).isDirectory()) {
            initConfig(new File(rootPath, subpath).getAbsolutePath());
            return;
        }
        if (!subpath.endsWith("/")) {
            subpath = subpath + "/";
        }
        this.mElementContext = new ScreenContext(this.mContext, new ZipResourceLoader(rootPath, subpath));
    }

    public void reinit() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.init();
        }
    }

    public void cleanUpAndKeepResource() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp(true);
        }
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
            try {
                Element root = this.mElementContext.mResourceManager.getManifestRoot();
                if (root == null) {
                    return;
                }
                if (ROOT_TAG.equalsIgnoreCase(root.getNodeName())) {
                    try {
                        this.mUpdateInterval = Integer.parseInt(root.getAttribute("update_interval"));
                    } catch (NumberFormatException e) {
                        this.mUpdateInterval = 60000;
                    }
                    this.mRoot = new ScreenElementRoot(this.mElementContext);
                    this.mRoot.setScaleByDensity(true);
                    this.mRoot.setDefaultFramerate(1000.0f / ((float) this.mUpdateInterval));
                    if (!this.mRoot.load()) {
                        return;
                    }
                    if (this.mRoot != null) {
                        this.mRoot.setOnExternCommandListener(this);
                        RenderThread globalThread = RenderThread.globalThread();
                        if (!globalThread.isStarted()) {
                            try {
                                globalThread.start();
                            } catch (IllegalThreadStateException e2) {
                            }
                        }
                        this.mAwesomeView = new AwesomeGadgetView(this.mContext, this.mRoot, globalThread);
                        this.mAwesomeView.setFocusable(false);
                        addView(this.mAwesomeView, new LayoutParams(-1, -1));
                        return;
                    }
                    return;
                }
                throw new Exception("bad root tag " + root.getNodeName());
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
    }

    public void onStart() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.onResume();
        }
    }

    public void onStop() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.onPause();
        }
    }

    public void onEditDisable() {
    }

    public void onEditNormal() {
    }

    public void onAdded() {
    }

    public void onDeleted() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp();
        }
    }

    public void onCommand(String command, Double para1, final String strPara) {
        if ("start_activity".equals(command)) {
            post(new Runnable() {
                public void run() {
                    Utilities.startActivity(AwesomeGadget.this.getContext(), strPara, AwesomeGadget.this);
                }
            });
        } else {
            Log.w(LOG_TAG, "Unsupported command: " + command);
        }
    }
}
