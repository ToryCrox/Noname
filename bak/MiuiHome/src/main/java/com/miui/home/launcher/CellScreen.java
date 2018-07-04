package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import java.lang.ref.SoftReference;
import miui.maml.FancyDrawable;
import miui.os.Build;

public class CellScreen extends FrameLayout implements OnClickListener, WallpaperColorChangedListener {
    private static final float EDITIMG_ANIM_TRANS_FROM_RATIO;
    private static DrawableWorker sOnVisibaleIconDrawableWorker = new DrawableWorker() {
        public void process(Drawable d) {
            if (d instanceof FancyDrawable) {
                ((FancyDrawable) d).onResume();
            }
        }

        public void process(ShortcutIcon icon) {
            if (((ShortcutInfo) icon.getTag()).needShowProgress()) {
                icon.onProgressStatusChanged();
            }
        }
    };
    public boolean autoScrolling = false;
    private AnimatorListenerAdapter mAnimComplateListener = new AnimatorListenerAdapter() {
        public void onAnimationStart(Animator animation) {
        }

        public void onAnimationEnd(Animator animation) {
            if (CellScreen.this.mWorkspace == null) {
                return;
            }
            if (CellScreen.this.mInEditing) {
                CellScreen.this.mWorkspace.onEditingModeEnterEnd();
            } else {
                CellScreen.this.mWorkspace.onEditingModeExitEnd();
            }
        }
    };
    private FrameLayout mBackgroundContainer;
    private float mCameraDistanceCache = 0.0f;
    private CellLayout mCellLayout;
    private ImageView mDeleteButton;
    private float mEditingAnimTransFrom;
    private float mEditingAnimTransTo;
    private SoftReference<Object> mEditingPreview = null;
    private boolean mInEditing = false;
    private boolean mIsEditingNewScreenMode = false;
    private ImageView mNewButton;
    private Workspace mWorkspace;
    private DrawableWorker sOnInVisibaleIconDrawableWorker = new DrawableWorker() {
        public void process(Drawable d) {
            if (d instanceof FancyDrawable) {
                ((FancyDrawable) d).onPause();
            }
        }

        public void process(ShortcutIcon icon) {
        }
    };

    private interface DrawableWorker {
        void process(Drawable drawable);

        void process(ShortcutIcon shortcutIcon);
    }

    static {
        float f;
        if (Build.IS_TABLET) {
            f = 0.043f;
        } else {
            f = -0.016f;
        }
        EDITIMG_ANIM_TRANS_FROM_RATIO = f;
    }

    public CellScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        this.mCellLayout = (CellLayout) findViewById(R.id.cell_layout);
        this.mCellLayout.animate().setListener(this.mAnimComplateListener).setDuration(370).setInterpolator(new AccelerateDecelerateInterpolator());
        this.mBackgroundContainer = (FrameLayout) findViewById(R.id.background_container);
        this.mDeleteButton = (ImageView) findViewById(R.id.delete_btn);
        this.mDeleteButton.setContentDescription(this.mContext.getString(R.string.delete_screen));
        this.mDeleteButton.setOnClickListener(this);
        this.mNewButton = (ImageView) findViewById(R.id.new_btn);
        this.mNewButton.setContentDescription(this.mContext.getString(R.string.add_screen));
        this.mNewButton.setOnClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mWorkspace = getParent() instanceof Workspace ? (Workspace) getParent() : null;
    }

    public void onWallpaperColorChanged() {
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mDeleteButton.setImageResource(R.drawable.editing_delete_screen_btn_dark);
            this.mNewButton.setImageResource(R.drawable.editing_new_screen_btn_dark);
        } else {
            this.mDeleteButton.setImageResource(R.drawable.editing_delete_screen_btn);
            this.mNewButton.setImageResource(R.drawable.editing_new_screen_btn);
        }
        WallpaperUtils.varyViewGroupByWallpaper(this);
    }

    public void addView(View child, int index, LayoutParams params) {
        WallpaperUtils.onAddViewToGroup(this, child, true);
        super.addView(child, index, params);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mWorkspace = null;
    }

    public CellLayout getCellLayout() {
        return this.mCellLayout;
    }

    private final float translateTouchX(float x) {
        return (1.1814744f * x) - ((((float) getMeasuredWidth()) * 0.18147445f) / 2.0f);
    }

    private final float translateTouchY(float y) {
        return (1.1814744f * y) - ((((float) getMeasuredHeight()) * 0.18147445f) / 3.0f);
    }

    void translateTouch(DragObject d) {
        if (this.mInEditing && d.dropAction == 1) {
            d.x = (int) translateTouchX((float) d.x);
            d.y = (int) translateTouchY((float) d.y);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int parentHeight = ((View) getParent()).getMeasuredHeight();
        this.mEditingAnimTransFrom = ((float) parentHeight) * EDITIMG_ANIM_TRANS_FROM_RATIO;
        this.mEditingAnimTransTo = ((float) parentHeight) * 0.05f;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean z;
        CellLayout cellLayout = this.mCellLayout;
        if (this.mInEditing && (isEditingNewScreenMode() || this.mCellLayout.getChildCount() == 0)) {
            z = true;
        } else {
            z = false;
        }
        cellLayout.setDisableTouch(z);
        return false;
    }

    public void onClick(View v) {
        if (this.mWorkspace == null) {
            return;
        }
        if (v == this.mDeleteButton) {
            this.mWorkspace.deleteScreen(this.mCellLayout.getScreenId(), true);
        } else if (v == this.mNewButton) {
            this.mWorkspace.insertNewScreen(-1, true);
        }
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mCellLayout.setOnLongClickListener(l);
    }

    public void updateLayout() {
        if (this.mInEditing) {
            if (isEditingNewScreenMode() || this.mCellLayout.getChildCount() != 0) {
                this.mDeleteButton.animate().cancel();
                if (this.mDeleteButton.getAlpha() != 0.0f) {
                    this.mDeleteButton.animate().alpha(0.0f).start();
                }
            } else if (this.mWorkspace != null && this.mWorkspace.isDeleteableScreen(this.mCellLayout.getScreenId())) {
                this.mDeleteButton.setVisibility(0);
                this.mDeleteButton.setAlpha(0.0f);
                this.mDeleteButton.animate().alpha(1.0f).start();
            }
            View parent = (View) getParent();
            if (parent != null) {
                this.mCellLayout.setPivotX((float) (parent.getWidth() / 2));
                this.mCellLayout.setPivotY(0.0f);
                this.mCellLayout.setScaleX(0.92f);
                this.mCellLayout.setScaleY(0.92f);
                this.mCellLayout.setTranslationY(this.mEditingAnimTransTo);
            }
        } else {
            this.mCellLayout.setTranslationY(0.0f);
            this.mCellLayout.setScaleX(1.0f);
            this.mCellLayout.setScaleY(1.0f);
        }
        this.mCellLayout.clearCellBackground();
    }

    void onEditingAnimationEnterStart() {
    }

    void onEditingAnimationEnterEnd() {
    }

    void onEditingAnimationExitStart() {
    }

    void onEditingAnimationExitEnd() {
        updateLayout();
    }

    void onQuickEditingModeChanged(boolean isEnable) {
    }

    public void onScreenOrientationChanged() {
        this.mCellLayout.onScreenOrientationChanged();
        ((MarginLayoutParams) this.mBackgroundContainer.getLayoutParams()).bottomMargin = DeviceConfig.getWorkspaceBackgroundMarginBottom();
    }

    public void onScreenSizeChanged() {
        this.mCellLayout.onScreenSizeChanged();
    }

    public void setEditMode(boolean isEditing, int curDist) {
        this.mInEditing = isEditing;
        this.mCellLayout.setEditMode(isEditing);
        updateLayout();
        if (isEditing) {
            this.mBackgroundContainer.setVisibility(0);
        } else {
            this.mBackgroundContainer.setVisibility(4);
        }
        switch (curDist) {
            case 0:
                if (this.mWorkspace != null) {
                    if (isEditing) {
                        this.mWorkspace.onEditingModeEnterStart();
                    } else {
                        this.mWorkspace.onEditingModeExitStart();
                    }
                }
                if (isEditing) {
                    this.mCellLayout.setScaleX(1.087f);
                    this.mCellLayout.setScaleY(1.087f);
                    this.mCellLayout.setTranslationY(this.mEditingAnimTransFrom);
                    this.mCellLayout.animate().scaleX(0.92f).scaleY(0.92f).translationY(this.mEditingAnimTransTo).start();
                    return;
                }
                this.mCellLayout.setScaleX(0.92f);
                this.mCellLayout.setScaleY(0.92f);
                this.mCellLayout.setTranslationY(this.mEditingAnimTransTo);
                this.mCellLayout.animate().scaleX(1.087f).scaleY(1.087f).translationY(this.mEditingAnimTransFrom).start();
                return;
            default:
                return;
        }
    }

    public boolean isEditingNewScreenMode() {
        return this.mIsEditingNewScreenMode;
    }

    public void setEditingNewScreenMode() {
        this.mNewButton.setVisibility(0);
        this.mCellLayout.setScreenId(-1);
        this.mIsEditingNewScreenMode = true;
    }

    public void setScreenType(int screenType) {
        this.mCellLayout.setScreenType(screenType);
    }

    void onDragEnter(DragObject d) {
        this.mCellLayout.onDragEnter(d);
    }

    void onDragExit(DragObject d) {
        clearDraggingState();
        this.mCellLayout.onDragExit(d);
    }

    void clearDraggingState() {
        if (isEditingNewScreenMode()) {
            this.mNewButton.setSelected(false);
        }
        this.mCellLayout.clearDraggingState();
    }

    void onDragOver(DragObject d) {
        if (isEditingNewScreenMode()) {
            this.mNewButton.setSelected(true);
            return;
        }
        translateTouch(d);
        this.mCellLayout.onDragOver(d);
    }

    void onDropStart(DragObject d) {
        this.mCellLayout.onDropStart(d);
    }

    public int[] findDropTargetPosition(DragObject d) {
        if (d.isFirstObject()) {
            translateTouch(d);
        }
        return this.mCellLayout.findDropTargetPosition(d);
    }

    boolean onDrop(DragObject d, View v) {
        if (d.isFirstObject()) {
            translateTouch(d);
        }
        return this.mCellLayout.onDrop(d, v);
    }

    public void buildDrawingCache(boolean autoScale) {
    }

    private void workOnAllCellLayoutIconDrawable(DrawableWorker worker) {
        for (int i = this.mCellLayout.getChildCount() - 1; i >= 0; i--) {
            View v = this.mCellLayout.getChildAt(i);
            if (v instanceof ShortcutIcon) {
                worker.process(((ShortcutIcon) v).getIcon().getDrawable());
                worker.process((ShortcutIcon) v);
            }
        }
    }

    public void onVisible() {
        workOnAllCellLayoutIconDrawable(sOnVisibaleIconDrawableWorker);
    }

    public void onInvisible() {
        workOnAllCellLayoutIconDrawable(this.sOnInVisibaleIconDrawableWorker);
    }

    public void setTag(int key, Object tag) {
        if (key == R.id.celllayout_thumbnail_for_workspace_editing_preview) {
            this.mEditingPreview = tag == null ? null : new SoftReference(tag);
        } else {
            super.setTag(key, tag);
        }
    }

    public Object getTag(int key) {
        if (key == R.id.celllayout_thumbnail_for_workspace_editing_preview) {
            return this.mEditingPreview == null ? null : this.mEditingPreview.get();
        } else {
            return super.getTag(key);
        }
    }

    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        updateVision();
        return super.invalidateChildInParent(location, dirty);
    }

    public void setCameraDistance(float distance) {
        if (distance != this.mCameraDistanceCache) {
            this.mCameraDistanceCache = distance;
            super.setCameraDistance(this.mCameraDistanceCache);
        }
    }

    void updateVision() {
        setTag(R.id.celllayout_thumbnail_for_workspace_editing_preview_dirty, Boolean.valueOf(true));
    }
}
