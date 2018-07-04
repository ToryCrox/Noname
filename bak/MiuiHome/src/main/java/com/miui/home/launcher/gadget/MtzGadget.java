package com.miui.home.launcher.gadget;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.AnalyticalDataCollector;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.Utilities;
import java.io.File;
import miui.maml.RenderThread;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.ScreenElementRoot.OnExternCommandListener;
import miui.maml.util.Utils;
import miui.maml.util.ZipResourceLoader;
import org.w3c.dom.Element;

public class MtzGadget extends Gadget {
    private static String ROOT_TAG = "gadget";
    private AwesomeGadgetView mAwesomeView;
    private OnExternCommandListener mCommandListener;
    private ScreenContext mElementContext;
    private GestureDetector mGestureDetector;
    private boolean mIsPlayer;
    private ScreenElementRoot mRoot;

    public MtzGadget(Context context, GadgetInfo info) {
        boolean z = true;
        super(context);
        if (!info.isMtzGadget()) {
            inflate(context, R.layout.appwidget_error, this);
        } else if (new File(info.getMtzUri().getPath()).exists()) {
            if (info.getCategoryId() != 1) {
                z = false;
            }
            this.mIsPlayer = z;
            this.mElementContext = new ScreenContext(this.mContext, new ZipResourceLoader(info.getMtzUri().getPath(), "content/").setLocal(context.getResources().getConfiguration().locale));
            setWillNotDraw(false);
        } else {
            ((TextView) inflate(getContext(), R.layout.appwidget_error, this).findViewById(R.id.error)).setText(R.string.mtzgadget_missing_text);
        }
        this.mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {
            public boolean onSingleTapUp(MotionEvent e) {
                MtzGadget.this.trackClick();
                return super.onSingleTapUp(e);
            }
        });
    }

    public void onDestroy() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp();
        }
    }

    public void onPause() {
        super.onPause();
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
        super.onResume();
        if (this.mRoot != null) {
            synchronized (this.mRoot) {
                this.mRoot.onCommand("resume");
            }
        }
        if (this.mAwesomeView != null) {
            this.mAwesomeView.onResume();
        }
    }

    public void updateConfig(Bundle config) {
    }

    public void onCreate() {
        if (this.mElementContext != null) {
            try {
                Element root = this.mElementContext.mResourceManager.getManifestRoot();
                if (root == null) {
                    return;
                }
                if (ROOT_TAG.equalsIgnoreCase(root.getNodeName())) {
                    this.mRoot = new ScreenElementRoot(this.mElementContext);
                    this.mRoot.setScaleByDensity(true);
                    this.mRoot.setDefaultFramerate(0.0f);
                    if (!this.mRoot.load()) {
                        return;
                    }
                    if (this.mRoot != null) {
                        this.mCommandListener = new OnExternCommandListener() {
                            public void onCommand(String command, Double numPara, final String strPara) {
                                if (MtzGadget.this.mIsPlayer && command.startsWith("track_music")) {
                                    AnalyticalDataCollector.trackMusicEvent(MtzGadget.this.mContext, command, strPara);
                                } else if (command.equals("start_activity")) {
                                    MtzGadget.this.post(new Runnable() {
                                        public void run() {
                                            Utilities.startActivity(MtzGadget.this.getContext(), strPara, MtzGadget.this);
                                        }
                                    });
                                }
                            }
                        };
                        this.mRoot.setOnExternCommandListener(this.mCommandListener);
                        this.mAwesomeView = new AwesomeGadgetView(this.mContext, this.mRoot, RenderThread.globalThread(true));
                        this.mAwesomeView.setFocusable(false);
                        addView(this.mAwesomeView, new LayoutParams(-1, -1));
                        return;
                    }
                    return;
                }
                throw new Exception("bad root tag " + root.getNodeName());
            } catch (Exception e) {
                e.printStackTrace();
                inflate(getContext(), R.layout.appwidget_error, this);
            }
        }
    }

    public void onStart() {
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

    public void onStop() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.onPause();
        }
    }

    public void onEditDisable() {
    }

    public void onEditNormal() {
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void onAdded() {
    }

    public void onDeleted() {
        if (this.mAwesomeView != null) {
            this.mAwesomeView.cleanUp();
        }
    }
}
