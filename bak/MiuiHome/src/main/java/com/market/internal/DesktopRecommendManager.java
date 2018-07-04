package com.market.internal;

import android.os.RemoteException;
import android.util.Log;
import com.market.sdk.DesktopRecommendCallback;
import com.market.sdk.DesktopRecommendInfo;
import com.market.sdk.IDesktopRecommendResponse.Stub;
import com.market.sdk.IMarketService;
import com.market.sdk.RemoteMethodInvoker;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DesktopRecommendManager {
    private static Set<Long> sLoadingRequest = new HashSet();

    private static class DesktopRecomendResponse extends Stub {
        private DesktopRecommendCallback mCallback;
        private long mFolderId;

        public DesktopRecomendResponse(long folderId, DesktopRecommendCallback callback) {
            this.mFolderId = folderId;
            this.mCallback = callback;
        }

        public void onLoadSuccess(DesktopRecommendInfo info) {
            DesktopRecommendManager.sLoadingRequest.remove(Long.valueOf(this.mFolderId));
            if (this.mCallback != null) {
                this.mCallback.onLoadSuccess(info);
            }
        }

        public void onLoadFailed() {
            DesktopRecommendManager.sLoadingRequest.remove(Long.valueOf(this.mFolderId));
            if (this.mCallback != null) {
                this.mCallback.onLoadFailed();
            }
        }
    }

    public static void loadDesktopRecommendInfo(long folderId, String folderName, List<String> pkgNameList, DesktopRecommendCallback callback) {
        synchronized (sLoadingRequest) {
            if (!sLoadingRequest.contains(Long.valueOf(folderId))) {
                sLoadingRequest.add(Long.valueOf(folderId));
                final long j = folderId;
                final DesktopRecommendCallback desktopRecommendCallback = callback;
                final String str = folderName;
                final List<String> list = pkgNameList;
                new RemoteMethodInvoker<Void>() {
                    public Void innerInvoke(IMarketService s) throws RemoteException {
                        try {
                            IMarketService iMarketService = s;
                            iMarketService.loadDesktopRecommendInfo(j, str, list, new DesktopRecomendResponse(j, desktopRecommendCallback));
                        } catch (Exception e) {
                            Log.e("MarketManager", "Exception when load desktop recommend info : " + e);
                        }
                        return null;
                    }
                }.invokeAsync();
            }
        }
    }
}
