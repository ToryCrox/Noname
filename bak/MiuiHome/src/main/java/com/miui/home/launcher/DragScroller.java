package com.miui.home.launcher;

import android.view.MotionEvent;

public interface DragScroller {
    boolean onEnterScrollArea(int i, int i2, int i3);

    boolean onExitScrollArea();

    void onSecondaryPointerDown(MotionEvent motionEvent, int i);

    void onSecondaryPointerMove(MotionEvent motionEvent, int i);

    void onSecondaryPointerUp(MotionEvent motionEvent, int i);

    void scrollDragingLeft();

    void scrollDragingRight();
}
