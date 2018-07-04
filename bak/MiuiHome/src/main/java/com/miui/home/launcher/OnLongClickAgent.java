package com.miui.home.launcher;

import android.view.MotionEvent;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

public class OnLongClickAgent {
    private static int MOVE_THRESHOLD = 15;
    private ViewGroup mClientView;
    private float mDonwX;
    private float mDonwY;
    private boolean mHasPerformedLongPress;
    private boolean mIsLongPressCheckPending;
    private Launcher mLauncher;
    private OnLongClickListener mOnLongClickListener;
    private CheckForLongPress mPendingCheckForLongPress;
    private long mTimeOut = 0;
    private VersionTagGenerator mVersionTagGenerator;

    public interface VersionTagGenerator {
        Object getVersionTag();
    }

    class CheckForLongPress implements Runnable {
        private Object zOriginalVersionTag;

        CheckForLongPress() {
        }

        public void run() {
            if (OnLongClickAgent.this.mIsLongPressCheckPending && OnLongClickAgent.this.mClientView.hasWindowFocus() && OnLongClickAgent.this.mClientView.getParent() != null && this.zOriginalVersionTag == OnLongClickAgent.this.mVersionTagGenerator.getVersionTag()) {
                if (OnLongClickAgent.this.mOnLongClickListener != null) {
                    OnLongClickAgent.this.mOnLongClickListener.onLongClick(OnLongClickAgent.this.mClientView);
                }
                OnLongClickAgent.this.mHasPerformedLongPress = true;
                OnLongClickAgent.this.mIsLongPressCheckPending = false;
            }
        }

        public void rememberVersionTag() {
            this.zOriginalVersionTag = OnLongClickAgent.this.mVersionTagGenerator.getVersionTag();
        }
    }

    public OnLongClickAgent(ViewGroup client, Launcher launcher, VersionTagGenerator versionTagGenerator) {
        this.mClientView = client;
        this.mLauncher = launcher;
        this.mVersionTagGenerator = versionTagGenerator;
    }

    public boolean onDispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction() & 255;
        if (this.mHasPerformedLongPress) {
            this.mHasPerformedLongPress = false;
            if (action != 0) {
                return true;
            }
        }
        switch (action) {
            case 0:
                this.mDonwX = ev.getX();
                this.mDonwY = ev.getY();
                postCheckForLongClick();
                return false;
            case 1:
            case 3:
                break;
            case 2:
                if (Math.abs(this.mDonwX - ev.getX()) < ((float) MOVE_THRESHOLD) && Math.abs(this.mDonwY - ev.getY()) < ((float) MOVE_THRESHOLD)) {
                    return false;
                }
            default:
                return false;
        }
        cancelCustomziedLongPress();
        return false;
    }

    public boolean isClickable() {
        return this.mLauncher == null || !this.mLauncher.isInEditing();
    }

    public void setTimeOut(long timeOut) {
        if (timeOut < 0) {
            timeOut = 0;
        }
        this.mTimeOut = timeOut;
    }

    private void postCheckForLongClick() {
        if (!this.mLauncher.isPrivacyModeEnabled()) {
            reset();
            if (this.mPendingCheckForLongPress == null) {
                this.mPendingCheckForLongPress = new CheckForLongPress();
            }
            this.mPendingCheckForLongPress.rememberVersionTag();
            if (this.mTimeOut == 0) {
                this.mTimeOut = this.mLauncher.isInEditing() ? 200 : 500;
            }
            this.mClientView.postDelayed(this.mPendingCheckForLongPress, this.mTimeOut);
            this.mIsLongPressCheckPending = true;
        }
    }

    public void cancelCustomziedLongPress() {
        this.mTimeOut = 0;
        reset();
    }

    private void reset() {
        this.mHasPerformedLongPress = false;
        this.mIsLongPressCheckPending = false;
        if (this.mPendingCheckForLongPress != null) {
            this.mClientView.removeCallbacks(this.mPendingCheckForLongPress);
        }
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mOnLongClickListener = l;
    }
}
