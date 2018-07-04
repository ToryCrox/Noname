package com.miui.home.launcher.gadget;

import android.content.Context;
import android.view.MotionEvent;
import miui.maml.MiAdvancedView;
import miui.maml.RenderThread;
import miui.maml.ScreenElementRoot;
import miui.maml.elements.ButtonScreenElement;

public class AwesomeGadgetView extends MiAdvancedView {
    private ButtonScreenElement mClickableArea;

    public AwesomeGadgetView(Context context, ScreenElementRoot root, RenderThread t) {
        super(context, root, t);
        try {
            this.mClickableArea = (ButtonScreenElement) this.mRoot.findElement("clickable_area");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mRoot != null) {
            boolean b = this.mRoot.needDisallowInterceptTouchEvent();
            if (this.mNeedDisallowInterceptTouchEvent != b) {
                getParent().requestDisallowInterceptTouchEvent(b);
                this.mNeedDisallowInterceptTouchEvent = b;
            }
            if (this.mClickableArea == null || this.mClickableArea.touched(event.getX(), event.getY())) {
                this.mRoot.postMessage(MotionEvent.obtain(event));
                return true;
            }
        }
        return false;
    }
}
