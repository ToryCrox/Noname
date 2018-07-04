package com.miui.home.launcher.setting;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.miui.home.R;
import com.miui.home.launcher.ThumbnailViewAdapter;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.AsyncTaskExecutorHelper.RejectedExecutionPolicy;
import com.miui.home.launcher.setting.ImageListPreference.OnItemClickListener;
import java.io.File;
import java.util.ArrayList;

public class LocalWallpaperAdapter extends ThumbnailViewAdapter implements OnItemClickListener {
    private LayoutInflater mInflater;
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private final ArrayList<String> mWallpaperUriList = new ArrayList();

    public void onItemClick(int position) {
        WallpaperUtils.startWallpaperPreviewActivity(this.mContext, Uri.fromFile(new File((String) this.mWallpaperUriList.get(position))));
    }

    public LocalWallpaperAdapter(Context context) {
        super(context);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mThumbnailWidth = context.getResources().getDimensionPixelSize(R.dimen.setting_wallpaper_image_width);
        this.mThumbnailHeight = context.getResources().getDimensionPixelSize(R.dimen.setting_wallpaper_image_height);
        refreshList();
    }

    public void refreshList() {
        WallpaperUtils.loadLocalWallpaperList(this.mWallpaperUriList);
    }

    public int getCount() {
        return this.mWallpaperUriList.size();
    }

    public View getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || !(convertView instanceof FrameLayout)) {
            convertView = (FrameLayout) this.mInflater.inflate(R.layout.preference_list_item, null);
        }
        final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        LayoutParams lp = icon.getLayoutParams();
        lp.width = this.mThumbnailWidth;
        lp.height = this.mThumbnailHeight;
        icon.setScaleType(ScaleType.CENTER_CROP);
        final String uri = (String) this.mWallpaperUriList.get(position);
        if (!((convertView.getTag() instanceof String) && uri.equals((String) convertView.getTag()))) {
            convertView.setTag(uri);
            new AsyncTask<Void, Void, Drawable>() {
                protected Drawable doInBackground(Void... params) {
                    return WallpaperUtils.getWallpaperThumbnail(uri, LocalWallpaperAdapter.this.mContext, LocalWallpaperAdapter.this.mThumbnailWidth, LocalWallpaperAdapter.this.mThumbnailHeight);
                }

                protected void onPostExecute(Drawable result) {
                    icon.setImageDrawable(result);
                    RejectedExecutionPolicy.executeRejectedTaskIfNeeded();
                }
            }.execute(new Void[0]);
        }
        return convertView;
    }
}
