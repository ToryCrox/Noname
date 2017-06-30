/*
 * Copyright (C) 2010 Daniel Nilsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tory.library.colorpicker;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.tory.library.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ColorPickerDialog
        extends
        Dialog
        implements
        OnColorChangedListener,
        View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener, ColorGridAdapter.OnColorClickListener {

    private ColorPickerView mColorPicker;

    private ColorPickerPanelView mOldColor;
    private ColorPickerPanelView mNewColor;

    private EditText mHexVal;
    private boolean mHexValueEnabled = false;
    private ColorStateList mHexDefaultTextColor;

    private OnColorChangedListener mListener;
    private int mOrientation;
    private View mLayout;

    private int mDialogWidth;

    private ArrayList<ColorItem> mColors;

    @Override
    public void onGlobalLayout() {
        if (getContext().getResources().getConfiguration().orientation != mOrientation) {
            final int oldcolor = mOldColor.getColor();
            final int newcolor = mNewColor.getColor();
            mLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            setUp(oldcolor);
            mNewColor.setColor(newcolor);
            mColorPicker.setColor(newcolor);
        }
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    public ColorPickerDialog(Context context, int initialColor) {
        this(context, 0, initialColor);
    }

    public ColorPickerDialog(Context context, int styleRes, int initialColor) {
        this(context, styleRes, initialColor, null);
    }

    public ColorPickerDialog(Context context, int styleRes, int initialColor, ArrayList<ColorItem> colors) {
        super(context, styleRes);

        init(initialColor);
        initColors(colors);
    }

    private void init(int color) {
        // To fight color banding.
        getWindow().setFormat(PixelFormat.RGBA_8888);

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int width = Math.min(dm.widthPixels, dm.heightPixels);
        mDialogWidth = width - dp2px(20,dm) * 2;

        setUp(color);
    }

    private void setUp(int color) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mLayout = inflater.inflate(R.layout.dialog_color_picker, null);
        mLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);

        mOrientation = getContext().getResources().getConfiguration().orientation;
        setContentView(mLayout);

        setTitle(R.string.dialog_color_picker);

        mColorPicker = (ColorPickerView) mLayout.findViewById(R.id.color_picker_view);
        mOldColor = (ColorPickerPanelView) mLayout.findViewById(R.id.old_color_panel);
        mNewColor = (ColorPickerPanelView) mLayout.findViewById(R.id.new_color_panel);

        mHexVal = (EditText) mLayout.findViewById(R.id.hex_val);
        mHexVal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mHexDefaultTextColor = mHexVal.getTextColors();

        mHexVal.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String s = mHexVal.getText().toString();
                    if (s.length() > 5 || s.length() < 10) {
                        try {
                            int c = ColorPickerPreference.convertToColorInt(s.toString());
                            mColorPicker.setColor(c, true);
                            mHexVal.setTextColor(mHexDefaultTextColor);
                        } catch (IllegalArgumentException e) {
                            mHexVal.setTextColor(Color.RED);
                        }
                    } else {
                        mHexVal.setTextColor(Color.RED);
                    }
                    return true;
                }
                return false;
            }
        });

        View cancle =  mLayout.findViewById(R.id.cancel);
        View ok =  mLayout.findViewById(R.id.ok);
        cancle.setOnClickListener(this);
        ok.setOnClickListener(this);
        mColorPicker.setOnColorChangedListener(this);
        mOldColor.setColor(color);
        mColorPicker.setColor(color, true);
    }

    private void initColors(ArrayList<ColorItem> colors) {
        mColors = colors != null ? new ArrayList<>(colors) : null;
        if(mColors == null){
            return;
        }
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int space = dp2px(10, dm);
        int width = mDialogWidth - dp2px(15, dm) * 2 - dp2px(90, dm);
        int cellWidth = (width - space * 3) / 4;

        RecyclerView recyclerView = (RecyclerView) mLayout.findViewById(R.id.recycler_view);

        ColorGridAdapter adapter = new ColorGridAdapter(getContext(), mColors);
        adapter.setColorItemWidth(cellWidth);
        adapter.setOnColorClickListener(this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new SpaceItemDecoration(space));
    }

    @Override
    public void onColorClicked(int color) {
        mColorPicker.setColor(color, true);
    }

    @Override
    public void onColorChanged(int color) {

        mNewColor.setColor(color);

        if (mHexValueEnabled) {
            updateHexValue(color);
        }

    }

    public void setHexValueEnabled(boolean enable) {
        mHexValueEnabled = enable;
        if (enable) {
            mHexVal.setVisibility(View.VISIBLE);
            updateHexLengthFilter();
            updateHexValue(getColor());
        } else
            mHexVal.setVisibility(View.GONE);
    }

    public boolean getHexValueEnabled() {
        return mHexValueEnabled;
    }

    private void updateHexLengthFilter() {
        if (getAlphaSliderVisible())
            mHexVal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        else
            mHexVal.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    private void updateHexValue(int color) {
        String colorText;
        if (getAlphaSliderVisible()) {
            colorText =  ColorPickerPreference.convertToARGB(color).toUpperCase(Locale.getDefault());
        } else {
            colorText = ColorPickerPreference.convertToRGB(color).toUpperCase(Locale.getDefault());
        }
        mHexVal.setText(colorText.substring(1, colorText.length()));
        mHexVal.setTextColor(mHexDefaultTextColor);
    }

    public void setAlphaSliderVisible(boolean visible) {
        mColorPicker.setAlphaSliderVisible(visible);
        if (mHexValueEnabled) {
            updateHexLengthFilter();
            updateHexValue(getColor());
        }
    }

    public boolean getAlphaSliderVisible() {
        return mColorPicker.getAlphaSliderVisible();
    }


    /**
     * Set a OnColorChangedListener to get notified when the color
     * selected by the user has changed.
     *
     * @param listener
     */
    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.ok) {
            if (mOldColor.getColor() != mNewColor.getColor()
                    && mListener != null) {
                mListener.onColorChanged(mNewColor.getColor());
            }

        }
        if(v.getId() == R.id.ok || v.getId() == R.id.cancel){
            dismiss();
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt("old_color", mOldColor.getColor());
        state.putInt("new_color", mNewColor.getColor());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mOldColor.setColor(savedInstanceState.getInt("old_color"));
        mColorPicker.setColor(savedInstanceState.getInt("new_color"), true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = mDialogWidth;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(lp);
    }

    public static int dp2px(float size, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }

    public static class SpaceItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public SpaceItemDecoration(int space) {
            this.space = space;
        }

        @Override public void getItemOffsets(Rect outRect, View view,
                                             RecyclerView parent, RecyclerView.State state) {
            RecyclerView.LayoutManager layout = parent.getLayoutManager();
            int position = parent.getChildAdapterPosition(view);
            if(layout instanceof GridLayoutManager){
                int spanCount = ((GridLayoutManager) layout).getSpanCount();
                int width = (parent.getWidth() - (spanCount - 1) * space) / spanCount;
                if(spanCount < 1) return;
                int row = position / spanCount;
                int col = position % spanCount;
                if(row > 0){
                    outRect.top = space;
                }

            }else if(layout instanceof RecyclerView.LayoutManager){
                outRect.top = space;
            }

        }
    }

}
