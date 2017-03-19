package com.tory.noname.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tory.noname.R;

/**
 * @Author: tory
 * Create: 2017/3/19
 * Update: ${UPDATE}
 */
public class ExpendDialog extends Dialog {

    FrameLayout mTopContent;
    ViewGroup mContent;
    TextView mMsg;
    TextView mSubMsg;

    public ExpendDialog(Context context) {
        this(context,0);
    }

    public ExpendDialog(Context context, int themeResId) {
        super(context, themeResId);

        setContentView(R.layout.dialog_shrink);
        initView();
    }

    private void initView() {
        mTopContent = (FrameLayout) findViewById(R.id.top_content);
        mTopContent.setVisibility(View.GONE);
        mContent = (ViewGroup) findViewById(R.id.content);
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopContent.setVisibility(View.VISIBLE);
            }
        });
    }
}
