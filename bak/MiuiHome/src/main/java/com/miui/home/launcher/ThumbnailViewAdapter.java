package com.miui.home.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.miui.home.R;

public class ThumbnailViewAdapter extends BaseAdapter {
    public static final int[] THUMBNAIL_BACKGROUND = new int[]{R.drawable.thumbnail_item_bg, R.drawable.thumbnail_item_bg_dark};
    protected View[] mAllThumbnailViews;
    protected Context mContext;
    private LayoutInflater mInflater;
    protected boolean mIsRefreshing = false;
    protected Launcher mLauncher;
    private LoadingItemsWorker mLoadingItemsWorker = new LoadingItemsWorker();
    private ViewGroup mSourceGroup;

    private class LoadingItemsWorker implements Runnable {
        private int mCounter;
        private boolean mIsLoading;
        private int mItemsCount;

        private LoadingItemsWorker() {
            this.mIsLoading = false;
        }

        public void startLoading() {
            this.mItemsCount = ThumbnailViewAdapter.this.getCount();
            this.mCounter = 0;
            this.mIsLoading = true;
            ThumbnailViewAdapter.this.mLauncher.getWorkspace().post(this);
        }

        public void stopLoading() {
            if (ThumbnailViewAdapter.this.mLauncher != null) {
                ThumbnailViewAdapter.this.mLauncher.getWorkspace().removeCallbacks(this);
            }
            this.mIsLoading = false;
        }

        public void run() {
            if (this.mIsLoading) {
                ThumbnailViewAdapter.this.loadContent(this.mCounter);
                this.mCounter++;
                if (this.mCounter >= this.mItemsCount || !this.mIsLoading) {
                    stopLoading();
                } else {
                    ThumbnailViewAdapter.this.mLauncher.getWorkspace().post(this);
                }
            }
        }
    }

    public ThumbnailViewAdapter(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    protected View inflateThumbnailView() {
        return (AutoLayoutThumbnailItem) this.mInflater.inflate(R.layout.thumbnail_item, null);
    }

    void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
    }

    boolean isRefreshing() {
        return this.mIsRefreshing;
    }

    public boolean adaptIconStyle(View view) {
        ImageView background = (ImageView) view.findViewById(R.id.background);
        if (background != null) {
            background.setImageDrawable(this.mContext.getResources().getDrawable(THUMBNAIL_BACKGROUND[ThumbnailView.CURR_ICON_DRAWABLE_INDEX]));
            background.getDrawable().mutate();
        }
        return true;
    }

    protected void loadContent(int pos) {
    }

    public void startLoading() {
        this.mLoadingItemsWorker.startLoading();
    }

    public void stopLoading() {
        this.mLoadingItemsWorker.stopLoading();
    }

    public void refreshList() {
    }

    public int getCount() {
        return this.mSourceGroup.getChildCount();
    }

    public View getItem(int position) {
        return this.mSourceGroup.getChildAt(position);
    }

    public long getItemId(int position) {
        View view = getItem(position);
        return view == null ? -1 : (long) view.getId();
    }

    protected View getThumbnailView(int position) {
        View resultView;
        if (this.mAllThumbnailViews == null) {
            this.mAllThumbnailViews = new View[getCount()];
        } else if (position == 0 && this.mAllThumbnailViews.length < getCount()) {
            View[] views = new View[getCount()];
            System.arraycopy(this.mAllThumbnailViews, 0, views, 0, this.mAllThumbnailViews.length);
            this.mAllThumbnailViews = views;
        }
        if (this.mAllThumbnailViews[position] != null) {
            resultView = this.mAllThumbnailViews[position];
        } else {
            resultView = inflateThumbnailView();
            this.mAllThumbnailViews[position] = resultView;
        }
        ((ThumbnailIcon) resultView.findViewById(R.id.icon)).setAlpha(1.0f);
        ((ImageView) resultView.findViewById(R.id.foreground)).setImageAlpha(255);
        ((ImageView) resultView.findViewById(R.id.background)).setImageAlpha(255);
        return resultView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public int getSelectedIndex() {
        return -1;
    }
}
