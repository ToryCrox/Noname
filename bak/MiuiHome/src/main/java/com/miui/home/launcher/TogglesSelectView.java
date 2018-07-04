package com.miui.home.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.IconsSelectView.MyAdapter;
import java.util.List;
import miui.app.ToggleManager;

public class TogglesSelectView extends IconsSelectView {
    Launcher mLauncher;

    public TogglesSelectView(Context context, Launcher launcher) {
        super(context, false);
        this.mLauncher = launcher;
        init();
    }

    private void init() {
        List<Integer> toggleIds = ToggleManager.getAllToggles(this.mContext);
        toggleIds.remove(Integer.valueOf(0));
        MyAdapter adapter = new MyAdapter(toggleIds);
        this.mAppsGrid.setAdapter(adapter);
        this.mAppsGrid.setOnItemClickListener(adapter);
        updateTitle();
    }

    protected View getItemView(int position, View convertView, ViewGroup parent, Object obj) {
        View app;
        int toggleId = ((Integer) obj).intValue();
        if (convertView != null) {
            app = convertView;
        } else {
            app = LayoutInflater.from(getContext()).inflate(R.layout.free_style_apps_application, null, false);
        }
        TextView label = (TextView) app.findViewById(R.id.title);
        label.setText(ToggleManager.getName(toggleId));
        setSelected((ViewGroup) app, this.mAppsGrid.isItemChecked(position));
        label.setCompoundDrawablesWithIntrinsicBounds(0, ToggleManager.getGeneralImage(toggleId), 0, 0);
        app.setTag(Integer.valueOf(toggleId));
        return app;
    }

    public void ok() {
        this.mLauncher.completeSelectToggle(((Integer) this.mSelectedObject).intValue());
        this.mLauncher.exitTogglesSelectView(false);
    }

    public void cancel() {
        this.mLauncher.exitTogglesSelectView(true);
    }

    protected void updateTitle() {
        this.mTitle.setText(R.string.toggle_select_title);
    }
}
