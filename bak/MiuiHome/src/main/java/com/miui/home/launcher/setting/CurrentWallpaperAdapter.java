package com.miui.home.launcher.setting;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.DeviceConfig;
import com.miui.home.launcher.ThumbnailViewAdapter;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.Utilities;
import com.miui.home.launcher.setting.ImageListPreference.OnItemClickListener;

public class CurrentWallpaperAdapter extends ThumbnailViewAdapter implements OnItemClickListener {
    private Context mContext;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private int mWallpaperPreviewCount = 2;

    public CurrentWallpaperAdapter(Context context) {
        super(context);
        this.mContext = context;
        this.mPreviewWidth = context.getResources().getDimensionPixelSize(R.dimen.setting_wallpaper_image_width);
        this.mPreviewHeight = (this.mPreviewWidth * DeviceConfig.getDeviceHeight()) / DeviceConfig.getDeviceWidth();
    }

    public int getCount() {
        return this.mWallpaperPreviewCount;
    }

    public View getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void onItemClick(int position) {
        String title;
        String settingFlag;
        if (position == 0) {
            title = this.mContext.getResources().getString(R.string.lock_screen_wallpaper);
            settingFlag = "lock_wallpaper";
        } else {
            title = this.mContext.getResources().getString(R.string.normal_wallpaper);
            settingFlag = "wallpaper";
        }
        Intent intent = WallpaperUtils.getThemeManagerWallpaperPickerIntent(this.mContext, title, false);
        if (intent == null) {
            Toast.makeText(this.mContext, R.string.no_detail_page, 200);
            return;
        }
        intent.putExtra("wallpaper_setting_type", settingFlag);
        this.mContext.startActivity(Utilities.generateShowFragmentIntent(intent, title));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.current_wallpaper, null);
        }
        final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.content);
        TextView title = (TextView) convertView.findViewById(R.id.content_title);
        LayoutParams params = thumbnail.getLayoutParams();
        params.height = this.mPreviewHeight;
        params.width = this.mPreviewWidth;
        thumbnail.setLayoutParams(params);
        if (position == 0) {
            title.setText(R.string.lock_screen);
            final HandlerThread thread = new HandlerThread("LockScreen_Thread");
            thread.start();
            new Handler(thread.getLooper()).post(new Runnable() {
                public void run() {
                    final Drawable lockScreen = WallpaperUtils.getLockScreenPreview(CurrentWallpaperAdapter.this.mPreviewWidth, CurrentWallpaperAdapter.this.mPreviewHeight);
                    thumbnail.post(new Runnable() {
                        public void run() {
                            thumbnail.setImageDrawable(lockScreen);
                        }
                    });
                    thread.quit();
                }
            });
        } else if (position == 1) {
            title.setText(R.string.home_screen);
            thumbnail.postDelayed(new Runnable() {
                public void run() {
                    thumbnail.setImageDrawable(WallpaperUtils.getHomeScreenPreview(CurrentWallpaperAdapter.this.mPreviewWidth, CurrentWallpaperAdapter.this.mPreviewHeight));
                }
            }, 200);
        }
        return convertView;
    }
}
