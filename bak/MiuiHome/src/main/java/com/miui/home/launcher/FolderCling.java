package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.FolderInfo.RecommendInfo;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.PreciseClickConfirmor;
import com.miui.home.launcher.common.Utilities;
import miui.os.Build;
import miui.widget.SlidingButton;

public class FolderCling extends FrameLayout implements OnClickListener, DragListener, DropTarget, ForceTouchTriggeredListener, WallpaperColorChangedListener {
    private static long CHECK_INTERVAL = 3600000;
    public static long RECOMMEND_DISABLE_INTERVAL = (168 * CHECK_INTERVAL);
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final HandlerThread sWorkerThread = new HandlerThread("market-thread");
    private Runnable mCheckAllowConnectToNetwork;
    private long mCheckCloudDataTime;
    private LinearLayout mChildrenExit;
    private PreciseClickConfirmor mClickConfirmor;
    private Runnable mCloseConfirm;
    private int mCloseTimeout;
    private float[] mCoord;
    private DragController mDragController;
    private final int mEditModePaddingBottom;
    private final int mEditModePaddingTop;
    private Folder mFolder;
    private int mFolderInitPaddingBottom;
    private int mFolderInitSize;
    private boolean mIsRecommendViewShow;
    private Launcher mLauncher;
    private boolean mMarketAllowConnectToNetwork;
    private Paint mMaskPaint;
    private final int mNormalModePaddingBottom;
    private final int mNormalModePaddingTop;
    private Runnable mOnFinishClose;
    private boolean mOpened = false;
    private RecommendScreen mRecommendScreen;
    private boolean mRecommendScreenAnimating;
    private SlidingButton mRecommendSlidingButton;
    private Rect mRect;
    private boolean mReverseDrawingMode;

    static {
        sWorkerThread.start();
    }

    public FolderCling(Context context, AttributeSet attrs) {
        boolean z;
        super(context, attrs);
        if (Build.IS_INTERNATIONAL_BUILD) {
            z = true;
        } else {
            z = false;
        }
        this.mMarketAllowConnectToNetwork = z;
        this.mCloseTimeout = 500;
        this.mIsRecommendViewShow = false;
        this.mCheckCloudDataTime = -1;
        this.mCheckAllowConnectToNetwork = new Runnable() {
            public void run() {
                try {
                    FolderCling.this.mMarketAllowConnectToNetwork = MarketManager.getManager(FolderCling.this.mContext).allowConnectToNetwork();
                } catch (Exception e) {
                }
            }
        };
        this.mCoord = new float[2];
        this.mRect = new Rect();
        this.mReverseDrawingMode = false;
        this.mOnFinishClose = new Runnable() {
            public void run() {
                FolderCling.this.setVisibility(8);
                FolderCling.this.showBackground(false, null);
                FolderCling.this.mFolder.setPadding(FolderCling.this.mFolder.getPaddingLeft(), FolderCling.this.mFolder.getPaddingTop(), FolderCling.this.mFolder.getPaddingRight(), FolderCling.this.mFolderInitPaddingBottom);
            }
        };
        this.mCloseConfirm = new Runnable() {
            public void run() {
                FolderCling.this.mLauncher.closeFolder();
                FolderInfo fInfo = FolderCling.this.mFolder.getInfo();
                ShortcutInfo sInfo = FolderCling.this.mFolder.getDragedItem();
                FolderCling.this.mFolder.removeItem(sInfo);
                if (sInfo != null && fInfo.container == -100 && !FolderCling.this.mLauncher.isInMultiSelecting() && fInfo.getAdapter(FolderCling.this.mContext).getCount() == 0) {
                    fInfo.icon.deleteSelf();
                    sInfo.copyPosition(fInfo);
                }
            }
        };
        this.mRecommendScreenAnimating = false;
        setClipChildren(false);
        setClipToPadding(false);
        this.mEditModePaddingTop = getResources().getDimensionPixelOffset(R.dimen.folder_edit_mode_top_padding);
        this.mEditModePaddingBottom = getResources().getDimensionPixelOffset(R.dimen.folder_edit_mode_bottom_padding);
        this.mNormalModePaddingTop = getResources().getDimensionPixelOffset(R.dimen.folder_top_padding);
        this.mNormalModePaddingBottom = getResources().getDimensionPixelOffset(R.dimen.folder_bottom_padding);
        this.mFolderInitPaddingBottom = 0;
        this.mClickConfirmor = new PreciseClickConfirmor(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildrenDrawingOrderEnabled(true);
        this.mFolder = (Folder) findViewById(R.id.folder);
        this.mChildrenExit = (LinearLayout) findViewById(R.id.children_exit);
        this.mChildrenExit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FolderCling.this.mLauncher.exitChildrenMode();
            }
        });
        this.mRecommendScreen = (RecommendScreen) findViewById(R.id.recommend_screen);
        this.mRecommendSlidingButton = (SlidingButton) findViewById(R.id.switch_button);
        this.mRecommendSlidingButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (FolderCling.this.mFolder.isEditing()) {
                    Context context = FolderCling.this.getContext().getApplicationContext();
                    FolderCling.this.mFolder.getInfo().setRecommendAppsViewEnable(isChecked);
                    FolderCling.this.mFolder.getInfo().recordRecommendAppsSwitchState(context, isChecked);
                }
            }
        });
        setOnClickListener(this);
        this.mFolder.setOnClickListener(this);
        this.mMaskPaint = new Paint();
        this.mMaskPaint.setColor(-16777216);
    }

    protected void dispatchDraw(Canvas canvas) {
        if (getFolder().getRecommendAppsSwitch().getVisibility() == 0) {
            canvas.save();
            Utilities.getDescendantCoordRelativeToAncestor(getFolder().getContent(), this, this.mCoord, true, true);
            this.mRect.set((int) this.mCoord[0], (int) this.mCoord[1], ((int) this.mCoord[0]) + getFolder().getContent().getWidth(), ((int) this.mCoord[1]) + getFolder().getContent().getHeight());
            canvas.clipRect(this.mRect, Op.DIFFERENCE);
            this.mMaskPaint.setAlpha((int) ((getFolder().getRecommendAppsSwitch().getAlpha() * 0.7f) * 255.0f));
            canvas.drawPaint(this.mMaskPaint);
            canvas.restore();
            super.dispatchDraw(canvas);
            canvas.save();
            canvas.clipRect(this.mRect, Op.REPLACE);
            canvas.drawPaint(this.mMaskPaint);
            canvas.restore();
            return;
        }
        super.dispatchDraw(canvas);
    }

    public void setDrawChildrenReverse(boolean reverse) {
        this.mReverseDrawingMode = reverse;
    }

    protected int getChildDrawingOrder(int childCount, int i) {
        if (this.mReverseDrawingMode) {
            return (childCount - i) - 1;
        }
        return i;
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mFolder.setLauncher(launcher);
        this.mRecommendScreen.setLauncher(launcher);
    }

    public boolean isMarketAllowConnectToNetwork() {
        return this.mMarketAllowConnectToNetwork;
    }

    public void setMarketAllowConnectToNetwork(boolean allow) {
        this.mMarketAllowConnectToNetwork = allow;
    }

    public Folder getFolder() {
        return this.mFolder;
    }

    public long getFolderId() {
        if (this.mFolder == null || this.mFolder.getInfo() == null) {
            return -1;
        }
        return this.mFolder.getInfo().id;
    }

    public void setDragController(DragController dragController) {
        this.mFolder.setDragController(dragController);
        this.mDragController = dragController;
    }

    void bind(FolderInfo info) {
        this.mFolder.bind(info);
        if (info.id != -1) {
            this.mRecommendScreen.bind(info);
        }
    }

    public void showBackground(boolean isShow, Drawable blurScreenShot) {
        if (isShow) {
            blurScreenShot.setAlpha(0);
            setBackground(blurScreenShot);
            return;
        }
        setBackground(null);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (DeviceConfig.isRotatable()) {
            this.mRecommendScreen.setPadding(0, 0, 0, DeviceConfig.getRecommendGridPaddingBottom());
            if (DeviceConfig.isScreenOrientationLandscape()) {
                this.mRecommendScreen.setHeaderBgRes(R.drawable.recommend_divider_landscape);
            } else {
                this.mRecommendScreen.setHeaderBgRes(R.drawable.recommend_divider);
            }
        }
    }

    public void checkMarketAllowConnectToNetwork() {
        if (!Build.IS_INTERNATIONAL_BUILD) {
            sWorker.post(this.mCheckAllowConnectToNetwork);
        }
    }

    void open() {
        checkMarketAllowConnectToNetwork();
        if (MarketManager.getManager(this.mLauncher).isAppStoreEnabled()) {
            if (this.mCheckCloudDataTime == -1 || System.currentTimeMillis() - this.mCheckCloudDataTime > CHECK_INTERVAL) {
                DeviceConfig.loadRecommendData(this.mLauncher);
                this.mCheckCloudDataTime = System.currentTimeMillis();
            }
            getFolder().getInfo().initRecommendEnableState(this.mLauncher);
        }
        boolean enableRecommendApps = this.mFolder.getInfo().isRecommendAppsViewEnable(this.mContext);
        this.mOpened = true;
        if (this.mLauncher.isInNormalEditing()) {
            setPadding(getPaddingLeft(), this.mEditModePaddingTop, getPaddingRight(), this.mEditModePaddingBottom);
            showBackground(false, null);
        } else {
            setPadding(getPaddingLeft(), this.mNormalModePaddingTop, getPaddingRight(), this.mNormalModePaddingBottom);
            if (Launcher.isChildrenModeEnabled()) {
                showBackground(false, null);
            } else {
                updateFolderPaddingBottom(enableRecommendApps);
                Drawable background = null;
                if (enableRecommendApps) {
                    Bitmap b = Utilities.getBitmapFromUri(this.mLauncher, this.mFolder.getInfo().getRecommendInfo(this.mLauncher).mBackgroundUri);
                    if (b != null) {
                        background = new BitmapDrawable(this.mLauncher.getResources(), b);
                    }
                }
                if (background != null) {
                    showBackground(true, background);
                } else {
                    setBackgroundResource(R.color.folder_background_mask);
                }
            }
        }
        setVisibility(0);
        this.mDragController.addDropTarget(this);
        this.mRecommendSlidingButton.setChecked(enableRecommendApps);
        if (Launcher.isChildrenModeEnabled()) {
            this.mChildrenExit.setVisibility(0);
            setPadding(getPaddingLeft(), this.mNormalModePaddingTop, getPaddingRight(), this.mEditModePaddingBottom);
            Launcher.performLayoutNow(this);
            this.mFolder.setPadding(this.mFolder.getPaddingLeft(), this.mFolder.getPaddingTop(), this.mFolder.getPaddingRight(), (int) (((double) this.mChildrenExit.getHeight()) * 1.4d));
            TextView title = (TextView) this.mChildrenExit.findViewById(R.id.title);
            ImageView btn = (ImageView) this.mChildrenExit.findViewById(R.id.btn_view);
            if (WallpaperUtils.hasAppliedLightWallpaper()) {
                title.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.dark);
                btn.setImageResource(R.drawable.children_mode_exit_btn_dark);
            } else {
                title.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle);
                btn.setImageResource(R.drawable.children_mode_exit_btn);
            }
            this.mFolder.onOpen(false);
        } else {
            this.mChildrenExit.setVisibility(4);
            this.mFolder.onOpen(true);
        }
        this.mFolderInitSize = this.mFolder.getFolderSize();
        requestFitSystemWindows();
    }

    public void setRecommendButtonChecked(boolean checked) {
        this.mRecommendSlidingButton.setChecked(checked);
    }

    public boolean stepClose() {
        if (!isOpened()) {
            return false;
        }
        if (this.mFolder.isEditing() || this.mFolder.isEditAnimatorShowing()) {
            this.mFolder.showEditPanel(false, true);
            showRecommendApps(this.mFolder.getInfo().isRecommendAppsViewEnable(this.mContext), true, 0);
            return true;
        }
        this.mLauncher.closeFolder();
        return true;
    }

    void close(boolean allowAnimation) {
        this.mDragController.removeDropTarget(this);
        this.mDragController.removeDragListener(this);
        this.mFolder.onClose(allowAnimation, this.mOnFinishClose);
        this.mOpened = false;
        if (isRecommendAppsViewShow()) {
            showRecommendApps(false, true, 0);
        }
        this.mFolderInitSize = -1;
        requestFitSystemWindows();
    }

    boolean isOpened() {
        return this.mOpened;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean r = super.onTouchEvent(ev);
        this.mClickConfirmor.onTouchEvent(ev);
        return r;
    }

    public boolean performClick() {
        if (this.mClickConfirmor.confirmClick()) {
            return super.performClick();
        }
        return false;
    }

    public void onClick(View v) {
        stepClose();
    }

    public View getHitView() {
        return this;
    }

    public boolean isDropEnabled() {
        return isOpened();
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        return false;
    }

    public void prepareAutoOpening() {
        this.mDragController.addDragListener(this);
        this.mCloseTimeout = 2000;
    }

    public void onDragEnter(DragObject dragObject) {
        postDelayed(this.mCloseConfirm, (long) this.mCloseTimeout);
        this.mCloseTimeout = 500;
    }

    public void onDragOver(DragObject dragObject) {
        this.mCloseTimeout = 500;
    }

    public void onDragExit(DragObject dragObject) {
        removeCallbacks(this.mCloseConfirm);
        this.mCloseTimeout = 500;
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        return dragObject.getDragInfo().itemType == 0 || dragObject.getDragInfo().itemType == 1 || dragObject.getDragInfo().itemType == 11;
    }

    public void enableInteractive(boolean enabled) {
        this.mFolder.enableInteractive(enabled);
        setEnabled(enabled);
    }

    public void setContentAlpha(float alpha) {
        this.mFolder.setContentAlpha(alpha);
        this.mRecommendScreen.setContentAlpha(alpha);
    }

    public void onDropCompleted() {
    }

    public void updateLayout(boolean isEditMode) {
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        if (isEditMode) {
            layoutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.folder_cling_edit_mode_margin_bottom);
            this.mFolder.setOpenedScale(0.8464f);
        } else {
            layoutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.folder_cling_normal_margin_bottom);
            this.mFolder.setOpenedScale(1.0f);
        }
        requestLayout();
        if (!Build.IS_TABLET) {
            ((LayoutParams) this.mFolder.getContent().getLayoutParams()).height = isEditMode ? getResources().getDimensionPixelSize(R.dimen.folder_content_edit_mode_height) : -2;
            this.mFolder.getContent().requestLayout();
        }
    }

    public void onDragStart(DragSource source, DragObject DragObject) {
    }

    public void onDragEnd() {
        if (this.mOpened && this.mFolderInitSize == this.mFolder.getFolderSize()) {
            post(this.mCloseConfirm);
        }
    }

    public boolean isRecommendScreenAnimating() {
        return this.mRecommendScreenAnimating;
    }

    public void showRecommendApps(final boolean isShow, final boolean needUpdateFolderPaddingBottom, int delay) {
        if (this.mIsRecommendViewShow != isShow && !this.mLauncher.isInNormalEditing() && MarketManager.getManager(this.mLauncher).isAppStoreEnabled()) {
            this.mRecommendScreen.animate().setListener(null).cancel();
            this.mIsRecommendViewShow = isShow;
            RecommendInfo recommendInfo = getRecommendScreen().getRecommendInfo();
            this.mRecommendScreenAnimating = true;
            if (isShow) {
                if (!isMarketAllowConnectToNetwork()) {
                    MarketManager.getManager(this.mLauncher).startUserAgreementActivity(this.mLauncher, 1001);
                }
                getRecommendScreen().init();
                if (recommendInfo.getCacheEndTime() < System.currentTimeMillis()) {
                    recommendInfo.clearContents(false);
                }
                this.mRecommendScreen.setVisibility(0);
                this.mRecommendScreen.getRecommendInfo().initRecommendViewAndRequest();
                if (needUpdateFolderPaddingBottom) {
                    updateFolderPaddingBottom(isShow);
                }
                this.mRecommendScreen.setTranslationY((float) (this.mRecommendScreen.getHeight() / 2));
                this.mRecommendScreen.setAlpha(0.0f);
                this.mRecommendScreen.animate().translationY(0.0f).alpha(1.0f).setDuration(200).setStartDelay((long) delay).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        FolderCling.this.mRecommendScreenAnimating = false;
                    }
                }).start();
                return;
            }
            this.mRecommendScreen.animate().translationY((float) (this.mRecommendScreen.getHeight() / 2)).alpha(0.0f).setDuration(200).setStartDelay(0).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (needUpdateFolderPaddingBottom) {
                        FolderCling.this.updateFolderPaddingBottom(isShow);
                    }
                    FolderCling.this.mRecommendScreen.setVisibility(8);
                    FolderCling.this.mRecommendScreenAnimating = false;
                }
            }).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    public RecommendScreen getRecommendScreen() {
        return this.mRecommendScreen;
    }

    public boolean isRecommendAppsViewShow() {
        return this.mIsRecommendViewShow;
    }

    public void setRecommendAppsViewShow(boolean show) {
        this.mIsRecommendViewShow = show;
    }

    private void updateFolderPaddingBottom(boolean enableRecommendApps) {
        if (this.mOpened) {
            int folderTop = this.mFolder.getTop();
            Launcher.performLayoutNow(this);
            this.mFolder.setPadding(this.mFolder.getPaddingLeft(), this.mFolder.getPaddingTop(), this.mFolder.getPaddingRight(), enableRecommendApps ? this.mRecommendScreen.getHeight() + (DeviceConfig.getCellHeight() / 4) : this.mFolderInitPaddingBottom);
            setPadding(getPaddingLeft(), this.mNormalModePaddingTop, getPaddingRight(), enableRecommendApps ? 0 : this.mNormalModePaddingBottom);
            Launcher.performLayoutNow(this);
            this.mFolder.animate().cancel();
            this.mFolder.setTranslationY((float) (folderTop - this.mFolder.getTop()));
            this.mFolder.animate().translationY(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    public void onWallpaperColorChanged() {
        this.mFolder.onWallpaperColorChanged();
    }

    public View getForceTouchSelectedView() {
        return this.mFolder.getForceTouchSelectedView();
    }

    public void onForceTouchTriggered() {
        this.mFolder.onForceTouchTriggered();
    }

    public void onForceTouchFinish() {
        this.mFolder.onForceTouchFinish();
    }
}
