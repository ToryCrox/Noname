package com.tory.library.component.loadmore.paginate.recycler;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;


public class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE_LOADING = Integer.MAX_VALUE - 50; // Magic

    private final RecyclerView.Adapter wrappedAdapter;
    private final LoadingListItemCreator loadingListItemCreator;
    private boolean displayLoadingRow = true;
    private int loadMoreAppearTimes = 0; //加载更多view出现次数

    public WrapperAdapter(RecyclerView.Adapter adapter, LoadingListItemCreator creator) {
        this.wrappedAdapter = adapter;
        this.loadingListItemCreator = creator;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {//只执行一次
        super.onAttachedToRecyclerView(recyclerView);
        wrappedAdapter.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridManager.getSpanSizeLookup();
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {//设置监听回调只需要设置一次
                @Override
                public int getSpanSize(int position) {//SpanSize 代表占几列
                    return (isLoadingRow(position)//加载更多
                            ? gridManager.getSpanCount() : spanSizeLookup.getSpanSize(position));
                }
            });
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        try {
            wrappedAdapter.onViewDetachedFromWindow(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        wrappedAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {//多次执行
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null
                && lp instanceof StaggeredGridLayoutManager.LayoutParams
                && (isLoadingRow(holder.getLayoutPosition()))) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);//头部或者尾部占满全行或者全列
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_LOADING) {
            return loadingListItemCreator.onCreateViewHolder(parent, viewType);
        } else {
            return wrappedAdapter.onCreateViewHolder(parent, viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (isLoadingRow(position)) {
            loadMoreAppearTimes++;
            loadingListItemCreator.onBindViewHolder(holder, position);
        } else {
            wrappedAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (isLoadingRow(position)) {
            loadMoreAppearTimes++;
            loadingListItemCreator.onBindViewHolder(holder, position);
        } else {
            wrappedAdapter.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        int itemCount = wrappedAdapter.getItemCount();
        if (displayLoadingRow) {
            itemCount++;
        }
        return itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingRow(position)) {
            return ITEM_VIEW_TYPE_LOADING;
        } else {
            return wrappedAdapter.getItemViewType(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return isLoadingRow(position) ? RecyclerView.NO_ID : wrappedAdapter.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
        wrappedAdapter.setHasStableIds(hasStableIds);
    }

    public RecyclerView.Adapter getWrappedAdapter() {
        return wrappedAdapter;
    }

    public int getLoadMoreAppearTimes() {
        return loadMoreAppearTimes;
    }

    public void resetLoadMoreAppearTimes(int i) {
        this.loadMoreAppearTimes = i;
    }

    boolean isDisplayLoadingRow() {
        return displayLoadingRow;
    }

    void displayLoadingRow(boolean displayLoadingRow) {
        if (this.displayLoadingRow != displayLoadingRow) {
            this.displayLoadingRow = displayLoadingRow;
//            notifyDataSetChanged();
            if (!displayLoadingRow) {
                if (getItemCount() > 0) {
                    notifyItemRemoved(getItemCount());
                }
                return;
            }
            if (getItemCount() - 1 >= 0) {
                try {
                    notifyItemRangeChanged(getItemCount() - 1, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                    notifyDataSetChanged();
                }
            } else {
                notifyDataSetChanged();
            }
        }
    }

    boolean isLoadingRow(int position) {
        return displayLoadingRow && position == getLoadingRowPosition();
    }

    private int getLoadingRowPosition() {
        return displayLoadingRow ? getItemCount() - 1 : -1;
    }

}
