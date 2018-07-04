package com.miui.home.launcher.lockwallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import miui.content.res.ThemeResources;
import miui.graphics.BitmapFactory;
import org.json.JSONObject;

public class LockWallpaperPreviewAdapter extends PagerAdapter {
    private static final boolean DEBUG = LockWallpaperPreviewActivity.DEBUG;
    private Context mContext;
    private Handler mHandler = new H();
    private LockWallpaperPreviewView mMainView;
    private int mMaxPixels;
    private List<WallpaperItem> mWallpaperItems;

    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            this.imageViewReference = new WeakReference(imageView);
        }

        protected Bitmap doInBackground(Integer... params) {
            Bitmap b = null;
            try {
                WallpaperInfo info = ((WallpaperItem) LockWallpaperPreviewAdapter.this.mWallpaperItems.get(params[0].intValue())).mInfo;
                if (info != null) {
                    b = BitmapFactory.decodeBitmap(LockWallpaperPreviewAdapter.this.mContext, Uri.parse(info.wallpaperUri), LockWallpaperPreviewAdapter.this.mMaxPixels, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e2) {
                e2.printStackTrace();
            }
            return b;
        }

        protected void onPostExecute(Bitmap bitmap) {
            if (this.imageViewReference != null && bitmap != null) {
                ImageView imageView = (ImageView) this.imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    protected class H extends Handler {
        protected H() {
        }

        public void handleMessage(Message m) {
            switch (m.what) {
                case 100:
                    LockWallpaperPreviewAdapter.this.handleRecordEvent(m.arg1, m.arg2);
                    return;
                default:
                    return;
            }
        }
    }

    public LockWallpaperPreviewAdapter(Context context, List<WallpaperInfo> wallpaperInfos) {
        this.mContext = context;
        this.mWallpaperItems = new ArrayList();
        do {
            for (int i = 0; i < wallpaperInfos.size(); i++) {
                WallpaperInfo info = (WallpaperInfo) wallpaperInfos.get(i);
                WallpaperItem item = new WallpaperItem();
                item.mInfo = info;
                item.mRealIndex = i;
                this.mWallpaperItems.add(item);
            }
            if (this.mWallpaperItems.size() <= 1) {
                break;
            }
        } while (this.mWallpaperItems.size() <= 5);
        if (DEBUG) {
            Log.d("LockWallpaperPreviewAdapter", "info size:" + wallpaperInfos.size() + " item size:" + this.mWallpaperItems.size());
        }
        DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
        this.mMaxPixels = dm.widthPixels * dm.heightPixels;
    }

    public void recordEvent(int position, int event) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(100, position, event));
    }

    public void handleRecordEvent(int position, int event) {
        if (DEBUG) {
            Log.d("LockWallpaperPreviewAdapter", "handleRecordEvent position:" + position + " event:" + event);
        }
        WallpaperInfo info = ((WallpaperItem) this.mWallpaperItems.get(position)).mInfo;
        String providerInCharge = info.authority;
        if (!TextUtils.isEmpty(providerInCharge) && !"com.miui.home.none_provider".equals(providerInCharge)) {
            IContentProvider provider = this.mContext.getContentResolver().acquireUnstableProvider(Uri.parse("content://" + providerInCharge));
            if (provider != null) {
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("key", info.key);
                    jo.put("event", event);
                    handleRecordEvent(provider, jo.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    this.mContext.getContentResolver().releaseProvider(provider);
                }
            }
        }
    }

    private void handleRecordEvent(IContentProvider provider, String requestJson) {
        if (DEBUG) {
            Log.d("LockWallpaperPreviewAdapter", "handleRecordEvent requestJson:" + requestJson);
        }
        try {
            Bundle extras = new Bundle();
            extras.putString("request_json", requestJson);
            provider.call(this.mContext.getPackageName(), "recordEvent", null, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WallpaperInfo getWallpaperInfo(int position) {
        return ((WallpaperItem) this.mWallpaperItems.get(position)).mInfo;
    }

    public View getView(int position) {
        return ((WallpaperItem) this.mWallpaperItems.get(position)).mView;
    }

    public int getCount() {
        return this.mWallpaperItems.size() == 1 ? 1 : Integer.MAX_VALUE;
    }

    public int getSize() {
        return this.mWallpaperItems.size();
    }

    public boolean isViewFromObject(View view, Object object) {
        return view == ((WallpaperItem) object).mView;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        final int pos = position % getSize();
        WallpaperItem item = (WallpaperItem) this.mWallpaperItems.get(pos);
        final WallpaperInfo info = item.mInfo;
        View viewgroup = LayoutInflater.from(this.mContext).inflate(R.layout.page_template, null);
        TextView title = (TextView) viewgroup.findViewById(R.id.title);
        TextView content = (TextView) viewgroup.findViewById(R.id.content);
        viewgroup.findViewById(R.id.click_area).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = info.buildIntent();
                if (intent != null) {
                    try {
                        LockWallpaperPreviewAdapter.this.mContext.startActivity(intent);
                        LockWallpaperPreviewAdapter.this.recordEvent(pos, 2);
                        LockWallpaperPreviewAdapter.this.mContext.sendBroadcast(new Intent("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"));
                        ((Activity) LockWallpaperPreviewAdapter.this.mMainView.getContext()).finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        ImageView image = (ImageView) viewgroup.findViewById(R.id.wallpaper);
        if (item.mRealIndex == 0) {
            image.setImageDrawable(ThemeResources.getLockWallpaperCache(this.mContext));
        } else {
            loadBitmap(pos, image);
        }
        title.setText(info.title);
        content.setText(info.content);
        container.addView(viewgroup, new LayoutParams(-1, -1));
        item.mView = viewgroup;
        return item;
    }

    public void loadBitmap(int position, ImageView imageView) {
        new BitmapWorkerTask(imageView).execute(new Integer[]{Integer.valueOf(position)});
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        WallpaperItem item = (WallpaperItem) object;
        container.removeView(item.mView);
        item.mView = null;
    }

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        View clickArea = view.findViewById(R.id.click_area);
        View title = view.findViewById(R.id.title);
        View content = view.findViewById(R.id.content);
        if (position < -1.0f) {
            title.setTranslationX(0.0f);
            title.setAlpha(0.0f);
            content.setTranslationX(0.0f);
            content.setAlpha(0.0f);
        } else if (position <= 1.0f) {
            clickArea.setAlpha(1.0f);
            title.setTranslationX((((float) pageWidth) * position) * 0.2f);
            title.setAlpha(getTitleFactor(1.0f - Math.abs(position)));
            content.setTranslationX((((float) pageWidth) * position) * 0.1f);
            content.setAlpha(getContentFactor(1.0f - Math.abs(position)));
        } else {
            title.setTranslationX(0.0f);
            title.setAlpha(0.0f);
            content.setTranslationX(0.0f);
            content.setAlpha(0.0f);
        }
    }

    private float getTitleFactor(float position) {
        return (position * position) * position;
    }

    private float getContentFactor(float position) {
        return position;
    }

    public void setViewPager(LockWallpaperPreviewView lockWallpaperPreviewView) {
        this.mMainView = lockWallpaperPreviewView;
    }
}
