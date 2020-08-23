package com.tory.library.colorpicker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tory.library.R;
import com.tory.library.recycler.BaseViewHolder;

import java.util.ArrayList;

/**
 * Created by tao.xu2 on 2017/5/17.
 */

public class ColorGridAdapter extends RecyclerView.Adapter<BaseViewHolder>{


    private ArrayList<ColorItem> mItems;
    private Context context;
    private LayoutInflater mInflater;
    private int pickedColorIndex = -1;
    private int colorItemWidth;

    private OnColorClickListener mOnColorClickListener;
    private int pickedColor;

    public ColorGridAdapter(@NonNull Context context, ArrayList<ColorItem> items) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mItems = items != null ? new ArrayList<>(items) : new ArrayList<ColorItem>();
    }

    public void setColorItemWidth(int width){
        this.colorItemWidth = width;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final BaseViewHolder holder;
        final View view = mInflater.inflate(R.layout.item_color_picker_grid, parent, false);
        if(colorItemWidth > 0){
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if(lp != null){
                lp.width = colorItemWidth;
                view.setLayoutParams(lp);
            }
        }

        holder = new BaseViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick(holder.itemView, holder.getLayoutPosition());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        ColorItem item = mItems.get(position);
        if (item.colorDrawable == null) {
            item.colorDrawable = createColorDrawable(item.color);
        }
        ImageView image = holder.getView(R.id.image_color);
        image.setBackground(item.colorDrawable);
        boolean selected = position == pickedColorIndex;
        image.setSelected(selected);
    }

    public void onItemClick(View itemView, int position) {
        pickedColorIndex = position;
        ColorItem item = mItems.get(position);
        if (mOnColorClickListener != null) {
            mOnColorClickListener.onColorClicked(item.color);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems != null ? mItems.size() : 0;
    }

    public void setCurrentColor(int currentColor) {
        pickedColor = currentColor;
        final int len = mItems != null ? mItems.size() : 0;
        for (int i = 0; i < len; i++) {
            ColorItem item = mItems.get(i);
            if (item.color == currentColor) {
                pickedColorIndex = i;
            }
        }
    }

    public void setOnColorClickListener(OnColorClickListener listener) {
        mOnColorClickListener = listener;
    }

    private Drawable createColorDrawable(@ColorInt int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(color);
        shape.setStroke(1, strokeColor(color));
        return shape;
    }

    private int strokeColor(int color) {
        return Color.rgb(Color.red(color) * 192 / 256, Color.green(color) * 192 / 256,
                Color.blue(color) * 192 / 256);
    }


    public interface OnColorClickListener{
        public void onColorClicked(int color);
    }
}
