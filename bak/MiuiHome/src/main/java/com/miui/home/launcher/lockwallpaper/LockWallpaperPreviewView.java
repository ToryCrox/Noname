package com.miui.home.launcher.lockwallpaper;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.ViewPager.PageTransformer;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import com.miui.home.R;

public class LockWallpaperPreviewView extends FrameLayout {
    private ActionMenus mActionMenus;
    private LockWallpaperPreviewAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                ((Activity) LockWallpaperPreviewView.this.getContext()).finish();
            }
        }
    };
    private boolean mHasShowHint;
    private boolean mInExit;
    private int mLastDragValue = 0;
    private LoadingContainer mLoadingView;
    private View mMask;
    ViewConfiguration mViewConfiguration;
    private CustomViewPager mViewPager;

    public interface OnPageChangeListener extends android.support.v4.view.ViewPager.OnPageChangeListener {
    }

    public LockWallpaperPreviewView(Context context) {
        super(context);
    }

    public LockWallpaperPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockWallpaperPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mViewConfiguration = ViewConfiguration.get(this.mContext);
        this.mViewPager = new CustomViewPager(this.mContext);
        this.mViewPager.setOffscreenPageLimit(2);
        this.mViewPager.setMainView(this);
        addView(this.mViewPager, 0);
        this.mActionMenus = (ActionMenus) findViewById(R.id.menu);
        this.mActionMenus.setMainView(this);
        this.mLoadingView = (LoadingContainer) findViewById(R.id.loading_container);
        this.mMask = findViewById(R.id.mask);
        this.mViewPager.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0 || event.getAction() == 2) {
                    return LockWallpaperPreviewView.this.mInExit;
                }
                return false;
            }
        });
        setOnPageChangeListener(null);
    }

    public void setAdapter(LockWallpaperPreviewAdapter adapter) {
        this.mAdapter = adapter;
        this.mAdapter.setViewPager(this);
        this.mViewPager.setAdapter(this.mAdapter);
        this.mViewPager.setPageTransformer(true, new PageTransformer() {
            public void transformPage(View view, float position) {
                int pageWidth = view.getWidth();
                View wallpaper = view.findViewById(R.id.wallpaper);
                if (position < -1.0f) {
                    wallpaper.setTranslationX(0.0f);
                    view.setTranslationX(0.0f);
                } else if (position <= 1.0f) {
                    wallpaper.setTranslationX(((float) pageWidth) * getFactor(position));
                    view.setTranslationX(8.0f * position);
                } else {
                    wallpaper.setTranslationX(0.0f);
                    view.setTranslationX(0.0f);
                }
                if (LockWallpaperPreviewView.this.mHasShowHint && !LockWallpaperPreviewView.this.mInExit) {
                    LockWallpaperPreviewView.this.mAdapter.transformPage(view, position);
                }
            }

            private float getFactor(float position) {
                return (-position) / 2.0f;
            }
        });
        this.mActionMenus.updateLikeView();
    }

    public void setOnPageChangeListener(final OnPageChangeListener listener) {
        this.mViewPager.setOnPageChangeListener(new android.support.v4.view.ViewPager.OnPageChangeListener() {
            public void onPageSelected(int position) {
                LockWallpaperPreviewView.this.mAdapter.recordEvent(position % LockWallpaperPreviewView.this.mAdapter.getSize(), 1);
                LockWallpaperPreviewView.this.mActionMenus.updateLikeView();
                if (listener != null) {
                    listener.onPageSelected(position);
                }
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (listener != null) {
                    listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            public void onPageScrollStateChanged(int state) {
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        });
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void startExitAnim() {
        this.mInExit = true;
        View view = this.mAdapter.getView(getCurrentItem());
        if (view != null) {
            view.findViewById(R.id.click_area).animate().alpha(0.0f).setDuration(500).setStartDelay(100).setListener(null).start();
        }
    }

    public void showHint() {
        this.mActionMenus.show(true);
        View view = this.mAdapter.getView(getCurrentItem());
        if (view != null) {
            view.findViewById(R.id.click_area).animate().alpha(1.0f).setDuration(500).setStartDelay(100).setListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    LockWallpaperPreviewView.this.showScrollHint();
                }

                public void onAnimationCancel(Animator animation) {
                }
            }).start();
        } else {
            showScrollHint();
        }
    }

    private void showScrollHint() {
        if (!this.mInExit) {
            this.mHasShowHint = true;
            ValueAnimator animator = ValueAnimator.ofInt(new int[]{0, (int) (-30.0f * this.mContext.getResources().getDisplayMetrics().density)});
            animator.setDuration(500);
            animator.addListener(new AnimatorListener() {
                public void onAnimationStart(Animator animation) {
                    LockWallpaperPreviewView.this.mLastDragValue = 0;
                    LockWallpaperPreviewView.this.mViewPager.beginFakeDrag();
                }

                public void onAnimationRepeat(Animator animation) {
                }

                public void onAnimationEnd(Animator animation) {
                    if (LockWallpaperPreviewView.this.mViewPager.isFakeDragging()) {
                        LockWallpaperPreviewView.this.mViewPager.endFakeDrag();
                    }
                }

                public void onAnimationCancel(Animator animation) {
                }
            });
            animator.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (LockWallpaperPreviewView.this.mViewPager.isFakeDragging()) {
                        int value = ((Integer) animation.getAnimatedValue()).intValue();
                        LockWallpaperPreviewView.this.mViewPager.fakeDragBy((float) (value - LockWallpaperPreviewView.this.mLastDragValue));
                        LockWallpaperPreviewView.this.mLastDragValue = value;
                    }
                }
            });
            animator.start();
        }
    }

    public int getCurrentItem() {
        return this.mViewPager.getCurrentItem() % this.mAdapter.getSize();
    }

    public LockWallpaperPreviewAdapter getAdapter() {
        return this.mAdapter;
    }

    public View getLoadingView() {
        return this.mLoadingView;
    }

    public void showMask() {
        this.mMask.animate().alpha(1.0f).start();
    }

    public void hideMask() {
        this.mMask.animate().alpha(0.0f).start();
    }

    public void toggleMenus() {
        this.mActionMenus.toggle();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.mViewPager.isFakeDragging()) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
