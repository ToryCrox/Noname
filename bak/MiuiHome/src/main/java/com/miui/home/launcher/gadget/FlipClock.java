package com.miui.home.launcher.gadget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.miui.home.R;
import com.miui.home.launcher.gadget.Clock.ClockStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import miui.maml.ScreenContext;
import miui.maml.util.ZipResourceLoader;

public class FlipClock extends Gadget implements ClockStyle {
    private AwesomeView mBottomBar;
    private FrameLayout mContainer;
    private ScreenContext mElementContext;
    private int mLastHour = -1;
    private int mLastMin = -1;
    PageCache mPageCache = new PageCache();
    private ViewList mViewList = new ViewList();

    private class PageCache {
        LinkedList<View> mPages;

        private PageCache() {
            this.mPages = new LinkedList();
        }

        public void put(ViewList vl) {
            Iterator i$ = vl.iterator();
            while (i$.hasNext()) {
                this.mPages.add((View) i$.next());
            }
        }

        public View get(int pageType) {
            Iterator i$ = this.mPages.iterator();
            while (i$.hasNext()) {
                View v = (View) i$.next();
                if (v.getTag().equals(Integer.valueOf(pageType))) {
                    this.mPages.remove(v);
                    return v;
                }
            }
            return null;
        }
    }

    private class ViewList extends ArrayList<View> {
        private ViewList() {
        }
    }

    public static int modAdd(int a, int b, int mod) {
        return (a + b) % mod;
    }

    public static int modSub(int a, int b, int mod) {
        return a - b < 0 ? (a - b) + mod : a - b;
    }

    public FlipClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mBottomBar != null) {
            this.mBottomBar.layout(this.mContainer.getLeft(), this.mContainer.getBottom(), getWidth(), getHeight());
        }
    }

    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        if (this.mBottomBar != null) {
            this.mBottomBar.invalidate();
        }
        return super.invalidateChildInParent(location, dirty);
    }

    public void initConfig(String config) {
        this.mElementContext = new ScreenContext(this.mContext, new ZipResourceLoader(config));
    }

    public int getUpdateInterval() {
        int awesomeInterval = this.mBottomBar.getUpdateInterval();
        if (awesomeInterval <= 0 || awesomeInterval >= 60000) {
            return 60000;
        }
        return this.mBottomBar.getUpdateInterval();
    }

    public boolean is24HourFormat() {
        return true;
    }

    public void updateAppearance(Calendar calendar) {
        int h = calendar.get(is24HourFormat() ? 11 : 10);
        int m = calendar.get(12);
        if (!(h == this.mLastHour && m == this.mLastMin)) {
            if (this.mLastHour < 0) {
                this.mLastHour = 0;
            }
            if (this.mLastMin < 0) {
                this.mLastMin = 0;
            }
            resetContainer();
            genCurrentViewList(h, m, this.mLastHour, this.mLastMin);
            this.mLastHour = h;
            this.mLastMin = m;
            Iterator i$ = this.mViewList.iterator();
            while (i$.hasNext()) {
                this.mContainer.addView((View) i$.next());
            }
        }
        if (this.mElementContext != null && this.mBottomBar != null) {
            this.mBottomBar.tick(SystemClock.elapsedRealtime());
            this.mBottomBar.invalidate();
        }
    }

    private void resetContainer() {
        Iterator i$ = this.mViewList.iterator();
        while (i$.hasNext()) {
            ((View) i$.next()).clearAnimation();
        }
        this.mContainer.removeAllViewsInLayout();
        this.mPageCache.put(this.mViewList);
        this.mViewList.clear();
    }

    private void genCurrentViewList(int h, int m, int startHour, int startMin) {
        int i;
        int lh = startHour;
        int lm = startMin;
        if (modSub(h, startHour, 24) > 1) {
            lh = modSub(h, 1, 24);
        }
        if (modSub(m, startMin, 60) > 2) {
            lm = modSub(m, 2, 60);
        }
        int delta = lh != startHour ? 1 : 0;
        int count = modSub(h, lh, 24);
        int dist = count + delta;
        for (i = count; i >= 0; i--) {
            this.mViewList.add(getPagePart(0, modAdd(lh, i, 24), dist, i + delta));
        }
        if (delta != 0) {
            this.mViewList.add(getPagePart(0, startHour, dist, 0));
            this.mViewList.add(getPagePart(1, startHour, dist, 0));
        }
        for (i = 0; i <= count; i++) {
            this.mViewList.add(getPagePart(1, modAdd(lh, i, 24), dist, i + delta));
        }
        delta = lm != startMin ? 1 : 0;
        count = modSub(m, lm, 60);
        dist = delta + count;
        for (i = count; i >= 0; i--) {
            this.mViewList.add(getPagePart(2, modAdd(lm, i, 60), dist, i + delta));
        }
        if (delta != 0) {
            this.mViewList.add(getPagePart(2, startMin, dist, 0));
            this.mViewList.add(getPagePart(3, startMin, dist, 0));
        }
        for (i = 0; i <= count; i++) {
            this.mViewList.add(getPagePart(3, modAdd(lm, i, 60), dist, i + delta));
        }
    }

    private FlipPage getPagePart(int pageType, int number, int dist, int offset) {
        int resID = -1;
        int flipDelay = 0;
        FlipPage page = null;
        switch (pageType) {
            case 0:
                resID = R.layout.gadget_flipclock_page_lu;
                flipDelay = 180;
                break;
            case 1:
                flipDelay = 180;
                resID = R.layout.gadget_flipclock_page_ld;
                break;
            case 2:
                flipDelay = 120;
                resID = R.layout.gadget_flipclock_page_ru;
                break;
            case 3:
                flipDelay = 120;
                resID = R.layout.gadget_flipclock_page_rd;
                break;
        }
        if (resID != -1) {
            page = (FlipPage) this.mPageCache.get(pageType);
            if (page == null) {
                page = (FlipPage) LayoutInflater.from(getContext()).inflate(resID, this.mContainer, false);
                page.setTag(Integer.valueOf(pageType));
            }
            page.init(this.mElementContext.mResourceManager, pageType, number, dist, offset, flipDelay);
        }
        return page;
    }

    public void onResume() {
        this.mBottomBar.resume();
    }

    public void onPause() {
        this.mBottomBar.pause();
    }

    public void onDestroy() {
        this.mBottomBar.finish();
        if (this.mElementContext != null) {
            this.mElementContext.mResourceManager.finish(false);
        }
    }

    public void updateConfig(Bundle config) {
    }

    public void onCreate() {
        this.mBottomBar = (AwesomeView) findViewById(R.id.bottom_bar);
        this.mBottomBar.setVisibility(0);
        this.mBottomBar.setTargetDensity(getDisplayMetrics().densityDpi);
        this.mBottomBar.load(this.mElementContext);
        this.mContainer = (FrameLayout) findViewById(R.id.container);
        this.mContainer.setBackground(this.mElementContext.mResourceManager.getDrawable(getResources(), "flip_bg.9.png"));
        prepairLayout();
    }

    private void prepairLayout() {
        LayoutParams lp = (LayoutParams) getLayoutParams();
        switch (((GadgetInfo) getTag()).getGadgetId()) {
            case 4:
                lp.setMargins(0, 0, 0, 0);
                setLayoutParams(lp);
                lp = (LayoutParams) this.mContainer.getLayoutParams();
                lp.gravity = 17;
                this.mContainer.setLayoutParams(lp);
                return;
            default:
                lp = (LayoutParams) this.mContainer.getLayoutParams();
                lp.gravity = 49;
                this.mContainer.setLayoutParams(lp);
                return;
        }
    }

    private DisplayMetrics getDisplayMetrics() {
        Display display = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay();
        DisplayMetrics me = new DisplayMetrics();
        display.getMetrics(me);
        return me;
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public void onEditDisable() {
    }

    public void onEditNormal() {
    }

    public void onAdded() {
    }

    public void onDeleted() {
    }
}
