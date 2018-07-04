package com.miui.home.launcher;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.miui.home.R;
import java.util.ArrayList;

public class WallpaperThumbnailViewAdapter extends ThumbnailViewAdapter {
    private static int CUSTOM_WALLPAPER_PICKER_COUNT;
    private static final int[] WALLPAPER_THUMBNAIL_FG = new int[]{R.drawable.thumbnail_item_fg, R.drawable.thumbnail_item_fg_dark};
    public static final ArrayList<String> mSkippedItems = new ArrayList();
    private int mCurrentSelectedIndex = -1;
    private int mCustomPickerStartIndex;
    private boolean mIsCurrentWallpaperExist = true;
    private boolean mIsOnlineWallpaperExist = true;
    private final ArrayList<ResolveInfo> mPickerList = new ArrayList();
    private Resources mResources;
    private int mThumbnailHeight;
    private int mThumbnailWidth;
    private String mWallpaperBackupPath = null;
    private ArrayList<String> mWallpaperUriList = new ArrayList();

    public WallpaperThumbnailViewAdapter(Context context) {
        super(context);
        this.mResources = context.getResources();
        Drawable mask = this.mResources.getDrawable(R.drawable.thumbnail_item_mask);
        this.mThumbnailWidth = mask.getIntrinsicWidth();
        this.mThumbnailHeight = mask.getIntrinsicHeight();
        refreshList();
    }

    public boolean adaptIconStyle(View view) {
        super.adaptIconStyle(view);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (icon == null || icon.getVisibility() == 4) {
            return false;
        }
        ImageView foreground = (ImageView) view.findViewById(R.id.foreground);
        if ((view.getTag() instanceof String) || (view.getTag() instanceof WallpaperInfo) || (view.getTag() instanceof WallpaperThumbnailInfo)) {
            foreground.setImageResource(WALLPAPER_THUMBNAIL_FG[ThumbnailView.CURR_ICON_DRAWABLE_INDEX]);
            foreground.setVisibility(0);
            LayoutParams lp = icon.getLayoutParams();
            lp.width = this.mThumbnailWidth;
            lp.height = this.mThumbnailHeight;
            icon.setScaleType(ScaleType.CENTER_CROP);
        } else {
            foreground.setVisibility(4);
        }
        return true;
    }

    public void refreshList() {
        WallpaperUtils.loadLocalWallpaperList(this.mWallpaperUriList);
        WallpaperUtils.loadImagePickerList(this.mContext, this.mPickerList);
        CUSTOM_WALLPAPER_PICKER_COUNT = this.mPickerList.size();
        this.mCustomPickerStartIndex = (this.mWallpaperUriList.size() + 1) + 1;
    }

    public void loadContent(int pos) {
    }

    public int getCustomHeaderIndex() {
        int i = 0;
        int i2 = this.mCustomPickerStartIndex - (this.mIsCurrentWallpaperExist ? 0 : 1);
        if (!this.mIsOnlineWallpaperExist) {
            i = 1;
        }
        return i2 - i;
    }

    public int getCount() {
        return (this.mCustomPickerStartIndex + CUSTOM_WALLPAPER_PICKER_COUNT) + 1;
    }

    public View getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) getThumbnailView(position);
        resultView.setLauncher(this.mLauncher);
        final ImageView icon = (ImageView) resultView.findViewById(R.id.icon);
        icon.setVisibility(0);
        TextView title = (TextView) resultView.findViewById(R.id.content_title);
        title.setText(null);
        ((ImageView) resultView.findViewById(R.id.background)).setVisibility(0);
        ((ImageView) resultView.findViewById(R.id.foreground)).setVisibility(4);
        LayoutParams lp = icon.getLayoutParams();
        lp.width = -2;
        lp.height = -2;
        if (position == 0) {
            Intent intent = WallpaperUtils.getThemeManagerWallpaperPickerIntent(this.mContext, this.mResources.getString(R.string.wallpaper), true);
            if (intent != null) {
                resultView.setTag(intent);
                title.setText(this.mResources.getString(R.string.online_wallpaper));
                title.setVisibility(0);
            } else {
                this.mIsOnlineWallpaperExist = false;
                return null;
            }
        }
        if (position == 1) {
            int i;
            this.mIsCurrentWallpaperExist = true;
            WallpaperManager wpm = (WallpaperManager) this.mLauncher.getSystemService("wallpaper");
            if (wpm.getWallpaperInfo() != null) {
                this.mWallpaperBackupPath = null;
                WallpaperInfo paperInfo = wpm.getWallpaperInfo();
                new AsyncTask<Void, Void, Drawable>() {
                    protected Drawable doInBackground(Void... params) {
                        Drawable preview = WallpaperUtils.getLiveWallpaperPreview(WallpaperThumbnailViewAdapter.this.mContext);
                        if (preview == null || !(preview instanceof BitmapDrawable)) {
                            return null;
                        }
                        return WallpaperUtils.getWallpaperThumbnail(preview, WallpaperThumbnailViewAdapter.this.mContext, WallpaperThumbnailViewAdapter.this.mThumbnailWidth, WallpaperThumbnailViewAdapter.this.mThumbnailHeight);
                    }

                    protected void onPostExecute(Drawable result) {
                        WallpaperThumbnailViewAdapter.this.setWallpaperPreview(result, icon, resultView);
                    }
                }.execute(new Void[0]);
                resultView.setTag(paperInfo);
            } else {
                String wallpaperSrc = WallpaperUtils.getWallpaperSourcePath("pref_key_current_wallpaper_path");
                if ((TextUtils.isEmpty(wallpaperSrc) || !this.mWallpaperUriList.contains(wallpaperSrc)) && WallpaperUtils.isFileExist("current_wallpaper")) {
                    new AsyncTask<Void, Void, Drawable>() {
                        protected Drawable doInBackground(Void... params) {
                            WallpaperThumbnailViewAdapter.this.mWallpaperBackupPath = WallpaperUtils.copyFile("current_wallpaper", "backup_wallpaper");
                            resultView.setTag(new WallpaperThumbnailInfo(WallpaperThumbnailViewAdapter.this.mWallpaperBackupPath));
                            return WallpaperUtils.getWallpaperThumbnail(WallpaperThumbnailViewAdapter.this.mWallpaperBackupPath, WallpaperThumbnailViewAdapter.this.mContext, WallpaperThumbnailViewAdapter.this.mThumbnailWidth, WallpaperThumbnailViewAdapter.this.mThumbnailHeight);
                        }

                        protected void onPostExecute(Drawable result) {
                            WallpaperThumbnailViewAdapter.this.setWallpaperPreview(result, icon, resultView);
                            WallpaperThumbnailViewAdapter.this.adaptIconStyle(resultView);
                        }
                    }.execute(new Void[0]);
                } else {
                    this.mWallpaperBackupPath = null;
                    this.mIsCurrentWallpaperExist = false;
                    this.mCurrentSelectedIndex = -1;
                    return null;
                }
            }
            if (this.mIsOnlineWallpaperExist) {
                i = 0;
            } else {
                i = 1;
            }
            this.mCurrentSelectedIndex = position - i;
        } else if (position > 1 && position <= this.mWallpaperUriList.size() + 1) {
            String uri = (String) this.mWallpaperUriList.get((position - 1) - 1);
            resultView.setTag(new WallpaperThumbnailInfo(uri));
            title.setVisibility(4);
            if (uri.equals(WallpaperUtils.getWallpaperSourcePath("pref_key_current_wallpaper_path"))) {
                this.mCurrentSelectedIndex = (position - (this.mIsOnlineWallpaperExist ? 0 : 1)) - (this.mIsCurrentWallpaperExist ? 0 : 1);
            }
        } else if (position > this.mWallpaperUriList.size() + 1 && position <= this.mCustomPickerStartIndex) {
            title.setText(this.mResources.getString(R.string.custom_wallpaper_picker));
            title.setVisibility(0);
        } else if (position > this.mCustomPickerStartIndex && position <= this.mCustomPickerStartIndex + CUSTOM_WALLPAPER_PICKER_COUNT) {
            ResolveInfo info = (ResolveInfo) this.mPickerList.get((position - this.mCustomPickerStartIndex) - 1);
            resultView.setTag(WallpaperUtils.getIntent(info));
            icon.setVisibility(4);
            title.setVisibility(0);
            title.setText(info.activityInfo.loadLabel(this.mLauncher.getPackageManager()));
            title.setVisibility(0);
        }
        adaptIconStyle(resultView);
        return resultView;
    }

    public View getThumbnailView(String wallpaperPath) {
        AutoLayoutThumbnailItem resultView = (AutoLayoutThumbnailItem) inflateThumbnailView();
        resultView.setLauncher(this.mLauncher);
        resultView.setTag(new WallpaperThumbnailInfo(wallpaperPath));
        ((TextView) resultView.findViewById(R.id.content_title)).setVisibility(4);
        adaptIconStyle(resultView);
        return resultView;
    }

    public void releaseThumbnailContent(View resultView) {
        if ((resultView instanceof AutoLayoutThumbnailItem) && (resultView.getTag() instanceof WallpaperThumbnailInfo)) {
            ((ImageView) resultView.findViewById(R.id.icon)).setImageDrawable(null);
        }
    }

    public void loadThumbnailContent(final View resultView) {
        if ((resultView instanceof AutoLayoutThumbnailItem) && (resultView.getTag() instanceof WallpaperThumbnailInfo)) {
            final String uri = ((WallpaperThumbnailInfo) resultView.getTag()).getWallpaperPath();
            final ImageView icon = (ImageView) resultView.findViewById(R.id.icon);
            if (icon.getDrawable() == null) {
                new AsyncTask<Void, Void, Drawable>() {
                    protected Drawable doInBackground(Void... params) {
                        return WallpaperUtils.getWallpaperThumbnail(uri, WallpaperThumbnailViewAdapter.this.mContext, WallpaperThumbnailViewAdapter.this.mThumbnailWidth, WallpaperThumbnailViewAdapter.this.mThumbnailHeight);
                    }

                    protected void onPostExecute(Drawable result) {
                        WallpaperThumbnailViewAdapter.this.setWallpaperPreview(result, icon, resultView);
                    }
                }.execute(new Void[0]);
            }
        }
    }

    private void setWallpaperPreview(Drawable preview, ImageView icon, View resultView) {
        if (preview != null) {
            icon.setImageDrawable(preview);
            return;
        }
        icon.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.wallpaper_load_fail));
        icon.setScaleType(ScaleType.CENTER);
        resultView.setTag("");
    }

    public String getBackupPath() {
        return this.mWallpaperBackupPath;
    }

    public int getSelectedIndex() {
        return this.mCurrentSelectedIndex;
    }

    public boolean isOnlineWallpaperExist() {
        return this.mIsOnlineWallpaperExist;
    }
}
