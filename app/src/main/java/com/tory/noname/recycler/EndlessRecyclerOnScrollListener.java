package com.tory.noname.recycler;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = false;
    //list到达 最后一个item的时候 触发加载
    private int visibleThreshold = 1;
    // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;
    //默认第一页
    private int currentPage = 1;
 
    private LinearLayoutManager mLinearLayoutManager;
 
    public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }
 
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
 
        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
 
        //判断加载完成了...
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && totalItemCount > visibleItemCount
                &&(totalItemCount - visibleItemCount) < (firstVisibleItem + visibleThreshold)) {
            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }
    }
 
    public abstract void onLoadMore(int current_page);
}