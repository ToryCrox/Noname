package com.miui.home.launcher;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import com.miui.home.R;
import java.util.List;

public abstract class IconsSelectView extends FrameLayout {
    protected GridView mAppsGrid = ((GridView) findViewById(R.id.folder_content));
    private TextView mBtnCancel = ((TextView) findViewById(R.id.btnCancel));
    private TextView mBtnOK = ((TextView) findViewById(R.id.btnOk));
    private boolean mCanSelectMultiple;
    private View mContainer = findViewById(R.id.container);
    protected Object mSelectedObject;
    protected TextView mTitle = ((TextView) findViewById(R.id.title));

    public class MyAdapter extends BaseAdapter implements OnItemClickListener {
        List mList;

        public MyAdapter(List list) {
            this.mList = list;
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int position) {
            return this.mList.get(position);
        }

        public boolean hasStableIds() {
            return true;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return IconsSelectView.this.getItemView(position, convertView, parent, this.mList.get(position));
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (IconsSelectView.this.mCanSelectMultiple) {
                IconsSelectView.this.setSelected((ViewGroup) view, IconsSelectView.this.mAppsGrid.isItemChecked(position));
                IconsSelectView.this.updateTitle();
                return;
            }
            IconsSelectView.this.mSelectedObject = view.getTag();
            IconsSelectView.this.ok();
        }
    }

    public abstract void cancel();

    protected abstract View getItemView(int i, View view, ViewGroup viewGroup, Object obj);

    public abstract void ok();

    protected abstract void updateTitle();

    public IconsSelectView(Context context, boolean canSelectMultiple) {
        super(context);
        this.mCanSelectMultiple = canSelectMultiple;
        inflate(context, R.layout.free_style_apps_select, this);
        ViewGroup buttonsContainer = (ViewGroup) findViewById(R.id.buttons_container);
        if (!canSelectMultiple) {
            buttonsContainer.removeAllViews();
            buttonsContainer.setBackgroundResource(R.drawable.free_style_apps_single_select_bottom_bg);
        }
        this.mBtnOK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                IconsSelectView.this.ok();
            }
        });
        this.mBtnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                IconsSelectView.this.cancel();
            }
        });
        post(new Runnable() {
            public void run() {
                IconsSelectView.this.setFocusable(true);
                IconsSelectView.this.setFocusableInTouchMode(true);
                IconsSelectView.this.requestFocus();
            }
        });
    }

    public boolean performClick() {
        cancel();
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        cancel();
        return true;
    }

    public boolean canMultiSelect() {
        return this.mCanSelectMultiple;
    }

    protected void setSelected(ViewGroup icon, boolean isSelected) {
        if (icon != null) {
            if (isSelected) {
                icon.setBackgroundResource(R.drawable.free_style_apps_application_bg_s);
                icon.findViewById(R.id.selector).setBackgroundResource(R.drawable.icon_selection_s);
                return;
            }
            icon.setBackgroundDrawable(null);
            icon.findViewById(R.id.selector).setBackgroundDrawable(null);
        }
    }
}
