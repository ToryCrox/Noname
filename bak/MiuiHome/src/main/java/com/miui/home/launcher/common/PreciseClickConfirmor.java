package com.miui.home.launcher.common;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class PreciseClickConfirmor {
    private boolean mConfirmClick;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mTouchSlop;

    public PreciseClickConfirmor(Context context) {
        this.mTouchSlop = (int) Math.sqrt((double) ViewConfiguration.get(context).getScaledTouchSlop());
    }

    public void onTouchEvent(MotionEvent ev) {
        boolean z = true;
        switch (ev.getAction()) {
            case 0:
                this.mLastTouchX = ev.getX();
                this.mLastTouchY = ev.getY();
                this.mConfirmClick = true;
                return;
            case 1:
            case 2:
                if (this.mConfirmClick) {
                    if (Math.abs(this.mLastTouchX - ev.getX()) >= ((float) this.mTouchSlop) && Math.abs(this.mLastTouchY - ev.getY()) >= ((float) this.mTouchSlop)) {
                        z = false;
                    }
                    this.mConfirmClick = z;
                    return;
                }
                return;
            default:
                return;
        }
    }

    public boolean confirmClick() {
        return this.mConfirmClick;
    }
}
