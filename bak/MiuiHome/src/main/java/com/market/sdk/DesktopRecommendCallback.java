package com.market.sdk;

public interface DesktopRecommendCallback {
    void onLoadFailed();

    void onLoadSuccess(DesktopRecommendInfo desktopRecommendInfo);
}
