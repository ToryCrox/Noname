package com.miui.home.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import java.util.ArrayList;
import java.util.Iterator;

public class ScreenCellsThumbnailView extends ThumbnailView implements OnClickListener {
    private float mCameraDistanceCache;
    private Context mContext;
    private LayoutInflater mInflater;
    private String mLastScreenCells;
    private Launcher mLauncher;

    public ScreenCellsThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScreenCellsThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mCameraDistanceCache = 0.0f;
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        initThumbnailView();
        setOnClickListener(this);
    }

    private void initThumbnailView() {
        setScreenTransitionType(10);
        setScreenLayoutMode(1);
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    public void reLoadThumbnails() {
        removeAllScreens();
        ArrayList<CharSequence> screenCellsOptions = ScreenUtils.getScreenCellsSizeOptions(this.mLauncher);
        if (screenCellsOptions.size() != 0) {
            int[] cells = new int[2];
            Iterator i$ = screenCellsOptions.iterator();
            while (i$.hasNext()) {
                if (ScreenUtils.parseCellsSize(((CharSequence) i$.next()).toString(), cells)) {
                    addCellsThumbnail(cells[0], cells[1]);
                }
            }
            setCurrentScreen(0);
        }
    }

    private void addCellsThumbnail(int cellX, int cellY) {
        View thumbnail = getView(cellX, cellY, null);
        WallpaperUtils.onAddViewToGroup(this, thumbnail, true);
        addView(thumbnail);
        thumbnail.setOnClickListener(this);
    }

    protected void adaptThumbnailItemStyle() {
        for (int i = 0; i < getScreenCount(); i++) {
            View v = getScreen(i);
            setupDrawableRes(v, (ImageView) v.findViewById(R.id.icon));
        }
    }

    public View getView(int cellX, int cellY, View convertView) {
        AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) this.mInflater.inflate(R.layout.thumbnail_item, null);
        String cellType = cellX + "x" + cellY;
        resultView.setTag(cellType);
        TextView title = (TextView) resultView.findViewById(R.id.title);
        title.setText(cellType);
        if (cellX == DeviceConfig.getCellCountX() && cellY == DeviceConfig.getCellCountY()) {
            setCurrentSelected(title);
            this.mLastScreenCells = cellType;
        }
        ImageView icon = (ImageView) resultView.findViewById(R.id.icon);
        LayoutParams lp = icon.getLayoutParams();
        lp.width = -2;
        lp.height = -2;
        setupDrawableRes(resultView, icon);
        return resultView;
    }

    private void setupDrawableRes(View thumbnail, ImageView icon) {
        ImageView background = (ImageView) thumbnail.findViewById(R.id.background);
        if (background != null) {
            background.setImageDrawable(this.mContext.getResources().getDrawable(ThumbnailViewAdapter.THUMBNAIL_BACKGROUND[ThumbnailView.CURR_ICON_DRAWABLE_INDEX]));
            background.getDrawable().mutate();
        }
        int resId = this.mLauncher.getResources().getIdentifier("cell_" + thumbnail.getTag().toString(), "drawable", this.mLauncher.getPackageName());
        if (resId == 0) {
            resId = R.drawable.thumbnail_entry_screen_cells;
        }
        icon.setImageDrawable(this.mContext.getResources().getDrawable(resId));
    }

    public void onClick(View v) {
        if (isShown() && !this.mLauncher.isPrivacyModeEnabled()) {
            int[] cells = new int[2];
            if (!ScreenUtils.parseCellsSize(v.getTag().toString(), cells)) {
                return;
            }
            if (cells[0] != DeviceConfig.getCellCountX() || cells[1] != DeviceConfig.getCellCountY()) {
                TextView selected = (TextView) v.findViewById(R.id.title);
                selected.setVisibility(0);
                setCurrentSelected(selected);
                DeviceConfig.setScreenCells(this.mLauncher, cells[0], cells[1]);
                this.mLauncher.onScreenCellsChanged();
            }
        }
    }

    public void confirmCellsSize() {
        DeviceConfig.confirmCellsCount(this.mLauncher);
        if (this.mCurrentSelected != null) {
            String currentCellsSize = this.mCurrentSelected.getText().toString();
            if (this.mLastScreenCells != null && !this.mLastScreenCells.equals(currentCellsSize)) {
                AnalyticalDataCollector.trackScreenCellsSizeChanged(currentCellsSize);
            }
        }
    }

    public void setCameraDistance(float distance) {
        if (distance != this.mCameraDistanceCache) {
            this.mCameraDistanceCache = distance;
            super.setCameraDistance(this.mCameraDistanceCache);
        }
    }
}
