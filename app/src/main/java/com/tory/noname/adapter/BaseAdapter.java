package com.tory.noname.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Tory
 * Create: 2016/9/11
 * Update: 2016/9/11
 *
 * http://www.jianshu.com/p/b1ad50633732
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder>{

    protected List<T> mData;
    private int mLayoutResId;
    OnRecyclerViewItemClickListener mOnItemClickListener;
    public interface OnRecyclerViewItemClickListener{
        void onItemClick(View v,int position);
    }

    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onItemClickListener){
        mOnItemClickListener = onItemClickListener;
    }

    public BaseAdapter(int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final BaseViewHolder holder = onCreateDefViewHolder(parent, viewType);
        if(mOnItemClickListener != null){
            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder.getView(),holder.getLayoutPosition());
                }
            });
        }
        return holder;
    }

    private BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return new BaseViewHolder(getItemView(mLayoutResId, parent));
    }

    private View getItemView(int layoutResId, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return layoutInflater.inflate(layoutResId,parent,false);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        convert(holder, mData.get(position));
    }

    protected abstract void convert(BaseViewHolder helper, T item);

    @Override
    public int getItemCount() {
        return mData.size();
    }


}
