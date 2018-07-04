package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Rect;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;

public class AppWidgetResizeFrame extends FrameLayout {
    private static Rect mTmpRect = new Rect();
    final int BACKGROUND_PADDING = 24;
    final float DIMMED_HANDLE_ALPHA = 0.0f;
    final float RESIZE_THRESHOLD = 0.66f;
    final int SNAP_DURATION = 150;
    private int mBackgroundPadding;
    private int mBaselineHeight;
    private int mBaselineWidth;
    private int mBaselineX;
    private int mBaselineY;
    private boolean mBottomBorderActive;
    private ImageView mBottomHandle;
    private int mBottomTouchRegionAdjustment = 0;
    private CellLayout mCellLayout;
    private int mDeltaX;
    private int mDeltaXAddOn;
    private int mDeltaY;
    private int mDeltaYAddOn;
    int[] mDirectionVector = new int[2];
    private DragLayer mDragLayer;
    int[] mLastDirectionVector = new int[2];
    private Launcher mLauncher;
    private boolean mLeftBorderActive;
    private ImageView mLeftHandle;
    private int mMinHSpan;
    private int mMinVSpan;
    private int mResizeMode;
    private boolean mRightBorderActive;
    private ImageView mRightHandle;
    private int mRunningHInc;
    private int mRunningVInc;
    float[] mTmpPt = new float[2];
    private boolean mTopBorderActive;
    private ImageView mTopHandle;
    private int mTopTouchRegionAdjustment = 0;
    private int mTouchTargetWidth;
    private int mWidgetPaddingBottom;
    private int mWidgetPaddingLeft;
    private int mWidgetPaddingRight;
    private int mWidgetPaddingTop;
    private LauncherAppWidgetHostView mWidgetView;

    public AppWidgetResizeFrame(Context context, LauncherAppWidgetHostView widgetView, CellLayout cellLayout, DragLayer dragLayer) {
        super(context);
        this.mLauncher = (Launcher) context;
        this.mCellLayout = cellLayout;
        this.mWidgetView = widgetView;
        this.mResizeMode = widgetView.getAppWidgetInfo().resizeMode;
        this.mDragLayer = dragLayer;
        AppWidgetProviderInfo info = widgetView.getAppWidgetInfo();
        this.mMinHSpan = DeviceConfig.getWidgetSpanX(info.minResizeWidth);
        this.mMinVSpan = DeviceConfig.getWidgetSpanY(info.minResizeHeight);
        setBackgroundResource(R.drawable.widget_resize_frame_holo);
        setPadding(0, 0, 0, 0);
        this.mLeftHandle = new ImageView(context);
        this.mLeftHandle.setImageResource(R.drawable.widget_resize_handle_left);
        addView(this.mLeftHandle, new LayoutParams(-2, -2, 19));
        this.mRightHandle = new ImageView(context);
        this.mRightHandle.setImageResource(R.drawable.widget_resize_handle_right);
        addView(this.mRightHandle, new LayoutParams(-2, -2, 21));
        this.mTopHandle = new ImageView(context);
        this.mTopHandle.setImageResource(R.drawable.widget_resize_handle_top);
        addView(this.mTopHandle, new LayoutParams(-2, -2, 49));
        this.mBottomHandle = new ImageView(context);
        this.mBottomHandle.setImageResource(R.drawable.widget_resize_handle_bottom);
        addView(this.mBottomHandle, new LayoutParams(-2, -2, 81));
        Rect p = AppWidgetHostView.getDefaultPaddingForWidget(context, widgetView.getAppWidgetInfo().provider, null);
        this.mWidgetPaddingLeft = p.left;
        this.mWidgetPaddingTop = p.top;
        this.mWidgetPaddingRight = p.right;
        this.mWidgetPaddingBottom = p.bottom;
        if (this.mResizeMode == 1) {
            this.mTopHandle.setVisibility(8);
            this.mBottomHandle.setVisibility(8);
        } else if (this.mResizeMode == 2) {
            this.mLeftHandle.setVisibility(8);
            this.mRightHandle.setVisibility(8);
        }
        this.mBackgroundPadding = (int) Math.ceil((double) (24.0f * this.mLauncher.getResources().getDisplayMetrics().density));
        this.mTouchTargetWidth = this.mBackgroundPadding * 2;
        this.mCellLayout.updateCellOccupiedMarks(this.mWidgetView, true);
    }

    public boolean beginResizeIfPointInRegion(int x, int y) {
        boolean z;
        boolean anyBordersActive;
        float f = 1.0f;
        boolean horizontalActive;
        if ((this.mResizeMode & 1) != 0) {
            horizontalActive = true;
        } else {
            horizontalActive = false;
        }
        boolean verticalActive;
        if ((this.mResizeMode & 2) != 0) {
            verticalActive = true;
        } else {
            verticalActive = false;
        }
        if (x >= this.mTouchTargetWidth || !horizontalActive) {
            z = false;
        } else {
            z = true;
        }
        this.mLeftBorderActive = z;
        if (x <= getWidth() - this.mTouchTargetWidth || !horizontalActive) {
            z = false;
        } else {
            z = true;
        }
        this.mRightBorderActive = z;
        if (y >= this.mTouchTargetWidth + this.mTopTouchRegionAdjustment || !verticalActive) {
            z = false;
        } else {
            z = true;
        }
        this.mTopBorderActive = z;
        if (y <= (getHeight() - this.mTouchTargetWidth) + this.mBottomTouchRegionAdjustment || !verticalActive) {
            z = false;
        } else {
            z = true;
        }
        this.mBottomBorderActive = z;
        if (this.mLeftBorderActive || this.mRightBorderActive || this.mTopBorderActive || this.mBottomBorderActive) {
            anyBordersActive = true;
        } else {
            anyBordersActive = false;
        }
        this.mBaselineWidth = getMeasuredWidth();
        this.mBaselineHeight = getMeasuredHeight();
        this.mBaselineX = getLeft();
        this.mBaselineY = getTop();
        if (anyBordersActive) {
            float f2;
            ImageView imageView = this.mLeftHandle;
            if (this.mLeftBorderActive) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            imageView.setAlpha(f2);
            imageView = this.mRightHandle;
            if (this.mRightBorderActive) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            imageView.setAlpha(f2);
            imageView = this.mTopHandle;
            if (this.mTopBorderActive) {
                f2 = 1.0f;
            } else {
                f2 = 0.0f;
            }
            imageView.setAlpha(f2);
            ImageView imageView2 = this.mBottomHandle;
            if (!this.mBottomBorderActive) {
                f = 0.0f;
            }
            imageView2.setAlpha(f);
        }
        return anyBordersActive;
    }

    public void updateDeltas(int deltaX, int deltaY) {
        if (this.mLeftBorderActive) {
            this.mDeltaX = Math.max(-this.mBaselineX, deltaX);
            this.mDeltaX = Math.min(this.mBaselineWidth - (this.mTouchTargetWidth * 2), this.mDeltaX);
        } else if (this.mRightBorderActive) {
            this.mDeltaX = Math.min(this.mDragLayer.getWidth() - (this.mBaselineX + this.mBaselineWidth), deltaX);
            this.mDeltaX = Math.max((-this.mBaselineWidth) + (this.mTouchTargetWidth * 2), this.mDeltaX);
        }
        if (this.mTopBorderActive) {
            this.mDeltaY = Math.max(-this.mBaselineY, deltaY);
            this.mDeltaY = Math.min(this.mBaselineHeight - (this.mTouchTargetWidth * 2), this.mDeltaY);
        } else if (this.mBottomBorderActive) {
            this.mDeltaY = Math.min(this.mDragLayer.getHeight() - (this.mBaselineY + this.mBaselineHeight), deltaY);
            this.mDeltaY = Math.max((-this.mBaselineHeight) + (this.mTouchTargetWidth * 2), this.mDeltaY);
        }
    }

    public void visualizeResizeForDelta(int deltaX, int deltaY) {
        visualizeResizeForDelta(deltaX, deltaY, false);
    }

    private void visualizeResizeForDelta(int deltaX, int deltaY, boolean onDismiss) {
        updateDeltas(deltaX, deltaY);
        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
        if (this.mLeftBorderActive) {
            lp.x = this.mBaselineX + this.mDeltaX;
            lp.width = this.mBaselineWidth - this.mDeltaX;
        } else if (this.mRightBorderActive) {
            lp.width = this.mBaselineWidth + this.mDeltaX;
        }
        if (this.mTopBorderActive) {
            lp.y = this.mBaselineY + this.mDeltaY;
            lp.height = this.mBaselineHeight - this.mDeltaY;
        } else if (this.mBottomBorderActive) {
            lp.height = this.mBaselineHeight + this.mDeltaY;
        }
        resizeWidgetIfNeeded(onDismiss);
        requestLayout();
    }

    private void resizeWidgetIfNeeded(boolean onDismiss) {
        float hSpanIncF = ((1.0f * ((float) (this.mDeltaX + this.mDeltaXAddOn))) / ((float) (this.mCellLayout.getCellWidth() + this.mCellLayout.getCellWidthGap()))) - ((float) this.mRunningHInc);
        float vSpanIncF = ((1.0f * ((float) (this.mDeltaY + this.mDeltaYAddOn))) / ((float) (this.mCellLayout.getCellHeight() + this.mCellLayout.getCellHeightGap()))) - ((float) this.mRunningVInc);
        int hSpanInc = 0;
        int vSpanInc = 0;
        int cellXInc = 0;
        int cellYInc = 0;
        int countX = DeviceConfig.getWidgetCellCountX();
        int countY = DeviceConfig.getWidgetCellCountY();
        if (Math.abs(hSpanIncF) > 0.66f) {
            hSpanInc = Math.round(hSpanIncF);
        }
        if (Math.abs(vSpanIncF) > 0.66f) {
            vSpanInc = Math.round(vSpanIncF);
        }
        if (onDismiss || hSpanInc != 0 || vSpanInc != 0) {
            ItemInfo info = (ItemInfo) this.mWidgetView.getTag();
            int spanX = info.spanX;
            int spanY = info.spanY;
            int cellX = info.cellX;
            int cellY = info.cellY;
            int hSpanDelta = 0;
            int vSpanDelta = 0;
            if (this.mLeftBorderActive) {
                cellXInc = Math.min(info.spanX - this.mMinHSpan, Math.max(-cellX, hSpanInc));
                hSpanInc = Math.max(-(info.spanX - this.mMinHSpan), Math.min(cellX, hSpanInc * -1));
                hSpanDelta = -hSpanInc;
            } else if (this.mRightBorderActive) {
                hSpanInc = Math.max(-(info.spanX - this.mMinHSpan), Math.min(countX - (cellX + spanX), hSpanInc));
                hSpanDelta = hSpanInc;
            }
            if (this.mTopBorderActive) {
                cellYInc = Math.min(info.spanY - this.mMinVSpan, Math.max(-cellY, vSpanInc));
                vSpanInc = Math.max(-(info.spanY - this.mMinVSpan), Math.min(cellY, vSpanInc * -1));
                vSpanDelta = -vSpanInc;
            } else if (this.mBottomBorderActive) {
                vSpanInc = Math.max(-(info.spanY - this.mMinVSpan), Math.min(countY - (cellY + spanY), vSpanInc));
                vSpanDelta = vSpanInc;
            }
            this.mDirectionVector[0] = 0;
            this.mDirectionVector[1] = 0;
            if (this.mLeftBorderActive || this.mRightBorderActive) {
                spanX += hSpanInc;
                cellX += cellXInc;
                if (hSpanDelta != 0) {
                    this.mDirectionVector[0] = this.mLeftBorderActive ? -1 : 1;
                }
            }
            if (this.mTopBorderActive || this.mBottomBorderActive) {
                spanY += vSpanInc;
                cellY += cellYInc;
                if (vSpanDelta != 0) {
                    this.mDirectionVector[1] = this.mTopBorderActive ? -1 : 1;
                }
            }
            if (onDismiss || vSpanDelta != 0 || hSpanDelta != 0) {
                if (onDismiss) {
                    this.mDirectionVector[0] = this.mLastDirectionVector[0];
                    this.mDirectionVector[1] = this.mLastDirectionVector[1];
                } else {
                    this.mLastDirectionVector[0] = this.mDirectionVector[0];
                    this.mLastDirectionVector[1] = this.mDirectionVector[1];
                }
                if (!this.mCellLayout.isCellOccupied(cellX, cellY, spanX, spanY)) {
                    info.cellX = cellX;
                    info.cellY = cellY;
                    info.spanX = spanX;
                    info.spanY = spanY;
                    this.mRunningVInc += vSpanDelta;
                    this.mRunningHInc += hSpanDelta;
                    if (!onDismiss) {
                        updateWidgetSizeRanges(spanX, spanY);
                    }
                }
                this.mWidgetView.requestLayout();
            }
        }
    }

    void updateWidgetSizeRanges(int spanX, int spanY) {
        this.mCellLayout.getWidgetMeasureSpec(spanX, spanY, mTmpRect);
        this.mWidgetView.updateAppWidgetSize(null, mTmpRect.left, mTmpRect.top, mTmpRect.right, mTmpRect.bottom);
    }

    public void commitResize() {
        resizeWidgetIfNeeded(true);
        this.mCellLayout.updateCellOccupiedMarks(this.mWidgetView, false);
        ItemInfo info = (ItemInfo) this.mWidgetView.getTag();
        LauncherModel.resizeItemInDatabase(getContext(), (ItemInfo) this.mWidgetView.getTag(), info.cellX, info.cellY, info.spanX, info.spanY);
        requestLayout();
    }

    public void onTouchUp() {
        int yThreshold = this.mCellLayout.getCellHeight() + this.mCellLayout.getCellHeightGap();
        this.mDeltaXAddOn = this.mRunningHInc * (this.mCellLayout.getCellWidth() + this.mCellLayout.getCellWidthGap());
        this.mDeltaYAddOn = this.mRunningVInc * yThreshold;
        this.mDeltaX = 0;
        this.mDeltaY = 0;
        post(new Runnable() {
            public void run() {
                AppWidgetResizeFrame.this.snapToWidget(true);
            }
        });
    }

    public void snapToWidget(boolean animate) {
        DragLayer.LayoutParams lp = (DragLayer.LayoutParams) getLayoutParams();
        int newWidth = ((this.mWidgetView.getWidth() + (this.mBackgroundPadding * 2)) - this.mWidgetPaddingLeft) - this.mWidgetPaddingRight;
        int newHeight = ((this.mWidgetView.getHeight() + (this.mBackgroundPadding * 2)) - this.mWidgetPaddingTop) - this.mWidgetPaddingBottom;
        this.mTmpPt[0] = (float) this.mWidgetView.getLeft();
        this.mTmpPt[1] = (float) this.mWidgetView.getTop();
        Utilities.getDescendantCoordRelativeToAncestor(this.mWidgetView, this.mDragLayer, this.mTmpPt, true, false);
        int newX = (int) ((this.mTmpPt[0] - ((float) this.mBackgroundPadding)) + ((float) this.mWidgetPaddingLeft));
        int newY = (int) ((this.mTmpPt[1] - ((float) this.mBackgroundPadding)) + ((float) this.mWidgetPaddingTop));
        if (newY < 0) {
            this.mTopTouchRegionAdjustment = -newY;
        } else {
            this.mTopTouchRegionAdjustment = 0;
        }
        if (newY + newHeight > this.mDragLayer.getHeight()) {
            this.mBottomTouchRegionAdjustment = -((newY + newHeight) - this.mDragLayer.getHeight());
        } else {
            this.mBottomTouchRegionAdjustment = 0;
        }
        if (animate) {
            PropertyValuesHolder width = PropertyValuesHolder.ofInt("width", new int[]{lp.width, newWidth});
            PropertyValuesHolder height = PropertyValuesHolder.ofInt("height", new int[]{lp.height, newHeight});
            PropertyValuesHolder x = PropertyValuesHolder.ofInt("x", new int[]{lp.x, newX});
            PropertyValuesHolder y = PropertyValuesHolder.ofInt("y", new int[]{lp.y, newY});
            ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(lp, this, width, height, x, y);
            ObjectAnimator leftOa = LauncherAnimUtils.ofFloat(this.mLeftHandle, "alpha", 1.0f);
            ObjectAnimator rightOa = LauncherAnimUtils.ofFloat(this.mRightHandle, "alpha", 1.0f);
            ObjectAnimator topOa = LauncherAnimUtils.ofFloat(this.mTopHandle, "alpha", 1.0f);
            ObjectAnimator bottomOa = LauncherAnimUtils.ofFloat(this.mBottomHandle, "alpha", 1.0f);
            oa.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    AppWidgetResizeFrame.this.requestLayout();
                }
            });
            AnimatorSet set = LauncherAnimUtils.createAnimatorSet();
            if (this.mResizeMode == 2) {
                set.playTogether(new Animator[]{oa, topOa, bottomOa});
            } else if (this.mResizeMode == 1) {
                set.playTogether(new Animator[]{oa, leftOa, rightOa});
            } else {
                set.playTogether(new Animator[]{oa, leftOa, rightOa, topOa, bottomOa});
            }
            set.setDuration(150);
            set.start();
            return;
        }
        lp.width = newWidth;
        lp.height = newHeight;
        lp.x = newX;
        lp.y = newY;
        this.mLeftHandle.setAlpha(1.0f);
        this.mRightHandle.setAlpha(1.0f);
        this.mTopHandle.setAlpha(1.0f);
        this.mBottomHandle.setAlpha(1.0f);
        requestLayout();
    }
}
