package com.miui.home.launcher.lockwallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import android.widget.TextView;
import com.google.gson.Gson;
import com.miui.home.R;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.lockwallpaper.mode.WallpaperInfo;
import java.io.File;
import java.io.IOException;
import miui.content.res.ThemeResources;
import miui.graphics.BitmapFactory;
import miui.view.animation.SineEaseInInterpolator;
import miui.view.animation.SineEaseInOutInterpolator;

public class ActionMenus extends LinearLayout implements OnClickListener {
    private View[] mActions = new View[4];
    private boolean mCanFinish;
    private Gson mGson;
    private Animator[] mItemAnimIn = new Animator[4];
    private Animator[] mItemAnimOut = new Animator[4];
    private AnimatorSet mItemAnimSetIn = new AnimatorSet();
    private AnimatorSet mItemAnimSetOut = new AnimatorSet();
    private View mLikeView;
    private LockWallpaperPreviewView mMainView;
    private boolean mNeedShowLoading;
    private boolean mRegistered;
    private boolean mShow = false;
    private String mSnapShotDir;
    private BroadcastReceiver mWallpaperChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final boolean succeed = intent.getBooleanExtra("set_lock_wallpaper_result", true);
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    if (succeed) {
                        ThemeResources.getLockWallpaperCache(ActionMenus.this.getContext().getApplicationContext());
                    }
                    return null;
                }

                protected void onPostExecute(Void result) {
                    ActionMenus.this.mMainView.postDelayed(new Runnable() {
                        public void run() {
                            ((Activity) ActionMenus.this.getContext()).finish();
                        }
                    }, 400);
                }
            }.execute(new Void[0]);
        }
    };

    private class SavePicTask extends AsyncTask<Void, Void, Boolean> {
        private Bitmap mBitmap;

        private SavePicTask() {
            this.mBitmap = null;
        }

        public void setBitmap(Bitmap b) {
            this.mBitmap = b;
        }

        public boolean saveBitmapToPNG(Bitmap b, String dir, String name) {
            boolean z = false;
            String path = dir + name;
            File destDir = new File(dir);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }
            if (b != null) {
                try {
                    z = BitmapFactory.saveToFile(b, path);
                } catch (IOException e) {
                }
            }
            return z;
        }

        protected Boolean doInBackground(Void... params) {
            ActionMenus.this.mNeedShowLoading = true;
            return Boolean.valueOf(saveBitmapToPNG(this.mBitmap, ActionMenus.this.mSnapShotDir, "lock_wallpaper.jpg"));
        }

        protected void onPostExecute(Boolean result) {
            if (!((Activity) ActionMenus.this.getContext()).isFinishing()) {
                if (result.booleanValue()) {
                    try {
                        Uri imageUri = FileProvider.getUriForFile(ActionMenus.this.getContext(), "com.miui.home.fileprovider", new File(ActionMenus.this.mSnapShotDir + "lock_wallpaper.jpg"));
                        Intent shareIntent = new Intent("android.intent.action.SEND");
                        shareIntent.setType("image/*");
                        shareIntent.putExtra("android.intent.extra.STREAM", imageUri);
                        shareIntent.setFlags(268435457);
                        ActionMenus.this.mContext.startActivity(Intent.createChooser(shareIntent, ActionMenus.this.mContext.getString(R.string.share)));
                        ActionMenus.this.getContext().sendBroadcast(new Intent("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"));
                    } catch (Exception e) {
                        Log.w("ActionMenus", "share failed!", e);
                    }
                    ((Activity) ActionMenus.this.getContext()).finish();
                }
                ActionMenus.this.mNeedShowLoading = false;
            }
        }
    }

    public ActionMenus(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mSnapShotDir = getContext().getFilesDir() + "/images/";
        this.mGson = new Gson();
        this.mActions[0] = createImage(R.drawable.settings, 0, R.string.settings);
        View[] viewArr = this.mActions;
        View createImage = createImage(R.drawable.like, 1, R.string.like);
        this.mLikeView = createImage;
        viewArr[1] = createImage;
        this.mActions[2] = createImage(R.drawable.share, 2, R.string.share);
        this.mActions[3] = createImage(R.drawable.apply, 3, R.string.apply);
        LayoutParams lp = new LayoutParams(0, 0, 100.0f);
        addView(new Space(this.mContext), lp);
        for (int i = 0; i < 4; i++) {
            addView(this.mActions[i], new LayoutParams(-2, -2));
        }
        addView(new Space(this.mContext), lp);
        initAnim();
        this.mItemAnimSetIn.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                for (int i = 0; i < 4; i++) {
                    ActionMenus.this.mActions[i].setAlpha(0.0f);
                }
                ActionMenus.this.setVisibility(0);
            }
        });
        this.mItemAnimSetOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ActionMenus.this.setVisibility(4);
                if (ActionMenus.this.mCanFinish) {
                    ((Activity) ActionMenus.this.getContext()).finish();
                } else if (ActionMenus.this.mNeedShowLoading) {
                    ActionMenus.this.mMainView.getLoadingView().setVisibility(0);
                }
            }
        });
    }

    private void initAnim() {
        for (int i = 0; i < 4; i++) {
            this.mItemAnimIn[i] = getItemAnimIn(this.mActions[i]);
            this.mItemAnimIn[i].setStartDelay((long) (i * 50));
            this.mItemAnimOut[i] = getItemAnimOut(this.mActions[i]);
            this.mItemAnimOut[i].setStartDelay((long) (i * 50));
        }
        this.mItemAnimSetIn.playTogether(this.mItemAnimIn);
        this.mItemAnimSetOut.playTogether(this.mItemAnimOut);
    }

    public void setMainView(LockWallpaperPreviewView view) {
        this.mMainView = view;
    }

    private View createImage(int imageId, int tag, int textId) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, null);
        ((ImageView) view.findViewById(R.id.menu_item_image)).setImageResource(imageId);
        ((TextView) view.findViewById(R.id.menu_item_text)).setText(textId);
        view.setTag(Integer.valueOf(tag));
        view.setOnClickListener(this);
        return view;
    }

    public void onClick(View v) {
        requestDisallowInterceptTouchEvent(true);
        switch (((Integer) v.getTag()).intValue()) {
            case 0:
                onSetting();
                return;
            case 1:
                onLike(v);
                return;
            case 2:
                onShare();
                return;
            case 3:
                onApply();
                return;
            default:
                return;
        }
    }

    private void onApply() {
        show(false);
        this.mMainView.startExitAnim();
        int currentItem = this.mMainView.getCurrentItem();
        WallpaperInfo info = this.mMainView.getAdapter().getWallpaperInfo(currentItem);
        if (TextUtils.isEmpty(info.key)) {
            this.mCanFinish = true;
            return;
        }
        try {
            this.mMainView.getAdapter().recordEvent(currentItem, 4);
            Intent intent = new Intent("android.miui.REQUEST_LOCKSCREEN_WALLPAPER");
            intent.putExtra("wallpaperInfo", this.mGson.toJson(info));
            if (currentItem == 0) {
                this.mCanFinish = true;
            } else {
                intent.putExtra("apply", true);
                this.mNeedShowLoading = true;
                if (!this.mRegistered) {
                    IntentFilter wallpaperChangeIntentFilter = new IntentFilter();
                    wallpaperChangeIntentFilter.addAction("com.miui.keyguard.setwallpaper");
                    getContext().registerReceiver(this.mWallpaperChangeReceiver, wallpaperChangeIntentFilter);
                    this.mRegistered = true;
                }
            }
            getContext().sendBroadcast(intent);
        } catch (Exception e) {
            this.mCanFinish = true;
        }
    }

    private void onShare() {
        try {
            View view = this.mMainView.getAdapter().getView(this.mMainView.getCurrentItem());
            if (view != null) {
                Bitmap bitmap = convertViewToBitmap(view, view.getWidth(), view.getHeight());
                SavePicTask task = new SavePicTask();
                task.setBitmap(bitmap);
                task.execute(new Void[0]);
                show(false);
                this.mMainView.startExitAnim();
            }
        } catch (Exception e) {
        }
    }

    public static Bitmap convertViewToBitmap(View view, int bitmapWidth, int bitmapHeight) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    private void onLike(View v) {
        try {
            int currentItem = this.mMainView.getCurrentItem();
            WallpaperInfo info = this.mMainView.getAdapter().getWallpaperInfo(this.mMainView.getCurrentItem());
            boolean isLiked = !info.like;
            ImageView image = (ImageView) v.findViewById(R.id.menu_item_image);
            image.setImageResource(isLiked ? R.drawable.liked : R.drawable.like);
            ((TextView) v.findViewById(R.id.menu_item_text)).setText(isLiked ? R.string.liked : R.string.like);
            info.like = isLiked;
            this.mMainView.getAdapter().recordEvent(currentItem, isLiked ? 3 : 5);
            AnimationSet animationSet = new AnimationSet(false);
            Animation animation1 = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, 1, 0.5f, 1, 0.5f);
            animation1.setInterpolator(new SineEaseInInterpolator());
            animation1.setDuration(100);
            Animation animation2 = new ScaleAnimation(1.1f, 0.9f, 1.1f, 0.9f, 1, 0.5f, 1, 0.5f);
            animation2.setInterpolator(new SineEaseInOutInterpolator());
            animation2.setDuration(200);
            animation2.setStartOffset(100);
            Animation animation3 = new ScaleAnimation(0.9f, 1.0f, 0.9f, 1.0f, 1, 0.5f, 1, 0.5f);
            animation3.setInterpolator(new SineEaseInOutInterpolator());
            animation3.setDuration(200);
            animation3.setStartOffset(300);
            animationSet.addAnimation(animation1);
            animationSet.addAnimation(animation2);
            animationSet.addAnimation(animation3);
            image.startAnimation(animationSet);
            if (currentItem == 0) {
                Intent intent = new Intent("android.miui.REQUEST_LOCKSCREEN_WALLPAPER");
                intent.putExtra("wallpaperInfo", this.mGson.toJson(info));
                getContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSetting() {
        getContext().sendBroadcast(new Intent("xiaomi.intent.action.SHOW_SECURE_KEYGUARD"));
        ComponentName cn = getSettingsComponent(WallpaperUtils.getLockWallpaperProvider(this.mContext));
        if (cn == null) {
            cn = getSettingsComponent(WallpaperUtils.sDefaultLockWallpaperProvider);
        }
        if (cn == null) {
            cn = getDefaultComponent();
        }
        Intent intent = new Intent();
        intent.setComponent(cn);
        intent.addFlags(268435456);
        try {
            getContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ((Activity) getContext()).finish();
    }

    private ComponentName getSettingsComponent(String providerInCharge) {
        ComponentName componentName = null;
        if (!TextUtils.isEmpty(providerInCharge)) {
            IContentProvider provider = this.mContext.getContentResolver().acquireUnstableProvider(Uri.parse("content://" + providerInCharge));
            if (provider != null) {
                try {
                    String name = provider.call(this.mContext.getPackageName(), "getSettingsComponent", null, null).getString("result_string");
                    if (!TextUtils.isEmpty(name)) {
                        componentName = ComponentName.unflattenFromString(name);
                        this.mContext.getContentResolver().releaseProvider(provider);
                    }
                } catch (RemoteException e) {
                } finally {
                    this.mContext.getContentResolver().releaseProvider(provider);
                }
            }
        }
        return componentName;
    }

    private ComponentName getDefaultComponent() {
        return new ComponentName("com.miui.home", "com.miui.home.launcher.setting.ChooseWallpaperPreference");
    }

    public void updateLikeView() {
        ImageView image = (ImageView) this.mLikeView.findViewById(R.id.menu_item_image);
        WallpaperInfo info = this.mMainView.getAdapter().getWallpaperInfo(this.mMainView.getCurrentItem());
        boolean isLiked = info.like;
        image.setImageResource(isLiked ? R.drawable.liked : R.drawable.like);
        ((TextView) this.mLikeView.findViewById(R.id.menu_item_text)).setText(isLiked ? R.string.liked : R.string.like);
        this.mLikeView.setTransitionAlpha(info.supportLike ? 1.0f : 0.5f);
        this.mLikeView.setEnabled(info.supportLike);
    }

    private Animator getItemAnimOut(View v) {
        ObjectAnimator alphaAnimOut = ObjectAnimator.ofFloat(v, "alpha", new float[]{1.0f, 0.0f});
        ObjectAnimator scaleXAnimOut = ObjectAnimator.ofFloat(v, "scaleX", new float[]{1.0f, 0.9f});
        ObjectAnimator scaleYAnimOut = ObjectAnimator.ofFloat(v, "scaleY", new float[]{1.0f, 0.9f});
        AnimatorSet animOut = new AnimatorSet();
        animOut.play(alphaAnimOut).with(scaleXAnimOut).with(scaleYAnimOut);
        animOut.setDuration(250);
        return animOut;
    }

    private Animator getItemAnimIn(View v) {
        ObjectAnimator alphaAnimIn = ObjectAnimator.ofFloat(v, "alpha", new float[]{0.0f, 1.0f});
        ObjectAnimator scaleXAnimIn = ObjectAnimator.ofFloat(v, "scaleX", new float[]{0.9f, 1.0f});
        ObjectAnimator scaleYAnimIn = ObjectAnimator.ofFloat(v, "scaleY", new float[]{0.9f, 1.0f});
        AnimatorSet animIn = new AnimatorSet();
        animIn.play(alphaAnimIn).with(scaleXAnimIn).with(scaleYAnimIn);
        animIn.setDuration(250);
        return animIn;
    }

    public void show(boolean show) {
        if (this.mShow != show) {
            this.mShow = show;
            this.mItemAnimSetIn.end();
            this.mItemAnimSetOut.end();
            if (show) {
                this.mItemAnimSetIn.start();
            } else {
                this.mItemAnimSetOut.start();
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mRegistered) {
            getContext().unregisterReceiver(this.mWallpaperChangeReceiver);
            this.mRegistered = false;
        }
    }

    public void toggle() {
        show(!this.mShow);
        if (this.mShow) {
            this.mMainView.showMask();
        } else {
            this.mMainView.hideMask();
        }
    }
}
