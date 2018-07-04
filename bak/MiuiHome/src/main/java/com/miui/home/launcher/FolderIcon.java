package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.miui.home.R;
import com.miui.home.launcher.Folder.FolderCallback;
import com.miui.home.launcher.common.AppCategoryManager;
import com.miui.home.launcher.common.AppCategoryManager.OnCategoryLoadedListener;
import com.miui.home.launcher.common.Utilities;
import miui.content.res.IconCustomizer;
import miui.maml.FancyDrawable;

public class FolderIcon extends ItemIcon implements DropTarget, FolderCallback {
    private ValueAnimator mAlphaAnimator = new ValueAnimator();
    private Runnable mDragOpenFolder = new Runnable() {
        public void run() {
            FolderIcon.this.mAlphaAnimator.cancel();
            FolderIcon.this.mLauncher.getFolderCling().prepareAutoOpening();
            FolderIcon.this.mLauncher.openFolder(FolderIcon.this.mInfo, null);
        }
    };
    private ValueAnimator mDragOverAnimator = new ValueAnimator();
    private Drawable mFolderBackground;
    private ImageView mFolderCover;
    private IconLoader mIconLoader;
    private FolderInfo mInfo;
    private boolean mIsDragingEnter = false;
    private boolean mIsPreRemoved = false;
    private PreviewIconView[] mItemIcons;
    private Launcher mLauncher;
    private View mPreviewContainer;
    private float[] mTmpPos = new float[2];

    public static class PreviewIconView extends ImageView {
        public ShortcutInfo mBuddyInfo;
        private Context mContext;

        public PreviewIconView(Context context) {
            super(context);
            this.mContext = context;
        }

        public PreviewIconView(Context context, AttributeSet attrs) {
            super(context, attrs, 0);
            this.mContext = context;
        }

        public PreviewIconView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.mContext = context;
        }

        protected void onDraw(Canvas canvas) {
            if (this.mBuddyInfo == null || this.mBuddyInfo.progressStatus == -5) {
                super.onDraw(canvas);
                return;
            }
            canvas.save();
            canvas.scale(((float) getWidth()) / ((float) this.mBuddyInfo.getIconBitmap().getWidth()), ((float) getHeight()) / ((float) this.mBuddyInfo.getIconBitmap().getHeight()));
            ApplicationProgressProcessor.drawProgressIcon(this.mContext, canvas, this.mBuddyInfo.getIconBitmap(), this.mBuddyInfo.progressPercent);
            canvas.restore();
        }
    }

    public FolderIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mIconLoader = Application.getLauncherApplication(context).getIconLoader();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mItemIcons = new PreviewIconView[]{(PreviewIconView) findViewById(R.id.item1), (PreviewIconView) findViewById(R.id.item2), (PreviewIconView) findViewById(R.id.item3), (PreviewIconView) findViewById(R.id.item4), (PreviewIconView) findViewById(R.id.item5), (PreviewIconView) findViewById(R.id.item6), (PreviewIconView) findViewById(R.id.item7), (PreviewIconView) findViewById(R.id.item8), (PreviewIconView) findViewById(R.id.item9)};
        this.mPreviewContainer = findViewById(R.id.preview_icons_container);
        Resources res = this.mContext.getResources();
        this.mFolderBackground = loadFolderIconBitmap(this.mContext);
        this.mDragOverAnimator.setDuration((long) res.getInteger(17694720));
        this.mDragOverAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                FolderIcon.this.mIcon.setScaleX(value);
                FolderIcon.this.mIcon.setScaleY(value);
                FolderIcon.this.mFolderCover.setScaleX(value);
                FolderIcon.this.mFolderCover.setScaleY(value);
            }
        });
        this.mDragOverAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                if (FolderIcon.this.mIsDragingEnter) {
                    FolderIcon.this.mAlphaAnimator.start();
                }
            }
        });
        this.mAlphaAnimator.setFloatValues(new float[]{1.0f, 0.5f, 0.0f, 0.5f, 1.0f});
        this.mAlphaAnimator.setDuration(300);
        this.mAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                FolderIcon.this.mIcon.setAlpha(value);
                FolderIcon.this.mFolderCover.setAlpha(value);
            }
        });
        this.mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                FolderIcon.this.mFolderCover.setAlpha(1.0f);
                FolderIcon.this.mIcon.setAlpha(1.0f);
            }
        });
        this.mAlphaAnimator.setRepeatCount(-1);
        this.mAlphaAnimator.setRepeatMode(1);
        Drawable cover = IconCustomizer.getRawIconDrawable("folder_icon_cover.png");
        if (cover == null) {
            cover = IconCustomizer.getRawIconDrawable("folder_icon_cover_01.png");
        }
        this.mFolderCover = (ImageView) findViewById(R.id.cover);
        if (cover != null) {
            this.mFolderCover.setImageDrawable(cover);
        } else {
            this.mFolderCover.setImageResource(R.drawable.folder_icon_cover);
        }
    }

    public int getDefaultLayerType() {
        return 1;
    }

    public static final Drawable loadFolderIconBitmap(Context context) {
        Drawable icon = IconCustomizer.getRawIconDrawable("icon_folder.png");
        if (icon == null) {
            return context.getResources().getDrawable(R.drawable.icon_folder);
        }
        return icon;
    }

    public static final Drawable loadFolderIconLightBitmap(Context context) {
        Drawable icon = IconCustomizer.getRawIconDrawable("icon_folder_light.png");
        if (icon == null) {
            return context.getResources().getDrawable(R.drawable.icon_folder_light);
        }
        return icon;
    }

    public void onWallpaperColorChanged() {
        super.onWallpaperColorChanged();
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mFolderBackground = loadFolderIconLightBitmap(this.mContext);
        } else {
            this.mFolderBackground = loadFolderIconBitmap(this.mContext);
        }
        setIcon(this.mFolderBackground, null);
    }

    public static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group, FolderInfo folderInfo) {
        FolderIcon fi = folderInfo.getBuddyIconView();
        if (fi == null) {
            fi = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
            folderInfo.setBuddyIconView(fi);
        }
        LayoutParams olp = fi.getLayoutParams();
        LayoutParams nlp = null;
        if (olp == null) {
            olp = new LayoutParams(-1, -1);
        }
        if ((group instanceof CellLayout) && !(olp instanceof CellLayout.LayoutParams)) {
            nlp = new CellLayout.LayoutParams(olp);
        }
        if (nlp != null) {
            fi.setLayoutParams(nlp);
        }
        fi.setIcon(fi.mFolderBackground, null);
        fi.setTitle(folderInfo.getTitle(launcher));
        fi.setTag(folderInfo);
        fi.mInfo = folderInfo;
        fi.mLauncher = launcher;
        folderInfo.icon = fi;
        folderInfo.notifyDataSetChanged();
        folderInfo.preLoadContentView(launcher);
        return fi;
    }

    public void invalidatePreviews() {
        for (PreviewIconView invalidate : this.mItemIcons) {
            invalidate.invalidate();
        }
    }

    public void setPreviewPosition(int width, int height, int topMargin) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) this.mPreviewContainer.getLayoutParams();
        lp.width = width;
        lp.height = height;
        lp.topMargin = topMargin;
    }

    public void loadItemIcons() {
        int i;
        int iconIndex = 0;
        int itemCount = this.mInfo.count();
        for (i = 0; i < itemCount && iconIndex < this.mItemIcons.length; i++) {
            ShortcutInfo si = this.mInfo.getAdapter(this.mLauncher).getItem(i);
            Drawable d = si.getIcon(getContext(), this.mIconLoader, this.mItemIcons[iconIndex].getDrawable());
            if (d instanceof FancyDrawable) {
                final Drawable clone = d.getConstantState().newDrawable();
                postDelayed(new Runnable() {
                    public void run() {
                        ((FancyDrawable) clone).onPause();
                    }
                }, 100);
                this.mItemIcons[iconIndex].setImageDrawable(clone);
            } else {
                this.mItemIcons[iconIndex].setImageDrawable(d);
            }
            this.mItemIcons[iconIndex].mBuddyInfo = si;
            if (this.mItemIcons[iconIndex].mBuddyInfo.progressStatus != -5) {
                this.mItemIcons[iconIndex].invalidate();
            }
            iconIndex++;
        }
        for (i = iconIndex; i < this.mItemIcons.length; i++) {
            this.mItemIcons[i].setImageDrawable(null);
            this.mItemIcons[i].mBuddyInfo = null;
        }
        this.mLauncher.updateFolderMessage(this.mInfo);
    }

    public View getHitView() {
        return this;
    }

    public boolean acceptDrop(DragObject d) {
        return isDropable(d.getDragInfo());
    }

    private boolean isDropable(ItemInfo dragInfo) {
        return ((dragInfo.itemType != 0 && dragInfo.itemType != 1 && dragInfo.itemType != 11) || dragInfo.container == -1 || this.mInfo.opened) ? false : true;
    }

    public void onDropStart(DragObject dragObject) {
        this.mAlphaAnimator.cancel();
    }

    public boolean onDrop(DragObject d) {
        int index = this.mInfo.count();
        if (d.dropAction == 3 && d.getDragInfo().cellX < this.mItemIcons.length) {
            index = d.getDragInfo().cellX;
        }
        View animationTarget = this.mItemIcons[Math.min(index, this.mItemIcons.length - 1)];
        DragView dragView = d.getDragView();
        dragView.setScaleTarget(((float) animationTarget.getWidth()) / ((float) dragView.getContent().getWidth()));
        if (index > this.mItemIcons.length) {
            dragView.setFakeTargetMode();
            dragView.setFadeoutAnimationMode();
        }
        dragView.setPivotX(0.0f);
        dragView.setPivotY(0.0f);
        dragView.setAnimateTarget(animationTarget);
        this.mInfo.add((ShortcutInfo) d.getDragInfo(), d.dropAction != 3);
        this.mInfo.notifyDataSetChanged();
        if (d.isLastObject()) {
            LauncherModel.updateFolderItems(this.mContext, this.mInfo);
        }
        return true;
    }

    public void onDragEnter(DragObject d) {
        if (isDropable(d.getDragInfo())) {
            if (isDockViewMode()) {
                this.mDragOverAnimator.setFloatValues(new float[]{1.0f, 1.12f});
            } else {
                this.mDragOverAnimator.setFloatValues(new float[]{1.0f, 1.2f});
            }
            this.mIsDragingEnter = true;
            this.mDragOverAnimator.start();
            postDelayed(this.mDragOpenFolder, (this.mAlphaAnimator.getDuration() * 2) + this.mDragOverAnimator.getDuration());
        }
    }

    public void onDragOver(DragObject d) {
    }

    public void onDragExit(DragObject d) {
        if (isDockViewMode()) {
            this.mDragOverAnimator.setFloatValues(new float[]{1.12f, 1.0f});
        } else {
            this.mDragOverAnimator.setFloatValues(new float[]{1.2f, 1.0f});
        }
        this.mIsDragingEnter = false;
        this.mDragOverAnimator.start();
        this.mAlphaAnimator.cancel();
        getHandler().removeCallbacks(this.mDragOpenFolder);
    }

    public void onOpen() {
    }

    public void onClose() {
        if (this.mLauncher.isInNormalEditing()) {
            loadItemIcons();
        }
    }

    public void getHitRect(Rect outRect) {
        outRect.set(this.mLeft + this.mIcon.getLeft(), this.mTop, (this.mLeft + this.mIcon.getWidth()) + this.mIcon.getLeft(), this.mBottom);
    }

    public boolean isPreRemoved() {
        return this.mIsPreRemoved;
    }

    public boolean isDropEnabled() {
        return true;
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public void deleteSelf() {
        if (this.mInfo.contents.isEmpty()) {
            this.mLauncher.preRemoveItem(this);
            this.mIsPreRemoved = true;
            Animation anim = AnimationUtils.loadAnimation(this.mContext, R.anim.fade_out);
            anim.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    FolderIcon.this.post(new Runnable() {
                        public void run() {
                            FolderIcon.this.mLauncher.removeFolder(FolderIcon.this);
                        }
                    });
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            startAnimation(anim);
        }
    }

    public float getPreviewPosition(Rect rect) {
        float[] fArr = this.mTmpPos;
        this.mTmpPos[1] = 0.0f;
        fArr[0] = 0.0f;
        float scale = Utilities.getDescendantCoordRelativeToAncestor(this.mPreviewContainer, this.mLauncher.getDragLayer(), this.mTmpPos, this.mLauncher.isInNormalEditing(), false);
        if (!this.mLauncher.isInNormalEditing()) {
            scale = 1.0f;
        }
        rect.set((int) this.mTmpPos[0], (int) this.mTmpPos[1], Math.round(this.mTmpPos[0] + (((float) this.mPreviewContainer.getMeasuredWidth()) * scale)), Math.round(this.mTmpPos[1] + (((float) this.mPreviewContainer.getMeasuredHeight()) * scale)));
        return scale;
    }

    public void showPreview(boolean isShow) {
        this.mPreviewContainer.setAlpha(isShow ? 1.0f : 0.0f);
        this.mPreviewContainer.invalidate();
    }

    public int getPreviewCount() {
        return this.mItemIcons.length;
    }

    public ImageView getCover() {
        return this.mFolderCover;
    }

    public void onDropCompleted() {
    }

    public void updateFolderTilte(ShortcutInfo dragItem, ShortcutInfo overItem) {
        AppCategoryManager.getInstance().getAppCategoryId(this.mContext.getApplicationContext(), new OnCategoryLoadedListener() {
            public void onAppCategoryIdLoaded(int resId) {
                FolderIcon.this.mInfo.setTitle(FolderIcon.this.getResources().getString(resId), FolderIcon.this.mContext.getApplicationContext());
            }
        }, dragItem.getPackageName(), overItem.getPackageName());
    }
}
