package com.miui.home.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.common.CubicEaseInOutInterpolater;
import java.util.ArrayList;

public class WorkspaceThumbnailView extends DragableScreenView implements OnLongClickListener, DragSource, DropTarget {
    private static int SCROLL_ANIMATION_DURATION = 1000;
    private static int THUMBNAIL_CONTENT_PADDING_TOP = 10;
    private final OnClickListener DELETE_SCREEN_HANDLER;
    private final OnClickListener HOME_MARK_CLICK_HANDLER;
    private final OnClickListener NEW_SCREEN_HANDLER;
    private final OnClickListener THUMBNAIL_CLICK_HANDLER;
    private int mAnimationDuration;
    private AnimationListener mAnimationListener;
    private CellBackground mCellBackground;
    private ImageView mDefaultScreenButton;
    private DragController mDragController;
    private int mDraggedUpPos;
    private View mDraggingView;
    private View mFocusedThumbnail;
    private final LayoutInflater mInflater;
    private boolean mIsInAnimation;
    private boolean mIsShowing;
    private long mLastTouchLeftEdgeTime;
    private long mLastTouchRightEdgeTime;
    private ImageView mNewScreenView;
    private int mNewScreenViewIndex;
    private int mPlaceHolderIndex;
    private LongSparseArray<Integer> mScreenIdMap;
    private Drawable mSetHomeOn;
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private Workspace mWorkspace;

    public WorkspaceThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkspaceThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsShowing = false;
        this.mIsInAnimation = false;
        this.mDraggedUpPos = -1;
        this.mPlaceHolderIndex = -1;
        this.mNewScreenViewIndex = -1;
        this.mLastTouchLeftEdgeTime = -1;
        this.mLastTouchRightEdgeTime = -1;
        this.mScreenIdMap = new LongSparseArray();
        this.mThumbnailHeight = 0;
        this.mThumbnailWidth = 0;
        this.THUMBNAIL_CLICK_HANDLER = new OnClickListener() {
            public void onClick(View v) {
                if (!WorkspaceThumbnailView.this.mIsInAnimation && WorkspaceThumbnailView.this.mIsShowing) {
                    WorkspaceThumbnailView.this.mFocusedThumbnail = v;
                    WorkspaceThumbnailView.this.mWorkspace.exitPreview(((Long) v.getTag()).longValue());
                }
            }
        };
        this.DELETE_SCREEN_HANDLER = new OnClickListener() {
            public void onClick(View v) {
                if (!WorkspaceThumbnailView.this.mIsInAnimation && WorkspaceThumbnailView.this.mIsShowing) {
                    long screenId = ((Long) v.getTag()).longValue();
                    WorkspaceThumbnailView.this.removeScreen(((Integer) WorkspaceThumbnailView.this.mScreenIdMap.get(screenId)).intValue());
                    WorkspaceThumbnailView.this.refreshScreenIdMap();
                    WorkspaceThumbnailView.this.mWorkspace.deleteScreen(screenId, false);
                }
            }
        };
        this.HOME_MARK_CLICK_HANDLER = new OnClickListener() {
            public void onClick(View v) {
                if (!WorkspaceThumbnailView.this.mIsInAnimation && WorkspaceThumbnailView.this.mDefaultScreenButton != v) {
                    WorkspaceThumbnailView.this.mWorkspace.setDefaultScreenId(((Long) v.getTag()).longValue());
                    if (WorkspaceThumbnailView.this.mDefaultScreenButton != null) {
                        WorkspaceThumbnailView.this.mDefaultScreenButton.setImageResource(R.drawable.home_button_sethome_off);
                    }
                    WorkspaceThumbnailView.this.mDefaultScreenButton = (ImageView) v;
                    WorkspaceThumbnailView.this.mDefaultScreenButton.setImageResource(R.drawable.home_button_sethome_on);
                }
            }
        };
        this.NEW_SCREEN_HANDLER = new OnClickListener() {
            public void onClick(View v) {
                if (!WorkspaceThumbnailView.this.mIsInAnimation) {
                    WorkspaceThumbnailView.this.insertConvertView(WorkspaceThumbnailView.this.mWorkspace.insertNewScreen(WorkspaceThumbnailView.this.getScreenCount() - 1, false), WorkspaceThumbnailView.this.getScreenCount() - 1);
                    WorkspaceThumbnailView.this.refreshScreenIdMap();
                }
            }
        };
        this.mSetHomeOn = context.getResources().getDrawable(R.drawable.home_button_sethome_on);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mAnimationDuration = context.getResources().getInteger(17694722);
        this.mCellBackground = new CellBackground(context);
        this.mCellBackground.setImageAlpha(51);
        setScreenTransitionType(10);
        setScreenLayoutMode(4);
        setScrollWholeScreen(true);
        this.mThumbnailWidth = this.mSetHomeOn.getIntrinsicWidth();
        this.mThumbnailHeight = (int) (((float) (DeviceConfig.getScreenHeight() - ((int) context.getResources().getDimension(R.dimen.status_bar_height)))) * (((float) this.mSetHomeOn.getIntrinsicWidth()) / ((float) DeviceConfig.getScreenWidth())));
    }

    public void setResource(Workspace workspace) {
        this.mWorkspace = workspace;
    }

    public void setDragController(DragController d) {
        this.mDragController = d;
    }

    private ImageView getNewScreenView() {
        if (this.mNewScreenView == null) {
            this.mNewScreenView = new CellBackground(this.mContext);
            this.mNewScreenView.setImageResource(R.drawable.thumbnail_new_screen);
            this.mNewScreenView.setOnClickListener(this.NEW_SCREEN_HANDLER);
        }
        return this.mNewScreenView;
    }

    public void loadThumbnails(boolean isRefresh) {
        removeAllViews();
        this.mScreenIdMap.clear();
        for (int i = 0; i < this.mWorkspace.getScreenCount(); i++) {
            View thumbnail = insertConvertView(this.mWorkspace.getCellScreen(i), i);
            if (!isRefresh && (thumbnail instanceof HostView)) {
                ((HostView) thumbnail).setSkipNextAutoLayoutAnimation(true);
            }
            this.mScreenIdMap.append(((Long) thumbnail.getTag()).longValue(), Integer.valueOf(i));
        }
        View newScreenView = getNewScreenView();
        if (!isRefresh) {
            ((HostView) newScreenView).setSkipNextAutoLayoutAnimation(true);
        }
        newScreenView.setOnLongClickListener(this);
        addView(newScreenView);
        if (!(this.mNewScreenView == null || this.mNewScreenView.getLayoutParams() == null)) {
            this.mNewScreenView.getLayoutParams().width = this.mThumbnailWidth;
            this.mNewScreenView.getLayoutParams().height = this.mThumbnailHeight;
            this.mNewScreenView.setScaleType(ScaleType.FIT_XY);
        }
        Launcher.performLayoutNow(getRootView());
    }

    private View insertConvertView(CellScreen cs, int pos) {
        if (cs == null) {
            return null;
        }
        Drawable drawable;
        long screenId = cs.getCellLayout().getScreenId();
        View convertView = this.mInflater.inflate(R.layout.workspace_preview_item, null);
        convertView.setOnLongClickListener(this);
        convertView.setTag(Long.valueOf(screenId));
        ThumbnailContainer thumbnail = (ThumbnailContainer) convertView.findViewById(R.id.thumbnail);
        if (cs == this.mWorkspace.getCurrentCellScreen()) {
            drawable = getResources().getDrawable(R.drawable.thumbnail_bg_current);
        } else {
            drawable = getResources().getDrawable(R.drawable.thumbnail_bg);
        }
        thumbnail.setBackground(drawable);
        thumbnail.setOnLongClickListener(this);
        thumbnail.setOnClickListener(this.THUMBNAIL_CLICK_HANDLER);
        thumbnail.setTag(Long.valueOf(screenId));
        thumbnail.getLayoutParams().width = this.mThumbnailWidth;
        thumbnail.getLayoutParams().height = this.mThumbnailHeight;
        setCellLayoutThumbnail(cs, convertView);
        ImageView homeButton = (ImageView) convertView.findViewById(R.id.home_mark);
        homeButton.setTag(Long.valueOf(screenId));
        homeButton.setOnClickListener(this.HOME_MARK_CLICK_HANDLER);
        homeButton.setOnLongClickListener(this);
        if (this.mWorkspace.isDefaultScreen(screenId)) {
            this.mDefaultScreenButton = homeButton;
            homeButton.setImageResource(R.drawable.home_button_sethome_on);
        } else {
            homeButton.setImageResource(R.drawable.home_button_sethome_off);
        }
        ImageView deleteButton = (ImageView) convertView.findViewById(R.id.delete);
        deleteButton.setTag(Long.valueOf(screenId));
        deleteButton.setVisibility(0);
        deleteButton.setImageResource(R.drawable.delete_screen_btn);
        if (cs.getCellLayout().getChildCount() == 0) {
            deleteButton.setVisibility(0);
        } else {
            deleteButton.setVisibility(4);
        }
        deleteButton.setOnClickListener(this.DELETE_SCREEN_HANDLER);
        if (cs == this.mWorkspace.getCurrentCellScreen()) {
            this.mFocusedThumbnail = convertView;
        }
        addView(convertView, pos);
        return convertView;
    }

    private void setCellLayoutThumbnail(CellScreen cs, View convertView) {
        ThumbnailContainer thumbnail = (ThumbnailContainer) convertView.findViewById(R.id.thumbnail);
        if (thumbnail != null) {
            float scale = Math.max(((float) ((this.mThumbnailWidth - thumbnail.getPaddingLeft()) - thumbnail.getPaddingRight())) / ((float) cs.getWidth()), ((float) ((((this.mThumbnailHeight - this.mSetHomeOn.getIntrinsicHeight()) - thumbnail.getPaddingTop()) - thumbnail.getPaddingBottom()) - THUMBNAIL_CONTENT_PADDING_TOP)) / ((float) cs.getHeight()));
            thumbnail.setContent(cs, scale, scale, (float) thumbnail.getPaddingLeft(), (float) (thumbnail.getPaddingTop() + THUMBNAIL_CONTENT_PADDING_TOP));
        }
    }

    public void show(boolean isShow, boolean withAnim) {
        this.mFocusedThumbnail = null;
        if (isShow) {
            this.mDragController.cancelDrag();
            loadThumbnails(false);
            setCurrentScreen(this.mWorkspace.getCurrentScreenIndex());
            scrollToScreen(this.mWorkspace.getCurrentScreenIndex());
        } else {
            setVisibility(4);
            this.mDragController.cancelDrag();
            scrollToScreen(this.mWorkspace.getCurrentScreenIndex());
        }
        if (withAnim) {
            startSwitchingAnimation(isShow);
        }
        this.mIsShowing = isShow;
    }

    public boolean isShowing() {
        return this.mIsShowing;
    }

    public void updateHomeMark(long defaultScreenId) {
        int pos = ((Integer) this.mScreenIdMap.get(defaultScreenId)).intValue();
        if (pos >= 0) {
            View newHomeMark = getScreen(pos).findViewById(R.id.home_mark);
            if (!this.mIsInAnimation && this.mDefaultScreenButton != newHomeMark) {
                if (this.mDefaultScreenButton != null) {
                    this.mDefaultScreenButton.setImageResource(R.drawable.home_button_sethome_off);
                }
                this.mDefaultScreenButton = (ImageView) newHomeMark;
                this.mDefaultScreenButton.setImageResource(R.drawable.home_button_sethome_on);
            }
        }
    }

    public void updateCurrentScreen(long screenId) {
        View newCurrentScreen = getScreen(((Integer) this.mScreenIdMap.get(screenId)).intValue());
        if (newCurrentScreen != this.mFocusedThumbnail) {
            this.mFocusedThumbnail.findViewById(R.id.thumbnail).setBackground(getResources().getDrawable(R.drawable.thumbnail_bg));
            newCurrentScreen.findViewById(R.id.thumbnail).setBackground(getResources().getDrawable(R.drawable.thumbnail_bg_current));
            this.mFocusedThumbnail = newCurrentScreen;
        }
    }

    private void refreshScreenIdMap() {
        this.mScreenIdMap.clear();
        for (int i = 0; i < getScreenCount() - 1; i++) {
            this.mScreenIdMap.append(((Long) getChildAt(i).getTag()).longValue(), Integer.valueOf(i));
        }
    }

    public View getHitView() {
        return this;
    }

    public boolean isDropEnabled() {
        return true;
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        removeView(this.mCellBackground);
        if (this.mDraggingView instanceof HostView) {
            ((HostView) this.mDraggingView).setSkipNextAutoLayoutAnimation(true);
        }
        if (this.mDraggingView == this.mNewScreenView) {
            this.mNewScreenView.setImageResource(R.drawable.thumbnail_new_screen_n);
            if (this.mPlaceHolderIndex != this.mNewScreenViewIndex) {
                insertConvertView(this.mWorkspace.insertNewScreen(this.mPlaceHolderIndex, false), this.mPlaceHolderIndex);
            }
            addView(this.mNewScreenView);
            refreshScreenIdMap();
        } else {
            addView(this.mDraggingView, this.mPlaceHolderIndex);
            if (this.mPlaceHolderIndex != this.mDraggedUpPos) {
                if (this.mPlaceHolderIndex == this.mNewScreenViewIndex) {
                    removeView(this.mNewScreenView);
                    addView(this.mNewScreenView);
                    this.mPlaceHolderIndex--;
                }
                ArrayList<Long> screenIds = this.mWorkspace.getScreenIds();
                screenIds.clear();
                for (int i = 0; i < getScreenCount() - 1; i++) {
                    screenIds.add((Long) getChildAt(i).getTag());
                }
                this.mWorkspace.changeTargetScreenOrder(this.mDraggedUpPos, this.mPlaceHolderIndex);
                this.mWorkspace.reorderScreens(false);
                refreshScreenIdMap();
            }
        }
        dragObject.getDragView().setAnimateTarget(this.mDraggingView);
        this.mDraggingView = null;
        return true;
    }

    public void onDropCompleted() {
    }

    public void onDragEnter(DragObject dragObject) {
    }

    private int getTouchedChildIndex(int x, int y) {
        return getScreenIndexByPoint(x + (getWidth() * (getCurrentScreenIndex() / this.mVisibleRange)), y);
    }

    public void onDragOver(DragObject dragObject) {
        int currentWholeScreenIndex = getCurrentScreenIndex();
        if (dragObject.x < this.mPaddingLeft && this.mCurrentScreen >= this.mVisibleRange) {
            if (this.mLastTouchLeftEdgeTime == -1) {
                this.mLastTouchLeftEdgeTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mLastTouchLeftEdgeTime > ((long) SCROLL_ANIMATION_DURATION)) {
                snapToScreen(currentWholeScreenIndex - this.mVisibleRange);
                this.mLastTouchLeftEdgeTime = System.currentTimeMillis();
            }
        } else if (dragObject.x <= getWidth() - this.mPaddingRight || this.mCurrentScreen >= getScreenCount() - 1) {
            this.mLastTouchRightEdgeTime = -1;
            this.mLastTouchLeftEdgeTime = -1;
            int newPlaceHolderIndex = getTouchedChildIndex(dragObject.x, dragObject.y);
            if (newPlaceHolderIndex != -1 && newPlaceHolderIndex != this.mPlaceHolderIndex) {
                removeView(this.mCellBackground);
                addView(this.mCellBackground, newPlaceHolderIndex);
                this.mPlaceHolderIndex = newPlaceHolderIndex;
            }
        } else {
            if (this.mLastTouchRightEdgeTime == -1) {
                this.mLastTouchRightEdgeTime = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - this.mLastTouchRightEdgeTime > ((long) SCROLL_ANIMATION_DURATION)) {
                snapToScreen(this.mVisibleRange + currentWholeScreenIndex);
                this.mLastTouchRightEdgeTime = System.currentTimeMillis();
            }
        }
    }

    public void onDragExit(DragObject dragObject) {
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        return dragObject.dragSource == this;
    }

    private void setCellBackground(Bitmap b) {
        if (b != null) {
            this.mCellBackground.setImageBitmap(b);
            this.mCellBackground.setScaleType(ScaleType.CENTER);
        }
        this.mCellBackground.setSkipNextAutoLayoutAnimation(true);
    }

    public boolean onLongClick(View v) {
        if (this.mDraggingView != null || !this.mIsShowing) {
            return false;
        }
        if (v == this.mNewScreenView) {
            this.mNewScreenView.setImageResource(R.drawable.thumbnail_new_screen_p);
        } else {
            v = getScreen(((Integer) this.mScreenIdMap.get(((Long) v.getTag()).longValue())).intValue());
        }
        if (!this.mDragController.startDrag(v, true, this, 0)) {
            return false;
        }
        this.mDraggingView = v;
        setCellBackground(DragController.createViewBitmap(v, 1.0f));
        if (v == this.mNewScreenView) {
            this.mPlaceHolderIndex = getScreenCount();
        } else {
            this.mPlaceHolderIndex = ((Integer) this.mScreenIdMap.get(((Long) v.getTag()).longValue())).intValue();
        }
        this.mDraggedUpPos = this.mPlaceHolderIndex;
        addView(this.mCellBackground, this.mPlaceHolderIndex);
        this.mNewScreenViewIndex = getScreenCount() - 1;
        return true;
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
    }

    public void onDropBack(DragObject d) {
        removeView(this.mCellBackground);
        addView(this.mDraggingView, this.mDraggedUpPos);
        this.mDraggingView = null;
        this.mDraggedUpPos = -1;
    }

    protected void onPinchOut(ScaleGestureDetector detector) {
        finishCurrentGesture();
        int thumbnailIndex = getScreenIndexByPoint(((int) detector.getFocusX()) + (getWidth() * getCurrentScreenIndex()), (int) detector.getFocusY());
        if (thumbnailIndex >= 0 && thumbnailIndex != getScreenCount() - 1) {
            this.mWorkspace.exitPreview(((Long) getScreen(thumbnailIndex).getTag()).longValue());
        }
        super.onPinchOut(detector);
    }

    protected void startSwitchingAnimation(boolean entering) {
        float toRatio;
        TranslateAnimation translateAnimation;
        Interpolator interpolator = new CubicEaseInOutInterpolater();
        int focusedThumbnailIndex = this.mWorkspace.getCurrentScreenIndex();
        this.mFocusedThumbnail = getScreen(focusedThumbnailIndex);
        if (entering) {
            this.mAnimationListener = this.mWorkspace.getEnterAnimationListener();
        } else {
            this.mAnimationListener = this.mWorkspace.getExitAnimationListener();
        }
        float scaleRatio = (float) (getWidth() / (this.mFocusedThumbnail.getWidth() - (getScreenLayoutX(0) * 2)));
        float fromRatio = entering ? scaleRatio : 1.0f;
        if (entering) {
            toRatio = 1.0f;
        } else {
            toRatio = scaleRatio;
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromRatio, toRatio, fromRatio, toRatio, 0.5f, 0.5f);
        float deltaX = (((float) (-(this.mFocusedThumbnail.getLeft() - (getWidth() * (focusedThumbnailIndex / this.mVisibleRange))))) * scaleRatio) - ((float) (this.mFocusedThumbnail.getWidth() / 2));
        float deltaY = ((float) (-this.mFocusedThumbnail.getTop())) * scaleRatio;
        if (entering) {
            translateAnimation = new TranslateAnimation(deltaX, 0.0f, deltaY, 0.0f);
        } else {
            translateAnimation = new TranslateAnimation(0.0f, deltaX, 0.0f, deltaY);
        }
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(scaleAnimation);
        animation.addAnimation(translateAnimation);
        animation.setDuration((long) this.mAnimationDuration);
        animation.setInterpolator(interpolator);
        startAnimation(animation);
        startSourceViewAnimation(entering, interpolator);
    }

    void startSourceViewAnimation(boolean entering, Interpolator interpolator) {
        View thumbnail = this.mFocusedThumbnail.findViewById(R.id.thumbnail);
        int focusedThumbnailIndex = this.mWorkspace.getCurrentScreenIndex();
        AnimationSet animation = new AnimationSet(true);
        View sourceView = this.mWorkspace.getCurrentScreen();
        if (sourceView != null) {
            int thumbnailTop = this.mFocusedThumbnail.getTop();
            int thumbnailLeft = this.mFocusedThumbnail.getLeft() - (getWidth() * (focusedThumbnailIndex / this.mVisibleRange));
            int thumbnailPaddingTop = thumbnail == null ? 0 : thumbnail.getPaddingTop() + THUMBNAIL_CONTENT_PADDING_TOP;
            int thumbnailPaddingBottom = thumbnail == null ? 0 : thumbnail.getPaddingBottom();
            int thumbnailPaddingLeft = thumbnail == null ? 0 : thumbnail.getPaddingLeft();
            float scale = Math.max(((float) ((this.mFocusedThumbnail.getWidth() - thumbnailPaddingLeft) - (thumbnail == null ? 0 : thumbnail.getPaddingRight()))) / ((float) sourceView.getWidth()), ((float) ((this.mFocusedThumbnail.getHeight() - thumbnailPaddingTop) - thumbnailPaddingBottom)) / ((float) sourceView.getHeight()));
            if (entering) {
                animation.addAnimation(new ScaleAnimation(1.0f, scale, 1.0f, scale, 0.0f, 0.0f));
            } else {
                animation.addAnimation(new ScaleAnimation(scale, 1.0f, scale, 1.0f, 0.0f, 0.0f));
            }
            float deltaX = (float) thumbnailLeft;
            float deltaY = (float) thumbnailTop;
            if (entering) {
                animation.addAnimation(new TranslateAnimation(0.0f, ((float) thumbnailPaddingLeft) + deltaX, 0.0f, (((float) thumbnailPaddingTop) + deltaY) - ((float) this.mWorkspace.getPaddingTop())));
            } else {
                animation.addAnimation(new TranslateAnimation(((float) thumbnailPaddingLeft) + deltaX, 0.0f, (((float) thumbnailPaddingTop) + deltaY) - ((float) this.mWorkspace.getPaddingTop()), 0.0f));
            }
            animation.setDuration((long) this.mAnimationDuration);
            animation.setInterpolator(interpolator);
            sourceView.startAnimation(animation);
        }
    }

    protected void onAnimationStart() {
        super.onAnimationStart();
        this.mIsInAnimation = true;
        if (this.mFocusedThumbnail != null) {
            this.mAnimationListener.onAnimationStart(null);
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setVisibility(this.mFocusedThumbnail == getChildAt(i) ? 4 : 0);
            }
        }
    }

    protected void onAnimationEnd() {
        super.onAnimationEnd();
        this.mIsInAnimation = false;
        if (this.mFocusedThumbnail != null) {
            this.mAnimationListener.onAnimationEnd(null);
            for (int i = 0; i < getChildCount(); i++) {
                getChildAt(i).setVisibility(0);
            }
        }
    }
}
