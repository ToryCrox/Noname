package com.miui.home.launcher.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.miui.home.R;
import com.miui.home.launcher.ThumbnailViewAdapter;
import com.miui.home.launcher.WallpaperUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import miui.view.EditActionMode;

public class ImageListPreference extends Preference implements OnClickListener {
    private DialogInterface.OnClickListener deleteListener;
    private ActionMode mActionMode;
    private Menu mActionbBarMenu;
    private ThumbnailViewAdapter mAdapter;
    private final OnClickListener mEditModeClickListener;
    private boolean mEnableSelectMode;
    private int mInitPosition;
    private boolean mIsInSelectingAll;
    private boolean mIsInSelectionMode;
    private OnItemClickListener mItemClickListener;
    private int mItemPadding;
    private final OnLongClickListener mLongClickListener;
    private EditModeWrapper mModeCallback;
    private final OnClickListener mNormalClickListener;
    private View mRoot;
    private HashSet<Integer> mSeletedItemInPosition;
    private TextView mShowAllBtn;
    private boolean mShowingAllItems;

    public interface OnItemClickListener {
        void onItemClick(int i);
    }

    class EditModeWrapper implements Callback {
        EditModeWrapper() {
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(String.format(ImageListPreference.this.getContext().getResources().getString(R.string.editing_mode_title), new Object[]{Integer.valueOf(ImageListPreference.this.mSeletedItemInPosition.size())}));
            ImageListPreference.this.mActionMode = mode;
            ImageListPreference.this.mActionbBarMenu = menu;
            mode.getMenuInflater().inflate(R.menu.delete_wallpaper, menu);
            innerEnterEditMode(Integer.valueOf(ImageListPreference.this.mInitPosition));
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (ImageListPreference.this.mModeCallback == null || !ImageListPreference.this.mIsInSelectionMode) {
                return false;
            }
            switch (item.getItemId()) {
                case 16908313:
                    ImageListPreference.this.exitEditMode();
                    break;
                case 16908314:
                    if (ImageListPreference.this.mIsInSelectingAll) {
                        ImageListPreference.this.checkNothing();
                    } else {
                        ImageListPreference.this.checkAll();
                    }
                    ((EditActionMode) mode).setButton(16908314, ImageListPreference.this.mIsInSelectingAll ? R.string.deselect_all : R.string.select_all);
                    break;
                case R.id.action_delete:
                    ImageListPreference.this.confirmDelete(ImageListPreference.this.getContext(), ImageListPreference.this.deleteListener, ImageListPreference.this.buildDeleteMessage());
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            ImageListPreference.this.checkNothing();
            ImageListPreference.this.exitEditMode();
        }

        private void innerEnterEditMode(Integer initPosition) {
            if (!ImageListPreference.this.mIsInSelectionMode) {
                ImageListPreference.this.mIsInSelectionMode = true;
                ImageListPreference.this.selectItem(initPosition.intValue(), true, false);
                ImageListPreference.this.setItemOnClickListener(ImageListPreference.this.mEditModeClickListener);
            }
        }
    }

    public void enableSeletMode(boolean enable) {
        this.mEnableSelectMode = enable;
    }

    private void onDelete() {
        GridLayout grid = getGridView(this.mRoot);
        ArrayList<View> removed = new ArrayList();
        if (this.mSeletedItemInPosition.size() > 0) {
            Iterator i$ = this.mSeletedItemInPosition.iterator();
            while (i$.hasNext()) {
                View child = grid.getChildAt(((Integer) i$.next()).intValue());
                String path = (String) child.getTag();
                if (!TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file != null && file.exists() && file.delete()) {
                        removed.add(child);
                    }
                }
            }
            i$ = removed.iterator();
            while (i$.hasNext()) {
                grid.removeView((View) i$.next());
            }
            if (this.mAdapter != null) {
                this.mAdapter.refreshList();
                updateGrid(this.mRoot);
            }
        }
    }

    private String buildDeleteMessage() {
        String deleteMessage = "";
        if (this.mSeletedItemInPosition.size() == 1) {
            return String.format(getContext().getResources().getString(R.string.delete_wallpaper_message), new Object[]{Integer.valueOf(this.mSeletedItemInPosition.size())});
        } else if (this.mSeletedItemInPosition.size() <= 1) {
            return deleteMessage;
        } else {
            return String.format(getContext().getResources().getString(R.string.delete_wallpapers_message), new Object[]{Integer.valueOf(this.mSeletedItemInPosition.size())});
        }
    }

    private void confirmDelete(Context context, DialogInterface.OnClickListener deleteListener, CharSequence message) {
        WallpaperUtils.showConfirmAlert(context, true, deleteListener, null, context.getString(R.string.delete), message, R.string.delete, 17039360);
    }

    private void exitEditMode() {
        if (this.mModeCallback != null && this.mIsInSelectionMode) {
            this.mModeCallback = null;
            this.mInitPosition = -1;
            this.mIsInSelectionMode = false;
            this.mActionMode.finish();
            this.mActionMode = null;
            setItemOnClickListener(this.mNormalClickListener);
            checkNothing();
        }
    }

    private void checkNothing() {
        GridLayout grid = getGridView(this.mRoot);
        for (int i = 0; i < grid.getChildCount(); i++) {
            selectItem(i, false, true);
        }
        this.mIsInSelectingAll = false;
    }

    private void checkAll() {
        GridLayout grid = getGridView(this.mRoot);
        for (int i = 0; i < grid.getChildCount(); i++) {
            selectItem(i, true, true);
        }
        this.mIsInSelectingAll = true;
    }

    private void selectItem(int pos, boolean selected, boolean ignoreToast) {
        View item = getGridView(this.mRoot).getChildAt(pos);
        if (!WallpaperUtils.isSystemPresetWallpaper((String) item.getTag())) {
            ImageView selectedIcon = (ImageView) item.findViewById(R.id.selected);
            if (selectedIcon != null) {
                if (selected) {
                    this.mSeletedItemInPosition.add(Integer.valueOf(pos));
                    selectedIcon.setVisibility(0);
                    selectedIcon.setImageResource(R.drawable.icon_selection_s);
                } else {
                    this.mSeletedItemInPosition.remove(Integer.valueOf(pos));
                    selectedIcon.setVisibility(4);
                    selectedIcon.setImageDrawable(null);
                }
            }
            updateActionMode();
        } else if (!ignoreToast) {
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.is_system_wallpaper_message), 200).show();
        }
    }

    private void updateActionMode() {
        boolean z = true;
        if (this.mActionMode != null && this.mActionbBarMenu != null) {
            this.mActionMode.setTitle(String.format(getContext().getResources().getString(R.string.editing_mode_title), new Object[]{Integer.valueOf(this.mSeletedItemInPosition.size())}));
            MenuItem menuDelete = this.mActionbBarMenu.findItem(R.id.action_delete);
            if (this.mSeletedItemInPosition.size() <= 0) {
                z = false;
            }
            menuDelete.setEnabled(z);
        }
    }

    private void setItemOnClickListener(OnClickListener clickListener) {
        GridLayout grid = getGridView(this.mRoot);
        for (int i = 0; i < grid.getChildCount(); i++) {
            grid.getChildAt(i).setOnClickListener(clickListener);
        }
    }

    public ImageListPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mShowingAllItems = false;
        this.mEnableSelectMode = false;
        this.mIsInSelectionMode = false;
        this.mIsInSelectingAll = false;
        this.mSeletedItemInPosition = new HashSet();
        this.mInitPosition = -1;
        this.mNormalClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (ImageListPreference.this.mItemClickListener != null) {
                    ImageListPreference.this.mItemClickListener.onItemClick(((Integer) v.getTag(R.layout.image_grid_list)).intValue());
                }
            }
        };
        this.mEditModeClickListener = new OnClickListener() {
            public void onClick(View v) {
                boolean hasSelected;
                boolean z = true;
                int position = ((Integer) v.getTag(R.layout.image_grid_list)).intValue();
                if (((ImageView) ImageListPreference.this.getGridView(ImageListPreference.this.mRoot).getChildAt(position).findViewById(R.id.selected)).getVisibility() == 0) {
                    hasSelected = true;
                } else {
                    hasSelected = false;
                }
                ImageListPreference imageListPreference = ImageListPreference.this;
                if (hasSelected) {
                    z = false;
                }
                imageListPreference.selectItem(position, z, false);
            }
        };
        this.mLongClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (ImageListPreference.this.mIsInSelectionMode || !ImageListPreference.this.mEnableSelectMode) {
                    return false;
                }
                ImageListPreference.this.mInitPosition = ((Integer) v.getTag(R.layout.image_grid_list)).intValue();
                GridLayout grid = ImageListPreference.this.getGridView(ImageListPreference.this.mRoot);
                ImageListPreference.this.mModeCallback = new EditModeWrapper();
                grid.startActionMode(ImageListPreference.this.mModeCallback);
                return true;
            }
        };
        this.deleteListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ImageListPreference.this.onDelete();
                ImageListPreference.this.exitEditMode();
            }
        };
        setLayoutResource(R.layout.image_grid_list);
        this.mItemPadding = context.getResources().getDimensionPixelSize(R.dimen.setting_wallpaper_image_padding);
    }

    public ImageListPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageListPreference(Context context) {
        this(context, null);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_all:
                if (this.mAdapter != null) {
                    this.mShowingAllItems = true;
                    updateGrid(this.mRoot);
                    return;
                }
                return;
            default:
                return;
        }
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.mRoot = view;
        this.mShowAllBtn = getShowAllBtn(view);
        this.mShowAllBtn.setOnClickListener(this);
        view.setPadding(50, view.getPaddingTop(), 50, view.getPaddingBottom());
        updateGrid(view);
    }

    public View getView(View convertView, ViewGroup parent) {
        if (this.mRoot == null) {
            return super.getView(convertView, parent);
        }
        return this.mRoot;
    }

    public void setAdapter(ThumbnailViewAdapter adapter) {
        this.mAdapter = adapter;
        update();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    public void update() {
        if (this.mRoot != null) {
            updateGrid(this.mRoot);
        }
    }

    private TextView getShowAllBtn(View root) {
        return (TextView) root.findViewById(R.id.show_all);
    }

    private GridLayout getGridView(View root) {
        return (GridLayout) root.findViewById(16908298);
    }

    private void updateGrid(View root) {
        GridLayout grid = getGridView(root);
        if (this.mAdapter == null || grid.getColumnCount() == 0) {
            grid.removeAllViews();
            return;
        }
        int adapterCount;
        if (this.mAdapter.getCount() <= 10 || this.mShowingAllItems) {
            this.mShowAllBtn.setVisibility(8);
        } else {
            this.mShowAllBtn.setVisibility(0);
        }
        if (this.mShowingAllItems) {
            adapterCount = Math.min(this.mAdapter.getCount(), 100);
        } else {
            adapterCount = Math.min(this.mAdapter.getCount(), 10);
        }
        int childCount = grid.getChildCount();
        int reuseCount = Math.min(adapterCount, childCount);
        int index = 0;
        while (index < reuseCount) {
            View v = grid.getChildAt(index);
            if (v == this.mAdapter.getView(index, v, grid)) {
                v.setTag(R.layout.image_grid_list, Integer.valueOf(index));
                index++;
            } else {
                throw new UnsupportedOperationException("convert view must be reused!");
            }
        }
        while (index < adapterCount) {
            v = this.mAdapter.getView(index, null, grid);
            initItem(v);
            grid.addView(v);
            v.setClickable(true);
            v.setLongClickable(true);
            v.setOnClickListener(this.mIsInSelectionMode ? this.mEditModeClickListener : this.mNormalClickListener);
            v.setOnLongClickListener(this.mLongClickListener);
            v.setTag(R.layout.image_grid_list, Integer.valueOf(index));
            index++;
        }
        if (index < childCount) {
            grid.removeViews(index, childCount - index);
        }
        grid.setBackgroundResource(0);
        grid.requestLayout();
    }

    private void initItem(View v) {
        Spec rowSpec = GridLayout.spec(Integer.MIN_VALUE, 1, GridLayout.CENTER);
        Spec columnSpec = GridLayout.spec(Integer.MIN_VALUE, 1, GridLayout.CENTER);
        LayoutParams lp = v.getLayoutParams();
        GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, columnSpec);
        if (lp != null) {
            params.height = lp.height;
            params.width = lp.width;
        }
        int i = this.mItemPadding / 2;
        params.rightMargin = i;
        params.leftMargin = i;
        i = this.mItemPadding / 2;
        params.bottomMargin = i;
        params.topMargin = i;
        v.setLayoutParams(params);
    }
}
