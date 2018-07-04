package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.ImageView;
import com.miui.home.R;

public class ThumbnailIcon extends ImageView {
    private boolean mDrawTouchMask = false;
    private boolean mEnableDrawMaskOnPressed = true;
    private int mMaskColor;

    public ThumbnailIcon(Context context) {
        super(context);
    }

    public ThumbnailIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void enableDrawMaskOnPressed(boolean enable) {
        this.mEnableDrawMaskOnPressed = enable;
    }

    protected void onFinishInflate() {
        this.mMaskColor = getResources().getColor(R.color.folder_foreground_mask);
    }

    protected void drawableStateChanged() {
        boolean drawMask = StateSet.stateSetMatches(PRESSED_STATE_SET, getDrawableState());
        if (this.mDrawTouchMask != drawMask) {
            this.mDrawTouchMask = drawMask;
            invalidate();
        }
        super.drawableStateChanged();
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.save();
        if (this.mEnableDrawMaskOnPressed && this.mDrawTouchMask) {
            canvas.drawColor(this.mMaskColor, Mode.SRC_ATOP);
        }
        canvas.restore();
    }

    public boolean isInScrollingContainer() {
        return false;
    }
}
