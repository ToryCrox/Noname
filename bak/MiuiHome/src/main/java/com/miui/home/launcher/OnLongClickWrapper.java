package com.miui.home.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.launcher.OnLongClickAgent.VersionTagGenerator;

public class OnLongClickWrapper extends FrameLayout implements VersionTagGenerator {
    private OnLongClickAgent mOnLongClickAgent;

    public OnLongClickWrapper(Launcher launcher) {
        super(launcher);
        super.setClickable(true);
        setLauncher(launcher);
    }

    public OnLongClickWrapper(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
    }

    public void setLauncher(Launcher launcher) {
        this.mOnLongClickAgent = new OnLongClickAgent(this, launcher, this);
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mOnLongClickAgent == null || !this.mOnLongClickAgent.onDispatchTouchEvent(ev)) {
            return super.dispatchTouchEvent(ev);
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if (preventPressState() && !isClickable()) {
            setPressed(false);
            super.cancelLongPress();
        }
        return result;
    }

    public boolean preventPressState() {
        return false;
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        if (this.mOnLongClickAgent != null) {
            this.mOnLongClickAgent.setOnLongClickListener(l);
        }
    }

    public void cancelLongPress() {
        if (this.mOnLongClickAgent != null) {
            this.mOnLongClickAgent.cancelCustomziedLongPress();
        }
    }

    public Object getVersionTag() {
        return Integer.valueOf(getWindowAttachCount());
    }

    public boolean isClickable() {
        return (this.mOnLongClickAgent == null || this.mOnLongClickAgent.isClickable()) && super.isClickable();
    }

    public void addView(View child) {
        super.addView(child, new LayoutParams(-1, -1));
    }
}
