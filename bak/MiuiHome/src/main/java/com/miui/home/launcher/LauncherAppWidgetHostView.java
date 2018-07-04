package com.miui.home.launcher;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.RemoteViews;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.OnLongClickAgent.VersionTagGenerator;

public class LauncherAppWidgetHostView extends AppWidgetHostView implements HostView, VersionTagGenerator {
    private LayoutInflater mInflater;
    private boolean mIsEnableAutoLayoutAnimation = true;
    private Launcher mLauncher;
    private OnLongClickAgent mOnLongClickAgent;
    private boolean mSkipNextAutoLayoutAnimation = false;

    public LauncherAppWidgetHostView(Context context, Launcher launcher) {
        super(context);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mOnLongClickAgent = new OnLongClickAgent(this, launcher, this);
        this.mLauncher = launcher;
        setLayerType(2, null);
    }

    protected View getErrorView() {
        return this.mInflater.inflate(R.layout.appwidget_error, this, false);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mOnLongClickAgent.onDispatchTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mLauncher.isInEditing()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mOnLongClickAgent.setOnLongClickListener(l);
    }

    public void cancelLongPress() {
        this.mOnLongClickAgent.cancelCustomziedLongPress();
    }

    public void buildDrawingCache(boolean autoScale) {
    }

    public Object getVersionTag() {
        return Integer.valueOf(getWindowAttachCount());
    }

    public void setEnableAutoLayoutAnimation(boolean isEnable) {
        this.mIsEnableAutoLayoutAnimation = isEnable;
    }

    public void setSkipNextAutoLayoutAnimation(boolean isSkip) {
        this.mSkipNextAutoLayoutAnimation = isSkip;
    }

    public boolean getSkipNextAutoLayoutAnimation() {
        return this.mSkipNextAutoLayoutAnimation;
    }

    public boolean isEnableAutoLayoutAnimation() {
        return this.mIsEnableAutoLayoutAnimation;
    }

    public boolean superSetFrame(int left, int top, int right, int bottom) {
        return super.setFrame(left, top, right, bottom);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        return AutoLayoutAnimation.setFrame(this, left, top, right, bottom);
    }

    public void setGhostView(GhostView gv) {
    }

    public GhostView getGhostView() {
        return null;
    }

    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);
        if (getChildCount() == 1) {
            View content = getChildAt(0);
            if (content != null && (content.getLayoutParams() instanceof MarginLayoutParams)) {
                MarginLayoutParams lp = (MarginLayoutParams) content.getLayoutParams();
                lp.setMargins(0, 0, 0, 0);
                content.setLayoutParams(lp);
                content.requestLayout();
            }
        }
    }
}
