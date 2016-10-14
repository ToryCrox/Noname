package com.tory.noname.recycler;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tory.noname.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 * <p>
 * http://www.jianshu.com/p/b1ad50633732
 */
public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {
    protected static final String TAG = BaseRecyclerAdapter.class.getSimpleName();

    public static final int TYPE_ITEM = 0;
    public static final int TYPE_HEADER = -0x101;
    public static final int TYPE_LOADING = -0x102;
    public static final int TYPE_FOOTER = -0x103;
    public static final int TYPE_EMPTY = -0x104;

    protected View mHeader;
    protected View mFooter;

    protected List<T> mData;
    private int mLayoutResId;

    protected int mFooterResId;
    protected boolean mHasFooter = false;

    OnRecyclerViewItemClickListener mOnItemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View v, int position);
    }

    OnRecyclerViewItemLongClickListener mOnItemLongClickListener;

    public interface OnRecyclerViewItemLongClickListener {
        boolean onLongClick(View v, int position);
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener mItemLongClickListener) {
        mOnItemLongClickListener = mItemLongClickListener;
    }

    public BaseRecyclerAdapter(@LayoutRes int layoutResId) {
        this(layoutResId, null);
    }

    public BaseRecyclerAdapter(@LayoutRes int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder holder = null;
        if (viewType == TYPE_FOOTER) {
            holder = new BaseViewHolder(getFooterView(parent));
        } else {
            holder = onCreateDefViewHolder(parent, viewType);
        }
        return holder;
    }

    private BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        int layoutResId = getLayoutId(viewType);
        final BaseViewHolder holder = new BaseViewHolder(getItemView(layoutResId, parent));
        if (mOnItemClickListener != null) {
            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getView(), holder.getLayoutPosition());
                }
            });
        }
        if (mOnItemLongClickListener != null) {
            holder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return mOnItemLongClickListener.onLongClick(holder.getView(), holder.getLayoutPosition());
                }
            });
        }
        return holder;
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return layoutInflater.inflate(layoutResId, parent, false);
    }

    @LayoutRes
    protected int getLayoutId(int viewType) {
        return mLayoutResId;
    }

    private View getFooterView(ViewGroup parent) {
        if (mHasFooter && parent != null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int resId = mFooterResId != 0 ? mFooterResId : R.layout.item_loading_footer_view;
            return inflater.inflate(resId, parent, false);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        int viewType = holder.getItemViewType();
        if (viewType != TYPE_HEADER && viewType != TYPE_FOOTER) {
            convert(holder, mData.get(position));
        }
    }

    protected abstract void convert(BaseViewHolder holder, T item);

    @Override
    public int getItemCount() {
        int dataSize = mData != null ? mData.size() : 0;
        int count = dataSize + getFooterCount();
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        int itemCount = getItemCount();
        if (mHasFooter && position == itemCount - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }


    public int getFooterCount() {
        return mHasFooter ? 1 : 0;
    }

    public void addFooterView() {
        mHasFooter = true;
    }

    public void addFooterView(@LayoutRes int footerResId) {
        mFooterResId = footerResId;
        mHasFooter = true;
    }

    public View getHeaderView() {
        return mHeader;
    }

    public View getFooterView() {
        return mFooter;
    }

    public void addHeaderView(View header) {
        if (hasHeaderView())
            throw new IllegalStateException("You have already added a header view.");
        mHeader = header;
        setLayoutParams(mHeader);

        notifyItemInserted(0);
    }

    public void addFooterView(View footer) {
        if (hasFooterView())
            throw new IllegalStateException("You have already added a footer view.");
        mFooter = footer;
        setLayoutParams(mFooter);
        notifyItemInserted(getItemCount() - 1);
    }

    private void setLayoutParams(View view) {
        view.setLayoutParams(new ViewGroup.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT));
    }


    public boolean removeHeaderView() {
        if (hasHeaderView()) {
            mHeader = null;
            notifyItemRemoved(0);
            return true;
        }
        return false;
    }

    public boolean removeFooterView() {
        if (hasFooterView()) {
            int footerPosition = getItemCount() - 1;
            mFooter = null;
            notifyItemRemoved(footerPosition);
            return true;
        }
        return false;
    }

    public boolean hasHeaderView() {
        return getHeaderView() != null;
    }

    public boolean hasFooterView() {
        return getFooterView() != null;
    }

    public boolean isHeaderView(int position) {
        return hasHeaderView() && position == 0;
    }

    public boolean isFooterView(int position) {
        return hasFooterView() && position == getItemCount() - 1;
    }


    public void add(T bean) {
        mData.add(bean);
        notifyDataSetChanged();
    }

    public void addAll(@NonNull List<T> beans) {
        mData.addAll(beans);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return position < mData.size() && position >= 0 ? mData.get(position) : null;
    }
}
