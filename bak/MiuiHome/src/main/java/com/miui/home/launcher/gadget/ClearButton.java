package com.miui.home.launcher.gadget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.ItemIcon;
import com.miui.home.launcher.ItemInfo;
import com.miui.home.launcher.WallpaperUtils;
import com.miui.home.launcher.common.CircleProgressBar;
import com.miui.home.launcher.common.CircleProgressBar.OnProgressChangedListener;
import miui.content.res.IconCustomizer;
import miui.util.HardwareInfo;

public class ClearButton extends Gadget {
    private static boolean sIsSony;
    private boolean mIsPaused;
    private TextView mLabel;
    private int mPreUsedMemory;
    protected CircleProgressBar mProgressBar;
    private Runnable mRefreshAndScheduleRunnable = new Runnable() {
        public void run() {
            ClearButton.this.refreshAndSchedue();
        }
    };
    private TextView mTitle;
    private ViewGroup mTitleContainer;
    private int mTotalMemory;

    static {
        boolean z = "LT26i".equals(Build.DEVICE) || "LT18i".equals(Build.DEVICE);
        sIsSony = z;
    }

    public ClearButton(Context context) {
        super(context);
    }

    public void onCreate() {
        this.mTotalMemory = (int) (HardwareInfo.getTotalPhysicalMemory() / 1048576);
        inflate(this.mContext, R.layout.gadget_clear_button, this);
        ((ImageView) findViewById(R.id.background)).setImageDrawable(IconCustomizer.generateIconStyleDrawable(this.mContext.getResources().getDrawable(R.drawable.gadget_clear_button_bg)));
        this.mTitleContainer = (ViewGroup) findViewById(R.id.title_container);
        this.mTitle = (TextView) findViewById(R.id.label);
        this.mProgressBar = (CircleProgressBar) findViewById(R.id.progressBar);
        this.mLabel = (TextView) findViewById(R.id.label);
        this.mProgressBar.setClickable(true);
        this.mProgressBar.setMax(this.mTotalMemory);
        this.mProgressBar.setRotateVelocity(600);
        this.mProgressBar.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ClearButton.this.execClear();
                ClearButton.this.trackClick();
            }
        });
        this.mProgressBar.setOnProgressChangedListener(new OnProgressChangedListener() {
            public void onProgressChanged() {
                ClearButton.this.updateLabel();
            }
        });
        initProgressBar();
    }

    protected void initProgressBar() {
        this.mProgressBar.setDrawablesForLevels(null, new int[]{R.drawable.gadget_clear_button_circle}, new int[]{R.drawable.gadget_clear_button_fore_normal});
    }

    public void onWallpaperColorChanged() {
        int themeShadowColor;
        int shadowColor = 0;
        if (WallpaperUtils.hasAppliedLightWallpaper()) {
            this.mTitle.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle.dark);
            themeShadowColor = getContext().getResources().getColor(R.color.icon_title_text_shadow_light);
        } else {
            this.mTitle.setTextAppearance(this.mContext, R.style.WorkspaceIconTitle);
            themeShadowColor = getContext().getResources().getColor(R.color.icon_title_text_shadow);
            ItemInfo info = (ItemInfo) getTag();
            shadowColor = WallpaperUtils.getTitleShadowColor(WallpaperUtils.getIconTitleBgMode(info.cellX, info.cellY, false));
        }
        if (themeShadowColor != 0) {
            shadowColor = themeShadowColor;
        }
        ItemIcon.setTitleShadow(this.mContext, this.mTitle, shadowColor);
    }

    public void onResume() {
        this.mIsPaused = false;
        refreshAndSchedue();
    }

    public void onPause() {
        this.mIsPaused = true;
        stopSchedule();
    }

    public void onDestroy() {
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void updateConfig(Bundle config) {
    }

    public void onEditDisable() {
    }

    public void onEditNormal() {
    }

    public void onAdded() {
    }

    public void onDeleted() {
    }

    protected void doClear() {
        this.mContext.sendBroadcast(new Intent("com.android.systemui.taskmanager.Clear"));
    }

    private void execClear() {
        if (this.mPreUsedMemory == 0) {
            stopSchedule();
            this.mPreUsedMemory = this.mProgressBar.getProgress();
            doClear();
            LayoutParams lp = (LayoutParams) this.mLabel.getLayoutParams();
            if (!(sIsSony || lp.gravity == 85)) {
                lp.rightMargin = (this.mTitleContainer.getWidth() - this.mTitleContainer.getPaddingRight()) - this.mLabel.getRight();
                lp.gravity = 85;
                this.mLabel.setLayoutParams(lp);
            }
            postDelayed(new Runnable() {
                public void run() {
                    ClearButton.this.mProgressBar.setProgressByAnimator(0, new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            ClearButton.this.refreshMemoryUsed(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animation) {
                                    String msg;
                                    int offset = ClearButton.this.mPreUsedMemory - ClearButton.this.mProgressBar.getProgress();
                                    int free = ClearButton.this.getFreeMemory();
                                    if (offset > 0) {
                                        msg = ClearButton.this.getResources().getString(285671558, new Object[]{ClearButton.getFormatedMemory((long) offset, false), ClearButton.getFormatedMemory((long) free, false)});
                                    } else {
                                        msg = ClearButton.this.getResources().getString(285671559);
                                    }
                                    Toast.makeText(ClearButton.this.mContext, msg, 0).show();
                                    ClearButton.this.mPreUsedMemory = 0;
                                    ClearButton.this.startSchedule();
                                }
                            });
                        }
                    });
                }
            }, 120);
        }
    }

    private void updateLabel() {
        this.mLabel.setText(((this.mProgressBar.getProgress() * 100) / this.mProgressBar.getMax()) + "%");
    }

    private void refreshAndSchedue() {
        refreshMemoryUsed(null);
        startSchedule();
    }

    private void refreshMemoryUsed(AnimatorListener listener) {
        int used = this.mTotalMemory - getFreeMemory();
        if (this.mProgressBar.getProgress() != used) {
            this.mProgressBar.setProgressByAnimator(used, listener);
        }
    }

    private void startSchedule() {
        if (!this.mIsPaused) {
            postDelayed(this.mRefreshAndScheduleRunnable, 5000);
        }
    }

    private void stopSchedule() {
        removeCallbacks(this.mRefreshAndScheduleRunnable);
    }

    private int getFreeMemory() {
        return (int) (HardwareInfo.getFreeMemory() / 1048576);
    }

    public static String getFormatedMemory(long memoryM, boolean onlyM) {
        if (onlyM || memoryM < 1024) {
            return memoryM + "M";
        }
        float memoryG = ((float) memoryM) / 1024.0f;
        if (memoryG == ((float) ((int) memoryG))) {
            return String.format("%.0fG", new Object[]{Float.valueOf(memoryG)});
        }
        return String.format("%.1fG", new Object[]{Float.valueOf(memoryG)});
    }
}
