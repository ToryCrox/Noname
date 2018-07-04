package com.miui.home.launcher;

import android.view.View;

public interface DropTarget {
    boolean acceptDrop(DragObject dragObject);

    DropTarget getDropTargetDelegate(DragObject dragObject);

    View getHitView();

    boolean isDropEnabled();

    void onDragEnter(DragObject dragObject);

    void onDragExit(DragObject dragObject);

    void onDragOver(DragObject dragObject);

    boolean onDrop(DragObject dragObject);

    void onDropCompleted();

    void onDropStart(DragObject dragObject);
}
