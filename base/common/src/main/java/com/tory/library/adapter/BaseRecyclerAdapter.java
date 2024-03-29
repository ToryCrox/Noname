package com.tory.library.adapter;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter {

    protected List<T> mData;

    public BaseRecyclerAdapter(List<T> data) {
        mData = data == null ? new ArrayList<>() : data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(), null);
        return getViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        bindData((VH) holder, mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    protected abstract VH getViewHolder(View itemView, int viewType);

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract void bindData(VH holder, T data);

}