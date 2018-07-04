package com.miui.home.launcher.upsidescene;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.FolderIcon;
import com.miui.home.launcher.FolderInfo;
import com.miui.home.launcher.Launcher;
import com.miui.home.launcher.ShortcutIcon;
import com.miui.home.launcher.ShortcutInfo;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.gadget.AwesomeGadget;
import com.miui.home.launcher.gadget.FreeButtonAwesomeGadget;
import com.miui.home.launcher.gadget.Gadget;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.upsidescene.FreeLayout.LayoutParams;
import com.miui.home.launcher.upsidescene.data.Appearance.FreeButtonAppearance;
import com.miui.home.launcher.upsidescene.data.Appearance.StageImageAppearance;
import com.miui.home.launcher.upsidescene.data.FreeStyleSerializer;
import com.miui.home.launcher.upsidescene.data.Function.AppFunction;
import com.miui.home.launcher.upsidescene.data.Function.FolderFunction;
import com.miui.home.launcher.upsidescene.data.Function.MtzGadgetFunction;
import com.miui.home.launcher.upsidescene.data.Function.SystemGadgetFunction;
import com.miui.home.launcher.upsidescene.data.Function.ToggleFunction;
import com.miui.home.launcher.upsidescene.data.Function.WidgetFunction;
import com.miui.home.launcher.upsidescene.data.Sprite;
import java.io.IOException;
import miui.util.FileAccessable.Factory;

public class SpriteView extends FrameLayout implements AnimatorUpdateListener {
    private static final float BIGGER_OFFSET = ((float) Utilities.getDipPixelSize(5));
    private static final int SHAKE_OFFSET = Utilities.getDipPixelSize(3);
    private static int sCellHeight;
    static ValueAnimator sHighlightAllAlphaAnimator;
    static float sHighlightCurrentValue = 1.0f;
    private View mContent;
    private float mHighlightOffsetRandom = (((float) Math.random()) * 4.0f);
    private boolean mIsHighlight;
    private SceneScreen mSceneScreen;
    private Sprite mSpriteData;

    private static class UpsideFolderInfo extends FolderInfo {
        SceneScreen mSceneScreen;
        Sprite mSprite;

        public UpsideFolderInfo(Sprite sprite, SceneScreen sceneScreen) {
            this.mSprite = sprite;
            this.mSceneScreen = sceneScreen;
        }

        public void setTitle(CharSequence title, Context context) {
            super.setTitle(title, context);
            ((FolderFunction) this.mSprite.getFunction()).setFolderName(title.toString());
            this.mSceneScreen.save();
        }
    }

    public SpriteView(Context context, Sprite sprite, SceneScreen sceneScreen, View contentView) {
        super(context);
        sCellHeight = getResources().getDimensionPixelSize(R.dimen.workspace_cell_height);
        this.mSpriteData = sprite;
        this.mSceneScreen = sceneScreen;
        setContentView(contentView);
    }

    public Sprite getSpriteData() {
        return this.mSpriteData;
    }

    public View getContentView() {
        return this.mContent;
    }

    public FreeLayout getParentLayout() {
        return (FreeLayout) getParent();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mSpriteData.getAppearance().getType() == 0 && (this.mSpriteData.getFunction().getType() == 2 || this.mSpriteData.getFunction().getType() == 9 || this.mSpriteData.getFunction().getType() == 1 || this.mSpriteData.getFunction().getType() == 7)) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(sCellHeight, 1073741824);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void notifyGadget(int state) {
        if (this.mContent instanceof Gadget) {
            Gadget gadget = this.mContent;
            switch (state) {
                case 1:
                    gadget.onStart();
                    return;
                case 2:
                    gadget.onStop();
                    return;
                case 3:
                    gadget.onPause();
                    return;
                case 4:
                    gadget.onResume();
                    return;
                case 5:
                    gadget.onCreate();
                    return;
                case 6:
                    gadget.onDestroy();
                    return;
                case 7:
                    gadget.onEditDisable();
                    return;
                case 8:
                    gadget.onEditNormal();
                    return;
                case 15:
                    if (gadget instanceof AwesomeGadget) {
                        ((AwesomeGadget) gadget).reinit();
                        return;
                    }
                    return;
                case 16:
                    if (gadget instanceof AwesomeGadget) {
                        ((AwesomeGadget) gadget).cleanUpAndKeepResource();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void gotoEditMode() {
        if (isEditable()) {
            setClickable(isEditable());
            setState(2);
            refreshGadgetEditMode();
            invalidate();
        }
    }

    public void exitEditMode() {
        if (isEditable()) {
            setClickable(false);
            setState(1);
            destroyHighlight();
            refreshGadgetEditMode();
            invalidate();
        }
    }

    public void gotoMoveMode() {
        setClickable(isEditable());
    }

    public void exitMoveMode() {
        setClickable(false);
        destroyHighlight();
    }

    public boolean isEditable() {
        return this.mSpriteData.getAppearance().getType() != 1;
    }

    public boolean isMovable() {
        return isEditable();
    }

    private void refreshGadgetEditMode() {
        if (this.mContent instanceof Gadget) {
            Gadget gadget = this.mContent;
            if (this.mSceneScreen.isInEditMode()) {
                gadget.onEditNormal();
            } else {
                gadget.onEditDisable();
            }
        }
    }

    private static View createFreeButton(Sprite sprite, SceneScreen sceneScreen, Context context) {
        FreeButtonAppearance appearance = (FreeButtonAppearance) sprite.getAppearance();
        if (appearance.getFreeButtonInfo() == null) {
            return null;
        }
        if (!appearance.getFreeButtonInfo().isMamlGadget()) {
            return new FreeButton(context, sprite, sceneScreen);
        }
        View freeGadget = new FreeButtonAwesomeGadget(context, appearance.getFreeButtonInfo().getMamlContext(), sprite, sceneScreen);
        if (!appearance.getFreeButtonInfo().getFile().exists()) {
            return null;
        }
        freeGadget.onAdded();
        freeGadget.onCreate();
        freeGadget.onStart();
        return freeGadget;
    }

    private static View createShortcut(AppFunction appFunc, SceneScreen sceneScreen, Context context) {
        ShortcutInfo shortcutInfo = new ShortcutInfo();
        shortcutInfo.setActivity(appFunc.getComponentName(), 270532608, Process.myUserHandle());
        PackageManager pm = context.getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(shortcutInfo.intent, 0);
        if (resolveInfo == null) {
            return null;
        }
        shortcutInfo.setTitle(resolveInfo.loadLabel(pm), context);
        ShortcutIcon shortcutView = ShortcutIcon.fromXml(R.layout.application, sceneScreen.getLauncher(), null, shortcutInfo);
        shortcutView.setOnClickListener(sceneScreen.getLauncher());
        shortcutView.setIconTitleVisible(appFunc.isShowIcon(), appFunc.isShowTitle());
        return shortcutView;
    }

    private static View createChildrenExit(Context context, Sprite sprite, SceneScreen sceneScreen) {
        FrameLayout content = (FrameLayout) LayoutInflater.from(sceneScreen.getLauncher()).inflate(R.layout.children_exit_btn, null, false);
        ((FreeButton) content.findViewById(R.id.btn_view)).init(context, sprite, sceneScreen);
        ((TextView) content.findViewById(R.id.title)).setText(R.string.children_mode_exit);
        return content;
    }

    private static View createToggle(ToggleFunction function, SceneScreen sceneScreen, Context context) {
        if (function.getToggleId() <= 0) {
            return null;
        }
        ShortcutIcon shortcutView = ShortcutIcon.fromXml(R.layout.application, sceneScreen.getLauncher(), null, function.getShortcutInfo());
        shortcutView.setOnClickListener(sceneScreen.getLauncher());
        shortcutView.setIconTitleVisible(function.isShowIcon(), function.isShowTitle());
        return shortcutView;
    }

    private static View createFolder(Sprite sprite, FolderFunction folderFunc, final SceneScreen sceneScreen, Context context) {
        int type = folderFunc.getType();
        PackageManager pm = context.getPackageManager();
        FolderInfo info = new UpsideFolderInfo(sprite, sceneScreen);
        info.setTitle(folderFunc.getFolderName(), context);
        if (TextUtils.isEmpty(info.getTitle(context))) {
            info.setTitle(sceneScreen.getContext().getString(type == 2 ? R.string.folder_name : R.string.children_folder_title), context);
        }
        for (ComponentName componentName : folderFunc.getComponentNames(context)) {
            Intent shortcutIntent = new Intent();
            shortcutIntent.setComponent(componentName);
            ResolveInfo resolveInfo = pm.resolveActivity(shortcutIntent, 0);
            if (resolveInfo != null) {
                info.add(new ShortcutInfo(context, resolveInfo, Process.myUserHandle()));
            }
        }
        FolderIcon folderIcon = FolderIcon.fromXml(R.layout.folder_icon, sceneScreen.getLauncher(), null, info);
        folderIcon.setTag(info);
        folderIcon.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sceneScreen.getLauncher().openFolder((FolderInfo) v.getTag(), (FolderIcon) v);
            }
        });
        return folderIcon;
    }

    private static View createWidget(WidgetFunction widgetFunc, SceneScreen sceneScreen, Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (widgetFunc.getId() <= 0) {
            try {
                int widgetId = sceneScreen.getAppWidgetHost().allocateAppWidgetId();
                appWidgetManager.bindAppWidgetId(widgetId, widgetFunc.getProviderComponent());
                widgetFunc.setId(widgetId);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(widgetFunc.getId());
        AppWidgetHostView hostView = sceneScreen.getAppWidgetHost().createView(context, widgetFunc.getId(), widgetInfo);
        hostView.setAppWidget(widgetFunc.getId(), widgetInfo);
        return hostView;
    }

    private static View createSystemGadget(SystemGadgetFunction sysGadgetFunc, SceneScreen sceneScreen, Context context) {
        GadgetInfo gadgetInfo = GadgetFactory.getInfo(sysGadgetFunc.getGadgetId());
        if (gadgetInfo == null) {
            return null;
        }
        gadgetInfo.id = (long) sysGadgetFunc.getId();
        View gadget = GadgetFactory.createGadget(sceneScreen.getLauncher(), gadgetInfo, 101);
        gadget.setTag(gadgetInfo);
        gadget.onAdded();
        gadget.onCreate();
        String resourcePath = sysGadgetFunc.getResourcePath();
        if (!TextUtils.isEmpty(resourcePath)) {
            Bundle b = new Bundle();
            b.putString("RESPONSE_PICKED_RESOURCE", resourcePath);
            gadget.updateConfig(b);
        }
        gadget.onStart();
        return gadget;
    }

    private static View createMtzGadget(MtzGadgetFunction mtzGadgetFunc, Context context) {
        try {
            if (!Factory.create(FreeStyleSerializer.DATA_PATH, mtzGadgetFunc.getMtzRelativePath()).exists()) {
                return null;
            }
            View awesomeView = new AwesomeGadget(context);
            awesomeView.initConfig(FreeStyleSerializer.DATA_PATH, mtzGadgetFunc.getMtzRelativePath());
            awesomeView.onAdded();
            awesomeView.onCreate();
            awesomeView.onStart();
            return awesomeView;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEditable()) {
            return false;
        }
        if (this.mSceneScreen.isInEditMode()) {
            if (this.mSpriteData.getFunction().getType() == 4 && this.mSceneScreen.getEditFocusedSprite() == this) {
                return false;
            }
            return true;
        } else if (this.mSceneScreen.isInMoveMode()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean performClick() {
        if (!this.mSceneScreen.isInEditMode() || !isEditable() || this.mSceneScreen.getEditFocusedSprite() == this) {
            return super.performClick();
        }
        this.mSceneScreen.setEditFocusedSprite(this);
        return true;
    }

    public void updateGadgetConfig(Bundle extras) {
        if (this.mSpriteData.getFunction().getType() == 4) {
            ((Gadget) this.mContent).updateConfig(extras);
            ((SystemGadgetFunction) this.mSpriteData.getFunction()).setResourcePath(extras.getString("RESPONSE_PICKED_RESOURCE"));
        }
    }

    public static View createContentView(Sprite sprite, SceneScreen sceneScreen, Context context) {
        View content;
        switch (sprite.getAppearance().getType()) {
            case 0:
                switch (sprite.getFunction().getType()) {
                    case 0:
                        return null;
                    case 1:
                        content = createShortcut((AppFunction) sprite.getFunction(), sceneScreen, context);
                        break;
                    case 2:
                    case 9:
                        content = createFolder(sprite, (FolderFunction) sprite.getFunction(), sceneScreen, context);
                        break;
                    case 3:
                        content = new FreeButton(context, sprite, sceneScreen);
                        break;
                    case 4:
                        content = createSystemGadget((SystemGadgetFunction) sprite.getFunction(), sceneScreen, context);
                        break;
                    case 5:
                        content = createWidget((WidgetFunction) sprite.getFunction(), sceneScreen, context);
                        break;
                    case 6:
                        content = createMtzGadget((MtzGadgetFunction) sprite.getFunction(), context);
                        break;
                    case 7:
                        content = createToggle((ToggleFunction) sprite.getFunction(), sceneScreen, context);
                        break;
                    case 8:
                        content = createChildrenExit(context, sprite, sceneScreen);
                        break;
                    default:
                        throw new RuntimeException("unknown function type:" + sprite.getFunction().getType());
                }
            case 1:
                Bitmap bitmap = ((StageImageAppearance) sprite.getAppearance()).getBitmap();
                if (bitmap != null) {
                    content = new ImageView(context);
                    BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                    drawable.setFilterBitmap(true);
                    drawable.setAntiAlias(true);
                    ((ImageView) content).setImageDrawable(drawable);
                    ((ImageView) content).setScaleType(ScaleType.FIT_XY);
                    break;
                }
                return null;
            case 2:
                content = createFreeButton(sprite, sceneScreen, context);
                break;
            default:
                throw new RuntimeException("unknown appearance type:" + sprite.getAppearance().getType());
        }
        return content;
    }

    public void rebuildContentView() {
        setContentView(createContentView(this.mSpriteData, this.mSceneScreen, this.mContext));
    }

    private void setContentView(View contentView) {
        this.mContent = contentView;
        removeAllViews();
        addView(this.mContent, -1, -1);
        if (this.mSpriteData.getRotation() != 0.0f) {
            setRotation(this.mSpriteData.getRotation());
        }
        if (this.mSpriteData.getScaleX() != 0.0f) {
            setScaleX(this.mSpriteData.getScaleX());
        }
        if (this.mSpriteData.getScaleY() != 0.0f) {
            setScaleY(this.mSpriteData.getScaleY());
        }
        if (this.mContent instanceof Gadget) {
            ((Gadget) this.mContent).onResume();
        }
        refreshGadgetEditMode();
    }

    public void moveTo(int left, int top) {
        this.mSpriteData.setLocation(left, top);
        LayoutParams lp = (LayoutParams) getLayoutParams();
        lp.left = left;
        lp.top = top;
        getParent().requestLayout();
    }

    private void enterHighlight() {
        if (!isInHighlight() && !Launcher.isChildrenModeEnabled()) {
            this.mIsHighlight = true;
            if (sHighlightAllAlphaAnimator == null) {
                sHighlightAllAlphaAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 4.0f});
                sHighlightAllAlphaAnimator.setDuration(600);
                sHighlightAllAlphaAnimator.setInterpolator(new LinearInterpolator());
                sHighlightAllAlphaAnimator.setRepeatCount(-1);
                sHighlightAllAlphaAnimator.setRepeatMode(1);
                sHighlightAllAlphaAnimator.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        SpriteView.sHighlightCurrentValue = ((Float) animation.getAnimatedValue()).floatValue();
                    }
                });
                sHighlightAllAlphaAnimator.start();
            }
            sHighlightAllAlphaAnimator.addUpdateListener(this);
        }
    }

    public void onAnimationUpdate(ValueAnimator animation) {
        float value = (this.mHighlightOffsetRandom + sHighlightCurrentValue) % 4.0f;
        if (value > 1.0f) {
            if (value <= 2.0f) {
                value = 2.0f - value;
            } else if (value <= 3.0f) {
                value = 2.0f - value;
            } else if (value <= 4.0f) {
                value -= 4.0f;
            }
        }
        float needBiger = (BIGGER_OFFSET * ((-value) + 1.0f)) / 2.0f;
        float scaleX = (((float) getWidth()) + needBiger) / ((float) getWidth());
        float scaleY = (((float) getHeight()) + needBiger) / ((float) getHeight());
        setTranslationY(((float) SHAKE_OFFSET) * value);
        setScaleX(scaleX);
        setScaleY(scaleY);
    }

    private void exitHighlight() {
        if (isInHighlight()) {
            if (sHighlightAllAlphaAnimator != null) {
                sHighlightAllAlphaAnimator.removeUpdateListener(this);
            }
            setAlpha(1.0f);
            setTranslationY(0.0f);
            setScaleX(1.0f);
            setScaleY(1.0f);
            this.mIsHighlight = false;
        }
    }

    private boolean isInHighlight() {
        return this.mIsHighlight;
    }

    private void enterTranslucent() {
        this.mContent.setAlpha(0.3f);
    }

    private void exitTranslucent() {
        this.mContent.setAlpha(1.0f);
    }

    private void destroyHighlight() {
        if (sHighlightAllAlphaAnimator != null) {
            sHighlightAllAlphaAnimator.removeAllUpdateListeners();
            sHighlightAllAlphaAnimator = null;
        }
    }

    public void setState(int state) {
        exitHighlight();
        exitTranslucent();
        switch (state) {
            case 2:
                if (isEditable()) {
                    enterHighlight();
                    return;
                }
                return;
            case 4:
                if (isEditable()) {
                    enterTranslucent();
                    return;
                }
                return;
            case 5:
                if (isMovable()) {
                    enterHighlight();
                    return;
                }
                return;
            case 6:
                if (isMovable()) {
                    enterHighlight();
                    return;
                }
                return;
            default:
                return;
        }
    }
}
