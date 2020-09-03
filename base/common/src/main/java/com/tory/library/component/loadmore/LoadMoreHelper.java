package com.tory.library.component.loadmore;

import android.text.TextUtils;

import androidx.recyclerview.widget.RecyclerView;

import com.tory.library.component.loadmore.paginate.Paginate;
import com.tory.library.component.loadmore.paginate.RecyclerViewLoadMoreCreator;

import java.util.List;

public class LoadMoreHelper implements Paginate.Callbacks {

    private LoadMoreListener loadMoreListener;
    private int threshold = 3;
    private Paginate aginate;
    private boolean notLoadMore = true;
    private RecyclerViewLoadMoreCreator loadMoreCreator;


    private LoadMoreHelper() {
    }

    public static LoadMoreHelper newInstance(LoadMoreListener loadMoreListener) {
        return newInstance(loadMoreListener, 3);
    }

    /**
     * @param loadMoreListener loadmore
     * @param threshold        倒数第几个开始加载更多
     * @return
     */
    public static LoadMoreHelper newInstance(LoadMoreListener loadMoreListener, int threshold) {
        LoadMoreHelper paginateHelper = new LoadMoreHelper();
        paginateHelper.loadMoreListener = loadMoreListener;
        paginateHelper.threshold = threshold;
        return paginateHelper;
    }

    public void initLoadMoreView(RecyclerView recyclerView) {
        loadMoreCreator = new RecyclerViewLoadMoreCreator();
        aginate = Paginate.with(recyclerView, this)
                .setLoadingTriggerThreshold(threshold)
                .setLoadingListItemCreator(loadMoreCreator)
                .addLoadingListItem(true)
                .build();
        aginate.setHasMoreDataToLoad(false);
    }

    @Override
    public final void onLoadMore() {
        if (!isLoading() && loadMoreListener != null) {
            notLoadMore = true;
            loadMoreListener.loadData(false);
        }
    }

    @Override
    public final boolean isLoading() {
        return notLoadMore;
    }

    @Override
    public final boolean hasLoadedAllItems() {
        return notLoadMore;
    }

    public void onDestroy() {
        if (aginate != null) {
            aginate.unbind();
            aginate = null;
        }
    }

    public boolean hasMoreDataToLoad(String lastId) {
        if (aginate == null) {
            return false;
        }
        notLoadMore = TextUtils.isEmpty(lastId);
        if (notLoadMore) { //如果已经没有更多了
            loadMoreCreator.setNotLoadMore(true);
        } else {
            loadMoreCreator.setNotLoadMore(false);
            aginate.setHasMoreDataToLoad(!notLoadMore);
        }
        return !notLoadMore;
    }

    public boolean hasMoreDataToLoad(List<?> list) {
        if (aginate == null) {
            return false;
        }
        notLoadMore = list == null || list.isEmpty();
        if (notLoadMore) { //如果已经没有更多了
            loadMoreCreator.setNotLoadMore(true);
        } else {
            loadMoreCreator.setNotLoadMore(false);
            aginate.setHasMoreDataToLoad(!notLoadMore);
        }
        return !notLoadMore;
    }

    public void hintProgress() {
        loadMoreCreator.setNotLoadMore(true);
    }

    //加载更多出现次数
    public int getLoadMoreAppearTimes(){
        return aginate.getLoadMoreAppearTimes();
    }

    public void resetLoadMoreTimes(int i){
        aginate.resetLoadMoreAppearTimes(i);
    }

    public void stopLoadMore() {
        notLoadMore = true;
        aginate.setHasMoreDataToLoad(false);
    }

    public interface LoadMoreListener {
        void loadData(boolean isRefresh);
    }
}
