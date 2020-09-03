package com.tory.library.component.loadmore.paginate;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tory.library.R;
import com.tory.library.component.loadmore.paginate.recycler.LoadingListItemCreator;

;


/**
 * Author:    valuesfeng
 * Version    V1.0
 * Date:      15/11/23
 * Description:
 * Modification  History:
 * Date         	Author        		Version        	Description
 * -----------------------------------------------------------------------------------
 * 15/11/23          valuesfeng              1.0                    1.0
 * Why & What is modified:
 * 分页加载更多
 */
public class RecyclerViewLoadMoreCreator implements LoadingListItemCreator {

    private TextView tvLoadMsg;
    private ImageView ivLoading;


    private boolean notLoadMore;
    private String msg;

    private AnimationDrawable frameAnim;

    public void setLoadMoreMessage(String msg) {
        this.msg = msg;
        if (tvLoadMsg == null || TextUtils.isEmpty(msg)) {
            return;
        }
        tvLoadMsg.setText(msg);
    }

    public void setNotLoadMore(boolean notLoadMore) {
        this.notLoadMore = notLoadMore;
        if (tvLoadMsg == null || ivLoading == null) {
            return;
        }
        if (notLoadMore) {
            ivLoading.setVisibility(View.GONE);
            frameAnim.stop();
            tvLoadMsg.setText("期待更多...");
        } else {
            ivLoading.setVisibility(View.VISIBLE);
            frameAnim.start();
            tvLoadMsg.setText("加载中");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_item_load_more, parent, false);
        setNotLoadMore(notLoadMore);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // No binding for default loading row
    }
}
