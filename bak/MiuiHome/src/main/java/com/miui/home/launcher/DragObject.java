package com.miui.home.launcher;

import android.graphics.Bitmap;

public class DragObject {
    public boolean automatic;
    public int dragAction;
    public DragSource dragSource = null;
    public int dropAction;
    private int mCurrentIndex;
    private DragView[] mDragViews;
    private int mDropAnimationCounter = 0;
    private boolean mIsAllDropedSuccess;
    private boolean mIsDroped;
    public Runnable postAnimationRunnable = null;
    public boolean removeDragViewsAtLast = false;
    public int x = -1;
    public int xOffset = -1;
    public int y = -1;
    public int yOffset = -1;

    public boolean isDroped() {
        return this.mIsDroped;
    }

    public boolean isAllDropedSuccess() {
        return this.mIsAllDropedSuccess;
    }

    public boolean isMultiDrag() {
        return this.mDragViews.length > 1;
    }

    public boolean atLeastOneDropSucceeded() {
        if (this.mDragViews[0].isDropSucceeded()) {
            return true;
        }
        return false;
    }

    public void onDragCompleted() {
        onDragCompleted(false);
    }

    public void onDragCompleted(boolean isCanceled) {
        this.mIsDroped = true;
        this.dropAction = 3;
        int count = this.mDragViews.length;
        this.mCurrentIndex = 0;
        while (this.mCurrentIndex < count) {
            if (!getDragView().isDropSucceeded()) {
                this.dragSource.onDropBack(this);
                this.mIsAllDropedSuccess = false;
            }
            this.mCurrentIndex++;
        }
        Launcher.performLayoutNow(this.mDragViews[0].getRootView());
        this.mCurrentIndex = 0;
        while (this.mCurrentIndex < count) {
            DragView dv = getDragView();
            if (isCanceled) {
                dv.setCanceledMode();
            }
            dv.animateToTarget();
            this.mCurrentIndex++;
        }
        this.mCurrentIndex = 0;
    }

    public int getDraggingSize() {
        return this.mDragViews.length;
    }

    public DragView getDragView() {
        return this.mDragViews[this.mCurrentIndex];
    }

    public ItemInfo getDragInfo() {
        return this.mDragViews[this.mCurrentIndex].getDragInfo();
    }

    public Bitmap getOutline() {
        return this.mDragViews[this.mCurrentIndex].getOutline();
    }

    public boolean nextDragView(boolean isDropSucceeded) {
        if (isDropSucceeded) {
            this.mDragViews[this.mCurrentIndex].setDropSucceed();
        }
        this.mCurrentIndex++;
        if (this.mCurrentIndex < this.mDragViews.length) {
            return true;
        }
        this.mCurrentIndex = 0;
        return false;
    }

    public ItemInfo getDragInfo(int index) {
        if (index < 0 || index >= this.mDragViews.length) {
            return null;
        }
        return this.mDragViews[index].getDragInfo();
    }

    public boolean isFirstObject() {
        return this.mCurrentIndex == 0;
    }

    public boolean isLastObject() {
        return this.mCurrentIndex == this.mDragViews.length + -1;
    }

    public void move(int touchX, int touchY) {
        for (DragView dv : this.mDragViews) {
            dv.move(touchX, touchY);
        }
    }

    public void onDropAnimationStart(DragView dragView) {
        this.mDropAnimationCounter++;
    }

    public void onDropAnimationFinished(DragView dragView) {
        this.mDropAnimationCounter--;
        if (!this.removeDragViewsAtLast || dragView.isCanceledMode()) {
            dragView.remove();
        } else if (this.mDropAnimationCounter == 0) {
            for (DragView dv : this.mDragViews) {
                dv.remove();
            }
        }
    }

    public DragObject(DragView[] dragViews) {
        this.mDragViews = dragViews;
        for (DragView dragView : dragViews) {
            dragView.setDragGroup(this);
        }
        this.mCurrentIndex = 0;
        this.mIsDroped = false;
        this.mIsAllDropedSuccess = true;
    }
}
