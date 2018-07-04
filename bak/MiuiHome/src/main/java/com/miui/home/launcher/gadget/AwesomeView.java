package com.miui.home.launcher.gadget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import miui.maml.RenderThread;
import miui.maml.RendererController.IRenderable;
import miui.maml.ScreenContext;
import miui.maml.ScreenElementRoot;
import miui.maml.util.Utils;
import org.w3c.dom.Element;

public class AwesomeView extends View implements IRenderable {
    private ScreenElementRoot mRoot;
    private int mTargetDensity;
    private int mUpdateInterval;

    public AwesomeView(Context context) {
        super(context);
    }

    public AwesomeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mRoot != null) {
            try {
                this.mRoot.render(canvas);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("gadget_clock", e.toString());
            } catch (OutOfMemoryError e2) {
                e2.printStackTrace();
                Log.e("gadget_clock", e2.toString());
            }
        }
    }

    public AwesomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean load(ScreenContext elementContext) {
        try {
            Element root = elementContext.mResourceManager.getManifestRoot();
            if ("clock".equalsIgnoreCase(root.getNodeName())) {
                this.mRoot = new ScreenElementRoot(elementContext);
                this.mRoot.setScaleByDensity(true);
                if (!this.mRoot.load()) {
                    return false;
                }
                this.mRoot.setRenderControllerRenderable(this);
                this.mRoot.attachToRenderThread(RenderThread.globalThread(true));
                this.mRoot.selfInit();
                try {
                    this.mUpdateInterval = Integer.parseInt(root.getAttribute("update_interval"));
                } catch (NumberFormatException e) {
                    this.mUpdateInterval = 60000;
                }
                return true;
            }
            throw new Exception("bad root tag " + root.getNodeName());
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.mRoot != null) {
            Utils.putVariableNumber("view_width", this.mRoot.getContext().mVariables, Double.valueOf((double) (right - left)).doubleValue() / ((double) this.mRoot.getScale()));
            Utils.putVariableNumber("view_height", this.mRoot.getContext().mVariables, Double.valueOf((double) (bottom - top)).doubleValue() / ((double) this.mRoot.getScale()));
        }
    }

    public int getUpdateInterval() {
        return this.mUpdateInterval;
    }

    public void resume() {
        if (this.mRoot != null) {
            this.mRoot.selfResume();
        }
    }

    public void pause() {
        if (this.mRoot != null) {
            this.mRoot.selfPause();
        }
    }

    public void finish() {
        if (this.mRoot != null) {
            this.mRoot.selfFinish();
        }
    }

    public void tick(long currentTime) {
        if (this.mRoot != null) {
            this.mRoot.tick(currentTime);
        }
    }

    public void doRender() {
        postInvalidate();
    }

    void setTargetDensity(int density) {
        this.mTargetDensity = density;
    }
}
