package com.miui.home.launcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.miui.home.R;
import com.miui.home.launcher.FolderInfo.RecommendInfo;
import java.util.ArrayList;
import miui.maml.animation.interpolater.CubicEaseInInterpolater;
import miui.maml.animation.interpolater.CubicEaseOutInterpolater;

public class RecommendShortcutsAdapter extends ThumbnailViewAdapter {
    private final Launcher mLauncher;
    private RecommendInfo mRecommendInfo;
    private ArrayList<ShortcutInfo> mRecommendList = new ArrayList();

    public RecommendShortcutsAdapter(Context context, RecommendInfo info) {
        super(context);
        this.mLauncher = Application.getLauncherApplication(context).getLauncher();
        this.mRecommendInfo = info;
        refreshList(false);
    }

    public void initRecommendList() {
        if (this.mRecommendList.size() < 4) {
            this.mRecommendList.clear();
            this.mRecommendList.addAll(this.mRecommendInfo.mRecommendDefaultContents);
        }
    }

    public void refreshList(boolean requestMore) {
        this.mRecommendList.clear();
        if (this.mRecommendInfo.mRecommendAppsContents.size() > 4) {
            this.mRecommendList.addAll(this.mRecommendInfo.mRecommendAppsContents);
        } else {
            if (requestMore) {
                this.mRecommendInfo.requestDataFromMarket();
            }
            this.mRecommendList.addAll(this.mRecommendInfo.mRecommendAppsContents);
        }
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        for (int i = 0; i < getCount(); i++) {
            ShortcutInfo info = (ShortcutInfo) this.mRecommendList.get(i);
            if (DeviceConfig.sRecommendDefaultTitle.equals(info.getTitle(null))) {
                info.setBuddyIconView(null, null);
            }
        }
        super.notifyDataSetChanged();
    }

    public void remove(ShortcutInfo info) {
        this.mRecommendList.remove(info);
        if (this.mRecommendList.size() < 4) {
            refreshList(true);
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.mRecommendList.size();
    }

    public View getItem(int position) {
        return null;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mLauncher.getFolderCling().isOpened()) {
            return null;
        }
        View icon;
        ShortcutInfo info = (ShortcutInfo) this.mRecommendList.get(position);
        boolean isDefaultItem = DeviceConfig.sRecommendDefaultTitle.equals(info.getTitle(null));
        boolean refreshDefault = false;
        if (convertView == null || !(convertView instanceof ShortcutIcon) || isDefaultItem) {
            icon = ShortcutIcon.fromXml(R.layout.recommend_icon, this.mLauncher, parent, info);
        } else {
            icon = (ShortcutIcon) convertView;
            if (DeviceConfig.sRecommendDefaultTitle.equals(((ShortcutInfo) icon.getTag()).getTitle(null))) {
                refreshDefault = true;
            }
            info.setBuddyIconView(icon, parent);
            icon.updateInfo(this.mLauncher, info);
        }
        icon.setLayerType(icon.getDefaultLayerType(), null);
        ImageView iconView = (ImageView) icon.findViewById(R.id.icon_icon);
        FrameLayout loadingView = (FrameLayout) icon.findViewById(R.id.loading_container);
        final TextView title = (TextView) icon.findViewById(R.id.icon_title);
        final CharSequence newTitle = title.getText();
        if (refreshDefault && !isDefaultItem) {
            loadingView.setVisibility(0);
            loadingView.animate().alpha(0.0f).setDuration(460).start();
            iconView.setVisibility(0);
            iconView.setAlpha(0.0f);
            iconView.setScaleX(0.7f);
            iconView.setScaleY(0.7f);
            iconView.animate().alpha(1.0f).scaleX(1.0f).scaleY(1.0f).setDuration(460).start();
            title.setText(DeviceConfig.sRecommendDefaultTitle);
            ObjectAnimator animOut = ObjectAnimator.ofFloat(title, "alpha", new float[]{0.5f, 0.0f});
            animOut.setInterpolator(new CubicEaseOutInterpolater());
            animOut.setDuration(230);
            animOut.start();
            ObjectAnimator animIn = ObjectAnimator.ofFloat(title, "alpha", new float[]{0.0f, 1.0f});
            animIn.setInterpolator(new CubicEaseInInterpolater());
            animIn.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    title.setText(newTitle);
                }
            });
            animIn.setDuration(230);
            animIn.setStartDelay(230);
            animIn.start();
        }
        if (isDefaultItem) {
            iconView.setVisibility(8);
            title.setAlpha(0.5f);
            icon.startLoadingAnim();
        } else {
            iconView.setVisibility(0);
            loadingView.setVisibility(8);
        }
        icon.setSkipNextAutoLayoutAnimation(true);
        icon.setScaleX(0.8f);
        icon.setScaleY(0.8f);
        icon.setAlpha(1.0f);
        return icon;
    }
}
