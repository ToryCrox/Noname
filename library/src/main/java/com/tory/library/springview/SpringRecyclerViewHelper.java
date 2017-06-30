package com.tory.library.springview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tao.xu2 on 2017/6/15.
 */

public class SpringRecyclerViewHelper extends SpringViewHelper {

    private RecyclerView mView;

    public SpringRecyclerViewHelper(@NonNull RecyclerView view) {
        super(view);
        mView = view;
    }

    @Override
    public void init(AttributeSet attrs, int defStyleAttr) {
        mView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        mView.setWillNotDraw(false);
        super.init(attrs, defStyleAttr);
    }
}
