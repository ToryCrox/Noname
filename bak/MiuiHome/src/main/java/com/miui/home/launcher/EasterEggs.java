package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.miui.home.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

public class EasterEggs implements OnTouchListener {
    private int mBackCounter = 0;
    private int mBackToActive = -1;
    private int mClickToActive = -1;
    private ImageView mContent;
    private Context mContext;
    private boolean mDeactived = false;
    private FrameLayout mEggView;
    private long mEndTime;
    private ViewGroup mOwner;
    private long mStartTime;
    private ArrayList<View> mViewList;

    private EasterEggs(Context context, long startTime, long endTime, ViewGroup owner) {
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mOwner = owner;
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        this.mClickToActive = random.nextInt(5) + 5;
        this.mBackToActive = random.nextInt(10) + 10;
        this.mViewList = new ArrayList();
        this.mContext = context;
    }

    public static EasterEggs init(Context context, ViewGroup owner) {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_key_fools_day_activied", false)) {
                Date foolsEnd = dateFormater.parse("2013-04-02 00:00:00");
                if (System.currentTimeMillis() < foolsEnd.getTime()) {
                    return new EasterEggs(context, dateFormater.parse("2013-04-01 10:00:00").getTime(), foolsEnd.getTime(), owner);
                }
            }
        } catch (ParseException e) {
        }
        return null;
    }

    public void onStart() {
        if (!this.mDeactived) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= this.mStartTime && currentTime < this.mEndTime) {
                this.mBackCounter++;
            }
        }
    }

    public void onStop() {
        if (!this.mDeactived) {
            clear();
        }
    }

    public boolean onClick(View v) {
        if (this.mDeactived || this.mBackCounter <= this.mBackToActive) {
            return false;
        }
        if (this.mEggView == null && this.mViewList.size() > this.mClickToActive) {
            showEggs();
        }
        this.mViewList.add(v);
        v.animate().alpha(0.0f);
        return true;
    }

    private void clear() {
        Iterator i$ = this.mViewList.iterator();
        while (i$.hasNext()) {
            ((View) i$.next()).animate().alpha(1.0f);
        }
        this.mViewList.clear();
    }

    private void showEggs() {
        Launcher launcher = LauncherApplication.getLauncher(this.mContext);
        if (launcher != null) {
            final Drawable bg = launcher.getBlurScreenShot(false);
            this.mEggView = (FrameLayout) LayoutInflater.from(this.mContext).inflate(R.layout.easter_eggs, null);
            this.mEggView.setOnTouchListener(this);
            this.mOwner.addView(this.mEggView);
            this.mEggView.setBackground(bg);
            this.mContent = (ImageView) this.mEggView.findViewById(R.id.content);
            bg.setAlpha(0);
            this.mContent.setAlpha(0.0f);
            ValueAnimator animator = new ValueAnimator();
            animator.setInterpolator(new OvershootInterpolator());
            animator.setDuration(1000);
            animator.setFloatValues(new float[]{0.0f, 3.0f});
            animator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    EasterEggs.this.mEggView.postDelayed(new Runnable() {
                        public void run() {
                            EasterEggs.this.deactive();
                        }
                    }, 5000);
                }
            });
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = ((Float) animation.getAnimatedValue()).floatValue();
                    if (value < 1.0f) {
                        bg.setAlpha((int) (255.0f * value));
                        return;
                    }
                    bg.setAlpha(255);
                    EasterEggs.this.mContent.setAlpha((value - 1.0f) / 2.0f);
                    float scale = 0.5f + (((value - 1.0f) * 0.5f) / 2.0f);
                    EasterEggs.this.mContent.setScaleX(scale);
                    EasterEggs.this.mContent.setScaleY(scale);
                }
            });
            animator.start();
        }
    }

    private void deactive() {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(500);
        animator.setFloatValues(new float[]{2.0f, 0.0f});
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                EasterEggs.this.mOwner.removeView(EasterEggs.this.mEggView);
                EasterEggs.this.mDeactived = true;
                PreferenceManager.getDefaultSharedPreferences(EasterEggs.this.mContext).edit().putBoolean("pref_key_fools_day_activied", true).commit();
                EasterEggs.this.clear();
            }
        });
        animator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = ((Float) animation.getAnimatedValue()).floatValue();
                if (value < 1.0f) {
                    EasterEggs.this.mEggView.getBackground().setAlpha((int) (255.0f * value));
                    return;
                }
                EasterEggs.this.mEggView.getBackground().setAlpha(255);
                EasterEggs.this.mContent.setAlpha(value - 1.0f);
                float scale = 0.5f + ((value - 1.0f) * 0.5f);
                EasterEggs.this.mContent.setScaleX(scale);
                EasterEggs.this.mContent.setScaleY(scale);
            }
        });
        animator.start();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }
}
