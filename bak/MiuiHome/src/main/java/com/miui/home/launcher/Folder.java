package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.CubicEaseInOutInterpolater;
import com.miui.home.launcher.common.Utilities;
import com.xiaomi.analytics.Actions;
import com.xiaomi.analytics.AdAction;
import com.xiaomi.analytics.Analytics;

public class Folder extends LinearLayout implements OnClickListener, OnItemClickListener, OnItemLongClickListener, DragSource, ForceTouchTriggeredListener, WallpaperColorChangedListener {
    public static int DEFAULT_FOLDER_BACKGROUND_LONG_DURATION = 300;
    public static int DEFAULT_FOLDER_BACKGROUND_SHORT_DURATION = 150;
    public static int DEFAULT_FOLDER_CLOSE_DURATION = 300;
    private static int DEFAULT_FOLDER_EDITING_BACKGROUND_DRUATION = 300;
    public static int DEFAULT_FOLDER_EDITING_DRUATION = 350;
    public static int DEFAULT_FOLDER_OPEN_DURATION = 300;
    private Drawable mBackground;
    protected ViewGroup mBackgroundView;
    private Interpolator mCloseAnimInterpolator = new CubicEaseInOutInterpolater();
    private boolean mClosing;
    protected FolderGridView mContent;
    private Drawable mContentBackground;
    private Rect mContentRect = new Rect();
    private String mDefaultFolderName;
    private String mDefaultUnnamedFolderName;
    protected DragController mDragController;
    protected ShortcutInfo mDragItem;
    private ValueAnimator mEditAnimator = new ValueAnimator();
    private boolean mEditAnimatorShowing = false;
    private ValueAnimator mEditBackgroundAnimator = new ValueAnimator();
    private View mHeader;
    private InputMethodManager mImm;
    protected FolderInfo mInfo = null;
    private boolean mIsEditing;
    private boolean mLastEnableRecommend = false;
    private float mLastOpenedScale = 1.0f;
    protected Launcher mLauncher;
    private Runnable mOnFinishClose = null;
    private Interpolator mOpenAnimInterpolator = new CubicEaseInOutInterpolater();
    private ValueAnimator mOpenCloseAnimator = new ValueAnimator();
    private float mOpenCloseScale;
    private float mOpenedScale = 1.0f;
    private Rect mPreviewPosRect = new Rect();
    private NonOverlapLinearLayout mRecommendAppsSwitch;
    private EditText mRenameEdit;
    protected TextView mTitleText;
    private float[] mTmpPos = new float[2];
    private float mVisionCenterX;
    private float mVisionCenterY;

    public interface FolderCallback {
        void deleteSelf();

        int getPreviewCount();

        float getPreviewPosition(Rect rect);

        void loadItemIcons();

        void onClose();

        void onOpen();

        void setTitle(CharSequence charSequence);

        void showPreview(boolean z);
    }

    public void onForceTouchTriggered() {
        if (this.mContent.getAdapter() != null) {
            ((ShortcutsAdapter) this.mContent.getAdapter()).setForceTouchStarted(true);
            ((ShortcutsAdapter) this.mContent.getAdapter()).notifyDataSetChanged();
        }
    }

    public void onForceTouchFinish() {
        if (this.mContent.getAdapter() != null) {
            ((ShortcutsAdapter) this.mContent.getAdapter()).setForceTouchStarted(false);
            ((ShortcutsAdapter) this.mContent.getAdapter()).setForceTouchSelectedShortcutInfo(null);
            ((ShortcutsAdapter) this.mContent.getAdapter()).notifyDataSetChanged();
        }
    }

    public Folder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        setChildrenDrawingOrderEnabled(true);
        this.mContent = (FolderGridView) findViewById(R.id.folder_content);
        this.mBackgroundView = (ViewGroup) findViewById(R.id.background);
        this.mContent.setOnItemClickListener(this);
        this.mContent.setOnItemLongClickListener(this);
        this.mContentBackground = this.mBackgroundView.getBackground();
        this.mTitleText = (TextView) findViewById(R.id.title);
        this.mTitleText.setOnClickListener(this);
        this.mHeader = findViewById(R.id.folder_header);
        this.mRenameEdit = (EditText) findViewById(R.id.rename_edit);
        this.mImm = (InputMethodManager) this.mContext.getSystemService("input_method");
        this.mRecommendAppsSwitch = (NonOverlapLinearLayout) findViewById(R.id.recommend_apps_switch);
        this.mIsEditing = false;
        this.mBackground = Utilities.loadThemeCompatibleDrawable(this.mContext, R.drawable.folder_background);
        setBackground(this.mBackground);
        this.mOpenCloseAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                Folder.this.mLastOpenedScale = Folder.this.mOpenedScale;
            }

            public void onAnimationEnd(Animator animation) {
                if (Folder.this.mClosing) {
                    Folder.this.onCloseAnimationFinished();
                } else {
                    Folder.this.onOpenAnimationFinished();
                }
            }

            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });
        this.mOpenCloseAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                float scale = (Folder.this.mOpenCloseScale + ((1.0f - Folder.this.mOpenCloseScale) * value)) * Folder.this.mLastOpenedScale;
                Folder.this.setScaleX(scale);
                Folder.this.setScaleY(scale);
                float moveY = (((((float) Folder.this.mPreviewPosRect.centerY()) - Folder.this.mVisionCenterY) - ((float) Folder.this.getTop())) - ((float) Folder.this.mLauncher.getFolderCling().getPaddingTop())) * (1.0f - value);
                Folder.this.setTranslationX(((((float) Folder.this.mPreviewPosRect.centerX()) - Folder.this.mVisionCenterX) - ((float) Folder.this.getLeft())) * (1.0f - value));
                Folder.this.setTranslationY(moveY);
                Folder.this.setupOutOfPreviewContent(value);
                Folder.this.setBackgroundAlpha(value);
            }
        });
        Resources res = this.mContext.getResources();
        this.mDefaultFolderName = res.getString(R.string.folder_name);
        this.mDefaultUnnamedFolderName = res.getString(R.string.unnamed_folder_name);
        this.mEditAnimator.setDuration((long) DEFAULT_FOLDER_EDITING_DRUATION);
        this.mEditAnimator.setFloatValues(new float[]{0.0f, 1.0f});
        this.mEditAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                if (Folder.this.mIsEditing) {
                    value = 1.0f - value;
                }
                Folder.this.mTitleText.setAlpha(1.0f - value);
                Folder.this.mRenameEdit.setAlpha(value);
                if (Folder.this.mLauncher.isInEditing()) {
                    Folder.this.mContent.setAlpha(((0.3f * value) + 1.0f) - value);
                }
            }
        });
        this.mEditAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                Folder.this.mEditAnimatorShowing = true;
            }

            public void onAnimationEnd(Animator animation) {
                boolean z;
                Folder folder = Folder.this;
                if (Folder.this.mIsEditing) {
                    z = false;
                } else {
                    z = true;
                }
                folder.showEditPanel(z, false);
                Folder.this.mTitleText.setAlpha(1.0f);
                Folder.this.mRenameEdit.setAlpha(1.0f);
                Folder.this.mEditAnimatorShowing = false;
            }
        });
        this.mEditBackgroundAnimator.setDuration((long) DEFAULT_FOLDER_EDITING_BACKGROUND_DRUATION);
        this.mEditBackgroundAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Folder.this.mContentBackground.setAlpha(((Integer) animation.getAnimatedValue()).intValue());
            }
        });
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setDragController(DragController dragController) {
        this.mDragController = dragController;
    }

    public void setOpenedScale(float scale) {
        this.mOpenedScale = scale;
    }

    public FolderGridView getContent() {
        return this.mContent;
    }

    public NonOverlapLinearLayout getRecommendAppsSwitch() {
        return this.mRecommendAppsSwitch;
    }

    public void onDropBack(DragObject d) {
        if (!this.mInfo.contains((ShortcutInfo) d.getDragInfo())) {
            if (((FolderIcon) this.mInfo.icon).isPreRemoved()) {
                this.mLauncher.addItem(d.getDragInfo(), false);
                return;
            }
            this.mInfo.add((ShortcutInfo) d.getDragInfo());
            if (d.isLastObject()) {
                this.mInfo.notifyDataSetChanged();
            }
        }
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        if (!(this.mClosing || this.mLauncher.isInNormalEditing())) {
            this.mEditBackgroundAnimator.setIntValues(new int[]{255, 0});
            this.mEditBackgroundAnimator.start();
        }
        this.mDragItem = null;
    }

    public void removeItem(ShortcutInfo info) {
        this.mInfo.remove(info);
        this.mInfo.notifyDataSetChanged();
    }

    void setContentAdapter(BaseAdapter adapter) {
        this.mContent.setAdapter((ListAdapter) adapter);
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mContent.setLauncher(this.mLauncher);
    }

    FolderInfo getInfo() {
        return this.mInfo;
    }

    void onOpen(boolean allowAnimation) {
        this.mClosing = false;
        this.mInfo.opened = true;
        this.mContentBackground.setAlpha(0);
        try {
            AdAction action = Actions.newAdAction("VIEW");
            action.addParam("folder_title", this.mInfo.getTitle(this.mLauncher).toString());
            action.addParam("folder_size", this.mInfo.getAdapter(this.mLauncher).getCount());
            action.addParam("folder_recommend", String.valueOf(this.mInfo.isRecommendAppsViewEnable(this.mLauncher)));
            Analytics.trackSystem(this.mContext, "miuihome_openfolder1", action);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setVisibility(0);
        this.mDragController.addDropTarget(this.mContent);
        requestFocus();
        if (this.mInfo != null) {
            this.mInfo.icon.onOpen();
            this.mInfo.icon.showPreview(false);
            this.mContent.setAlpha(1.0f);
            this.mContent.requestLayout();
            if (allowAnimation) {
                setBackgroundAlpha(0.0f);
                this.mInfo.icon.getPreviewPosition(this.mPreviewPosRect);
                float[] fArr = this.mTmpPos;
                this.mTmpPos[1] = 0.0f;
                fArr[0] = 0.0f;
                Utilities.getDescendantCoordRelativeToAncestor(this.mContent, this, this.mTmpPos, false, false);
                this.mContentRect.set((int) this.mTmpPos[0], (int) this.mTmpPos[1], ((int) this.mTmpPos[0]) + this.mContent.getMeasuredWidth(), ((int) this.mTmpPos[1]) + this.mContent.getMeasuredHeight());
                this.mOpenCloseScale = ((float) this.mPreviewPosRect.width()) / (((float) this.mContentRect.width()) * this.mOpenedScale);
                this.mOpenCloseAnimator.setFloatValues(new float[]{0.0f, 1.0f});
                this.mOpenCloseAnimator.setDuration((long) DEFAULT_FOLDER_OPEN_DURATION);
                this.mOpenCloseAnimator.setInterpolator(this.mOpenAnimInterpolator);
                this.mOpenCloseAnimator.start();
                this.mVisionCenterX = (float) this.mContentRect.centerX();
                this.mVisionCenterY = (float) ((this.mContentRect.width() / 2) + this.mContentRect.top);
                setPivotX(this.mVisionCenterX);
                setPivotY(this.mVisionCenterY);
                return;
            }
            if (this.mLauncher.isInNormalEditing()) {
                this.mContentBackground.setAlpha(255);
            }
            if (Launcher.isChildrenModeEnabled()) {
                setScaleX(1.0f);
                setScaleY(1.0f);
                setTranslationX(0.0f);
                setTranslationY(0.0f);
            }
        }
    }

    void onClose(boolean allowAnimation, Runnable onFinishClose) {
        if (!this.mClosing) {
            this.mClosing = true;
            this.mInfo.opened = false;
            this.mInfo.getAdapter(this.mLauncher).notifyDataSetChanged();
            clearAnimation();
            showEditPanel(false, false);
            this.mDragController.removeDropTarget(this.mContent);
            this.mOnFinishClose = onFinishClose;
            if (this.mInfo.icon != null) {
                this.mInfo.icon.onClose();
                if (allowAnimation) {
                    this.mOpenCloseAnimator.setFloatValues(new float[]{1.0f, 0.0f});
                    this.mOpenCloseAnimator.setDuration((long) DEFAULT_FOLDER_CLOSE_DURATION);
                    this.mOpenCloseAnimator.setInterpolator(this.mCloseAnimInterpolator);
                    this.mOpenCloseAnimator.start();
                    return;
                }
                onCloseAnimationFinished();
                return;
            }
            onCloseAnimationFinished();
        }
    }

    private void onCloseAnimationFinished() {
        if (this.mInfo != null) {
            this.mInfo.icon.showPreview(true);
        }
        if (this.mOnFinishClose != null) {
            this.mOnFinishClose.run();
            this.mOnFinishClose = null;
        }
        this.mClosing = false;
    }

    private void onOpenAnimationFinished() {
        setScaleX(this.mOpenedScale);
        setScaleY(this.mOpenedScale);
        setBackgroundAlpha(1.0f);
        setupOutOfPreviewContent(1.0f);
        if (this.mLauncher.isInNormalEditing() || !this.mInfo.isRecommendAppsViewEnable(this.mContext)) {
            this.mLauncher.getFolderCling().getRecommendScreen().setVisibility(8);
            if (!this.mLauncher.isInNormalEditing()) {
                AnalyticalDataCollector.trackFolderOpenWithRecommend("off");
            }
            this.mLauncher.getFolderCling().setRecommendAppsViewShow(false);
            return;
        }
        this.mLauncher.getFolderCling().getRecommendScreen().setVisibility(0);
        this.mLauncher.getFolderCling().showRecommendApps(true, false, 0);
        AnalyticalDataCollector.trackFolderOpenWithRecommend("on");
        this.mLauncher.getFolderCling().setRecommendAppsViewShow(true);
    }

    private CharSequence getEditText(CharSequence title) {
        if (this.mDefaultFolderName.equals(title) || TextUtils.isEmpty(title)) {
            return this.mDefaultUnnamedFolderName;
        }
        return title;
    }

    protected void updateAppearance() {
        if (Launcher.isChildrenModeEnabled()) {
            this.mTitleText.setVisibility(4);
            return;
        }
        this.mTitleText.setVisibility(0);
        CharSequence title = getEditText(this.mInfo.getTitle(this.mContext));
        if (TextUtils.isEmpty(title)) {
            this.mTitleText.setText(this.mDefaultUnnamedFolderName);
            this.mRenameEdit.setText(this.mDefaultUnnamedFolderName);
            this.mInfo.icon.setTitle(this.mInfo.getTitle(this.mContext));
            return;
        }
        if (!this.mTitleText.getText().equals(title)) {
            this.mTitleText.setText(title);
            this.mInfo.icon.setTitle(this.mInfo.getTitle(this.mContext));
        }
        if (!this.mRenameEdit.getText().toString().equals(title)) {
            this.mRenameEdit.setText(title);
        }
    }

    void bind(FolderInfo info) {
        this.mInfo = info;
        if ((this.mLauncher.isInNormalEditing() || Launcher.isChildrenModeEnabled()) && WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mTitleText.setTextColor(this.mLauncher.getResources().getColor(R.color.folder_title_text_dark));
        } else {
            this.mTitleText.setTextColor(this.mLauncher.getResources().getColor(R.color.folder_title_text));
        }
        this.mContent.adapterScrollBar();
        updateAppearance();
        if (info == null) {
            setContentAdapter(null);
        } else {
            setContentAdapter(info.getAdapter(this.mContext));
        }
    }

    public void onClick(View v) {
        if (!this.mLauncher.isPrivacyModeEnabled()) {
            switch (v.getId()) {
                case R.id.title:
                    showEditPanel(true, true);
                    return;
                default:
                    return;
            }
        }
    }

    public void showEditPanel(boolean isShow, boolean doAnim) {
        boolean z = true;
        int i = 0;
        if (!isShow && this.mEditAnimatorShowing) {
            this.mEditAnimator.cancel();
            this.mContent.animate().cancel();
        }
        if (!this.mLauncher.isUninstallDialogShowing() && this.mIsEditing != isShow) {
            String title = this.mRenameEdit.getText().toString();
            if (!title.equals(getEditText(this.mInfo.getTitle(this.mContext)))) {
                this.mInfo.setTitle(title, this.mLauncher);
            }
            updateAppearance();
            if (!(Launcher.isChildrenModeEnabled() || this.mLauncher.isSceneShowing())) {
                showRecommendAppsSwitch(isShow, doAnim);
            }
            if (!doAnim) {
                int i2;
                this.mIsEditing = isShow;
                setEnableContent(!isShow);
                if (isShow) {
                    this.mRenameEdit.selectAll();
                    this.mRenameEdit.requestFocus();
                    if (this.mInfo.launchCount != 1) {
                        this.mImm.showSoftInput(this.mRenameEdit, 0);
                    }
                    this.mLastEnableRecommend = this.mInfo.isRecommendAppsViewEnable(this.mContext);
                } else {
                    this.mImm.hideSoftInputFromWindow(getWindowToken(), 0);
                    this.mLauncher.scrollToDefault();
                    this.mContent.setAlpha(1.0f);
                    boolean enableRecommend = this.mInfo.isRecommendAppsViewEnable(this.mContext);
                    if (this.mLastEnableRecommend == enableRecommend) {
                        AnalyticalDataCollector.setRecommendSwitchChanged(this.mContext, "none");
                    } else if (this.mLastEnableRecommend && !enableRecommend) {
                        AnalyticalDataCollector.setRecommendSwitchChanged(this.mContext, "disable");
                    } else if (!this.mLastEnableRecommend && enableRecommend) {
                        AnalyticalDataCollector.setRecommendSwitchChanged(this.mContext, "enable");
                    }
                }
                TextView textView = this.mTitleText;
                if (isShow) {
                    i2 = 4;
                } else {
                    i2 = 0;
                }
                textView.setVisibility(i2);
                EditText editText = this.mRenameEdit;
                if (!isShow) {
                    i = 4;
                }
                editText.setVisibility(i);
            } else if (this.mEditAnimatorShowing) {
                this.mEditAnimator.cancel();
            } else {
                this.mEditAnimator.start();
                this.mTitleText.setVisibility(0);
                this.mRenameEdit.setVisibility(0);
                FolderGridView folderGridView = this.mContent;
                if (isShow) {
                    z = false;
                }
                folderGridView.setEnabled(z);
            }
        }
    }

    private void transFolderGrid(boolean transDown, boolean doAnim) {
        int transY = transDown ? (int) (((double) DeviceConfig.getCellHeight()) * 0.8d) : 0;
        if (doAnim) {
            this.mContent.animate().translationY((float) transY).setDuration((long) DEFAULT_FOLDER_EDITING_DRUATION).start();
        } else {
            this.mContent.setTranslationY((float) transY);
        }
    }

    public void showRecommendAppsSwitch(boolean isShow, boolean doAnim) {
        if (Utilities.isRecommendationEnabled(this.mContext)) {
            if (!this.mLauncher.isInEditing()) {
                transFolderGrid(isShow, doAnim);
            }
            this.mRecommendAppsSwitch.animate().setListener(null).cancel();
            this.mLauncher.getFolderCling().setDrawChildrenReverse(isShow);
            if (!isShow || this.mLauncher.isInNormalEditing()) {
                if (doAnim) {
                    this.mRecommendAppsSwitch.animate().scaleY(0.0f).alpha(0.0f).setDuration((long) DEFAULT_FOLDER_EDITING_DRUATION).setListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            Folder.this.mRecommendAppsSwitch.setVisibility(4);
                        }
                    }).setUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Folder.this.mLauncher.getFolderCling().getRecommendScreen().setAlpha(1.0f - Folder.this.mRecommendAppsSwitch.getAlpha());
                            Folder.this.mLauncher.getFolderCling().invalidate();
                        }
                    }).setInterpolator(new DecelerateInterpolator()).start();
                } else {
                    this.mRecommendAppsSwitch.setVisibility(4);
                }
            } else if (doAnim) {
                this.mRecommendAppsSwitch.setVisibility(0);
                this.mRecommendAppsSwitch.setAlpha(0.0f);
                this.mRecommendAppsSwitch.setScaleY(0.0f);
                this.mRecommendAppsSwitch.animate().scaleY(1.0f).alpha(1.0f).setDuration((long) DEFAULT_FOLDER_EDITING_DRUATION).setUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        Folder.this.mLauncher.getFolderCling().getRecommendScreen().setAlpha(1.0f - Folder.this.mRecommendAppsSwitch.getAlpha());
                        Folder.this.mLauncher.getFolderCling().invalidate();
                    }
                }).setInterpolator(new DecelerateInterpolator()).start();
            } else {
                this.mRecommendAppsSwitch.setVisibility(0);
                this.mRecommendAppsSwitch.setAlpha(1.0f);
                this.mRecommendAppsSwitch.setScaleY(1.0f);
            }
        }
    }

    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if (this.mRecommendAppsSwitch.getVisibility() == 0) {
            invalidate();
        }
        return super.invalidateChildInParent(location, dirty);
    }

    public boolean isEditing() {
        return this.mIsEditing;
    }

    private void setEnableContent(boolean enabled) {
        this.mContent.setEnabled(enabled);
        this.mContent.setClickable(enabled);
        this.mContent.setLongClickable(enabled);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!this.mClosing && this.mInfo.opened) {
            this.mLauncher.onClick(view);
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!view.isInTouchMode() || Launcher.isChildrenModeEnabled() || Utilities.isScreenCellsLocked(this.mLauncher) || this.mLauncher.isPrivacyModeEnabled() || this.mLauncher.isSceneShowing() || this.mClosing || !this.mInfo.opened) {
            return false;
        }
        ShortcutInfo app = (ShortcutInfo) view.getTag();
        app.setBuddyIconView(null, null);
        this.mDragController.startDrag(view, true, this, 3);
        this.mDragItem = app;
        if (!this.mLauncher.isInNormalEditing()) {
            this.mEditBackgroundAnimator.setIntValues(new int[]{0, 255});
            this.mEditBackgroundAnimator.start();
        }
        return true;
    }

    public ShortcutInfo getDragedItem() {
        return this.mDragItem;
    }

    private void setBackgroundAlpha(float alpha) {
        if (this.mBackground != null) {
            this.mBackground.setAlpha((int) (alpha * 255.0f));
        }
        if (this.mLauncher.isInNormalEditing()) {
            this.mContentBackground.setAlpha((int) (alpha * 255.0f));
        }
    }

    public void setupOutOfPreviewContent(float offset) {
        int previewCount = this.mInfo.icon.getPreviewCount();
        if (this.mContent.getChildCount() > previewCount) {
            for (int i = this.mContent.getChildCount() - 1; i >= previewCount; i--) {
                this.mContent.getChildAt(i).setAlpha(offset);
            }
        }
    }

    public void enableInteractive(boolean enabled) {
        setEnableContent(enabled);
        this.mTitleText.setEnabled(enabled);
        setEnabled(enabled);
    }

    public void setContentAlpha(float alpha) {
        this.mTitleText.setAlpha(alpha);
        this.mContent.setAlpha(alpha);
    }

    public int getFolderSize() {
        if (this.mInfo == null) {
            return -1;
        }
        return this.mInfo.count();
    }

    public void onWallpaperColorChanged() {
        if (WallpaperUtils.getCurrentWallpaperColorMode() == 2) {
            this.mBackgroundView.setBackgroundResource(R.drawable.folder_content_bg_light);
            this.mRenameEdit.setBackgroundResource(R.drawable.folder_setting_rename_bg_light);
            this.mRenameEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getContext().getResources().getDrawable(R.drawable.edit_text_search_clear_btn_on_light), null);
        } else {
            this.mBackgroundView.setBackgroundResource(R.drawable.folder_content_bg_dark);
            this.mRenameEdit.setBackgroundResource(R.drawable.folder_setting_rename_bg_dark);
            this.mRenameEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getContext().getResources().getDrawable(R.drawable.edit_text_search_clear_btn_on_dark), null);
        }
        this.mContentBackground = this.mBackgroundView.getBackground();
        if (this.mLauncher.getFolderCling().isOpened()) {
            this.mContentBackground.setAlpha(0);
        }
    }

    public View getForceTouchSelectedView() {
        return this.mContent.getForceTouchSelectedView();
    }

    public boolean isEditAnimatorShowing() {
        return this.mEditAnimatorShowing;
    }
}
