package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.AutoLayoutAnimation.GhostView;
import com.miui.home.launcher.AutoLayoutAnimation.HostView;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import com.miui.home.launcher.common.Utilities;
import java.lang.reflect.Field;
import miui.maml.FancyDrawable;

public abstract class ItemIcon extends FrameLayout implements HostView, VisualizeCalibration, WallpaperColorChangedListener {
    private static Canvas sCanvas = null;
    private static final Paint sTitleLayerPaint = new Paint(2);
    private static Rect sTmpRect = new Rect();
    static TypedValue sTmpValue = new TypedValue();
    private boolean mDrawTouchMask = false;
    private boolean mEnableTouchMask = true;
    private boolean mFirstDrawMark = false;
    private GhostView mGhostView;
    protected ImageView mIcon;
    private Bitmap mIconBitmap = null;
    protected FrameLayout mIconContainer;
    private Bitmap mIconDarkShadow = null;
    protected ImageView mIconTile;
    private boolean mIsDockMode = false;
    private boolean mIsEnableAutoLayoutAnimation = false;
    private boolean mIsHideShadow;
    private boolean mIsHideTitle;
    private boolean mIsHideTouchMask;
    private boolean mIsShowMessageAnimation = false;
    protected int mMaskColor;
    protected TextView mMessage;
    private Runnable mMessageAnimation = new Runnable() {
        public void run() {
            if (ItemIcon.this.mMessage != null) {
                ItemIcon.this.mMessage.animate().setListener(null).cancel();
                if (ItemIcon.this.mIsShowMessageAnimation) {
                    ItemIcon.this.mMessage.setScaleX(0.0f);
                    ItemIcon.this.mMessage.setScaleY(0.0f);
                    ItemIcon.this.mMessage.animate().scaleX(1.0f).scaleY(1.0f).setStartDelay(0).start();
                    ItemIcon.this.mMessage.setVisibility(0);
                    return;
                }
                ItemIcon.this.mMessage.animate().scaleX(0.0f).scaleY(0.0f).setStartDelay(0).setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ItemIcon.this.mMessage.setText(null);
                        ItemIcon.this.mMessage.setVisibility(4);
                        ItemIcon.this.mMessageBackground = null;
                    }
                }).start();
            }
        }
    };
    private String mMessageBackground;
    private byte[] mMessageIconTile;
    private CharSequence mMessageOldText = "";
    private OnSlideVerticallyListener mOnSlideVerticallyListener;
    private Runnable mPerformClickRunnable;
    private boolean mSkipNextAutoLayoutAnimation = false;
    protected TextView mTitle;
    protected View mTitleContainer;
    private boolean mTouchDown = false;

    public interface OnSlideVerticallyListener {
        void onSlideVertically(ItemIcon itemIcon);
    }

    public ItemIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        setLayerType(getDefaultLayerType(), null);
        this.mMaskColor = res.getColor(R.color.folder_foreground_mask);
    }

    protected void onFinishInflate() {
        this.mIconContainer = (FrameLayout) findViewById(R.id.icon_container);
        this.mIconTile = (ImageView) findViewById(R.id.icon_tile);
        this.mIcon = (ImageView) findViewById(R.id.icon_icon);
        this.mMessage = (TextView) findViewById(R.id.icon_msg);
        if (this.mMessage != null) {
            this.mMessage.animate().setDuration(300);
        }
        this.mTitle = (TextView) findViewById(R.id.icon_title);
        this.mTitleContainer = findViewById(R.id.icon_title_container);
        this.mTitle.setLayerPaint(sTitleLayerPaint);
    }

    public int getDefaultLayerType() {
        return 0;
    }

    private void drawDarkShadow(Canvas canvas, int width, int height) {
        if (!WallpaperUtils.hasAppliedLightWallpaper()) {
            if (this.mIconBitmap != null && this.mIconDarkShadow == null && width > 0 && height > 0) {
                Resources res = getContext().getResources();
                float shadowSize = res.getDimension(R.dimen.icon_shadow_size);
                Bitmap mask = this.mIconBitmap.extractAlpha(Utilities.getIconShadowBlurPaint(shadowSize), null);
                if (sCanvas == null) {
                    sCanvas = new Canvas();
                }
                this.mIconDarkShadow = Utilities.createBitmapSafely(width, height, mask.getConfig());
                if (this.mIconDarkShadow != null) {
                    sCanvas.setBitmap(this.mIconDarkShadow);
                    sTmpRect.left = this.mIconContainer.getLeft() + this.mIcon.getLeft();
                    sTmpRect.top = this.mIconContainer.getTop() + this.mIcon.getTop();
                    sTmpRect.right = sTmpRect.left + this.mIcon.getWidth();
                    sTmpRect.bottom = sTmpRect.top + this.mIcon.getHeight();
                    sCanvas.drawBitmap(mask, null, sTmpRect, Utilities.getIconDarkShadowPaint(shadowSize, res.getColor(R.color.icon_shadow)));
                } else {
                    return;
                }
            }
            if (this.mIconDarkShadow != null) {
                canvas.drawBitmap(this.mIconDarkShadow, 0.0f, 0.0f, null);
            }
        }
    }

    private void drawReflectionShadow(Canvas canvas) {
        if (isDockViewMode() && this.mIconBitmap != null) {
            canvas.clipRect(0, 0, getWidth(), getHeight());
            Matrix m = new Matrix();
            m.setScale(((float) this.mIcon.getWidth()) / ((float) this.mIconBitmap.getWidth()), ((float) (-this.mIcon.getHeight())) / ((float) this.mIconBitmap.getHeight()), ((float) this.mIcon.getWidth()) / 2.0f, ((float) this.mIcon.getHeight()) / 2.0f);
            m.postTranslate((float) ((getWidth() - this.mIconBitmap.getWidth()) / 2), ((float) this.mIcon.getBottom()) + getContext().getResources().getDimension(R.dimen.icon_reflection_gap));
            canvas.drawBitmap(this.mIconBitmap, m, null);
            Paint p = new Paint();
            p.setShader(new LinearGradient(0.0f, (float) this.mIcon.getHeight(), 0.0f, (float) getHeight(), WallpaperUtils.hasAppliedLightWallpaper() ? getContext().getResources().getColor(R.color.light_icon_reflection_level) : getContext().getResources().getColor(R.color.default_icon_reflection_level), 16777215, TileMode.CLAMP));
            p.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
            canvas.drawRect(0.0f, (float) this.mIcon.getHeight(), (float) getWidth(), (float) getHeight(), p);
        }
    }

    public void onWallpaperColorChanged() {
        boolean z = true;
        if (this.mTitle != null) {
            ItemInfo info = (ItemInfo) getTag();
            boolean useDarkTitle = WallpaperUtils.hasAppliedLightWallpaper();
            int iconTitleBgMode = 0;
            if (!isInFolder()) {
                if (DeviceConfig.isScreenOrientationLandscape() && info.container == -100) {
                    ContentValues value = new ContentValues();
                    value.put("cellX", Integer.valueOf(info.cellX));
                    value.put("cellY", Integer.valueOf(info.cellY));
                    DeviceConfig.portraitCellPosition(value);
                    iconTitleBgMode = WallpaperUtils.getIconTitleBgMode(value.getAsInteger("cellX").intValue(), value.getAsInteger("cellY").intValue(), false);
                } else {
                    iconTitleBgMode = WallpaperUtils.getIconTitleBgMode(info.cellX, info.cellY, info.container == -101);
                }
            }
            if (useDarkTitle) {
                z = false;
            }
            setTitleColorMode(useDarkTitle, z, WallpaperUtils.getTitleShadowColor(iconTitleBgMode));
            invalidate();
        }
    }

    private boolean isInFolder() {
        if (this instanceof ShortcutIcon) {
            ShortcutInfo info = (ShortcutInfo) getTag();
            if (!(info.container == -100 || info.container == -101)) {
                return true;
            }
        }
        return false;
    }

    public void initDefaultTitle() {
        setTitleColorMode(false, false, 0);
    }

    public void setTitleColorMode(boolean isDark, boolean usingShadow, int shadowColor) {
        int titleStyle;
        int themeShadowColorId;
        boolean inFolder = isInFolder();
        if (isDark) {
            titleStyle = inFolder ? R.style.WorkspaceIconTitle.folder.dark : R.style.WorkspaceIconTitle.dark;
            themeShadowColorId = inFolder ? R.color.folder_icon_title_text_shadow_light : R.color.icon_title_text_shadow_light;
        } else {
            titleStyle = inFolder ? R.style.WorkspaceIconTitle.folder : R.style.WorkspaceIconTitle;
            themeShadowColorId = inFolder ? R.color.folder_icon_title_text_shadow : R.color.icon_title_text_shadow;
        }
        this.mTitle.setTextAppearance(this.mContext, titleStyle);
        if (this.mContext.getResources().getColor(themeShadowColorId) != 0) {
            setTitleShadow(this.mContext, this.mTitle, this.mContext.getResources().getColor(themeShadowColorId));
        } else {
            setTitleShadow(this.mContext, this.mTitle, shadowColor);
        }
    }

    public static void setTitleShadow(Context context, TextView title, int shadowColor) {
        Resources res = context.getResources();
        res.getValue(R.dimen.workspace_icon_text_shadow_radius, sTmpValue, true);
        float radius = sTmpValue.getFloat();
        res.getValue(R.dimen.workspace_icon_text_shadow_dx, sTmpValue, true);
        float dx = sTmpValue.getFloat();
        res.getValue(R.dimen.workspace_icon_text_shadow_dy, sTmpValue, true);
        title.setShadowLayer(radius, dx, sTmpValue.getFloat(), shadowColor);
    }

    public void setMessage(String text) {
        setMessage(text, null, null);
    }

    public void setMessage(String text, String textBg, byte[] tile) {
        if (text == null) {
            text = "";
        }
        Launcher launcher = LauncherApplication.getLauncher(this.mContext);
        if (launcher != null) {
            if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(this.mMessageOldText) && textBg == null) {
                this.mMessageOldText = "";
                this.mIsShowMessageAnimation = false;
                launcher.getForegroundTaskQueue().addTask(launcher, launcher.getWorkspace().getHandler(), this.mMessageAnimation);
            } else if (!(TextUtils.equals(text, this.mMessageOldText) && textBg == null)) {
                this.mIsShowMessageAnimation = true;
                setMessageTextBackground(textBg);
                if (TextUtils.isEmpty(this.mMessageOldText)) {
                    this.mMessage.animate().setListener(null).cancel();
                    launcher.getForegroundTaskQueue().addTask(launcher, launcher.getWorkspace().getHandler(), this.mMessageAnimation);
                }
                this.mMessage.setText(text);
                this.mMessageOldText = text;
            }
            setMessageIconTile(tile);
            this.mMessageIconTile = tile;
        }
    }

    private Drawable getRemoteResourceDrawable(String resString) {
        Drawable drawable = null;
        if (resString != null) {
            try {
                Resources res = this.mContext.getPackageManager().getResourcesForApplication(getResourcePackage(resString));
                drawable = res.getDrawable(res.getIdentifier(resString, null, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return drawable;
    }

    private void setMessageTextBackground(String textBg) {
        Drawable drawable = getRemoteResourceDrawable(textBg);
        if (drawable != null) {
            this.mMessage.setBackground(drawable);
        } else {
            this.mMessage.setBackgroundResource(R.drawable.icon_notification_bg);
        }
        this.mMessageBackground = textBg;
    }

    private void setMessageIconTile(byte[] tile) {
        if (!(this.mIcon.getDrawable() instanceof FancyDrawable) && this.mMessageIconTile != tile && this.mIconTile != null) {
            if (tile != null) {
                this.mIconTile.setImageBitmap(BitmapFactory.decodeByteArray(tile, 0, tile.length));
                this.mIconTile.setVisibility(0);
                return;
            }
            this.mIconTile.setImageBitmap(null);
            this.mIconTile.setVisibility(4);
        }
    }

    private String getResourcePackage(String resource) {
        return resource.substring(0, resource.indexOf(58));
    }

    public void setOnSlideVerticallyListener(OnSlideVerticallyListener l) {
        this.mOnSlideVerticallyListener = l;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mOnSlideVerticallyListener != null) {
            switch (event.getAction()) {
                case 0:
                    this.mTouchDown = true;
                    break;
                case 1:
                case 3:
                    this.mTouchDown = false;
                    break;
                case 2:
                    float localY = event.getY();
                    if (this.mTouchDown && (localY < 0.0f || localY > ((float) getHeight()))) {
                        this.mOnSlideVerticallyListener.onSlideVertically(this);
                        this.mTouchDown = false;
                        requestDisallowInterceptTouchEvent(true);
                        break;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    private void initPerformClickRunnable() {
        try {
            Field field = View.class.getDeclaredField("mPerformClick");
            if (field != null) {
                field.setAccessible(true);
                this.mPerformClickRunnable = (Runnable) field.get(this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean post(Runnable action) {
        if (!(getParent() instanceof MultiSelectContainerView)) {
            if (this.mPerformClickRunnable == null) {
                initPerformClickRunnable();
            }
            if (action != null && action == this.mPerformClickRunnable) {
                action.run();
                return true;
            }
        }
        return super.post(action);
    }

    public boolean isInScrollingContainer() {
        return false;
    }

    protected void drawableStateChanged() {
        int[] stateSets = getDrawableState();
        boolean drawMask = StateSet.stateSetMatches(PRESSED_STATE_SET, stateSets) || StateSet.stateSetMatches(FOCUSED_WINDOW_FOCUSED_STATE_SET, stateSets);
        if (this.mDrawTouchMask != drawMask && this.mEnableTouchMask) {
            this.mDrawTouchMask = drawMask;
            invalidate();
        }
        super.drawableStateChanged();
    }

    protected void dispatchDraw(Canvas canvas) {
        this.mFirstDrawMark = true;
        super.dispatchDraw(canvas);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (this.mFirstDrawMark) {
            if (Launcher.isEnableIconShadow()) {
                if (!this.mIsHideShadow) {
                    drawReflectionShadow(canvas);
                    drawDarkShadow(canvas, getMeasuredWidth(), getMeasuredHeight());
                }
            } else if (!(this.mIconDarkShadow == null || Launcher.isEnableIconShadow())) {
                this.mIconDarkShadow.recycle();
                this.mIconDarkShadow = null;
            }
            this.mFirstDrawMark = false;
        }
        if (this.mDrawTouchMask && child == this.mIconContainer && !this.mIsHideTouchMask) {
            canvas.saveLayer((float) this.mIconContainer.getLeft(), (float) this.mIconContainer.getTop(), (float) this.mIconContainer.getRight(), (float) this.mIconContainer.getBottom(), null, 31);
            boolean result = super.drawChild(canvas, child, drawingTime);
            canvas.drawColor(this.mMaskColor, Mode.SRC_ATOP);
            canvas.restore();
            return result;
        } else if (this.mIsHideTitle && child == this.mTitleContainer) {
            return false;
        } else {
            return super.drawChild(canvas, child, drawingTime);
        }
    }

    public void setIcon(Drawable d, Bitmap bitmap) {
        this.mIcon.setImageDrawable(d);
        if (bitmap != null) {
            this.mIconBitmap = bitmap;
        } else if (d instanceof BitmapDrawable) {
            this.mIconBitmap = ((BitmapDrawable) d).getBitmap();
        }
    }

    public ImageView getIcon() {
        return this.mIcon;
    }

    public void getVisionOffset(int[] offset) {
        offset[0] = this.mIcon.getLeft();
        offset[1] = this.mIcon.getTop();
    }

    public void setTitle(CharSequence text) {
        if (!this.mTitle.getText().equals(text)) {
            this.mTitle.setText(text);
            setContentDescription(text);
        }
    }

    public boolean isDockViewMode() {
        return this.mIsDockMode;
    }

    public void setDockViewMode(boolean isDockMode) {
        if (this.mIsDockMode != isDockMode) {
            this.mIsDockMode = isDockMode;
            invalidate();
        }
    }

    public void setEnableAutoLayoutAnimation(boolean isEnable) {
        this.mIsEnableAutoLayoutAnimation = isEnable;
    }

    public void setSkipNextAutoLayoutAnimation(boolean isSkip) {
        this.mSkipNextAutoLayoutAnimation = isSkip;
    }

    public boolean getSkipNextAutoLayoutAnimation() {
        return this.mSkipNextAutoLayoutAnimation;
    }

    public boolean isEnableAutoLayoutAnimation() {
        return this.mIsEnableAutoLayoutAnimation;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getSize(widthMeasureSpec) > DeviceConfig.getCellWidth()) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(DeviceConfig.getCellWidth(), 1073741824);
        }
        if (MeasureSpec.getSize(heightMeasureSpec) > DeviceConfig.getCellHeight()) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(DeviceConfig.getCellHeight(), 1073741824);
        }
        if (DeviceConfig.isNote720pMode()) {
            LayoutParams lp = this.mIcon.getLayoutParams();
            lp.width = DeviceConfig.getIconWidth();
            lp.height = DeviceConfig.getIconHeight();
            Resources res = getContext().getResources();
            this.mTitle.setTextSize(0, (float) res.getDimensionPixelSize(R.dimen.note_720p_workspace_icon_text_size));
            this.mTitleContainer.setPadding(this.mTitleContainer.getPaddingLeft(), this.mTitleContainer.getPaddingTop(), this.mTitleContainer.getPaddingRight(), res.getDimensionPixelSize(R.dimen.note_720p_icon_title_padding_bottom));
            if (this instanceof FolderIcon) {
                FolderIcon fi = (FolderIcon) this;
                fi.setPreviewPosition(res.getDimensionPixelSize(R.dimen.note_720p_folder_preview_width), res.getDimensionPixelSize(R.dimen.note_720p_folder_preview_height), res.getDimensionPixelSize(R.dimen.note_720p_folder_preview_top_margin));
                lp = fi.getCover().getLayoutParams();
                lp.width = DeviceConfig.getIconWidth();
                lp.height = DeviceConfig.getIconHeight();
            } else if (this instanceof ShortcutIcon) {
                View fbg = findViewById(R.id.icon_folder_creation_bg);
                if (fbg != null) {
                    lp = fbg.getLayoutParams();
                    if (lp != null) {
                        lp.width = DeviceConfig.getIconWidth();
                        lp.height = DeviceConfig.getIconHeight();
                    }
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public boolean superSetFrame(int left, int top, int right, int bottom) {
        return super.setFrame(left, top, right, bottom);
    }

    protected boolean setFrame(int left, int top, int right, int bottom) {
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        if (!AutoLayoutAnimation.setFrame(this, left, top, right, bottom)) {
            return false;
        }
        if (!(this.mIconDarkShadow == null || (oldWidth == getWidth() && oldHeight == getHeight()))) {
            this.mIconDarkShadow = null;
        }
        return true;
    }

    protected boolean isTransformedTouchPointInView(float x, float y, View child, PointF outLocalPoint) {
        return false;
    }

    public void setGhostView(GhostView gv) {
        this.mGhostView = gv;
    }

    public GhostView getGhostView() {
        return this.mGhostView;
    }

    public void setTextAlpha(float alpha) {
        this.mTitle.setAlpha(alpha);
    }

    public void setIsHideTitle(boolean isHideTitle) {
        this.mIsHideTitle = isHideTitle;
    }

    public boolean getIsHideTitle() {
        return this.mIsHideTitle;
    }

    public void setIsHideShadow(boolean isHideShadow) {
        this.mIsHideShadow = isHideShadow;
    }

    public boolean getIsHideShadow() {
        return this.mIsHideShadow;
    }

    public void enableDrawTouchMask(boolean enable) {
        this.mEnableTouchMask = enable;
    }

    public void buildDrawingCache(boolean autoScale) {
        if (getLayerType() == 1) {
            super.buildDrawingCache(autoScale);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void setIsHideTouchMask(boolean isHideTouchMask) {
        this.mIsHideTouchMask = isHideTouchMask;
    }

    public TextView getTitle() {
        return this.mTitle;
    }
}
