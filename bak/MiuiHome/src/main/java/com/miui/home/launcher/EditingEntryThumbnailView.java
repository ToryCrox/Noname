package com.miui.home.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import java.util.ArrayList;
import miui.theme.ThemeManagerHelper;

public class EditingEntryThumbnailView extends ThumbnailView implements OnClickListener {
    private static final int[] ICON_BACKGROUND = new int[]{R.drawable.thumbnail_entry_background, R.drawable.thumbnail_entry_background_dark};
    private static String[] mEditingModeValues;
    private static String[] mEditingModes;
    private static ComponentName mThemePickCN = new ComponentName("com.android.thememanager", "com.android.thememanager.activity.ThemeSettingsActivity");
    private Context mContext;
    private int[] mEntryDrawableIds;
    private ArrayList<Integer> mEntryList;
    private LayoutInflater mInflater;
    private Launcher mLauncher;

    public EditingEntryThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditingEntryThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mEntryDrawableIds = null;
        this.mEntryList = new ArrayList();
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        if (!ThemeManagerHelper.needDisableTheme(context)) {
            this.mEntryList.add(Integer.valueOf(0));
        }
        this.mEntryList.add(Integer.valueOf(1));
        this.mEntryList.add(Integer.valueOf(2));
        this.mEntryList.add(Integer.valueOf(3));
        if (DeviceConfig.needShowCellsEntry(context)) {
            this.mEntryList.add(Integer.valueOf(4));
        }
        initIconDrawableIds(context);
        initWidgetThumbnailView();
    }

    private void initIconDrawableIds(Context context) {
        mEditingModes = this.mContext.getResources().getStringArray(R.array.editing_mode_entries);
        this.mEntryDrawableIds = new int[mEditingModes.length];
        this.mEntryDrawableIds[0] = R.drawable.thumbnail_entry_theme;
        this.mEntryDrawableIds[1] = R.drawable.thumbnail_entry_add_widget;
        this.mEntryDrawableIds[2] = R.drawable.thumbnail_entry_set_wallpaper;
        this.mEntryDrawableIds[3] = R.drawable.thumbnail_entry_transition_effect;
        this.mEntryDrawableIds[4] = R.drawable.thumbnail_entry_screen_cells;
    }

    private void initWidgetThumbnailView() {
        setScreenTransitionType(10);
        setScrollWholeScreen(false);
        int width = DeviceConfig.getScreenWidth();
        int entryLayoutWidth = this.mContext.getResources().getDrawable(ICON_BACKGROUND[0]).getIntrinsicWidth();
        if (width >= this.mEntryList.size() * entryLayoutWidth) {
            setScreenLayoutMode(1);
            return;
        }
        setScreenLayoutMode(3);
        setFixedGap((int) Math.floor((double) (((float) (width - (this.mEntryList.size() * entryLayoutWidth))) / (((float) this.mEntryList.size()) - 1.0f))));
    }

    public void reLoadThumbnails() {
        removeAllViews();
        mEditingModes = this.mContext.getResources().getStringArray(R.array.editing_mode_entries);
        mEditingModeValues = this.mContext.getResources().getStringArray(R.array.editing_mode_values);
        for (int i = 0; i < this.mEntryList.size(); i++) {
            int entryIndex = ((Integer) this.mEntryList.get(i)).intValue();
            AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) this.mInflater.inflate(R.layout.thumbnail_item, this, false);
            resultView.setTag(Integer.valueOf(Integer.valueOf(mEditingModeValues[entryIndex]).intValue()));
            resultView.setOnClickListener(this);
            ThumbnailIcon icon = (ThumbnailIcon) resultView.findViewById(R.id.icon);
            icon.enableDrawMaskOnPressed(false);
            LayoutParams lp = icon.getLayoutParams();
            lp.width = -2;
            lp.height = -2;
            setIconDrawable(resultView);
            ((TextView) resultView.findViewById(R.id.title)).setText(mEditingModes[entryIndex]);
            WallpaperUtils.onAddViewToGroup(this, resultView, true);
            addView(resultView);
        }
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    private void setIconDrawable(View thumbnail) {
        ImageView background = (ImageView) thumbnail.findViewById(R.id.background);
        if (background != null) {
            background.setImageDrawable(this.mContext.getResources().getDrawable(ICON_BACKGROUND[ThumbnailView.CURR_ICON_DRAWABLE_INDEX]));
        }
        ((ImageView) thumbnail.findViewById(R.id.icon)).setImageDrawable(this.mContext.getResources().getDrawable(this.mEntryDrawableIds[((Integer) thumbnail.getTag()).intValue()]));
    }

    protected void adaptThumbnailItemStyle() {
        for (int i = 0; i < getScreenCount(); i++) {
            setIconDrawable(getScreen(i));
        }
    }

    public void onClick(View v) {
        if (isShown() && !this.mLauncher.isPrivacyModeEnabled()) {
            switch (((Integer) v.getTag()).intValue()) {
                case 0:
                    Intent intent = new Intent();
                    intent.setComponent(mThemePickCN);
                    intent.setFlags(268435456);
                    intent.putExtra("REQUEST_ENTRY_TYPE", "home");
                    this.mLauncher.startActivity(intent);
                    AnalyticalDataCollector.trackEditingEntryClicked("theme");
                    return;
                case 1:
                    this.mLauncher.setEditingState(11);
                    AnalyticalDataCollector.trackEditingEntryClicked("widget_pick");
                    return;
                case 2:
                    this.mLauncher.setEditingState(12);
                    AnalyticalDataCollector.trackEditingEntryClicked("wallpaper_settings");
                    return;
                case 3:
                    this.mLauncher.setEditingState(13);
                    AnalyticalDataCollector.trackEditingEntryClicked("transition_settings");
                    return;
                case 4:
                    this.mLauncher.setEditingState(14);
                    AnalyticalDataCollector.trackEditingEntryClicked("screen_cells_settings");
                    return;
                default:
                    return;
            }
        }
    }
}
