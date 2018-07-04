package com.miui.home.launcher;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.FolderInfo.RecommendInfo;
import java.util.ArrayList;

public class RecommendScreen extends LinearLayout implements OnClickListener, DragSource {
    private Launcher mLauncher;
    private RecommendAppsThumbnailView mRecommendAppsView;
    private RecommendBannerScreenView mRecommendBannerView;
    private ArrayList<ShortcutInfo> mRecommendDefaultContents = new ArrayList();
    private FrameLayout mRecommendHeader;
    private RecommendInfo mRecommendInfo;
    private TextView mRecommendTitle;
    private ImageView mRefresh;

    public RecommendScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        this.mRecommendHeader = (FrameLayout) findViewById(R.id.folder_recommend_header);
        this.mRecommendHeader.getBackground().setAlpha(0);
        this.mRecommendTitle = (TextView) findViewById(R.id.recommend_title);
        if (DeviceConfig.isLayoutRtl() && VERSION.SDK_INT < 21) {
            this.mRecommendTitle.setGravity(5);
        }
        this.mRefresh = (ImageView) findViewById(R.id.refresh);
        this.mRefresh.setOnClickListener(this);
        this.mRecommendAppsView = (RecommendAppsThumbnailView) findViewById(R.id.recommend_apps);
        this.mRecommendBannerView = (RecommendBannerScreenView) findViewById(R.id.recommend_banner);
    }

    public void setLauncher(Launcher launcher) {
        this.mLauncher = launcher;
        this.mRecommendAppsView.setLauncher(launcher);
        initDefaultDisplayContents(launcher);
    }

    void bind(FolderInfo folderInfo) {
        this.mRecommendInfo = folderInfo.getRecommendInfo(this.mLauncher);
        if (this.mRecommendInfo == null) {
            setContentAdapter(null);
        }
    }

    public void init() {
        if (this.mRecommendInfo != null) {
            setContentAdapter(this.mRecommendInfo.getAdapter());
            this.mRecommendBannerView.loadContent(this.mRecommendInfo.getBannerList());
            if (this.mRecommendBannerView.getScreenCount() > 0) {
                this.mRecommendBannerView.setVisibility(0);
                this.mRecommendBannerView.setNextView(this.mRecommendAppsView);
                this.mRecommendAppsView.setVisibility(4);
                this.mRecommendAppsView.invalidate();
                this.mRecommendAppsView.setPreView(this.mRecommendBannerView);
                this.mRecommendBannerView.setTranslationX(0.0f);
            } else {
                this.mRecommendBannerView.setVisibility(4);
                this.mRecommendAppsView.setVisibility(0);
                this.mRecommendAppsView.invalidate();
                this.mRecommendAppsView.setPreView(null);
            }
            if (TextUtils.isEmpty(this.mRecommendInfo.mRecommendTitle)) {
                this.mRecommendTitle.setText(R.string.recommend_apps_notice);
            } else {
                this.mRecommendTitle.setText(this.mRecommendInfo.mRecommendTitle);
            }
        }
    }

    public RecommendInfo getRecommendInfo() {
        return this.mRecommendInfo;
    }

    public TextView getRecommendTitle() {
        return this.mRecommendTitle;
    }

    void setContentAdapter(RecommendShortcutsAdapter adapter) {
        if (adapter != null) {
            adapter.setLauncher(this.mLauncher);
            getRecommendInfo().getAdapter().initRecommendList();
        }
        this.mRecommendAppsView.setAdapter(adapter);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                this.mRefresh.setRotation(0.0f);
                this.mRefresh.animate().cancel();
                this.mRefresh.animate().rotation(-360.0f).setDuration(600).setInterpolator(new LinearInterpolator()).start();
                doRefresh();
                return;
            default:
                return;
        }
    }

    public void doRefresh() {
        if (this.mRecommendInfo.count() > 4) {
            refreshDisplayContents();
            if (this.mRecommendInfo.getCacheEndTime() < System.currentTimeMillis()) {
                this.mRecommendInfo.requestDataFromMarket();
                return;
            }
            return;
        }
        this.mRecommendInfo.initRecommendViewAndRequest();
    }

    public void refreshDisplayContents() {
        if (!this.mRecommendBannerView.snapToNextScreen()) {
            this.mRecommendAppsView.snapToNextScreen();
        }
    }

    public void onAppStartDownload(String appId) {
        if (this.mRecommendAppsView.getVisibility() == 0) {
            this.mRecommendAppsView.onAppStartDownload(appId);
        }
    }

    public void snapToAppView(String appId) {
        if (this.mRecommendAppsView.getVisibility() == 0) {
            this.mRecommendAppsView.snapToAppView(appId);
        }
    }

    public void onDragCompleted(DropTarget target, DragObject d) {
    }

    public void onDropBack(DragObject d) {
        if (!this.mRecommendInfo.contains((ShortcutInfo) d.getDragInfo())) {
            this.mRecommendInfo.add((ShortcutInfo) d.getDragInfo(), true);
        }
    }

    public void setContentAlpha(float alpha) {
        this.mRecommendHeader.setAlpha(alpha);
        this.mRecommendAppsView.setAlpha(alpha);
    }

    public void setHeaderBgRes(int resId) {
        this.mRecommendHeader.setBackgroundResource(resId);
        this.mRecommendHeader.getBackground().setAlpha(0);
    }

    public RecommendAppsThumbnailView getContent() {
        return this.mRecommendAppsView;
    }

    private void initDefaultDisplayContents(Launcher launcher) {
        for (int i = 0; i < 4; i++) {
            ShortcutInfo info = new ShortcutInfo();
            info.itemType = 13;
            info.intent = new Intent();
            info.setTitle(DeviceConfig.sRecommendDefaultTitle, launcher);
            ShortcutIcon.fromXml(R.layout.recommend_icon, launcher, launcher.getFolderCling().getRecommendScreen().getContent(), info).enableDrawTouchMask(false);
            this.mRecommendDefaultContents.add(info);
        }
    }

    public ArrayList<ShortcutInfo> getRecommendDefaultContents() {
        return this.mRecommendDefaultContents;
    }
}
