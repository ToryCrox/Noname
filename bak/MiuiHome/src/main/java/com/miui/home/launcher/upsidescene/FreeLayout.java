package com.miui.home.launcher.upsidescene;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import com.miui.home.R;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.upsidescene.data.Function.SystemGadgetFunction;
import com.miui.home.launcher.upsidescene.data.Function.WidgetFunction;
import com.miui.home.launcher.upsidescene.data.Screen;
import com.miui.home.launcher.upsidescene.data.Sprite;

public class FreeLayout extends ViewGroup {
    private static Rect mTmpRect = new Rect();
    private SceneScreen mSceneScreen;
    private Screen mScreenData;

    public static class LayoutParams extends MarginLayoutParams {
        public int left;
        public int top;

        public LayoutParams() {
            super(-2, -2);
        }
    }

    public FreeLayout(Context context) {
        super(context);
    }

    public FreeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FreeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setSceneScreen(SceneScreen sceneScreen) {
        this.mSceneScreen = sceneScreen;
    }

    public void setScreenData(Screen screenData) {
        this.mScreenData = screenData;
        getLayoutParams().width = screenData.getWidth();
        removeAllViews();
        for (Sprite sprite : this.mScreenData.getSprites()) {
            addSpriteView(sprite);
        }
    }

    public Screen getScreenData() {
        return this.mScreenData;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == 0 || heightSpecMode == 0) {
            widthSpecSize = this.mScreenData.getWidth();
            heightSpecSize = this.mScreenData.getHeight();
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            measureChild(getChildAt(i));
        }
    }

    private void measureChild(View child) {
        int childWidthSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(0, 0);
        if (child.getLayoutParams().width > 0) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(child.getLayoutParams().width, 1073741824);
            childHeightSpec = MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, 1073741824);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                int childLeft = lp.left;
                int childTop = lp.top;
                child.layout(childLeft, childTop, child.getMeasuredWidth() + childLeft, child.getMeasuredHeight() + childTop);
            }
        }
    }

    public void notifyGadgets(int state) {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof SpriteView) {
                ((SpriteView) child).notifyGadget(state);
            }
        }
    }

    public void bringToTop(SpriteView spriteView) {
        this.mSceneScreen.getFreeStyle().bringSpriteToTop(this.mScreenData, spriteView.getSpriteData());
        spriteView.bringToFront();
    }

    public SpriteView addSprite(Sprite sprite) {
        this.mSceneScreen.getFreeStyle().addSprite(this.mScreenData, sprite);
        return addSpriteView(sprite);
    }

    public SpriteView addSpriteView(Sprite sprite) {
        View contentView = SpriteView.createContentView(sprite, this.mSceneScreen, this.mContext);
        if (contentView == null) {
            return null;
        }
        SpriteView spriteView = new SpriteView(this.mContext, sprite, this.mSceneScreen, contentView);
        LayoutParams lp = new LayoutParams();
        lp.top = sprite.getTop();
        lp.left = sprite.getLeft();
        if (sprite.getWidth() > 0 && sprite.getHeight() > 0) {
            lp.width = sprite.getWidth();
            lp.height = sprite.getHeight();
        } else if (sprite.getFunction().getType() == 4) {
            float cellWidth = getResources().getDimension(R.dimen.freeStyleWidthCellWidth);
            float cellHeight = getResources().getDimension(R.dimen.freeStyleWidthCellHeight);
            GadgetInfo gadgetInfo = GadgetFactory.getInfo(((SystemGadgetFunction) sprite.getFunction()).getGadgetId());
            lp.width = (int) (((float) gadgetInfo.spanX) * cellWidth);
            lp.height = (int) (((float) gadgetInfo.spanY) * cellHeight);
        }
        addView(spriteView, lp);
        return spriteView;
    }

    public void removeSprite(SpriteView spriteView) {
        if (spriteView.getSpriteData().getFunction().getType() == 5) {
            this.mSceneScreen.getAppWidgetHost().deleteAppWidgetId(((WidgetFunction) spriteView.getSpriteData().getFunction()).getId());
        } else if (spriteView.getContentView() instanceof Gadget) {
            ((Gadget) spriteView.getContentView()).onDeleted();
        }
        this.mSceneScreen.getFreeStyle().removeSprite(this.mScreenData, spriteView.getSpriteData());
        removeView(spriteView);
    }

    public void gotoEditMode() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof SpriteView) {
                ((SpriteView) child).gotoEditMode();
            }
        }
    }

    public void exitEditMode() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof SpriteView) {
                ((SpriteView) child).exitEditMode();
            }
        }
    }

    public void gotoMoveMode() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof SpriteView) {
                ((SpriteView) child).gotoMoveMode();
            }
        }
    }

    public void exitMoveMode() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (child instanceof SpriteView) {
                ((SpriteView) child).exitMoveMode();
            }
        }
    }
}
