package com.miui.home.launcher.upsidescene;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.LauncherAppWidgetProviderInfo;
import com.miui.home.launcher.ShortcutPlaceholderProviderInfo;
import com.miui.home.launcher.gadget.GadgetFactory;
import com.miui.home.launcher.gadget.GadgetInfo;
import com.miui.home.launcher.upsidescene.BottomItemsDragSource.ItemData;
import com.miui.home.launcher.upsidescene.data.FreeButtonInfo;
import com.miui.home.launcher.upsidescene.data.FreeStyle.MtzGadgetInfo;
import com.miui.home.launcher.upsidescene.data.Function;
import com.miui.home.launcher.upsidescene.data.Sprite;
import java.util.ArrayList;
import java.util.List;
import miui.widget.ScreenView;

public class EditModeBottomBar extends FrameLayout {
    private BottomItemsDragSource mBottomItemsDragSourceFreeButton;
    private BottomItemsDragSource mBottomItemsDragSourceShortcut;
    private BottomItemsDragSource mBottomItemsDragSourceTool;
    private View mCkbFreeButtonApp;
    private View mCkbFreeButtonDrawer;
    private View mCkbFreeButtonEmpty;
    private View mCkbFreeButtonFolder;
    private View mCkbFreeButtonToggle;
    private ViewGroup mEditDragSource;
    private TextView mEditModeDraggingTip;
    private ScreenView mEditModeFreeButton;
    private TextView mEditModeMoveTip;
    private boolean mIsBuilded;
    private SceneScreen mSceneScreen;
    private View mTabLabelFreeButton;
    private View mTabLabelShortcut;
    private View mTabLabelWidget;

    public EditModeBottomBar(Context context) {
        this(context, null);
    }

    public EditModeBottomBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditModeBottomBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getLayoutParams().height = calcHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mEditModeMoveTip = (TextView) findViewById(R.id.editModeMoveTip);
        this.mEditModeDraggingTip = (TextView) findViewById(R.id.editModeDraggingTip);
        this.mEditModeFreeButton = (ScreenView) findViewById(R.id.editModeFreeButton);
        this.mEditDragSource = (ViewGroup) findViewById(R.id.editDragSource);
        this.mTabLabelShortcut = findViewById(R.id.tabLabelShortcut);
        this.mTabLabelFreeButton = findViewById(R.id.tabLabelFreeButton);
        this.mTabLabelWidget = findViewById(R.id.tabLabelWidget);
        View editModeFreeButtonEmpty = findViewById(R.id.editModeFreeButtonEmpty);
        View editModeFreeButtonApp = findViewById(R.id.editModeFreeButtonApp);
        View editModeFreeButtonFolder = findViewById(R.id.editModeFreeButtonFolder);
        View editModeFreeButtonDrawer = findViewById(R.id.editModeFreeButtonDrawer);
        View editModeFreeButtonToggle = findViewById(R.id.editModeFreeButtonToggle);
        TextView labelFreeButtonApp = (TextView) editModeFreeButtonApp.findViewById(R.id.label);
        TextView labelFreeButtonFolder = (TextView) editModeFreeButtonFolder.findViewById(R.id.label);
        TextView labelFreeButtonDrawer = (TextView) editModeFreeButtonDrawer.findViewById(R.id.label);
        TextView labelFreeButtonToggle = (TextView) editModeFreeButtonToggle.findViewById(R.id.label);
        ((TextView) editModeFreeButtonEmpty.findViewById(R.id.label)).setText(R.string.free_style_edit_bar_empty);
        labelFreeButtonApp.setText(R.string.free_style_edit_app);
        labelFreeButtonFolder.setText(R.string.folder_name);
        labelFreeButtonDrawer.setText(R.string.free_style_edit_drawer);
        labelFreeButtonToggle.setText(R.string.toggle_title);
        ImageView imageFreeButtonApp = (ImageView) editModeFreeButtonApp.findViewById(R.id.image);
        ImageView imageFreeButtonFolder = (ImageView) editModeFreeButtonFolder.findViewById(R.id.image);
        ImageView imageFreeButtonDrawer = (ImageView) editModeFreeButtonDrawer.findViewById(R.id.image);
        ImageView imageFreeButtonToggle = (ImageView) editModeFreeButtonToggle.findViewById(R.id.image);
        ((ImageView) editModeFreeButtonEmpty.findViewById(R.id.image)).setImageResource(R.drawable.free_style_edit_nothing_icon);
        imageFreeButtonApp.setImageResource(R.drawable.free_style_icon_app);
        imageFreeButtonFolder.setImageResource(R.drawable.free_style_icon_folder);
        imageFreeButtonDrawer.setImageResource(R.drawable.free_style_icon_drawer);
        imageFreeButtonToggle.setImageResource(R.drawable.gadget_toggle_icon);
        this.mCkbFreeButtonEmpty = editModeFreeButtonEmpty.findViewById(R.id.checkbox);
        this.mCkbFreeButtonApp = editModeFreeButtonApp.findViewById(R.id.checkbox);
        this.mCkbFreeButtonFolder = editModeFreeButtonFolder.findViewById(R.id.checkbox);
        this.mCkbFreeButtonDrawer = editModeFreeButtonDrawer.findViewById(R.id.checkbox);
        this.mCkbFreeButtonToggle = editModeFreeButtonToggle.findViewById(R.id.checkbox);
        this.mEditModeFreeButton.setScrollWholeScreen(true);
        this.mEditModeFreeButton.setSlideBarPosition(new LayoutParams(-1, -2, 83), R.drawable.free_style_edit_bottom_bar_slide_bar, R.drawable.free_style_edit_bottom_bar_slide_bar_bg, true);
        for (int i = this.mEditModeFreeButton.getScreenCount() - 1; i >= 0; i--) {
            this.mEditModeFreeButton.getScreen(i).getLayoutParams().width = getResources().getDisplayMetrics().widthPixels / 3;
        }
        editModeFreeButtonApp.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.mSceneScreen.showSelectApps(false);
            }
        });
        editModeFreeButtonFolder.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.mSceneScreen.showSelectApps(true);
            }
        });
        editModeFreeButtonEmpty.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.mSceneScreen.getEditFocusedSprite().getSpriteData().setFunction(Function.createFunction(0));
                EditModeBottomBar.this.refreshFreeButtonCheckbox();
            }
        });
        editModeFreeButtonDrawer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.mSceneScreen.getEditFocusedSprite().getSpriteData().setFunction(Function.createFunction(3));
                EditModeBottomBar.this.refreshFreeButtonCheckbox();
            }
        });
        editModeFreeButtonToggle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.mSceneScreen.getLauncher().showTogglesSelectView();
            }
        });
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(View v) {
                EditModeBottomBar.this.selectTab(v);
            }
        };
        this.mTabLabelShortcut.setOnClickListener(clickListener);
        this.mTabLabelFreeButton.setOnClickListener(clickListener);
        this.mTabLabelWidget.setOnClickListener(clickListener);
    }

    private void selectTab(View selectedView) {
        if (selectedView == this.mTabLabelShortcut) {
            this.mTabLabelShortcut.setSelected(true);
            this.mTabLabelFreeButton.setSelected(false);
            this.mTabLabelWidget.setSelected(false);
            this.mBottomItemsDragSourceShortcut.setVisibility(0);
            this.mBottomItemsDragSourceFreeButton.setVisibility(4);
            this.mBottomItemsDragSourceTool.setVisibility(4);
        } else if (selectedView == this.mTabLabelFreeButton) {
            this.mTabLabelShortcut.setSelected(false);
            this.mTabLabelFreeButton.setSelected(true);
            this.mTabLabelWidget.setSelected(false);
            this.mBottomItemsDragSourceShortcut.setVisibility(4);
            this.mBottomItemsDragSourceFreeButton.setVisibility(0);
            this.mBottomItemsDragSourceTool.setVisibility(4);
        } else if (selectedView == this.mTabLabelWidget) {
            this.mTabLabelShortcut.setSelected(false);
            this.mTabLabelFreeButton.setSelected(false);
            this.mTabLabelWidget.setSelected(true);
            this.mBottomItemsDragSourceShortcut.setVisibility(4);
            this.mBottomItemsDragSourceFreeButton.setVisibility(4);
            this.mBottomItemsDragSourceTool.setVisibility(0);
        }
    }

    public void setSceneScreen(SceneScreen sceneScreen) {
        this.mSceneScreen = sceneScreen;
    }

    public int calcHeight() {
        return getBackground().getIntrinsicHeight();
    }

    public void switchEditWidgetBar() {
        boolean isFreeButton = true;
        if (!this.mIsBuilded) {
            buildBottomWidgetDragSources();
            this.mIsBuilded = true;
            selectTab(this.mTabLabelShortcut);
        }
        if (this.mSceneScreen.isDragging()) {
            this.mEditModeDraggingTip.setVisibility(0);
            this.mEditModeMoveTip.setVisibility(4);
            this.mEditModeFreeButton.setVisibility(4);
            this.mEditDragSource.setVisibility(4);
            return;
        }
        this.mEditModeDraggingTip.setVisibility(4);
        if (this.mSceneScreen.getEditFocusedSprite() == null) {
            this.mEditModeMoveTip.setVisibility(4);
            this.mEditModeFreeButton.setVisibility(4);
            this.mEditDragSource.setVisibility(0);
            return;
        }
        this.mEditDragSource.setVisibility(4);
        if (this.mSceneScreen.getEditFocusedSprite().getSpriteData().getAppearance().getType() != 2) {
            isFreeButton = false;
        }
        if (isFreeButton) {
            this.mEditModeMoveTip.setVisibility(4);
            this.mEditModeFreeButton.setVisibility(0);
            refreshFreeButtonCheckbox();
            return;
        }
        this.mEditModeMoveTip.setVisibility(0);
        this.mEditModeFreeButton.setVisibility(4);
    }

    public void refreshFreeButtonCheckbox() {
        if (this.mSceneScreen.getEditFocusedSprite() != null && this.mSceneScreen.getEditFocusedSprite().getSpriteData().getAppearance().getType() == 2) {
            this.mCkbFreeButtonEmpty.setSelected(false);
            this.mCkbFreeButtonApp.setSelected(false);
            this.mCkbFreeButtonToggle.setSelected(false);
            this.mCkbFreeButtonFolder.setSelected(false);
            this.mCkbFreeButtonDrawer.setSelected(false);
            Sprite sprite = this.mSceneScreen.getEditFocusedSprite().getSpriteData();
            switch (sprite.getFunction().getType()) {
                case 0:
                    this.mCkbFreeButtonEmpty.setSelected(true);
                    return;
                case 1:
                    this.mCkbFreeButtonApp.setSelected(true);
                    return;
                case 2:
                    this.mCkbFreeButtonFolder.setSelected(true);
                    return;
                case 3:
                    this.mCkbFreeButtonDrawer.setSelected(true);
                    return;
                case 7:
                    this.mCkbFreeButtonToggle.setSelected(true);
                    return;
                default:
                    throw new RuntimeException("unknown function type:" + sprite.getFunction().getType());
            }
        }
    }

    private void buildBottomWidgetDragSources() {
        FrameLayout dragSourceTabHost = (FrameLayout) findViewById(R.id.dragSourceTabHost);
        dragSourceTabHost.removeAllViews();
        List<ItemData> shortcutItemDatas = new ArrayList();
        shortcutItemDatas.add(new ItemData(getResources().getDrawable(R.drawable.free_style_icon_app), this.mContext.getString(R.string.free_style_edit_app), new ShortcutPlaceholderProviderInfo(1)));
        shortcutItemDatas.add(new ItemData(getResources().getDrawable(R.drawable.free_style_icon_folder), this.mContext.getString(R.string.folder_name), new ShortcutPlaceholderProviderInfo(2)));
        shortcutItemDatas.add(new ItemData(getResources().getDrawable(R.drawable.free_style_icon_drawer), this.mContext.getString(R.string.free_style_edit_drawer), new ShortcutPlaceholderProviderInfo(3)));
        shortcutItemDatas.add(new ItemData(getResources().getDrawable(R.drawable.gadget_toggle_icon), this.mContext.getString(R.string.toggle_title), new ShortcutPlaceholderProviderInfo(4)));
        this.mBottomItemsDragSourceShortcut = new BottomItemsDragSource(this.mContext, this.mSceneScreen, true);
        this.mBottomItemsDragSourceShortcut.setItemDatas(shortcutItemDatas);
        this.mBottomItemsDragSourceShortcut.setDragController(this.mSceneScreen.getLauncher().getDragController());
        dragSourceTabHost.addView(this.mBottomItemsDragSourceShortcut, -1, -1);
        List<ItemData> freeButtonItemDatas = new ArrayList();
        for (FreeButtonInfo freeButtonInfo : this.mSceneScreen.getFreeStyle().getFreeButtons()) {
            if (freeButtonInfo.getPreviewImage() != null) {
                freeButtonItemDatas.add(new ItemData(new BitmapDrawable(getResources(), freeButtonInfo.getPreviewImage()), "", freeButtonInfo));
            }
        }
        this.mBottomItemsDragSourceFreeButton = new BottomItemsDragSource(this.mContext, this.mSceneScreen, false);
        this.mBottomItemsDragSourceFreeButton.setItemDatas(freeButtonItemDatas);
        this.mBottomItemsDragSourceFreeButton.setVisibility(4);
        this.mBottomItemsDragSourceFreeButton.setDragController(this.mSceneScreen.getLauncher().getDragController());
        dragSourceTabHost.addView(this.mBottomItemsDragSourceFreeButton, -1, -1);
        List<ItemData> toolItemDatas = new ArrayList();
        for (MtzGadgetInfo gadgetInfo : this.mSceneScreen.getFreeStyle().getMtzGadgets()) {
            if (gadgetInfo.preview != null) {
                toolItemDatas.add(new ItemData(new BitmapDrawable(getResources(), gadgetInfo.preview), gadgetInfo.title, gadgetInfo));
            }
        }
        for (int gadgetId : GadgetFactory.getGadgetIdList(this.mContext)) {
            GadgetInfo info = GadgetFactory.getInfo(gadgetId);
            if (info != null && gadgetId < 1000) {
                toolItemDatas.add(new ItemData(info.getIcon(getContext()), info.getTitle(getContext()), info));
            }
        }
        PackageManager pm = this.mContext.getPackageManager();
        for (AppWidgetProviderInfo info2 : AppWidgetManager.getInstance(this.mContext).getInstalledProviders()) {
            Drawable drawable = pm.getDrawable(info2.provider.getPackageName(), info2.icon, null);
            LauncherAppWidgetProviderInfo providerInfo = new LauncherAppWidgetProviderInfo(info2);
            providerInfo.spanX = DeviceConfig.getWidgetSpanX(info2.minWidth);
            providerInfo.spanY = DeviceConfig.getWidgetSpanY(info2.minHeight);
            toolItemDatas.add(new ItemData(drawable, info2.label, providerInfo));
        }
        this.mBottomItemsDragSourceTool = new BottomItemsDragSource(this.mContext, this.mSceneScreen, true);
        this.mBottomItemsDragSourceTool.setItemDatas(toolItemDatas);
        this.mBottomItemsDragSourceTool.setVisibility(4);
        this.mBottomItemsDragSourceTool.setDragController(this.mSceneScreen.getLauncher().getDragController());
        dragSourceTabHost.addView(this.mBottomItemsDragSourceTool, -1, -1);
    }
}
