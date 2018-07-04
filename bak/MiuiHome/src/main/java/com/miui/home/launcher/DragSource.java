package com.miui.home.launcher;

public interface DragSource {
    void onDragCompleted(DropTarget dropTarget, DragObject dragObject);

    void onDropBack(DragObject dragObject);
}
