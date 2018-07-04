package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.market.sdk.AppstoreAppInfo;
import com.market.sdk.ImageCallback;
import com.market.sdk.MarketManager;
import com.miui.home.R;
import com.miui.home.launcher.common.Utilities;
import miui.app.ToggleManager;
import miui.app.ToggleManager.OnToggleChangedListener;
import miui.content.res.IconCustomizer;
import miui.maml.FancyDrawable;
import miui.maml.animation.interpolater.CubicEaseInInterpolater;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class ShortcutIcon extends ItemIcon implements DropTarget, OnToggleChangedListener {
    public static final int DOWNLOADING_BLUR_RADIUS = Utilities.getDipPixelSize(4);
    private static boolean sEnableLoadingAnim = false;
    private Context mContext;
    private ImageView mFolderCreationBg;
    private Animation mFolderCreationBgEnter = null;
    private Animation mFolderCreationBgExit = null;
    private ScaleType mGeneralScaleType;
    private Launcher mLauncher;
    private boolean mStopLoading = true;

    public ShortcutIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public static void setEnableLoadingAnim(boolean enable) {
        sEnableLoadingAnim = enable;
    }

    public void stopLoading() {
        this.mStopLoading = true;
    }

    public void startLoadingAnim() {
        FrameLayout loadingView = (FrameLayout) findViewById(R.id.loading_container);
        if (sEnableLoadingAnim) {
            this.mStopLoading = false;
            loadingView.setVisibility(0);
            View[] loadItems = new View[]{findViewById(R.id.item1), findViewById(R.id.item2), findViewById(R.id.item3), findViewById(R.id.item4)};
            for (View alpha : loadItems) {
                alpha.setAlpha(Launcher.isSupportCompleteAnimation() ? 0.1f : 0.3f);
            }
            startLoading(loadItems);
        }
    }

    private void startLoading(View[] v) {
        if (v != null && v[0] != null && v.length == 4) {
            final ObjectAnimator item1AnimIn = getItemAnimIn(v[0]);
            final ObjectAnimator item1AnimOut = getItemAnimOut(v[0]);
            final ObjectAnimator item2AnimIn = getItemAnimIn(v[1]);
            final ObjectAnimator item2AnimOut = getItemAnimOut(v[1]);
            final ObjectAnimator item3AnimIn = getItemAnimIn(v[2]);
            final ObjectAnimator item3AnimOut = getItemAnimOut(v[2]);
            final ObjectAnimator item4AnimIn = getItemAnimIn(v[3]);
            final ObjectAnimator item4AnimOut = getItemAnimOut(v[3]);
            item1AnimIn.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (ShortcutIcon.sEnableLoadingAnim && !ShortcutIcon.this.mStopLoading) {
                        item1AnimOut.start();
                        item2AnimIn.start();
                    }
                }
            });
            item2AnimIn.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (ShortcutIcon.sEnableLoadingAnim && !ShortcutIcon.this.mStopLoading) {
                        item2AnimOut.start();
                        item3AnimIn.start();
                    }
                }
            });
            item3AnimIn.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (ShortcutIcon.sEnableLoadingAnim && !ShortcutIcon.this.mStopLoading) {
                        item3AnimOut.start();
                        item4AnimIn.start();
                    }
                }
            });
            item4AnimIn.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (ShortcutIcon.sEnableLoadingAnim && !ShortcutIcon.this.mStopLoading) {
                        item4AnimOut.start();
                        item1AnimIn.start();
                    }
                }
            });
            item1AnimIn.start();
        }
    }

    private ObjectAnimator getItemAnimOut(View v) {
        ObjectAnimator animOut = ObjectAnimator.ofFloat(v, "alpha", new float[]{0.3f, 0.1f});
        animOut.setInterpolator(new CubicEaseInInterpolater());
        animOut.setDuration(200);
        return animOut;
    }

    private ObjectAnimator getItemAnimIn(View v) {
        ObjectAnimator animIn = ObjectAnimator.ofFloat(v, "alpha", new float[]{0.1f, 0.3f});
        animIn.setInterpolator(new CubicEaseOutInterpolater());
        animIn.setDuration(200);
        return animIn;
    }

    public void updateTitleMaxWidth() {
        ShortcutInfo info = (ShortcutInfo) getTag();
        if (info == null || info.itemFlags != 4) {
            this.mTitle.setMaxWidth(DeviceConfig.getCellWidth());
            return;
        }
        this.mTitle.setMaxWidth(DeviceConfig.getCellWidth() - (this.mContext.getResources().getDrawable(R.drawable.new_install_notification).getIntrinsicWidth() * 2));
    }

    public void updateInfo(Launcher launcher, ShortcutInfo info) {
        this.mLauncher = launcher;
        setTag(info);
        this.mIcon.setBackground(null);
        updateTitleMaxWidth();
        Drawable icon = info.getIcon(this.mContext, launcher.getIconLoader(), info.getIcon());
        if (icon instanceof FancyDrawable) {
            ((FancyDrawable) icon).onResume();
        }
        setIconTitle(info, launcher, icon);
        if (ProgressManager.isProgressType(info)) {
            onProgressStatusChanged();
        }
        if (!(this.mMessage == null || TextUtils.isEmpty(this.mMessage.getText()))) {
            this.mMessage.requestLayout();
        }
        if (this.mFolderCreationBgEnter == null) {
            this.mFolderCreationBgEnter = AnimationUtils.loadAnimation(launcher, R.anim.folder_creation_bg_enter);
            this.mFolderCreationBgExit = AnimationUtils.loadAnimation(launcher, R.anim.folder_creation_bg_exit);
        }
    }

    public void loadIconFromMarket(final Launcher launcher, final ShortcutInfo info) {
        final AppstoreAppInfo appInfo = info.getAppInfo();
        if (!TextUtils.isEmpty(appInfo.appId)) {
            if (appInfo.iconUri == null || !WallpaperUtils.isUriFileExists(appInfo.iconUri)) {
                MarketManager.getManager(launcher).loadIcon(appInfo.appId, appInfo.iconMask, new ImageCallback() {
                    public void onImageLoadFailed(String url) {
                    }

                    public void onImageLoadSuccess(String url, Uri uri) {
                        final Uri iconUri = uri;
                        launcher.runOnUiThread(new Runnable() {
                            public void run() {
                                appInfo.iconUri = iconUri;
                                info.setIcon(null);
                                ShortcutIcon.this.setIconTitle(info, launcher, info.getIcon(ShortcutIcon.this.mContext, launcher.getIconLoader(), info.getIcon()));
                            }
                        });
                    }
                });
            }
        }
    }

    private void setIconTitle(ShortcutInfo info, Launcher launcher, Drawable icon) {
        if (ProgressManager.isProgressType(info)) {
            setIcon(null, info.getIconBitmap());
            setTitle(info.progressTitle);
        } else {
            setIcon(icon, info.getIconBitmap());
            if (info.mIconType == 3) {
                setTitle(ToggleManager.getStatusName(info.getToggleId(), this.mContext.getResources()));
            } else {
                setTitle(info.getTitle(launcher));
            }
        }
        if (Utilities.hasDefaultIconBackground(info.intent.getComponent())) {
            this.mIcon.setBackground(Utilities.getDefaultIconBackground(launcher, info.intent.getComponent()));
            this.mIcon.setScaleType(ScaleType.CENTER);
        } else if (icon == null || icon.getIntrinsicWidth() == IconCustomizer.getCustomizedIconWidth()) {
            this.mIcon.setScaleType(this.mGeneralScaleType);
        } else if (icon instanceof FancyDrawable) {
            icon.setBounds(0, 0, this.mIcon.getWidth(), this.mIcon.getHeight());
        }
    }

    public void showInstallingAnim() {
    }

    public void onProgressStatusChanged() {
        setTitle(((ShortcutInfo) getTag()).progressTitle);
        invalidate();
    }

    public static ShortcutIcon fromXml(int resId, Launcher launcher, ViewGroup group, ShortcutInfo shortcutInfo) {
        int titleStyleId;
        int shadowColor;
        ShortcutIcon sci = shortcutInfo.getBuddyIconView(group);
        if (sci == null || !(sci.getParent() == null || sci.getParent() == group)) {
            sci = (ShortcutIcon) LayoutInflater.from(launcher).inflate(resId, group, false);
            shortcutInfo.setBuddyIconView(sci, group);
        }
        LayoutParams olp = sci.getLayoutParams();
        LayoutParams nlp = null;
        Resources res = launcher.getResources();
        sci.setVisibility(0);
        if (group instanceof AbsListView) {
            int w = DeviceConfig.getCellWidth();
            int h = DeviceConfig.getCellHeight();
            if (olp == null) {
                olp = new AbsListView.LayoutParams(w, h);
                nlp = olp;
            } else if (!(olp.width == w && olp.height == h)) {
                olp.width = w;
                olp.height = h;
                nlp = olp;
            }
            if (!(olp instanceof AbsListView.LayoutParams)) {
                nlp = new AbsListView.LayoutParams(olp);
            }
            sci.setFocusable(false);
            sci.setAlpha(1.0f);
            sci.setTextAlpha(1.0f);
            sci.setClickable(false);
            sci.setLongClickable(false);
            sci.setEnableAutoLayoutAnimation(false);
            titleStyleId = R.style.WorkspaceIconTitle.folder;
            shadowColor = res.getColor(R.color.folder_icon_title_text_shadow);
        } else {
            if (olp == null) {
                olp = new LayoutParams(-1, -1);
                nlp = olp;
            } else if (!(olp.width == -1 && olp.height == -1)) {
                olp.height = -1;
                olp.width = -1;
                nlp = olp;
            }
            if ((group instanceof CellLayout) && !(olp instanceof CellLayout.LayoutParams)) {
                nlp = new CellLayout.LayoutParams(olp);
            }
            sci.setFocusable(true);
            sci.setEnableAutoLayoutAnimation(true);
            titleStyleId = R.style.WorkspaceIconTitle;
            shadowColor = res.getColor(R.color.icon_title_text_shadow);
        }
        if (nlp != null) {
            sci.setLayoutParams(nlp);
            sci.mTitle.setTextAppearance(launcher, titleStyleId);
            ItemIcon.setTitleShadow(launcher, sci.mTitle, shadowColor);
        }
        sci.updateInfo(launcher, shortcutInfo);
        if (shortcutInfo.mIconType == 3) {
            ToggleManager toggleManager = ToggleManager.createInstance(launcher.getApplicationContext());
            toggleManager.removeToggleChangedListener(sci);
            toggleManager.setOnToggleChangedListener(sci);
        }
        return sci;
    }

    public void OnToggleChanged(int id) {
        ShortcutInfo shortcutInfo = (ShortcutInfo) getTag();
        int toggleId = shortcutInfo.getToggleId();
        if (toggleId == id && this.mLauncher != null) {
            setIcon(shortcutInfo.getIcon(this.mContext, this.mLauncher.getIconLoader(), shortcutInfo.getIcon()), Utilities.loadToggleBackground(this.mContext).getBitmap());
            setTitle(ToggleManager.getStatusName(toggleId, this.mContext.getResources()));
            FolderIcon fi = this.mLauncher.getParentFolderIcon(shortcutInfo);
            if (fi != null) {
                fi.loadItemIcons();
            }
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mGeneralScaleType = this.mIcon.getScaleType();
        this.mFolderCreationBg = (ImageView) findViewById(R.id.icon_folder_creation_bg);
        if (this.mFolderCreationBg != null) {
            Drawable folderIcon = FolderIcon.loadFolderIconBitmap(this.mContext);
            if (folderIcon != null) {
                this.mFolderCreationBg.setImageDrawable(folderIcon);
            }
        }
    }

    public View getHitView() {
        return this;
    }

    public boolean isDropEnabled() {
        return !isDockViewMode();
    }

    public void onDropStart(DragObject dragObject) {
    }

    public boolean onDrop(DragObject dragObject) {
        if (!isDropable(dragObject)) {
            return false;
        }
        this.mFolderCreationBg.startAnimation(this.mFolderCreationBgExit);
        this.mLauncher.getWorkspace().createUserFolderWithDragOverlap(dragObject, (ShortcutInfo) getTag());
        return true;
    }

    public void onDragEnter(DragObject dragObject) {
        this.mFolderCreationBg.startAnimation(this.mFolderCreationBgEnter);
        invalidate();
    }

    public void onDragOver(DragObject dragObject) {
    }

    public void onDragExit(DragObject dragObject) {
        this.mFolderCreationBg.startAnimation(this.mFolderCreationBgExit);
        invalidate();
    }

    private boolean isDropable(DragObject dragObject) {
        return dragObject.getDragInfo().itemType == 0 || dragObject.getDragInfo().itemType == 1 || dragObject.getDragInfo().itemType == 11;
    }

    public DropTarget getDropTargetDelegate(DragObject dragObject) {
        return null;
    }

    public boolean acceptDrop(DragObject dragObject) {
        return isDropable(dragObject);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (getTag() instanceof ShortcutInfo) {
            ShortcutInfo info = (ShortcutInfo) getTag();
            if (child == this.mTitleContainer && !getIsHideTitle() && info.itemFlags == 4 && info.progressStatus == -5) {
                canvas.save();
                Drawable d = this.mContext.getResources().getDrawable(R.drawable.new_install_notification);
                canvas.drawBitmap(((BitmapDrawable) d).getBitmap(), (float) ((this.mTitleContainer.getLeft() + this.mTitle.getLeft()) - d.getIntrinsicWidth()), (float) ((this.mTitleContainer.getTop() + this.mTitle.getTop()) + (((this.mTitle.getBottom() - this.mTitle.getTop()) - d.getIntrinsicHeight()) / 2)), null);
                canvas.restore();
            }
            if (!(child != this.mIconContainer || info.progressStatus == -5 || info.getIconBitmap() == null)) {
                boolean result = super.drawChild(canvas, child, drawingTime);
                canvas.save();
                canvas.translate((float) (this.mIconContainer.getLeft() + this.mIcon.getLeft()), (float) (this.mIconContainer.getTop() + this.mIcon.getTop()));
                canvas.scale(((float) this.mIcon.getWidth()) / ((float) info.getIconBitmap().getWidth()), ((float) this.mIcon.getHeight()) / ((float) info.getIconBitmap().getHeight()));
                ApplicationProgressProcessor.drawProgressIcon(this.mContext, canvas, info.getIconBitmap(), info.progressPercent);
                canvas.restore();
                return result;
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    public void setIconTitleVisible(boolean showIcon, boolean showTitle) {
        int i;
        int i2 = 0;
        ImageView imageView = this.mIcon;
        if (showIcon) {
            i = 0;
        } else {
            i = 4;
        }
        imageView.setVisibility(i);
        TextView textView = this.mTitle;
        if (!showTitle) {
            i2 = 4;
        }
        textView.setVisibility(i2);
    }

    public String toString() {
        return super.toString() + "(" + (getTag() instanceof ShortcutInfo ? ((ShortcutInfo) getTag()).getTitle(this.mContext) : "null") + ")";
    }

    public void onDropCompleted() {
    }
}
