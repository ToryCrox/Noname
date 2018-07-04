package com.miui.home.launcher.upsidescene;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.DragController;
import com.miui.home.launcher.DragObject;
import com.miui.home.launcher.DragSource;
import com.miui.home.launcher.DropTarget;
import com.miui.home.launcher.ItemInfo;
import com.miui.home.launcher.LauncherAppWidgetProviderInfo;
import com.miui.home.launcher.ScreenView;
import java.util.List;

public class BottomItemsDragSource extends ScreenView implements DragSource {
    boolean mIsShowName;
    SceneScreen mSceneScreen;

    public static class ItemData {
        public Drawable icon;
        public ItemInfo itemInfo;
        public String title;

        public ItemData(Drawable icon, String title, ItemInfo itemInfo) {
            this.icon = icon;
            this.title = title;
            this.itemInfo = itemInfo;
        }
    }

    class MyAdapter extends BaseAdapter {
        List<ItemData> mItemDatas;

        public MyAdapter(List<ItemData> itemDatas) {
            this.mItemDatas = itemDatas;
        }

        public int getCount() {
            return this.mItemDatas.size();
        }

        public Object getItem(int position) {
            return Integer.valueOf(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ItemData itemData = (ItemData) this.mItemDatas.get(position);
            ViewGroup itemView = (ViewGroup) View.inflate(BottomItemsDragSource.this.mContext, R.layout.free_style_edit_bar_item, null);
            TextView txtName = (TextView) itemView.findViewById(R.id.txtName);
            final ImageView imgPreview = (ImageView) itemView.findViewById(R.id.imgPreview);
            txtName.setText(itemData.title);
            if (!BottomItemsDragSource.this.mIsShowName) {
                txtName.setVisibility(8);
            }
            itemView.setTag(itemData.itemInfo);
            imgPreview.setImageDrawable(itemData.icon);
            itemView.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    BottomItemsDragSource.this.doDrag(imgPreview, itemData.icon, itemData.itemInfo);
                    return true;
                }
            });
            return itemView;
        }
    }

    private void doDrag(View v, Drawable icon, ItemInfo itemInfo) {
        Drawable drawable = null;
        if (itemInfo instanceof LauncherAppWidgetProviderInfo) {
            LauncherAppWidgetProviderInfo widgetInfo = (LauncherAppWidgetProviderInfo) itemInfo;
            Drawable d = this.mContext.getPackageManager().getDrawable(widgetInfo.providerInfo.provider.getPackageName(), widgetInfo.providerInfo.previewImage, null);
            if (d != null) {
                drawable = d;
            }
        }
        if (drawable == null && icon != null) {
            drawable = icon.getConstantState().newDrawable();
        }
        float scale = (this.mSceneScreen.getEditModeScaleFactor() * ((float) drawable.getIntrinsicWidth())) / ((float) v.getWidth());
        drawable.setBounds(0, 0, v.getWidth(), (int) (((this.mSceneScreen.getEditModeScaleFactor() * ((float) drawable.getIntrinsicHeight())) / scale) + 0.5f));
        int[] loc = new int[2];
        this.mSceneScreen.getLauncher().getDragLayer().getLocationInDragLayer(v, loc, false);
        int dragLayerX = loc[0];
        this.mSceneScreen.getLauncher().getDragController().startDrag(drawable, itemInfo, dragLayerX + ((v.getWidth() / 2) - (drawable.getBounds().width() / 2)), loc[1] + ((v.getHeight() / 2) - (drawable.getBounds().height() / 2)), scale, (DragSource) this, 2);
        this.mSceneScreen.onExternalDragStart();
    }

    public BottomItemsDragSource(Context context, SceneScreen sceneScreen, boolean isShowName) {
        super(context);
        this.mSceneScreen = sceneScreen;
        this.mIsShowName = isShowName;
        setScrollWholeScreen(true);
        setSlideBarPosition(new LayoutParams(-1, -2, 83), R.drawable.free_style_edit_bottom_bar_slide_bar, R.drawable.free_style_edit_bottom_bar_slide_bar_bg, true);
    }

    public void setItemDatas(List<ItemData> itemDatas) {
        boolean onlyOnePage;
        int i = 0;
        MyAdapter adapter = new MyAdapter(itemDatas);
        for (int i2 = 0; i2 < adapter.getCount(); i2++) {
            addView(adapter.getView(i2, null, null), getResources().getDisplayMetrics().widthPixels / 3, -1);
        }
        if (adapter.getCount() <= 3) {
            onlyOnePage = true;
        } else {
            onlyOnePage = false;
        }
        if (onlyOnePage) {
            i = 8;
        }
        setSlideBarVisibility(i);
    }

    public void setDragController(DragController dragger) {
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
        this.mSceneScreen.onExternalDragEnd();
    }

    public void onDropBack(DragObject d) {
    }
}
