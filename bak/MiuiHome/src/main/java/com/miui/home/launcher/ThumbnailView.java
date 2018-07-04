package com.miui.home.launcher;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils.WallpaperColorChangedListener;
import java.util.ArrayList;

public class ThumbnailView extends LinkedScreenView implements WallpaperColorChangedListener {
    protected static int CURR_ICON_DRAWABLE_INDEX = 0;
    protected ThumbnailViewAdapter mAdapter;
    protected ThumbnailViewAdapterObserver mAdapterObserver;
    protected int mAnimationDuration;
    protected boolean mChangeAlongWallpaper;
    protected TextView mCurrentSelected;
    private boolean mShowing;

    private class ThumbnailViewAdapterObserver extends DataSetObserver {
        private ThumbnailViewAdapterObserver() {
        }

        public void onChanged() {
            ThumbnailView.this.reLoadThumbnails();
            ThumbnailView.this.requestLayout();
        }

        public void onInvalidated() {
            onChanged();
        }
    }

    public ThumbnailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mAdapterObserver = new ThumbnailViewAdapterObserver();
        this.mShowing = false;
        this.mChangeAlongWallpaper = true;
        this.mAnimationDuration = context.getResources().getInteger(17694722);
    }

    public void setAdapter(ThumbnailViewAdapter adapter) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mAdapterObserver);
        }
        this.mAdapter = adapter;
        if (hasAdapter()) {
            this.mAdapter.registerDataSetObserver(this.mAdapterObserver);
            this.mAdapter.refreshList();
        }
        this.mAdapterObserver.onInvalidated();
    }

    public boolean hasAdapter() {
        return this.mAdapter != null;
    }

    public void setOnLongClickListener(OnLongClickListener l) {
        this.mLongClickListener = l;
        for (int screenIndex = getScreenCount() - 1; screenIndex >= 0; screenIndex--) {
            getScreen(screenIndex).setOnLongClickListener(l);
        }
    }

    public void setOnClickListener(OnClickListener l) {
        this.mClickListener = l;
        for (int screenIndex = getScreenCount() - 1; screenIndex >= 0; screenIndex--) {
            getScreen(screenIndex).setOnClickListener(l);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void removeAllScreens() {
        if (this.mAdapter == null || !this.mAdapter.isRefreshing()) {
            for (int i = 0; i < getScreenCount(); i++) {
                clearThumbnail(getScreen(i));
            }
        }
        super.removeAllScreens();
        this.mCurrentScreen = -1;
    }

    protected void clearThumbnail(View resultView) {
        resultView.setTag(null);
        ((ImageView) resultView.findViewById(R.id.icon)).setImageDrawable(null);
        TextView title = (TextView) resultView.findViewById(R.id.title);
        title.setVisibility(4);
        title.setAlpha(1.0f);
        title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        TextView contentTitle = (TextView) resultView.findViewById(R.id.content_title);
        contentTitle.setVisibility(4);
        contentTitle.setAlpha(1.0f);
        ((ImageView) resultView.findViewById(R.id.background)).setImageDrawable(null);
        ((ImageView) resultView.findViewById(R.id.foreground)).setImageDrawable(null);
        resultView.setAlpha(1.0f);
    }

    public static void adaptIconDrawableIndex() {
        CURR_ICON_DRAWABLE_INDEX = WallpaperUtils.hasAppliedLightWallpaper() ? 1 : 0;
    }

    public void onWallpaperColorChanged() {
        WallpaperUtils.varyViewGroupByWallpaper(this);
        adaptThumbnailItemStyle();
    }

    protected void adaptThumbnailItemStyle() {
        if (this.mAdapter != null) {
            for (int i = 0; i < getScreenCount(); i++) {
                this.mAdapter.adaptIconStyle(getScreen(i));
            }
        }
    }

    protected void reLoadThumbnails() {
        int i;
        View[] children = new View[0];
        if (getScreenCount() > 0) {
            children = new View[getScreenCount()];
            for (i = 0; i < children.length; i++) {
                children[i] = getScreen(i);
            }
        }
        removeAllScreens();
        if (hasAdapter()) {
            int count = this.mAdapter.getCount();
            ArrayList<View> allViewList = new ArrayList();
            i = 0;
            while (i < count) {
                View thumbnail = this.mAdapter.getView(i, i < children.length ? children[i] : null, this);
                if (thumbnail != null) {
                    WallpaperUtils.onAddViewToGroup(this, thumbnail, this.mChangeAlongWallpaper);
                    allViewList.add(thumbnail);
                    if (this.mClickListener != null) {
                        thumbnail.setOnClickListener(this.mClickListener);
                    }
                    if (this.mLongClickListener != null) {
                        thumbnail.setOnLongClickListener(this.mLongClickListener);
                    }
                }
                i++;
            }
            reorderAndAddAllViews(allViewList);
        }
    }

    protected void reorderAndAddAllViews(ArrayList<View> allViews) {
        for (int i = 0; i < allViews.size(); i++) {
            addView((View) allViews.get(i));
        }
    }

    protected void setCurrentSelected(TextView currentSelected) {
        if (this.mCurrentSelected != null) {
            this.mCurrentSelected.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        this.mCurrentSelected = currentSelected;
        if (this.mCurrentSelected != null) {
            this.mCurrentSelected.setVisibility(0);
            this.mCurrentSelected.setCompoundDrawablePadding(getContext().getResources().getDimensionPixelSize(R.dimen.thumbnail_view_selected_dot_padding));
            if (DeviceConfig.isLayoutRtl()) {
                this.mCurrentSelected.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.editing_mode_be_selected, 0);
            } else {
                this.mCurrentSelected.setCompoundDrawablesWithIntrinsicBounds(R.drawable.editing_mode_be_selected, 0, 0, 0);
            }
        }
    }
}
