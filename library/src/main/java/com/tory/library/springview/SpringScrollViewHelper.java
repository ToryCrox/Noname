package com.tory.library.springview;

import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tao.xu2 on 2017/6/15.
 */

public class SpringScrollViewHelper extends SpringViewHelper {

    NestedScrollView mScrollView;

    public SpringScrollViewHelper(@NonNull NestedScrollView view) {
        super(view);
        mScrollView = view;
    }

    @Override
    public void init(AttributeSet attrs, int defStyleAttr) {
        mView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mView.setWillNotDraw(false);
        super.init(attrs, defStyleAttr);
        setOrientation(SpringScrollable.VERTICAL);
    }
}
