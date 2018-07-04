package com.miui.home.launcher;

import android.content.Context;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miui.home.R;

public class FolderContainer extends FrameLayout {
    private LinearLayout mRecommendAppsSwitch;

    public FolderContainer(Context context) {
        this(context, null);
    }

    public FolderContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public FolderContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected void onFinishInflate() {
        setClipChildren(false);
        setClipToPadding(false);
        super.onFinishInflate();
        this.mRecommendAppsSwitch = (LinearLayout) findViewById(R.id.recommend_apps_switch);
        if (DeviceConfig.isLayoutRtl() && VERSION.SDK_INT < 21) {
            ((TextView) this.mRecommendAppsSwitch.findViewById(R.id.switch_title)).setGravity(5);
        }
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
