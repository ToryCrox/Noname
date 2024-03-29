package com.tory.library.adapter.binder;

import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tory.library.R;
import com.tory.library.adapter.BaseLoadFailedBinder;
import com.tory.library.adapter.BaseViewHolder;

/**
 * Created by miserydx on 17/12/20.
 */

public class DefaultLoadFailedBinder extends BaseLoadFailedBinder<DefaultLoadFailedBinder.LoadFailedHolder> {

    @NonNull
    @Override
    protected LoadFailedHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(R.layout.common_item_load_failed, parent, false);
        return new LoadFailedHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(@NonNull LoadFailedHolder holder) {
        holder.tvLoadFailed.setText(holder.tvLoadFailed.getContext().getText(R.string.load_failed));
    }

    class LoadFailedHolder extends BaseViewHolder {

        TextView tvLoadFailed;

        public LoadFailedHolder(View itemView) {
            super(itemView);
            tvLoadFailed = itemView.findViewById(R.id.tv_load_failed);
        }
    }

}
